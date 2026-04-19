package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

@UnstableApi
class PlayerPool(context: Context) {

    private val mediaSourceFactory =
        DefaultMediaSourceFactory(CacheFactory.create(context))

    val currentPlayer = buildPlayer(context)
    val nextPlayer = buildPlayer(context)
    val prevPlayer = buildPlayer(context)

    private fun buildPlayer(context: Context): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 1f
            }
    }

    fun release() {
        currentPlayer.release()
        nextPlayer.release()
        prevPlayer.release()
    }
}