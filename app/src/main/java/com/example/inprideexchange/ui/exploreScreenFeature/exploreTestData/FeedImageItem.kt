package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData


data class FeedImageItem(
    val id: Int,
    val imageUrl: String
)

object FakeImageFeedData {
    val following = listOf(
        FeedImageItem(1, "https://picsum.photos/400/800?1"),
        FeedImageItem(2, "https://picsum.photos/400/800?2"),
        FeedImageItem(3, "https://picsum.photos/400/800?3"),
        FeedImageItem(4, "https://picsum.photos/400/800?4"),
    )
}