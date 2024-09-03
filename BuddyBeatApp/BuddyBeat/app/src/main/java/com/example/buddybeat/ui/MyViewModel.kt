package com.example.buddybeat.ui

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.mutableStateListOf
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.example.buddybeat.BeatExtractor
import com.example.buddybeat.DataStoreManager
import com.example.buddybeat.DataStoreManager.Companion.ALL_SONGS_KEY
import com.example.buddybeat.DataStoreManager.Companion.BPM_UPDATED_KEY
import com.example.buddybeat.DataStoreManager.Companion.FAVORITES_KEY
import com.example.buddybeat.DataStoreManager.Companion.IS_UPLOADED_KEY
import com.example.buddybeat.DataStoreManager.Companion.MODALITY
import com.example.buddybeat.DataStoreManager.Companion.MODE
import com.example.buddybeat.data.models.Playlist
import com.example.buddybeat.data.models.PlaylistSongCrossRef
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.data.repository.AudioRepository
import com.example.buddybeat.player.PlaybackService.Companion.AUTO_MODE
import com.example.buddybeat.player.PlaybackService.Companion.MANUAL_MODE
import com.example.buddybeat.player.PlaybackService.Companion.OFF_MODE
import com.example.buddybeat.player.PlaybackService.Companion.audiolist
import com.example.buddybeat.player.PlaybackService.Companion.manualBpm
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

    // PREFERENCES
    val isUploaded = dataStoreManager.getPreference(IS_UPLOADED_KEY).asLiveData(Dispatchers.IO)
    val bpmUpdated = dataStoreManager.getPreference(BPM_UPDATED_KEY).asLiveData(Dispatchers.IO)
    val allSongsId = dataStoreManager.getPreferenceLong(ALL_SONGS_KEY).asLiveData(Dispatchers.IO)
    val favoritesId = dataStoreManager.getPreferenceLong(FAVORITES_KEY).asLiveData(Dispatchers.IO)
    val mode = dataStoreManager.getPreferenceLong(MODE).asLiveData(Dispatchers.IO)
    val modality = dataStoreManager.getPreferenceLong(MODALITY).asLiveData(Dispatchers.IO)
    //val manualBpm = dataStoreManager.getPreferenceLong(MANUAL_BPM).asLiveData(Dispatchers.IO)

    private fun setPreference(key: Preferences.Key<Boolean>, value: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setPreference(key, value)
        }
    }

    private fun setPreferenceLong(key: Preferences.Key<Long>, value: Long) {
        viewModelScope.launch {
            dataStoreManager.setPreferenceLong(key, value)
        }
    }


    private val _progressLoading = MutableStateFlow(0)
    val progressLoading: StateFlow<Int> = _progressLoading.asStateFlow()


    private val _itemCount = songRepo.getCount()
    val itemCount: LiveData<Int> = _itemCount

    //STEP FREQ

    private val _stepFreq = MutableStateFlow(0)
    val stepFreq: StateFlow<Int> = _stepFreq.asStateFlow()

    //CURRENT SONG

    private val _currentSong = MutableStateFlow(CurrentSong(0L, "", "", "", -1))
    val currentSong: StateFlow<CurrentSong> = _currentSong.asStateFlow()

    /*private val _currentId = MutableStateFlow(-1L)
    val currentId: StateFlow<Long> = _currentId.asStateFlow()*/

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    /*private val _currentBpm = MutableStateFlow(0)
    val currentBpm: StateFlow<Int> = _currentBpm.asStateFlow()*/

    //CURRENT PLAYLIST

    //val currentPlaylist = MutableStateFlow(PlaylistWithSongs(Playlist(title = "", description = ""), mutableListOf()))
    val currentPlaylistId = MutableStateFlow(0L)


    //ALL PLAYLISTS
    private val _allPlaylist: LiveData<MutableList<PlaylistWithSongs>> = songRepo.getAllPlaylist()
    val allPlaylist = _allPlaylist

    //ALL SONGS
    private val _audioList = songRepo.getAllSongs()
    val allSongs = _audioList

    //QUEUE
    var queueList1 = mutableStateListOf<Song>()

    var queueList2 = mutableStateListOf<Song>()

    private val _showQueue = MutableStateFlow(false)
    val showQueue: StateFlow<Boolean> = _showQueue.asStateFlow()


    fun containsSong(idPlaylist: Long, idSong: Long): LiveData<Int> {
        return songRepo.containsSong(idPlaylist, idSong)
    }

    fun removeFromPlaylist(idPlaylist: Long, idSong: Long) {
        //Log.d("favoriteId", favoritesId.value.toString())
        viewModelScope.launch {
            val psc = PlaylistSongCrossRef(idPlaylist, idSong)
            songRepo.delete(psc)
            if (idPlaylist == allSongsId.value) {
                songRepo.delete(Song(songId = idSong, "", "", "", 0, ""))
            }
        }
    }

    fun addToPlaylist(idPlaylist: Long, idSong: Long) {
        //Log.d("favoriteId", favoritesId.value.toString())
        viewModelScope.launch {
            val psc = PlaylistSongCrossRef(idPlaylist, idSong)
            songRepo.insert(psc)
        }
    }

    fun setVisiblePlaylist(playlist: PlaylistWithSongs) {
        currentPlaylistId.value = playlist.playlist.playlistId
        //currentPlaylist.value = playlist
    }



    fun addPlaylist(playlist: Playlist) = viewModelScope.launch {
        songRepo.insert(playlist)
    }

    fun removePlaylist(playlist: Playlist) = viewModelScope.launch {
        songRepo.delete(playlist)
    }

    fun updatePlaylist(title: String, id: Long) = viewModelScope.launch {
        songRepo.updatePlaylist(title, id)
    }


    fun insertPlaylist(playlist: Playlist): Long {
        val defaultButtonDeferred: Deferred<Long> = CoroutineScope(Dispatchers.Default).async {
            songRepo.insertPlaylist(playlist)
        }
        // do other stuff
        return runBlocking { defaultButtonDeferred.await() }
    }

    private fun insertAllSongs(list: List<Song>, playlistId: Long) {
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

    //with REPLACE
    private fun insertSong(list: List<Song>): Flow<Long> = flow {
        for (i in list) {
            val x = songRepo.insertSong(i)
            emit(x)
        }
    }

    // functions to update bpm of songs at the beginning
    fun updateBpm() = viewModelScope.launch(Dispatchers.IO) {
        calculateBpm().collect { value ->
            songRepo.updateBpm(value.first, value.second)
            if(value.first == currentSong.value.id){
                _currentSong.update {
                    it.copy(
                        id = it.id,
                        title = it.title,
                        artist = it.artist,
                        uri = it.uri,
                        bpm = value.second
                    )
                }
            }
            if(audiolist.find { it.songId == value.first }!=null) {
                audiolist.find { it.songId == value.first }!!.bpm = value.second
            }
        }
    }

    private fun calculateBpm(): Flow<Pair<Long, Int>> = flow {
        //for each song in database calculate bpm and update
        for (i in songRepo.getSongs()) {
            if (songRepo.getBpm(i.songId) == -1) {
                val x = beatExtractor.beatDetection(
                    i.uri,
                    i.duration
                )
                emit(Pair(i.songId, x)) // emit next value
            }
            _progressLoading.update {
                _progressLoading.value + 1
            }
        }
        setPreference(BPM_UPDATED_KEY, true)
    }

    fun changeSong(song: MediaItem?) {
        if (song != null) {
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
                    uri = currentSongUri,
                    bpm = currentSongBpm ?: -1
                )
            }
        }

    }

    fun updateIsPlaying(isPlaying: Boolean) {
        _isPlaying.update {
            isPlaying
        }
    }

    fun updateDuration(duration: Long) {
        _duration.update {
            duration
        }
    }


    fun updateProgress(progr: Long) {
        _progress.update {
            if (progr > 0) (((progr.toFloat()) / (duration.value.toFloat()) * 100f))
            else 0f
        }
    }

    fun updateFreq(stepFreq: Int) {
        _stepFreq.update {
            stepFreq
        }
        if (showQueue.value)
            getQueue()
    }

    fun showQueue(value: Boolean) {
        _showQueue.update {
            value
        }
    }

    @OptIn(UnstableApi::class)
    private fun getQueue() {
        queueList1.clear()
        queueList1.addAll(queue)
        val target = when (modality.value) {
            AUTO_MODE -> stepFreq.value.toDouble()
            MANUAL_MODE -> manualBpm.toDouble()
            OFF_MODE -> 0.0
            else -> throw Exception("Invalid speed mode")
        }
        val l = if (target!=0.0) orderSongs(stepFreq.value.toDouble(), audiolist) else audiolist
        l.removeAll { playlist.contains(it.uri) }
        if(modality.value!=OFF_MODE)
            l.removeAll { it.bpm == -1 || it.bpm == 0 }
        /*l.forEach {
            Log.d(it.toString(), it.bpm.toString())
        }*/
        queueList2.clear()
        queueList2.addAll(l)
    }

    @OptIn(UnstableApi::class)
    fun orderSongs(target: Double, list: MutableList<Song>): MutableList<Song> {

        Log.d("Ordering", "Song list reordered")
        val myCustomComparator = Comparator<Song> { a, b ->

            when {
                a.bpm <= 0 && b.bpm <= 0 -> return@Comparator 0
                a.bpm <= 0 -> return@Comparator 1
                b.bpm <= 0 -> return@Comparator -1
                else -> {
                    var logBpmA = log2(a.bpm.toFloat())
                    var logBpmB = log2(b.bpm.toFloat())
                    var logTarget = log2(target)
                    logBpmA -= floor(logBpmA)
                    logBpmB -= floor(logBpmB)
                    logTarget -= floor(logTarget)
                    var distA = abs(logBpmA - logTarget)
                    var distB = abs(logBpmB - logTarget)
                    if (distA > 0.5f)
                        distA = 1.0f - distA
                    if (distB > 0.5f)
                        distB = 1.0f - distB
                    //Log.d("Comparator", "Comparing ${a.bpm} and ${b.bpm} => result: $distA, $distB")
                    when {
                        distA < distB -> return@Comparator -1
                        distA > distB -> return@Comparator 1
                        else -> return@Comparator 0
                    }
                }
            }

        }
        return list.sortedWith(myCustomComparator).toMutableList()
    }

    fun removeFromQueue1(song: Song) {
        queue.remove(song)
        getQueue()
    }

    fun removeFromQueue2(song: Song) {
        audiolist.remove(song)
        getQueue()
    }

    fun updateSongs() {
        val list = songRepo.getData()
        viewModelScope.launch(Dispatchers.IO) {
            updateSong(list).collect { value ->
                val psc = PlaylistSongCrossRef(allSongsId.value!!, value)
                Log.d("setting psc", psc.toString())
                songRepo.insert(psc)
            }
        }
        setPreference(IS_UPLOADED_KEY, true)
        setPreference(BPM_UPDATED_KEY, false)
    }

    //with IGNORE
    private fun updateSong(list: List<Song>): Flow<Long> = flow {
        val l = list.toMutableList()
        for (i in list) {
            try {
                songRepo.insert(i)
            } catch (e: Exception) {
                l.remove(i)
                Log.d("l", l.toString())
            }
        }
        for (i in l) {
            val d = songRepo.insertSong(i)
            Log.d("d", d.toString())
            emit(d)
        }


    }

    fun insertAllSongs() {
        val list = songRepo.getData()
        val allSongsPlaylist = Playlist(title = "ALL SONGS", description = "All songs")
        val favorites = Playlist(title = "FAVORITES", description = "Favorites")
        val l = insertPlaylist(allSongsPlaylist)
        Log.d("PlaylistIdL", l.toString())
        setPreferenceLong(ALL_SONGS_KEY, l)
        insertAllSongs(list, l)
        val d = insertPlaylist(favorites)
        Log.d("PlaylistIdD", d.toString())
        setPreferenceLong(FAVORITES_KEY, d)
    }

    fun setMode(mode: Long) {
        setPreferenceLong(MODE, mode)
    }

    fun setModality(modality: Long) {
        setPreferenceLong(MODALITY, modality)
    }
}

data class CurrentSong(val id: Long, val title: String, val artist: String, val uri: String, val bpm : Int)



