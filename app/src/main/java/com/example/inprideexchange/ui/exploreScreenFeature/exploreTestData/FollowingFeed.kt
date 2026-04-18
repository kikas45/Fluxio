package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage

@Composable
fun FollowingFeed() {

    val pagerSate = rememberPagerState(
        pageCount = { FakeImageFeedData.following.size }
    )


    VerticalPager(
        state = pagerSate,
        modifier = Modifier.fillMaxSize(),

        ) { page ->
        val item = FakeImageFeedData.following[page]

        AsyncImage(
            model = item.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FollowingFeedPreview() {
    FollowingFeed()
}