package com.example.buddybeat.ui.audio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.example.buddybeat.data.models.Playlist
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.ui.CurrentSong
import com.example.buddybeat.ui.components.HomeBottomBar
import com.example.buddybeat.ui.components.SongItem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Brush

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun HomeScreen(
    showPlayer: Boolean,
    changeShow : () -> Unit,
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
    onProgress: (Float) -> Unit,
    onStart: () -> Unit,
    incrementSpeed: () -> Unit,
    decrementSpeed: () -> Unit,
    addToFavorite : (Long) -> Unit,
    favoriteContainsSong : (Long) -> LiveData<Int>,
    removeFavorite : (Long) -> Unit,
    shouldShowDialogTwo : MutableState<Boolean>,
    shouldShowDialogOne : MutableState<Boolean>,
    shouldShowDialogThree: MutableState<Boolean>,
    songClicked : MutableState<Long>,
    addToQueue : (Song) -> Unit,
    showBottomSheet : MutableState<Boolean>,
    playlistLongClicked: MutableState<Playlist>,
    updateSongs: () -> Unit,
    allSongsId : Long
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
                    .padding(horizontal = 15.dp).fillMaxHeight()
            ) {
                TopBar(updateSongs = updateSongs)
                TopPopup()
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
                    ModeButton("Walking", color = Color(0xFFB1B2FF), onClick = { /* Handle Walking Mode */ })
                    ModeButton("Running", Color(0xFFD0EB34), onClick = { /* Handle Running Mode */ })
                }
                Row(  modifier = Modifier.fillMaxWidth() .padding(start=20.dp, end = 20.dp),verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End){
                    SongsOfTheWeek("YOUR PLAYLISTS")
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {shouldShowDialogOne.value = true}, modifier = Modifier.size(20.dp)) {
                        Icon(painter = painterResource(id = R.drawable.add_to_playlist), contentDescription = "Add playlist", )
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
                        playlistClicked(it) },
                    allPlaylist = allPlaylist,
                    showBottomSheet = showBottomSheet,
                    playlistLongClicked = playlistLongClicked,
                    allSongsId = allSongsId
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
fun TopBar(
    updateSongs : () -> Unit
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
        Row (horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically){
            Card(
                onClick = { /*TODO*/ },
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.help),
                    contentDescription = "Buddy Beat Logo",
                    modifier = Modifier.size(40.dp)
                )
            }

        }
    }
}

@Composable
fun TopPopup() {
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
                    onClick = { /* Handle close popup */ },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close popup")
                }
            }
            Text(
                text = "Welcome to Buddy Beat! In this app, your music moves with you—literally. Whether you're walking or running, the speed of your songs adjusts to match your activity, keeping you in the perfect rhythm. You can create your playlists and let the app automatically reorder your tracks based on your pace. If you need help, you can always refer to the help section!",
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 14.sp,
                lineHeight = 18.sp

            )
            Button(
                onClick = { /* Handle button click */ },
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
    playlistLongClicked : MutableState<Playlist>,
    allSongsId : Long
) {
    Column(
        modifier = Modifier
            .fillMaxWidth().height(280.dp)
            .padding(top = 5.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .combinedClickable (
                    onClick = {
                        allPlaylist.find{it.playlist.playlistId==allSongsId}
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
                            colors = listOf(Color(0xFF7172CC), Color(0xFF91A6FE)) // Green to Blue gradient
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {


                allPlaylist.find{
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
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        //.weight(1f)
                        .fillMaxWidth()
                        .height(80.dp)
                        //.padding(5.dp)
                        .combinedClickable(
                            onClick = {
                                playlistClicked(playlist)
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showBottomSheet.value = true
                                playlistLongClicked.value = playlist.playlist
                            }
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF8BA4FE), Color(0xFFAC9DFF)) // Green to Blue gradient
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


        /*LazyColumn {
            itemsIndexed(allPlaylist.drop(1)) { index, playlist ->
                Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .height(80.dp)
                            .combinedClickable(
                                onClick = {
                                    setVisiblePlaylist(
                                        playlist.playlist.playlistId,
                                        playlist.songs, playlist.playlist.title
                                    )
                                    allSongsClicked(playlist)
                                },
                                onLongClick = {
                                    /*TODO Show menu to rename playlist and delete playlist*/
                                }
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = "",
                                contentDescription = "Now Playing",
                                placeholder = painterResource(id = R.drawable.playlistimg02),
                                error = painterResource(id = R.drawable.playlistimg02),
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                playlist.playlist.title.uppercase(),
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }*/
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

