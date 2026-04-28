package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

/**
 * Builds ExoPlayer instances for the pool.
 *
 * ── MEMORY FIX: removed anonymous Player.Listener ────────────────────────────
 *
 * The previous version added an anonymous Player.Listener in build() to handle
 * STATE_ENDED by calling seekTo(0) + play(). This had two problems:
 *
 *   1. The listener was never explicitly removed. ExoPlayer clears listeners on
 *      release(), so no permanent leak — but it held a live reference for the
 *      entire lifetime of the player, and the manual loop logic raced with
 *      ExoPlayer's own repeat mechanism.
 *
 *   2. It was redundant. repeatMode = Player.REPEAT_MODE_ONE already instructs
 *      ExoPlayer to loop automatically on STATE_ENDED with no extra code needed.
 *      The manual seekTo(0) + play() on STATE_ENDED was firing ON TOP of the
 *      repeat, causing a potential double-seek on every loop.
 *
 * Fix: listener removed entirely. REPEAT_MODE_ONE handles looping on its own.
 */
@UnstableApi
object CachedPlayerFactory {

    data class PlayerBuildResult(
        val player        : ExoPlayer,
        val trackSelector : DefaultTrackSelector,
    )

    fun build(
        context : Context,
        role    : QualityPolicy.PlayerRole = QualityPolicy.PlayerRole.CURRENT,
    ): PlayerBuildResult {
        val trackSelector = DefaultTrackSelector(context.applicationContext)
        QualityPolicy.applyTrackSelection(trackSelector)

        val loadControl = QualityPolicy.buildLoadControl(role)

        val player = ExoPlayer.Builder(context.applicationContext)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(CacheFactory.create(context.applicationContext))
            )
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build()
            .apply {
                repeatMode    = Player.REPEAT_MODE_ONE   // handles looping — no listener needed
                volume        = 1f
                playWhenReady = false

                setAudioAttributes(
                    androidx.media3.common.AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .build(),
                    true
                )
                setHandleAudioBecomingNoisy(true)
            }

        return PlayerBuildResult(player, trackSelector)
    }
}