package com.example.inprideexchange.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.inprideexchange.Utils.Constants
import com.example.inprideexchange.ui.bottomBar.bottomBarrFeatures.BottomNavItem
import com.example.inprideexchange.ui.bottomBar.tipScreen.TripsScreen
import com.example.inprideexchange.ui.bottomBar.tipScreen.TripsViewModel
import com.example.inprideexchange.ui.bottomBar.wishScreen.WishlistScreen
import com.example.inprideexchange.ui.exploreScreenFeature.ExploreScreen
import com.example.inprideexchange.ui.exploreScreenFeature.exoplayer.SeekBarViewModel

@Composable
fun BottomBarNavigation(
    navController    : NavHostController,
    modifier         : Modifier = Modifier,
    topNavController : NavHostController,
    seekBarViewModel : SeekBarViewModel,          // ← passed in, not created here
) {
    NavHost(
        navController     = navController,
        startDestination  = BottomNavItem.ScreenA.route,
        modifier          = modifier,
        enterTransition   = { EnterTransition.None },
        exitTransition    = { ExitTransition.None },
        popEnterTransition  = { EnterTransition.None },
        popExitTransition   = { ExitTransition.None }
    ) {

        composable(BottomNavItem.ScreenA.route) {
            ExploreScreen(seekBarViewModel = seekBarViewModel)  // ← forwarded
        }

        composable(BottomNavItem.ScreenB.route) {
            WishlistScreen(
                onNavigateToSampleScreen = {
                    topNavController.navigate(Constants.SCREEN_THEME)
                }
            )
        }

        composable(BottomNavItem.ScreenC.route) { backStackEntry ->
            val tripsViewModel: TripsViewModel = viewModel(backStackEntry)
            TripsScreen(tripsViewModel)
        }
    }
}