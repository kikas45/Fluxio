package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import android.net.ConnectivityManager
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

private const val PROGRESS_POLL_MS   = 200L
private const val WATCHDOG_POLL_MS   = 1_000L
private const val BUFFERING_STUCK_MS = 4_000L

/**
 * Single video page in the TikTok feed.
 *
 * ── SEEKBAR: draggable with live time label ───────────────────────────────────
 *
 * The bottom bar is now an interactive seek bar with three visual states:
 *
 *   IDLE (not dragging, not scrolling):
 *     • Thin 2dp track — same as before, unobtrusive.
 *     • No thumb visible — clean TikTok look.
 *
 *   DRAGGING:
 *     • Track expands to 4dp height.
 *     • A circular thumb appears at the drag position.
 *     • A pill-shaped time label floats above the thumb showing
 *       "MM:SS" (< 1 hour) or "HH:MM:SS" (≥ 1 hour).
 *     • Playback is paused for the duration of the drag.
 *     • The progress poll is still running but its writes to `progress`
 *       are gated by `isDragging` — so the track position is frozen at
 *       `dragProgress` during the drag and never fights the poll.
 *
 *   ON DRAG END:
 *     • player.seekTo(targetMs) is called once.
 *     • If the video was playing before the drag started, playback resumes.
 *     • Time label fades out via AnimatedVisibility.
 *     • Track shrinks back to 2dp.
 *
 * ── CACHE INTERACTION ─────────────────────────────────────────────────────────
 *
 * Seeking into an uncached region is handled transparently by ExoPlayer:
 *
 *   • CacheDataSource with FLAG_BLOCK_ON_CACHE serves any bytes already on disk.
 *   • FLAG_IGNORE_CACHE_ON_ERROR falls through to network for any gap.
 *   • ExoPlayer's internal DASH segment loading will request only the segments
 *     starting at the seek target — it does NOT re-download earlier segments.
 *
 * So a drag from 2s → 30s causes exactly one new HTTP range request starting
 * at the DASH segment containing 30s. Bytes from 2s–30s are simply never
 * fetched (a gap in cache). If the user returns, the gap is filled by network
 * on demand — exactly option 1 from the design notes.
 *
 * No changes to VideoCache, CacheFactory, or VideoPreloader are required.
 *
 * ── INTERACTION CONFLICT AVOIDANCE ───────────────────────────────────────────
 *
 * The seek bar sits inside the Box that has a .clickable for tap-to-pause.
 * To prevent horizontal drags on the seek bar from also triggering the click,
 * the seek bar's pointerInput block consumes pointer events independently via
 * detectHorizontalDragGestures, which only fires on actual horizontal movement.
 * A vertical swipe (page navigation) is not consumed here and propagates
 * normally to the pager's NestedScrollConnection.
 *
 * ── MEMORY FIX: ConnectivityManager hoisted out of the watchdog loop ─────────
 *
 * ConnectivityManager is a process-scoped singleton; fetching it once at
 * composable entry and sharing the reference is both correct and cheaper.
 */
@OptIn(UnstableApi::class)
@Composable
fun TikTokVideoItem(
    player      : ExoPlayer,
    isPlaying   : Boolean,
    isScrolling : Boolean,
    onPause     : () -> Unit,
    onResume    : () -> Unit,
    modifier    : Modifier = Modifier,
) {
    val context = LocalContext.current

    // Hoisted once — not fetched inside any loop or lambda.
    val cm = remember(context) {
        context.getSystemService(ConnectivityManager::class.java)
    }

    var manuallyPaused      by remember { mutableStateOf(false) }
    var progress            by remember { mutableFloatStateOf(0f) }
    var isBufferingMidVideo by remember { mutableStateOf(false) }
    var errorWhilePlaying   by remember { mutableStateOf(false) }

    // ── Seek bar state ────────────────────────────────────────────────────────
    var isDragging          by remember { mutableStateOf(false) }
    var dragProgress        by remember { mutableFloatStateOf(0f) }
    // Whether the video was playing when the drag started — so we can resume.
    var wasPlayingOnDragStart by remember { mutableStateOf(false) }
    // Pixel width of the seek bar track, measured via onSizeChanged.
    var trackWidthPx        by remember { mutableIntStateOf(0) }

    val currentIsPlaying      by rememberUpdatedState(isPlaying)
    val currentManuallyPaused by rememberUpdatedState(manuallyPaused)

    // ── Derived: which progress value drives the track ────────────────────────
    // During drag: dragProgress (frozen at thumb position).
    // During normal play: progress (updated by poll every 200ms).
    val displayProgress = if (isDragging) dragProgress else progress

    // ── Drive playback from isPlaying ─────────────────────────────────────────
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            player.playWhenReady = true
            player.play()
        } else {
            player.pause()
            player.playWhenReady = false
            manuallyPaused        = false
            isBufferingMidVideo   = false
            errorWhilePlaying     = false
            // Also cancel any in-progress drag when page is no longer current.
            isDragging            = false
        }
    }

    // ── Player state listener ─────────────────────────────────────────────────
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        isBufferingMidVideo = false
                        errorWhilePlaying   = false
                        if (currentIsPlaying && !currentManuallyPaused && !player.isPlaying) {
                            player.playWhenReady = true
                            player.play()
                        }
                    }
                    Player.STATE_BUFFERING -> {
                        if (player.duration > 0 && !currentManuallyPaused) {
                            isBufferingMidVideo = true
                        }
                    }
                    Player.STATE_IDLE -> {
                        if (currentIsPlaying && !currentManuallyPaused) {
                            isBufferingMidVideo = true
                        }
                    }
                    Player.STATE_ENDED -> {
                        isBufferingMidVideo = false
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                if (currentIsPlaying && !currentManuallyPaused) {
                    errorWhilePlaying   = true
                    isBufferingMidVideo = true
                }
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    // ── Watchdog ──────────────────────────────────────────────────────────────
    LaunchedEffect(player, isPlaying) {
        if (!isPlaying) return@LaunchedEffect

        var lastPosition       = -1L
        var positionStuckSince = 0L

        while (isActive) {
            delay(WATCHDOG_POLL_MS)

            if (!currentIsPlaying || currentManuallyPaused) {
                lastPosition       = -1L
                positionStuckSince = 0L
                continue
            }

            val state    = player.playbackState
            val position = player.currentPosition
            val duration = player.duration
            val now      = System.currentTimeMillis()

            // Case B: post-error STATE_IDLE — ExoPlayer gave up.
            if (errorWhilePlaying && state == Player.STATE_IDLE) {
                if (cm.activeNetwork != null) {
                    player.prepare()
                    player.playWhenReady = true
                    player.play()
                    errorWhilePlaying   = false
                    isBufferingMidVideo = false
                    lastPosition        = -1L
                    positionStuckSince  = 0L
                }
                continue
            }

            // Case A: STATE_BUFFERING with position not advancing.
            if (state == Player.STATE_BUFFERING && duration > 0) {
                if (lastPosition < 0) lastPosition = position

                if (position == lastPosition) {
                    if (positionStuckSince == 0L) positionStuckSince = now
                    if (now - positionStuckSince >= BUFFERING_STUCK_MS && cm.activeNetwork != null) {
                        if (player.playbackState == Player.STATE_IDLE) player.prepare()
                        player.playWhenReady = true
                        player.play()
                        positionStuckSince = 0L
                    }
                } else {
                    lastPosition       = position
                    positionStuckSince = 0L
                }
            } else {
                lastPosition       = if (state == Player.STATE_READY) position else -1L
                positionStuckSince = 0L
                if (state == Player.STATE_READY) isBufferingMidVideo = false
            }
        }
    }

    // ── Lifecycle: lock screen pause / unlock resume ──────────────────────────
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE  -> {
                    player.pause()
                    player.playWhenReady = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (currentIsPlaying && !currentManuallyPaused) {
                        if (player.playbackState == Player.STATE_IDLE) player.prepare()
                        player.playWhenReady = true
                        player.play()
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.pause()
        }
    }

    // ── Progress poll ─────────────────────────────────────────────────────────
    // Gate writes by isDragging so the thumb position is never overwritten
    // by the poll while the user's finger is on screen.
    LaunchedEffect(player, isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        while (isActive) {
            val duration = player.duration.takeIf { it > 0 } ?: 0L
            val position = player.currentPosition
            if (duration > 0 && !isDragging) {
                progress = position.toFloat() / duration
            }
            delay(PROGRESS_POLL_MS)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) {
                // Only handle tap-to-pause if NOT dragging (drag end is not a tap).
                if (!isDragging) {
                    if (player.isPlaying) {
                        manuallyPaused = true
                        onPause()
                    } else {
                        manuallyPaused = false
                        onResume()
                    }
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode    = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                }
            },
            update = { view ->
                if (view.player !== player) view.player = player
            },
            modifier = Modifier.fillMaxSize()
        )

        DisposableEffect(player) {
            onDispose { player.clearVideoSurface() }
        }

        // ── Tap-to-pause icon ─────────────────────────────────────────────────
        if (manuallyPaused && !isScrolling) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .background(Color.Black.copy(alpha = 0.50f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.PlayArrow,
                    contentDescription = "Tap to resume",
                    tint               = Color.White,
                    modifier           = Modifier.size(46.dp)
                )
            }
        }

        // ── Buffering spinner ─────────────────────────────────────────────────
        if (isBufferingMidVideo && !isScrolling && !manuallyPaused && isPlaying) {
            CircularProgressIndicator(
                modifier    = Modifier.align(Alignment.Center).size(48.dp),
                color       = Color.White,
                strokeWidth = 3.dp,
            )
        }

        // ── Seek bar (replaces the old LinearProgressIndicator) ───────────────
        if (!isScrolling) {
            SeekBar(
                progress       = displayProgress,
                isDragging     = isDragging,
                trackWidthPx   = trackWidthPx,
                player         = player,
                modifier       = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { trackWidthPx = it.width }
                    .pointerInput(player) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                if (trackWidthPx <= 0) return@detectHorizontalDragGestures
                                isDragging            = true
                                wasPlayingOnDragStart = player.isPlaying
                                // Initialise dragProgress from where the finger lands,
                                // not the current playhead — feels more direct.
                                dragProgress = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                                // Pause immediately so the frame freezes while scrubbing.
                                player.pause()
                                player.playWhenReady = false
                            },
                            onHorizontalDrag = { change, _ ->
                                if (trackWidthPx <= 0) return@detectHorizontalDragGestures
                                change.consume()
                                // Accumulate absolute position from the pointer's current x.
                                dragProgress = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                            },
                            onDragEnd = {
                                val duration = player.duration.takeIf { it > 0 } ?: 0L
                                if (duration > 0) {
                                    val targetMs = (dragProgress * duration).toLong()
                                    // Seek ExoPlayer to the new position.
                                    // ExoPlayer will:
                                    //   1. Serve cached bytes for any segment already on disk.
                                    //   2. Fall through to network for any gap (FLAG_IGNORE_CACHE_ON_ERROR).
                                    // No change to SimpleCache or VideoPreloader needed.
                                    player.seekTo(targetMs)
                                    // Sync the progress tracker so there is no visual jump
                                    // when the poll resumes writing after isDragging = false.
                                    progress = dragProgress
                                }
                                isDragging = false
                                // Resume only if the video was playing before the drag.
                                if (wasPlayingOnDragStart && !manuallyPaused && isPlaying) {
                                    if (player.playbackState == Player.STATE_IDLE) player.prepare()
                                    player.playWhenReady = true
                                    player.play()
                                }
                            },
                            onDragCancel = {
                                // Drag cancelled (e.g. pointer lifted mid-frame) —
                                // do NOT seek; just restore playback state.
                                isDragging = false
                                if (wasPlayingOnDragStart && !manuallyPaused && isPlaying) {
                                    if (player.playbackState == Player.STATE_IDLE) player.prepare()
                                    player.playWhenReady = true
                                    player.play()
                                }
                            }
                        )
                    }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SeekBar — stateless drawing component
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Custom seek bar drawn with Canvas-free Compose primitives.
 *
 * Layout (bottom of screen):
 *
 *   ┌─────────────────────────────────────────────────────────────────────┐
 *   │                  [  MM:SS  ]  ← time label (AnimatedVisibility)     │
 *   │  ──────────────●────────────────────────────────────────────────── │
 *   │  ↑ played      ↑ thumb                       ↑ remaining           │
 *   └─────────────────────────────────────────────────────────────────────┘
 *
 * The thumb and expanded track only appear while isDragging == true.
 * The time label uses AnimatedVisibility (fadeIn/fadeOut) for a clean UX.
 *
 * Time formatting:
 *   < 3600s  →  "M:SS"  (e.g.  "2:07")
 *   ≥ 3600s  →  "H:MM:SS" (e.g. "1:02:07")
 */
@Composable
private fun SeekBar(
    progress     : Float,
    isDragging   : Boolean,
    trackWidthPx : Int,
    player       : ExoPlayer,
    modifier     : Modifier = Modifier,
) {
    val duration = player.duration.takeIf { it > 0 } ?: 0L
    val positionMs = (progress * duration).toLong()

    // ── Time label text ───────────────────────────────────────────────────────
    val timeText = remember(positionMs) { formatMediaTime(positionMs) }

    // Thumb X offset in dp — derived from progress × track width.
    // We compute in px and convert so it stays in sync with the track.
    val thumbOffsetFraction = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            // Generous hit area — 48dp tall — standard Android touch target.
            // The visible track is only 2–4dp, centered vertically.
            .height(48.dp)
    ) {
        // ── Track ─────────────────────────────────────────────────────────────
        val trackHeight = if (isDragging) 4.dp else 2.dp

        // Background (remaining) track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.25f))
        )

        // Foreground (played) track
        Box(
            modifier = Modifier
                .fillMaxWidth(thumbOffsetFraction)
                .height(trackHeight)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White)
        )

        // ── Thumb + time label (only while dragging) ──────────────────────────
        AnimatedVisibility(
            visible = isDragging,
            enter   = fadeIn(),
            exit    = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                // Offset the thumb so its centre tracks the progress position.
                // We use fillMaxWidth fraction for the played track above, and
                // mirror that fraction for the thumb offset. Since Box alignment
                // is CenterStart, we shift by (progress × trackWidth) - thumbRadius.
                // Using padding here keeps everything in Compose layout space.
                .padding(
                    start = if (trackWidthPx > 0) {
                        val thumbRadiusDp = 6.dp
                        // Convert fraction to dp offset, clamped so thumb never bleeds.
                        val offsetPx = (thumbOffsetFraction * trackWidthPx).roundToInt()
                        val offsetDp = with(androidx.compose.ui.platform.LocalDensity.current) { offsetPx.toDp() }
                        (offsetDp - thumbRadiusDp).coerceAtLeast(0.dp)
                    } else 0.dp
                )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Time pill above thumb
                Text(
                    text       = timeText,
                    color      = Color.White,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier
                        .offset(y = (-28).dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                // Thumb dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Time formatting
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Format milliseconds to a human-readable time string.
 *
 * @param ms Milliseconds (non-negative)
 * @return  "M:SS" for durations under 1 hour, "H:MM:SS" for ≥ 1 hour.
 *
 * Examples:
 *   67_000  → "1:07"
 *   3723_000 → "1:02:03"
 */
private fun formatMediaTime(ms: Long): String {
    val totalSec = (ms / 1_000L).coerceAtLeast(0L)
    val hours    = totalSec / 3600
    val minutes  = (totalSec % 3600) / 60
    val seconds  = totalSec % 60

    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}