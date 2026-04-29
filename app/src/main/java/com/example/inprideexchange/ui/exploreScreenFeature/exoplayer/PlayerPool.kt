package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

/**
 * 3-player rotating pool — PREV + CURRENT + NEXT.
 *
 * ── WHY 3 PLAYERS ────────────────────────────────────────────────────────────
 *
 * With 2 players the backward swipe always flashed white/black before the
 * previous video appeared. The root cause:
 *
 *   rotate(-1) called stop() on both players. stop() blanks the SurfaceView —
 *   the last rendered frame is destroyed. The previous page's PlayerView had
 *   nothing to show until ExoPlayer re-prepared, causing a white flash.
 *
 * THE FIX — demote, never stop on rotate:
 *
 *   Forward (+1):
 *     Old PREV   → recycled (stop + clear — 2 pages back, safe to destroy)
 *     Old CURRENT → demoted to PREV  (pause + seekTo(0), NOT stopped)
 *     Old NEXT   → promoted to CURRENT
 *
 *   Backward (-1):
 *     Old NEXT   → recycled (stop + clear — lookahead no longer needed)
 *     Old CURRENT → demoted to NEXT   (pause + seekTo(0), NOT stopped)
 *     Old PREV   → promoted to CURRENT (frame already in SurfaceView buffer!)
 *
 * ── ROLE ASSIGNMENT FIX ──────────────────────────────────────────────────────
 *
 * Original code: `if (i == 1) CURRENT else NEIGHBOUR`
 *
 * This assigned CURRENT role to physical player index 1 only. But slot[]
 * permutes on every rotate() — physical player 1 may become PREV or NEXT.
 * More critically, physical players 0 and 2 were always built as NEIGHBOUR,
 * meaning whichever of them was rotated into CURRENT still had a 1s buffer
 * ceiling — starvation risk during playback after a few swipes.
 *
 * Fix: all three players are built with CURRENT role. The LoadControl
 * distinction between CURRENT and NEIGHBOUR was a premature optimisation
 * that conflicted with the slot-permutation design. Neighbours' download
 * aggression is already bounded by:
 *   • NEIGHBOUR players are always in pause() state — ExoPlayer's loader
 *     thread only downloads when the player is playing or preparing.
 *   • VideoPreloader already seeds the cache for NEXT, so the NEXT player
 *     reads from disk on prepare() — it never actually needs the 15s buffer.
 *
 * If strict per-role buffer control is needed in the future, LoadControl
 * must be swapped at the pool level when rotate() changes a player's role,
 * which ExoPlayer does not support at runtime. The correct approach would
 * be a separate ExoPlayer subclass per role, which adds complexity for
 * negligible real-world gain given the preloader's cache seeding.
 *
 * ── MEMORY ───────────────────────────────────────────────────────────────────
 *
 * loadedUrl slots are cleared on release() so nothing prevents the Strings
 * from being collected after the pool is released.
 *
 * ── NETWORK RECOVERY ─────────────────────────────────────────────────────────
 *
 * playCurrentPlayer() skips seekTo(0) when CURRENT is not STATE_IDLE —
 * preserves mid-video position during network stall recovery.
 * playCurrentPlayerFromStart() is the explicit boot variant only.
 */
@UnstableApi
class PlayerPool(context: Context) {

    // All 3 players built with CURRENT role — see role assignment fix above.
    private val buildResults = Array(3) {
        CachedPlayerFactory.build(context, QualityPolicy.PlayerRole.CURRENT)
    }
    private val p              = Array(3) { buildResults[it].player }
    private val trackSelectors = Array(3) { buildResults[it].trackSelector }

    // slot[PREV/CURRENT/NEXT] → index into p[].
    // Only slot[] is permuted on rotate() — p[] never moves.
    private val slot      = intArrayOf(0, 1, 2)
    private val loadedUrl = arrayOfNulls<String>(3)

    val prevPlayer:    ExoPlayer get() = p[slot[PREV]]
    val currentPlayer: ExoPlayer get() = p[slot[CURRENT]]
    val nextPlayer:    ExoPlayer get() = p[slot[NEXT]]

    // ── Load ──────────────────────────────────────────────────────────────────
    fun load(slotIndex: Int, url: String?) {
        if (url == null) return
        val physIdx = slot[slotIndex]
        val player  = p[physIdx]

        if (loadedUrl[physIdx] == url) {
            // Already loaded — just make sure it's prepared and at position 0.
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
     * Start/resume CURRENT without resetting the playhead.
     * Pauses PREV and NEXT (frames stay alive — not stopped).
     * seekTo(0) on CURRENT only if STATE_IDLE (first start) — preserves
     * mid-video position during network-drop recovery.
     */
    fun playCurrentPlayer() {
        p[slot[PREV]].apply  { pause(); playWhenReady = false }
        p[slot[NEXT]].apply  { pause(); playWhenReady = false }
        p[slot[CURRENT]].apply {
            if (playbackState == Player.STATE_IDLE) seekTo(0)
            playWhenReady = true
            play()
        }
    }

    /** Explicitly start CURRENT from position 0. Boot use only. */
    fun playCurrentPlayerFromStart() {
        p[slot[PREV]].apply  { pause(); playWhenReady = false }
        p[slot[NEXT]].apply  { pause(); playWhenReady = false }
        p[slot[CURRENT]].apply { seekTo(0); playWhenReady = true; play() }
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

            // ── Forward (+1) ──────────────────────────────────────────────────
            //   Old PREV   → recycled  → new NEXT  (stop + clear)
            //   Old CURRENT → demoted  → new PREV  (pause + seekTo, NOT stopped)
            //   Old NEXT   → promoted  → new CURRENT
            direction > 0 -> {
                val physPrev    = slot[PREV]
                val physCurrent = slot[CURRENT]
                val physNext    = slot[NEXT]

                // Demote CURRENT: keep last frame alive in SurfaceView buffer.
                p[physCurrent].pause()
                p[physCurrent].seekTo(0)
                p[physCurrent].playWhenReady = false

                // Recycle old PREV — 2 pages back, safe to destroy.
                p[physPrev].stop()
                p[physPrev].clearMediaItems()
                loadedUrl[physPrev] = null

                slot[PREV]    = physCurrent
                slot[CURRENT] = physNext
                slot[NEXT]    = physPrev
            }

            // ── Backward (-1) ─────────────────────────────────────────────────
            //   Old NEXT   → recycled  → new PREV  (stop + clear)
            //   Old CURRENT → demoted  → new NEXT  (pause + seekTo, NOT stopped)
            //   Old PREV   → promoted  → new CURRENT (frame already in buffer!)
            direction < 0 -> {
                val physPrev    = slot[PREV]
                val physCurrent = slot[CURRENT]
                val physNext    = slot[NEXT]

                // Demote CURRENT: keep last frame alive.
                p[physCurrent].pause()
                p[physCurrent].seekTo(0)
                p[physCurrent].playWhenReady = false

                // Recycle old NEXT — lookahead no longer needed.
                p[physNext].stop()
                p[physNext].clearMediaItems()
                loadedUrl[physNext] = null

                slot[PREV]    = physNext
                slot[CURRENT] = physPrev
                slot[NEXT]    = physCurrent
            }
        }
    }

    fun reapplyQuality() {
        trackSelectors.forEach { QualityPolicy.applyTrackSelection(it) }
    }

    /** Release all 3 players. Call ONLY from screen-level onDispose. */
    fun release() {
        p.forEach { it.clearVideoSurface(); it.release() }
        loadedUrl[0] = null
        loadedUrl[1] = null
        loadedUrl[2] = null
    }

    companion object {
        const val PREV    = 0
        const val CURRENT = 1
        const val NEXT    = 2
    }
}