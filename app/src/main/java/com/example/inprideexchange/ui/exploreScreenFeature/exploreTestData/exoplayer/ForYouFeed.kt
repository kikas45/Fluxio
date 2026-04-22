package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  HOW THIS WORKS  (read before editing)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 *  [PlayerPool] owns exactly 3 ExoPlayers for the entire lifetime of this
 *  screen. They rotate through PREV / CURRENT / NEXT slots as the user swipes.
 *  No new players are ever allocated after startup.
 *
 *  [VideoPreloader] concurrently downloads the first 2 MB of upcoming videos
 *  to disk (VideoCache / SimpleCache). When PlayerPool.load() is called for
 *  those pages, CacheDataSource reads from disk instead of the network.
 *
 *  Swipe sequence (forward):
 *    1. pagerState.currentPage changes
 *    2. pool.rotate(+1)  →  old NEXT becomes CURRENT instantly (zero alloc)
 *    3. pool.load(NEXT)  →  recycled player gets the new N+1 URL
 *    4. preloader starts writing N+2 to disk in the background
 *    5. VerticalPager assigns the right physical player to each visible page
 *
 * ═══════════════════════════════════════════════════════════════════════════
 */
@OptIn(UnstableApi::class)
@Composable
fun ForYouFeed() {

    val context    = LocalContext.current
    val feed       = rememberFeed()
    val pool       = remember { PlayerPool(context) }
    val preloader  = remember { VideoPreloader(context) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount   = { feed.size }
    )

    // Track the previous page index to calculate swipe direction for rotation
    var prevPage by remember { mutableIntStateOf(0) }

    // ── Full cleanup when screen leaves composition ──────────────────────────
    DisposableEffect(Unit) {
        onDispose {
            preloader.cancel()
            pool.release()
        }
    }

    // ── Initial load before the user has swiped at all ──────────────────────
    LaunchedEffect(Unit) {
        pool.load(PlayerPool.CURRENT, feed.getOrNull(0)?.videoUrl)
        pool.load(PlayerPool.NEXT,    feed.getOrNull(1)?.videoUrl)
        feed.getOrNull(2)?.videoUrl?.let { preloader.preload(it) }
    }

    // ── React to every page change ───────────────────────────────────────────
    LaunchedEffect(pagerState.currentPage) {
        val current   = pagerState.currentPage
        val direction = current - prevPage          // +1 forward, -1 backward
        prevPage = current

        // 1. Rotate the slot assignments so CURRENT always points to the right player
        pool.rotate(direction)

        // 2. Load the new neighbour into the freshly-recycled slot
        //    load() is a no-op if the player already has this URL — safe to call always
        pool.load(PlayerPool.CURRENT, feed.getOrNull(current)?.videoUrl)
        pool.load(PlayerPool.NEXT,    feed.getOrNull(current + 1)?.videoUrl)
        pool.load(PlayerPool.PREV,    feed.getOrNull(current - 1)?.videoUrl)

        // 3. Prefetch the page AFTER next to disk (two pages ahead)
        feed.getOrNull(current + 2)?.videoUrl?.let { preloader.preload(it) }

        // 4. Infinite scroll — load more items when 2 pages from the end
        if (current >= feed.size - 2) {
            feed.addAll(FakeVideoRepository.loadMore(feed.size))
        }
    }

    // ── Render ───────────────────────────────────────────────────────────────
    VerticalPager(
        state                   = pagerState,
        modifier                = Modifier.fillMaxSize(),
        beyondViewportPageCount = 1,
        key                     = { page -> feed.getOrNull(page)?.id ?: page }
    ) { page ->

        // Map each visible page to the correct physical player from the pool.
        // Pages more than 1 away receive null → black screen, no wasted resources.
        val player = when (page) {
            pagerState.currentPage     -> pool.currentPlayer
            pagerState.currentPage + 1 -> pool.nextPlayer
            pagerState.currentPage - 1 -> pool.prevPlayer
            else                       -> null
        }

        TikTokVideoItem(
            player    = player,
            isPlaying = pagerState.currentPage == page
        )
    }
}