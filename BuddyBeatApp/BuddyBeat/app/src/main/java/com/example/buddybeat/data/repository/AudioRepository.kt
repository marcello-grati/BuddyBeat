package com.example.buddybeat.data.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.buddybeat.data.ContentResolverHelper
import com.example.buddybeat.data.SongDatabase
import com.example.buddybeat.data.models.Playlist
import com.example.buddybeat.data.models.PlaylistSongCrossRef
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.data.models.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AudioRepository @Inject
constructor(private val contentResolverHelper: ContentResolverHelper,
            private val database: SongDatabase) {


    private val allSongs: LiveData<MutableList<Song>> = database.songDao().getAllSongs()
    private val allPlaylists : LiveData<MutableList<PlaylistWithSongs>> = database.songDao().getPlaylistsWithSongs()

    fun getAllSongs() : LiveData<MutableList<Song>> {
        return allSongs
    }

    fun getAllPlaylist() : LiveData<MutableList<PlaylistWithSongs>>{
        return allPlaylists
    }

    suspend fun getSongs() : List<Song> {
        return database.songDao().getSongs()
    }

    fun containsSong(playlistId:Long, songId:Long) : LiveData<Int> {
        return database.songDao().containsSong(playlistId,songId)
    }

    /*fun getSongsFromPlaylist(id:Long): LiveData<MutableList<Song>>{
        val l = database.songDao().getSongsFromPlaylist(id)
        Log.d("listsongs",l.value.toString())
        return l
    }*/

    fun getSongsOrdered() : LiveData<MutableList<Song>> {
        return database.songDao().getSongsOrdered()
    }

    fun getBpm(id: Long) : Int {
        return database.songDao().getBpm(id)
    }

    fun getCount() : LiveData<Int> {
        return database.songDao().getItemCount()
    }

    suspend fun updateBpm(id: Long, bpm : Int) {
        database.songDao().updateBpm(id, bpm)
    }

    @WorkerThread
    suspend fun insert(song: Song) {
        database.songDao().insert(song)
    }

    @WorkerThread
    suspend fun insert(playlist : Playlist) {
        database.songDao().insert(playlist)
    }

    @WorkerThread
    suspend fun insertPlaylist(playlist : Playlist) : Long {
        return database.songDao().insertPlaylist(playlist)
    }

    @WorkerThread
    suspend fun insertSong(song : Song) : Long {
        return database.songDao().insertSong(song)
    }

    @WorkerThread
    suspend fun insert(psc : PlaylistSongCrossRef) {
        database.songDao().insert(psc)
    }

    @WorkerThread
    suspend fun delete(psc : PlaylistSongCrossRef) {
        database.songDao().delete(psc)
    }


}