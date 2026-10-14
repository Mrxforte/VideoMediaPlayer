package com.videomediaplayer.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.videomediaplayer.app.data.local.PlaybackHistoryDao
import com.videomediaplayer.app.data.local.PlaybackHistoryEntity
import com.videomediaplayer.app.domain.model.MediaFolder
import com.videomediaplayer.app.domain.model.MediaItem
import com.videomediaplayer.app.domain.model.SortOption
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val historyDao: PlaybackHistoryDao
) {

    suspend fun loadVideos(sort: SortOption = SortOption.NAME): List<MediaItem> =
        withContext(Dispatchers.IO) {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_ID
            )

            val sortOrder = when (sort) {
                SortOption.NAME -> "${MediaStore.Video.Media.DISPLAY_NAME} ASC"
                SortOption.DATE -> "${MediaStore.Video.Media.DATE_ADDED} DESC"
                SortOption.DURATION -> "${MediaStore.Video.Media.DURATION} DESC"
                SortOption.SIZE -> "${MediaStore.Video.Media.SIZE} DESC"
            }

            val items = mutableListOf<MediaItem>()
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val dateModCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val uri = ContentUris.withAppendedId(collection, id)
                    val path = cursor.getString(dataCol) ?: ""
                    val folderName = cursor.getString(bucketCol) ?: File(path).parentFile?.name ?: ""

                    items.add(
                        MediaItem(
                            id = id,
                            uri = uri,
                            path = path,
                            displayName = cursor.getString(nameCol) ?: "Unknown",
                            durationMs = cursor.getLong(durationCol),
                            sizeBytes = cursor.getLong(sizeCol),
                            width = cursor.getInt(widthCol),
                            height = cursor.getInt(heightCol),
                            mimeType = cursor.getString(mimeCol) ?: "video/*",
                            dateAdded = cursor.getLong(dateAddedCol),
                            dateModified = cursor.getLong(dateModCol),
                            folderName = folderName,
                            folderPath = File(path).parent ?: "",
                            isVideo = true
                        )
                    )
                }
            }
            items
        }

    suspend fun loadAudio(sort: SortOption = SortOption.NAME): List<MediaItem> =
        withContext(Dispatchers.IO) {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.BUCKET_DISPLAY_NAME
            )

            val sortOrder = when (sort) {
                SortOption.NAME -> "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"
                SortOption.DATE -> "${MediaStore.Audio.Media.DATE_ADDED} DESC"
                SortOption.DURATION -> "${MediaStore.Audio.Media.DURATION} DESC"
                SortOption.SIZE -> "${MediaStore.Audio.Media.SIZE} DESC"
            }

            val items = mutableListOf<MediaItem>()
            context.contentResolver.query(
                collection,
                projection,
                "${MediaStore.Audio.Media.IS_MUSIC} != 0",
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dateModCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val uri = ContentUris.withAppendedId(collection, id)
                    val path = cursor.getString(dataCol) ?: ""
                    items.add(
                        MediaItem(
                            id = id,
                            uri = uri,
                            path = path,
                            displayName = cursor.getString(nameCol) ?: "Unknown",
                            durationMs = cursor.getLong(durationCol),
                            sizeBytes = cursor.getLong(sizeCol),
                            mimeType = cursor.getString(mimeCol) ?: "audio/*",
                            dateAdded = cursor.getLong(dateAddedCol),
                            dateModified = cursor.getLong(dateModCol),
                            folderName = cursor.getString(bucketCol) ?: "",
                            folderPath = File(path).parent ?: "",
                            isVideo = false
                        )
                    )
                }
            }
            items
        }

    fun getFolders(videos: List<MediaItem>): List<MediaFolder> {
        return videos
            .groupBy { it.folderPath to it.folderName }
            .map { (key, items) ->
                MediaFolder(
                    name = key.second.ifBlank { "Unknown" },
                    path = key.first,
                    videoCount = items.size,
                    thumbnailUri = items.firstOrNull()?.uri
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    fun observeHistory(): Flow<List<PlaybackHistoryEntity>> = historyDao.getAllHistory()

    fun observeFavorites(): Flow<List<PlaybackHistoryEntity>> = historyDao.getFavorites()

    suspend fun savePlaybackPosition(item: MediaItem, positionMs: Long) {
        val entity = PlaybackHistoryEntity(
            mediaId = item.id,
            uri = item.uri.toString(),
            displayName = item.displayName,
            lastPositionMs = positionMs,
            durationMs = item.durationMs,
            lastPlayedAt = System.currentTimeMillis(),
            isFavorite = historyDao.getById(item.id)?.isFavorite ?: false
        )
        historyDao.insert(entity)
    }

    suspend fun toggleFavorite(id: Long, currentlyFavorite: Boolean) {
        val existing = historyDao.getById(id)
        if (existing != null) {
            historyDao.setFavorite(id, !currentlyFavorite)
        }
    }

    suspend fun getLastPosition(id: Long): Long {
        return historyDao.getById(id)?.lastPositionMs ?: 0L
    }

    suspend fun deleteHistory(id: Long) {
        historyDao.delete(id)
    }
}
