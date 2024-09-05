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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.buddybeat.player.PlaybackService
import com.example.buddybeat.player.PlaybackService.Companion.AUTO_MODE
import com.example.buddybeat.player.PlaybackService.Companion.speedMode
import com.example.buddybeat.ui.CurrentSong
import com.example.buddybeat.ui.components.HomeBottomBar

@RequiresApi(Build.VERSION_CODES.O)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun HomeScreen(
    showPlayer: Boolean,
    changeShow: () -> Unit,
    playlistClicked: (PlaylistWithSongs) -> Unit,
    allPlaylist: List<PlaylistWithSongs>,
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
    shouldShowDialogOne: MutableState<Boolean>,
    showBottomSheet: MutableState<Boolean>,
    playlistLongClicked: MutableState<Playlist>,
    updateSongs: () -> Unit,
    allSongsId: Long,
    favoritesId: Long,
    setModality: (Long) -> Unit,
    changeMode: (Long) -> Unit,
    mode: Long,
    colorUI: Color,
    changeShowHelp: (Boolean) -> Unit,
    closedHelp: Boolean,
    setTargetBpm : (Int) -> Unit,
    shouldShowHelpScreen: () -> Unit,
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
                },
                removeFavorite = {
                    removeFavorite(song.id)
                })
    })
    { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .fillMaxHeight()
            ) {
                TopBar(changeShowHelp = changeShowHelp)
                TopPopup(
                    onClickClose = {
                        changeShowHelp(it)
                    },
                    closedHelp = closedHelp,
                    shouldShowHelpScreen = shouldShowHelpScreen
                )
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
                    ModeButton("Walking", color = colorWalking, icon = Icons.Default.DirectionsWalk,onClick = {
                        changeMode(1L)
                        speedMode = AUTO_MODE
                        setModality(speedMode)
                        if (speedMode == PlaybackService.MANUAL_MODE || speedMode == PlaybackService.OFF_MODE)
                            PlaybackService.ALPHA = 0.4f
                        else if (speedMode == AUTO_MODE)
                            PlaybackService.ALPHA = 0.7f
                        setTargetBpm(100)
                        //startWalkingMode(context) /* Handle Walking Mode */
                    })
                    ModeButton("Running", color = colorRunning,icon = Icons.Default.DirectionsRun, onClick = {
                        changeMode(2L)
                        speedMode = AUTO_MODE
                        setModality(speedMode)
                        if (speedMode == PlaybackService.MANUAL_MODE || speedMode == PlaybackService.OFF_MODE)
                            PlaybackService.ALPHA = 0.4f
                        else if (speedMode == AUTO_MODE)
                            PlaybackService.ALPHA = 0.7f
                        setTargetBpm(160)
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
                            modifier = Modifier.background(Color(0xFFF0F0F0))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Update songs", color = Color.DarkGray) },
                                onClick = {
                                    expanded = false
                                    updateSongs()
                                }
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
                    favoritesId = favoritesId,
                    colorUI = colorUI
                )
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
    changeShowHelp: (Boolean) -> Unit
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
            onClick = { changeShowHelp(false) },
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
    onClickClose: (Boolean) -> Unit,
    closedHelp: Boolean,
    shouldShowHelpScreen : () -> Unit,
) {
    if (!closedHelp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(
                    color = if (isSystemInDarkTheme()) Color(0xFF3C3C3C) else Color(
                        0xFFE6E8E5
                    ), RoundedCornerShape(10.dp)
                )
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
                        modifier = Modifier.align(Alignment.TopEnd),
                        onClick = {
                            onClickClose(true)
                        },

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
                    onClick = { shouldShowHelpScreen() },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000))
                ) {
                    Text("Help", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ModeButton(text: String, color: Color, onClick: () -> Unit, icon : ImageVector) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(150.dp)
            .height(50.dp), // Removed Modifier.background
        colors = ButtonDefaults.buttonColors(containerColor = color), // Set the background color via ButtonDefaults
    ) {
        Icon(imageVector = icon, contentDescription = "mode", tint = Color.Black)
        Spacer(modifier = Modifier.width(10.dp))
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
    favoritesId: Long,
    colorUI: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            //.height(280.dp)
            .padding(top = 5.dp, bottom = 5.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
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
                                colorUI.copy(red = 0.5f),
                                colorUI
                            ) // Green to Blue gradient
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {


                allPlaylist.find {
                    it.playlist.playlistId == allSongsId
                }?.playlist?.title?.let {
                    Text(it, color = Color(0xFF111111), fontSize = 20.sp, fontWeight = FontWeight.W900, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) }
            }
        }
        val haptics = LocalHapticFeedback.current
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(5.dp),
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
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        colorUI, colorUI.copy(red = 0.5f)
                                    ) // Green to Blue gradient
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            playlist.playlist.title,
                            color = Color(0xFF111111),
                            fontSize = 18.sp, fontWeight = FontWeight.SemiBold
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
