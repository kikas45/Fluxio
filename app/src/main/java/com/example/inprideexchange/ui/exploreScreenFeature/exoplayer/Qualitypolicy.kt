package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

/**
 * Single source of truth for all quality and buffer decisions.
 *
 * ── WHY THIS EXISTS ───────────────────────────────────────────────────────────
 *
 * Previously, resolution constraints were duplicated across CachedPlayerFactory
 * and implicitly inherited by VideoPreloader. Changing quality required hunting
 * down multiple call sites. This object centralises every quality decision:
 *
 *   • Which video rendition to select (height cap + force-lowest flag)
 *   • How much to buffer for each player role (CURRENT / NEIGHBOUR / PRELOADER)
 *   • Whether data-saver mode is active
 *
 * To change quality tomorrow: edit ONE place — this file.
 * To add data-saver toggle: flip dataSaverEnabled and call
 *   pool.reapplyQuality() to push the new parameters to all running players.
 *
 * ── ROLES ─────────────────────────────────────────────────────────────────────
 *
 * CURRENT — the video the user is actively watching.
 *   maxBufferMs = 15_000 (15 sec lookahead):
 *   ExoPlayer buffers up to 15s ahead of the current playhead at any time.
 *   This means:
 *     • If the user watches 30s of a 2min video, cached bytes ≈ playhead(30s) +
 *       lookahead(≤15s) = at most ~45s total. Not the whole video.
 *     • If the user pauses at 40s, the buffer drains no further — the playhead
 *       is frozen so the 15s window is also frozen. Downloads stop once the
 *       window is full (≈ 40s+15s = 55s). Not 3 minutes.
 *     • 15s is enough lookahead to absorb any realistic network hiccup, so
 *       the user never sees a rebuffer spinner mid-watch.
 *   Previously this was 120_000 (2 min), which caused ExoPlayer to aggressively
 *   download far beyond the watched position, wasting bandwidth.
 *
 * NEIGHBOUR (PREV / NEXT in PlayerPool) — prepared but not playing.
 *   maxBufferMs = 1_000 (1 sec):
 *   These players are paused at position 0. The preloader has already written
 *   1s of init+media segments to SimpleCache for the NEXT URL, so the pool
 *   player reads from disk on prepare() and hits STATE_READY without any
 *   network activity. The 1s ceiling ensures that even if it does need to
 *   download (cache miss), it cannot compete meaningfully with CURRENT.
 *
 * PRELOADER — silent background ExoPlayer inside VideoPreloader.
 *   maxBufferMs = 1_000 (1 sec, must equal PRELOAD_TARGET_MS in VideoPreloader):
 *   One DASH init segment + one media segment ≈ 1s. That is the minimum needed
 *   for the pool player to hit STATE_READY from cache on the next swipe.
 *
 *   Ceiling == poll target is critical for fairness between concurrent jobs:
 *   When two preloader sessions run simultaneously (for B and C), the LoadControl
 *   stops both at exactly 1s. There is no gap in which the first job can keep
 *   downloading after its poll exits — so B and C always get identical cache
 *   sizes regardless of which job started a few milliseconds earlier.
 *
 * ── RESOLUTION MODES ──────────────────────────────────────────────────────────
 *
 * LOWEST (default):
 *   forceLowestBitrate = true  → picks the single track with minimum bitrate
 *   maxVideoHeight = 360       → belt-and-suspenders cap
 *
 * DATA_SAVER:
 *   Same as LOWEST but maxVideoHeight = 144.
 *   Call setDataSaverEnabled(true) then pool.reapplyQuality().
 *
 * To change quality globally tomorrow:
 *   Change MAX_VIDEO_HEIGHT_NORMAL (e.g. 360 → 480 to allow slightly higher).
 *   All players and preloaders update on next applyTrackSelection() call.
 */
@OptIn(UnstableApi::class)
object QualityPolicy {

    // ── Resolution knobs — THE ONLY PLACE to change resolution ───────────────
    private const val MAX_VIDEO_HEIGHT_NORMAL     = 360
    private const val MAX_VIDEO_HEIGHT_DATA_SAVER = 144

    // ── Buffer knobs — THE ONLY PLACE to change buffer sizes ─────────────────

    // CURRENT: small lookahead window — download only what is near the playhead.
    // Keeps cached bytes proportional to what the user actually watched.
    private const val CURRENT_MIN_BUFFER_MS      = 3_000
    private const val CURRENT_MAX_BUFFER_MS      = 15_000  // 15s lookahead only
    private const val CURRENT_PLAYBACK_MS        = 500
    private const val CURRENT_AFTER_REBUFFER_MS  = 1_500

    // NEIGHBOUR (pool PREV/NEXT): absolute minimum — the preloader already seeded
    // cache for NEXT so the pool player rarely needs to download anything.
    // 1s ceiling means it stays prepared but never competes with CURRENT.
    private const val NEIGHBOUR_MIN_BUFFER_MS     = 500
    private const val NEIGHBOUR_MAX_BUFFER_MS     = 1_000  // just enough to stay READY
    private const val NEIGHBOUR_PLAYBACK_MS       = 500
    private const val NEIGHBOUR_AFTER_REBUFFER_MS = 500

    // PRELOADER: ceiling MUST equal PRELOAD_TARGET_MS in VideoPreloader (1s).
    // When ceiling == target there is zero gap where ExoPlayer keeps downloading
    // after the poll exits — both batch jobs hit the same hard wall at the same
    // buffer depth, producing identical preload sizes for B and C.
    private const val PRELOADER_MIN_BUFFER_MS     = 500
    private const val PRELOADER_MAX_BUFFER_MS     = 1_000  // == PRELOAD_TARGET_MS
    private const val PRELOADER_PLAYBACK_MS       = 500
    private const val PRELOADER_AFTER_REBUFFER_MS = 500

    // ── State ──────────────────────────────────────────────────────────────────
    @Volatile var dataSaverEnabled: Boolean = false

    // ── Role ──────────────────────────────────────────────────────────────────
    enum class PlayerRole { CURRENT, NEIGHBOUR, PRELOADER }

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Returns a DefaultLoadControl sized for the given role.
     * LoadControl cannot be changed at runtime — call once per player construction.
     */
    fun buildLoadControl(role: PlayerRole): DefaultLoadControl {
        val minMs      = when (role) {
            PlayerRole.CURRENT   -> CURRENT_MIN_BUFFER_MS
            PlayerRole.NEIGHBOUR -> NEIGHBOUR_MIN_BUFFER_MS
            PlayerRole.PRELOADER -> PRELOADER_MIN_BUFFER_MS
        }
        val maxMs      = when (role) {
            PlayerRole.CURRENT   -> CURRENT_MAX_BUFFER_MS
            PlayerRole.NEIGHBOUR -> NEIGHBOUR_MAX_BUFFER_MS
            PlayerRole.PRELOADER -> PRELOADER_MAX_BUFFER_MS
        }
        val playMs     = when (role) {
            PlayerRole.CURRENT   -> CURRENT_PLAYBACK_MS
            PlayerRole.NEIGHBOUR -> NEIGHBOUR_PLAYBACK_MS
            PlayerRole.PRELOADER -> PRELOADER_PLAYBACK_MS
        }
        val rebufferMs = when (role) {
            PlayerRole.CURRENT   -> CURRENT_AFTER_REBUFFER_MS
            PlayerRole.NEIGHBOUR -> NEIGHBOUR_AFTER_REBUFFER_MS
            PlayerRole.PRELOADER -> PRELOADER_AFTER_REBUFFER_MS
        }
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(minMs, maxMs, playMs, rebufferMs)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
    }

    /**
     * Applies the current resolution policy to a DefaultTrackSelector.
     * Safe to call multiple times — idempotent.
     * Call after construction AND whenever dataSaverEnabled changes.
     */
    fun applyTrackSelection(selector: DefaultTrackSelector) {
        val maxHeight = if (dataSaverEnabled) MAX_VIDEO_HEIGHT_DATA_SAVER
        else MAX_VIDEO_HEIGHT_NORMAL
        selector.setParameters(
            selector.buildUponParameters()
                .setForceLowestBitrate(true)
                .setMaxVideoSize(Int.MAX_VALUE, maxHeight)
                .build()
        )
    }

    /**
     * Optional: auto-detect data-saver based on metered network.
     * Returns true if on cellular (metered) or disconnected.
     * Wire this to a NetworkCallback in your ViewModel for automatic switching.
     */
    fun shouldAutoDataSaver(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return true) ?: return true
        return !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    }
}