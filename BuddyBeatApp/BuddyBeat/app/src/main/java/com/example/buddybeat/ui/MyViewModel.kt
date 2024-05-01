package com.example.buddybeat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.data.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MyViewModel @Inject constructor(
    private val songRepo: AudioRepository
) : ViewModel() {

    private val _currentSong = MutableStateFlow(CurrentSong("", ""))
    val currentSong: StateFlow<CurrentSong> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _audioList = songRepo.getAllSongs()
    val audioList = _audioList


    fun insert(song: Song) = viewModelScope.launch {
        songRepo.insert(song)
    }

    fun changeSong(song: MediaItem) {
        val currentSongTitle = song.mediaMetadata.title.toString()
        val currentSongArtist = song.mediaMetadata.artist.toString()
        _currentSong.update {
            it.copy(
                title = currentSongTitle,
                artist = currentSongArtist
            )
        }
    }

    fun updateIsPlaying(isPlaying:Boolean) {
        _isPlaying.update{
            isPlaying
        }
    }

    fun updateDuration(duration : Long){
        _duration.update{
            duration
        }
    }

    fun updateProgress(progr:Long) {
        _progress.update {
            if (progr>0) (((progr.toFloat())/(duration.value.toFloat()) * 100f))
            else 0f
        }
    }
}

data class CurrentSong(val title : String, val artist: String)


