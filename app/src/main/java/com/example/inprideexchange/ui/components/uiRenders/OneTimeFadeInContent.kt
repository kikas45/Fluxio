package com.example.inprideexchange.ui.components.uiRenders
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay


@Composable
fun OneTimeFadeInContent(
    playAnimation: Boolean,
    delayMillis: Long = 500,
    content: @Composable () -> Unit
) {
    var visible by rememberSaveable { mutableStateOf(!playAnimation) }

    LaunchedEffect(playAnimation) {
        if (playAnimation) {
            delay(delayMillis)
            visible = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (playAnimation && !visible) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(450))
        ) {
            content()
        }
    }
}


