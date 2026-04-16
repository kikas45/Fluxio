package com.example.inprideexchange.ui.exploreScreenFeature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        // 🔥 CONTENT FIRST (goes behind)
        when (selectedTab) {
            ExploreTabItem.Following -> FollowingFeed()
            ExploreTabItem.ForYou -> ForYouFeed()
            else -> {}
        }

        // 🔥 TOP BAR FLOATING ON TOP
        ExploreTopBar(
            selectedTab = selectedTab,
            onTabSelected = viewModel::onTabSelected,
            onSearchClick = {}
        )
    }
}



/*
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        ExploreTopBar(
            selectedTab = selectedTab,
            onTabSelected = viewModel::onTabSelected,
            onSearchClick = {
                // later: navigate or open search screen
            }
        )

        when (selectedTab) {
            ExploreTabItem.Following -> FollowingFeed()
            ExploreTabItem.ForYou -> ForYouFeed()
            else -> {

            }
        }
    }
}*/
