package com.videomediaplayer.app.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videomediaplayer.app.data.local.PreferencesManager
import com.videomediaplayer.app.data.repository.MediaRepository
import com.videomediaplayer.app.playback.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val controlsVisible: Boolean = true,
    val isLocked: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    val showSpeedDialog: Boolean = false,
    val showAspectDialog: Boolean = false,
    val showSleepDialog: Boolean = false,
    val volumeIndicator: Float? = null,
    val brightnessIndicator: Float? = null,
    val seekIndicator: String? = null,
    val title: String = "",
    val sleepRemainingMs: Long? = null
)

enum class AspectRatioMode { FIT, FILL, CROP, STRETCH, ORIGINAL }

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager,
    private val mediaRepository: MediaRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var positionJob: Job? = null
    private var controlsHideJob: Job? = null
    private var currentMediaId: Long = 0L
    private var sleepJob: Job? = null

    val player get() = playerManager.getPlayer()

    fun prepare(mediaId: Long, uriString: String, title: String = "") {
        currentMediaId = mediaId
        viewModelScope.launch {
            val rememberEnabled = preferencesManager.rememberPosition.first()
            val startPos = if (rememberEnabled) {
                mediaRepository.getLastPosition(mediaId)
            } else {
                0L
            }

            val uri = Uri.parse(uriString)
            playerManager.prepare(uri, startPos)
            _uiState.update { it.copy(title = title.ifBlank { uri.lastPathSegment ?: "Video" }) }
            startPositionUpdates()
            scheduleControlsHide()
        }
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = viewModelScope.launch {
            while (isActive) {
                playerManager.updatePosition()
                _uiState.update {
                    it.copy(
                        isPlaying = playerManager.isPlaying.value,
                        positionMs = playerManager.position.value,
                        durationMs = playerManager.duration.value,
                        playbackSpeed = playerManager.playbackSpeed.value
                    )
                }
                delay(200)
            }
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
        scheduleControlsHide()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
        scheduleControlsHide()
    }

    fun seekBy(deltaMs: Long) {
        playerManager.seekBy(deltaMs)
        val label = if (deltaMs > 0) "+${deltaMs / 1000}s" else "${deltaMs / 1000}s"
        _uiState.update { it.copy(seekIndicator = label) }
        viewModelScope.launch {
            delay(800)
            _uiState.update { it.copy(seekIndicator = null) }
        }
        scheduleControlsHide()
    }

    fun setSpeed(speed: Float) {
        playerManager.setSpeed(speed)
        _uiState.update { it.copy(playbackSpeed = speed, showSpeedDialog = false) }
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        _uiState.update { it.copy(aspectRatioMode = mode, showAspectDialog = false) }
    }

    fun toggleControls() {
        if (_uiState.value.isLocked) return
        val visible = !_uiState.value.controlsVisible
        _uiState.update { it.copy(controlsVisible = visible) }
        if (visible) scheduleControlsHide()
    }

    fun toggleLock() {
        _uiState.update {
            it.copy(
                isLocked = !it.isLocked,
                controlsVisible = it.isLocked // show controls when unlocking
            )
        }
    }

    fun showSpeedDialog(show: Boolean) {
        _uiState.update { it.copy(showSpeedDialog = show) }
    }

    fun showAspectDialog(show: Boolean) {
        _uiState.update { it.copy(showAspectDialog = show) }
    }

    fun showSleepDialog(show: Boolean) {
        _uiState.update { it.copy(showSleepDialog = show) }
    }

    fun setSleepTimer(minutes: Int) {
        sleepJob?.cancel()
        if (minutes <= 0) {
            _uiState.update { it.copy(sleepRemainingMs = null, showSleepDialog = false) }
            return
        }
        val totalMs = minutes * 60_000L
        _uiState.update { it.copy(sleepRemainingMs = totalMs, showSleepDialog = false) }
        sleepJob = viewModelScope.launch {
            var remaining = totalMs
            while (remaining > 0) {
                delay(1000)
                remaining -= 1000
                _uiState.update { it.copy(sleepRemainingMs = remaining) }
            }
            playerManager.pause()
            _uiState.update { it.copy(sleepRemainingMs = null) }
        }
    }

    fun setVolumeIndicator(level: Float?) {
        _uiState.update { it.copy(volumeIndicator = level) }
    }

    fun setBrightnessIndicator(level: Float?) {
        _uiState.update { it.copy(brightnessIndicator = level) }
    }

    private fun scheduleControlsHide() {
        controlsHideJob?.cancel()
        controlsHideJob = viewModelScope.launch {
            delay(3500)
            if (!_uiState.value.isLocked) {
                _uiState.update { it.copy(controlsVisible = false) }
            }
        }
    }

    fun savePosition() {
        viewModelScope.launch {
            val pos = playerManager.getCurrentPosition()
            // We only have mediaId; title/uri saved earlier via repository if needed
            mediaRepository.savePlaybackPosition(
                com.videomediaplayer.app.domain.model.MediaItem(
                    id = currentMediaId,
                    uri = Uri.EMPTY,
                    path = "",
                    displayName = _uiState.value.title,
                    durationMs = playerManager.getDuration(),
                    sizeBytes = 0,
                    mimeType = "video/*",
                    dateAdded = 0,
                    dateModified = 0
                ),
                pos
            )
        }
    }

    override fun onCleared() {
        savePosition()
        positionJob?.cancel()
        controlsHideJob?.cancel()
        sleepJob?.cancel()
        // Do not fully release player here to allow background / PiP continuity
        super.onCleared()
    }
}
