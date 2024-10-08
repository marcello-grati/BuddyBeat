package com.example.buddybeat.data.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.buddybeat.data.ContentResolverHelper
import com.example.buddybeat.data.SongDatabase
import com.example.buddybeat.data.models.Playlist
import com.example.buddybeat.data.models.PlaylistSongCrossRef
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.data.models.Song
import javax.inject.Inject

class AudioRepository @Inject
constructor(private val contentResolverHelper: ContentResolverHelper,
            private val database: SongDatabase) {

    private val allPlaylists : LiveData<MutableList<PlaylistWithSongs>> = database.songDao().getPlaylistsWithSongs()

    fun getData() : List<Song>{
        return contentResolverHelper.getAudioData()
    }

    fun getCountBpm(): LiveData<Int> {
        return database.songDao().getCountBpm()
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

    suspend fun delete(playlist: Playlist){
        return database.songDao().delete(playlist)
    }

    suspend fun updatePlaylist(title:String, id:Long){
        return database.songDao().update(title, id)
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
    suspend fun insert(song: Song) : Long {
        return database.songDao().insert(song)
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

    @WorkerThread
    suspend fun delete(song : Song) {
        database.songDao().delete(song)
    }
}