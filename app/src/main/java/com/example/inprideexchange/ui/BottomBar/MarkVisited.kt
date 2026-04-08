package com.example.inprideexchange.ui.BottomBar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun MarkVisited(
    route: String,
    visitedScreens: MutableMap<String, Boolean>
) {
    LaunchedEffect(Unit) {
        if (visitedScreens[route] == false) {
            visitedScreens[route] = true
        }
    }
}