package com.example.inprideexchange.ui.sampleScreen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.inprideexchange.ui.BottomBar.bottomBarrFeatures.BaseScreen


@Composable
fun SampleScreen() {
    var clickCount by remember { mutableStateOf(0) }

    BaseScreen(title = "SampleScreen") {
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            clickCount++
        }) {
            Text("Click Me")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Button clicked $clickCount times")
    }
}