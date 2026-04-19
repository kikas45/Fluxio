package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

/*
@Composable
fun rememberFeed(): SnapshotStateList<FeedVideoItem> {
    return remember {
        mutableStateListOf<FeedVideoItem>().apply {
            addAll(FakeVideoRepository.getAll())
        }
    }
}

*/




@Composable
fun rememberFeed(): SnapshotStateList<FeedVideoItem> {
    return remember {
        mutableStateListOf(
            FakeVideoRepository.getItem(0),
            FakeVideoRepository.getItem(1),
            FakeVideoRepository.getItem(2),
            FakeVideoRepository.getItem(3),
            FakeVideoRepository.getItem(4),
            FakeVideoRepository.getItem(5),
            FakeVideoRepository.getItem(6),
            FakeVideoRepository.getItem(7),
        )
    }
}
