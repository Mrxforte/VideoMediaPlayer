package com.videomediaplayer.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videomediaplayer.app.data.local.PreferencesManager
import com.videomediaplayer.app.data.repository.MediaRepository
import com.videomediaplayer.app.domain.model.LibraryTab
import com.videomediaplayer.app.domain.model.MediaFolder
import com.videomediaplayer.app.domain.model.MediaItem
import com.videomediaplayer.app.domain.model.SortOption
import com.videomediaplayer.app.domain.model.ViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val isLoading: Boolean = false,
    val videos: List<MediaItem> = emptyList(),
    val audio: List<MediaItem> = emptyList(),
    val folders: List<MediaFolder> = emptyList(),
    val filteredVideos: List<MediaItem> = emptyList(),
    val currentTab: LibraryTab = LibraryTab.ALL,
    val sortOption: SortOption = SortOption.NAME,
    val viewMode: ViewMode = ViewMode.GRID,
    val searchQuery: String = "",
    val hasPermission: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferencesManager.sortOption,
                preferencesManager.viewMode
            ) { sort, view ->
                sort to view
            }.collect { (sort, view) ->
                _uiState.update { it.copy(sortOption = sort, viewMode = view) }
            }
        }
    }

    fun setPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(hasPermission = granted, isLoading = if (!granted) false else it.isLoading) }
        if (granted) loadMedia()
    }

    fun loadMedia() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val sort = _uiState.value.sortOption
                val videos = mediaRepository.loadVideos(sort)
                val audio = mediaRepository.loadAudio(sort)
                val folders = mediaRepository.getFolders(videos)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        videos = videos,
                        audio = audio,
                        folders = folders,
                        filteredVideos = applyFilters(videos, it.currentTab, it.searchQuery)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load media")
                }
            }
        }
    }

    fun selectTab(tab: LibraryTab) {
        _uiState.update {
            it.copy(
                currentTab = tab,
                filteredVideos = applyFilters(it.videos, tab, it.searchQuery)
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredVideos = applyFilters(it.videos, it.currentTab, query)
            )
        }
    }

    fun setSortOption(option: SortOption) {
        viewModelScope.launch {
            preferencesManager.setSortOption(option)
            loadMedia()
        }
    }

    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch {
            preferencesManager.setViewMode(mode)
        }
    }

    private fun applyFilters(
        videos: List<MediaItem>,
        tab: LibraryTab,
        query: String
    ): List<MediaItem> {
        var result = when (tab) {
            LibraryTab.ALL, LibraryTab.FOLDERS -> videos
            LibraryTab.RECENT -> videos.sortedByDescending { it.dateModified }.take(50)
            LibraryTab.FAVORITES -> videos // favorites merged via history in a real app
            LibraryTab.MUSIC -> emptyList()
        }
        if (query.isNotBlank()) {
            result = result.filter {
                it.displayName.contains(query, ignoreCase = true)
            }
        }
        return result
    }

    fun getVideosInFolder(folderPath: String): List<MediaItem> {
        return _uiState.value.videos.filter { it.folderPath == folderPath }
    }
}
