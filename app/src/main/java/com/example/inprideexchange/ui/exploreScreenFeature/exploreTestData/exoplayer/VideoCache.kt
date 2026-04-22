package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
object VideoCache {

    private const val CACHE_DIR_NAME  = "video_cache"
    private const val CACHE_MAX_BYTES = 200L * 1024 * 1024

    @Volatile
    private var cache: SimpleCache? = null

    fun getInstance(context: Context): SimpleCache =
        cache ?: synchronized(this) {
            cache ?: buildCache(context.applicationContext).also { cache = it }
        }

    fun release() {
        synchronized(this) {
            cache?.release()
            cache = null
        }
    }

    private fun buildCache(appContext: Context): SimpleCache {
        val dir = File(appContext.cacheDir, CACHE_DIR_NAME)
        return SimpleCache(
            dir,
            LeastRecentlyUsedCacheEvictor(CACHE_MAX_BYTES),
            StandaloneDatabaseProvider(appContext)
        )
    }
}