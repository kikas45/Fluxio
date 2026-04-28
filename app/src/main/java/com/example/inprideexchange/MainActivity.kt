package com.example.inprideexchange

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.inprideexchange.ui.InstaGramReel.ReelsView
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
                AppNavigation(navController = navController)
              //  ReelsView()
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        super.onBackPressed()
        Toast.makeText(applicationContext, "BAck pressed called", Toast.LENGTH_SHORT).show()
    }
}