package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

private const val ICON_HIDE_DELAY_MS = 800L
private const val PROGRESS_POLL_MS   = 200L

@OptIn(UnstableApi::class)
@Composable
fun TikTokVideoItem(
    player    : ExoPlayer?,
    isPlaying : Boolean
) {
    var showIcon by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    // Drive playback from the pager's current-page signal
    LaunchedEffect(isPlaying) {
        player?.playWhenReady = isPlaying
    }

    // Auto-hide the play/pause icon after a short delay
    LaunchedEffect(showIcon) {
        if (showIcon) {
            delay(ICON_HIDE_DELAY_MS)
            showIcon = false
        }
    }

    // Poll position for the progress bar — only while this item is active
    LaunchedEffect(player, isPlaying) {
        while (true) {
            val duration = player?.duration?.takeIf { it > 0 } ?: 0L
            val position = player?.currentPosition ?: 0L
            progress = if (duration > 0) position.toFloat() / duration else 0f
            delay(PROGRESS_POLL_MS)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null    // no ripple — matches TikTok UX
            ) {
                player?.let {
                    it.playWhenReady = !it.isPlaying
                    showIcon = true
                }
            }
    ) {

        // ── Video surface ────────────────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { view ->
                // Reference-equality check — prevents surface detach/reattach flicker
                if (view.player !== player) {
                    view.player = player
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Play / Pause icon (centre, auto-hides after 800 ms) ──────────────
        AnimatedVisibility(
            visible  = showIcon,
            modifier = Modifier.align(Alignment.Center),
            enter    = fadeIn(),
            exit     = fadeOut()
        ) {
            Box(
                modifier         = Modifier
                    .size(80.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = if (player?.isPlaying == true) Icons.Default.Pause
                    else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(44.dp)
                )
            }
        }

        // ── Progress bar (bottom edge) ────────────────────────────────────────
        LinearProgressIndicator(
            progress   = { progress },
            modifier   = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter),
            color      = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
            strokeCap  = StrokeCap.Butt
        )
    }
}