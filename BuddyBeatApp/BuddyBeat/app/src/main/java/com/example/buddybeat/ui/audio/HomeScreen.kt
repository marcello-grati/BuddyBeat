package com.example.buddybeat.ui.audio

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.example.buddybeat.R
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.ui.CurrentSong
import com.example.buddybeat.ui.components.HomeBottomBar
import com.example.buddybeat.ui.components.SongItem

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun HomeScreen(
    showPlayer: Boolean,
    changeShow : () -> Unit,
    allSongsClicked: (PlaylistWithSongs) -> Unit,
    allSongs : List<Song>,
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
    setVisiblePlaylist : (Long, List<Song>, String) -> Unit,
    addToFavorite : (Long) -> Unit,
    favoriteContainsSong : (Long) -> LiveData<Int>,
    removeFavorite : (Long) -> Unit
    ) {
    Scaffold(bottomBar = {
        if (song.title != "")
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
                    .padding(horizontal = 15.dp),
            ) {
                TopBar()
                SearchBar()
                SongsOfTheWeek("YOUR PLAYLISTS")
                MainButtons(
                    allSongsClicked = {
                        Log.d("PlaylistMainButton",it.toString())
                        allSongsClicked(it) },
                    allPlaylist = allPlaylist,
                    setVisiblePlaylist = setVisiblePlaylist,
                    allSongs = allSongs
                )
                SongsOfTheWeek("RECOMMENDED:")
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    /*items(3) { index ->
                    //SongItem(
                    //    //songName = "Song Name $index",
                    //    //artist = "Artist $index",
                    //    isPlaying = index == 0
                    //)
                }*/
                    itemsIndexed(audioList) { index, audio ->
                        SongItem(
                            audio = audio,
                            onItemClick = {
                                onItemClick(index)
                            },
                            isPlaying = audio.songId==song.id,
                            addToFavorite = addToFavorite,
                            favoriteContainsSong = favoriteContainsSong,
                            removeFavorite = removeFavorite
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
                if(showPlayer) {
                    onBarClick()
                    changeShow()
                }
            }
            else -> { /**/ }
        }
    }
}

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event:Lifecycle.Event) -> Unit) {
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
fun TopBar() {
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
        Row (horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically){
            Card(
                onClick = { /*TODO*/ },
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.myprofilenow),
                    contentDescription = "Buddy Beat Logo",
                    modifier = Modifier.size(50.dp)
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "",
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        border = BorderStroke(3.dp, Color(0xFF0C30AF)),
        shape = RoundedCornerShape(50.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        TextField(
            value = "",
            onValueChange = { /* TODO: Handle search query */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            placeholder = { Text("Search") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Blue
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            )
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainButtons(
    allSongsClicked: (PlaylistWithSongs) -> Unit,
    allPlaylist: List<PlaylistWithSongs>,
    setVisiblePlaylist : (Long, List<Song>, String) -> Unit,
    allSongs : List<Song>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        /*Card(
            onClick = {
                setVisiblePlaylist(1L, allSongs, "ALL SONGS")
                allSongsClicked(playlist) },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = "",
                    contentDescription = "Now Playing",
                    placeholder = painterResource(id = R.drawable.img3),
                    error = painterResource(id = R.drawable.img3),
                    contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxWidth()
                )
                Text("ALL SONGS", color = Color.White, fontSize = 13.sp)
            }
        }*/
        LazyColumn {
            itemsIndexed(allPlaylist/*.drop(1)*/) { index, playlist ->
                Card(
                    onClick = {
                        setVisiblePlaylist(playlist.playlist.playlistId,
                            playlist.songs, playlist.playlist.title
                        )
                        allSongsClicked(playlist) },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = "",
                            contentDescription = "Now Playing",
                            placeholder = painterResource(id = R.drawable.playlistimg02),
                            error = painterResource(id = R.drawable.playlistimg02),
                            contentScale = ContentScale.FillBounds
                        )
                        Text(playlist.playlist.title.uppercase(), color = Color.White, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        /*Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                onClick = { /*TODO*/ },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .height(100.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = "",
                        contentDescription = "Now Playing",
                        placeholder = painterResource(id = R.drawable.img2),
                        error = painterResource(id = R.drawable.img2),
                        contentScale = ContentScale.FillBounds, modifier = Modifier.scale(10f, 3f)
                    )
                    Text("RECENTLY PLAYED", color = Color.White, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Card(
                onClick = { /*TODO*/ },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .height(100.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = "",
                        contentDescription = "Now Playing",
                        placeholder = painterResource(id = R.drawable.img2),
                        error = painterResource(id = R.drawable.img2),
                        contentScale = ContentScale.FillBounds, modifier = Modifier.scale(10f, 3f)
                    )
                    Text("FAVORITES", color = Color.White, fontSize = 13.sp)
                }
            }
        }*/
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

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongItem(songName: String, artist: String, isPlaying: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEAEAEA), shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            onClick = { /*TODO*/ }, shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x00ffffff)),
            modifier = Modifier.size(45.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = "",
                    contentDescription = "Now Playing",
                    placeholder = painterResource(id = R.drawable.musicicon),
                    error = painterResource(id = R.drawable.musicicon),
                    modifier = Modifier.width(45.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
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
        IconButton(onClick = { /* TODO: Show more options */ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = Color.Black
            )
        }
    }
}*/

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlaying(songName: String, artist: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF000000))
            .padding(vertical = 15.dp, horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Card(
                onClick = { /*TODO*/ }, shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x00ffffff)),
                modifier = Modifier.size(50.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = "",
                        contentDescription = "Now Playing",
                        placeholder = painterResource(id = R.drawable.musicicon1),
                        error = painterResource(id = R.drawable.musicicon1),
                        modifier = Modifier.width(100.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (songName.length > 15) "${songName.take(15)}..." else songName,
                    color = Color.White, fontSize = 13.sp
                )
                Text(
                    text = if (artist.length > 15) "${artist.take(15)}..." else artist,
                    color = Color.LightGray, fontSize = 13.sp
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IconButton(
                onClick = { /* TODO: Decrease speed */ }, modifier = Modifier
                    .background(
                        Color.White,
                        RoundedCornerShape(30.dp)
                    )
                    .size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease speed",
                    tint = Color.Black
                )
            }
            IconButton(
                onClick = { /* TODO: Decrease speed */ }, modifier = Modifier
                    .background(
                        Color.White,
                        RoundedCornerShape(30.dp)
                    )
                    .size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Decrease speed",
                    tint = Color.Black
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
                onClick = { /* TODO: Add to favorites */ }, modifier = Modifier

                    .size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Add to favorites",
                    tint = Color.Black
                )
            }
            Card(
                onClick = {},
                shape = CircleShape,
                modifier = Modifier.size(35.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "",
                        modifier = Modifier.size(30.dp), tint = Color.Black
                    )
                }
            }
        }
    }
}*/

/*@Preview(showSystemUi = true, showBackground = true)
@Composable
fun show() {
    HomeScreen()
}*/

