package com.example.inprideexchange.ui.exploreScreenFeature

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FollowingFeed() {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 80.dp,   // 🔥 push content below floating toolbar
            bottom = 16.dp,
            start = 16.dp,
            end = 16.dp
        )
    ) {

        items(FakeFeedData.following) { item ->
            FeedCard(item)
        }
    }
}