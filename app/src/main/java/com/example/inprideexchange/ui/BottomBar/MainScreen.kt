package com.example.inprideexchange.ui.BottomBar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.inprideexchange.Utils.Constants

@Composable
fun MainScreen() {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val visitedScreens = remember {
        mutableStateMapOf(
            Constants.Explore to false,
            Constants.Wishlist to false,
            Constants.Trips to false,
            Constants.Messages to false,
            Constants.Profile to false
        )
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentRoute = navBackStackEntry?.destination?.route,
                onItemClick = { item ->
                    navController.navigate(item.route) {
                        popUpTo(Constants.Explore)
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Constants.Explore,
            modifier = Modifier.padding(padding)
        ) {

//            composable(Constants.Explore) { Text("Explore Screen") }
//            composable(Constants.Wishlist) { Text("Wishlist Screen") }
//            composable(Constants.Trips) { Text("Trips Screen") }
//            composable(Constants.Messages) { Text("Messages Screen") }
//            composable(Constants.Profile) { Text("Profile Screen") }



            composable(Constants.Explore) {
                MarkVisited(Constants.Explore, visitedScreens)
                ExploreScreen(isVisited = visitedScreens[Constants.Explore] == true)
            }

            composable(Constants.Wishlist) {
                MarkVisited(Constants.Wishlist, visitedScreens)
                WishlistScreen(isVisited = visitedScreens[Constants.Wishlist] == true)
            }

            composable(Constants.Trips) {
                MarkVisited(Constants.Trips, visitedScreens)
                TripsScreen(isVisited = visitedScreens[Constants.Trips] == true)
            }

            composable(Constants.Messages) {
                MarkVisited(Constants.Messages, visitedScreens)
                MessagesScreen(isVisited = visitedScreens[Constants.Messages] == true)
            }

            composable(Constants.Profile) {
                MarkVisited(Constants.Profile, visitedScreens)
                ProfileScreen(isVisited = visitedScreens[Constants.Profile] == true)
            }




        }
    }
}