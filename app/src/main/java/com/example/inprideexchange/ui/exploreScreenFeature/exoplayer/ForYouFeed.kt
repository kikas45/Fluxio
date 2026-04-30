package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
 *
 * ── GRADIENT COVER (replaces CircularProgressIndicator) ──────────────────────
 *
 * When the user swipes forward to a page whose ExoPlayer has not yet rendered
 * its first frame, a dark gradient overlay is shown on top of the PlayerView.
 * This is vastly preferable to a blank black screen or a spinner:
 *
 *   • The gradient IS the "not ready" state — it looks intentional, not broken.
 *   • The overlay sits ON TOP of the PlayerView surface. When ExoPlayer is
 *     ready and the first frame is painted into the SurfaceView, the gradient
 *     fades out smoothly (animateFloatAsState, 200 ms). There is NEVER a
 *     frame where both the gradient and a black SurfaceView compete — the
 *     SurfaceView is always beneath, and the gradient alpha transitions from
 *     1.0 → 0.0 over the fade duration.
 *
 * ── WHY the cover is controlled here, not inside TikTokVideoItem ─────────────
 *
 * TikTokVideoItem does not know which pool slot it is bound to. Only
 * ForYouFeed knows that page N+1 maps to PlayerPool.NEXT and whether that
 * player is ready. Passing `showLoadingCover: Boolean` down to TikTokVideoItem
 * keeps the readiness logic in one place (here) and keeps TikTokVideoItem's
 * player-state listener free of pool-level concerns.
 *
 * ── NO FLICKER GUARANTEE ─────────────────────────────────────────────────────
 *
 * The critical constraint: never let the PlayerView surface and the gradient
 * cover compete for the same visual space while both are transitioning.
 *
 * How we guarantee this:
 *   1. `nextPlayerReady` is polled every 100 ms. When it flips to true,
 *      `showCoverForPage` for that page is set to false.
 *   2. TikTokVideoItem receives `showLoadingCover` and drives a
 *      `animateFloatAsState` from 1f → 0f over 200 ms.
 *   3. The gradient Box has `alpha = animatedAlpha`. At alpha = 0f the Box
 *      is still in the layout but invisible — it never pops out abruptly.
 *   4. The PlayerView is always present underneath at full opacity. The
 *      gradient dims to reveal it — there is no moment where neither is visible.
 *
 * ── BACKWARD SWIPE: no cover ──────────────────────────────────────────────────
 *
 * The PREV player is always paused at position 0 with its last decoded frame
 * alive in the SurfaceView buffer (rotate() calls prepare() but NOT play(),
 * preserving the frame). The cover is never shown for PREV pages.
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

    // pageIndex → whether the gradient loading cover should be shown.
    // Only ever set to true for the NEXT page when isNextReady() == false.
    // Cleared immediately when the page becomes CURRENT or when NEXT becomes ready.
    val showCoverForPage = remember { mutableStateMapOf<Int, Boolean>() }

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

            // ── Cover map update ──────────────────────────────────────────────
            // The page that just became CURRENT must never show the cover —
            // its player was already prepared (as NEXT before the swipe).
            // Clear any cover that may have been set for s when it was NEXT.
            showCoverForPage.remove(s)

            // The old CURRENT page (now PREV or NEXT depending on direction)
            // should also have its cover cleared — it has a live frame.
            when {
                direction > 0 -> showCoverForPage.remove(s - 1)
                direction < 0 -> showCoverForPage.remove(s + 1)
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

    // ── Poll: NEXT player readiness — drives gradient cover ───────────────────
    //
    // Polls pool.isNextReady() every 100 ms.
    //
    // When the user is actively scrolling forward and NEXT is not ready,
    // we mark the NEXT page with a cover. The cover is cleared the moment
    // isNextReady() flips to true — TikTokVideoItem then fades it out
    // smoothly (200 ms animateFloatAsState) to reveal the live SurfaceView.
    //
    // Flicker prevention:
    //   • We only SET the cover (showCoverForPage[nextPage] = true) while
    //     the pager is actively scrolling forward. We never re-add it after
    //     the page has settled (that would flash the cover on a ready player).
    //   • We CLEAR it (remove) as soon as ready, regardless of scroll state,
    //     so the fade-out begins at the earliest possible moment.
    //   • The soft race where rotate() changes the NEXT slot between the poll
    //     read and the showCoverForPage write is harmless: at worst the cover
    //     shows for one extra 100 ms poll cycle on the new NEXT page — still
    //     correct because that page genuinely may not be ready yet.
    LaunchedEffect(Unit) {
        while (true) {
            val ready = pool.isNextReady()

            if (ready != nextPlayerReady) {
                nextPlayerReady = ready
            }

            val s        = pagerState.settledPage
            val nextPage = s + 1

            if (!ready && pagerState.isScrollInProgress) {
                // Scrolling forward into a not-yet-ready NEXT page — show cover.
                // Guard: only for forward scroll (offset fraction > 0).
                val offsetFraction = pagerState.currentPage + pagerState.currentPageOffsetFraction - s
                if (offsetFraction > 0.01f && nextPage < feed.size) {
                    showCoverForPage[nextPage] = true
                }
            } else if (ready) {
                // NEXT is ready — clear its cover so TikTokVideoItem can fade it out.
                if (showCoverForPage[nextPage] == true) {
                    showCoverForPage.remove(nextPage)
                }
            }

            delay(100)
        }
    }

    // ── Fling + scroll connection ─────────────────────────────────────────────
    val flingBehavior    = rememberTikTokFlingBehavior(pagerState)
    val scrollConnection = rememberSinglePageScrollConnection(pagerState, pageHeight)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp)
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

            // showLoadingCover is true only for the NEXT page when its player
            // has not yet produced its first frame. It is never set for CURRENT
            // or PREV. TikTokVideoItem fades it out once the player is ready.
            val showLoadingCover = showCoverForPage[page] == true

            TikTokVideoItem(
                player            = player,
                isPlaying         = pagerState.settledPage == page,
                isScrolling       = pagerState.isScrollInProgress,
                showLoadingCover  = showLoadingCover,
                onPause           = { pool.pauseCurrentPlayer() },
                onResume          = { pool.resumeCurrentPlayer() },
            )
        }

        // ── CircularProgressIndicator removed ────────────────────────────────
        // Replaced by the per-page gradient cover inside TikTokVideoItem.
        // The gradient is visually superior (matches the reference design) and
        // avoids the z-ordering issue where the spinner floated above ALL pages
        // simultaneously, including ones that were actually ready.
    }
}