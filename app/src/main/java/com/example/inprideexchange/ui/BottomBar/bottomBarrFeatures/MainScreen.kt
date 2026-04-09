package com.example.inprideexchange.ui.BottomBar.bottomBarrFeatures

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.inprideexchange.ui.navigation.BottomBarNavigation
import com.example.inprideexchange.ui.theme.SetSystemBarsColor


@Composable
fun MainScreen(topNavController: NavHostController) {

    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()

    val bottomBarScreens = listOf(
        BottomNavItem.ScreenA.route,
        BottomNavItem.ScreenB.route,
        BottomNavItem.ScreenC.route
    )

    val showBottomBar = navBackStackEntry?.destination?.route in bottomBarScreens

    // 🔥 Decide bottom bar color here
    val bottomBarColor = Color.Red // or MaterialTheme.colorScheme.surfaceVariant

    // 🔥 Sync system nav bar with bottom bar
    if (showBottomBar) {
        SetSystemBarsColor(
            statusBarColor = MaterialTheme.colorScheme.background,
            navigationBarColor = bottomBarColor
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    AppBottomBar(
                        currentRoute = navBackStackEntry?.destination?.route,
                        onItemClick = { item ->
                            bottomNavController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(bottomNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        },
                        containerColor = bottomBarColor // 👈 pass it down
                    )
                }
            }
        ) { padding ->
            BottomBarNavigation(
                navController = bottomNavController,
                modifier = Modifier.padding(padding),
                topNavController = topNavController
            )
        }
    }
}












/*
@Composable
fun MainScreen(topNavController: NavHostController) {

    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()

    val bottomBarScreens = listOf(
        BottomNavItem.ScreenA.route,
        BottomNavItem.ScreenB.route,
        BottomNavItem.ScreenC.route
    )

    val showBottomBar = navBackStackEntry?.destination?.route in bottomBarScreens

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    AppBottomBar(
                        currentRoute = navBackStackEntry?.destination?.route,
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
        ) { padding ->
            BottomBarNavigation(
                navController = bottomNavController,
                modifier = Modifier.padding(padding),
                topNavController = topNavController
            )
        }
    }
}

*/
