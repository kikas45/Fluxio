package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class, ExperimentalFoundationApi::class)
@Composable
fun ForYouFeed(
    seekBarViewModel: SeekBarViewModel,
) {
    val context   = LocalContext.current
    val feed      = rememberFeed()
    val pool      = remember { PlayerPool(context) }
    val preloader = remember { VideoPreloader(context) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount   = { feed.size }
    )

    val pageHeight  = LocalConfiguration.current.screenHeightDp.dp
    val slotForPage = remember { mutableStateMapOf<Int, Int>() }
    var lastSettled by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        onDispose { preloader.cancel(); pool.release() }
    }

    LaunchedEffect(Unit) {
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
            if (s > 0) slotForPage[s - 1] = PlayerPool.PREV
            slotForPage[s]     = PlayerPool.CURRENT
            slotForPage[s + 1] = PlayerPool.NEXT
            when {
                direction > 0 -> slotForPage.remove(s - 2)
                direction < 0 -> slotForPage.remove(s + 2)
            }
            when {
                direction > 0 -> pool.load(PlayerPool.NEXT, feed.getOrNull(s + 1)?.videoUrl)
                direction < 0 -> pool.load(PlayerPool.PREV, feed.getOrNull(s - 1)?.videoUrl)
            }
        }

        pool.playCurrentPlayer()

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

    val flingBehavior    = rememberTikTokFlingBehavior(pagerState)
    val scrollConnection = rememberSinglePageScrollConnection(pagerState, pageHeight)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
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

            // ── Per-page state ────────────────────────────────────────────────
            var actionState by remember { mutableStateOf(VideoActionState()) }
            val video       = feed.getOrNull(page)
            val isDragging  by seekBarViewModel.isDragging.collectAsState()

            ConstraintLayout(modifier = Modifier.fillMaxSize()) {

                val (videoRef, actionsRef, textRef) = createRefs()

                // ── Video ─────────────────────────────────────────────────────
                TikTokVideoItem(
                    player           = player,
                    isPlaying        = pagerState.settledPage == page,
                    isScrolling      = pagerState.isScrollInProgress,
                    onPause          = { pool.pauseCurrentPlayer() },
                    onResume         = { pool.resumeCurrentPlayer() },
                    seekBarViewModel = seekBarViewModel,
                    modifier         = Modifier.constrainAs(videoRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width  = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                    },
                )

                // ── Action buttons — hidden while dragging seekbar ────────────
                if (!isDragging) {
                    VideoActionButtons(
                        state     = actionState,
                        onLike    = {
                            actionState = actionState.copy(isLiked = !actionState.isLiked)
                        },
                        onComment = { /* TODO: open comments sheet */ },
                        onMore    = { /* TODO: show more options */ },
                        modifier  = Modifier.constrainAs(actionsRef) {
                            end.linkTo(parent.end, margin = 12.dp)
                            bottom.linkTo(textRef.bottom, margin = 120.dp)
                            top.linkTo(parent.top, margin = 20.dp)
                            verticalBias = 1f
                        },
                    )
                }

                // ── Title + description — hidden while dragging seekbar ────────
                if (!isDragging) {
                    VideoTextOverlay(
                        title       = "Lord of the Ring",
                        description = "Movie number one , best action fighting scene with proper recaps ",
                        modifier    = Modifier.constrainAs(textRef) {
                            start.linkTo(parent.start, margin = 12.dp)
                            end.linkTo(actionsRef.start, margin = 8.dp)
                            bottom.linkTo(parent.bottom, margin = 120.dp)
                            width = Dimension.fillToConstraints
                        },
                    )
                }

            }
        }
    }
}