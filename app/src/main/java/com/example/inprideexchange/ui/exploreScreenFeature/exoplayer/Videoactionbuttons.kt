package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Data model ────────────────────────────────────────────────────────────────

data class VideoActionState(
    val commentCount : String  = "15.6K",
    val likeCount    : String  = "4.5K",
    val isLiked      : Boolean = false,
)

// ── Main composable ───────────────────────────────────────────────────────────

/**
 * Vertical column of TikTok-style action buttons.
 *
 * Integrate in TikTokVideoItem ConstraintLayout:
 *
 *   val (playerViewRef, initialSpinnerRef, pauseIconRef, spinnerRef, actionsRef) = createRefs()
 *
 *   var actionState by remember { mutableStateOf(VideoActionState()) }
 *
 *   VideoActionButtons(
 *       state     = actionState,
 *       onLike    = { actionState = actionState.copy(isLiked = !actionState.isLiked) },
 *       onComment = { },
 *       onMore    = { },
 *       modifier  = Modifier.constrainAs(actionsRef) {
 *           end.linkTo(parent.end, margin = 12.dp)
 *           bottom.linkTo(parent.bottom, margin = 80.dp)
 *       }
 *   )
 */
@Composable
fun VideoActionButtons(
    state     : VideoActionState,
    onLike    : () -> Unit,
    onComment : () -> Unit,
    onMore    : () -> Unit,
    modifier  : Modifier = Modifier,
) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {

        // ── 1. Comment icon ───────────────────────────────────────────────────
        ActionButton(
            icon    = Icons.Outlined.ChatBubbleOutline,
            label   = state.commentCount,
            onClick = onComment,
        )

        // ── 2. Like / heart icon ──────────────────────────────────────────────
        LikeButton(
            isLiked = state.isLiked,
            count   = state.likeCount,
            onClick = onLike,
        )

        // ── 3. More icon ──────────────────────────────────────────────────────
        ActionButton(
            icon    = Icons.Filled.MoreVert,
            label   = "More",
            onClick = onMore,
        )
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun LikeButton(
    isLiked : Boolean,
    count   : String,
    onClick : () -> Unit,
) {
    // Bouncy pop animation when tapped
    var bounce by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue      = if (bounce) 1.4f else 1f,
        animationSpec    = spring(dampingRatio = 0.3f, stiffness = 700f),
        finishedListener = { bounce = false },
        label            = "likeScale",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                ) {
                    bounce = true
                    onClick()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = if (isLiked) Icons.Filled.Favorite
                else         Icons.Filled.FavoriteBorder,
                contentDescription = if (isLiked) "Unlike" else "Like",
                tint               = if (isLiked) Color(0xFFFF0033) else Color.White,
                modifier           = Modifier.size(28.dp),
            )
        }

        Text(
            text       = count,
            color      = Color.White,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ActionButton(
    icon    : ImageVector,
    label   : String,
    onClick : () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = Color.White,
                modifier           = Modifier.size(26.dp),
            )
        }

        Text(
            text       = label,
            color      = Color.White,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}