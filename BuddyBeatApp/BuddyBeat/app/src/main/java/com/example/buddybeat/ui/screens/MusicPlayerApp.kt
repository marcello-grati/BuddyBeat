package com.example.buddybeat.ui.screens

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
import com.example.buddybeat.ui.MyViewModel
import com.example.buddybeat.ui.components.AddToPlaylist
import com.example.buddybeat.ui.components.AreYouSure
import com.example.buddybeat.ui.components.NewPlaylist
import com.example.buddybeat.ui.components.Queue
import com.example.buddybeat.ui.components.RenamePlaylist
import kotlinx.coroutines.flow.update

/*Possible screens*/
object Destination {
    const val home = "home"
    const val playlist = "playlist"
    const val songScreen = "songScreen"
    const val queue = "queue"
    const val help = "help"
}

/*Main Composable that keeps track of the navigation*/
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
    buildMediaItem: (Song) -> MediaItem?,
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
    buildMediaItem: (Song) -> MediaItem?,
    setSongInPlaylist: (MediaItem) -> Unit,
    setMode: (Long) -> Unit
) {

    //variables for showing calculation bpm
    val isLoading by viewModel.bpmUpdated.observeAsState(initial = false)
    val progressLoading by viewModel.progressLoading.collectAsState(initial = 0)
    val count by viewModel.itemCount.observeAsState(initial = 1)

    //all playlist
    val allPlaylist by viewModel.allPlaylist.observeAsState(initial = listOf())

    //id known playlists
    val favorites by viewModel.favoritesId.observeAsState(initial = 0L)
    val allSongs by viewModel.allSongsId.observeAsState(initial = 0L)

    //current song
    val currentSong by viewModel.currentSong.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()

    //id playlist clicked
    val currentId = remember { mutableLongStateOf(0L) }

    //songClicked when showing options
    val songClicked = remember { mutableLongStateOf(-1L) }

    //playlist longClicked for options
    val playlistLongClicked = remember { mutableStateOf(Playlist(title = "", description = "")) }

    //variables for managing dialogs
    val shouldShowDialogOne = remember { mutableStateOf(false) }
    val shouldShowDialogTwo = remember { mutableStateOf(false) }
    val shouldShowDialogThree = remember { mutableStateOf(false) }
    val shouldShowDialogFour = remember { mutableStateOf(false) }
    val shouldShowDialogFive = remember { mutableStateOf(false) }
    val showBottomSheet = remember { mutableStateOf(false) }

    //show HelpSection
    val closedHelp by viewModel.help.observeAsState(initial = false)

    //list of songs for queue
    val queue = viewModel.queueList1 //inserted by user
    val audioList = viewModel.queueList2 //selected by system

    // off/walking/running
    val mode by viewModel.mode.observeAsState(initial = 0L)
    // off/auto/manual
    val modality by viewModel.modality.observeAsState(initial = 0L)
    //stepFreq
    val stepFreq by viewModel.stepFreq.collectAsState(0)
    //manualBpm
    val targetBpm by viewModel.targetBpm.collectAsState(100)

    //colorUI according to mode
    val colorUI = when (mode) {
        1L -> Color(0xFFB1B2FF)
        2L -> Color(0xFFD0EB34)
        else -> Color(0xFFBEEBF2)
    }

    //DIALOGS
    if (shouldShowDialogOne.value) {
        NewPlaylist(shouldShowDialog = shouldShowDialogOne,
            insertPlaylist = { viewModel.insertPlaylist(Playlist(title = it, description = it)) })
    }
    if (shouldShowDialogTwo.value) {
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
        //HOMESCREEN
        composable(route = Destination.home) {
            HomeScreen(
                //showing player
                showPlayer = showPlayer,
                changeShow = changeShow,
                //variables for displaying playlists
                allPlaylist = allPlaylist,
                allSongsId = allSongs,
                favoritesId = favorites,
                //click playlist
                playlistClicked = {
                    viewModel.setVisiblePlaylist(it)
                    currentId.longValue = it.playlist.playlistId
                    playlistLongClicked.value = it.playlist
                    navController.navigate(Destination.playlist)
                },
                //bottomBar
                isPlaying = isPlaying,
                onBarClick = { navController.navigate(Destination.songScreen) },
                song = currentSong,
                progress = progress,
                onStart = onStart,
                nextSong = nextSong,
                prevSong = prevSong,
                //managing favorites functionality
                addToFavorite = {
                    viewModel.addToPlaylist(favorites, it)
                },
                favoriteContainsSong = {
                    viewModel.containsSong(favorites, it)
                },
                removeFavorite = {
                    viewModel.removeFromPlaylist(favorites, it)
                },
                //dialogs
                shouldShowDialogOne = shouldShowDialogOne,
                showBottomSheet = showBottomSheet,
                playlistLongClicked = playlistLongClicked,
                //update songs
                updateSongs = {
                    viewModel.updateSongs()
                },
                // mode and modality functionality
                setModality = {
                    viewModel.setModality(it)
                },
                setTargetBpm = {
                    viewModel.setTargetBpm(it)
                },
                changeMode = {
                    setMode(it)
                },
                mode = mode,
                //color according to mode
                colorUI = colorUI,
                //help section
                changeShowHelp = {
                    viewModel.changeShowHelp(it)
                },
                closedHelp = closedHelp,
                shouldShowHelpScreen = { navController.navigate(Destination.help) }
            )
        }
        //HELPSCREEN
        composable(route = Destination.help) {
            HelpScreen(
                onNavigateUp = {
                    navController.navigateUp()
                })
        }
        //PLAYLISTSCREEN
        composable(route = Destination.playlist) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column {
                    PlaylistScreen(
                        //variables for displaying playlist
                        allPlaylist = allPlaylist,
                        currentId = currentId.longValue,
                        allSongsId = allSongs,
                        favoritesId = favorites,
                        //clicking songItem
                        onItemClick = { index ->
                            if (audioListId.value != currentId.longValue) {
                                audioListId.update {
                                    currentId.longValue
                                }
                                audiolist.clear()
                                audiolist.addAll(
                                    allPlaylist.find { it.playlist.playlistId == currentId.longValue }?.songs
                                        ?: mutableListOf()
                                )
                            } else {
                                val play =
                                    allPlaylist.find { it.playlist.playlistId == currentId.longValue }?.songs
                                        ?: mutableListOf()
                                audiolist.clear()
                                audiolist.addAll(play)
                            }
                            Log.d("AUDIOLIST", audiolist.toList().toString())
                            val song =
                                allPlaylist.find { it.playlist.playlistId == currentId.longValue }?.songs!!.sortedBy { it.title }[index]
                            Log.d("Song selected", "$song")
                            if (song.songId == currentSong.id) {
                                playPause()
                            } else {
                                val media = buildMediaItem(song)
                                //if Uri is valid
                                if (media != null) {
                                    setSongInPlaylist(media)
                                    playPause()
                                }
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
                        //managing favorites playlist
                        addToFavorite = {
                            viewModel.addToPlaylist(favorites, it)
                        },
                        favoriteContainsSong = {
                            viewModel.containsSong(favorites, it)
                        },
                        removeFavorite = {
                            viewModel.removeFromPlaylist(favorites, it)
                        },
                        //dialogs
                        songClicked = songClicked,
                        shouldShowDialogTwo = shouldShowDialogTwo,
                        shouldShowDialogThree = shouldShowDialogThree,
                        shouldShowDialogFive = shouldShowDialogFive,
                        shouldShowDialogFour = shouldShowDialogFour,
                        addToQueue = addToQueue,
                        //colorUI according to mode
                        colorUI = colorUI,
                        mode = mode,
                        //starting first song
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
                            } else {
                                val play =
                                    allPlaylist.find { it.playlist.playlistId == currentId.longValue }?.songs
                                        ?: mutableListOf()
                                audiolist.clear()
                                audiolist.addAll(play)
                            }
                            Log.d("AUDIOLIST", audiolist.toList().toString())
                            nextSong()
                            playPause()
                        },
                        onNavigateUp = { navController.navigateUp() }
                    )
                }
            }
        }
        //PLAYSCREEN
        composable(route = Destination.songScreen) {
            PlayScreenDesign(
                onNavigateUp = { navController.navigateUp() },
                shouldShowHelpScreen = { navController.navigate(Destination.help) },
                //currentSong
                song = currentSong,
                isPlaying = isPlaying,
                progress = progress / 100,
                onProgress = onProgress,
                duration = duration,
                //playpause
                onStart = onStart,
                //next prev
                nextSong = nextSong,
                prevSong = prevSong,
                //modality functionality
                toggleMode = toggleMode,
                plus = plus,
                minus = minus,
                target = targetBpm.toString(),
                bpm = currentSong.bpm.toString(),
                modality = modality,
                //showing Queue
                queue = {
                    viewModel.showQueue(true)
                    navController.navigate(Destination.queue)
                },
                //managing favorites playlist
                addToFavorite = {
                    viewModel.addToPlaylist(favorites, it)
                },
                favoriteContainsSong = {
                    viewModel.containsSong(favorites, it)
                },
                removeFavorite = {
                    viewModel.removeFromPlaylist(favorites, it)
                },
                //colorUI according to mode
                colorUI = colorUI
            )
        }
        //QUEUE
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
                //list of songs for queue
                queue = queue, //selected by user
                audioList = audioList, //selected by system
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
