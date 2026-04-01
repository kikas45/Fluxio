package com.example.inprideexchange.ui.themeScreen

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.inprideexchange.appThemeScreen.ThemeDataStore
import com.example.inprideexchange.ui.components.buttons.SmartProgressBarButton
import com.example.inprideexchange.ui.designSystem.dimens.AppDimens

@Composable
fun ThemeScreen(
    state: ThemeUiState,
    onThemeSelected: (ThemeDataStore.AppTheme) -> Unit,
    onColorSelected: (ThemeDataStore.AppColorScheme) -> Unit,
    onContinue: () -> Unit
) {

    if (state.isLoading) return

    val isDynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val themeOptions = listOf(
        ThemeDataStore.AppTheme.LIGHT,
        ThemeDataStore.AppTheme.DARK,
        ThemeDataStore.AppTheme.SYSTEM
    )

    val colorOptions = remember(isDynamicSupported) {
        if (isDynamicSupported) {
            listOf(
                ThemeDataStore.AppColorScheme.DYNAMIC,
                ThemeDataStore.AppColorScheme.ORANGE,
                ThemeDataStore.AppColorScheme.BLUE,
                ThemeDataStore.AppColorScheme.GREEN,
                ThemeDataStore.AppColorScheme.BLACK
            )
        } else {
            listOf(
                ThemeDataStore.AppColorScheme.ORANGE,
                ThemeDataStore.AppColorScheme.BLUE,
                ThemeDataStore.AppColorScheme.GREEN,
                ThemeDataStore.AppColorScheme.BLACK
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppDimens.PaddingLarge),
            verticalArrangement = Arrangement.Top
        ) {

            // Text(text = "Appearance", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(AppDimens.PaddingMedium))

            Text(
                text = "Choose how your app looks and feels",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(AppDimens.PaddingLarge))

            // THEME
            Text("Theme Mode", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(AppDimens.PaddingSmall))

            themeOptions.forEach { theme ->
                ThemeItem(
                    title = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = state.theme == theme,
                    onClick = { onThemeSelected(theme) }
                )
            }

            Spacer(Modifier.height(AppDimens.PaddingLarge))

            // COLORS
            Text("Color Scheme", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(AppDimens.PaddingSmall))

            colorOptions.forEach { color ->
                ThemeItem(
                    title = color.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = state.colorScheme == color,
                    onClick = { onColorSelected(color) }
                )
            }

            Spacer(Modifier.height(AppDimens.PaddingLarge))

            /*            Button(
                            onClick = onContinue,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Continue")
                        }*/



            SmartProgressBarButton(
                text = "Continue with phone",
                isEnabled = true,
                isLoading = false,

                onClick = {
                },

                modifier = Modifier.fillMaxWidth()
            )


        }
    }
}

@Preview
@Composable
fun ThemeScreenPreview() {

    ThemeScreen(
        state = ThemeUiState(
            theme = ThemeDataStore.AppTheme.DARK,
            colorScheme = ThemeDataStore.AppColorScheme.ORANGE,
            isLoading = false
        ),
        onThemeSelected = {},
        onColorSelected = {},
        onContinue = {}
    )

}