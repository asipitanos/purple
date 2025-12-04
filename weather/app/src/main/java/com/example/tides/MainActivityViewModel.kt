package com.example.tides

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tides.datastore.AppPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val appPreferences: AppPreferences,
) : ViewModel() {
    val darkMode: StateFlow<Boolean?> =
        appPreferences.darkMode
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    fun saveDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            appPreferences.saveDarkMode(isDark)
        }
    }
}
