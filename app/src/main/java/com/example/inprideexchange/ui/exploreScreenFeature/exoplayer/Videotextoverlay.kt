package com.example.inprideexchange.ui.exploreScreenFeature.exoplayer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Bottom-left text overlay — title + description.
 *
 * Integrate in ForYouFeed's ConstraintLayout:
 *
 *   val (videoRef, actionsRef, textRef) = createRefs()
 *
 *   VideoTextOverlay(
 *       title       = feed.getOrNull(page)?.title       ?: "",
 *       description = feed.getOrNull(page)?.description ?: "",
 *       modifier    = Modifier.constrainAs(textRef) {
 *           start.linkTo(parent.start, margin = 12.dp)
 *           end.linkTo(actionsRef.start, margin = 8.dp)
 *           bottom.linkTo(parent.bottom, margin = 120.dp)
 *           width = Dimension.fillToConstraints
 *       }
 *   )
 */
@Composable
fun VideoTextOverlay(
    title       : String,
    description : String,
    modifier    : Modifier = Modifier,
) {
    // Soft drop-shadow so text stays legible over any video colour
    val textShadow = Shadow(
        color  = Color.Black.copy(alpha = 0.75f),
        offset = Offset(1f, 1f),
        blurRadius = 6f,
    )

    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {

        // ── Title — single line, bold ─────────────────────────────────────────
        Text(
            text     = title,
            style    = TextStyle(
                color      = Color.White,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                shadow     = textShadow,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // ── Description — up to 3 lines ───────────────────────────────────────
        Text(
            text     = description,
            style    = TextStyle(
                color      = Color.White.copy(alpha = 0.90f),
                fontSize   = 13.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 18.sp,
                shadow     = textShadow,
            ),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}