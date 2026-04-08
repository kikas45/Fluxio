package com.example.inprideexchange.ui.BottomBar
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun WishlistScreen(isVisited: Boolean) {
    BaseScreen(title = "Wishlists") {

        Text(
            text = if (isVisited) "Visited ✅" else "First Time ❌"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("No saved homes yet 🏡")
    }
}