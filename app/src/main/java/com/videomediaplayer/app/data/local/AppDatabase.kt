package com.videomediaplayer.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey val mediaId: Long,
    val uri: String,
    val displayName: String,
    val lastPositionMs: Long,
    val durationMs: Long,
    val lastPlayedAt: Long,
    val isFavorite: Boolean = false
)

@Dao
interface PlaybackHistoryDao {
    @Query("SELECT * FROM playback_history ORDER BY lastPlayedAt DESC")
    fun getAllHistory(): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history WHERE isFavorite = 1 ORDER BY lastPlayedAt DESC")
    fun getFavorites(): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history WHERE mediaId = :id LIMIT 1")
    suspend fun getById(id: Long): PlaybackHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PlaybackHistoryEntity)

    @Update
    suspend fun update(entity: PlaybackHistoryEntity)

    @Query("UPDATE playback_history SET isFavorite = :favorite WHERE mediaId = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE playback_history SET lastPositionMs = :position, lastPlayedAt = :timestamp WHERE mediaId = :id")
    suspend fun updatePosition(id: Long, position: Long, timestamp: Long)

    @Query("DELETE FROM playback_history WHERE mediaId = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM playback_history")
    suspend fun clearAll()
}

@Database(
    entities = [PlaybackHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playbackHistoryDao(): PlaybackHistoryDao
}
