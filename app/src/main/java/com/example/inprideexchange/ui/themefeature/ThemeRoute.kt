package com.example.inprideexchange.ui.themefeature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ThemeRoute(
    viewModel: ThemeViewModel = hiltViewModel(),
    onContinue: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    ThemeWrapperLayout(
        helpText = "Appearance",
        onBack = { /* handle back */ }
    ) {
        ThemeScreen(
            state = state,
            onThemeSelected = viewModel::onThemeSelected,
            onColorSelected = viewModel::onColorSelected,
            onContinue = onContinue
        )
    }
}