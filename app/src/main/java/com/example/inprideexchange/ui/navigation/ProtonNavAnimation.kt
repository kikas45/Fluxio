package com.example.inprideexchange.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut

object ProtonNavAnimation {

    // ---------- Forward navigation: A → B ----------
    val enter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        scaleIn(
            initialScale = 0.82f,
            animationSpec = tween(
                durationMillis = 600,
                delayMillis = 30,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            initialAlpha = 0.05f,
            animationSpec = tween(
                durationMillis = 600,
                delayMillis = 30,
                easing = FastOutSlowInEasing
            )
        )
    }

    val exit: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(
                durationMillis = 250,
                easing = FastOutSlowInEasing
            )
        ) + fadeOut(
            targetAlpha = 0.0f,
            animationSpec = tween(
                durationMillis = 250,
                easing = FastOutSlowInEasing
            )
        )
    }

    // ---------- Back navigation: B → A ----------
    val popEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        scaleIn(
            initialScale = 0.88f,
            animationSpec = tween(
                durationMillis = 550,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            initialAlpha = 0.05f,
            animationSpec = tween(
                durationMillis = 550,
                easing = FastOutSlowInEasing
            )
        )
    }

    val popExit: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(
                durationMillis = 250,
                easing = FastOutSlowInEasing
            )
        ) + fadeOut(
            targetAlpha = 0.0f,
            animationSpec = tween(
                durationMillis = 250,
                easing = FastOutSlowInEasing
            )
        )
    }
}