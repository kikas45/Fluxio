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
 * If the internet drops while a video is playing, ExoPlayer enters STATE_BUFFERING
 * and the UI shows a spinner (handled by TikTokVideoItem). When internet returns,
 * resumeCurrentPlayer() is called and playback continues — this path is unchanged.
 *
 * The fixed path: if the user swipes away while STATE_BUFFERING (internet still down),
 * rotate() now calls stop() before seekTo(0) + prepare() on the demoted player.
 * stop() → STATE_IDLE clears the stalled buffer. The subsequent seekTo(0) + prepare()
 * always lands cleanly and re-loads from position 0 (cache for the first segment,
 * then network for the rest). When the user swipes back, playCurrentPlayer() also
 * calls stop() + seekTo(0) + prepare() unconditionally — so the video always restarts
 * from the beginning regardless of what state it was in when it was demoted.
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
            // Same URL — reset to position 0 regardless of current state.
            // stop() first in case the player is in STATE_BUFFERING (a network
            // drop may have stalled it); seekTo() is silently ignored on a
            // buffering player, so stop() → STATE_IDLE is required first.
            if (player.playbackState != Player.STATE_IDLE) player.stop()
            player.seekTo(0)
            player.playWhenReady = false
            player.prepare()
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
     * Start CURRENT from the beginning. Always seeks to 0.
     *
     * ── WHY we always seekTo(0) here (changed from STATE_IDLE guard) ──────────
     *
     * The old guard `if (playbackState == STATE_IDLE) seekTo(0)` was designed
     * to preserve mid-video position during a network stall on the CURRENT
     * screen (internet drops → spinner shows → internet returns → continues).
     * That use-case is now handled by resumeCurrentPlayer() from TikTokVideoItem.
     *
     * The guard became a bug when a player arrived in the CURRENT slot in
     * STATE_BUFFERING (internet dropped mid-video, user swiped away and back):
     *   • rotate() called seekTo(0) on a buffering player → silently dropped.
     *   • playCurrentPlayer() saw STATE_BUFFERING (not STATE_IDLE) → skipped seekTo.
     *   • play() resumed buffering from the stalled position → frozen frame, no spinner.
     *
     * Fix: always stop() + seekTo(0) + prepare() so the player is guaranteed to
     * start from position 0 regardless of what state it arrived in.
     * stop() → STATE_IDLE (clears the stalled buffer), then seekTo(0) + prepare()
     * re-loads from the cache/network cleanly from the start.
     */
    fun playCurrentPlayer() {
        p[slot[PREV]].apply  { pause(); playWhenReady = false }
        p[slot[NEXT]].apply  { pause(); playWhenReady = false }
        p[slot[CURRENT]].apply {
            stop()          // → STATE_IDLE, clears any stalled buffer state
            seekTo(0)
            prepare()       // re-prepare from position 0 (cache-first)
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

                // Demote CURRENT → new PREV.
                // stop() first to clear any stalled STATE_BUFFERING caused by a network
                // drop. A buffering player ignores seekTo() — stop() brings it to
                // STATE_IDLE so the subsequent seekTo(0) + prepare() always lands cleanly.
                // prepare() re-loads from position 0 (cache-first), and keeps the last
                // decoded frame alive in the SurfaceView buffer until the first new frame
                // arrives — so there is no black flash on backward swipe.
                p[physCurrent].stop()
                p[physCurrent].seekTo(0)
                p[physCurrent].playWhenReady = false
                p[physCurrent].prepare()

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

                // Demote CURRENT → new NEXT. Same stop+seekTo(0)+prepare logic
                // as the forward case — clears any stalled STATE_BUFFERING so the
                // player is guaranteed to be at position 0 when promoted back to CURRENT.
                p[physCurrent].stop()
                p[physCurrent].seekTo(0)
                p[physCurrent].playWhenReady = false
                p[physCurrent].prepare()

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