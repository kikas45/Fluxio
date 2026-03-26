package com.example.inprideexchange.ui.components.uiRenders

import android.content.res.Configuration
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp


@Composable
fun ResponsiveStandardContainer(
    modifier: Modifier = Modifier, // ✅ REQUIRED for ConstraintLayout
    content: @Composable () -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val configuration = LocalConfiguration.current
        val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        // ✅ Decide if scrolling is needed
        val shouldScroll = !isPortrait || (isPortrait && maxHeight < 600.dp)

        val columnModifier = if (shouldScroll) {
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        } else {
            Modifier.fillMaxSize()
        }

        Column(modifier = columnModifier) {
            content()
        }
    }
}

