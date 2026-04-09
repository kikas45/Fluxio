package com.example.inprideexchange.ui.BottomBar.bottomBarrFeatures

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight


@Composable
fun AppBottomBar(
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
    containerColor: Color // 👈 NEW
) {
    val items = listOf(
        BottomNavItem.ScreenA,
        BottomNavItem.ScreenB,
        BottomNavItem.ScreenC
    )

    NavigationBar(
        tonalElevation = 0.dp,
        containerColor = containerColor // 🔥 now dynamic
    ) {
        items.forEach { item ->

            val isSelected = currentRoute == item.route

            val targetColor = if (isSelected) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            val animatedColor by animateColorAsState(
                targetValue = targetColor,
                label = "nav_item_color"
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item) },

                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp),
                        tint = animatedColor
                    )
                },

                label = {
                    Text(
                        text = item.label,
                        color = animatedColor,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },

                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = containerColor, // 🔥 removes pill properly
                    selectedIconColor = animatedColor,
                    unselectedIconColor = animatedColor,
                    selectedTextColor = animatedColor,
                    unselectedTextColor = animatedColor
                )
            )
        }
    }
}




/*
@Composable
fun AppBottomBar(
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit
) {
    val items = listOf(
        BottomNavItem.ScreenA,
        BottomNavItem.ScreenB,
        BottomNavItem.ScreenC
    )

    NavigationBar(
        tonalElevation = 0.dp,
       // containerColor = MaterialTheme.colorScheme.background // 🔥 matches system nav bar
        containerColor = Color.Red // 🔥 matches system nav bar
    ) {
        items.forEach { item ->

            val isSelected = currentRoute == item.route

            val targetColor = if (isSelected) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            // 🎬 Smooth color animation
            val animatedColor by animateColorAsState(
                targetValue = targetColor,
                label = "nav_item_color"
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item) },

                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp),
                        tint = animatedColor
                    )
                },

                label = {
                    Text(
                        text = item.label,
                        color = animatedColor,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },

                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.background, // remove pill
                    selectedIconColor = animatedColor,
                    unselectedIconColor = animatedColor,
                    selectedTextColor = animatedColor,
                    unselectedTextColor = animatedColor
                )
            )
        }
    }
}

*/
