package com.example.buddybeat.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import coil.compose.AsyncImage
import com.example.buddybeat.R
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.ui.CurrentSong
import com.example.buddybeat.ui.components.HomeBottomBar
import com.example.buddybeat.ui.components.LinearDeterminateIndicator
import com.example.buddybeat.ui.components.SongItem
import com.example.buddybeat.ui.components.ValuesCard

/*PLAYLIST SCREEN*/
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistScreen(
    onItemClick: (Int) -> Unit,
    loading: Boolean,
    currentProgress: Float,
    song: CurrentSong,
    onBarClick: () -> Unit,
    prevSong: () -> Unit,
    nextSong: () -> Unit,
    progress: Float,
    isPlaying: Boolean,
    onStart: () -> Unit,
    text1: String,
    text2: String,
    text3: String,
    addToFavorite: (Long) -> Unit,
    removeFavorite: (Long) -> Unit,
    favoriteContainsSong: (Long) -> LiveData<Int>,
    shouldShowDialogTwo: MutableState<Boolean>,
    shouldShowDialogThree: MutableState<Boolean>,
    songClicked: MutableState<Long>,
    allPlaylist: List<PlaylistWithSongs>,
    currentId: Long,
    addToQueue: (Song) -> Unit,
    shouldShowDialogFive: MutableState<Boolean>,
    shouldShowDialogFour: MutableState<Boolean>,
    allSongsId: Long,
    favoritesId: Long,
    colorUI: Color,
    startFirstSong: () -> Unit,
    mode: Long,
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        //bottomBar player
        bottomBar = {
            if (song.title.isNotEmpty())
                HomeBottomBar(
                    song = song,
                    onBarClick = onBarClick,
                    prevSong = prevSong,
                    nextSong = nextSong,
                    progress = progress,
                    isPlaying = isPlaying,
                    onStart = onStart,
                    favoriteContains = favoriteContainsSong(song.id),
                    addToFavorite = {
                        addToFavorite(song.id)
                    },
                    removeFavorite = {
                        removeFavorite(song.id)
                    })
        }) {
        Column {
            //progress calculation of bpm
            if (!loading)
                LinearDeterminateIndicator(currentProgress, Color.Black, Color.White)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIos,
                        contentDescription = "back",
                    )
                }
                //if playlist different from all_songs and favorites -> show menu
                allPlaylist.find { it.playlist.playlistId == currentId }?.playlist?.let { it1 ->
                    if (it1.playlistId != allSongsId && it1.playlistId != favoritesId) {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "menu",
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color(0xFFF0F0F0)),
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Rename Playlist") },
                                    onClick = {
                                        shouldShowDialogFive.value = true
                                        expanded = false
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Playlist") },
                                    onClick = {
                                        shouldShowDialogFour.value = true
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            //SPM, BPM, ratio boxes
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                ValuesCard(text1, text2, text3, colorUI, mode)
            }
            //scrollable list of songs in playlist
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .padding(it),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                //Top section
                item {
                    allPlaylist.find { it.playlist.playlistId == currentId }?.playlist?.let { it1 ->
                        val modifier =
                            if (it1.playlistId != allSongsId && it1.playlistId != favoritesId)
                                Modifier.combinedClickable(
                                    onClick = { },
                                    onLongClick = {
                                        shouldShowDialogFive.value = true
                                    }
                                ) else Modifier
                        TopSection(
                            modifier,
                            it1.title,
                            startFirstSong = startFirstSong,
                            colorUI = colorUI
                        )
                    }
                }
                //list of songs
                allPlaylist.find { it.playlist.playlistId == currentId }?.let { it1 ->
                    itemsIndexed(it1.songs.sortedBy { it.title }) { index, audio ->
                        SongItem(
                            audio = audio,
                            onItemClick = {
                                onItemClick(index)
                            },
                            isPlaying = audio.songId == song.id,
                            addToFavorite = addToFavorite,
                            removeFavorite = removeFavorite,
                            favoriteContainsSong = favoriteContainsSong,
                            shouldShowDialogTwo = shouldShowDialogTwo,
                            songClicked = songClicked,
                            shouldShowDialogThree = shouldShowDialogThree,
                            addToQueue = addToQueue,
                            colorUI = colorUI
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSection(
    modifier: Modifier,
    title: String,
    resource: Int = R.drawable.mainimage,
    startFirstSong: () -> Unit,
    colorUI: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = "",
            placeholder = painterResource(id = resource),
            error = painterResource(id = resource),
            contentDescription = "image_playlist",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(250.dp)
                .padding(horizontal = 50.dp)
                .clip(RoundedCornerShape(bottomStart = 130.dp, bottomEnd = 130.dp))
        )
        Text(
            text = title,
            modifier = modifier.padding(bottom = 10.dp),
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.W900,
        )
        Card(
            onClick = {
                startFirstSong()
            },
            shape = CircleShape,
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomCenter)
                .padding(10.dp), colors =
            CardDefaults.cardColors(containerColor = colorUI)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "",
                    modifier = Modifier.size(45.dp), tint = Color.Black
                )
            }
        }
    }
}