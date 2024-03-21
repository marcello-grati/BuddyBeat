package com.example.buddybeat.playlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.buddybeat.models.Song

class PlaylistViewModel(application: Application): AndroidViewModel(application) {
    val songs = MutableLiveData<ArrayList<Song>>()

    private fun setSongs(list: ArrayList<Song>) {
        songs.value = list
    }

    fun getSongs() {
        val mockSongs = ArrayList<Song>()
        mockSongs.add(Song(author = "Taylor Swift", description = "First song", duration = "3:31", name = "Antihero", number = 1))
        mockSongs.add(Song(author = "Harry Styles", description = "Second song", duration = "2:58", name = "Kiwi", number = 2))
        mockSongs.add(Song(author = "Teddy Swims", description = "Third song", duration = "2:23", name = "Lose Control", number = 3))
        mockSongs.add(Song(author = "Ariana Grande", description = "Fourth song", duration = "3:31", name = "the boy is mine", number = 4))
        mockSongs.add(Song(author = "Taylor Swift", description = "Fifth song", duration = "4:01", name = "Cruel Summer", number = 5))
        mockSongs.add(Song(author = "Noah Kahan", description = "Sixth song", duration = "3:31", name = "Stick Season", number = 6))
        mockSongs.add(Song(author = "Duo Lipa", description = "Seventh song", duration = "3:42", name = "Houdini", number = 7))
        setSongs(mockSongs)
    }
}