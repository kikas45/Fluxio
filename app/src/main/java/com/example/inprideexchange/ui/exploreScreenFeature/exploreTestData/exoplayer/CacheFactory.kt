package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource

@OptIn(UnstableApi::class)
object CacheFactory {

    fun create(context: Context): CacheDataSource.Factory {

        val httpFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)

        return CacheDataSource.Factory()
            .setCache(VideoCache.getInstance(context))
            .setUpstreamDataSourceFactory(httpFactory)

            // 🔥 IMPORTANT: remove ignore flag (fix stuck videos)
            .setFlags(0)
    }
}




/*
object CacheFactory {

    @OptIn(UnstableApi::class)
    fun create(context: Context): CacheDataSource.Factory =
        CacheDataSource.Factory()
            .setCache(VideoCache.getInstance(context))
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
}*/
