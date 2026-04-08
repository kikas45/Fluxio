package com.example.inprideexchange.ui.BottomBar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.inprideexchange.Utils.Constants

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Explore : BottomNavItem(Constants.Explore, Icons.Default.Search, "Explore")
    object Wishlist : BottomNavItem(Constants.Wishlist, Icons.Default.FavoriteBorder, "Wishlists")
    object Trips : BottomNavItem(Constants.Trips, Icons.Default.AirplanemodeActive, "Trips")
    object Messages : BottomNavItem(Constants.Messages, Icons.Default.Message, "Messages")
    object Profile : BottomNavItem(Constants.Profile, Icons.Default.Person, "Profile")
}