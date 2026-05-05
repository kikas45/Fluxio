package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity

/**
 * Caps drag to ±1 page from the settled page.
 * PagerSnapDistance.atMost(1) handles fling — this handles slow drags.
 *
 * Optimised to exit as early as possible on the hot path (every drag frame).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberSinglePageScrollConnection(
    pagerState : PagerState,
    pageHeight : Dp,
): NestedScrollConnection {
    val density      = LocalDensity.current
    val pageHeightPx = with(density) { pageHeight.toPx() }

    return remember(pagerState, pageHeightPx) {
        object : NestedScrollConnection {

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // ── Fast exits ────────────────────────────────────────────────
                // Fling source is handled by PagerSnapDistance — don't touch it.
                // Zero delta and zero page height are no-ops.
                val deltaY = available.y
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                if (deltaY == 0f || pageHeightPx <= 0f)     return Offset.Zero

                val settled      = pagerState.settledPage
                val offsetFrac   = pagerState.currentPageOffsetFraction
                // currentPage as a continuous float, e.g. 1.4 = 40% into page 2
                val currentPage  = pagerState.currentPage + offsetFrac
                val fromSettled  = currentPage - settled

                return if (deltaY < 0) {
                    // ── Scrolling forward (next page) ─────────────────────────
                    when {
                        fromSettled >= 1f -> available                          // already at cap — block all
                        -deltaY > (1f - fromSettled) * pageHeightPx -> {
                            val allowed = (1f - fromSettled) * pageHeightPx    // only pass what fits
                            Offset(0f, -((-deltaY) - allowed))
                        }
                        else -> Offset.Zero
                    }
                } else {
                    // ── Scrolling backward (previous page) ────────────────────
                    when {
                        fromSettled <= -1f -> available                         // already at cap — block all
                        deltaY > (1f + fromSettled) * pageHeightPx -> {
                            val allowed = (1f + fromSettled) * pageHeightPx
                            Offset(0f, deltaY - allowed)
                        }
                        else -> Offset.Zero
                    }
                }
            }

            // Do NOT consume fling — PagerSnapDistance.atMost(1) owns it.
            override suspend fun onPreFling(available: Velocity) = Velocity.Zero
        }
    }
}

