package com.example.buddybeat.data.repository

import android.app.Application
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.data.SongDao
import com.example.buddybeat.data.SongDatabase

class SongRepository(application: Application) {

    // Room executes all queries on a separate thread.
    private val allSongs: LiveData<MutableList<Song>>
    private val songDao : SongDao

    init {
        val database : SongDatabase = SongDatabase.getDatabase(application)
        songDao = database.songDao()
        allSongs = songDao.getAllSongs()
    }


    fun getAllSongs() : LiveData<MutableList<Song>> {
        return allSongs
    }

    /*fun getAllIds() : List<Long>{
        return songDao.getAllIds()
    }*/

    suspend fun findSongById(id: Long): Song {
        return songDao.findSongById(id)
    }

    suspend fun getIdSong(uri: String): Long {
        return songDao.getIdSong(uri)
    }


    @WorkerThread
    suspend fun insertSong(song: Song): Long {
        return songDao.insertSong(song)
    }

    @WorkerThread
    suspend fun insert(song: Song) {
        songDao.insert(song)
    }

    @WorkerThread
    suspend fun delete(song: Song) {
        songDao.delete(song)
    }
}