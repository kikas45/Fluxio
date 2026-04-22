package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

@UnstableApi
object CachedPlayerFactory {

    fun build(context: Context): ExoPlayer =
        ExoPlayer.Builder(context.applicationContext)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(CacheFactory.create(context.applicationContext))
            )
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume     = 1f
            }
}