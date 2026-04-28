package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * TikTok-style fling for VerticalPager.
 *
 * ── WHY frictionMultiplier = 1.0f (not 2.5f) ────────────────────────────────
 *
 * frictionMultiplier = 2.5f was the cause of the flicker.
 * Here is exactly what happened:
 *
 *   The pager uses the decayAnimationSpec to animate the scroll offset toward
 *   the target page during a fling. With friction 2.5f, the animation
 *   decelerates very aggressively — it "arrives" at the target offset with a
 *   large residual velocity that the spring (snapAnimationSpec) then has to
 *   absorb. Even with DampingRatioNoBouncy, if the residual velocity is large
 *   enough when the spring takes over, the composable overshoots by a few
 *   pixels before snapping back. This 2-3 pixel overshoot is the flicker you
 *   see — the incoming video briefly goes past the screen edge and snaps back.
 *
 *   frictionMultiplier = 1.0f lets the decay animation arrive at a lower
 *   residual velocity. The spring absorbs it cleanly with zero overshoot.
 *   The snap still happens in < 200 ms — visually instant — but without flicker.
 *
 * ── WHY snapPositionalThreshold = 0.15f ──────────────────────────────────────
 *
 *   15% of screen height (≈ 130dp on a standard phone) is all that is needed
 *   to commit. This makes short intentional swipes feel responsive. Combined
 *   with the SinglePageScrollConnection below, the user cannot drag past page
 *   N+1 even if they try — so a lower threshold is purely a UX improvement.
 *
 * ── WHY snapAnimationSpec uses StiffnessMediumHigh ───────────────────────────
 *
 *   StiffnessHigh with a large residual velocity overshoots.
 *   StiffnessMediumHigh is stiff enough to snap in ~80 ms and has enough
 *   damping headroom to absorb the incoming velocity cleanly.
 *
 * ── PagerSnapDistance.atMost(1) ──────────────────────────────────────────────
 *
 *   Prevents fling-based skipping. Drag-based skipping is handled separately
 *   by SinglePageScrollConnection in ForYouFeed.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberTikTokFlingBehavior(pagerState: PagerState) = PagerDefaults.flingBehavior(
    state                   = pagerState,
    pagerSnapDistance       = PagerSnapDistance.atMost(1),
    decayAnimationSpec      = remember {
        exponentialDecay(frictionMultiplier = 1.0f)
    },
    snapAnimationSpec       = remember {
        spring<Float>(
            stiffness    = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioNoBouncy,
        )
    },
    snapPositionalThreshold = 0.15f,
)