package com.example.inprideexchange.AppScreens.UserReg

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.inprideexchange.Utils.UtilityScreenSize
import com.example.inprideexchange.ui.components.buttons.GoogleSmartButton
import com.example.inprideexchange.ui.designSystem.dimens.AppDimens
import com.example.inprideexchange.ui.designSystem.dimens.BrandText
import com.example.inprideexchange.ui.welcomeScreen.TermsText
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close


@Composable
fun WelcomeScreen(
    onPhoneClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onSkip: () -> Unit,   // ✅ NEW
    isLoading: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val context = LocalContext.current

    BackHandler {
        if (context is ComponentActivity) context.finish()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(AppDimens.PaddingMedium)
        ) {
            val (
                titleRef,
                subtitleRef,
                bodyRef,
                buttonRef,
                termsRef,
                closeRef
            ) = createRefs()




            IconButton(
                onClick = onSkip,
                modifier = Modifier
                    .constrainAs(closeRef) {
                        top.linkTo(parent.top, margin = 0.dp) // adaptive safe margin
                        end.linkTo(parent.end, margin = 0.dp)
                    }
                    .size(34.dp) // perfect touch target
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = colorScheme.onBackground
                )
            }




            // Main title "M25" with your adaptive font size
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    ) { append("M") }
                    withStyle(
                        style = SpanStyle(
                            color = BrandText.heroSecondary(),
                            fontWeight = FontWeight.Medium
                        )
                    ) { append("25+") }
                },
                style = typography.headlineSmall.copy(
                    fontSize = UtilityScreenSize.adaptiveFontSize(
                        phoneSp = 60.sp,
                        tabletSp = 100.sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onBackground,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.constrainAs(titleRef) {
                    top.linkTo(parent.top, margin = 100.dp)
                    bottom.linkTo(subtitleRef.top, margin = 20.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    verticalBias = 1f
                    width = Dimension.fillToConstraints
                }
            )

            // Subtitle "We build wealth, the smart way."
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Normal,
                            color = colorScheme.onBackground
                        )
                    ) { append("We build wealth, the") }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Normal,
                            color = colorScheme.primary
                        )
                    ) { append(" smart") }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Normal,
                            color = colorScheme.onBackground
                        )
                    ) { append(" way.") }
                },
                style = typography.headlineMedium.copy(
                    fontSize = UtilityScreenSize.adaptiveFontSize(30.sp, 40.sp),
                    letterSpacing = 1.sp,
                    lineHeight = UtilityScreenSize.adaptiveFontSize(34.sp, 48.sp),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.constrainAs(subtitleRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(buttonRef.top, margin = 10.dp, )
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
            )

            // Body text
            Text(
                text = "Join 1,000+ members learning smarter strategies to stay ahead of the market, trade smarter, and grow real returns with structured investment opportunities.",
                style = typography.bodyLarge.copy(
                    fontSize = UtilityScreenSize.adaptiveFontSize(phoneSp = 16.sp, tabletSp = 22f.sp),
                    lineHeight = UtilityScreenSize.adaptiveFontSize(22.sp, 28.sp),
                    textAlign = TextAlign.Center,
                    color = colorScheme.onSurface
                ),
                modifier = Modifier.constrainAs(bodyRef) {
                    top.linkTo(subtitleRef.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    bottom.linkTo(buttonRef.top)
                    end.linkTo(parent.end)
                    verticalBias =0f
                    width = Dimension.fillToConstraints
                }
            )

            // Google button
            GoogleSmartButton(
                text = "Continue with Google",
                isEnabled = true,
                isLoading = isLoading,
                onClick = onGoogleClick,
                modifier = Modifier.constrainAs(buttonRef) {
                    bottom.linkTo(termsRef.top, margin = 30.dp)
                    start.linkTo(parent.start, margin = 20.dp)
                    end.linkTo(parent.end, margin = 20.dp)
                    width = Dimension.fillToConstraints
                }
            )

            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .constrainAs(termsRef) {
                        bottom.linkTo(parent.bottom, margin = 30.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }) {

                TermsText(
                    onTermsClick = {
                        Toast.makeText(context, "onTermsClick", Toast.LENGTH_SHORT).show()
                    },
                    onPrivacyClick = {
                        Toast.makeText(context, "onPrivacyClick", Toast.LENGTH_SHORT).show()
                    },

                    )
            }

        }
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(onPhoneClick = {}, onGoogleClick = {}, onSkip = {}, isLoading = false)
}