package com.example.inprideexchange.ui.BottomBar.bottomBarrFeatures

import androidx.annotation.DrawableRes
import com.example.inprideexchange.R
import com.example.inprideexchange.Utils.Constants

sealed class BottomNavItem(
    val route: String,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
    val label: String
) {

    object ScreenA : BottomNavItem(
        route = Constants.ScreenA,
        selectedIcon = R.drawable.home_24px_filled,
        unselectedIcon = R.drawable.home_24px_outline,
        label = "Home"
    )

    object ScreenB : BottomNavItem(
        route = Constants.ScreenB,
        selectedIcon = R.drawable.explore_filled,      // your drawable
        unselectedIcon = R.drawable.explore_outlined,   // your drawable
        label = "Wishlist"
    )

    object ScreenC : BottomNavItem(
        route = Constants.ScreenC,
        selectedIcon = R.drawable.account_circle_24px_filled,          // your drawable
        unselectedIcon = R.drawable.account_circle_24px_outline,       // your drawable
        label = "Trips"
    )
}




/*
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
*/
