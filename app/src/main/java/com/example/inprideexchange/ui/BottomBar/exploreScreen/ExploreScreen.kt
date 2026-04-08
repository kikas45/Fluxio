package com.example.inprideexchange.ui.BottomBar.exploreScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inprideexchange.ui.BottomBar.bottomBarrFeatures.BaseScreen


@Composable
fun ExploreScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BaseScreen(title = "Explore") {

            Text(
                text = "Visited ✅",
                color = MaterialTheme.colorScheme.primary

            )

            Spacer(modifier = Modifier.height(16.dp))

            repeat(3) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Beautiful Apartment #$it")
                        Text("₦120,000 / night", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}