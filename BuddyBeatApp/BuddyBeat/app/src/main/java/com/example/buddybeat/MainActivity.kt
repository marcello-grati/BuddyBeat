package com.example.buddybeat

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.buddybeat.data.ContentResolverHelper
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.player.PlaybackService
import com.example.buddybeat.ui.MyViewModel
import com.example.buddybeat.ui.audio.MusicPlayerApp
import com.example.buddybeat.ui.theme.BuddyBeatTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    private val viewModel: MyViewModel by viewModels()

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() =
            if (controllerFuture.isDone && !controllerFuture.isCancelled) controllerFuture.get() else null

    private var job: Job? = Job()

    var speed : Float = 1f

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
            controller?.currentMediaItem?.let { viewModel.changeSong(it) }
            controller?.let { viewModel.updateDuration(it.duration) }
        }
    }


    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startService(Intent(this, SensorService::class.java))

        setContent {
            BuddyBeatTheme {
                val state =
                    rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
                when {
                    state.status.isGranted -> {
                        val isUploaded by viewModel.isUploaded.observeAsState()
                        val bpmUpdated by viewModel.bpmUpdated.observeAsState()
                        if(isUploaded == true && bpmUpdated==false) {
                            viewModel.updateBpm()
                        }
                        if(bpmUpdated == true){
                            Log.d("FINITOOOOOOOOO", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
                        }
                        MusicPlayerApp(
                            viewModel = viewModel,
                            onItemClick = {
                                setSong(it)
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
                            })
                    }
                    else -> RequiredPermission(state)
                }
            }
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

    @OptIn(DelicateCoroutinesApi::class)
    override fun onResume() {
        super.onResume()
        GlobalScope.launch(Dispatchers.Main){ startProgressUpdate() }
    }

    override fun onStop() {
        super.onStop()
        MediaController.releaseFuture(controllerFuture)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProgressUpdate()
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

    private fun setPlaylist() {
        viewModel.audioList.value?.forEach { audio ->
            val media = buildMediaItem(audio)
            controller?.addMediaItem(media)
        }

        Log.d("count", controller?.mediaItemCount.toString())
    }

    private fun setSong(index: Int) {
        if (controller?.mediaItemCount == 0)
            setPlaylist()
        when (index) {
            controller?.currentMediaItemIndex -> {
                playPause()
            }

            else -> {
                controller?.seekToDefaultPosition(index)
                controller?.let { viewModel.updateDuration(it.duration) }
                playPause()
            }
        }
    }

    private fun buildMediaItem(audio: Song): MediaItem {
        return MediaItem.Builder()
            .setMediaId(audio.uri)
            .setRequestMetadata(
                MediaItem.RequestMetadata.Builder()
                    .setMediaUri(audio.uri.toUri())
                    .build()
            )
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setAlbumArtist(audio.artist)
                    .setArtist(audio.artist)
                    .setTitle(audio.title)
                    .setDisplayTitle(audio.title)
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
        controller?.seekToDefaultPosition(controller?.currentMediaItemIndex?.plus(1) ?: 0)
    }

    private fun prevSong() {
        controller?.seekToPreviousMediaItem()
    }

    private fun incrementSpeed(value : Int){
        if(value > 0)
            speed += 0.05f
        else
            speed -= 0.05f
        controller?.setPlaybackSpeed(speed)
    }

    private fun onProgress(seekTo : Float){
        controller?.seekTo((controller?.duration?.times(seekTo.toLong()) ?: 0) /100)
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalPermissionsApi::class, DelicateCoroutinesApi::class)
    @Composable
    fun RequiredPermission(state: PermissionState) {
        Scaffold {
            DisposableEffect(state) {
                state.launchPermissionRequest()
                onDispose {
                    val list = ContentResolverHelper(applicationContext).getAudioData()
                    viewModel.update(list)
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
}

