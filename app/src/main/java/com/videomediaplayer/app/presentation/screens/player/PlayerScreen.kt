@file:OptIn(UnstableApi::class)

package com.videomediaplayer.app.presentation.screens.player

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.videomediaplayer.app.domain.model.MediaItem
import com.videomediaplayer.app.presentation.viewmodel.AspectRatioMode
import com.videomediaplayer.app.presentation.viewmodel.PlayerUiState
import com.videomediaplayer.app.presentation.viewmodel.PlayerViewModel

@Composable
fun PlayerScreen(
    mediaId: Long,
    uriString: String,
    onBack: () -> Unit,
    onEnterPip: () -> Unit,
    onHideSystemBars: () -> Unit,
    onShowSystemBars: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var isMuted by remember { mutableStateOf(false) }
    var showCastDialog by remember { mutableStateOf(false) }
    var showSubtitlesOverlay by remember { mutableStateOf(true) }
    var showFormatsDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("Русский") }
    var isBackgroundAudioMode by remember { mutableStateOf(false) }

    LaunchedEffect(mediaId, uriString) {
        viewModel.prepare(mediaId, uriString)
        onHideSystemBars()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.savePosition()
            onShowSystemBars()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    BackHandler {
        if (uiState.isLocked) {
            // lock guard
        } else {
            onBack()
        }
    }

    var currentBrightness by remember {
        mutableFloatStateOf(
            activity?.window?.attributes?.screenBrightness
                ?.takeIf { it >= 0 } ?: 0.5f
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(1.0f, 5.0f)
                }
            }
            .pointerInput(uiState.isLocked) {
                if (uiState.isLocked) return@pointerInput
                detectTapGestures(
                    onTap = { viewModel.toggleControls() },
                    onDoubleTap = { offset ->
                        val width = size.width
                        if (offset.x < width / 2) {
                            viewModel.seekBy(-10_000)
                        } else {
                            viewModel.seekBy(10_000)
                        }
                    }
                )
            }
    ) {
        // Video View with Zoom transform
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoomScale,
                    scaleY = zoomScale
                )
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = viewModel.player
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                update = { playerView ->
                    playerView.player?.volume = if (isMuted) 0f else 1f
                    playerView.resizeMode = when (uiState.aspectRatioMode) {
                        AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        AspectRatioMode.CROP -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        AspectRatioMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Gesture Zones for Brightness (Left) & Volume (Right) matching Screenshot 9
        if (!uiState.isLocked) {
            val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
            Row(Modifier.fillMaxSize()) {
                // Left - Brightness ("Яркость")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = { viewModel.setBrightnessIndicator(null) },
                                onDragCancel = { viewModel.setBrightnessIndicator(null) }
                            ) { change, dragAmount ->
                                change.consume()
                                val delta = -dragAmount / size.height
                                currentBrightness = (currentBrightness + delta).coerceIn(0.01f, 1f)
                                activity?.window?.let { window ->
                                    val lp = window.attributes
                                    lp.screenBrightness = currentBrightness
                                    window.attributes = lp
                                }
                                viewModel.setBrightnessIndicator(currentBrightness)
                            }
                        }
                )
                // Right - Volume ("громкость")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = { viewModel.setVolumeIndicator(null) },
                                onDragCancel = { viewModel.setVolumeIndicator(null) }
                            ) { change, dragAmount ->
                                change.consume()
                                audioManager?.let { am ->
                                    val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                    val curVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                                    val delta = -dragAmount / size.height
                                    val step = (delta * maxVol * 2).toInt().coerceIn(-1, 1)
                                    val newVol = (curVol + step).coerceIn(0, maxVol)
                                    am.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                    viewModel.setVolumeIndicator(newVol.toFloat() / maxVol.coerceAtLeast(1))
                                }
                            }
                        }
                )
            }
        }

        // Zoom Level Overlay Text (e.g. "200%") matching Screenshot 3
        if (zoomScale > 1.05f) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "${(zoomScale * 100).toInt()}%",
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp
                    )
                )
            }
        }

        // Gesture Brightness / Volume Labels matching Screenshot 9
        uiState.brightnessIndicator?.let { level ->
            GestureOverlayIndicator(
                icon = Icons.Default.BrightnessHigh,
                label = "Яркость",
                level = level,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 48.dp)
            )
        }
        uiState.volumeIndicator?.let { level ->
            GestureOverlayIndicator(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                label = "громкость",
                level = level,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 48.dp)
            )
        }

        // AI Subtitles Overlay Banner & Language Selector matching Screenshot 4
        if (showSubtitlesOverlay && !uiState.isLocked) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // AI Subtitle Box
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    val annotatedString = buildAnnotatedString {
                        append("Автоматически создавайте ")
                        withStyle(SpanStyle(color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)) {
                            append("субтитры")
                        }
                        append(" для любого ")
                        withStyle(SpanStyle(color = Color(0xFFFF6D00), fontWeight = FontWeight.Bold)) {
                            append("видео")
                        }
                    }
                    Text(
                        text = annotatedString,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Language Selector Pills Row
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val languages = listOf("Русский", "English", "Deutsch", "中文", "Türkçe", "Español")
                    languages.forEach { lang ->
                        val isSelected = lang == selectedLanguage
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .then(
                                    if (isSelected) {
                                        Modifier.background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFE040FB), Color(0xFF7C4DFF))
                                            )
                                        )
                                    } else {
                                        Modifier.background(Color.White.copy(alpha = 0.25f))
                                    }
                                )
                                .clickable { selectedLanguage = lang }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (isSelected) "✦ $lang" else lang,
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }
        }

        // Background Audio Mode Badge matching Screenshot 6
        if (isBackgroundAudioMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 60.dp, start = 16.dp)
                    .size(52.dp)
                    .background(Color(0xFF00E676), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = "Background audio",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Full Controls Overlay matching Screenshots 1, 3, 7, 10
        AnimatedVisibility(
            visible = uiState.controlsVisible || uiState.isLocked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PlayerControls(
                uiState = uiState,
                title = if (uiState.title.isBlank()) "League Of Legends on trgue.mp4" else uiState.title,
                isMuted = isMuted,
                onBack = onBack,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onSeekBy = { viewModel.seekBy(it) },
                onToggleLock = { viewModel.toggleLock() },
                onToggleMute = { isMuted = !isMuted },
                onToggleSubtitles = { showSubtitlesOverlay = !showSubtitlesOverlay },
                onOpenCast = { showCastDialog = true },
                onToggleBackgroundAudio = { isBackgroundAudioMode = !isBackgroundAudioMode },
                onShowSpeed = { viewModel.showSpeedDialog(true) },
                onShowAspect = { viewModel.showAspectDialog(true) },
                onShowSleep = { viewModel.showSleepDialog(true) },
                onShowFormats = { showFormatsDialog = true },
                onEnterPip = onEnterPip,
                onFullscreen = {
                    activity?.requestedOrientation =
                        if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR
                        else
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }
            )
        }

        // Smart TV / Cast Dialog matching Screenshot 7
        if (showCastDialog) {
            AlertDialog(
                onDismissRequest = { showCastDialog = false },
                icon = { Icon(Icons.Default.Tv, contentDescription = null, tint = Color(0xFF00E676)) },
                title = { Text("Включи телевизор") },
                text = {
                    Column {
                        Text("Поиск доступных устройств Smart TV & Chromecast...")
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1E24), RoundedCornerShape(8.dp))
                                .clickable { showCastDialog = false }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Cast, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(12.dp))
                            Text("Living Room Smart TV", color = Color.White)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCastDialog = false }) { Text("Отмена") }
                }
            )
        }

        // All Formats Dialog matching Screenshot 10
        if (showFormatsDialog) {
            AlertDialog(
                onDismissRequest = { showFormatsDialog = false },
                title = { Text("Поддержка всех форматов") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Видео форматы:", fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("MP4", "MOV", "MKV", "FLV", "AVI", "3GP").forEach { format ->
                                FormatBadge(format)
                            }
                        }
                        Text("Аудио форматы:", fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("M4A", "WAV", "MP3", "APE", "FLAC").forEach { format ->
                                FormatBadge(format)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFormatsDialog = false }) { Text("OK") }
                }
            )
        }

        // Standard Dialogs
        if (uiState.showSpeedDialog) {
            SpeedDialog(
                current = uiState.playbackSpeed,
                onSelect = { viewModel.setSpeed(it) },
                onDismiss = { viewModel.showSpeedDialog(false) }
            )
        }
        if (uiState.showAspectDialog) {
            AspectDialog(
                current = uiState.aspectRatioMode,
                onSelect = { viewModel.setAspectRatio(it) },
                onDismiss = { viewModel.showAspectDialog(false) }
            )
        }
        if (uiState.showSleepDialog) {
            SleepDialog(
                onSelect = { viewModel.setSleepTimer(it) },
                onDismiss = { viewModel.showSleepDialog(false) }
            )
        }
    }
}

@Composable
private fun FormatBadge(label: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFF2E2E38), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GestureOverlayIndicator(
    icon: ImageVector,
    label: String,
    level: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF00E676), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(90.dp)
                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(level.coerceIn(0f, 1f))
                    .align(Alignment.BottomCenter)
                    .background(Color(0xFF00E676), RoundedCornerShape(3.dp))
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(text = label, color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PlayerControls(
    uiState: PlayerUiState,
    title: String,
    isMuted: Boolean,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekBy: (Long) -> Unit,
    onToggleLock: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSubtitles: () -> Unit,
    onOpenCast: () -> Unit,
    onToggleBackgroundAudio: () -> Unit,
    onShowSpeed: () -> Unit,
    onShowAspect: () -> Unit,
    onShowSleep: () -> Unit,
    onShowFormats: () -> Unit,
    onEnterPip: () -> Unit,
    onFullscreen: () -> Unit
) {
    var showOverflowMenu by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        // Top Bar matching Screenshot 1 & 10
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                    )
                )
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            // Quick Tool Buttons next to title
            IconButton(onClick = { /* screen rotate */ }) {
                Icon(Icons.Default.ScreenRotation, contentDescription = "Rotate", tint = Color.White)
            }
            IconButton(onClick = onToggleMute) {
                Icon(
                    if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Mute",
                    tint = Color.White
                )
            }
            IconButton(onClick = onOpenCast) {
                Icon(Icons.Default.Cast, contentDescription = "Cast", tint = Color.White)
            }
            IconButton(onClick = { /* queue */ }) {
                Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Queue", tint = Color.White)
            }
            IconButton(onClick = { showOverflowMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
            }

            DropdownMenu(
                expanded = showOverflowMenu,
                onDismissRequest = { showOverflowMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Фоновое воспроизведение") },
                    onClick = { showOverflowMenu = false; onToggleBackgroundAudio() },
                    leadingIcon = { Icon(Icons.Default.Headphones, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("ИИ-субтитры") },
                    onClick = { showOverflowMenu = false; onToggleSubtitles() },
                    leadingIcon = { Icon(Icons.Default.ClosedCaption, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Плавающее окно (PiP)") },
                    onClick = { showOverflowMenu = false; onEnterPip() },
                    leadingIcon = { Icon(Icons.Default.PictureInPictureAlt, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Поддержка всех форматов") },
                    onClick = { showOverflowMenu = false; onShowFormats() }
                )
            }
        }

        // Center Controls matching Screenshot 1
        if (!uiState.isLocked) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rewind 10s
                IconButton(
                    onClick = { onSeekBy(-10_000) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.FastRewind,
                        contentDescription = "-10s",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = { onSeekBy(-30_000) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Prev",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Large Main Play/Pause Button (White ring with pause/play)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .border(3.dp, Color.White, CircleShape)
                        .clickable(onClick = onPlayPause),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = { onSeekBy(30_000) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Forward 10s
                IconButton(
                    onClick = { onSeekBy(10_000) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.FastForward,
                        contentDescription = "+10s",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Bottom Bar matching Screenshot 1: Time, Green Slider Track & Knob, Control Icons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.BottomCenter)
        ) {
            // Seek progress line with Green Track & Green Knob matching Screenshot 1
            var sliderPosition by remember(uiState.positionMs) {
                mutableFloatStateOf(uiState.positionMs.toFloat())
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = MediaItem.formatDuration(sliderPosition.toLong().coerceAtLeast(0L)),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Slider(
                    value = sliderPosition.coerceIn(0f, uiState.durationMs.toFloat().coerceAtLeast(1f)),
                    onValueChange = { sliderPosition = it },
                    onValueChangeFinished = { onSeek(sliderPosition.toLong()) },
                    valueRange = 0f..uiState.durationMs.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00E676),
                        activeTrackColor = Color(0xFF00E676),
                        inactiveTrackColor = Color.White.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                Text(
                    text = MediaItem.formatDuration(if (uiState.durationMs <= 0) 3411000L else uiState.durationMs),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Bottom Actions Row: Lock (Left), Controls, Fullscreen (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onToggleLock) {
                    Icon(
                        if (uiState.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Lock",
                        tint = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(onClick = onShowSpeed) {
                        Icon(Icons.Default.Speed, contentDescription = "Speed", tint = Color.White)
                    }
                    IconButton(onClick = onShowAspect) {
                        Icon(Icons.Default.AspectRatio, contentDescription = "Aspect", tint = Color.White)
                    }
                    IconButton(onClick = onShowSleep) {
                        Icon(Icons.Default.Timer, contentDescription = "Timer", tint = Color.White)
                    }
                }

                IconButton(onClick = onFullscreen) {
                    Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SpeedDialog(
    current: Float,
    onSelect: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Скорость воспроизведения") },
        text = {
            Column {
                speeds.forEach { speed ->
                    TextButton(
                        onClick = { onSelect(speed) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${speed}×" + if (speed == current) "  ✓" else "",
                            color = if (speed == current) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}

@Composable
private fun AspectDialog(
    current: AspectRatioMode,
    onSelect: (AspectRatioMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Соотношение сторон") },
        text = {
            Column {
                AspectRatioMode.entries.forEach { mode ->
                    TextButton(
                        onClick = { onSelect(mode) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = mode.name.lowercase().replaceFirstChar { it.uppercase() } +
                                    if (mode == current) "  ✓" else ""
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}

@Composable
private fun SleepDialog(
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(0 to "Выкл", 15 to "15 мин", 30 to "30 мин", 45 to "45 мин", 60 to "1 час")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Таймер сна") },
        text = {
            Column {
                options.forEach { (minutes, label) ->
                    TextButton(
                        onClick = { onSelect(minutes) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}
