package com.example.inprideexchange.ui.welcomeScreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.inprideexchange.Utils.UtilityScreenSize
import com.example.inprideexchange.ui.designSystem.dimens.BrandText

@Composable
fun Logo(
    modifier: Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Text(
        text = buildAnnotatedString {

            // Normal part
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            ) {
                append("M")
            }

            // Primary colored part
            withStyle(
                style = SpanStyle(
                    color = BrandText.heroSecondary(),
                    fontWeight = FontWeight.Medium
                )
            ) {
                append("25")
            }
        },
        style = typography.headlineSmall.copy(
            fontSize = UtilityScreenSize.adaptiveFontSize(phoneSp = 30.sp),
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onBackground,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth()
    )


}