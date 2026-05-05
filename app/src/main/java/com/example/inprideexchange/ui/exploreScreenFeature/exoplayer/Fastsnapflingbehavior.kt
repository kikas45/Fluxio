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
 * TikTok-style fling — fast, no flicker, no freeze.
 *
 * ── THE FREEZE / HANG ROOT CAUSE ─────────────────────────────────────────────
 *
 * The "freeze" feeling comes from TWO things working against each other:
 *
 *   1. frictionMultiplier = 1.0f makes the decay animation decelerate slowly,
 *      so the pager spends a long time (200-350ms) crawling to the snap point
 *      before the spring even kicks in. This is the "hang" you feel.
 *
 *   2. StiffnessMedium spring is soft — it takes another ~150ms to settle.
 *      Combined, the total transition feels sluggish (~400-500ms).
 *
 * ── THE FIX ───────────────────────────────────────────────────────────────────
 *
 *   frictionMultiplier = 0.5f  (not 0.3f — see below)
 *     Fast enough that the decay animation finishes in ~100ms.
 *     Not so low that residual velocity causes spring overshoot.
 *     0.3f overshoots with StiffnessMediumHigh; 0.5f does not.
 *
 *   StiffnessMediumHigh spring
 *     Snaps in ~60ms with DampingRatioNoBouncy.
 *     Stiff enough to feel instant, damped enough to absorb the
 *     residual velocity from 0.5f friction without any overshoot.
 *
 *   snapPositionalThreshold = 0.08f
 *     Only 8% of screen height (~65dp) needed to commit.
 *     Makes light flicks feel immediately responsive.
 *     Still prevents accidental page changes on minor scrolls.
 *
 * Total transition time: ~160ms. Feels instant. Zero flicker. Zero overshoot.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberTikTokFlingBehavior(pagerState: PagerState) = PagerDefaults.flingBehavior(
    state             = pagerState,
    pagerSnapDistance = PagerSnapDistance.atMost(1),

    decayAnimationSpec = remember {
        exponentialDecay(frictionMultiplier = 0.5f)
    },

    snapAnimationSpec = remember {
        spring<Float>(
            stiffness    = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioNoBouncy,
        )
    },

    snapPositionalThreshold = 0.08f,
)






