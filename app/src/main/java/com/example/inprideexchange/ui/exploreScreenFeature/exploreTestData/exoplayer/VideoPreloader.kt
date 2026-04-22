package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Downloads the first [BYTES_TO_CACHE] bytes of upcoming videos into
 * [VideoCache] BEFORE the user swipes to them.
 *
 * Works hand-in-hand with [PlayerPool]:
 *   - VideoPreloader writes bytes to disk for pages N+1 and N+2.
 *   - When PlayerPool.load() is called for those pages, CacheDataSource
 *     finds the bytes already on disk and skips the network entirely.
 *
 * Jobs are tracked per URL so preload() is idempotent — safe to call
 * on every page change without launching duplicate downloads.
 */
@UnstableApi
class VideoPreloader(private val context: Context) {

    private companion object {
        const val BYTES_TO_CACHE = 2L * 1024 * 1024   // 2 MB — enough for instant start
    }

    // SupervisorJob: one failed prefetch doesn't cancel the others
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val jobs  = mutableMapOf<String, Job>()

    /** Begin caching [url] to disk. No-op if already started for this URL. */
    fun preload(url: String) {
        if (jobs.containsKey(url)) return

        jobs[url] = scope.launch {
            runCatching {
                val dataSpec = DataSpec(
                    android.net.Uri.parse(url),
                    /* absoluteStreamPosition= */ 0L,
                    BYTES_TO_CACHE
                )
                CacheWriter(
                    CacheFactory.create(context.applicationContext).createDataSource(),
                    dataSpec,
                    /* temporaryBuffer= */ null,
                    /* progressListener= */ null
                ).cache()
            }
            // Best-effort: swallow errors. Real playback error handling is in the player.
        }
    }

    /** Cancel all in-progress prefetch jobs. Call from DisposableEffect.onDispose. */
    fun cancel() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
    }
}