package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

object FakeVideoRepository {

    private val videos = listOf(
        "https://firebasestorage.googleapis.com/v0/b/snappay-bad30.appspot.com/o/TestVideos%2FWhatsApp%20Video%202026-05-02%20at%2011.29.12%20AM.mp4?alt=media&token=ff9d8958-a980-416b-b4a6-6213980acca0",
        "https://firebasestorage.googleapis.com/v0/b/snappay-bad30.appspot.com/o/TestVideos%2Fzazu.mp4?alt=media&token=da8668f7-7df4-46be-8465-970ddb7b6b87",
        "https://firebasestorage.googleapis.com/v0/b/snappay-bad30.appspot.com/o/TestVideos%2FWhatsApp%20Video%202026-05-02%20at%2010.51.11%20PM.mp4?alt=media&token=15190663-8e53-4ca6-8129-501598dc491c",
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