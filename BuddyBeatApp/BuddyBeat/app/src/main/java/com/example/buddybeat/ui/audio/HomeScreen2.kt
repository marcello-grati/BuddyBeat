package com.example.buddybeat.ui.audio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.buddybeat.R

@Composable
fun HomeScreen() {
    Scaffold(bottomBar = {
        NowPlaying("Song Name 2", "Artist 1")
    }) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                item { TopBar() }
                item { SearchBar() }
                item { SongsOfTheWeek("YOUR PLAYLISTS") }
                item { MainButtons() }
                item { SongsOfTheWeek("RECCOMMENDED:") }
                items(3) { index ->
                    SongItem(
                        songName = "Song Name $index",
                        artist = "Artist $index",
                        isPlaying = index == 0
                    )
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
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
            painter = painterResource(id = R.drawable.lefticonnew),
            contentDescription = "Buddy Beat Logo",
            modifier = Modifier.size(90.dp)
        )
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        border = BorderStroke(2.dp, Color.Black),
        shape = RoundedCornerShape(50.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        TextField(
            value = "",
            onValueChange = { /* TODO: Handle search query */ },
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = { Text("Search") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
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
fun MainButtons() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            onClick = { /*TODO*/ },
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
        }
        Row(
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
                        contentScale = ContentScale.FillBounds, modifier = Modifier.scale(20f,3f)
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
                        contentScale = ContentScale.FillBounds, modifier = Modifier.scale(10f,3f)
                    )
                    Text("FAVORITES", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}


@Composable
fun SongsOfTheWeek(data:String) {
    Text(
        data,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongItem(songName: String, artist: String, isPlaying: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE4E8E6), shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            onClick = { /*TODO*/ }, shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
            modifier = Modifier.size(45.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = "",
                    contentDescription = "Now Playing",
                    placeholder = painterResource(id = R.drawable.musicnote),
                    error = painterResource(id = R.drawable.musicnote),
                    modifier = Modifier.width(25.dp)
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
}

@OptIn(ExperimentalMaterial3Api::class)
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFffffff)),
                modifier = Modifier.size(50.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = "",
                        contentDescription = "Now Playing",
                        placeholder = painterResource(id = R.drawable.musicnote),
                        error = painterResource(id = R.drawable.musicnote),
                        modifier = Modifier.width(25.dp)
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
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun show() {
    HomeScreen()
}