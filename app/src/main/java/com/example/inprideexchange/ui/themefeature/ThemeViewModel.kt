package com.example.inprideexchange.ui.themefeature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inprideexchange.appThemeScreen.ThemeDataStore
import com.example.inprideexchange.data.theme.ThemeRepository
import com.example.inprideexchange.ui.theme.ThemeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val repository: ThemeRepository
) : ViewModel() {

    val currentTheme: StateFlow<ThemeDataStore.AppTheme> =
        repository.theme.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeDataStore.AppTheme.SYSTEM
        )

    val currentColorScheme: StateFlow<ThemeDataStore.AppColorScheme> =
        repository.colorScheme.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeDataStore.AppColorScheme.ORANGE
        )

    // UI STATE (for screen)
    val state: StateFlow<ThemeUiState> =
        combine(
            repository.theme,
            repository.colorScheme
        ) { theme, color ->
            ThemeUiState(
                theme = theme,
                colorScheme = color,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeUiState()
        )

    fun onThemeSelected(theme: ThemeDataStore.AppTheme) {
        viewModelScope.launch {
            repository.setTheme(theme)
        }
    }

    fun onColorSelected(color: ThemeDataStore.AppColorScheme) {
        viewModelScope.launch {
            repository.setColorScheme(color)
        }
    }
}