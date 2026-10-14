package com.videomediaplayer.app.domain.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val path: String,
    val displayName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val width: Int = 0,
    val height: Int = 0,
    val mimeType: String,
    val dateAdded: Long,
    val dateModified: Long,
    val folderName: String = "",
    val folderPath: String = "",
    val isFavorite: Boolean = false,
    val lastPositionMs: Long = 0L,
    val isVideo: Boolean = true
) {
    val resolution: String
        get() = if (width > 0 && height > 0) "${width}x$height" else ""

    val formattedDuration: String
        get() = formatDuration(durationMs)

    val formattedSize: String
        get() = formatSize(sizeBytes)

    companion object {
        fun formatDuration(ms: Long): String {
            if (ms <= 0) return "00:00"
            val totalSeconds = ms / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

        fun formatSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1 -> String.format("%.1f GB", gb)
                mb >= 1 -> String.format("%.1f MB", mb)
                else -> String.format("%.0f KB", kb)
            }
        }
    }
}

data class MediaFolder(
    val name: String,
    val path: String,
    val videoCount: Int,
    val thumbnailUri: Uri? = null
)

enum class SortOption {
    NAME, DATE, DURATION, SIZE
}

enum class ViewMode {
    GRID, LIST
}

enum class LibraryTab {
    ALL, FOLDERS, RECENT, FAVORITES, MUSIC
}
