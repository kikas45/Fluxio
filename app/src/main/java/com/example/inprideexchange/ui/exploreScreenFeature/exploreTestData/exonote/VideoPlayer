package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    player: ExoPlayer?,   // 🔥 IMPORTANT: external player
    videoUrl: String,
    isPlaying: Boolean
) {
    val scope = rememberCoroutineScope()

    var showIcon by remember { mutableStateOf(false) }
    var isCurrentlyPlaying by remember { mutableStateOf(isPlaying) }

    // 🔥 Load video ONLY when url changes
    LaunchedEffect(videoUrl, player) {
        player?.let {
            it.setMediaItem(MediaItem.fromUri(videoUrl))
            it.prepare()
        }
    }

    // 🔥 Sync play state
    LaunchedEffect(isPlaying, player) {
        player?.playWhenReady = isPlaying
        isCurrentlyPlaying = isPlaying
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                player?.let {
                    val newState = !it.isPlaying
                    it.playWhenReady = newState
                    isCurrentlyPlaying = newState

                    showIcon = true

                    scope.launch {
                        delay(600)
                        showIcon = false
                    }
                }
            }
    ) {

        // 🎥 VIDEO
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    useController = false
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { view ->
                view.player = player   // 🔥 CLEAN SAFE BINDING
            },
            modifier = Modifier.fillMaxSize()
        )

        // ▶️ ⏸ ICON
        AnimatedVisibility(
            visible = showIcon,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = if (isCurrentlyPlaying)
                    Icons.Default.Pause
                else
                    Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
        }
    }
}