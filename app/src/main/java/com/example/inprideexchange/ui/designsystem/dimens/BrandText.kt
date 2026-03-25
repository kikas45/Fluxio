package com.example.inprideexchange.ui.designsystem.dimens



import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.inprideexchange.ui.theme.LocalIsDarkTheme

object BrandText {

    /**
     * Absolute hero text (titles, headers)
     */
    val hero: @Composable () -> Color = {
        if (LocalIsDarkTheme.current) {
            Color.White   // dark → absolute white
        } else {
            Color.Black   // light → absolute black
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


    //  more like dark gray type

    val heroAsh: @Composable () -> Color = {
        if (LocalIsDarkTheme.current) {
            Color.LightGray   // dark → absolute white
        } else {
            Color.Black   // light → absolute black
        }
    }

    val heroWhitewash: @Composable () -> Color = {
        if (LocalIsDarkTheme.current) {
            Color(0xFF969696)  // dark → absolute white
        } else {
            Color(0xFF7A7979)// light → absolute black
        }
    }



    val heroInverted: @Composable () -> Color = {
        if (LocalIsDarkTheme.current) {
            Color.Black
        } else {
            Color.White
        }
    }


    val heroInvertedBackground: @Composable () -> Color = {
        if (LocalIsDarkTheme.current) {
            Color.Black
        } else {
            Color(0xFFF3EDED)
        }
    }

    val heroCustomTextColor: @Composable () -> Color = {
        if (LocalIsDarkTheme.current) {
            MaterialTheme.colorScheme.primary
        } else {

            Color.White
        }
    }


}