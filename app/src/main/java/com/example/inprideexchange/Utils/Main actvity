package com.example.inprideexchange

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.inprideexchange.ui.navigation.AppNavigation
import com.example.inprideexchange.ui.theme.InPrideExchangeTheme
import com.example.inprideexchange.ui.themeScreen.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val currentTheme by themeViewModel.currentTheme.collectAsState()
            val currentColorScheme by themeViewModel.currentColorScheme.collectAsState()

            val navController = rememberNavController()

            InPrideExchangeTheme(
                appTheme = currentTheme,
                colorSchemeChoice = currentColorScheme
            ) {
                AppNavigation(
                    navController = navController
                )
            }
        }
    }
}