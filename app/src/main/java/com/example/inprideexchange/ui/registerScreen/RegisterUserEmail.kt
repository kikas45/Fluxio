package com.example.inprideexchange.ui.registerScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.compose.runtime.Composable

@Composable
fun RegisterUserEmail() {


    //  BaseStandardSmoothScreen(


    BaseHelperSmoothLayout(
        helpText = "Need help?",
        // showTopProgress = isLoading // ✅ SAME FLAG
    ) {


        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) //  important for scrolling state
                .padding(horizontal = 18.dp, vertical = 0.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {

            val (
                headerTitle,
                description,
                emailField,
                continueButton,
                termsRow,
                alreadyText,
                loginText
            ) = createRefs()




            Text(
                text = "Hello!",
                modifier = Modifier.constrainAs(headerTitle) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
                color = MaterialTheme.colorScheme.onSurface,
                //  color = BrandText.hero(),
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 30.sp,
                letterSpacing = 0.2.sp,
                textAlign = TextAlign.Start
            )


            // DESCRIPTION
            Text(
                text = "Welcome to R30+ World! Enter your email to get started.",
                fontSize = 16.sp,
                lineHeight = 22.sp,
                //  color = BrandText.heroSecondary(),
                modifier = Modifier.constrainAs(description) {
                    top.linkTo(headerTitle.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
            )


            val mes = "Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started." +
                    "Welcome to R30+ World! Enter your email to get started." +
                    "Welcome to R30+ World! Enter your email to get started." +
                    "Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started." +
                    "Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started." +
                    "" +
                    "Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started." +
                    "" +
                    "" +
                    "Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started." +
                    "" +
                    "Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started." +
                    "Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started.Welcome to R30+ World! Enter your email to get started." +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "Welcome to R30+ World! Enter your email to get started." +
                    "Welcome to R30+ World! Enter your email to get started."



            Text(
               // text = "Welcome to R30+ World! Enter your email to get started.",
                text = mes,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                //  color = BrandText.heroSecondary(),
                modifier = Modifier.constrainAs(description) {
                    top.linkTo(headerTitle.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
            )


        }
    }
}