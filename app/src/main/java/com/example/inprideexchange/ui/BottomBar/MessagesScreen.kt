package com.example.inprideexchange.ui.BottomBar
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MessagesScreen(isVisited: Boolean) {
    BaseScreen(title = "Messages") {

        Text(
            text = if (isVisited) "Visited ✅" else "First Time ❌"
        )

        Spacer(modifier = Modifier.height(16.dp))

        repeat(3) {
            Text("Conversation with Host #$it")
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}