package com.example.inprideexchange.ui.BottomBar.tipScreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment


@Composable
fun TripsScreen(tripsViewModel: TripsViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {


        var startAnimation by remember { mutableStateOf(false) }

        val progress by animateFloatAsState(
            targetValue = if (startAnimation) 1f else 0f,
            animationSpec = tween(
                durationMillis = 2000 // 2 seconds
            ),
            label = "progressAnimation"
        )

        LaunchedEffect(Unit) {
            startAnimation = true
        }




        val tripsText by tripsViewModel.tripsText.collectAsState()

        Column(modifier = Modifier.padding(16.dp)) {

            Text("Trips", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = tripsText,
                onValueChange = { tripsViewModel.updateTripsText(it) },
                label = { Text("Edit Text") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = progress,
                    strokeWidth = 6.dp
                )

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyLarge
                )
            }


            Button(
                onClick = {
                    tripsViewModel.updateTripsText("Clicked ✅")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Click Me")
            }
        }
    }
}