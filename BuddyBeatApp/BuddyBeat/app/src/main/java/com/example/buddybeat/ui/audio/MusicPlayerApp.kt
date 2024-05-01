package com.example.buddybeat.ui.audio

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.buddybeat.ui.MyViewModel
import com.example.buddybeat.ui.components.HomeBottomBar

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
    decrementSpeed: () -> Unit
) {

    NavHost(navController = navController, startDestination = Destination.home) {
        composable(route = Destination.home) {
            val isInitialized = rememberSaveable { mutableStateOf(false) }

            if (!isInitialized.value) {
                LaunchedEffect(key1 = Unit) {
                    isInitialized.value = true
                }
            }
            val audioList by viewModel.audioList.observeAsState(initial = listOf())
            val currentSong by viewModel.currentSong.collectAsState()
            val isPlaying by viewModel.isPlaying.collectAsState()
            val progress by viewModel.progress.collectAsState()
            Box(modifier = Modifier.fillMaxSize()) {
                HomeScreen(
                    audioList = audioList,
                    onItemClick = {
                        onItemClick(it)
                    }
                )
                if (isInitialized.value) {
                    HomeBottomBar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter),
                        song = currentSong,
                        onBarClick = { navController.navigate(Destination.songScreen) },
                        isPlaying = isPlaying,
                        progress = progress,
                        onProgress = onProgress,
                        onStart = onStart,
                        nextSong = nextSong,
                        prevSong = prevSong,
                        incrementSpeed = incrementSpeed,
                        decrementSpeed = decrementSpeed
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