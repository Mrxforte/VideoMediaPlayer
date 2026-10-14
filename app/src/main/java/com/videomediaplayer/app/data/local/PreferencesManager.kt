package com.videomediaplayer.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.videomediaplayer.app.domain.model.SortOption
import com.videomediaplayer.app.domain.model.ViewMode
import com.videomediaplayer.app.presentation.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val SORT = stringPreferencesKey("sort_option")
        val VIEW_MODE = stringPreferencesKey("view_mode")
        val REMEMBER_POSITION = booleanPreferencesKey("remember_position")
        val DEFAULT_SPEED = floatPreferencesKey("default_speed")
        val GESTURES_ENABLED = booleanPreferencesKey("gestures_enabled")
        val AUTO_PIP = booleanPreferencesKey("auto_pip")
        val BACKGROUND_PLAYBACK = booleanPreferencesKey("background_playback")
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        when (prefs[Keys.THEME]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    val sortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        when (prefs[Keys.SORT]) {
            "DATE" -> SortOption.DATE
            "DURATION" -> SortOption.DURATION
            "SIZE" -> SortOption.SIZE
            else -> SortOption.NAME
        }
    }

    val viewMode: Flow<ViewMode> = dataStore.data.map { prefs ->
        when (prefs[Keys.VIEW_MODE]) {
            "LIST" -> ViewMode.LIST
            else -> ViewMode.GRID
        }
    }

    val rememberPosition: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.REMEMBER_POSITION] ?: true
    }

    val defaultSpeed: Flow<Float> = dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_SPEED] ?: 1.0f
    }

    val gesturesEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.GESTURES_ENABLED] ?: true
    }

    val autoPip: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.AUTO_PIP] ?: false
    }

    val backgroundPlayback: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.BACKGROUND_PLAYBACK] ?: true
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setSortOption(option: SortOption) {
        dataStore.edit { it[Keys.SORT] = option.name }
    }

    suspend fun setViewMode(mode: ViewMode) {
        dataStore.edit { it[Keys.VIEW_MODE] = mode.name }
    }

    suspend fun setRememberPosition(enabled: Boolean) {
        dataStore.edit { it[Keys.REMEMBER_POSITION] = enabled }
    }

    suspend fun setDefaultSpeed(speed: Float) {
        dataStore.edit { it[Keys.DEFAULT_SPEED] = speed }
    }

    suspend fun setGesturesEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.GESTURES_ENABLED] = enabled }
    }

    suspend fun setAutoPip(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_PIP] = enabled }
    }

    suspend fun setBackgroundPlayback(enabled: Boolean) {
        dataStore.edit { it[Keys.BACKGROUND_PLAYBACK] = enabled }
    }
}
