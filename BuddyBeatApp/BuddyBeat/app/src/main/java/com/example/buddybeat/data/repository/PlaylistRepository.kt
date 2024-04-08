package com.example.buddybeat.data.repository

import android.app.Application
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.buddybeat.data.SongDao
import com.example.buddybeat.data.SongDatabase
import com.example.buddybeat.data.models.Playlist
import com.example.buddybeat.data.models.PlaylistSongCrossRef
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.data.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaylistRepository(application: Application) {

    // Room executes all queries on a separate thread.

    private val songDao : SongDao
    private val playlists : LiveData<MutableList<Playlist>>
    private val playlistsWithSongs: LiveData<MutableList<PlaylistWithSongs>>

    init {
        val database : SongDatabase = SongDatabase.getDatabase(application)
        songDao = database.songDao()
        playlistsWithSongs = songDao.getPlaylistsWithSongs()
        playlists = songDao.getAllPlaylists()
    }

    fun getPlaylistsWithSongs() : LiveData<MutableList<PlaylistWithSongs>> {
        return playlistsWithSongs
    }

    fun getAllPlaylists() : LiveData<MutableList<Playlist>> {
        return playlists
    }

    fun getSongsFromPlaylist(id : Long) : LiveData<MutableList<Song>> {
        return songDao.getSongsFromPlaylist(id)
    }

    suspend fun findPlaylistById(id: Long): Playlist {
        return songDao.findPlaylistById(id)
    }

    @WorkerThread
    suspend fun insertPlaylist(playlist: Playlist): Long {
        return songDao.insertPlaylist(playlist)
    }


    @WorkerThread
    suspend fun insert(playlist: Playlist) {
        songDao.insert(playlist)
    }


    @WorkerThread
    suspend fun insert(ref: PlaylistSongCrossRef) {
        songDao.insert(ref)
    }

    @WorkerThread
    suspend fun delete(playlist: Playlist) {
        songDao.delete(playlist)
    }

    @WorkerThread
    suspend fun delete(ref: PlaylistSongCrossRef) {
        songDao.delete(ref)
    }
}