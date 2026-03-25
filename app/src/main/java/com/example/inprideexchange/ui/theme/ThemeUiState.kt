package com.example.inprideexchange.ui.theme
import com.example.inprideexchange.appThemeScreen.ThemeDataStore

data class ThemeUiState(
    val theme: ThemeDataStore.AppTheme = ThemeDataStore.AppTheme.SYSTEM,
    val colorScheme: ThemeDataStore.AppColorScheme = ThemeDataStore.AppColorScheme.ORANGE,
    val isLoading: Boolean = true
)