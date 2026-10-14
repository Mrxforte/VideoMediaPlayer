@file:OptIn(UnstableApi::class)

package com.videomediaplayer.app.playback

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var player: ExoPlayer? = null
    private val trackSelector = DefaultTrackSelector(context)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun getPlayer(): ExoPlayer {
        if (player == null) {
            player = ExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .build(),
                    true
                )
                .setHandleAudioBecomingNoisy(true)
                .build().also { exo ->
                    exo.addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.value = isPlaying
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_READY) {
                                _duration.value = exo.duration.coerceAtLeast(0L)
                            }
                        }
                    })
                }
        }
        return player!!
    }

    fun prepare(uri: android.net.Uri, startPositionMs: Long = 0L) {
        val exo = getPlayer()
        val mediaItem = MediaItem.fromUri(uri)
        exo.setMediaItem(mediaItem, startPositionMs)
        exo.prepare()
        exo.playWhenReady = true
    }

    fun play() {
        getPlayer().play()
    }

    fun pause() {
        getPlayer().pause()
    }

    fun togglePlayPause() {
        val exo = getPlayer()
        if (exo.isPlaying) exo.pause() else exo.play()
    }

    fun seekTo(positionMs: Long) {
        getPlayer().seekTo(positionMs.coerceAtLeast(0L))
    }

    fun seekBy(deltaMs: Long) {
        val exo = getPlayer()
        val newPos = (exo.currentPosition + deltaMs).coerceIn(0L, exo.duration.coerceAtLeast(0L))
        exo.seekTo(newPos)
    }

    fun setSpeed(speed: Float) {
        getPlayer().setPlaybackSpeed(speed)
        _playbackSpeed.value = speed
    }

    fun setRepeatMode(mode: Int) {
        getPlayer().repeatMode = mode
    }

    fun release() {
        player?.release()
        player = null
        _isPlaying.value = false
        _position.value = 0L
        _duration.value = 0L
    }

    fun updatePosition() {
        player?.let {
            _position.value = it.currentPosition
            _duration.value = it.duration.coerceAtLeast(0L)
        }
    }

    fun getCurrentPosition(): Long = player?.currentPosition ?: 0L

    fun getDuration(): Long = player?.duration?.coerceAtLeast(0L) ?: 0L
}
