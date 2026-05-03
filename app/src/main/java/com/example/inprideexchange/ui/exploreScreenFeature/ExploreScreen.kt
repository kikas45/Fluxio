package com.example.inprideexchange.ui.exploreScreenFeature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.FollowingFeed
import com.example.inprideexchange.ui.exploreScreenFeature.exoplayer.ForYouFeed
import com.example.inprideexchange.ui.exploreScreenFeature.exoplayer.SeekBarViewModel

@Composable
fun ExploreScreen(
    viewModel        : ExploreViewModel = viewModel(),
    seekBarViewModel : SeekBarViewModel,               // ← passed in
) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        when (selectedTab) {
            ExploreTabItem.Following -> FollowingFeed()
            ExploreTabItem.ForYou    -> ForYouFeed(seekBarViewModel = seekBarViewModel)  // ← forwarded
            else -> {}
        }

        ExploreTopBar(
            selectedTab   = selectedTab,
            onTabSelected = viewModel::onTabSelected,
            onSearchClick = {}
        )
    }
}