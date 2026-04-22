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

/**
 * Full-screen vertical feed with TikTok-style playback behaviour.
 *
 * KEY CHANGES FROM ORIGINAL:
 *
 * FIX 1 — double-swipe on first video:
 *   The original used LaunchedEffect(pagerState.currentPage) which only
 *   fires when currentPage *changes*. At launch, currentPage is already 0,
 *   so the effect never fires and the player at page 0 never gets
 *   playWhenReady = true until the user swipes away and back.
 *
 *   Fix: add LaunchedEffect(Unit) in ForYouFeed to prime page 0 on first
 *   composition, AND move the isPlaying signal into TikTokVideoItem via
 *   a combination of LaunchedEffect(Unit) + LaunchedEffect(isPlaying).
 *
 * FIX 2 — off-screen player plays audio:
 *   The original derived isPlaying from pagerState.currentPage inside the
 *   VerticalPager slot. During a fast swipe, currentPage can lag behind
 *   the visible page, so a page that just left the screen still sees
 *   isPlaying = true for a brief window. Switching to
 *   pagerState.settledPage means isPlaying only becomes true AFTER the
 *   pager has fully settled on a page, never during mid-swipe.
 *
 * FIX 3 — audio on lock screen:
 *   Handled inside TikTokVideoItem via LifecycleEventObserver. No changes
 *   needed in this composable.
 */
@OptIn(UnstableApi::class)
@Composable
fun ForYouFeed() {

    val context = LocalContext.current
    val feed    = rememberFeed()


    val pool       = remember { PlayerPool(context) }


    // Cache-aware player map: pageIndex → ExoPlayer
    val players = remember { mutableMapOf<Int, androidx.media3.exoplayer.ExoPlayer>() }

    // Background disk prefetcher
    val preloader = remember { VideoPreloader(context) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount   = { feed.size }
    )

    // Cleanup on exit
    DisposableEffect(Unit) {
        onDispose {
            preloader.cancel()
           // players.values.forEach { it.release() }
            pool.release()
            players.clear()
        }
    }


    // ── FIX 1: Prime page 0 on first composition ─────────────────────────────
    // Without this, currentPage starts at 0 and never triggers the
    // LaunchedEffect(pagerState.settledPage) below because it doesn't change.
    LaunchedEffect(Unit) {
        val first = feed.getOrNull(0) ?: return@LaunchedEffect
        val player = players.getOrPut(0) { CachedPlayerFactory.build(context) }
        player.setMediaItem(MediaItem.fromUri(first.videoUrl))
        player.prepare()
        // Prefetch the next two items immediately at launch
        feed.getOrNull(1)?.videoUrl?.let { preloader.preload(it) }
        feed.getOrNull(2)?.videoUrl?.let { preloader.preload(it) }
    }

    // ── FIX 2: Use settledPage instead of currentPage ────────────────────────
    // settledPage only updates when the pager has FULLY settled — not during
    // the swipe animation. This prevents isPlaying from being true for a
    // page that is mid-swipe off screen.
    LaunchedEffect(pagerState.settledPage) {
        val current = pagerState.settledPage

        // Infinite scroll: load more when near the end
        if (current >= feed.size - 2) {
            feed.addAll(FakeVideoRepository.loadMore(feed.size))
        }

        // Background prefetch: N+1 and N+2 bytes to disk before user arrives
        feed.getOrNull(current + 1)?.videoUrl?.let { preloader.preload(it) }
        feed.getOrNull(current + 2)?.videoUrl?.let { preloader.preload(it) }

        // Release players outside the ±1 window.
        // IMPORTANT: collect stale keys into a separate list FIRST to avoid
        // ConcurrentModificationException when mutating while iterating.
        val keepWindow = (current - 1)..(current + 1)
        val stalePages = players.keys.filter { it !in keepWindow }
        stalePages.forEach { page ->
            players.remove(page)?.release()
        }
    }

    VerticalPager(
        state                    = pagerState,
        modifier                 = Modifier.fillMaxSize(),
        beyondViewportPageCount  = 1,
        key                      = { page -> feed.getOrNull(page)?.id ?: page }
    ) { page ->

        val item = feed.getOrNull(page) ?: return@VerticalPager

        val player = players.getOrPut(page) {
            CachedPlayerFactory.build(context)
        }

        LaunchedEffect(player, item.videoUrl) {
            player.setMediaItem(MediaItem.fromUri(item.videoUrl))
            player.prepare()
        }

        TikTokVideoItem(
            player    = player,
            // ── FIX 2: settledPage, not currentPage ──────────────────────
            // currentPage changes during the swipe animation; settledPage
            // only changes once the pager has fully come to rest on a page.
            isPlaying = pagerState.settledPage == page,
        )
    }
}