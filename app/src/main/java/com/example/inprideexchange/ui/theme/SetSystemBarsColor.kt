package com.example.inprideexchange.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@Composable
fun SetSystemBarsColor(
    statusBarColor: Color = MaterialTheme.colorScheme.background,
    navigationBarColor: Color = MaterialTheme.colorScheme.background
) {
    val systemUiController = rememberSystemUiController()

    val useDarkIcons = navigationBarColor.luminance() > 0.5f

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = statusBarColor,
            darkIcons = useDarkIcons
        )

        systemUiController.setNavigationBarColor(
            color = navigationBarColor,
            darkIcons = useDarkIcons
        )
    }
}





/*
@Composable
fun SetSystemBarsColor() {
    val systemUiController = rememberSystemUiController()
    val colors = MaterialTheme.colorScheme
    val useDarkIcons = colors.background.luminance() > 0.5f

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = colors.background,
            darkIcons = useDarkIcons
        )
        systemUiController.setNavigationBarColor(
            color = colors.background,
            darkIcons = useDarkIcons
        )
    }
}

*/
