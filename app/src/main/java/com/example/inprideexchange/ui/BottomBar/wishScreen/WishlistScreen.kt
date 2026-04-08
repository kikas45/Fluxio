package com.example.inprideexchange.ui.BottomBar.wishScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.inprideexchange.ui.BottomBar.bottomBarrFeatures.BaseScreen

@Composable
fun WishlistScreen(

    onNavigateToSampleScreen: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        var clickCount by remember { mutableStateOf(0) }

        BaseScreen(title = "Wishlists") {
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                clickCount++
                onNavigateToSampleScreen()
            }) {
                Text("Click Me")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Button clicked $clickCount times")
        }
    }
}