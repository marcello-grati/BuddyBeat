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
    // Observed Flow will notify the observer when the data has changed.
    private val allSongs: LiveData<MutableList<Song>>
    private val songDao : SongDao

    //inizializzazione del database, del songDao, e della lista di canzoni
    init {
        val database : SongDatabase = SongDatabase.getDatabase(application)
        songDao = database.songDao()
        allSongs = songDao.getAll()
    }

    fun getAllSongs() : LiveData<MutableList<Song>> {
        return allSongs
    }

    /*
    * Funzioni che chiamano le corrispondenti funzioni in SongDao.
    * Le operazioni sul database Room non vengono eseguite sul main thread ma sul thread determinato dal Dispatchers.IO
    * Sono dichiarate come suspend perchè anche withContext (che permette l'esecuzione su un thread separato)
    * è una suspend function. Queste funzioni verranno chiamate all'interno di una coroutine.
    */

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