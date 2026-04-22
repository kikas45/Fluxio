package com.example.inprideexchange.ui.bottomBar.bottomBarrFeatures
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.example.inprideexchange.ui.navigation.BottomBarNavigation


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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
        ) {
            BottomBarNavigation(
                navController = bottomNavController,
                topNavController = topNavController
            )
        }
    }
}


