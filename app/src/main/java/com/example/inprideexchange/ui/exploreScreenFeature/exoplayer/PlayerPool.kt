package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

/**
 * 2-player rotating pool — CURRENT + NEXT only.
 *
 * ── MEMORY FIX: loadedUrl cleared on release() ───────────────────────────────
 *
 * Previously release() called player.release() but left loadedUrl[] holding
 * two String references. Minor in isolation, but a released pool should be
 * fully clean — nothing should prevent the strings from being collected if
 * the pool itself is no longer reachable.
 *
 * ── NETWORK RECOVERY FIX: playCurrentPlayer() preserves playhead ─────────────
 *
 * playCurrentPlayer() no longer unconditionally seekTo(0) on CURRENT.
 * If the player is not STATE_IDLE (e.g. STATE_BUFFERING during a network stall),
 * we skip seekTo(0) so the watchdog in TikTokVideoItem can recover from the
 * correct position. seekTo(0) is only applied when the player is STATE_IDLE
 * (i.e. freshly prepared and never started).
 *
 * playCurrentPlayerFromStart() is the explicit "boot" variant used only in
 * ForYouFeed's initial LaunchedEffect.
 */
@UnstableApi
class PlayerPool(context: Context) {

    private val buildResults   = Array(2) { CachedPlayerFactory.build(context) }
    private val p              = Array(2) { buildResults[it].player }
    private val trackSelectors = Array(2) { buildResults[it].trackSelector }

    private val slot      = intArrayOf(0, 1)
    private val loadedUrl = arrayOfNulls<String>(2)

    val currentPlayer: ExoPlayer get() = p[slot[CURRENT]]
    val nextPlayer:    ExoPlayer get() = p[slot[NEXT]]

    // ── Load ──────────────────────────────────────────────────────────────────
    fun load(slotIndex: Int, url: String?) {
        if (url == null) return
        val physIdx = slot[slotIndex]
        val player  = p[physIdx]

        if (loadedUrl[physIdx] == url) {
            if (player.playbackState == Player.STATE_IDLE) player.prepare()
            player.seekTo(0)
            player.playWhenReady = false
            return
        }

        player.stop()
        player.clearMediaItems()
        player.setMediaItem(MediaItem.fromUri(url))
        player.seekTo(0)
        player.playWhenReady = false
        player.prepare()
        loadedUrl[physIdx] = url
    }

    // ── Playback control ──────────────────────────────────────────────────────

    /**
     * Resume/start CURRENT without resetting the playhead.
     *
     * seekTo(0) is skipped when the player already has media and is not IDLE —
     * this preserves the mid-video position during network-drop recovery so the
     * watchdog in TikTokVideoItem can call prepare()+play() at the right spot.
     */
    fun playCurrentPlayer() {
        p[slot[NEXT]].apply {
            pause()
            seekTo(0)
            playWhenReady = false
        }
        p[slot[CURRENT]].apply {
            if (playbackState == Player.STATE_IDLE) seekTo(0)
            playWhenReady = true
            play()
        }
    }

    /** Explicitly start CURRENT from position 0. Used only on boot. */
    fun playCurrentPlayerFromStart() {
        p[slot[NEXT]].apply {
            pause(); seekTo(0); playWhenReady = false
        }
        p[slot[CURRENT]].apply {
            seekTo(0); playWhenReady = true; play()
        }
    }

    fun resumeCurrentPlayer() {
        p[slot[CURRENT]].apply { playWhenReady = true; play() }
    }

    fun pauseCurrentPlayer() {
        p[slot[CURRENT]].apply { pause(); playWhenReady = false }
    }

    fun isNextReady(): Boolean {
        val state = p[slot[NEXT]].playbackState
        return state == Player.STATE_READY || state == Player.STATE_ENDED
    }

    // ── Rotate ────────────────────────────────────────────────────────────────
    fun rotate(direction: Int) {
        when {
            direction > 0 -> {
                val recycled = slot[CURRENT]
                p[recycled].apply { stop(); clearMediaItems() }
                loadedUrl[recycled] = null
                slot[CURRENT] = slot[NEXT]
                slot[NEXT]    = recycled
            }
            direction < 0 -> {
                val recycled   = slot[NEXT]
                val oldCurrent = slot[CURRENT]
                p[recycled].apply { stop(); clearMediaItems() }
                loadedUrl[recycled] = null
                p[oldCurrent].apply { stop(); clearMediaItems() }
                loadedUrl[oldCurrent] = null
                slot[CURRENT] = oldCurrent
                slot[NEXT]    = recycled
            }
        }
    }

    fun reapplyQuality() {
        trackSelectors.forEach { QualityPolicy.applyTrackSelection(it) }
    }

    /** Release both players. Call ONLY from screen-level onDispose. */
    fun release() {
        p.forEach { it.clearVideoSurface(); it.release() }
        // Clear URL strings so nothing holds references after release.
        loadedUrl[0] = null
        loadedUrl[1] = null
    }

    companion object {
        const val CURRENT = 0
        const val NEXT    = 1
    }
}