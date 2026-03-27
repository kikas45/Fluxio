package com.example.inprideexchange.ui.welcomefeature

import androidx.compose.runtime.Immutable

@Immutable
data class OnboardingItem(
    val image: Any,
    val title: String,
    val subtitle: String
)