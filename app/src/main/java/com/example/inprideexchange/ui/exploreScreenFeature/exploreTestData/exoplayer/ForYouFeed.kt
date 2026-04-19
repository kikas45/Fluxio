package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer


@OptIn(UnstableApi::class)
@Composable
fun ForYouFeed() {

    val context = LocalContext.current
    val feed = rememberFeed()
    val playerPool = remember { PlayerPool(context) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { feed.size }
    )

    DisposableEffect(Unit) {
        onDispose { playerPool.release() }
    }

    LaunchedEffect(pagerState.currentPage, feed.size) {

        val current = pagerState.currentPage

        val currentItem = feed.getOrNull(current) ?: return@LaunchedEffect
        val nextItem = feed.getOrNull(current + 1)
        val prevItem = feed.getOrNull(current - 1)

        fun warm(player: ExoPlayer, url: String?) {
            if (url == null) return

            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.playWhenReady = false
        }

        warm(playerPool.currentPlayer, currentItem.videoUrl)
        warm(playerPool.nextPlayer, nextItem?.videoUrl)
        warm(playerPool.prevPlayer, prevItem?.videoUrl)
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 1
    ) { page ->

        val player = when (page) {
            pagerState.currentPage -> playerPool.currentPlayer
            pagerState.currentPage + 1 -> playerPool.nextPlayer
            pagerState.currentPage - 1 -> playerPool.prevPlayer
            else -> null
        }

        TikTokVideoItem(
            player = player,
            isPlaying = pagerState.currentPage == page
        )
    }
}