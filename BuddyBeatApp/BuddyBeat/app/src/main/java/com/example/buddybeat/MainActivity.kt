package com.example.buddybeat

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.player.PlaybackService
import com.example.buddybeat.player.PlaybackService.Companion.BPM_STEP
import com.example.buddybeat.player.PlaybackService.Companion.audiolist
import com.example.buddybeat.player.PlaybackService.Companion.manualBpm
import com.example.buddybeat.player.PlaybackService.Companion.playlist
import com.example.buddybeat.player.PlaybackService.Companion.queue
import com.example.buddybeat.player.PlaybackService.Companion.speedMode
import com.example.buddybeat.ui.MyViewModel
import com.example.buddybeat.ui.audio.MusicPlayerApp
import com.example.buddybeat.ui.theme.BuddyBeatTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    private val viewModel: MyViewModel by viewModels()

    var showPlayer = false

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() =
            if (controllerFuture.isDone && !controllerFuture.isCancelled) controllerFuture.get() else null

    private var job: Job? = Job()

    var speed: Float = 1f

    var _ratio: MutableStateFlow<Float> = MutableStateFlow(1f)
    val ratio: StateFlow<Float> = _ratio.asStateFlow()

    //to check
    private var listener = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    controller?.let { viewModel.updateDuration(it.duration) }
                    Log.d("onPlaybackStateChangedR", playbackState.toString())
                }

                Player.STATE_ENDED -> {
                    controller?.playWhenReady = true
                    nextSong()
                    Log.d("onPlaybackStateChangedE", playbackState.toString())
                }

                Player.STATE_IDLE -> {
                    controller?.prepare()
                    Log.d("onPlaybackStateChangedI", playbackState.toString())
                }

                Player.STATE_BUFFERING -> {
                    //
                }
            }

        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            viewModel.updateIsPlaying(isPlaying)
        }

        //changing song
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Log.d(
                "IOOOO",
                "onMediaItemTransition with media " + mediaItem?.mediaMetadata?.title.toString()
            )
            controller?.currentMediaItem?.let { viewModel.changeSong(it) }
            controller?.let { viewModel.updateDuration(it.duration) }
            controller?.currentMediaItem?.mediaMetadata?.extras?.getInt("bpm")?.let { /*mService.updateBpm(it)*/ }
            PlaybackService.ratio = 1.0f
        }
    }

    //Bind to SensorService
    private lateinit var mService: SensorService
    private var mBound: Boolean = false


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as SensorService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val interval: Long = 1000

    private val sensorDataRunnable = object : Runnable {
        override fun run() {
            if (mBound) {
                updateDataTextView()
                handler.postDelayed(this, interval)
            }
        }
    }
    
    private fun toggleSpeedMode() {
        speedMode = (speedMode + 1) % 3
    }

//    fun setManualBpm(bpm : Int) {
//        manualBpm = bpm
//    }
    fun increaseManualBpm() {
        manualBpm += BPM_STEP
    }
    fun decreaseManualBpm() {
        manualBpm -= BPM_STEP
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startSensorService() {
        startForegroundService(Intent(this, SensorService::class.java))
        Intent(this, SensorService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        handler.postDelayed(sensorDataRunnable, interval)
    }

    private fun changeShow(){
        showPlayer = false
    }



    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent : String? = intent.extras?.getString("DESTINATION")
        if(intent == "player")
            showPlayer = true

        setContent {
            val permissionsRequested by remember { mutableStateOf(false) }

            BuddyBeatTheme {
                val state = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    rememberMultiplePermissionsState(
                        permissions = listOf(
                            Manifest.permission.READ_MEDIA_AUDIO,
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    )
                } else {
                    rememberMultiplePermissionsState(permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                }
                /*LaunchedEffect(Unit) {
                    if(!state.allPermissionsGranted){
                        state.launchMultiplePermissionRequest()
                    }
                }
                LaunchedEffect(state.allPermissionsGranted) {
                    if (state.allPermissionsGranted) {
                        startSensorService()
                        val list = ContentResolverHelper(applicationContext).getAudioData()
                        viewModel.loadSongs(list)
                    }
                }*/
                when {
                    state.allPermissionsGranted -> {
                        startSensorService()
                        val isUploaded by viewModel.isUploaded.observeAsState()
                        val bpmUpdated by viewModel.bpmUpdated.observeAsState()
                        val r by ratio.collectAsState()
                        if (isUploaded == true && bpmUpdated == false) {
                            Log.d("IOOOO", "Songs are uploaded but bpm not updated")
                            viewModel.updateBpm()
                        }
                        if (bpmUpdated == true) {
                            Log.d("IOOOO", "BPMs are updated")
                        }
                        //PlayScreenDesign()
                        MusicPlayerApp(
                            showPlayer = showPlayer,
                            changeShow = {
                                changeShow()
                            },
                            viewModel = viewModel,
                            onItemClick = {
                                //setSong(it)
                            },
                            nextSong = {
                                nextSong()
                            },
                            onStart = {
                                playPause()
                            },
                            incrementSpeed = {
                                incrementSpeed(+1)
                            },
                            decrementSpeed = {
                                incrementSpeed(-1)
                            },
                            onProgress = {
                                onProgress(it)
                            },
                            prevSong = {
                                prevSong()
                            },
                            text3 = r.toString(),
                            toggleMode = {
                                toggleSpeedMode()
                            },
                            plus = {
                                increaseManualBpm()
                            },
                            minus = {
                                decreaseManualBpm()
                            },
                            addToQueue = {
                                addToQueue(it)
                            },
                            playPause = {
                                playPause()
                            },
                            buildMediaItem = {
                                buildMediaItem(it)
                            },
                            setSongInPlaylist = {
                                setSongInPlaylist(it)
                            },
                            setMode = {
                                viewModel.setMode(it)
                                //mService.changeMode(it)
                            }
                        )
                    }
//                    else -> RequiredPermission(state = state) {
//                        state.launchMultiplePermissionRequest()
//                    }
                    else -> RequiredPermission(state = state)
                }
            }
        }
    }

    private fun addToQueue(song: Song) {
        queue.add(song)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun RequiredPermission(state: MultiplePermissionsState) {
        Scaffold {
            DisposableEffect(state) {
                state.launchMultiplePermissionRequest()
                onDispose {
                    /*val list = ContentResolverHelper(applicationContext).getAudioData()
                    val allSongsPlaylist = Playlist(title = "ALL SONGS", description = "All songs")
                    val favorites = Playlist(title = "FAVORITES", description = "Favorites")
                    val l = viewModel.insertPlaylist(allSongsPlaylist)
                    Log.d("PlaylistIdL", l.toString())
                    viewModel.setPreferenceLong(ALL_SONGS_KEY, l)
                    viewModel.insertAllSongs(list,l)
                    val d = viewModel.insertPlaylist(favorites)
                    Log.d("PlaylistIdD", d.toString())
                    viewModel.setPreferenceLong(FAVORITES_KEY, d)*/
                    viewModel.insertAllSongs()
                    //viewModel.insertAllSongs(l)
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(Modifier.padding(vertical = 120.dp, horizontal = 16.dp)) {
                    Icon(
                        Icons.Rounded.LibraryMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    val text = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        "Read external storage permissions required"
                    } else {
                        "Read external storage and notification permissions required"
                    }
                    Text(
                        text,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    val text1 = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        "This is required in order for the app to collect the songs"
                    } else {
                        "This is required in order for the app to collect the songs and calculate the step frequency"
                    }
                    Text(text1)
                }
                val context = LocalContext.current
                Button(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    onClick = {
                        val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        startActivity(intent)
                    }) {
                    Text("Go to settings")
                }
            }
        }
    }

    /*@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalPermissionsApi::class, DelicateCoroutinesApi::class)
    @Composable
    fun RequiredPermission(
        state: MultiplePermissionsState,
        onRequestPermission: () -> Unit
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(Modifier.padding(vertical = 120.dp, horizontal = 16.dp)) {
                Icon(
                    Icons.Rounded.LibraryMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Read external storage permission required",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(4.dp))
                Text("This is required in order for the app to collect the songs")
            }
            val context = LocalContext.current
            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = {
                    if (state.shouldShowRationale) {
                        onRequestPermission()
                    } else {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                }
            ) {
                Text(if (state.shouldShowRationale) "Request Permission" else "Go to settings")
            }
        }
    }*/


    private fun updateDataTextView() {
        run {
            viewModel.updateFreq(mService.stepFreq)
            _ratio.update { controller?.playbackParameters?.speed ?: 1f }
        }
    }

//    private fun updateRatio() {
//
//        if (speedMode != OFF_MODE) {
//
//            val stepFreq = when (speedMode) {
//                AUTO_MODE -> mService.stepFreq.toFloat()
//                MANUAL_MODE -> manualBpm.toFloat()
//                else -> throw Exception("Invalid speed mode")
//            }
//            var bpm = controller?.mediaMetadata?.extras?.getInt("bpm")
//            var inRatio = 1f
//            var outRatio = _ratio.value
//
//            if (bpm != 0 && bpm != null) {
//
//                // We compute the log_2() of step frequency and of double, half and original value of BPM
//                val logStepFreq = log2(stepFreq)
//                var logBpm = log2(bpm.toFloat())
//                var logHalfBPM = log2(bpm.toFloat() / 2.0f)
//                var logDoubleBPM = log2(bpm.toFloat() * 2.0f)
//
//                // We update BPM if one of its multiples has a smaller distance value
//                while (abs(logStepFreq - logBpm) > abs(logStepFreq - logHalfBPM)) {
//                    bpm /= 2
//                    logBpm = logHalfBPM
//                    logHalfBPM = log2(bpm.toFloat() / 2.0f)
//                }
//                while (abs(logStepFreq - logBpm) > abs(logStepFreq - logDoubleBPM)) {
//                    bpm *= 2
//                    logBpm = logDoubleBPM
//                    logDoubleBPM = log2(bpm.toFloat() * 2.0f)
//                }
//                // Speed-up ratio computed as step frequency / BPM
//                inRatio = stepFreq / bpm.toFloat()
//            }
//            if (stepFreq < 60)
//                inRatio = 1f
//
//            // ratio forced into [0.8, 1.2] range
//            inRatio = inRatio.coerceAtLeast(0.8f)
//            inRatio = inRatio.coerceAtMost(1.25f)
//
//            outRatio = ALPHA * outRatio + (1 - ALPHA) * inRatio
//
//            _ratio.update { outRatio }
//        } else {
//            _ratio.update { 1f }
//        }
//    }

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        val sessionToken =
            SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({ setController() }, ContextCompat.getMainExecutor(this))
        //controllerFuture.addListener({ setController() }, MoreExecutors.directExecutor())
    }

    private fun setController() {
        val controller = this.controller ?: return
        controller.addListener(listener)
        initController(controller)
    }

    private fun initController(controller: MediaController) {
        Log.d("IOOOO", "initController")
        controller.currentMediaItem?.let { viewModel.changeSong(it) }
        controller.isPlaying.let { viewModel.updateIsPlaying(it) }
        controller.duration.let { viewModel.updateDuration(it) }
        controller.currentPosition.let { viewModel.updateProgress(it) }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onResume() {
        super.onResume()
        GlobalScope.launch(Dispatchers.Main) { startProgressUpdate() }
    }

    override fun onStop() {
        super.onStop()
        MediaController.releaseFuture(controllerFuture)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProgressUpdate()
        unbindService(connection)
        mBound = false
    }

    private suspend fun startProgressUpdate() {
        job?.run {
            while (isActive) {
                delay(200)
                val pos = controller?.currentPosition
                if (pos != null) {
                    viewModel.updateProgress(pos)
                }
            }
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
    }


    //used in UI

    /*private fun setPlaylist() {
//        Log.d("IOOOO", "setting playlist..." )
////        viewModel.audioList.value?.forEach { audio ->
////            val media = buildMediaItem(audio)
////            controller?.addMediaItem(media)
////        }
//        val mediaItems = viewModel.audioList.value?.map { mediaitem -> buildMediaItem(mediaitem)}
//        if (mediaItems != null) {
//            controller?.addMediaItems(mediaItems)
//        }
//        //controller?.clearMediaItems()
//        Log.d("IOOOO", "Playlist set " + controller?.mediaItemCount.toString() )
    }*/

    private fun setSongInPlaylist(media: MediaItem) {
        Log.d("IOOOO", "setting media: ${media.mediaId}")
        //Log.d("IOOOO", "currentMediaItemIndex + currentMediaItem" +controller?.currentMediaItemIndex.toString() + "   " + controller?.currentMediaItem.toString())
        controller?.addMediaItem(controller!!.currentMediaItemIndex+1, media)
        //Log.d("IOOOO", "currentMediaItemIndex + currentMediaItem" +controller?.currentMediaItemIndex.toString() + "   " + controller?.getMediaItemAt(controller!!.currentMediaItemIndex).toString())
        controller?.seekToDefaultPosition(controller!!.currentMediaItemIndex + 1)
        //controller?.seekToDefaultPosition(controller!!.mediaItemCount+1)
        Log.d(
            "IOOOO",
            "currentMediaItemIndex + currentMediaItem" + controller?.currentMediaItemIndex.toString() + "   " + controller?.currentMediaItem.toString()
        )
        //controller?.removeMediaItems(controller!!.currentMediaItemIndex+2, controller!!.mediaItemCount)
        Log.d("IOOOO", "mediaItemCount: " + controller?.mediaItemCount.toString())
    }

    /*private fun setSong(index: Int) {
        Log.d("IOOOO", "index clicked: $index")
        if(audioListId.value!=viewModel.currentPlaylistId.value) {
            audioListId.update {
                viewModel.currentPlaylistId.value
            }
            audiolist.update{
                viewModel.allPlaylist.value?.find { it.playlist.playlistId == viewModel.currentPlaylistId.value }?.songs
                    ?: mutableListOf()
            }
        }
        val song = viewModel.allPlaylist.value?.find { it.playlist.playlistId == viewModel.currentPlaylistId.value }?.songs?.sortedBy{it.title}
            ?.get(index)
        Log.d("IOOOO", "Song clicked: $song")
        if (song.songId == viewModel.currentId.value) {
            Log.d("IOOOO", "same id: ${song.songId}")
            playPause()
        } else {
            Log.d("IOOOO", "not same id, new id: ${song.songId}")
            val media = buildMediaItem(song)
            setSongInPlaylist(media)
            playPause()
        }
    }*/

    private fun buildMediaItem(audio: Song): MediaItem {
        Log.d("IOOOO bpm" , audio.bpm.toString())
        return MediaItem.Builder()
            .setMediaId(audio.uri)
            .setRequestMetadata(
                MediaItem.RequestMetadata.Builder()
                    .setMediaUri(audio.uri.toUri())
                    .build()
            )
            .setUri(audio.uri.toUri())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setAlbumArtist(audio.artist)
                    .setArtist(audio.artist)
                    .setTitle(audio.title)
                    .setDisplayTitle(audio.title)
                    .setExtras(Bundle().apply {
                        putInt("bpm", audio.bpm)
                        putLong("id", audio.songId)
                    })
                    .build()
            )
            .build()
    }

    private fun playPause() {
        if (controller?.isPlaying == true) {
            controller?.pause()
        } else {
            controller?.play()
        }
    }

    private fun nextSong() {
        val stepFreq = run{
            val d = mService.previousStepFrequency_3.takeLastWhile { it > 50 }.takeLast(5)
            var l = d.average()
            if(l.isNaN()){
                l=0.0
            }
            l
        }
        Log.d("stepFreq before nextSong()", stepFreq.toString())
        val queueNext = queue.removeFirstOrNull()
        if(queueNext!=null){
            val media = buildMediaItem(queueNext)
            setSongInPlaylist(media)
            return
        }
        val list = audiolist.value.toMutableList()
        val l = list.let { viewModel.orderSongs(stepFreq, it) }
        while (true) {
            val nextSong = l.removeFirstOrNull()
            if (nextSong != null) {
                val media = buildMediaItem(nextSong)
                Log.d("IOOOO","From MainActivity, next song ${media.mediaId}?")
                Log.d("playlist before adding", playlist.toString())
                if (!playlist.contains(media.mediaId)) {
                    Log.d("IOOOO","From MainActivity, it does not contain ${media.mediaId}")
                    //Log.d("playlist contains media? ", playlist.contains(media.mediaId).toString())
                    Log.d("media", media.toString())
                    Log.d("playlist", playlist.toString())
                    setSongInPlaylist(media)
                    break
                }
            }
        }
    }

    private fun prevSong() {
        controller?.seekToPreviousMediaItem()
    }

    private fun incrementSpeed(value: Int) {
        if (value > 0)
            speed += 0.05f
        else
            speed -= 0.05f
        controller?.setPlaybackSpeed(speed)
    }

    private fun onProgress(seekTo: Float) {
        controller?.seekTo((controller?.duration?.times(seekTo.toLong()) ?: 0) / 100)
    }

}

