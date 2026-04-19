package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
object VideoCache {

    private var cache: SimpleCache? = null

    fun getInstance(context: Context): SimpleCache {
        if (cache == null) {
            val cacheDir = File(context.cacheDir, "video_cache")

            cache = SimpleCache(
                cacheDir,
                LeastRecentlyUsedCacheEvictor(200L * 1024 * 1024), // 200MB
                StandaloneDatabaseProvider(context)
            )
        }
        return cache!!
    }
}