package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer

/*
object FakeVideoRepository {

    private val videos = listOf(
        "https://media.istockphoto.com/id/1467967077/video/phone-business-and-hands-of-black-woman-typing-text-message-writing-email-and-online.mp4?s=mp4-640x640-is&k=20&c=8PH3XzUw-t6HdtlHaiVanU97hXi7R6MOQwB_HV49DEc=",
        "https://media.istockphoto.com/id/2160469782/video/vertical-screen-zoom-out-top-down-view-diverse-team-of-business-professionals-with-laptops.mp4?s=mp4-640x640-is&k=20&c=5IBFYU-LaAEWyeH4m6dx2hNOxGeJsxe33LTa2DbXByA=",
        "https://media.istockphoto.com/id/1467181036/video/celebration-high-five-and-business-people-in-meeting-after-successful-ideas-planning-and.mp4?s=mp4-640x640-is&k=20&c=Y46nTUDZ7wND_KaNdbSUdUzc1rjG5Y4P-NWvWx6wShs=",
        "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
        "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
        "https://filesamples.com/samples/video/mp4/sample_640x360.mp4",
    )

    fun getAll(): List<FeedVideoItem> {
        return videos.mapIndexed { index, url ->
            FeedVideoItem(id = index, videoUrl = url)
        }
    }

    fun getItem(index: Int): FeedVideoItem {
        val safeIndex = Math.floorMod(index, videos.size)
        return FeedVideoItem(id = index, videoUrl = videos[safeIndex])
    }
}


*/





object FakeVideoRepository {

    private val videos = listOf(
        "https://media.istockphoto.com/id/1467967077/video/phone-business-and-hands-of-black-woman-typing-text-message-writing-email-and-online.mp4?s=mp4-640x640-is&k=20&c=8PH3XzUw-t6HdtlHaiVanU97hXi7R6MOQwB_HV49DEc=",
        "https://media.istockphoto.com/id/2160469782/video/vertical-screen-zoom-out-top-down-view-diverse-team-of-business-professionals-with-laptops.mp4?s=mp4-640x640-is&k=20&c=5IBFYU-LaAEWyeH4m6dx2hNOxGeJsxe33LTa2DbXByA=",
        "https://media.istockphoto.com/id/1467181036/video/celebration-high-five-and-business-people-in-meeting-after-successful-ideas-planning-and.mp4?s=mp4-640x640-is&k=20&c=Y46nTUDZ7wND_KaNdbSUdUzc1rjG5Y4P-NWvWx6wShs=",
        "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
        "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
        "https://filesamples.com/samples/video/mp4/sample_640x360.mp4",
    )

    fun getItem(index: Int): FeedVideoItem {
        val safeIndex = Math.floorMod(index, videos.size)

        return FeedVideoItem(
            id = index,
            videoUrl = videos[safeIndex]
        )
    }
}

