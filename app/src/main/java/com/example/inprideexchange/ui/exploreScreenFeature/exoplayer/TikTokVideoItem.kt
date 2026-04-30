package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

private const val PROGRESS_POLL_MS        = 200L
private const val WATCHDOG_POLL_MS        = 1_000L
private const val BUFFERING_STUCK_MS      = 4_000L
private const val PRE_START_RETRY_POLL_MS = 500L

/**
 * Single video page in the TikTok feed.
 *
 * ── LOADING COVER — soft frosted overlay ─────────────────────────────────────
 *
 * When ForYouFeed signals showLoadingCover = true (this page is the NEXT slot
 * and the player has not yet prepared), a soft light-gray gradient is shown on
 * top of the PlayerView. It is designed to feel calm and reassuring — closer to
 * a frosted glass or "blurred preview" aesthetic than a hard black wall.
 *
 * ── THE CORRECT SIGNAL: onRenderedFirstFrame ──────────────────────────────────
 *
 * Two signals could indicate "player is ready to show video":
 *
 *   STATE_READY:
 *     Fires when the decoder has enough data to start playback.
 *     The SurfaceView may still be black — the codec has not yet pushed a frame
 *     to the display buffer. Removing the cover here causes a black flash.
 *
 *   onRenderedFirstFrame:
 *     Fires only after the codec has decoded AND the display subsystem has
 *     submitted a frame to the SurfaceView. The surface is visually non-black.
 *     This is the only safe moment to reveal the video behind the cover.
 *
 * We use onRenderedFirstFrame as the gate. The cover holds until BOTH:
 *   • showLoadingCover == false  (ForYouFeed: player reached STATE_READY)
 *   • firstFrameRendered == true (listener: frame is on the display surface)
 *
 * ── COVER APPEARANCE ─────────────────────────────────────────────────────────
 *
 * Soft warm-white gradient (#F5F5F5 → #E8E8E8). Light enough to read as
 * "blurred loading preview" rather than a hard block, with slight brightening
 * at top and bottom edges for dimensionality. Alpha channel 0xEB = ~92% so a
 * ghost of any SurfaceView content bleeds through at the tail of the fade.
 *
 * ── FADE TIMING ──────────────────────────────────────────────────────────────
 *
 * Fade-in: instant (durationMillis = 0) — the cover must be opaque the moment
 *   the user begins swiping into a not-ready page. Any delay here would let the
 *   black SurfaceView show through during the swipe.
 *
 * Fade-out: 250 ms tween — gives the SurfaceView time to paint its second and
 *   third frames during the transition, so the video is visibly running before
 *   the cover is fully gone. Feels like a natural reveal, not a hard cut.
 *
 * ── RESET ON SLOT CHANGE ──────────────────────────────────────────────────────
 *
 * firstFrameRendered resets to false in LaunchedEffect(isPlaying) when isPlaying
 * flips false — the same moment ForYouFeed's rotate() demotes this slot. The
 * next assignment starts fresh with an opaque cover waiting for a new first frame.
 */
@SuppressLint("NewApi")
@OptIn(UnstableApi::class)
@Composable
fun TikTokVideoItem(
    player           : ExoPlayer,
    isPlaying        : Boolean,
    isScrolling      : Boolean,
    showLoadingCover : Boolean,
    onPause          : () -> Unit,
    onResume         : () -> Unit,
    modifier         : Modifier = Modifier,
) {
    val context = LocalContext.current

    val cm = remember(context) {
        context.getSystemService(ConnectivityManager::class.java)
    }

    var manuallyPaused      by remember { mutableStateOf(false) }
    var progress            by remember { mutableFloatStateOf(0f) }
    var isBufferingMidVideo by remember { mutableStateOf(false) }
    var errorWhilePlaying   by remember { mutableStateOf(false) }
    var hasStartedPlaying   by remember { mutableStateOf(false) }

    // True once onRenderedFirstFrame fires for this slot. The cover will not
    // begin fading until this is true, even if showLoadingCover is already false.
    // Reset to false when isPlaying flips false (slot is demoted / recycled).
    var firstFrameRendered  by remember { mutableStateOf(false) }

    // Cover is visible until BOTH gates clear. Uses short-circuit: if
    // showLoadingCover is still true we don't even need to check firstFrameRendered.
    val coverVisible = showLoadingCover || !firstFrameRendered

    // Instant snap to opaque when cover should be visible (no delay on swipe-in).
    // Slow 250 ms fade-out once both gates clear (video is genuinely running).
    val animatedCoverAlpha by animateFloatAsState(
        targetValue   = if (coverVisible) 1f else 0f,
        animationSpec = tween(durationMillis = if (coverVisible) 0 else 250),
        label         = "loadingCoverAlpha",
    )

    // ── Seek bar state ────────────────────────────────────────────────────────
    var isDragging            by remember { mutableStateOf(false) }
    var dragProgress          by remember { mutableFloatStateOf(0f) }
    var wasPlayingOnDragStart by remember { mutableStateOf(false) }
    var trackWidthPx          by remember { mutableIntStateOf(0) }

    val currentIsPlaying      by rememberUpdatedState(isPlaying)
    val currentManuallyPaused by rememberUpdatedState(manuallyPaused)

    val displayProgress = if (isDragging) dragProgress else progress

    // ── Drive playback from isPlaying ─────────────────────────────────────────
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            player.playWhenReady = true
            player.play()
        } else {
            player.pause()
            player.playWhenReady = false
            manuallyPaused      = false
            isBufferingMidVideo = false
            errorWhilePlaying   = false
            isDragging          = false
            hasStartedPlaying   = false
            firstFrameRendered  = false   // reset cover gate for next slot assignment
        }
    }

    // ── Player state + first-frame listener ───────────────────────────────────
    DisposableEffect(player) {
        val listener = object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        if (player.isPlaying || (currentIsPlaying && !currentManuallyPaused)) {
                            hasStartedPlaying = true
                        }
                        isBufferingMidVideo = false
                        errorWhilePlaying   = false
                        if (currentIsPlaying && !currentManuallyPaused && !player.isPlaying) {
                            player.playWhenReady = true
                            player.play()
                        }
                    }
                    Player.STATE_BUFFERING -> {
                        if (hasStartedPlaying && !currentManuallyPaused) {
                            isBufferingMidVideo = true
                        }
                    }
                    Player.STATE_IDLE -> {
                        if (hasStartedPlaying && currentIsPlaying && !currentManuallyPaused) {
                            isBufferingMidVideo = true
                        }
                    }
                    Player.STATE_ENDED -> {
                        isBufferingMidVideo = false
                    }
                }
            }

            // Fires after the codec has decoded AND the display subsystem has
            // submitted a frame to the SurfaceView. This is the only signal
            // that guarantees the surface is visually non-black. Clearing the
            // cover here means the user sees live video immediately as it fades.
            override fun onRenderedFirstFrame() {
                firstFrameRendered = true
            }

            override fun onPlayerError(error: PlaybackException) {
                if (hasStartedPlaying && currentIsPlaying && !currentManuallyPaused) {
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
                lastPosition = -1L; positionStuckSince = 0L; continue
            }

            val state    = player.playbackState
            val position = player.currentPosition
            val duration = player.duration
            val now      = System.currentTimeMillis()

            if (errorWhilePlaying && state == Player.STATE_IDLE && currentIsPlaying) {
                if (cm.activeNetwork != null) {
                    player.prepare()
                    player.playWhenReady = true
                    player.play()
                    errorWhilePlaying   = false
                    isBufferingMidVideo = false
                    // Do NOT reset hasStartedPlaying or firstFrameRendered here.
                    // This is a mid-video reconnect — the cover must stay hidden
                    // and only the buffering spinner should show. Resetting
                    // firstFrameRendered would make coverVisible = true, showing
                    // the dark overlay on top of a video that was already playing.
                    lastPosition        = -1L
                    positionStuckSince  = 0L
                }
                continue
            }

            if (state == Player.STATE_BUFFERING && duration > 0) {
                if (lastPosition < 0) lastPosition = position
                if (position == lastPosition) {
                    if (positionStuckSince == 0L) positionStuckSince = now
                    if (now - positionStuckSince >= BUFFERING_STUCK_MS
                        && cm.activeNetwork != null && currentIsPlaying
                    ) {
                        if (player.playbackState == Player.STATE_IDLE) player.prepare()
                        player.playWhenReady = true
                        player.play()
                        positionStuckSince = 0L
                    }
                } else {
                    lastPosition = position; positionStuckSince = 0L
                }
            } else {
                lastPosition = if (state == Player.STATE_READY) position else -1L
                positionStuckSince = 0L
                if (state == Player.STATE_READY) isBufferingMidVideo = false
            }
        }
    }

    // ── Pre-start connectivity recovery ──────────────────────────────────────
    //
    // PROBLEM this solves:
    //   Cold start with no internet — ExoPlayer enters STATE_BUFFERING with
    //   duration == 0 (manifest never fetched). The existing watchdog guards on
    //   `duration > 0` and `errorWhilePlaying`, both of which are false at this
    //   stage, so NO retry ever fires. The cover never lifts when internet returns.
    //
    // HOW IT WORKS:
    //   We use ConnectivityManager.registerNetworkCallback to get a real-time
    //   signal the moment internet is restored — no polling needed.
    //   When the callback fires AND the player is still in the pre-start stuck
    //   state (isPlaying, !hasStartedPlaying, STATE_BUFFERING or STATE_IDLE),
    //   we call stop() + prepare() + play() to give ExoPlayer a clean restart
    //   with the now-available network.
    //
    // WHY stop() before prepare():
    //   A player stuck in STATE_BUFFERING ignores seekTo(). stop() → STATE_IDLE
    //   flushes the stalled buffer. The subsequent prepare() re-fetches the
    //   manifest from scratch on the now-live network.
    //
    // WHY a separate effect (not the watchdog):
    //   The watchdog's stuck-buffering path is intentionally gated on duration > 0
    //   (mid-video stall detection). Mixing pre-start recovery into that path
    //   would loosen a guard that exists to avoid false positives during normal
    //   buffering. Keeping the two paths separate makes each easier to reason about.
    //
    // SCOPE: this effect only runs while isPlaying == true. The moment the user
    //   swipes away (isPlaying → false), the coroutine is cancelled and the
    //   NetworkCallback is unregistered. No dangling callbacks.
    DisposableEffect(player, isPlaying) {
        if (!isPlaying) return@DisposableEffect onDispose {}

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Network just came back. Retry only if still stuck pre-start.
                val state = player.playbackState
                if (!hasStartedPlaying &&
                    currentIsPlaying &&
                    (state == Player.STATE_BUFFERING || state == Player.STATE_IDLE)
                ) {
                    player.stop()
                    player.prepare()
                    player.playWhenReady = true
                    player.play()
                }
            }
        }

        // Register with a main-thread Handler so onAvailable() is delivered on
        // the main thread — ExoPlayer enforces main-thread access and crashes
        // immediately if called from ConnectivityThread (the default callback thread).
        val mainHandler = Handler(Looper.getMainLooper())
        val request = NetworkRequest.Builder().build()
        try {
            cm.registerNetworkCallback(request, callback, mainHandler)
        } catch (_: Exception) { /* permission missing or CM unavailable — safe to skip */ }

        onDispose {
            try { cm.unregisterNetworkCallback(callback) } catch (_: Exception) {}
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    player.pause(); player.playWhenReady = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (currentIsPlaying && !currentManuallyPaused) {
                        if (player.playbackState == Player.STATE_IDLE) player.prepare()
                        player.playWhenReady = true; player.play()
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ── Progress poll ─────────────────────────────────────────────────────────
    LaunchedEffect(player, isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        while (isActive) {
            val duration = player.duration.takeIf { it > 0 } ?: 0L
            val position = player.currentPosition
            if (duration > 0 && !isDragging) progress = position.toFloat() / duration
            delay(PROGRESS_POLL_MS)
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(interactionSource = interactionSource, indication = null) {
                if (!isDragging) {
                    if (player.isPlaying) { manuallyPaused = true; onPause() }
                    else                 { manuallyPaused = false; onResume() }
                }
            }
    ) {
        // ── Video surface ──────────────────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode    = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update  = { view -> if (view.player !== player) view.player = player },
            modifier = Modifier.fillMaxSize()
        )

        // ── Soft frosted loading cover ─────────────────────────────────────────
        //
        // Light warm-gray gradient layered on top of the PlayerView.
        // Held fully opaque until the ExoPlayer surface has a real frame,
        // then fades to reveal the live video beneath over 250 ms.
        //
        // The `if (animatedCoverAlpha > 0f)` guard skips this Box entirely
        // once the fade completes — no overdraw cost while the video plays.
        if (animatedCoverAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(animatedCoverAlpha)
                    .background(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.00f to Color(0xEB3D3939),   // near-white top
                                0.30f to Color(0xEBDC8080),   // soft light gray
                                0.50f to Color(0xEBC06F6F),   // slightly deeper centre
                                0.70f to Color(0xEB7A2121),   // soft light gray
                                1.00f to Color(0xEBF5F5F5),   // near-white bottom
                            )
                        )
                    )
            )
        }

        // ── Tap-to-pause overlay ──────────────────────────────────────────────
        if (manuallyPaused && !isScrolling) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .background(Color.Black.copy(alpha = 0.50f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Default.PlayArrow,
                    contentDescription = "Tap to resume",
                    tint               = Color.White,
                    modifier           = Modifier.size(46.dp),
                )
            }
        }

        // ── Mid-playback buffering spinner ────────────────────────────────────
        if (isBufferingMidVideo && hasStartedPlaying && !isScrolling && !manuallyPaused && isPlaying) {
            CircularProgressIndicator(
                modifier    = Modifier.align(Alignment.Center).size(48.dp),
                color       = Color.White,
                strokeWidth = 3.dp,
            )
        }

        // ── Seek bar ──────────────────────────────────────────────────────────
        if (!isScrolling) {
            SeekBar(
                progress     = displayProgress,
                isDragging   = isDragging,
                trackWidthPx = trackWidthPx,
                player       = player,
                modifier     = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { trackWidthPx = it.width }
                    .pointerInput(player) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                if (trackWidthPx <= 0) return@detectHorizontalDragGestures
                                isDragging            = true
                                wasPlayingOnDragStart = player.isPlaying
                                dragProgress          = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                                player.pause(); player.playWhenReady = false
                            },
                            onHorizontalDrag = { change, _ ->
                                if (trackWidthPx <= 0) return@detectHorizontalDragGestures
                                change.consume()
                                dragProgress = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                            },
                            onDragEnd = {
                                val dur = player.duration.takeIf { it > 0 } ?: 0L
                                if (dur > 0) {
                                    player.seekTo((dragProgress * dur).toLong())
                                    progress = dragProgress
                                }
                                isDragging = false
                                if (wasPlayingOnDragStart && !manuallyPaused && isPlaying) {
                                    if (player.playbackState == Player.STATE_IDLE) player.prepare()
                                    player.playWhenReady = true; player.play()
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                if (wasPlayingOnDragStart && !manuallyPaused && isPlaying) {
                                    if (player.playbackState == Player.STATE_IDLE) player.prepare()
                                    player.playWhenReady = true; player.play()
                                }
                            },
                        )
                    },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SeekBar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SeekBar(
    progress     : Float,
    isDragging   : Boolean,
    trackWidthPx : Int,
    player       : ExoPlayer,
    modifier     : Modifier = Modifier,
) {
    val duration   = player.duration.takeIf { it > 0 } ?: 0L
    val positionMs = (progress * duration).toLong()
    val timeText   = remember(positionMs) { formatMediaTime(positionMs) }
    val thumbFrac  = progress.coerceIn(0f, 1f)

    Box(modifier = modifier.fillMaxWidth().height(48.dp)) {
        val trackH = if (isDragging) 4.dp else 2.dp

        Box(
            modifier = Modifier
                .fillMaxWidth().height(trackH).align(Alignment.Center)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.25f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(thumbFrac).height(trackH).align(Alignment.CenterStart)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White)
        )

        AnimatedVisibility(
            visible  = isDragging,
            enter    = fadeIn(),
            exit     = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(
                    start = if (trackWidthPx > 0) {
                        val px = (thumbFrac * trackWidthPx).roundToInt()
                        val dp = with(androidx.compose.ui.platform.LocalDensity.current) { px.toDp() }
                        (dp - 6.dp).coerceAtLeast(0.dp)
                    } else 0.dp
                ),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = timeText,
                    color      = Color.White,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier
                        .offset(y = (-28).dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
                Box(modifier = Modifier.size(12.dp).background(Color.White, CircleShape))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Time formatting — zero-allocation
// ─────────────────────────────────────────────────────────────────────────────

private fun formatMediaTime(ms: Long): String {
    val totalSec = (ms / 1_000L).coerceAtLeast(0L)
    val hours    = totalSec / 3600
    val minutes  = (totalSec % 3600) / 60
    val seconds  = totalSec % 60
    return buildString {
        if (hours > 0) {
            append(hours); append(':')
            appendTwoDigits(minutes); append(':'); appendTwoDigits(seconds)
        } else {
            append(minutes); append(':'); appendTwoDigits(seconds)
        }
    }
}

private fun StringBuilder.appendTwoDigits(value: Long) {
    if (value < 10) append('0')
    append(value)
}