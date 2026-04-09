package com.example.inprideexchange.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.inprideexchange.Utils.Constants
import com.example.inprideexchange.ui.BottomBar.bottomBarrFeatures.BottomNavItem
import com.example.inprideexchange.ui.BottomBar.exploreScreen.ExploreScreen
import com.example.inprideexchange.ui.BottomBar.tipScreen.TripsScreen
import com.example.inprideexchange.ui.BottomBar.tipScreen.TripsViewModel
import com.example.inprideexchange.ui.BottomBar.wishScreen.WishlistScreen

@Composable
fun BottomBarNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    topNavController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.ScreenA.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.ScreenA.route) {
            ExploreScreen()
        }

        composable(BottomNavItem.ScreenB.route) {
            WishlistScreen(
                onNavigateToSampleScreen = {
                    topNavController.navigate(Constants.SCREEN_THEME)
                }
            )
        }


        composable(BottomNavItem.ScreenC.route) { backStackEntry ->

            // ✅ CORRECT ViewModel scoping
            val tripsViewModel: TripsViewModel = viewModel(backStackEntry)

            TripsScreen(tripsViewModel)
        }
    }
}