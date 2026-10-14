package com.videomediaplayer.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videomediaplayer.app.data.local.PreferencesManager
import com.videomediaplayer.app.presentation.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val rememberPosition: StateFlow<Boolean> = preferencesManager.rememberPosition
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val defaultSpeed: StateFlow<Float> = preferencesManager.defaultSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val gesturesEnabled: StateFlow<Boolean> = preferencesManager.gesturesEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoPip: StateFlow<Boolean> = preferencesManager.autoPip
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val backgroundPlayback: StateFlow<Boolean> = preferencesManager.backgroundPlayback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    }

    fun setRememberPosition(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setRememberPosition(enabled) }
    }

    fun setDefaultSpeed(speed: Float) {
        viewModelScope.launch { preferencesManager.setDefaultSpeed(speed) }
    }

    fun setGesturesEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setGesturesEnabled(enabled) }
    }

    fun setAutoPip(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setAutoPip(enabled) }
    }

    fun setBackgroundPlayback(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setBackgroundPlayback(enabled) }
    }
}
