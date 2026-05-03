package com.example.inprideexchange.ui.theme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object BrandText {

    /**
     * Absolute hero text (titles, headers)
     */
    val hero: @Composable () -> Color = {
        if (LocalIsDarkTheme.current) {
            Color.Black   // dark → absolute white
        } else {
            Color.White   // light → absolute black
        }
    }



    /**
     * Softer secondary hero text
     */
    val heroSecondary: @Composable () -> Color = {
        if (LocalIsDarkTheme.current) {
            Color(0xFFF2F2F2)
        } else {
            Color(0xFF1C1C1C)
        }
    }

}