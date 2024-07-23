package com.example.buddybeat.ui

import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.example.buddybeat.BeatExtractor
import com.example.buddybeat.DataStoreManager
import com.example.buddybeat.DataStoreManager.Companion.BPM_UPDATED_KEY
import com.example.buddybeat.DataStoreManager.Companion.IS_UPLOADED_KEY
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.data.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log2


@HiltViewModel
class MyViewModel @Inject constructor(
    private val songRepo: AudioRepository,
    private val beatExtractor: BeatExtractor,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _currentSong = MutableStateFlow(CurrentSong("", ""))
    val currentSong: StateFlow<CurrentSong> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentBpm = MutableStateFlow(0)
    val currentBpm: StateFlow<Int> = _currentBpm.asStateFlow()

    private val _audioList = songRepo.getAllSongs()
    val audioList = _audioList

    val isUploaded = dataStoreManager.getPreference(IS_UPLOADED_KEY).asLiveData(Dispatchers.IO)
    val bpmUpdated = dataStoreManager.getPreference(BPM_UPDATED_KEY).asLiveData(Dispatchers.IO)

    private val _progressLoading = MutableStateFlow(0)
    val progressLoading: StateFlow<Int> = _progressLoading.asStateFlow()

    private val _stepFreq = MutableStateFlow(0)
    val stepFreq: StateFlow<Int> = _stepFreq.asStateFlow()

    private val _itemCount = songRepo.getCount()
    val itemCount: LiveData<Int> = _itemCount


    private fun orderSongs() {

        Log.d("Ordering", "Song list reordered")
        val myCustomComparator =  Comparator<Song> { a, b ->

            when {
                a.bpm <= 0 && b.bpm <= 0 -> return@Comparator 0
                a.bpm <= 0 -> return@Comparator 1
                b.bpm <= 0 -> return@Comparator -1
                else -> {
                    var logBpmA = log2(a.bpm.toFloat())
                    var logBpmB = log2(b.bpm.toFloat())
                    var logStepFreq = log2(stepFreq.value.toFloat())
                    logBpmA -= floor(logBpmA)
                    logBpmB -= floor(logBpmB)
                    logStepFreq -= floor(logStepFreq)
                    val distA = abs(logBpmA - logStepFreq)
                    val distB = abs(logBpmB - logStepFreq)

                    when {
                        distA < distB -> return@Comparator -1
                        distA > distB -> return@Comparator 1
                        else ->  return@Comparator 0
                    }
                }
            }

        }
        audioList.value?.sortWith(myCustomComparator)
        Log.d("songlist", audioList.value.toString())
    }


    fun setPreference(key : Preferences.Key<Boolean>, value : Boolean){
        viewModelScope.launch {
            dataStoreManager.setPreference(key, value)
        }
    }

    private fun insert(song: Song) = viewModelScope.launch {
        songRepo.insert(song)
    }

    fun update(list: List<Song>) = viewModelScope.launch {
        var one = list.forEach {
            songRepo.insert(it)
        }
        setPreference(IS_UPLOADED_KEY, true)
    }

    // functions to update bpm of songs at the beginning
    fun updateBpm() = viewModelScope.launch(Dispatchers.IO){
        simple().collect { value ->
            songRepo.updateBpm(value.first, value.second)
        }
    }
    private fun simple() : Flow<Pair<Long, Int>> = flow {
        //for each song in database calculate bpm and update
        for (i in songRepo.getSongs()) {
            if (songRepo.getBpm(i.songId)==-1) {
                val x = beatExtractor.beatDetection(
                    i.uri,
                    i.duration
                )
                emit(Pair(i.songId, x)) // emit next value
            }
            _progressLoading.update {
                _progressLoading.value+1
            }
        }
        setPreference(BPM_UPDATED_KEY, true)
    }



    fun changeSong(song: MediaItem) {
        val currentSongTitle = song.mediaMetadata.title.toString()
        val currentSongArtist = song.mediaMetadata.artist.toString()
        val currentSongBpm = song.mediaMetadata.extras?.getInt("bpm")
        _currentSong.update {
            it.copy(
                title = currentSongTitle,
                artist = currentSongArtist
            )
        }
        if (currentSongBpm != null)
            _currentBpm.update {
                currentSongBpm
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

    fun updateFreq(stepFreq: Int) {
        _stepFreq.update{
            stepFreq
        }
        orderSongs()
    }
}

data class CurrentSong(val title : String, val artist: String)


