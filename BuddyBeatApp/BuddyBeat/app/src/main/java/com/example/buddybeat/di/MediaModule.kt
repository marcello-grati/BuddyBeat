package com.example.buddybeat.di

import android.content.Context
import com.example.buddybeat.BeatExtractor
import com.example.buddybeat.DataStoreManager
import com.example.buddybeat.data.ContentResolverHelper
import com.example.buddybeat.data.SongDatabase
import com.example.buddybeat.data.repository.AudioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

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
        crh : ContentResolverHelper,
        db : SongDatabase
    ): AudioRepository {
        return AudioRepository(crh,db)
    }

    @Provides
    @Singleton
    fun provideBeatExtractor(
        @ApplicationContext context: Context
    ): BeatExtractor {
        return BeatExtractor(context)
    }

    @Provides
    @Singleton
    fun provideDataStoreManager(
        @ApplicationContext context: Context
    ): DataStoreManager {
        return DataStoreManager(context)
    }
    
}