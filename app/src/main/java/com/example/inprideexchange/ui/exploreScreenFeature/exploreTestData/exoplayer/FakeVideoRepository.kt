package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer

object FakeVideoRepository {

    private val videos = listOf(
        "https://media.istockphoto.com/id/1479305023/video/closeup-research-and-hands-with-pipette-medical-and-health-with-breakthrough-cure-and.mp4?s=mp4-640x640-is&k=20&c=-K_N_NbZze7i9voLpX-1hBsbsZA_CEZ5vU8RbryyQ3w=",
        "https://media.istockphoto.com/id/2160469782/video/vertical-screen-zoom-out-top-down-view-diverse-team-of-business-professionals-with-laptops.mp4?s=mp4-640x640-is&k=20&c=5IBFYU-LaAEWyeH4m6dx2hNOxGeJsxe33LTa2DbXByA=",
        "https://media.istockphoto.com/id/1467181036/video/celebration-high-five-and-business-people-in-meeting-after-successful-ideas-planning-and.mp4?s=mp4-640x640-is&k=20&c=Y46nTUDZ7wND_KaNdbSUdUzc1rjG5Y4P-NWvWx6wShs=",
        "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
        "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
        "https://filesamples.com/samples/video/mp4/sample_640x360.mp4",
    )

    fun getInitialFeed(): List<FeedVideoItem> =
        videos.mapIndexed { index, url ->
            FeedVideoItem(index, url)
        }

    // ✅ Stop loading when list ends
    fun loadMore(startIndex: Int, count: Int = 5): List<FeedVideoItem> {
        if (startIndex >= videos.size) return emptyList()

        val endIndex = (startIndex + count).coerceAtMost(videos.size)

        return (startIndex until endIndex).map {
            FeedVideoItem(id = it, videoUrl = videos[it])
        }
    }
}