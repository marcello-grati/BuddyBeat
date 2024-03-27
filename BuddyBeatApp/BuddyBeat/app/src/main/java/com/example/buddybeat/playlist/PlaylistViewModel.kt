package com.example.buddybeat.playlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.data.repository.SongRepository
import kotlinx.coroutines.launch

class PlaylistViewModel(application: Application) : AndroidViewModel(
    application
) {
    private val songRepo : SongRepository

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allSongs: LiveData<MutableList<Song>>

    init {
        songRepo = SongRepository(application)
        allSongs = songRepo.getAllSongs()
    }

    //funzione di inserimento che chiama la corrispondente funzione in SongRepository all'interno di una coroutine
    fun insert(song: Song) = viewModelScope.launch{
        songRepo.insert(song)
    }

    //funzione di rimozione che chiama la corrispondente funzione in SongRepository all'interno di una coroutine
    fun delete(song: Song) = viewModelScope.launch{
        songRepo.delete(song)
    }
}