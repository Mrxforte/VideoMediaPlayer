package com.videomediaplayer.app.presentation.screens.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.videomediaplayer.app.domain.model.LibraryTab
import com.videomediaplayer.app.domain.model.MediaFolder
import com.videomediaplayer.app.domain.model.MediaItem
import com.videomediaplayer.app.domain.model.SortOption
import com.videomediaplayer.app.domain.model.ViewMode
import androidx.compose.material.icons.filled.Lock
import com.videomediaplayer.app.presentation.components.EmptyLibraryState
import com.videomediaplayer.app.presentation.components.FolderItem
import com.videomediaplayer.app.presentation.components.VideoGridItem
import com.videomediaplayer.app.presentation.components.VideoListItem
import com.videomediaplayer.app.presentation.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateToPlayer: (MediaItem) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToVault: () -> Unit = {},
    onNavigateToFolder: (MediaFolder) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val permissionState = rememberMultiplePermissionsState(permissions)

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // Open single file – create a temporary MediaItem
            val item = MediaItem(
                id = System.currentTimeMillis(),
                uri = it,
                path = it.toString(),
                displayName = it.lastPathSegment ?: "Video",
                durationMs = 0,
                sizeBytes = 0,
                mimeType = "video/*",
                dateAdded = 0,
                dateModified = 0
            )
            onNavigateToPlayer(item)
        }
    }

    LaunchedEffect(permissionState.allPermissionsGranted) {
        viewModel.setPermissionGranted(permissionState.allPermissionsGranted)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        androidx.compose.material3.OutlinedTextField(
                            value = searchText,
                            onValueChange = {
                                searchText = it
                                viewModel.setSearchQuery(it)
                            },
                            placeholder = { Text("Search videos…") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Video Media Player")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) {
                            searchText = ""
                            viewModel.setSearchQuery("")
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = {
                        val next = if (uiState.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
                        viewModel.setViewMode(next)
                    }) {
                        Icon(
                            imageVector = if (uiState.viewMode == ViewMode.GRID)
                                Icons.Default.List else Icons.Default.GridView,
                            contentDescription = "Toggle view"
                        )
                    }
                    IconButton(onClick = { showOverflow = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                        DropdownMenu(
                            expanded = showOverflow,
                            onDismissRequest = { showOverflow = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Безопасное хранение") },
                                onClick = {
                                    showOverflow = false
                                    onNavigateToVault()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showOverflow = false
                                    onNavigateToSettings()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Refresh") },
                                onClick = {
                                    showOverflow = false
                                    viewModel.loadMedia()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            val tabs = listOf(
                LibraryTab.ALL to "All Videos",
                LibraryTab.FOLDERS to "Folders",
                LibraryTab.RECENT to "Recent",
                LibraryTab.FAVORITES to "Favorites",
                LibraryTab.MUSIC to "Music"
            )
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOfFirst { it.first == uiState.currentTab }.coerceAtLeast(0),
                edgePadding = 8.dp
            ) {
                tabs.forEach { (tab, title) ->
                    Tab(
                        selected = uiState.currentTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(title) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                !uiState.hasPermission || (uiState.videos.isEmpty() && uiState.currentTab != LibraryTab.MUSIC) -> {
                    EmptyLibraryState(
                        onOpenFiles = {
                            openDocumentLauncher.launch(arrayOf("video/*", "audio/*"))
                        },
                        onRequestPermission = { permissionState.launchMultiplePermissionRequest() },
                        hasPermission = uiState.hasPermission
                    )
                }
                uiState.currentTab == LibraryTab.FOLDERS -> {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.folders, key = { it.path }) { folder ->
                            FolderItem(folder = folder, onClick = { onNavigateToFolder(folder) })
                        }
                    }
                }
                uiState.currentTab == LibraryTab.MUSIC -> {
                    if (uiState.audio.isEmpty()) {
                        EmptyLibraryState(
                            onOpenFiles = { openDocumentLauncher.launch(arrayOf("audio/*")) },
                            onRequestPermission = { permissionState.launchMultiplePermissionRequest() },
                            hasPermission = uiState.hasPermission
                        )
                    } else {
                        MediaList(
                            items = uiState.audio,
                            viewMode = uiState.viewMode,
                            onItemClick = onNavigateToPlayer
                        )
                    }
                }
                else -> {
                    MediaList(
                        items = uiState.filteredVideos,
                        viewMode = uiState.viewMode,
                        onItemClick = onNavigateToPlayer
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaList(
    items: List<MediaItem>,
    viewMode: ViewMode,
    onItemClick: (MediaItem) -> Unit
) {
    if (viewMode == ViewMode.GRID) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items, key = { it.id }) { item ->
                VideoGridItem(item = item, onClick = { onItemClick(item) })
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(items, key = { it.id }) { item ->
                VideoListItem(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}
