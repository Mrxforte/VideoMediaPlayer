package com.videomediaplayer.app.presentation.screens.folders

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.videomediaplayer.app.domain.model.MediaItem
import com.videomediaplayer.app.presentation.components.MediaHeaderBar
import com.videomediaplayer.app.presentation.components.VideoGridItem
import com.videomediaplayer.app.presentation.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    folderPath: String,
    folderName: String,
    onBack: () -> Unit,
    onNavigateToPlayer: (MediaItem) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val rawVideos = uiState.videos.filter { it.folderPath == folderPath }
    val displayVideos = if (rawVideos.isNotEmpty()) rawVideos else mockDemoVideos()
    val totalSize = displayVideos.sumOf { it.sizeBytes }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (folderName.isBlank()) "Фильмы" else folderName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 22.sp
                        ),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* toggle grid layout */ }) {
                        Icon(Icons.Default.GridView, contentDescription = "Grid", tint = Color.White)
                    }
                    IconButton(onClick = { /* more */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E20)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // Header info bar matching screenshot 2: "19 VIDEOS  1.2 GB"
            MediaHeaderBar(
                videoCount = displayVideos.size,
                totalSizeBytes = totalSize
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 135.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayVideos, key = { it.id }) { item ->
                    VideoGridItem(
                        item = item,
                        onClick = { onNavigateToPlayer(item) }
                    )
                }
            }
        }
    }
}

private fun mockDemoVideos(): List<MediaItem> {
    val titles = listOf(
        "2016.First time out ...",
        "Travel.S01E09...",
        "2017.Big bear...",
        "Stories at the doorstep...",
        "The last rave party...",
        "One's holiday E23...",
        "Oktoberfest concert...",
        "Crazy holiday",
        "Hawaii Beach S02E09...",
        "Holiday travel HDT..."
    )
    return titles.mapIndexed { idx, name ->
        MediaItem(
            id = (idx + 100).toLong(),
            uri = Uri.EMPTY,
            path = "/demo/$name.mp4",
            displayName = name,
            durationMs = (1000L * (idx + 10) * 350) + 3600000L,
            sizeBytes = 120_000_000L,
            mimeType = "video/mp4",
            dateAdded = 1675800000L,
            dateModified = 1675800000L
        )
    }
}
