package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer


import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource

object CacheFactory {

    @OptIn(UnstableApi::class)
    fun create(context: Context): CacheDataSource.Factory {
        val cache = VideoCache.getInstance(context)

        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory()
            )
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
}