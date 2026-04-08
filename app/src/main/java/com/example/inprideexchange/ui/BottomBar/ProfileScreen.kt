package com.example.inprideexchange.ui.BottomBar
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun ProfileScreen(isVisited: Boolean) {
    BaseScreen(title = "Profile") {

        Text(
            text = if (isVisited) "Visited ✅" else "First Time ❌"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Name: Eric David")
        Text("Location: Lagos 🇳🇬")
    }
}