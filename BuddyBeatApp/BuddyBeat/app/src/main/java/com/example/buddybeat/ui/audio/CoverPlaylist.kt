/*package com.example.buddybeat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.buddybeat.R
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.ui.components.HomeBottomBar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverPlaylist(
    filePath: String = "",
    songName: String = "All Songs",
    showPlayer: Boolean,
    changeShow: () -> Unit,
    allSongsClicked: () -> Unit,
    allSongs: List<Song>,
    audioList: List<Song>,
    allPlaylist: List<PlaylistWithSongs>,
    onItemClick: (Int) -> Unit,
    isPlaying: Boolean,
    song: CurrentSong,
    onBarClick: () -> Unit,
    prevSong: () -> Unit,
    nextSong: () -> Unit,
    progress: Float,
    onProgress: (Float) -> Unit,
    onStart: () -> Unit,
    incrementSpeed: () -> Unit,
    decrementSpeed: () -> Unit,
    setVisiblePlaylist: (Long, List<Song>) -> Unit,
    addToFavorite: (Long) -> Unit,
    favoriteContainsSong: (Long) -> Boolean,
    removeFavorite: (Long) -> Unit,
    currentId: Long
) {
    Scaffold(bottomBar = {
        if (song.title.isNotEmpty())
            HomeBottomBar(
                song = song,
                onBarClick = onBarClick,
                isPlaying = isPlaying,
                progress = progress,
                onProgress = onProgress,
                onStart = onStart,
                nextSong = nextSong,
                prevSong = prevSong,
                incrementSpeed = incrementSpeed,
                decrementSpeed = decrementSpeed,
                favoriteContains = favoriteContainsSong(currentId),
                addToFavorite = {
                    addToFavorite(currentId)
                }
            ) {
                removeFavorite(currentId)
            }
    }) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                TopSection(filePath, songName) {
                    // Handle the click action if necessary
                }
            }
            items(8) { index ->
                SongItem(
                    songName = "Song Name $index",
                    artist = "Artist $index",
                    isPlaying = index == 0
                ) {
                    // Handle song item click action if necessary
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun TopSection(
    filePath: String,
    title: String,
    resource: Int = R.drawable.mainimage,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = File(filePath),
            placeholder = painterResource(id = resource),
            error = painterResource(id = resource),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(450.dp)
                .padding(horizontal = 50.dp)
                .clip(RoundedCornerShape(bottomStart = 130.dp, bottomEnd = 130.dp))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Handle back action */ }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIos,
                    contentDescription = "",
                )
            }
            IconButton(onClick = { /* Handle menu action */ }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "",
                )
            }
        }
        Text(
            text = title,
            modifier = Modifier
                .padding(bottom = 100.dp)
                .align(Alignment.BottomCenter)
                .clickable {
                    onClick()
                },
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.W900
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongItem(songName: String, artist: String, isPlaying: Boolean, openDialog: () -> Unit = {}) {
    val backgroundColor = if (isPlaying) Color(0xFF82B0E6) else Color(0xFFD6E1E7)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))  // Using hex for black color
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp)) // Adjusted spacing after removing the icon

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (songName.length > 15) "${songName.take(15)}..." else songName,
                color = Color.Black
            )
            Text(
                text = if (artist.length > 15) "${artist.take(15)}..." else artist,
                color = Color.DarkGray
            )
        }

        IconButton(onClick = { /* TODO: Add to favorites */ }) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Add to favorites",
                tint = Color.Black
            )
        }

        IconButton(onClick = { openDialog() }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = Color.Black
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewCoverPlaylist() {
    CoverPlaylist(
        filePath = "",
        songName = "All Songs",
        showPlayer = true,
        changeShow = {},
        allSongsClicked = {},
        allSongs = emptyList(),
        audioList = emptyList(),
        allPlaylist = emptyList(),
        onItemClick = {},
        isPlaying = false,
        song = CurrentSong(id = 0L, title = "Example Song", artist = "Example Artist", uri = "example_uri"),
        onBarClick = {},
        prevSong = {},
        nextSong = {},
        progress = 0f,
        onProgress = {},
        onStart = {},
        incrementSpeed = {},
        decrementSpeed = {},
        setVisiblePlaylist = { _, _ -> },
        addToFavorite = {},
        favoriteContainsSong = { false },
        removeFavorite = {},
        currentId = 0L
    )
}*/
