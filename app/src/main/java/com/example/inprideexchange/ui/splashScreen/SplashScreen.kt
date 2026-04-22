package com.example.inprideexchange.AppScreens.UserReg

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.ui.tooling.preview.Preview
import com.example.inprideexchange.ui.designSystem.dimens.BrandText

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    val density = LocalDensity.current
    var textWidthPx by remember { mutableStateOf(0) }

    // 🔁 Infinite color animation
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val animatedColor by infiniteTransition.animateColor(
        initialValue = MaterialTheme.colorScheme.primary,
        targetValue = MaterialTheme.colorScheme.secondary,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    // 📊 Progress animation
    var startAnimation by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1500, easing = LinearEasing),
        label = ""
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1500)
        onFinished()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.safeDrawing.asPaddingValues())
        ) {

            val (textRef, progressRef) = createRefs()

            // ✨ Animated Text (centered)
            Text(
                text = buildAnnotatedString {

                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                    ) {
                        append("M")
                    }

                    withStyle(
                        style = SpanStyle(
                            color = BrandText.heroSecondary(),
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append("25")
                    }
                },
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = animatedColor,
                modifier = Modifier
                    .onGloballyPositioned {
                        textWidthPx = it.size.width
                    }
                    .constrainAs(textRef) {
                        centerTo(parent)
                    }
            )

            // 📊 Progress Bar (below text)
            if (textWidthPx > 0) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .width(with(density) { textWidthPx.toDp() + 15.dp })
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .constrainAs(progressRef) {
                            top.linkTo(textRef.bottom, margin = 8.dp)
                            start.linkTo(textRef.start)
                            end.linkTo(textRef.end)
                        },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(onFinished = {})
}