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
import com.example.buddybeat.DataStoreManager.Companion.ALL_SONGS_KEY
import com.example.buddybeat.DataStoreManager.Companion.BPM_UPDATED_KEY
import com.example.buddybeat.DataStoreManager.Companion.FAVORITES_KEY
import com.example.buddybeat.DataStoreManager.Companion.IS_UPLOADED_KEY
import com.example.buddybeat.data.models.Playlist
import com.example.buddybeat.data.models.PlaylistSongCrossRef
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.data.repository.AudioRepository
import com.example.buddybeat.player.PlaybackService.Companion.audiolist
import com.example.buddybeat.player.PlaybackService.Companion.playlist
import com.example.buddybeat.player.PlaybackService.Companion.queue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    val allSongsId = dataStoreManager.getPreferenceLong(ALL_SONGS_KEY).asLiveData(Dispatchers.IO)
    val favoritesId = dataStoreManager.getPreferenceLong(FAVORITES_KEY).asLiveData(Dispatchers.IO)


    private val _progressLoading = MutableStateFlow(0)
    val progressLoading: StateFlow<Int> = _progressLoading.asStateFlow()


    private val _itemCount = songRepo.getCount()
    val itemCount: LiveData<Int> = _itemCount

    //STEP FREQ

    private val _stepFreq = MutableStateFlow(0)
    val stepFreq: StateFlow<Int> = _stepFreq.asStateFlow()

    //CURRENT SONG

    private val _currentSong = MutableStateFlow(CurrentSong(0L, "", "", ""))
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

    //CURRENT PLAYLIST

    val currentPlaylist = MutableStateFlow(PlaylistWithSongs(Playlist(title="", description = ""), mutableListOf()))
    val currentPlaylistId = MutableStateFlow(0L)


    //ALL PLAYLISTS
    private val _allPlaylist : LiveData<MutableList<PlaylistWithSongs>> = songRepo.getAllPlaylist()
    val allPlaylist = _allPlaylist

    //ALL SONGS
    private val _audioList = songRepo.getAllSongs()
    val allSongs = _audioList

    //QUEUE
    private val _queueList = MutableStateFlow<MutableList<Song>>(mutableListOf())
    val queueList = _queueList.asLiveData()

    private val _showQueue = MutableStateFlow(false)
    val showQueue: StateFlow<Boolean> = _showQueue.asStateFlow()


    fun containsSong(idPlaylist: Long, idSong: Long) : LiveData<Int>{
        return songRepo.containsSong(idPlaylist,idSong)
    }

    fun removeFromPlaylist(idPlaylist : Long, idSong:Long){
        //Log.d("favoriteId", favoritesId.value.toString())
        viewModelScope.launch {
            val psc = PlaylistSongCrossRef(idPlaylist,idSong)
            songRepo.delete(psc)
        }
    }

    fun addToPlaylist(idPlaylist : Long, idSong:Long){
        //Log.d("favoriteId", favoritesId.value.toString())
            viewModelScope.launch {
                val psc = PlaylistSongCrossRef(idPlaylist,idSong)
                songRepo.insert(psc)
            }
    }

    fun setVisiblePlaylist(playlist : PlaylistWithSongs){
        currentPlaylistId.value = playlist.playlist.playlistId
        currentPlaylist.value = playlist
    }

    private fun setPreference(key : Preferences.Key<Boolean>, value : Boolean){
        viewModelScope.launch {
            dataStoreManager.setPreference(key, value)
        }
    }

    fun setPreferenceLong(key : Preferences.Key<Long>, value : Long){
        viewModelScope.launch {
            dataStoreManager.setPreferenceLong(key, value)
        }
    }

    fun addPlaylist(playlist : Playlist) = viewModelScope.launch {
        songRepo.insert(playlist)
    }

    fun removePlaylist(playlist : Playlist) = viewModelScope.launch {
        songRepo.delete(playlist)
    }

    fun updatePlaylist(title : String, id:Long) = viewModelScope.launch {
        songRepo.updatePlaylist(title, id)
    }



    fun insertPlaylist(playlist : Playlist): Long {
        val defaultButtonDeferred: Deferred<Long> = CoroutineScope(Dispatchers.Default).async {
            songRepo.insertPlaylist(playlist)
        }
        // do other stuff
        return runBlocking { defaultButtonDeferred.await() }
    }

    fun insertAllSongs(list: List<Song>, playlistId: Long) {
        setPreferenceLong(ALL_SONGS_KEY, playlistId)
        Log.d("insertingAllSongs", list.toString())
        viewModelScope.launch(Dispatchers.IO) {
            insertSong(list).collect { value ->
                val psc = PlaylistSongCrossRef(playlistId, value)
                Log.d("setting psc", psc.toString())
                songRepo.insert(psc)
            }
        }
        setPreference(IS_UPLOADED_KEY, true)
    }

    private fun insertSong(list: List<Song>) : Flow<Long> = flow {
        for (i in list) {
            val x = songRepo.insertSong(i)
            emit(x)
        }
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
                    id = currentSongId!!,
                    title = currentSongTitle,
                    artist = currentSongArtist,
                    uri = currentSongUri
                )
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
        if(showQueue.value)
            getQueue()
    }

    fun showQueue(value : Boolean){
        _showQueue.update {
            value
        }
    }

    private fun getQueue() {
        val queue = queue.toList()
        val l = orderSongs(audiolist.value)
        while(true){
            val nextSong = l.firstOrNull()
            if(nextSong != null) {
                if (!playlist.contains(nextSong.uri)) {
                    //mediaSession?.player?.seekToDefaultPosition(mediaSession?.player!!.currentMediaItemIndex + 1)
                    break
                }
                else l.removeFirstOrNull()
            }
        }
        l.forEach {
            Log.d(it.toString(), it.bpm.toString())
        }
        l.addAll(0,queue)
        _queueList.update {
            l
        }
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

    fun removeFromQueue(song: Song) {
        queue.remove(song)
        audiolist.value.remove(song)
        getQueue()
    }
}

data class CurrentSong(val id : Long, val title : String, val artist: String, val uri: String)



