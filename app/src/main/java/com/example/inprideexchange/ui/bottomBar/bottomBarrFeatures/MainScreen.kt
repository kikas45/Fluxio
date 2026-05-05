package com.example.inprideexchange.ui.bottomBar.bottomBarrFeatures

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.inprideexchange.ui.exploreScreenFeature.exoplayer.SeekBarViewModel
import com.example.inprideexchange.ui.navigation.BottomBarNavigation

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(topNavController: NavHostController) {

    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()

    val seekBarViewModel: SeekBarViewModel = viewModel()

    val bottomBarScreens = listOf(
        BottomNavItem.ScreenA.route,
        BottomNavItem.ScreenB.route,
        BottomNavItem.ScreenC.route
    )

    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarScreens

    // ── Hide seekbar whenever the user is NOT on ScreenA ─────────────────────
    LaunchedEffect(currentRoute) {
        if (currentRoute != BottomNavItem.ScreenA.route) {
            seekBarViewModel.updateIsVisible(false)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    AppBottomBar(
                        currentRoute     = currentRoute,
                        seekBarViewModel = seekBarViewModel,
                        onItemClick = { item ->
                            bottomNavController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(bottomNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        }
                    )
                }
            }
        ) {
            BottomBarNavigation(
                navController    = bottomNavController,
                topNavController = topNavController,
                seekBarViewModel = seekBarViewModel,
            )
        }
    }
}