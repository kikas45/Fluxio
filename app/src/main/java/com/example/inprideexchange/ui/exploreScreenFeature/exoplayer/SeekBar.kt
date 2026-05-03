package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

private fun formatMs(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = ms / 1_000L
    val minutes  = totalSec / 60
    val seconds  = totalSec % 60
    return if (minutes >= 60) {
        val hours = minutes / 60
        val mins  = minutes % 60
        "%d:%02d:%02d".format(hours, mins, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

private val TRACK_H_IDLE = 2.5.dp
private val TRACK_H_DRAG = 4.dp
private val THUMB_IDLE   = 12.dp
private val THUMB_DRAG   = 18.dp
private val RESERVED_H   = 20.dp
private val SeekRed      = Color(0xFFFF0033)

@Composable
fun SeekBar(
    progress     : Float,
    isDragging   : Boolean,
    trackWidthPx : Int,
    viewModel    : SeekBarViewModel,
    modifier     : Modifier = Modifier,
) {
    val durationMs by viewModel.durationMs.collectAsState()
    val thumbFrac  = progress.coerceIn(0f, 1f)

    val trackH: Dp by animateDpAsState(
        targetValue   = if (isDragging) TRACK_H_DRAG else TRACK_H_IDLE,
        animationSpec = tween(100), label = "seekTrackH",
    )
    val thumbD: Dp by animateDpAsState(
        targetValue   = if (isDragging) THUMB_DRAG else THUMB_IDLE,
        animationSpec = tween(100), label = "seekThumbD",
    )

    val posStr = formatMs((thumbFrac * durationMs).toLong())
    val durStr = formatMs(durationMs)

    // RESERVED_H is the layout height of this composable.
    // The track is pinned to the BOTTOM of this box.
    // The thumb centre sits exactly on the track centre line.
    //
    // Because MainScreen wraps us in a Box that is NOT clipped, the thumb
    // freely draws both upward (into video) and downward (into NavBar).
    // No graphicsLayer trick needed — we just let overflow happen naturally.
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(RESERVED_H)
            .zIndex(2f),
    ) {
        val totalW = maxWidth
        val thumbR = thumbD / 2

        val thumbLeft = (totalW * thumbFrac - thumbR)
            .coerceAtLeast(0.dp)
            .coerceAtMost((totalW - thumbD).coerceAtLeast(0.dp))

        // Track centre is trackH/2 from the bottom of RESERVED_H.
        // Thumb centre should equal track centre.
        // Thumb is aligned BottomStart, so y=0 → bottom of thumb = bottom of box.
        // We want: bottom of thumb = track_centre - thumbR from box bottom
        //        = trackH/2 - thumbR
        // offset(y) is positive = DOWN. Negative = UP.
        // So: y = -(trackH/2 - thumbR) = thumbR - trackH/2
        // This is NEGATIVE when thumbR > trackH/2 (always true) → moves thumb UP,
        // so thumb centre aligns with track centre.
        // The bottom half of the thumb hangs BELOW RESERVED_H into NavBar. ✓
        // Both thumb and track are BottomStart-aligned.
        // Thumb bottom = box bottom. Thumb centre = thumbR above box bottom.
        // Track centre = trackH/2 above box bottom.
        // Thumb centre is (thumbR - trackH/2) TOO HIGH → push DOWN by that amount.
        // Positive y in offset() = downward. Result: thumb straddles the track line.
        val thumbOffsetY = thumbR - trackH / 2

        // Track background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackH)
                .align(Alignment.BottomStart)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.30f))
        )

        // Track fill
        if (thumbFrac > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(thumbFrac)
                    .height(trackH)
                    .align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(50))
                    .background(SeekRed)
            )
        }

        // Thumb — half above seek line (video), half below (NavBar)
        Box(
            modifier = Modifier
                .size(thumbD)
                .align(Alignment.BottomStart)
                .offset(x = thumbLeft, y = thumbOffsetY)
                .shadow(
                    elevation    = if (isDragging) 6.dp else 3.dp,
                    shape        = CircleShape,
                    clip         = false,
                    ambientColor = SeekRed.copy(alpha = 0.4f),
                    spotColor    = SeekRed.copy(alpha = 0.6f),
                )
                .background(SeekRed, CircleShape)
        )

        // Timestamp label — only while dragging, floats up into video
        if (isDragging && durationMs > 0L) {
            val labelEstW = 110.dp
            val labelX = (totalW * thumbFrac - labelEstW / 2)
                .coerceAtLeast(8.dp)
                .coerceAtMost(totalW - labelEstW - 8.dp)
            val labelYUp = -(RESERVED_H + THUMB_DRAG + 14.dp)

            Text(
                text       = "$posStr  /  $durStr",
                color      = Color.White,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = labelX, y = labelYUp)
                    .background(Color(0xDD000000), RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .zIndex(3f),
            )
        }
    }
}