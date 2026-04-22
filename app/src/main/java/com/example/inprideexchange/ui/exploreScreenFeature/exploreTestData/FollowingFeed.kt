package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun FollowingFeed() {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 65.dp,   // 🔥 push content below floating toolbar
            bottom = 16.dp,
            start = 16.dp,
            end = 16.dp
        )
    ) {

        items(FakeFeedData.forYou) { item ->
            FeedCard(item)
        }
    }

}

