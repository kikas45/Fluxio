package com.example.inprideexchange.ui.exploreScreenFeature

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExploreViewModel : ViewModel() {

    private val _selectedTab =
        MutableStateFlow<ExploreTabItem>(ExploreTabItem.ForYou)

    val selectedTab = _selectedTab.asStateFlow()

    fun onTabSelected(tab: ExploreTabItem) {
        _selectedTab.value = tab
    }
}