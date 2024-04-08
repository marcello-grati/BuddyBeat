package com.example.buddybeat.playlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.buddybeat.data.models.Playlist
import com.example.buddybeat.data.models.PlaylistSongCrossRef
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.data.repository.PlaylistRepository
import com.example.buddybeat.data.repository.SongRepository
import kotlinx.coroutines.launch


class PlaylistViewModel(application: Application, private var playlistId: Long) : AndroidViewModel(
    application
) {
    private val songRepo: SongRepository
    private val playlistRepo: PlaylistRepository

    // Using LiveData and caching what allSongs returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val playlist: LiveData<MutableList<Song>>

    init {
        songRepo = SongRepository(application)
        playlistRepo = PlaylistRepository(application)
        playlist = playlistRepo.getSongsFromPlaylist(playlistId)
    }

    suspend fun findPlaylistById(id: Long): Playlist {
        return playlistRepo.findPlaylistById(id)
    }

    fun insertInPlaylist(song: Song) {
        viewModelScope.launch {
            var songId = songRepo.insertSong(song)
            if (songId == -1L)
                songId = songRepo.getIdSong(song.uri)
            else
                playlistRepo.insert(PlaylistSongCrossRef(1, songId))
            playlistRepo.insert(PlaylistSongCrossRef(playlistId, songId))
        }
    }

    fun reInsertInPlaylist(song: Song) {
        viewModelScope.launch {
            var songId = songRepo.insertSong(song)
            if (songId == -1L)
                songId = songRepo.getIdSong(song.uri)
            playlistRepo.insert(PlaylistSongCrossRef(playlistId, songId))
        }
    }

    fun deleteSong(song: Song){
        viewModelScope.launch {
            val pl = PlaylistSongCrossRef(playlistId, song.songId)
            playlistRepo.delete(pl)
        }
    }

    class PlaylistViewModelFactory(
        private val mApplication: Application,
        private val mParam: Long
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return PlaylistViewModel(mApplication, mParam) as T
        }
    }
}


