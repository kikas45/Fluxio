package com.example.inprideexchange.ui.welcomeScreen

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource


@Composable
fun OnboardingImage(
    image: Int,
    modifier: Modifier = Modifier,
    //tint: Color? = MaterialTheme.colorScheme.onSurfaceVariant
    tint: Color? = MaterialTheme.colorScheme.primary
) {
    Image(
        painter = painterResource(id = image),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit,
        colorFilter = tint?.let { ColorFilter.tint(it) }
    )
}







/*

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

*/
