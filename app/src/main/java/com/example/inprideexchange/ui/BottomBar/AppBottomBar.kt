package com.example.inprideexchange.ui.BottomBar

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit
) {
    val items = listOf(
        BottomNavItem.Explore,
        BottomNavItem.Wishlist,
        BottomNavItem.Trips,
        BottomNavItem.Messages,
        BottomNavItem.Profile
    )

    NavigationBar(
        tonalElevation = 2.dp,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        items.forEach { item ->

            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onItemClick(item) },
                icon = {
                    Icon(item.icon, contentDescription = item.label)
                },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }
}