package com.example.inprideexchange.ui.welcomeScreen

import androidx.compose.runtime.Immutable

@Immutable
data class OnboardingItem(
    val image: Int,
    val title: String,
    val subtitle: String
)