package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

object FakeVideoRepository {




    private val videos = listOf(
       // "https://livesim.dashif.org/livesim/testpic_2s/Manifest.mpd",
        "https://dash.akamaized.net/envivio/EnvivioDash3/manifest.mpd",
        "https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd",
        "https://storage.googleapis.com/shaka-demo-assets/angel-one/dash.mpd",
        "https://storage.googleapis.com/shaka-demo-assets/sintel/dash.mpd",
        "https://dash.akamaized.net/dash264/TestCases/2c/qualcomm/1/MultiResMPEG2.mpd",
    )


    fun getInitialFeed(): List<FeedVideoItem> =
        videos.mapIndexed { index, url -> FeedVideoItem(index, url) }

    fun loadMore(startIndex: Int, count: Int = 5): List<FeedVideoItem> {
        if (startIndex >= videos.size) return emptyList()

        val endIndex = minOf(startIndex + count, videos.size)

        return (startIndex until endIndex).map {
            FeedVideoItem(id = it, videoUrl = videos[it])
        }
    }
}