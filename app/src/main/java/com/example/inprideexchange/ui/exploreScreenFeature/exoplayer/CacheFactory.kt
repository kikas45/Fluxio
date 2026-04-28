package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource

/**
 * Shared CacheDataSource.Factory — used by players AND the preloader.
 *
 * FLAG_BLOCK_ON_CACHE   — serve from disk if already cached (no re-fetch).
 * FLAG_IGNORE_CACHE_ON_ERROR — if a cached entry is corrupt (partial write
 *   from an interrupted session), fall through to network instead of blocking.
 *   Critical for DASH init segments — a corrupt init segment blocks the entire
 *   stream if this flag is absent.
 */
@OptIn(UnstableApi::class)
object CacheFactory {

    fun create(context: Context): CacheDataSource.Factory {

        val httpFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)
            .setDefaultRequestProperties(mapOf("Connection" to "keep-alive"))

        return CacheDataSource.Factory()
            .setCache(VideoCache.getInstance(context))
            .setUpstreamDataSourceFactory(httpFactory)
            .setFlags(
                CacheDataSource.FLAG_BLOCK_ON_CACHE or
                        CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
            )
    }
}