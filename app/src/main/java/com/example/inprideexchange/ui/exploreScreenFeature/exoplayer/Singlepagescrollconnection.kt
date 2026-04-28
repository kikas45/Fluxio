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
import androidx.compose.ui.unit.dp

/**
 * A NestedScrollConnection that prevents the user from scrolling more than
 * one page away from the currently settled page in a single continuous drag.
 *
 * ── THE PROBLEM IT SOLVES ────────────────────────────────────────────────────
 *
 * PagerSnapDistance.atMost(1) only caps FLING-based navigation. It has no
 * effect on slow drag gestures. If the user places two fingers and drags
 * slowly, VerticalPager happily scrolls past page 2, 3, 4... because from
 * VerticalPager's perspective, it is just responding to raw scroll deltas.
 *
 * ── HOW IT WORKS ─────────────────────────────────────────────────────────────
 *
 * A NestedScrollConnection sits above VerticalPager in the scroll hierarchy.
 * It intercepts scroll deltas BEFORE they reach the pager.
 *
 * On every scroll delta:
 *   1. Calculate the current scroll offset in pages:
 *      currentScrollPages = (settledPage * pageHeight + scrollOffset) / pageHeight
 *   2. Calculate how far from the settled page we already are:
 *      distanceFromSettled = currentScrollPages - settledPage
 *   3. If distanceFromSettled is already ≥ 1.0 page in the scroll direction,
 *      consume the delta entirely (return it as "consumed") so zero reaches
 *      the pager. The pager cannot scroll further.
 *   4. If the delta would push us past the 1-page boundary, only pass through
 *      the portion that gets us exactly to the boundary. Consume the rest.
 *
 * The result: no matter how many fingers, how slow, how deliberate — the
 * pager's scroll offset is physically capped at currentSettledPage ± 1 page.
 * The user MUST lift their finger, let the pager snap to N+1, then drag again
 * to reach N+2.
 *
 * ── WHY PRE-SCROLL (not post-scroll) ─────────────────────────────────────────
 *
 * onPreScroll fires BEFORE the pager consumes the delta. If we wait for
 * onPostScroll, the pager has already moved — we can't un-move it. Pre-scroll
 * interception is the only correct place to enforce this limit.
 *
 * ── FLING ─────────────────────────────────────────────────────────────────────
 *
 * We return Velocity.Zero from onPreFling — we do NOT consume fling velocity.
 * PagerSnapDistance.atMost(1) already handles fling correctly. Consuming fling
 * here would break the snap animation.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberSinglePageScrollConnection(
    pagerState : PagerState,
    pageHeight : Dp,
): NestedScrollConnection {
    val density         = LocalDensity.current
    val pageHeightPx    = with(density) { pageHeight.toPx() }

    return remember(pagerState, pageHeightPx) {
        object : NestedScrollConnection {

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Only intercept drag (not fling animations — those use NestedScrollSource.Fling
                // but PagerSnapDistance handles those already).
                if (source != NestedScrollSource.Drag) return Offset.Zero
                if (pageHeightPx <= 0f) return Offset.Zero

                val deltaY       = available.y
                val settledPage  = pagerState.settledPage

                // currentPage is a Float representing the fractional scroll position.
                // e.g. 1.4 means we are 40% into page 2.
                val currentPage  = pagerState.currentPage +
                        pagerState.currentPageOffsetFraction

                // How many pages away from settled are we right now?
                val offsetFromSettled = currentPage - settledPage

                // --- Scrolling DOWN (deltaY < 0 = moving to next page) ---
                if (deltaY < 0) {
                    // Already at or past 1 page forward — consume everything
                    if (offsetFromSettled >= 1.0f) return available
                    // Would go past 1 page — consume the excess
                    val spaceLeft = (1.0f - offsetFromSettled) * pageHeightPx
                    return if (-deltaY > spaceLeft) {
                        Offset(0f, -((-deltaY) - spaceLeft))  // consume the excess
                    } else {
                        Offset.Zero  // still within bounds — don't consume
                    }
                }

                // --- Scrolling UP (deltaY > 0 = moving to previous page) ---
                if (deltaY > 0) {
                    // Already at or past 1 page backward — consume everything
                    if (offsetFromSettled <= -1.0f) return available
                    // Would go past 1 page back — consume the excess
                    val spaceLeft = (1.0f + offsetFromSettled) * pageHeightPx
                    return if (deltaY > spaceLeft) {
                        Offset(0f, deltaY - spaceLeft)  // consume the excess
                    } else {
                        Offset.Zero
                    }
                }

                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                // Do NOT consume fling. PagerSnapDistance.atMost(1) handles it.
                return Velocity.Zero
            }
        }
    }
}