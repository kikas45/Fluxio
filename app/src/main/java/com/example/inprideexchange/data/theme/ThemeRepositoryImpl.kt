package com.example.inprideexchange.data.theme
import com.example.inprideexchange.appThemeScreen.ThemeDataStore
import jakarta.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val dataStore: ThemeDataStore
) : ThemeRepository {

    override val theme = dataStore.theme
    override val colorScheme = dataStore.colorScheme

    override suspend fun setTheme(theme: ThemeDataStore.AppTheme) {
        dataStore.setTheme(theme)
    }

    override suspend fun setColorScheme(color: ThemeDataStore.AppColorScheme) {
        dataStore.setColorScheme(color)
    }
}