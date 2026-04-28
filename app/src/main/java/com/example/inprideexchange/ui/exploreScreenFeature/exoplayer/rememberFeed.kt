package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

@Composable
fun rememberFeed(): SnapshotStateList<FeedVideoItem> =
    remember {
        mutableStateListOf<FeedVideoItem>().apply {
            addAll(FakeVideoRepository.getInitialFeed())
        }
    }