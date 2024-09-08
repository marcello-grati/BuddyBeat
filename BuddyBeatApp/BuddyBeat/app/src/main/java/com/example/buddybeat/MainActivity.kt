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
import java.io.FileNotFoundException

/*MainActivity that manages all the components (UI, Services)*/
@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    //viewModel
    private val viewModel: MyViewModel by viewModels()

    //controller media3
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() =
            if (controllerFuture.isDone && !controllerFuture.isCancelled) controllerFuture.get() else null

    //ratio
    private var _ratio: MutableStateFlow<Float> = MutableStateFlow(1f)
    private val ratio: StateFlow<Float> = _ratio.asStateFlow()

    //if notification is clicked it shows the player
    private var showPlayer = false
    private fun changeShow() {
        showPlayer = false
    }

    //player listener for controller media3
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
                "onMediaItemTransition with media:", mediaItem?.mediaMetadata?.title.toString()
            )
            controller?.currentMediaItem?.let { viewModel.changeSong(it) }
            controller?.let { viewModel.updateDuration(it.duration) }
            //setting ratio back to 1
            PlaybackService.ratio = 1.0f
            controller?.setPlaybackSpeed(1.0f)
        }
    }

    //Bind to SensorService
    private lateinit var mService: SensorService
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as SensorService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    //every second update text view with SPM taken from SensorService and with ratio from PlaybackService
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

    private fun updateDataTextView() {
        run {
            viewModel.updateFreq(if (System.currentTimeMillis() - mService.lastUpdate > 3000) 0
            else mService.previousStepFrequency.takeLast(7).takeWhile { it > 65 }.average()
                .toInt()
            )
            viewModel.updateFreqQueue(
                mService.previousStepFrequency.takeLast(7).takeWhile { it > 65 }.average()
            )
            _ratio.update { controller?.playbackParameters?.speed ?: 1f }
        }
    }

    //start SensorService
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startSensorService() {
        startForegroundService(Intent(this, SensorService::class.java))
        Intent(this, SensorService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        handler.postDelayed(sensorDataRunnable, interval) //start updating view every second
    }

    //onCreate()
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //if notification clicked
        val intent: String? = intent.extras?.getString("DESTINATION")
        if (intent == "player")
            showPlayer = true

        setContent {
            BuddyBeatTheme {
                //permissions
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
                    //if all permission are granted
                    state.allPermissionsGranted -> {

                        startSensorService()

                        //uploading song and calculation of BPMs
                        val isUploaded by viewModel.isUploaded.observeAsState()
                        val bpmUpdated by viewModel.bpmUpdated.observeAsState(initial = false)
                        val bpmCount by viewModel.bpmCount.observeAsState(initial = 0)
                        if (isUploaded == true && !bpmUpdated) {
                            Log.d("MainActivity", "Songs are uploaded but bpm not updated")
                            viewModel.updateBpm()
                        }
                        if (bpmUpdated) {
                            if (bpmCount > 0) //if there are still BPM to calculate
                                viewModel.setBpmUpdated(false)
                            else
                                Log.d("MainActivity", "BPMs are updated")
                        }
                        //managing state when reopening app
                        if (viewModel.mode.value != 0L) {
                            viewModel.mode.value?.let { viewModel.setMode(it) }
                        }
                        viewModel.setTargetBpm(manualBpm)

                        val ratioValue by ratio.collectAsState()
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
                            text3 = ratioValue.toString(),
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
                            })
                    }
                    //if permission not granted, it will ask
                    else -> RequiredPermission(state = state)
                }
            }
        }
    }

    //require permission
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun RequiredPermission(state: MultiplePermissionsState) {
        Scaffold {
            DisposableEffect(state) {
                state.launchMultiplePermissionRequest()
                onDispose {
                    //permission given, insert all songs
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

    //setting controller
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        val sessionToken =
            SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({ setController() }, ContextCompat.getMainExecutor(this))
    }

    private fun setController() {
        val controller = this.controller ?: return
        controller.addListener(listener)
        initController(controller)
    }

    private fun initController(controller: MediaController) {
        controller.currentMediaItem?.let { viewModel.changeSong(it) }
        controller.isPlaying.let { viewModel.updateIsPlaying(it) }
        controller.duration.let { viewModel.updateDuration(it) }
        controller.currentPosition.let { viewModel.updateProgress(it) }
        viewModel.modality.value?.let {
            speedMode = it
            if (it == MANUAL_MODE)
                ALPHA = 0.4f
            else if (it == AUTO_MODE)
                ALPHA = 0.8f
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onResume() {
        super.onResume()
        //progress update
        GlobalScope.launch(Dispatchers.Main) { startProgressUpdate() }
    }

    override fun onStop() {
        super.onStop()
        //release controller
        MediaController.releaseFuture(controllerFuture)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProgressUpdate()
        unbindService(connection)
        mBound = false
    }

    //progress update
    private var job: Job? = Job()
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

    //useful functions
    private fun addToQueue(song: Song) {
        queue.add(song)
    }

    //change mode (auto/manual)
    private fun toggleSpeedMode() {
        //if previous speed is off it will set the mode to the last mode saved (if any) or set walking mode
        if (speedMode == OFF_MODE) {
            if (viewModel.lastMode.value != -1L) {
                mService.changeMode(viewModel.lastMode.value)
                viewModel.setMode(viewModel.lastMode.value)
            } else {
                viewModel.setMode(1L)
                mService.changeMode(1L)
            }
        }
        //changing modality in PlaybackService and in viewModel
        speedMode = (speedMode + 1) % 3
        if (speedMode == OFF_MODE) {
            viewModel.setMode(0L)
            mService.changeMode(0L)
        }
        viewModel.setModality(speedMode)
        //changing ALPHA according to modality
        if (speedMode == MANUAL_MODE)
            ALPHA = 0.4F
        else if (speedMode == AUTO_MODE)
            ALPHA = 0.8f
        Log.d("MODALITY", speedMode.toString())
    }

    //increase target Bpm in PlaybackService and in viewModel
    private fun increaseManualBpm() {
        var bpm = viewModel.targetBpm.value + BPM_STEP
        bpm = bpm.coerceAtMost(230)
        viewModel.setTargetBpm(bpm)
        manualBpm = bpm
    }

    //decrease target Bpm in PlaybackService and in viewModel
    private fun decreaseManualBpm() {
        var bpm = viewModel.targetBpm.value - BPM_STEP
        bpm = bpm.coerceAtLeast(70)
        viewModel.setTargetBpm(bpm)
        manualBpm = bpm
    }

    private fun setSongInPlaylist(media: MediaItem) {
        Log.d("Setting media:", media.mediaMetadata.title.toString() + ", " + media.mediaId)
        controller?.addMediaItem(controller!!.currentMediaItemIndex + 1, media)
        controller?.seekToDefaultPosition(controller!!.currentMediaItemIndex + 1)
    }


    private fun buildMediaItem(audio: Song): MediaItem? {
        //check if audio file exists before creating mediaItem otherwise return null
        try {
            contentResolver.openInputStream(audio.uri.toUri())?.close()
        } catch (e: FileNotFoundException) {
            Log.d("buildMediaItem", e.toString())
            viewModel.removeFromPlaylist(viewModel.allSongsId.value!!, audio.songId)
            audiolist.remove(audio)
            playlist.remove(audio.uri)
            queue.remove(audio)
            return null
        }
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

    private fun prevSong() {
        if (controller?.currentPosition!! > 1000L)
            controller?.seekToDefaultPosition()
        else controller?.seekToPreviousMediaItem()
    }

    private fun onProgress(seekTo: Float) {
        controller?.seekTo((controller?.duration?.times(seekTo.toLong()) ?: 0) / 100)
    }

    private fun nextSong() {
        //search for songs in queue (inserted by user)
        while (true) {
            val queueNext = queue.removeFirstOrNull()
            if (queueNext != null) {
                val media = buildMediaItem(queueNext)
                if (media != null) {
                    setSongInPlaylist(media)
                    return
                }
            } else break
        }
        //get target Bpm according to the modality
        val target = when (speedMode) {
            AUTO_MODE -> run {
                //take the SPM from the Service making an average of the last values
                val d = mService.previousStepFrequency.takeLast(7).takeWhile { it > 65 }
                var l = d.average()
                if (l.isNaN()) {
                    l = 0.0
                }
                l
            }
            MANUAL_MODE -> manualBpm.toDouble()
            OFF_MODE -> 0.0
            else -> throw Exception("Invalid speed mode")
        }
        Log.d("SPM for choosing next Song", target.toString())
        //orderSongs according to targetBpm
        val l = if (target != 0.0) viewModel.orderSongs(
            target,
            audiolist
        ) else audiolist.toMutableList()
        //removing songs without bpm
        if (viewModel.modality.value != OFF_MODE)
            l.removeAll { it.bpm == -1 || it.bpm == 0 }
        //insert most fitting song in queue
        while (true) {
            val nextSong = l.removeFirstOrNull()
            if (nextSong != null) {
                val media = buildMediaItem(nextSong)
                if (media != null) {
                    //check if already played recently
                    if (!playlist.contains(media.mediaId)) {
                        setSongInPlaylist(media)
                        break
                    }
                }
            } else return
        }
    }
}