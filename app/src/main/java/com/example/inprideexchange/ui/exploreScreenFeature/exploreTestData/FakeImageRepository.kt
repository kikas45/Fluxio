package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData

object FakeImageRepository {

    fun getItem(index: Int): FeedImageItem {
        return FeedImageItem(
            id = index,
            imageUrl = "https://picsum.photos/400/800?random=$index"
        )
    }
}