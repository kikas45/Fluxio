package com.example.inprideexchange.ui.BottomBar.bottomBarrFeatures

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.inprideexchange.Utils.Constants

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {

    object ScreenA : BottomNavItem(Constants.ScreenA, Icons.Default.Search, "Explore")
    object ScreenB : BottomNavItem(Constants.ScreenB, Icons.Default.FavoriteBorder, "Wishlist")
    object ScreenC : BottomNavItem(Constants.ScreenC, Icons.Default.AirplanemodeActive, "Trips")
}