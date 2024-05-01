package com.example.buddybeat.data.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.buddybeat.data.ContentResolverHelper
import com.example.buddybeat.data.SongDatabase
import com.example.buddybeat.data.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AudioRepository @Inject
constructor(private val contentResolverHelper: ContentResolverHelper,
            private val database: SongDatabase) {

    suspend fun getAudioData():List<Song> = withContext(Dispatchers.IO){
        contentResolverHelper.getAudioData()
    }

    private val allSongs: LiveData<MutableList<Song>> = database.songDao().getAllSongs()

    fun getAllSongs() : LiveData<MutableList<Song>> {
        return allSongs
    }

    @WorkerThread
    suspend fun insert(song: Song) {
        database.songDao().insert(song)
    }
}