package com.example.buddybeat.ui.screens

import HelpScreen
import PlayScreenDesign
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.MediaItem
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.buddybeat.data.models.Playlist
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.player.PlaybackService.Companion.audioListId
import com.example.buddybeat.player.PlaybackService.Companion.audiolist
import com.example.buddybeat.player.PlaybackService.Companion.manualBpm
import com.example.buddybeat.ui.MyViewModel
import com.example.buddybeat.ui.components.AddToPlaylist
import com.example.buddybeat.ui.components.AreYouSure
import com.example.buddybeat.ui.components.NewPlaylist
import com.example.buddybeat.ui.components.Queue
import com.example.buddybeat.ui.components.RenamePlaylist
import kotlinx.coroutines.flow.update

object Destination {
    const val home = "home"
    const val playlist = "playlist"
    const val songScreen = "songScreen"
    const val queue = "queue"
    const val help = "help"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MusicPlayerApp(
    showPlayer: Boolean,
    changeShow: () -> Unit,
    viewModel: MyViewModel,
    prevSong: () -> Unit,
    nextSong: () -> Unit,
    onProgress: (Float) -> Unit,
    onStart: () -> Unit,
    toggleMode: () -> Unit,
    plus: () -> Unit,
    minus: () -> Unit,
    text3: String,
    addToQueue: (Song) -> Unit,
    playPause: () -> Unit,
    buildMediaItem: (Song) -> MediaItem,
    setSongInPlaylist: (MediaItem) -> Unit,
    setMode: (Long) -> Unit
) {
    val navController = rememberNavController()
    MusicPlayerNavHost(
        showPlayer = showPlayer,
        changeShow = changeShow,
        navController = navController,
        viewModel = viewModel,
        onProgress = onProgress,
        onStart = onStart,
        nextSong = nextSong,
        prevSong = prevSong,
        toggleMode = toggleMode,
        text3 = text3,
        plus = plus,
        minus = minus,
        addToQueue = addToQueue,
        playPause = playPause,
        buildMediaItem = buildMediaItem,
        setSongInPlaylist = setSongInPlaylist,
        setMode = setMode
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerNavHost(
    showPlayer: Boolean,
    changeShow: () -> Unit,
    navController: NavHostController,
    viewModel: MyViewModel,
    prevSong: () -> Unit,
    nextSong: () -> Unit,
    onProgress: (Float) -> Unit,
    onStart: () -> Unit,
    toggleMode: () -> Unit,
    plus: () -> Unit,
    minus: () -> Unit,
    text3: String,
    addToQueue: (Song) -> Unit,
    playPause: () -> Unit,
    buildMediaItem: (Song) -> MediaItem,
    setSongInPlaylist: (MediaItem) -> Unit,
    setMode: (Long) -> Unit
) {

    val isLoading by viewModel.bpmUpdated.observeAsState(initial = false)
    val progressLoading by viewModel.progressLoading.collectAsState(initial = 0)
    val allPlaylist by viewModel.allPlaylist.observeAsState(initial = listOf())
    val currentSong by viewModel.currentSong.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val count by viewModel.itemCount.observeAsState(initial = 1)
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val stepFreq by viewModel.stepFreq.collectAsState(0)
    //val bpm by viewModel.currentBpm.collectAsState(0)

    val favorites by viewModel.favoritesId.observeAsState(initial = 0L)
    val allSongs by viewModel.allSongsId.observeAsState(initial = 0L)

    val currentId = remember { mutableLongStateOf(0L) }

    val shouldShowDialogOne = remember { mutableStateOf(false) }
    val shouldShowDialogTwo = remember { mutableStateOf(false) }
    val shouldShowDialogThree = remember { mutableStateOf(false) }
    val shouldShowDialogFour = remember { mutableStateOf(false) }
    val shouldShowDialogFive = remember { mutableStateOf(false) }
    val showBottomSheet = remember { mutableStateOf(false) }
    val songClicked = remember { mutableLongStateOf(-1L) }
    val playlistLongClicked = remember { mutableStateOf(Playlist(title = "", description = "")) }

    // walking/running
    val mode by viewModel.mode.observeAsState(initial = 0L)
    val modality by viewModel.modality.observeAsState(initial = 0L)
    val closedHelp by viewModel.help.observeAsState(initial = false)

    val targetBpm by viewModel.targetBpm.collectAsState(100)

    val queue = viewModel.queueList1
    val audioList = viewModel.queueList2



    val colorUI = when (mode) {
        1L -> Color(0xFFB1B2FF)
        2L -> Color(0xFFD0EB34)
        else -> Color(0xFFBEEBF2)
    }


    if (shouldShowDialogOne.value) {
        NewPlaylist(shouldShowDialog = shouldShowDialogOne,
            insertPlaylist = { viewModel.insertPlaylist(Playlist(title = it, description = it)) })
    }

    if (shouldShowDialogTwo.value) {
        Log.d("idsong", songClicked.longValue.toString())
        AddToPlaylist(shouldShowDialogTwo = shouldShowDialogTwo,
            shouldShowDialogOne = shouldShowDialogOne,
            allPlaylist = allPlaylist,
            insertPlaylist = {
                viewModel.addToPlaylist(it, songClicked.longValue)
            })
    }

    if (shouldShowDialogThree.value) {
        AreYouSure(
            shouldShowDialog = shouldShowDialogThree,
            onClick = {
                viewModel.removeFromPlaylist(currentId.longValue, songClicked.longValue)
            },
            title = "Remove from playlist",
            text = "Do you want to remove the song from the playlist?"
        )
    }

    if (shouldShowDialogFour.value) {
        AreYouSure(shouldShowDialog = shouldShowDialogFour, onClick = {
            viewModel.removePlaylist(playlistLongClicked.value)
            showBottomSheet.value = false
        }, title = "Remove playlist", text = "Do you want to remove the playlist?")
    }

    if (shouldShowDialogFive.value) {
        RenamePlaylist(shouldShowDialogFive = shouldShowDialogFive, updatePlaylist = {
            viewModel.updatePlaylist(it, playlistLongClicked.value.playlistId)
            showBottomSheet.value = false
        }, title = playlistLongClicked.value.title)
    }

    if (showBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet.value = false
            }
        ) {
            ListItem(
                headlineContent = { Text("Rename playlist") },
                leadingContent = { Icon(Icons.Default.Edit, null) },
                modifier = Modifier.clickable {
                    shouldShowDialogFive.value = true
                    showBottomSheet.value = false
                }
            )
            ListItem(
                headlineContent = { Text("Delete Playlist") },
                leadingContent = { Icon(Icons.Default.PlaylistRemove, null) },
                modifier = Modifier.clickable {
                    shouldShowDialogFour.value = true
                    showBottomSheet.value = false
                }
            )
        }
    }



    NavHost(navController = navController, startDestination = Destination.home) {
        composable(route = Destination.home) {
            HomeScreen(
                showPlayer = showPlayer,
                changeShow = changeShow,
                playlistClicked = {
                    viewModel.setVisiblePlaylist(it)
                    currentId.longValue = it.playlist.playlistId
                    playlistLongClicked.value = it.playlist
                    navController.navigate(Destination.playlist)
                },
                isPlaying = isPlaying,
                allPlaylist = allPlaylist,
                onBarClick = { navController.navigate(Destination.songScreen) },
                song = currentSong,
                progress = progress,
                onStart = onStart,
                nextSong = nextSong,
                prevSong = prevSong,
                                addToFavorite = {
                    viewModel.addToPlaylist(favorites, it)
                },
                favoriteContainsSong = {
                    viewModel.containsSong(favorites, it)
                },
                removeFavorite = {
                    viewModel.removeFromPlaylist(favorites, it)
                },
                shouldShowDialogOne = shouldShowDialogOne,
                showBottomSheet = showBottomSheet,
                playlistLongClicked = playlistLongClicked,
                updateSongs = {
                    viewModel.updateSongs()
                },
                allSongsId = allSongs,
                favoritesId = favorites,
                setModality = {
                    viewModel.setModality(it)
                },
                changeMode = {
                    setMode(it)
                },
                mode = mode,
                colorUI = colorUI,
                changeShowHelp = {
                    viewModel.changeShowHelp(it)
                },
                closedHelp = closedHelp,
                setTargetBpm = {
                    viewModel.setTargetBpm(it)
                },
                shouldShowHelpScreen = { navController.navigate(Destination.help) }
            )
        }
        composable(route = Destination.help) {
                HelpScreen() {
                    navController.navigateUp()
                }
        }
        composable(route = Destination.playlist) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column {
                    PlaylistScreen(
                        onItemClick = { index ->
                            Log.d("IOOOO", "index clicked: $index")
                            if (audioListId.value != currentId.longValue) {
                                audioListId.update {
                                    currentId.longValue
                                }
                                audiolist.clear()
                                audiolist.addAll(
                                    allPlaylist.find { it.playlist.playlistId == currentId.longValue }?.songs
                                        ?: mutableListOf()
                                )
                                Log.d("AUDIOLIST", audiolist.toList().toString())
                            } else {
                                val play =
                                    allPlaylist.find { it.playlist.playlistId == currentId.longValue }?.songs
                                        ?: mutableListOf()
                                audiolist.replaceAll { it1 ->
                                    if (it1.bpm == -1) play.find { it.songId == it1.songId }
                                        ?: it1 else it1
                                }
                                Log.d("AUDIOLIST1", audiolist.toList().toString())
                            }
                            val song =
                                allPlaylist.find { it.playlist.playlistId == currentId.longValue }?.songs!!.sortedBy { it.title }[index]
                            Log.d("IOOOO", "Song clicked: $song")
                            if (song.songId == currentSong.id) {
                                Log.d("IOOOO", "same id: ${song.songId}")
                                playPause()
                            } else {
                                Log.d("IOOOO", "not same id, new id: ${song.songId}")
                                val media = buildMediaItem(song)
                                setSongInPlaylist(media)
                                playPause()
                            }
                        },
                        // indicator of progress of bpm calculation
                        loading = isLoading,
                        currentProgress = (progressLoading.toFloat().div(count.toFloat())),
                        //current Song
                        onBarClick = { navController.navigate(Destination.songScreen) },
                        song = currentSong,
                        isPlaying = isPlaying,
                        progress = progress,
                        //playpause
                        onStart = onStart,
                        //next prev
                        nextSong = nextSong,
                        prevSong = prevSong,
                        //top bar
                        text1 = stepFreq.toString(),
                        text2 = currentSong.bpm.toString(),
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
                        shouldShowDialogTwo = shouldShowDialogTwo,
                        songClicked = songClicked,
                        shouldShowDialogThree = shouldShowDialogThree,
                        allPlaylist = allPlaylist,
                        currentId = currentId.longValue,
                        addToQueue = addToQueue,
                        shouldShowDialogFive = shouldShowDialogFive,
                        shouldShowDialogFour = shouldShowDialogFour,
                        allSongsId = allSongs,
                        favoritesId = favorites,
                        colorUI = colorUI,
                        startFirstSong = {
                            if (audioListId.value != currentId.longValue) {
                                audioListId.update {
                                    currentId.longValue
                                }
                                audiolist.clear()
                                audiolist.addAll(
                                    allPlaylist.find { it.playlist.playlistId == currentId.longValue }?.songs
                                        ?: mutableListOf()
                                )
                                Log.d("AUDIOLIST", audiolist.toList().toString())
                            } else {
                                val play =
                                    allPlaylist.find { it.playlist.playlistId == currentId.longValue }?.songs
                                        ?: mutableListOf()
                                audiolist.replaceAll { it1 ->
                                    if (it1.bpm == -1) play.find { it.songId == it1.songId }
                                        ?: it1 else it1
                                }
                                Log.d("AUDIOLIST1", audiolist.toList().toString())
                            }
                            nextSong()
                            playPause()
                        },
                        mode = mode
                    ) { navController.navigateUp() }
                }
            }
        }
        composable(route = Destination.songScreen) {
            PlayScreenDesign(
                onNavigateUp = { navController.navigateUp() },
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
                bpm = currentSong.bpm.toString(),
                queue = {
                    viewModel.showQueue(true)
                    navController.navigate(Destination.queue)
                },
                target = targetBpm.toString(),
                modality = modality,
                addToFavorite = {
                    viewModel.addToPlaylist(favorites, it)
                },
                favoriteContainsSong = {
                    viewModel.containsSong(favorites, it)
                },
                removeFavorite = {
                    viewModel.removeFromPlaylist(favorites, it)
                },
                colorUI = colorUI,
                shouldShowHelpScreen = { navController.navigate(Destination.help) }
            )
        }
        composable(route = Destination.queue,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(1000)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(1000)
                )
            }) {
            Queue(
                queue = queue,
                audioList = audioList,
                removeFromQueue1 = {
                    viewModel.removeFromQueue1(it)
                },
                removeFromQueue2 = {
                    viewModel.removeFromQueue2(it)
                },
                onNavigateUp = {
                    viewModel.showQueue(false)
                    navController.navigateUp()
                },
                showQueue = {
                    viewModel.showQueue(it)
                },
                currentSong = currentSong
            )
        }
    }
}
