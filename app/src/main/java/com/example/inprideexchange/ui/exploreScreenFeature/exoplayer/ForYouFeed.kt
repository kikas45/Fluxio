package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.delay

/**
 * ForYouFeed — TikTok-style vertical video feed, 2-player pool edition.
 *
 * ── FIX: Boot block uses playCurrentPlayerFromStart() ────────────────────────
 *
 * playCurrentPlayer() no longer unconditionally seekTo(0) on CURRENT. On boot
 * we explicitly want to start from 0, so the boot LaunchedEffect now calls
 * playCurrentPlayerFromStart() instead. All subsequent calls (after swipes)
 * use playCurrentPlayer(), which preserves the playhead position — critical
 * for network-recovery correctness.
 */
@OptIn(UnstableApi::class, ExperimentalFoundationApi::class)
@Composable
fun ForYouFeed() {

    val context   = LocalContext.current
    val feed      = rememberFeed()
    val pool      = remember { PlayerPool(context) }
    val preloader = remember { VideoPreloader(context) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount   = { feed.size }
    )

    val pageHeight = LocalConfiguration.current.screenHeightDp.dp

    // pageIndex → PlayerPool slot constant (CURRENT / NEXT)
    val slotForPage = remember { mutableStateMapOf<Int, Int>() }

    var lastSettled     by remember { mutableIntStateOf(0) }
    var nextPlayerReady by remember { mutableStateOf(false) }

    // ── Cleanup ───────────────────────────────────────────────────────────────
    DisposableEffect(Unit) {
        onDispose {
            preloader.cancel()
            pool.release()
        }
    }

    // ── Boot ──────────────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        slotForPage[0] = PlayerPool.CURRENT
        slotForPage[1] = PlayerPool.NEXT

        pool.load(PlayerPool.CURRENT, feed.getOrNull(0)?.videoUrl)
        pool.load(PlayerPool.NEXT,    feed.getOrNull(1)?.videoUrl)

        // FIX: use playCurrentPlayerFromStart() on boot — we explicitly want
        // index 0 to start from the beginning. playCurrentPlayer() would also
        // seek to 0 here since the player is in STATE_IDLE, but being explicit
        // is safer and documents intent clearly.
        pool.playCurrentPlayerFromStart()

        val nextPoolUrl = feed.getOrNull(1)?.videoUrl
        preloader.preloadBatch(
            urls     = listOfNotNull(nextPoolUrl, feed.getOrNull(2)?.videoUrl),
            poolUrls = setOfNotNull(nextPoolUrl),
        )
    }

    // ── Orchestration — fires once per settled page ───────────────────────────
    LaunchedEffect(pagerState.settledPage) {
        val s = pagerState.settledPage

        val direction = when {
            s > lastSettled -> +1
            s < lastSettled -> -1
            else            ->  0
        }
        lastSettled = s

        if (direction != 0) {
            pool.rotate(direction)

            slotForPage.clear()
            slotForPage[s]     = PlayerPool.CURRENT
            slotForPage[s + 1] = PlayerPool.NEXT

            when {
                direction > 0 -> {
                    pool.load(PlayerPool.NEXT, feed.getOrNull(s + 1)?.videoUrl)
                }
                direction < 0 -> {
                    pool.load(PlayerPool.CURRENT, feed.getOrNull(s)?.videoUrl)
                    pool.load(PlayerPool.NEXT,    feed.getOrNull(s + 1)?.videoUrl)
                }
            }
        }

        // After a swipe, the new CURRENT is always a freshly loaded video
        // (rotate() + load() set it up from scratch), so playCurrentPlayer()
        // will correctly start it from 0. If direction == 0 (recompose on
        // same page), playCurrentPlayer() preserves the playhead — which is
        // the fix for network-drop recovery.
        pool.playCurrentPlayer()

        // ── Feed windowing ────────────────────────────────────────────────────
        if (s >= feed.size - 2) {
            feed.addAll(FakeVideoRepository.loadMore(feed.size))

            if (feed.size > 50) {
                val removeCount = minOf(10, feed.size)
                repeat(removeCount) { feed.removeAt(0) }

                val newPage = (pagerState.currentPage - removeCount).coerceAtLeast(0)
                pagerState.scrollToPage(newPage)
                lastSettled = newPage
            }
        }

        // ── Preloader ─────────────────────────────────────────────────────────
        if (direction > 0) {
            feed.getOrNull(s - 2)?.videoUrl?.let { preloader.cancelUrl(it) }
        } else if (direction < 0) {
            feed.getOrNull(s + 2)?.videoUrl?.let { preloader.cancelUrl(it) }
        }

        val nextPoolUrl = feed.getOrNull(s + 1)?.videoUrl
        preloader.preloadBatch(
            urls     = listOfNotNull(nextPoolUrl, feed.getOrNull(s + 2)?.videoUrl),
            poolUrls = setOfNotNull(nextPoolUrl),
        )
    }

    // ── Poll: NEXT player readiness for spinner ───────────────────────────────
    LaunchedEffect(Unit) {
        while (true) {
            val ready = pool.isNextReady()
            if (ready != nextPlayerReady) nextPlayerReady = ready
            delay(100)
        }
    }

    // ── Fling + scroll connection ─────────────────────────────────────────────
    val flingBehavior    = rememberTikTokFlingBehavior(pagerState)
    val scrollConnection = rememberSinglePageScrollConnection(pagerState, pageHeight)
    val isScrolling      = pagerState.isScrollInProgress

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 150.dp)
            .nestedScroll(scrollConnection)
    ) {
        VerticalPager(
            state                   = pagerState,
            modifier                = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            key                     = { page -> feed.getOrNull(page)?.id ?: page },
            flingBehavior           = flingBehavior,
            pageSpacing             = 0.dp,
        ) { page ->
            feed.getOrNull(page) ?: return@VerticalPager

            val slotIndex = slotForPage[page] ?: return@VerticalPager
            val player = when (slotIndex) {
                PlayerPool.CURRENT -> pool.currentPlayer
                PlayerPool.NEXT    -> pool.nextPlayer
                else               -> return@VerticalPager
            }

            TikTokVideoItem(
                player      = player,
                isPlaying   = pagerState.settledPage == page,
                isScrolling = isScrolling,
                onPause     = { pool.pauseCurrentPlayer() },
                onResume    = { pool.resumeCurrentPlayer() },
            )
        }

        // Loading spinner — shown while swiping and NEXT player is not ready
        if (isScrolling && !nextPlayerReady) {
            CircularProgressIndicator(
                modifier    = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .size(28.dp),
                color       = Color.White,
                strokeWidth = 2.5.dp,
            )
        }
    }
}