package com.example.inprideexchange.ui.exploreScreenFeature

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


/*
@Composable
fun ExploreTopBar(
    selectedTab: ExploreTabItem,
    onTabSelected: (ExploreTabItem) -> Unit,
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent) // 🔥 KEY
            .padding(top = 40.dp, start = 30.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(horizontalArrangement = Arrangement.spacedBy(30.dp)) {

            listOf(
                ExploreTabItem.Following,
                ExploreTabItem.ForYou
            ).forEach { tab ->

                Text(
                    text = tab.title,
                    color = if (tab == selectedTab)
                        Color.White // 🔥 TikTok uses white on video
                    else
                        Color.White.copy(alpha = 0.6f),

                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.clickable {
                        onTabSelected(tab)
                    }
                )
            }
        }

        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

*/


@Composable
fun ExploreTopBar(
    selectedTab: ExploreTabItem,
    onTabSelected: (ExploreTabItem) -> Unit,
    onSearchClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth().background(Color.Transparent)
            .padding(top = 40.dp, start = 30.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        // 🔥 LEFT + CENTER TABS
        Row(horizontalArrangement = Arrangement.spacedBy(30.dp)) {

            listOf(
              //  ExploreTabItem.Live,
                ExploreTabItem.Following,
                ExploreTabItem.ForYou
            ).forEach { tab ->

                Text(
                    text = tab.title,
                    color = if (tab == selectedTab)
                        MaterialTheme.colorScheme.onBackground
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),

                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.clickable {
                        onTabSelected(tab)
                    }
                )
            }
        }

        // 🔍 RIGHT ICON (Search action)
        IconButton(onClick = onSearchClick, modifier = Modifier.offset(y= -(15).dp)) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                Modifier.size(22.dp)
            )
        }

    }
}
