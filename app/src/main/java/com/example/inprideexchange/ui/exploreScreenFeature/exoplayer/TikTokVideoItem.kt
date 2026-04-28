package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import android.net.ConnectivityManager
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

private const val PROGRESS_POLL_MS   = 200L
private const val WATCHDOG_POLL_MS   = 1_000L
private const val BUFFERING_STUCK_MS = 4_000L

/**
 * Single video page in the TikTok feed.
 *
 * ── MEMORY FIX: ConnectivityManager hoisted out of the watchdog loop ─────────
 *
 * Previously, the watchdog called context.getSystemService() inside its polling
 * loop — a map lookup + cast on every iteration (every 1s while buffering).
 * ConnectivityManager is a process-scoped singleton; fetching it once at
 * composable entry and sharing the reference is both correct and cheaper.
 *
 * ── NETWORK RECOVERY (recap) ─────────────────────────────────────────────────
 *
 * Case A — STATE_BUFFERING with position not advancing for > BUFFERING_STUCK_MS:
 *   ExoPlayer is still retrying internally. Kick play() to unblock it.
 *
 * Case B — STATE_IDLE after onPlayerError():
 *   ExoPlayer exhausted its internal retries. errorWhilePlaying=true is set
 *   by onPlayerError(). Watchdog polls activeNetwork every second; when network
 *   returns it calls prepare() (no seekTo — mid-video resume) then play().
 *   This is the case that was silently broken before.
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

    val currentIsPlaying      by rememberUpdatedState(isPlaying)
    val currentManuallyPaused by rememberUpdatedState(manuallyPaused)

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

        // cm is captured from the outer scope — no getSystemService() in the loop.
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
                    player.prepare()        // re-arms at retained position
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
    LaunchedEffect(player, isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        while (isActive) {
            val duration = player.duration.takeIf { it > 0 } ?: 0L
            val position = player.currentPosition
            if (duration > 0) progress = position.toFloat() / duration
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
                if (player.isPlaying) {
                    manuallyPaused = true
                    onPause()
                } else {
                    manuallyPaused = false
                    onResume()
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

        if (isBufferingMidVideo && !isScrolling && !manuallyPaused && isPlaying) {
            CircularProgressIndicator(
                modifier    = Modifier.align(Alignment.Center).size(48.dp),
                color       = Color.White,
                strokeWidth = 3.dp,
            )
        }

        if (!isScrolling) {
            LinearProgressIndicator(
                progress   = { progress },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.BottomCenter),
                color      = Color.White,
                trackColor = Color.White.copy(alpha = 0.25f),
                strokeCap  = StrokeCap.Butt
            )
        }
    }
}