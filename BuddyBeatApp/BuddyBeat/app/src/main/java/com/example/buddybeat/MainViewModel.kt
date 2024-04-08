package com.example.buddybeat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.buddybeat.data.models.Playlist
import com.example.buddybeat.data.models.PlaylistSongCrossRef
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.data.repository.PlaylistRepository
import com.example.buddybeat.data.repository.SongRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application): AndroidViewModel(application){

    private val songRepo = SongRepository(application)
    private val playlistRepo = PlaylistRepository(application)
    //private val _currentSongId = MutableLiveData<Long>()
    //private val _currentPlaylistId = MutableLiveData<Long>()

    //val currentSongId: LiveData<Long> = _currentSongId
    //val currentPlaylistId: LiveData<Long> = _currentPlaylistId

    private val playlists : LiveData<MutableList<PlaylistWithSongs>> = playlistRepo.getPlaylistsWithSongs()
    private val songs : LiveData<MutableList<Song>> = songRepo.getAllSongs()


    fun addMusic(songs: ArrayList<Song>) = viewModelScope.launch{
        if (playlistRepo.findPlaylistById(1)==null)
            playlistRepo.insertPlaylist(Playlist(1, title = "Songs", description="All songs"))
        for (item in songs) {
            val id = songRepo.insertSong(item)
            val pl = PlaylistSongCrossRef(playlistId = 1L, songId = id)
            insert(pl)
        }
    }

    private fun insert(ref: PlaylistSongCrossRef) = viewModelScope.launch{
        playlistRepo.insert(ref)
    }

    fun getAllPlaylists() : LiveData<MutableList<PlaylistWithSongs>>{
        return playlists
    }

    fun getAllSongs() : LiveData<MutableList<Song>>{
        return songs
    }

    suspend fun findSongById(id: Long): Song {
        return songRepo.findSongById(id)
    }

    fun insert(playlist: Playlist) = viewModelScope.launch {
        playlistRepo.insert(playlist)
    }

    fun delete(playlist: Playlist) = viewModelScope.launch{
        playlistRepo.delete(playlist)
    }

    /*private fun insert(song: Song) = viewModelScope.launch{
       songRepo.insert(song)
   }*/



    /*fun getAllIds() : List<Long>{
        return songRepo.getAllIds()
    }*/




    /*fun returnCurrentPlaylist() : Playlist {
        return runBlocking { playlistRepo.findPlaylistById(currentPlaylistId.value!!)}
    }

    fun getSongs(): List<Song>? {
        return playlistRepo.getSongsOfPlaylist(currentPlaylistId.value!!)
    }

    fun setSong(songId: Long){
        _currentSongId.value = songId
    }

    fun setPlaylist(playlistId: Long){
        _currentPlaylistId.value = playlistId
    }

    fun nextSong() {

    }

    fun previousSong() {

    }

    fun returnCurrentSong() : Song{
        return runBlocking { songRepo.findSongById(currentSongId.value!!)}
    }*/
}