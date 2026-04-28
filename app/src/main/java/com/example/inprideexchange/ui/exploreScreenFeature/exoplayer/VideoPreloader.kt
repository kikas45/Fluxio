package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import android.content.Context
import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * DASH-aware background preloader using CacheWriter — zero ExoPlayer instances.
 *
 * ── MEMORY FIX: CacheDataSource.Factory built once, not per-job ──────────────
 *
 * Previously, each preload coroutine called CacheFactory.create(appContext) —
 * constructing a new CacheDataSource.Factory AND a new DefaultHttpDataSource.Factory
 * on every single job, then discarding both immediately after createDataSource().
 *
 * These are small objects (~few KB heap each), but on a fast-scrolling feed this
 * runs multiple times per second. The churn puts unnecessary pressure on the
 * allocator and triggers more frequent minor GCs.
 *
 * Fix: dataSourceFactory is built once in the constructor and reused for every
 * job. CacheDataSource.Factory is stateless between calls to createDataSource(),
 * so sharing it across concurrent coroutines is safe.
 */
@UnstableApi
class VideoPreloader(context: Context) {

    private val appContext = context.applicationContext

    // Built once — reused for every preload job. Thread-safe: createDataSource()
    // is stateless and CacheDataSource.Factory holds no mutable per-call state.
    private val dataSourceFactory: CacheDataSource.Factory =
        CacheFactory.create(appContext)

    private companion object {
        const val PRELOAD_BYTES: Long  = 150_000L
        const val BATCH_STAGGER_MS     = 150L
    }

    private val scope     = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs      = ConcurrentHashMap<String, Job>()
    private val cancelled = AtomicBoolean(false)

    fun preloadBatch(urls: List<String>, poolUrls: Set<String> = emptySet()) {
        if (cancelled.get()) return

        val toLoad = urls.filter { url ->
            url.isNotBlank()       &&
                    url !in poolUrls       &&
                    !jobs.containsKey(url)
        }

        toLoad.forEachIndexed { index, url ->
            val staggerMs = index * BATCH_STAGGER_MS
            val job = scope.launch {
                if (staggerMs > 0) delay(staggerMs)
                if (!isActive) return@launch

                var writer: CacheWriter? = null
                try {
                    // Reuse the shared factory — no allocation on the hot path.
                    val dataSource = dataSourceFactory.createDataSource()
                    val dataSpec   = DataSpec(Uri.parse(url), 0L, PRELOAD_BYTES)

                    writer = CacheWriter(
                        dataSource,
                        dataSpec,
                        /* temporaryBuffer  = */ null,
                        /* progressListener = */ null,
                    )
                    writer.cache()

                } catch (_: Exception) {
                    // Network error, cancellation, or DASH manifest failure — safe fail.
                } finally {
                    withContext(NonCancellable) {
                        try { writer?.cancel() } catch (_: Exception) {}
                    }
                    jobs.remove(url)
                }
            }
            jobs[url] = job
        }
    }

    fun cancelUrl(url: String) {
        jobs[url]?.cancel()
    }

    fun cancel() {
        cancelled.set(true)
        jobs.values.forEach { it.cancel() }
        jobs.clear()
        scope.cancel()
    }
}