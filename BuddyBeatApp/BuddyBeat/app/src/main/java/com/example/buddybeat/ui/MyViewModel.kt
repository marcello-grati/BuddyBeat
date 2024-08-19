package com.example.buddybeat.ui

import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
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

    val isUploaded = dataStoreManager.getPreference(IS_UPLOADED_KEY).asLiveData(Dispatchers.IO)
    val bpmUpdated = dataStoreManager.getPreference(BPM_UPDATED_KEY).asLiveData(Dispatchers.IO)

    private val _progressLoading = MutableStateFlow(0)
    val progressLoading: StateFlow<Int> = _progressLoading.asStateFlow()

    private val _audioList = songRepo.getAllSongs()
    val audioList = _audioList

    private val _itemCount = songRepo.getCount()
    val itemCount: LiveData<Int> = _itemCount

    private val _stepFreq = MutableStateFlow(0)
    val stepFreq: StateFlow<Int> = _stepFreq.asStateFlow()

    private val _currentSong = MutableStateFlow(CurrentSong("", "", ""))
    val currentSong: StateFlow<CurrentSong> = _currentSong.asStateFlow()

    private val _currentId = MutableStateFlow(-1L)
    val currentId: StateFlow<Long> = _currentId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentBpm = MutableStateFlow(0)
    val currentBpm: StateFlow<Int> = _currentBpm.asStateFlow()


    private fun setPreference(key : Preferences.Key<Boolean>, value : Boolean){
        viewModelScope.launch {
            dataStoreManager.setPreference(key, value)
        }
    }

    fun loadSongs(list: List<Song>) = viewModelScope.launch {
        list.forEach {
            songRepo.insert(it)
        }
        setPreference(IS_UPLOADED_KEY, true)
    }

    // functions to update bpm of songs at the beginning
    fun updateBpm() = viewModelScope.launch(Dispatchers.IO){
        calculateBpm().collect { value ->
            songRepo.updateBpm(value.first, value.second)
        }
    }
    private fun calculateBpm() : Flow<Pair<Long, Int>> = flow {
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


    fun changeSong(song: MediaItem?) {
        if(song!=null){
            val currentSongTitle = song.mediaMetadata.title.toString()
            val currentSongArtist = song.mediaMetadata.artist.toString()
            val currentSongUri = song.mediaId
            val currentSongBpm = song.mediaMetadata.extras?.getInt("bpm")
            val currentSongId = song.mediaMetadata.extras?.getLong("id")
            _currentSong.update {
                it.copy(
                    title = currentSongTitle,
                    artist = currentSongArtist,
                    uri = currentSongUri
                )
            }
            if (currentSongId != null)
                _currentId.update {
                    currentSongId
                }
            if (currentSongBpm != null)
                _currentBpm.update {
                    currentSongBpm
                }
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
        //orderSongs()
    }

    fun orderSongs(list: MutableList<Song>) : MutableList<Song> {

        Log.d("Ordering", "Song list reordered")
        val myCustomComparator =  Comparator<Song> { a, b ->

            when {
                a.bpm <= 0 && b.bpm <= 0 -> return@Comparator 0
                a.bpm <= 0 -> return@Comparator 1
                b.bpm <= 0 -> return@Comparator -1
                else -> {
                    var logBpmA = log2(a.bpm.toFloat())
                    var logBpmB = log2(b.bpm.toFloat())
                    val s = stepFreq.value.toFloat()
                    //val s = 180f
                    var logStepFreq = log2(s)
                    logBpmA -= floor(logBpmA)
                    logBpmB -= floor(logBpmB)
                    logStepFreq -= floor(logStepFreq)
                    var distA = abs(logBpmA - logStepFreq)
                    var distB = abs(logBpmB - logStepFreq)
                    if (distA > 0.5f)
                        distA = 1.0f - distA
                    if (distB > 0.5f)
                        distB = 1.0f - distB
                    //Log.d("Comparator", "Comparing ${a.bpm} and ${b.bpm} => result: $distA, $distB")
                    when {
                        distA < distB -> return@Comparator -1
                        distA > distB -> return@Comparator 1
                        else ->  return@Comparator 0
                    }
                }
            }

        }
        val l = list.sortedWith(myCustomComparator).toMutableList()
        l.forEach{
            Log.d(it.toString(), it.bpm.toString())
        }
        return l
    }
}

data class CurrentSong(val title : String, val artist: String, val uri: String)


