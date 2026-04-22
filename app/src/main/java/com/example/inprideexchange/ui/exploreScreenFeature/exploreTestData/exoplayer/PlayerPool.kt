package com.example.inprideexchange.ui.exploreScreenFeature.exploreTestData.exoplayer

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  ROTATING 3-PLAYER POOL  —  the exact model TikTok uses
 * ═══════════════════════════════════════════════════════════════════════════
 *
 *  Why exactly 3 players, never more, never less:
 *
 *    PREV ──── the page the user just came from (kept alive so back-swipe
 *              is instant; video resumes from its last position)
 *
 *    CURRENT ─ the page currently on screen and playing
 *
 *    NEXT ──── the page the user is about to swipe to (already prepared,
 *              already buffering, so the transition is seamless)
 *
 *  On every swipe forward:
 *    old PREV  →  recycled (stop + load new url for future NEXT slot)
 *    old CURRENT → becomes PREV
 *    old NEXT    → becomes CURRENT  (zero allocation, instant play)
 *    recycled    → becomes NEXT
 *
 *  On every swipe backward: mirror of the above.
 *
 *  Result: exactly 3 ExoPlayer instances exist for the entire lifetime of
 *  the feed screen regardless of how many hundreds of videos the user watches.
 *  Zero new allocations after the initial 3. Zero memory growth over time.
 *
 * ═══════════════════════════════════════════════════════════════════════════
 *
 *  URL tracking:
 *    Each physical player remembers which URL it last loaded.
 *    load() is a no-op when called with the same URL — prevents redundant
 *    prepare() calls when the user swipes back and forth on the same pages.
 *
 * ═══════════════════════════════════════════════════════════════════════════
 */
@UnstableApi
class PlayerPool(context: Context) {

    // ── The 3 physical players (created once, reused forever) ────────────────
    private val p0 = CachedPlayerFactory.build(context)
    private val p1 = CachedPlayerFactory.build(context)
    private val p2 = CachedPlayerFactory.build(context)

    // ── Slot array: slot[PREV/CURRENT/NEXT] → which physical player owns it ──
    //    We rotate indices, never the players themselves.
    private val slot = intArrayOf(0, 1, 2)   // slot[0]=PREV, slot[1]=CURRENT, slot[2]=NEXT

    // ── URL loaded per physical player (null = nothing loaded yet) ───────────
    private val loadedUrl = arrayOfNulls<String>(3)

    // ── Physical player lookup ───────────────────────────────────────────────
    private fun physical(slotIndex: Int): ExoPlayer = when (slot[slotIndex]) {
        0    -> p0
        1    -> p1
        else -> p2
    }

    // ── Public slot accessors ────────────────────────────────────────────────
    val prevPlayer    : ExoPlayer get() = physical(PREV)
    val currentPlayer : ExoPlayer get() = physical(CURRENT)
    val nextPlayer    : ExoPlayer get() = physical(NEXT)

    /**
     * Load [url] into the player at [slotIndex] and prepare it.
     * No-op if this player already has [url] loaded — safe to call repeatedly.
     */
    fun load(slotIndex: Int, url: String?) {
        if (url == null) return
        val physIdx = slot[slotIndex]
        if (loadedUrl[physIdx] == url) return          // already loaded — skip

        physical(slotIndex).apply {
            stop()
            clearMediaItems()
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = false
        }
        loadedUrl[physIdx] = url
    }

    /**
     * Call after the user has settled on a new page.
     *
     * @param direction  +1 = swiped forward (down), -1 = swiped backward (up)
     *
     * Rotation on forward swipe (+1):
     *   The physical player that was PREV is now free.
     *   CURRENT slides to PREV, NEXT slides to CURRENT, freed slot becomes NEXT.
     *   Its loadedUrl is cleared so the next load() call will prepare a new URL.
     *
     * Rotation on backward swipe (-1): mirror of the above.
     */
    fun rotate(direction: Int) {
        when {
            direction > 0 -> {
                val recycled = slot[PREV]           // free the old PREV
                slot[PREV]     = slot[CURRENT]
                slot[CURRENT]  = slot[NEXT]
                slot[NEXT]     = recycled
                loadedUrl[recycled] = null          // mark recycled slot as empty
            }
            direction < 0 -> {
                val recycled = slot[NEXT]           // free the old NEXT
                slot[NEXT]     = slot[CURRENT]
                slot[CURRENT]  = slot[PREV]
                slot[PREV]     = recycled
                loadedUrl[recycled] = null
            }
            // direction == 0 → initial load, no rotation needed
        }
    }

    /** Release all 3 players. Call from DisposableEffect.onDispose only. */
    fun release() {
        p0.release()
        p1.release()
        p2.release()
    }

    companion object {
        const val PREV    = 0
        const val CURRENT = 1
        const val NEXT    = 2
    }
}