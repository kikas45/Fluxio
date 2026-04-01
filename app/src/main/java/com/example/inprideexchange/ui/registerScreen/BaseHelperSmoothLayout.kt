package com.example.inprideexchange.ui.registerScreen


import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.inprideexchange.ui.components.uiRenders.OneTimeFadeInContent
import com.example.inprideexchange.ui.components.uiRenders.ResponsiveStandardContainer


@Composable
fun BaseHelperSmoothLayout(
    onBack: () -> Unit = {},
    onHelp: () -> Unit = {},
    helpText: String = "",
    showTopProgress: Boolean = false,
    content: @Composable () -> Unit
) {

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.safeDrawing.asPaddingValues())
        ) {



            val (topBar, progressBar, bodyContent) = createRefs()

            SimpleSmoothHelperToolBar(
                title = helpText,
                onBack = onBack,
                onTitleClick = onHelp,
                modifier = Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top, 8.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )


            ResponsiveStandardContainer(
                modifier = Modifier.constrainAs(bodyContent) {
                    top.linkTo(topBar.bottom, margin = 7.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
            ) {

                OneTimeFadeInContent(
                    playAnimation = true
                ) {
                    content()
                }

            }
        }
    }
}


