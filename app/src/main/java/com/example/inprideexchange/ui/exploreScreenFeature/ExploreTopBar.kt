package com.example.inprideexchange.ui.exploreScreenFeature

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.inprideexchange.ui.components.logo.M25Logo
import kotlinx.coroutines.NonDisposableHandle.parent


@Composable
fun ExploreTopBar(
    selectedTab: ExploreTabItem,
    onTabSelected: (ExploreTabItem) -> Unit,
    onSearchClick: () -> Unit
) {

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(top = 40.dp, start = 20.dp, end = 16.dp)
    ) {

        val (tabsRef, searchRef, logoRef) = createRefs()


        M25Logo(
            fontSize = 20.sp, // smaller for toolbar
            modifier = Modifier.constrainAs(logoRef) {
                start.linkTo(parent.start, margin = 9.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom, margin = 5.dp)
            }
        )



        // 🔥 Tabs Container
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.constrainAs(tabsRef) {
                end.linkTo(searchRef.start, margin = 16.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        ) {

            listOf(
                ExploreTabItem.Following,
                ExploreTabItem.ForYou
            ).forEach { tab ->

                val isSelected = tab == selectedTab

                val selectedColor = MaterialTheme.colorScheme.onBackground
                val unselectedColor =
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)

                Column(
                    modifier = Modifier
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = tab.title,
                        color = if (isSelected) selectedColor else unselectedColor,
                        style = MaterialTheme.typography.displayLarge
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                //  .fillMaxWidth(0.3f) // 🔥 dynamic width
                                .width(45.dp) // 🔥 dynamic width
                                .height(2.dp)
                                .background(selectedColor)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }

        // 🔍 Search Icon
        IconButton(
            onClick = onSearchClick,
            modifier = Modifier.constrainAs(searchRef) {
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom, margin = 9.dp)
            }
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}