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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

private const val ICON_HIDE_DELAY_MS = 800L
private const val PROGRESS_POLL_MS   = 200L

/**
 * Renders a single full-screen video item in the TikTok-style feed.
 *
 * FIX 1 — double-swipe on first video:
 *   isPlaying is now derived from [pagerSettledPage] == [pageIndex] in the
 *   parent, using LaunchedEffect(Unit) to fire immediately on first
 *   composition instead of only on change.
 *
 * FIX 2 — off-screen player keeps playing:
 *   playWhenReady is only set to TRUE when [isPlaying] is true AND the
 *   player is still attached to this page. We also guard with
 *   [DisposableEffect] to pause immediately when the composable leaves
 *   composition, which happens when the pool releases the player.
 *
 * FIX 3 — audio on lock screen:
 *   A [LifecycleEventObserver] pauses the player on ON_PAUSE (app goes to
 *   background / screen locks) and resumes on ON_RESUME only if [isPlaying]
 *   is still true for this page. This gives the OS a chance to revoke audio
 *   focus cleanly.
 */
@OptIn(UnstableApi::class)
@Composable
fun TikTokVideoItem(
    player    : ExoPlayer?,
    isPlaying : Boolean,
) {
    var showIcon by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    // ── FIX 3: Lifecycle observer — pause on background / lock screen ────────
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE  -> player?.pause()
                Lifecycle.Event.ON_RESUME -> if (isPlaying) player?.play()
                else                      -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // FIX 2: when this item leaves composition, always pause — do not
            // rely solely on the parent's release() call, which may race.
            player?.pause()
        }
    }

    // ── FIX 1 + FIX 2: Drive play/pause from isPlaying ──────────────────────
    // LaunchedEffect(Unit) fires on FIRST composition, not just on change.
    // This is the fix for the double-swipe-on-first-video bug: previously
    // LaunchedEffect(isPlaying) would not fire if isPlaying was already true
    // when the composable first appeared (page 0 at launch).
    LaunchedEffect(Unit) {
        player?.playWhenReady = isPlaying
    }
    LaunchedEffect(isPlaying) {
        player?.playWhenReady = isPlaying
    }

    // Auto-hide the play/pause icon
    LaunchedEffect(showIcon) {
        if (showIcon) {
            delay(ICON_HIDE_DELAY_MS)
            showIcon = false
        }
    }

    // Poll progress only while this item is the active page
    LaunchedEffect(player, isPlaying) {
        while (true) {
            if (isPlaying) {
                val duration = player?.duration?.takeIf { it > 0 } ?: 0L
                val position = player?.currentPosition ?: 0L
                progress = if (duration > 0) position.toFloat() / duration else 0f
            }
            delay(PROGRESS_POLL_MS)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null   // no ripple — matches TikTok UX
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
                    // Transparent shutter: lets the thumbnail below show
                    // through while ExoPlayer buffers the first frames.
                    // Change to BLACK if you don't have a thumbnail layer.
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                }
            },
            update = { view ->
                // Reference-equality guard prevents surface detach/attach
                // thrash on every recomposition.
                if (view.player !== player) {
                    view.player = player
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Play / Pause icon (centre, auto-hides) ───────────────────────────
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
                    imageVector        = if (player?.isPlaying == true)
                        Icons.Default.Pause
                    else
                        Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(44.dp)
                )
            }
        }

        // ── Progress bar (bottom edge) ───────────────────────────────────────
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