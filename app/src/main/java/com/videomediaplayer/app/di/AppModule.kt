package com.videomediaplayer.app.di

import android.content.Context
import androidx.room.Room
import com.videomediaplayer.app.data.local.AppDatabase
import com.videomediaplayer.app.data.local.PlaybackHistoryDao
import com.videomediaplayer.app.data.local.PreferencesManager
import com.videomediaplayer.app.data.repository.MediaRepository
import com.videomediaplayer.app.playback.PlayerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "video_media_player.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(db: AppDatabase): PlaybackHistoryDao = db.playbackHistoryDao()

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideMediaRepository(
        @ApplicationContext context: Context,
        dao: PlaybackHistoryDao
    ): MediaRepository = MediaRepository(context, dao)

    @Provides
    @Singleton
    fun providePlayerManager(@ApplicationContext context: Context): PlayerManager {
        return PlayerManager(context)
    }
}
