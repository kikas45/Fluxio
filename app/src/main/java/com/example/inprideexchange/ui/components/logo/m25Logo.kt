package com.example.inprideexchange.ui.components.logo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.inprideexchange.Utils.UtilityScreenSize
import com.example.inprideexchange.ui.designSystem.dimens.BrandText

@Composable
fun M25Logo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = UtilityScreenSize.adaptiveFontSize(
        phoneSp = 40.sp,
        tabletSp = 100.sp
    ),
    textAlign: TextAlign = TextAlign.Center
) {

    Text(
        text = buildAnnotatedString {

            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            ) {
                append("M")
            }

            withStyle(
                style = SpanStyle(
                    color = BrandText.heroSecondary(),
                    fontWeight = FontWeight.Medium
                )
            ) {
                append("25+")
            }
        },
        style = MaterialTheme.typography.headlineSmall.copy(
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = textAlign
        ),
        modifier = modifier // 🔥 key change
    )
}

/*



M25Logo(
fontSize = 18.sp, // smaller for toolbar
modifier = Modifier.constrainAs(logoRef) {
    start.linkTo(parent.start)
    top.linkTo(parent.top)
    bottom.linkTo(parent.bottom)
}
)



        */
