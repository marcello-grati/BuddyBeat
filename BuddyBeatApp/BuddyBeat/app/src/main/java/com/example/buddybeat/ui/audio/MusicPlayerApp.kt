package com.example.buddybeat.ui.audio

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
import com.example.buddybeat.ui.components.Queue
import kotlinx.coroutines.flow.update

object Destination {
    const val home = "home"
    const val playlist = "playlist"
    const val songScreen = "songScreen"
    const val queue = "queue"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MusicPlayerApp(
    showPlayer: Boolean,
    changeShow: () -> Unit,
    viewModel: MyViewModel,
    onItemClick: (Int) -> Unit,
    prevSong: () -> Unit,
    nextSong: () -> Unit,
    onProgress: (Float) -> Unit,
    onStart: () -> Unit,
    incrementSpeed: () -> Unit,
    decrementSpeed: () -> Unit,
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
    onItemClick: (Int) -> Unit,
    prevSong: () -> Unit,
    nextSong: () -> Unit,
    onProgress: (Float) -> Unit,
    onStart: () -> Unit,
    incrementSpeed: () -> Unit,
    decrementSpeed: () -> Unit,
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

    val queue = viewModel.queueList1
    val audioList = viewModel.queueList2

    if (shouldShowDialogOne.value) {
        DialogOne(shouldShowDialog = shouldShowDialogOne,
            insertPlaylist = { viewModel.insertPlaylist(Playlist(title = it, description = it)) })
    }

    if (shouldShowDialogTwo.value) {
        Log.d("idsong", songClicked.longValue.toString())
        DialogTwo(shouldShowDialogTwo = shouldShowDialogTwo,
            shouldShowDialogOne = shouldShowDialogOne,
            allPlaylist = allPlaylist,
            insertPlaylist = {
                viewModel.addToPlaylist(it, songClicked.longValue)
            })
    }

    if (shouldShowDialogThree.value) {
        DialogThree(shouldShowDialogThree = shouldShowDialogThree, removeSong = {
            viewModel.removeFromPlaylist(currentId.longValue, songClicked.longValue)
        })
    }

    if (shouldShowDialogFour.value) {
        DialogFour(shouldShowDialogFour = shouldShowDialogFour, removePlaylist = {
            viewModel.removePlaylist(playlistLongClicked.value)
            showBottomSheet.value = false
        })
    }

    if (shouldShowDialogFive.value) {
        DialogFive(shouldShowDialogFive = shouldShowDialogFive, updatePlaylist = {
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
                audioList = listOf(),
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
                //change speed
                incrementSpeed = incrementSpeed,
                decrementSpeed = decrementSpeed,
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
                shouldShowDialogOne = shouldShowDialogOne,
                songClicked = songClicked,
                shouldShowDialogThree = shouldShowDialogThree,
                addToQueue = addToQueue,
                showBottomSheet = showBottomSheet,
                playlistLongClicked = playlistLongClicked,
                updateSongs = {
                    viewModel.updateSongs()
                },
                allSongsId = allSongs,
                setModality = {
                    viewModel.setModality(it)
                },
                changeMode = {
                    setMode(it)
                },
                mode = mode
            )
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
                                Log.d("AUDIOLIST" , audiolist.toList().toString())
                            } else {
                                val play = allPlaylist.find { it.playlist.playlistId == currentId.longValue }?.songs
                                    ?: mutableListOf()
                                audiolist.replaceAll {it1 -> if (it1.bpm==-1) play.find { it.songId==it1.songId }?: it1 else it1}
                                Log.d("AUDIOLIST1" , audiolist.toList().toString())
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
                        favoritesId = favorites
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
                step = stepFreq.toString(),
                bpm = currentSong.bpm.toString(),
                ratio = text3,
                queue = {
                    viewModel.showQueue(true)
                    navController.navigate(Destination.queue)
                },
                target = manualBpm.toString(),
                //speedMode = speedMode,
                modality = modality,
                addToFavorite = {
                    viewModel.addToPlaylist(favorites, it)
                },
                favoriteContainsSong = {
                    viewModel.containsSong(favorites, it)
                },
                removeFavorite = {
                    viewModel.removeFromPlaylist(favorites, it)
                }
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
