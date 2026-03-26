package com.example.inprideexchange.ui.components.buttons

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inprideexchange.Utils.UtilityScreenSize
import com.example.inprideexchange.ui.designsystem.dimens.AppShapes

@Composable
fun SmartProgressBarButton(
    text: String,
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val disabledBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    val disabledContent = MaterialTheme.colorScheme.onSurfaceVariant

    val effectiveEnabled = isEnabled && !isLoading

    val backgroundColor by animateColorAsState(
        targetValue = if (effectiveEnabled) primary else disabledBg,
        label = "buttonBgAnim"
    )

    val textColor by animateColorAsState(
        targetValue = if (effectiveEnabled) onPrimary else disabledContent,
        label = "buttonTextAnim"
    )

    val borderColor by animateColorAsState(
        targetValue = if (effectiveEnabled) primary else disabledBg,
        label = "buttonBorderAnim"
    )

    val shape = AppShapes.Rounded12
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .height(UtilityScreenSize.adaptivePhoneHeight)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .clickable(
                enabled = effectiveEnabled,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = onPrimary.copy(alpha = if (effectiveEnabled) 0.3f else 0.15f)
                ),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp) // ✅ content-driven height
    ) {

        // ✅ Centered Text
        Text(
            text = text,
            color = textColor,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Normal
            )
        )

        // ✅ Loader (right aligned)
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(18.dp),
                strokeWidth = 2.dp,
                color = textColor
            )
        }
    }
}