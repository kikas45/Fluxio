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
import com.example.inprideexchange.AppScreens.UserReg.SplashScreen
import com.example.inprideexchange.AppScreens.UserReg.WelcomeScreen
import com.example.inprideexchange.ui.bottomBar.bottomBarrFeatures.MainScreen
import com.example.inprideexchange.ui.sampleScreen.SampleScreen
import com.example.inprideexchange.ui.registerScreen.RegisterUserEmail
import com.example.inprideexchange.ui.themeScreen.ThemeRoute



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController
) {

    NavHost(
        navController = navController,
        startDestination = Constants.SplashScreen,

        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(MaterialTheme.colorScheme.background),

        enterTransition = ProtonNavAnimation.enter,
        exitTransition = ProtonNavAnimation.exit,
        popEnterTransition = ProtonNavAnimation.popEnter,
        popExitTransition = ProtonNavAnimation.popExit
    ) {



        composable(Constants.SplashScreen) {
            SplashScreen(
                onFinished ={
                    navController.navigate(Constants.WelcomeScreen)
                }
            )
        }



        // ✅ Theme Screen (First screen)
        composable(Constants.SCREEN_THEME) {
            ThemeRoute(
                onContinue = {
                    // 👉 later: navigate to Home
                     navController.navigate(Constants.RegisterUserEmail)
                }
            )
        }

        composable(Constants.RegisterUserEmail) {
            RegisterUserEmail()
        }



        composable(Constants.WelcomeScreen) {
            WelcomeScreen(
                onPhoneClick = {
                    navController.navigate(Constants.RegisterUserEmail)
                },
                onGoogleClick = {
                    navController.navigate(Constants.SCREEN_THEME)
                },
                onSkip = {
                    navController.navigate(Constants.MainScreen) {
                        popUpTo(Constants.WelcomeScreen) { inclusive = true }
                    }
                },
                isLoading = false
            )
        }




        ////  Down Below , we shall now be working With Bottom Bar and how to Navigate Outside it


        // Main screen with bottom bar, we pass NavController to it and it then manage Bottom bar
        composable(Constants.MainScreen) {
            MainScreen(topNavController = navController)
        }

        // Full-screen screen, outside bottom bar
        composable(Constants.SampleScreen) {
            SampleScreen()
        }






    }
}