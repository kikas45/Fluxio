package com.example.inprideexchange.ui.bottomBar.bottomBarrFeatures

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.inprideexchange.ui.exploreScreenFeature.exoplayer.SeekBar
import com.example.inprideexchange.ui.exploreScreenFeature.exoplayer.SeekBarViewModel
import com.example.inprideexchange.ui.theme.BrandText

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

@SuppressLint("SuspiciousIndentation")
@Composable
fun AppBottomBar(
    currentRoute    : String?,
    onItemClick     : (BottomNavItem) -> Unit,
    seekBarViewModel: SeekBarViewModel,
) {
    val items = listOf(
        BottomNavItem.ScreenA,
        BottomNavItem.ScreenB,
        BottomNavItem.ScreenC
    )

    val seekProgress by seekBarViewModel.progress.collectAsState()
    val isDragging   by seekBarViewModel.isDragging.collectAsState()
    val isVisible    by seekBarViewModel.isVisible.collectAsState()
    val durationMs   by seekBarViewModel.durationMs.collectAsState()

    val seekBarAlpha = if (isVisible) 1f else 0f

    // Compute label strings here, at AppBottomBar level — no clipping issues
    val thumbFrac = seekProgress.coerceIn(0f, 1f)
    val posStr    = formatMs((thumbFrac * durationMs).toLong())
    val durStr    = formatMs(durationMs)

    // Wrap everything in a Box so we can overlay the label on top of everything
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { clip = false }
    ) {

        // ── Main content: SeekBar row + NavigationBar ─────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { clip = false }
        ) {

            // ── SeekBar row — 20dp layout height ─────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .zIndex(2f)
                    .alpha(seekBarAlpha)
                    .graphicsLayer { clip = false }
                    .onSizeChanged { seekBarViewModel.trackWidthPx = it.width },
            ) {
                // Invisible extended touch zone — 56dp tall, extends upward into video
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(56.dp)
                        .align(Alignment.BottomStart)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    val tw = seekBarViewModel.trackWidthPx
                                    if (tw > 0) seekBarViewModel.onDragStart
                                        ?.invoke((offset.x / tw).coerceIn(0f, 1f))
                                },
                                onHorizontalDrag = { change, _ ->
                                    val tw = seekBarViewModel.trackWidthPx
                                    if (tw > 0) {
                                        change.consume()
                                        seekBarViewModel.onDrag
                                            ?.invoke((change.position.x / tw).coerceIn(0f, 1f))
                                    }
                                },
                                onDragEnd    = { seekBarViewModel.onDragEnd?.invoke() },
                                onDragCancel = { seekBarViewModel.onDragCancel?.invoke() },
                            )
                        },
                )

                SeekBar(
                    progress     = seekProgress,
                    isDragging   = isDragging,
                    trackWidthPx = seekBarViewModel.trackWidthPx,
                    viewModel    = seekBarViewModel,
                    modifier     = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                )
            }

            // ── NavigationBar ─────────────────────────────────────────────────
            NavigationBar(
                modifier       = Modifier.height(97.dp),
                tonalElevation = 0.dp,
                containerColor = BrandText.hero()
            ) {
                items.forEach { item ->

                    val isSelected = currentRoute == item.route

                    val targetColor = if (isSelected) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                    }

                    val animatedColor by animateColorAsState(
                        targetValue = targetColor,
                        label       = "nav_item_color"
                    )

                    NavigationBarItem(
                        selected        = isSelected,
                        onClick         = { onItemClick(item) },
                        alwaysShowLabel = true,
                        icon = {
                            Column(
                                modifier            = Modifier.padding(top = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter            = painterResource(
                                        id = if (isSelected) item.selectedIcon else item.unselectedIcon
                                    ),
                                    contentDescription = item.label,
                                    modifier           = Modifier.size(25.dp),
                                    tint               = animatedColor
                                )
                                Text(
                                    text       = item.label,
                                    color      = animatedColor,
                                    fontSize   = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        label  = null,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor      = BrandText.hero(),
                            selectedIconColor   = animatedColor,
                            unselectedIconColor = animatedColor,
                            selectedTextColor   = animatedColor,
                            unselectedTextColor = animatedColor
                        )
                    )
                }
            }
        }

        // ── Timestamp label — rendered as overlay, ABOVE everything ──────────
        // Lives here at AppBottomBar level so it is NEVER clipped by any child.
        // Positioned at the TOP of this Box (which starts at the seekbar row),
        // then offset upward into the video area.
        if (isDragging && isVisible && durationMs > 0L) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .zIndex(10f)
            ) {
                val labelEstW = 120.dp
                val labelX = (maxWidth * thumbFrac - labelEstW / 2)
                    .coerceAtLeast(8.dp)
                    .coerceAtMost(maxWidth - labelEstW - 8.dp)

                // Offset upward from the top of AppBottomBar into video content.
                // Tune this value — 52dp clears the seekbar row + thumb + padding.
                val labelYUp = (-52).dp

                Text(
                    text       = "$posStr  /  $durStr",
                    color      = Color.White,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = labelX, y = labelYUp)
                        .background(Color(0xDD000000), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}