package com.example.inprideexchange.ui.navigation
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.inprideexchange.Utils.Constants
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import com.example.inprideexchange.ui.registerfeature.RegisterUserEmail
import com.example.inprideexchange.ui.themefeature.ThemeRoute


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController
) {

    NavHost(
        navController = navController,
        startDestination = Constants.SCREEN_THEME,

        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(MaterialTheme.colorScheme.background),

        enterTransition = ProtonNavAnimation.enter,
        exitTransition = ProtonNavAnimation.exit,
        popEnterTransition = ProtonNavAnimation.popEnter,
        popExitTransition = ProtonNavAnimation.popExit
    ) {

        // ✅ Theme Screen (First screen)
        composable(Constants.SCREEN_THEME) {
            ThemeRoute(
                onContinue = {
                    // 👉 later: navigate to Home
                    // navController.navigate(Constants.SCREEN_HOME)
                }
            )
        }

        composable(Constants.RegisterUserEmail) {
            RegisterUserEmail()
        }
    }
}