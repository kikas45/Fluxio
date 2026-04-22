package com.example.inprideexchange.data.theme
import com.example.inprideexchange.appThemeScreen.ThemeDataStore
import kotlinx.coroutines.flow.Flow



interface ThemeRepository {
    val theme: Flow<ThemeDataStore.AppTheme>
    val colorScheme: Flow<ThemeDataStore.AppColorScheme>

    suspend fun setTheme(theme: ThemeDataStore.AppTheme)
    suspend fun setColorScheme(color: ThemeDataStore.AppColorScheme)
}