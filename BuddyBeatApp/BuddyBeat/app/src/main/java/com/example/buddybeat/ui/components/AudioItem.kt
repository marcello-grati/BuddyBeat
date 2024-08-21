package com.example.buddybeat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import coil.compose.AsyncImage
import com.example.buddybeat.R
import com.example.buddybeat.data.models.Song
import kotlin.math.floor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueItem(
    audio: Song,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEAEAEA), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                shape = RoundedCornerShape(10.dp),
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
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = if (audio.title.length > 15) "${audio.title.take(15)}..." else audio.title,
                    color = Color.Black
                )
                Text(
                    text = if (audio.artist.length > 15) "${audio.artist.take(15)}..." else audio.artist,
                    color = Color.DarkGray
                )
            }
            Column(modifier = Modifier.weight(0.5f)) {
                Text(text = audio.bpm.toString())
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongItem(
    isPlaying: Boolean,
    audio: Song,
    onItemClick: () -> Unit,
    addToFavorite : (Long) -> Unit,
    removeFavorite : (Long) -> Unit,
    favoriteContainsSong : (Long) -> LiveData<Int>
) {
    val img by favoriteContainsSong(audio.songId).observeAsState(initial = 0)
    val backgroundColor = if (isPlaying) Color(0xFF82B0E6) else Color(0xFFD6E1E7)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            .clickable {
                onItemClick()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                shape = RoundedCornerShape(10.dp),
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
                    text = if (audio.title.length > 15) "${audio.title.take(15)}..." else audio.title,
                    color = Color.Black
                )
                Text(
                    text = if (audio.artist.length > 15) "${audio.artist.take(15)}..." else audio.artist,
                    color = Color.DarkGray
                )
            }

            IconButton(onClick = {
                if(img==0) {
                    addToFavorite(audio.songId)
                }
                else{
                    removeFavorite(audio.songId)
                }
            }) {
                Icon(
                    imageVector = if(img!=0) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Add to favorites",
                    tint = Color.Black,
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
}

private fun timeStampToDuration(position: Long): String {
    val totalSecond = floor(position / 1E3).toInt()
    val minutes = totalSecond / 60
    val remainingSeconds = totalSecond - (minutes * 60)
    return if (position < 0) "--:--"
    else "%02d:%02d".format(minutes, remainingSeconds)
}
