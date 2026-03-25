package com.example.inprideexchange.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.example.inprideexchange.appThemeScreen.ThemeDataStore
import com.example.inprideexchange.ui.designsystem.typography.AppTypography


@Composable
fun InPrideExchangeTheme(
    appTheme: ThemeDataStore.AppTheme,
    colorSchemeChoice: ThemeDataStore.AppColorScheme = ThemeDataStore.AppColorScheme.ORANGE,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        ThemeDataStore.AppTheme.LIGHT -> false
        ThemeDataStore.AppTheme.DARK -> true
        ThemeDataStore.AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current

    val isDynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val useDynamic = colorSchemeChoice == ThemeDataStore.AppColorScheme.DYNAMIC && isDynamicSupported

    val colors = when {
        useDynamic -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        else -> {
            when (colorSchemeChoice) {
                ThemeDataStore.AppColorScheme.ORANGE -> if (darkTheme) OrangeDarkColors else OrangeLightColors
                ThemeDataStore.AppColorScheme.BLUE -> if (darkTheme) BlueDarkColors else BlueLightColors
                ThemeDataStore.AppColorScheme.GREEN -> if (darkTheme) GreenDarkColors else GreenLightColors
                ThemeDataStore.AppColorScheme.BLACK -> if (darkTheme) BlackDarkColors else BlackLightColors
                ThemeDataStore.AppColorScheme.DYNAMIC -> if (darkTheme) OrangeDarkColors else OrangeLightColors
            }
        }
    }

    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = AppTypography
        ) {
            SetSystemBarsColor()
            content()
        }
    }
}


