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
    decrementSpeed: () -> Unit
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
        decrementSpeed = decrementSpeed
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
) {

    NavHost(navController = navController, startDestination = Destination.home) {
        composable(route = Destination.home) {
            val isLoading by viewModel.bpmUpdated.observeAsState(initial = false)
            val progressLoading by viewModel.progressLoading.collectAsState(initial = 0)
            val audioList by viewModel.audioList.observeAsState(initial = listOf())
            val currentSong by viewModel.currentSong.collectAsState()
            val count by viewModel.itemCount.observeAsState(initial = 1)
            val isPlaying by viewModel.isPlaying.collectAsState()
            val progress by viewModel.progress.collectAsState()
            val text by viewModel.stepFreq.collectAsState(0)
            Box(modifier = Modifier.fillMaxSize()) {
                Column {
                    HomeScreen(
                        audioList = audioList,
                        onItemClick = {
                            onItemClick(it)
                        },
                        loading = isLoading,
                        currentProgress = (progressLoading.toFloat().div(count.toFloat())),
                        modifier = Modifier,
                        song = currentSong,
                        onBarClick = { navController.navigate(Destination.songScreen) },
                        isPlaying = isPlaying,
                        progress = progress,
                        onProgress = onProgress,
                        onStart = onStart,
                        nextSong = nextSong,
                        prevSong = prevSong,
                        incrementSpeed = incrementSpeed,
                        decrementSpeed = decrementSpeed,
                        text = text.toString()
                    )
                }
            }
        }

        composable(route = Destination.songScreen) {
            SongScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}