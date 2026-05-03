package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SeekBarViewModel : ViewModel() {

    private val _progress   = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private val _isDragging = MutableStateFlow(false)
    val isDragging: StateFlow<Boolean> = _isDragging

    private val _isVisible  = MutableStateFlow(false)
    val isVisible: StateFlow<Boolean> = _isVisible

    // ── durationMs is now a StateFlow so SeekBar recomposes when first set ────
    // TikTokVideoItem writes seekBarViewModel.updateDurationMs(duration) from the
    // progress-poll loop; SeekBar reads durationMs.collectAsState() so it always
    // sees the latest value and never shows "0:00 / 0:00".
    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs

    var trackWidthPx : Int  = 0

    var onDragStart  : ((Float) -> Unit)? = null
    var onDrag       : ((Float) -> Unit)? = null
    var onDragEnd    : (() -> Unit)?       = null
    var onDragCancel : (() -> Unit)?       = null

    fun updateProgress(p: Float)     { _progress.value    = p }
    fun updateIsDragging(d: Boolean) { _isDragging.value  = d }
    fun updateIsVisible(v: Boolean)  { _isVisible.value   = v }
    fun updateDurationMs(ms: Long)   { if (_durationMs.value != ms) _durationMs.value = ms }
}