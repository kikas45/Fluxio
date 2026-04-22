package com.example.inprideexchange.appThemeScreen

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
private val Context.dataStore by preferencesDataStore("theme_prefs")

@Singleton
class ThemeDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    enum class AppTheme { LIGHT, DARK, SYSTEM }

    enum class AppColorScheme {
        DYNAMIC, ORANGE, BLUE, GREEN, BLACK
    }

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val COLOR_KEY = stringPreferencesKey("color")
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[THEME_KEY] = theme.name }
    }

    suspend fun setColorScheme(color: AppColorScheme) {
        context.dataStore.edit { it[COLOR_KEY] = color.name }
    }

    val theme: Flow<AppTheme> =
        context.dataStore.data.map {
            when (it[THEME_KEY]) {
                AppTheme.LIGHT.name -> AppTheme.LIGHT
                AppTheme.DARK.name -> AppTheme.DARK
                else -> AppTheme.SYSTEM
            }
        }

    val colorScheme: Flow<AppColorScheme> =
        context.dataStore.data.map {
            it[COLOR_KEY]?.let { saved ->
                AppColorScheme.valueOf(saved)
            } ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AppColorScheme.DYNAMIC
            } else {
                AppColorScheme.ORANGE
            }
        }
}