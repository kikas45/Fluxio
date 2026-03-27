package com.example.inprideexchange.ui.components.buttons

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inprideexchange.R
import com.example.inprideexchange.Utils.UtilityScreenSize
import com.example.inprideexchange.ui.designsystem.dimens.AppShapes


@Composable
fun GoogleSmartButton(
    text: String = "Continue with Google",
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {



    val backgroundColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    val effectiveEnabled = isEnabled && !isLoading


    val interactionSource = remember { MutableInteractionSource() }
    val shape = AppShapes.Rounded12



    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .height(UtilityScreenSize.adaptivePhoneHeight)
            .background(backgroundColor)
            .border(
                width = 1.2.dp,
                color = borderColor,
                shape = shape
            )
            .clickable(
                enabled = effectiveEnabled,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = if (effectiveEnabled) 0.3f else 0.15f)
                ),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp) // ✅ content-driven height
    ) {

        // ✅ ICON + TEXT grouped together (natural flow)
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.google_g_logo),
                contentDescription = "Google",
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = text,
               // color = textColor,
                modifier = Modifier,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal
                )
            )
        }

        // ✅ Loader stays pinned right
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(18.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
        }
    }
}


