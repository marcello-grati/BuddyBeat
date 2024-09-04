package com.example.buddybeat.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.media3.common.util.UnstableApi
import com.example.buddybeat.R
import com.example.buddybeat.SensorService
import com.example.buddybeat.data.models.Playlist
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.player.PlaybackService.Companion.AUTO_MODE
import com.example.buddybeat.player.PlaybackService.Companion.speedMode
import com.example.buddybeat.ui.CurrentSong
import com.example.buddybeat.ui.components.HomeBottomBar
import com.example.buddybeat.ui.components.SongItem

@RequiresApi(Build.VERSION_CODES.O)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun HomeScreen(
    showPlayer: Boolean,
    changeShow: () -> Unit,
    playlistClicked: (PlaylistWithSongs) -> Unit,
    audioList: List<Song>,
    allPlaylist: List<PlaylistWithSongs>,
    onItemClick: (Int) -> Unit,
    isPlaying: Boolean,
    song: CurrentSong,
    onBarClick: () -> Unit,
    prevSong: () -> Unit,
    nextSong: () -> Unit,
    progress: Float,
    onStart: () -> Unit,
    addToFavorite: (Long) -> Unit,
    favoriteContainsSong: (Long) -> LiveData<Int>,
    removeFavorite: (Long) -> Unit,
    shouldShowDialogTwo: MutableState<Boolean>,
    shouldShowDialogOne: MutableState<Boolean>,
    shouldShowDialogThree: MutableState<Boolean>,
    songClicked: MutableState<Long>,
    addToQueue: (Song) -> Unit,
    showBottomSheet: MutableState<Boolean>,
    playlistLongClicked: MutableState<Playlist>,
    updateSongs: () -> Unit,
    allSongsId: Long,
    favoritesId: Long,
    setModality: (Long) -> Unit,
    changeMode: (Long) -> Unit,
    mode: Long
) {
    val context = LocalContext.current
    val colorWalking = if (mode == 1L) Color(0xFFB1B2FF) else Color(0xFF80809C).copy(alpha = 0.5f)
    val colorRunning = if (mode == 2L) Color(0xFFD0EB34) else Color(0xFF8B8F73).copy(alpha = 0.5f)

    Scaffold(bottomBar = {
        if (song.title != "")
            HomeBottomBar(
                song = song,
                onBarClick = onBarClick,
                isPlaying = isPlaying,
                progress = progress,
                onStart = onStart,
                nextSong = nextSong,
                prevSong = prevSong,
                favoriteContains = favoriteContainsSong(song.id),
                addToFavorite = {
                    addToFavorite(song.id)
                }
            ) {
                removeFavorite(song.id)
            }
    })
    { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .fillMaxHeight()
            ) {
                TopBar()
                var expandedHelp by remember { mutableStateOf(true) }
                if (expandedHelp)
                    TopPopup(onClickClose = { expandedHelp = false })
                Text(
                    text = "CHOOSE YOUR MODE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModeButton("Walking", color = colorWalking, onClick = {
                        changeMode(1L)
                        speedMode = AUTO_MODE
                        setModality(speedMode)
                        //startWalkingMode(context) /* Handle Walking Mode */
                    })
                    ModeButton("Running", color = colorRunning, onClick = {
                        changeMode(2L)
                        speedMode = AUTO_MODE
                        setModality(speedMode)
                        //startRunningMode(context)/* Handle Running Mode */
                    })
                    /*ModeButton(
                        "Walking",
                        color = if (!running) Color(0xFFB1B2FF) else Color(0xFF80809C).copy(alpha = 0.5f),
                        onClick = { changeMode(0) })
                    ModeButton(
                        "Running",
                        color = if (running) Color(0xFFD0EB34) else Color(0xFF8B8F73).copy(alpha = 0.5f),
                        onClick = { changeMode(1) })*/
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    SongsOfTheWeek("YOUR PLAYLISTS")
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { shouldShowDialogOne.value = true },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_to_playlist),
                            contentDescription = "Add playlist",
                        )
                    }
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "",
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color(0xFFF0F0F0)),
                        ) {
                            DropdownMenuItem(
                                text = { Text("Update songs") },
                                onClick = {
                                    expanded = false
                                    updateSongs()
                                },
                            )
                        }
                    }

                }
                MainButtons(
                    playlistClicked = {
                        playlistClicked(it)
                    },
                    allPlaylist = allPlaylist,
                    showBottomSheet = showBottomSheet,
                    playlistLongClicked = playlistLongClicked,
                    allSongsId = allSongsId,
                    favoritesId = favoritesId
                )
                SongsOfTheWeek("RECOMMENDED:")
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(audioList) { index, audio ->
                        SongItem(
                            audio = audio,
                            onItemClick = {
                                onItemClick(index)
                            },
                            isPlaying = audio.songId == song.id,
                            addToFavorite = addToFavorite,
                            favoriteContainsSong = favoriteContainsSong,
                            removeFavorite = removeFavorite,
                            shouldShowDialogTwo = shouldShowDialogTwo,
                            songClicked = songClicked,
                            shouldShowDialogThree = shouldShowDialogThree,
                            addToQueue = addToQueue
                        )
                    }
                    item { Spacer(modifier = Modifier.height(10.dp)) }
                }
            }
        }

    }
    OnLifecycleEvent { owner, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (showPlayer) {
                    onBarClick()
                    changeShow()
                }
            }

            else -> { /**/
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun startWalkingMode(context: Context) {
    val intent = Intent(context, SensorService::class.java)
    intent.action = "SET_WALKING_MODE"
    context.startForegroundService(intent)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun startRunningMode(context: Context) {
    val intent = Intent(context, SensorService::class.java)
    intent.action = "SET_RUNNING_MODE"
    context.startForegroundService(intent)
}

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.bblogo),
            contentDescription = "Buddy Beat Logo",
            modifier = Modifier
                .size(width = 120.dp, height = 60.dp),  // Set different width and height
        )
        Card(
            onClick = { /*TODO Help Section*/ },
            shape = RoundedCornerShape(50.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Image(
                painter = painterResource(id = R.drawable.help),
                contentDescription = "Help Button",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun TopPopup(
    onClickClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFE6E8E5), RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Hi, user!",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 10.dp, top = 8.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { onClickClose() },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close popup")
                }
            }
            Text(
                text = stringResource(R.string.help_section),
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 14.sp,
                lineHeight = 18.sp

            )
            Button(
                onClick = { /*TODO help section */ },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000))
            ) {
                Text("Help")
            }
        }
    }
}

@Composable
fun ModeButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(150.dp)
            .height(50.dp), // Removed Modifier.background
        colors = ButtonDefaults.buttonColors(containerColor = color) // Set the background color via ButtonDefaults
    ) {
        Text(text, color = Color.Black, fontSize = 16.sp)
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainButtons(
    playlistClicked: (PlaylistWithSongs) -> Unit,
    allPlaylist: List<PlaylistWithSongs>,
    showBottomSheet: MutableState<Boolean>,
    playlistLongClicked: MutableState<Playlist>,
    allSongsId: Long,
    favoritesId: Long
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(top = 5.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .combinedClickable(
                    onClick = {
                        allPlaylist
                            .find { it.playlist.playlistId == allSongsId }
                            ?.let { playlistClicked(it) }
                    }
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF7172CC),
                                Color(0xFF91A6FE)
                            ) // Green to Blue gradient
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {


                allPlaylist.find {
                    it.playlist.playlistId == allSongsId
                }?.playlist?.title?.let { Text(it, color = Color.White, fontSize = 16.sp) }
            }
        }
        val haptics = LocalHapticFeedback.current
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            itemsIndexed(allPlaylist.dropWhile { it.playlist.playlistId == allSongsId }) { index, playlist ->
                val modifier = if (playlist.playlist.playlistId != favoritesId)
                    Modifier.combinedClickable(
                        onClick = {
                            playlistClicked(playlist)
                        },
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            showBottomSheet.value = true
                            playlistLongClicked.value = playlist.playlist
                        }
                    ) else Modifier.clickable(onClick = { playlistClicked(playlist) })
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF8BA4FE),
                                        Color(0xFFAC9DFF)
                                    ) // Green to Blue gradient
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            playlist.playlist.title,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SongsOfTheWeek(data: String) {
    Text(
        data,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}
