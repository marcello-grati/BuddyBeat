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
import com.example.buddybeat.ui.components.Queue

object Destination {
    const val home = "home"
    const val playlist = "playlist"
    const val songScreen = "songScreen"
    const val queue = "queue"
}

@Composable
fun MusicPlayerApp(
    showPlayer : Boolean,
    changeShow : () -> Unit,
    viewModel: MyViewModel,
    onItemClick: (Int) -> Unit,
    prevSong: () -> Unit,
    nextSong: () -> Unit,
    onProgress: (Float) -> Unit,
    onStart: () -> Unit,
    incrementSpeed: () -> Unit,
    decrementSpeed: () -> Unit,
    toggleMode : () -> Unit,
    plus : () -> Unit,
    minus : () -> Unit,
    text3 : String
) {
    val navController = rememberNavController()
    MusicPlayerNavHost(
        showPlayer = showPlayer,
        changeShow = changeShow,
        navController = navController,
        viewModel = viewModel,
        onItemClick = onItemClick,
        onProgress = onProgress,
        onStart = onStart,
        nextSong = nextSong,
        prevSong = prevSong,
        incrementSpeed = incrementSpeed,
        decrementSpeed = decrementSpeed,
        toggleMode = toggleMode,
        text3 = text3,
        plus = plus,
        minus = minus
    )
}


@Composable
fun MusicPlayerNavHost(
    showPlayer: Boolean,
    changeShow : () -> Unit,
    navController: NavHostController,
    viewModel: MyViewModel,
    onItemClick: (Int) -> Unit,
    prevSong: () -> Unit,
    nextSong: () -> Unit,
    onProgress: (Float) -> Unit,
    onStart: () -> Unit,
    incrementSpeed: () -> Unit,
    decrementSpeed: () -> Unit,
    toggleMode : () -> Unit,
    plus : () -> Unit,
    minus : () -> Unit,
    text3 : String
) {

    val isLoading by viewModel.bpmUpdated.observeAsState(initial = false)
    val progressLoading by viewModel.progressLoading.collectAsState(initial = 0)
    val audioList by viewModel.visiblePlaylist.observeAsState(initial = listOf())
    val allSongs by viewModel.allSongs.observeAsState(initial = listOf())
    val allPlaylist by viewModel.allPlaylist.observeAsState(initial = listOf())
    val currentSong by viewModel.currentSong.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val count by viewModel.itemCount.observeAsState(initial = 1)
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val stepFreq by viewModel.stepFreq.collectAsState(0)
    val bpm by viewModel.currentBpm.collectAsState(0)
    val favorites by viewModel.favoritesId.observeAsState(initial = 0L)
    val currentId by viewModel.currentId.collectAsState()

    NavHost(navController = navController, startDestination = Destination.home) {
        composable(route = Destination.home) {
            HomeScreen(
                showPlayer = showPlayer,
                changeShow = changeShow,
                allSongs = allSongs,
                allSongsClicked = { navController.navigate(Destination.playlist) },
                isPlaying = isPlaying,
                audioList = audioList,
                allPlaylist = allPlaylist,
                onItemClick = {
                    onItemClick(it)
                },
                onBarClick = { navController.navigate(Destination.songScreen) },
                song = currentSong,
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
                setVisiblePlaylist = { id, songs ->
                    viewModel.setVisiblePlaylist(id, songs)
                },
                addToFavorite = {
                    viewModel.addToPlaylist(favorites, it)
                },
                favoriteContainsSong = {
                    viewModel.containsSong(favorites, it)
                },
                removeFavorite = {
                    viewModel.removeFromPlaylist(favorites, it)
                },
                currentId = currentId
            )
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
                        //change speed
                        incrementSpeed = incrementSpeed,
                        decrementSpeed = decrementSpeed,
                        //top bar
                        text1 = stepFreq.toString(),
                        text2 = bpm.toString(),
                        text3 = text3,
                        addToFavorite = {
                            viewModel.addToPlaylist(favorites, it)
                        },
                        favoriteContainsSong = {
                            viewModel.containsSong(favorites, it)
                        },
                        removeFavorite = {
                            viewModel.removeFromPlaylist(favorites, it)
                        },
                        currentId = currentId
                    ) { navController.navigateUp() }
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
                progress = progress / 100,
                onProgress = onProgress,
                //playpause
                onStart = onStart,
                //next prev
                nextSong = nextSong,
                prevSong = prevSong,
                duration = duration,
                toggleMode = toggleMode,
                plus = plus,
                minus = minus,
                step = stepFreq.toString(),
                bpm = bpm.toString(),
                ratio = text3,
                queue = { navController.navigate(Destination.queue) }
            )
        }
        composable(route = Destination.queue) {
            val queue by viewModel.getQueue().observeAsState(initial = listOf())
            Queue(
                audioList = queue,
                onNavigateUp = { navController.navigateUp() },
            )
        }
    }
}
