package com.example.inprideexchange.ui.bottomBar.tipScreen


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TripsViewModel : ViewModel() {

    private val _tripsText = MutableStateFlow("Visited ✅")
    val tripsText: StateFlow<String> = _tripsText

    fun updateTripsText(newText: String) {
        _tripsText.value = newText
    }
}