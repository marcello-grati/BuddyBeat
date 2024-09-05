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
import com.example.buddybeat.player.PlaybackService.Companion.ALPHA
import com.example.buddybeat.player.PlaybackService.Companion.AUTO_MODE
import com.example.buddybeat.player.PlaybackService.Companion.BPM_STEP
import com.example.buddybeat.player.PlaybackService.Companion.MANUAL_MODE
import com.example.buddybeat.player.PlaybackService.Companion.OFF_MODE
import com.example.buddybeat.player.PlaybackService.Companion.audiolist
import com.example.buddybeat.player.PlaybackService.Companion.manualBpm
import com.example.buddybeat.player.PlaybackService.Companion.playlist
import com.example.buddybeat.player.PlaybackService.Companion.queue
import com.example.buddybeat.player.PlaybackService.Companion.speedMode
import com.example.buddybeat.ui.MyViewModel
import com.example.buddybeat.ui.screens.MusicPlayerApp
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
            controller?.setPlaybackSpeed(1.0f)
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
            //viewModel.mode.value?.let{ mService.changeMode(it) }
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
        if(speedMode == OFF_MODE){
            if(viewModel.lastMode.value!=-1L) {
                mService.changeMode(viewModel.lastMode.value)
                viewModel.setMode(viewModel.lastMode.value)
            }else{
                viewModel.setMode(1L)
                mService.changeMode(1L)
            }
        }
        speedMode = (speedMode + 1) % 3
        if(speedMode == OFF_MODE){
            viewModel.setMode(0L)
            mService.changeMode(0L)
        }
        viewModel.setModality(speedMode)
        if(speedMode == MANUAL_MODE || speedMode == OFF_MODE)
            ALPHA = 0.4F
        else if (speedMode == AUTO_MODE)
            ALPHA = 0.7f

        Log.d("MODALITY", speedMode.toString())
    }

    private fun increaseManualBpm() {
        manualBpm += BPM_STEP
    }
    private fun decreaseManualBpm() {
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
                        if(viewModel.mode.value!=0L){
                            viewModel.mode.value?.let { viewModel.setMode(it) }
                        }
                        MusicPlayerApp(
                            showPlayer = showPlayer,
                            changeShow = {
                                changeShow()
                            },
                            viewModel = viewModel,
                            nextSong = {
                                nextSong()
                            },
                            onStart = {
                                playPause()
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
                                mService.changeMode(it)
                            }
                        )
                    }
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
                    viewModel.insertAllSongs()
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

    private fun updateDataTextView() {
        run {
            viewModel.updateFreq(mService.stepFreq)
            viewModel.updateFreqQueue( mService.previousStepFrequency_3.takeLast(5).average())
            _ratio.update { controller?.playbackParameters?.speed ?: 1f }
        }
    }


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
        viewModel.modality.value?.let { Log.d("modality value", it.toString())
            Log.d("modality value", speedMode.toString())
            speedMode = it
            if(it == MANUAL_MODE || it == OFF_MODE)
                ALPHA = 0.4f
            else if (it == AUTO_MODE)
                ALPHA = 0.7f
        }
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
        val target = when (speedMode) {
            AUTO_MODE -> run{
                val d = mService.previousStepFrequency_3.takeLast(5)
                Log.d("PreviousFreq nextSong mainActivity", d.toString())
                var l = d.average()
                if(l.isNaN()){
                    l=0.0
                }
                l
            }
            MANUAL_MODE -> manualBpm.toDouble()
            OFF_MODE -> 0.0
            else -> throw Exception("Invalid speed mode")
        }
        Log.d("stepFreq before nextSong()", target.toString())
        val queueNext = queue.removeFirstOrNull()
        if(queueNext!=null){
            val media = buildMediaItem(queueNext)
            setSongInPlaylist(media)
            return
        }
        val l = if (target!=0.0) viewModel.orderSongs(target, audiolist) else audiolist.toMutableList()
        if(viewModel.modality.value!= OFF_MODE)
            l.removeAll { it.bpm == -1 || it.bpm == 0 }
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
            }else return
        }
    }

    private fun prevSong() {
        if(controller?.currentPosition!! > 1000L)
            controller?.seekToDefaultPosition()
        else controller?.seekToPreviousMediaItem()
    }

    private fun onProgress(seekTo: Float) {
        controller?.seekTo((controller?.duration?.times(seekTo.toLong()) ?: 0) / 100)
    }

}

