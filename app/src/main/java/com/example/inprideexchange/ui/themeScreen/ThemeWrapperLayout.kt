package com.example.inprideexchange.ui.themeScreen

import com.example.inprideexchange.ui.components.uiRenders.OneTimeFadeInContent
import com.example.inprideexchange.ui.components.uiRenders.ResponsiveStandardContainer
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension


@Composable
fun ThemeWrapperLayout(
    onBack: () -> Unit = {},
    onHelp: () -> Unit = {},
    helpText: String = "",
    content: @Composable () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    DisposableEffect(Unit) {
        onDispose { keyboardController?.hide() }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.safeDrawing.asPaddingValues())
        ) {
            val (topBar, bodyContent) = createRefs()

            ThemeToolBar(
                title = helpText,
                onBack = onBack,
                onTitleClick = onHelp,
                modifier = Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            ResponsiveStandardContainer(
                modifier = Modifier.constrainAs(bodyContent) {
                    top.linkTo(topBar.bottom, margin = 8.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
            ) {
                OneTimeFadeInContent(
                    playAnimation = false,
                    delayMillis = 300
                ) {
                    content()
                }
            }
        }
    }
}
