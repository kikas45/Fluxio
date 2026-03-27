package com.example.inprideexchange.ui.welcomefeature

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.inprideexchange.R


@Composable
fun OnboardingImage(
    image: Any,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = image,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit,

        // 🔥 THESE 2 LINES FIX RECOMPOSITION CRASHES
        placeholder = painterResource(R.drawable.ic_launcher_background),
        error = painterResource(R.drawable.ic_launcher_background)
    )
}

