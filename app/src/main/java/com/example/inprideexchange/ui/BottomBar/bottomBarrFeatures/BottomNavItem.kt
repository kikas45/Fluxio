package com.example.inprideexchange.ui.BottomBar.bottomBarrFeatures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AirplanemodeActive
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.inprideexchange.Utils.Constants

sealed class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
) {

    object ScreenA : BottomNavItem(
        route = Constants.ScreenA,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
        label = "Explore"
    )

    object ScreenB : BottomNavItem(
        route = Constants.ScreenB,
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder,
        label = "Wishlist"
    )

    object ScreenC : BottomNavItem(
        route = Constants.ScreenC,
        selectedIcon = Icons.Filled.AirplanemodeActive,
        unselectedIcon = Icons.Outlined.AirplanemodeActive,
        label = "Trips"
    )
}
