package com.example.buddybeat.ui.audio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.buddybeat.ui.MyViewModel

object Destination {
    const val home = "home"
    const val playlist = "playlist"
    const val songScreen = "songScreen"
}

@Composable
fun MusicPlayerApp(
    viewModel: MyViewModel,
    onItemClick: (Int) -> Unit,
    prevSong: () -> Unit,
    nextSong: () -> Unit,
    onProgress: (Float) -> Unit,
    onStart: () -> Unit,
    incrementSpeed: () -> Unit,
    decrementSpeed: () -> Unit,
    text3 : String
) {
    val navController = rememberNavController()
    MusicPlayerNavHost(
        navController = navController,
        viewModel = viewModel,
        onItemClick = onItemClick,
        onProgress = onProgress,
        onStart = onStart,
        nextSong = nextSong,
        prevSong = prevSong,
        incrementSpeed = incrementSpeed,
        decrementSpeed = decrementSpeed,
        text3 = text3
    )
}


@Composable
fun MusicPlayerNavHost(
    navController: NavHostController,
    viewModel: MyViewModel,
    onItemClick: (Int) -> Unit,
    prevSong: () -> Unit,
    nextSong: () -> Unit,
    onProgress: (Float) -> Unit,
    onStart: () -> Unit,
    incrementSpeed: () -> Unit,
    decrementSpeed: () -> Unit,
    text3 : String
) {
    val isLoading by viewModel.bpmUpdated.observeAsState(initial = false)
    val progressLoading by viewModel.progressLoading.collectAsState(initial = 0)
    val audioList by viewModel.audioList.observeAsState(initial = listOf())
    val currentSong by viewModel.currentSong.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val count by viewModel.itemCount.observeAsState(initial = 1)
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val stepFreq by viewModel.stepFreq.collectAsState(0)
    val bpm by viewModel.currentBpm.collectAsState(0)

    NavHost(navController = navController, startDestination = Destination.home) {
        composable(route = Destination.home){
            HomeScreen(allSongsClicked = {navController.navigate(Destination.playlist)})
        }
        composable(route = Destination.playlist) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column {
                    PlaylistScreen(
                        audioList = audioList,
                        onItemClick = {
                            onItemClick(it)
                        },
                        // indicator of progress of bpm calculation
                        loading = isLoading,
                        currentProgress = (progressLoading.toFloat().div(count.toFloat())),
                        modifier = Modifier,
                        //current Song
                        onBarClick = { navController.navigate(Destination.songScreen) },
                        song = currentSong,
                        isPlaying = isPlaying,
                        progress = progress,
                        onProgress = onProgress,
                        //playpause
                        onStart = onStart,
                        //next prev
                        nextSong = nextSong,
                        prevSong = prevSong,
                        //change spped
                        incrementSpeed = incrementSpeed,
                        decrementSpeed = decrementSpeed,
                        //top bar
                        text1 = stepFreq.toString(),
                        text2 = bpm.toString(),
                        text3 = text3,
                        onNavigateUp = { navController.navigateUp() }
                    )
                }
            }
        }

        composable(route = Destination.songScreen) {
            PlayScreenDesign(
                onNavigateUp = { navController.navigateUp() },
                //filePath = "",
                //songName = currentSong.title,
                //artist = currentSong.artist,
                //duration = duration,
                //currentTime = progress,
                //isPlaying = isPlaying,
                song = currentSong,
                isPlaying = isPlaying,
                progress = progress/100,
                onProgress = onProgress,
                //playpause
                onStart = onStart,
                //next prev
                nextSong = nextSong,
                prevSong = prevSong,
                duration = duration,
            )
        }
    }
}