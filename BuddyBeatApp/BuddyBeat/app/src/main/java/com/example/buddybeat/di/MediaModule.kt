package com.example.buddybeat.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.buddybeat.BeatExtractor
import com.example.buddybeat.DataStoreManager
import com.example.buddybeat.data.ContentResolverHelper
import com.example.buddybeat.data.SongDatabase
import com.example.buddybeat.data.repository.AudioRepository
import com.example.buddybeat.player.CustomMediaNotificationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*Module that provides singletons to the application*/
@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideDataStoreManager(
        @ApplicationContext context: Context
    ): DataStoreManager {
        return DataStoreManager(context)
    }

    @OptIn(UnstableApi::class)
    @Provides
    fun provideCustomMediaNotificationProvider(
        @ApplicationContext context: Context
    ): CustomMediaNotificationProvider {
        return CustomMediaNotificationProvider(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SongDatabase {
        return SongDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideAudioRepository(
        crh: ContentResolverHelper,
        db: SongDatabase
    ): AudioRepository {
        return AudioRepository(crh, db)
    }

    @Provides
    @Singleton
    fun provideBeatExtractor(
        @ApplicationContext context: Context
    ): BeatExtractor {
        return BeatExtractor(context)
    }

}