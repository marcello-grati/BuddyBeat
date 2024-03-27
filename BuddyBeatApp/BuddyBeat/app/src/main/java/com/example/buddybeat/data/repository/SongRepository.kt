package com.example.buddybeat.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.data.SongDao
import com.example.buddybeat.data.SongDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SongRepository(application: Application) {

    // Room executes all queries on a separate thread.
    private val allSongs: LiveData<MutableList<Song>>
    private val songDao : SongDao

    init {
        val database : SongDatabase = SongDatabase.getDatabase(application)
        songDao = database.songDao()
        allSongs = songDao.getAll()
    }

    fun getAllSongs() : LiveData<MutableList<Song>> {
        return allSongs
    }

    suspend fun findSongById(id : Int): Song {
        return withContext(Dispatchers.IO) {
            songDao.findSongById(id)
        }
    }

    suspend fun insert(song: Song) {
        return withContext(Dispatchers.IO) {
            songDao.insert(song)
        }
    }

    suspend fun delete(song: Song) {
        return withContext(Dispatchers.IO) {
            songDao.delete(song)
        }
    }
}