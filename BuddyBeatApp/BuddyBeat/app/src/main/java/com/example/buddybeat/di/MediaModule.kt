package com.example.buddybeat.di

import android.content.Context
import com.example.buddybeat.data.ContentResolverHelper
import com.example.buddybeat.data.SongDatabase
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
    ) : SongDatabase {
        val songdb = SongDatabase.getDatabase(context)
        return songdb
    }
}