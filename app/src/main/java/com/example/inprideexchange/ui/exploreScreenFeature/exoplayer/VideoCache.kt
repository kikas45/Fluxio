package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * Singleton disk cache — 200 MB LRU, shared across the entire app lifetime.
 *
 * RULES:
 *  • Only ONE SimpleCache instance may exist per directory at a time.
 *    Violating this crashes ExoPlayer with a database lock error.
 *  • release() is intentionally NOT called on normal screen exit to avoid
 *    the "cache already released" crash. Call it only in Application.onTerminate()
 *    or a dedicated ViewModel.onCleared() that truly owns app lifecycle.
 */
@UnstableApi
object VideoCache {

    private const val CACHE_DIR_NAME  = "video_cache"
    // Increased to cache up to ~20 average-quality videos
    private const val CACHE_MAX_BYTES = 200L * 1024 * 1024   // 200 MB

    @Volatile
    private var cache: SimpleCache? = null

    fun getInstance(context: Context): SimpleCache =
        cache ?: synchronized(this) {
            cache ?: buildCache(context.applicationContext).also { cache = it }
        }

    /**
     * Safe release — only call when the entire application is shutting down.
     * Do NOT call from composable DisposableEffect or ViewModel.onCleared()
     * because the cache is shared and another screen may still use it.
     */
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