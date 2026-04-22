package com.example.inprideexchange.ui.bottomBar.bottomBarrFeatures

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

