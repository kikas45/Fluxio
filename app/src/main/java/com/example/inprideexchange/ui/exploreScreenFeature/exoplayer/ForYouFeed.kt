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
 * ForYouFeed — TikTok-style vertical video feed, 3-player pool edition.
 *
 * ── SLOT MAP ─────────────────────────────────────────────────────────────────
 *
 * Three pages are always mapped at any time:
 *   slotForPage[s - 1] = PREV     (if s > 0)
 *   slotForPage[s]     = CURRENT
 *   slotForPage[s + 1] = NEXT
 *
 * ── RACE FIX: atomic slotForPage updates ────────────────────────────────────
 *
 * Previously slotForPage.clear() followed by three individual puts. Between
 * clear() and the first put, Compose could read an empty map and return early
 * from the pager lambda — producing a 1-frame black flash on fast swipes.
 *
 * Fix: build the complete new map first, then write all three keys in a single
 * targeted-put pass — never calling clear(). Keys for pages that no longer
 * exist are removed explicitly. The map is never empty between operations:
 *   1. Put new values for s-1, s, s+1 (overwriting stale values).
 *   2. Remove only the key that is now two pages away.
 *
 * This means slotForPage always contains at least the CURRENT mapping during
 * any recomposition that happens between steps 1 and 2.
 *
 * ── LIFECYCLE FIX: removed player.pause() from onDispose ────────────────────
 *
 * The LifecycleEventObserver's onDispose called player.pause() on whatever
 * player was bound at dispose time. By then rotate() may have already
 * reassigned that physical player to the CURRENT slot and started playing.
 * The onDispose pause() was silently stopping the currently playing video
 * every time a page left composition.
 *
 * Fix: onDispose no longer calls player.pause(). Playback state is fully
 * managed by ForYouFeed's orchestration (playCurrentPlayer / rotate) and
 * by the ON_PAUSE lifecycle event, which is the correct signal for pausing.
 *
 * ── LOAD STRATEGY ────────────────────────────────────────────────────────────
 *
 * Forward (+1):
 *   PREV  came from old CURRENT — already loaded, frame preserved.
 *   CURRENT came from old NEXT  — already loaded.
 *   NEXT  is the recycled slot  — load new URL.
 *
 * Backward (-1):
 *   NEXT  came from old CURRENT — already loaded, frame preserved.
 *   CURRENT came from old PREV  — already loaded, frame preserved.
 *   PREV  is the recycled slot  — load new URL.
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

    // pageIndex → PlayerPool logical slot (PREV / CURRENT / NEXT).
    // Never cleared — only targeted puts and removes to avoid the
    // empty-map race window that caused 1-frame black flashes.
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
        // Page 0 = CURRENT, page 1 = NEXT. No PREV on boot.
        slotForPage[0] = PlayerPool.CURRENT
        slotForPage[1] = PlayerPool.NEXT

        pool.load(PlayerPool.CURRENT, feed.getOrNull(0)?.videoUrl)
        pool.load(PlayerPool.NEXT,    feed.getOrNull(1)?.videoUrl)

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

            // ── Atomic slot map update — never clear(), only targeted writes ──
            //
            // Write all three live keys first (overwrite any stale values).
            // Then remove the single key that is now two pages away.
            // At no point is the map empty or missing the CURRENT key.
            if (s > 0) slotForPage[s - 1] = PlayerPool.PREV
            slotForPage[s]     = PlayerPool.CURRENT
            slotForPage[s + 1] = PlayerPool.NEXT

            // Remove stale keys: the page that just fell off the window.
            when {
                direction > 0 -> slotForPage.remove(s - 2)   // was old PREV
                direction < 0 -> slotForPage.remove(s + 2)   // was old NEXT
            }

            when {
                direction > 0 -> pool.load(PlayerPool.NEXT, feed.getOrNull(s + 1)?.videoUrl)
                direction < 0 -> pool.load(PlayerPool.PREV, feed.getOrNull(s - 1)?.videoUrl)
            }
        }

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
    //
    // Reads pool.isNextReady() every 100 ms. The soft race where rotate()
    // changes slot[NEXT] between the poll read and the slot reassignment is
    // harmless — the worst outcome is a 100 ms spinner flicker.
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
                PlayerPool.PREV    -> pool.prevPlayer
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

        // Spinner: shown while swiping forward and NEXT player isn't ready yet.
        // Not needed on backward swipe — PREV always has a live frame.
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