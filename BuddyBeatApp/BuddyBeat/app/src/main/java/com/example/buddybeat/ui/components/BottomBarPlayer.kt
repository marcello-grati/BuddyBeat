package com.example.buddybeat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import coil.compose.AsyncImage
import com.example.buddybeat.R
import com.example.buddybeat.ui.CurrentSong

/*BottomBarPlayer*/
@Composable
fun HomeBottomBar(
    song: CurrentSong,
    onBarClick: () -> Unit,
    prevSong: () -> Unit,
    nextSong: () -> Unit,
    progress: Float,
    isPlaying: Boolean,
    onStart: () -> Unit,
    favoriteContains: LiveData<Int>,
    addToFavorite: () -> Unit,
    removeFavorite: () -> Unit
) {

    var offsetX by remember { mutableFloatStateOf(0f) }

    AnimatedVisibility(
        visible = true
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > 0 -> {
                                    prevSong()
                                }

                                offsetX < 0 -> {
                                    nextSong()
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val (x, _) = dragAmount
                            offsetX = x
                        }
                    )

                }
                .background(
                    MaterialTheme.colorScheme.background
                ),
        ) {
            NowPlaying(
                song = song,
                onBarClick = onBarClick,
                progress = progress,
                isPlaying = isPlaying,
                onStart = onStart,
                favoriteContains = favoriteContains,
                addToFavorite = addToFavorite,
                removeFavorite = removeFavorite
            )
        }
    }
}

@Composable
fun NowPlaying(
    song: CurrentSong,
    onBarClick: () -> Unit,
    progress: Float,
    isPlaying: Boolean,
    onStart: () -> Unit,
    favoriteContains: LiveData<Int>,
    addToFavorite: () -> Unit,
    removeFavorite: () -> Unit
) {
    val img by favoriteContains.observeAsState(initial = 0)
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            LinearDeterminateIndicator(
                currentProgress = progress / 100,
                color = Color.White,
                trackColor = Color.DarkGray
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF000000))
                .padding(vertical = 15.dp, horizontal = 15.dp)
                .clickable {
                    onBarClick()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row() {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x00ffffff)),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "",
                            contentDescription = "Now Playing",
                            placeholder = painterResource(id = R.drawable.musicicon1),
                            error = painterResource(id = R.drawable.musicicon1),
                            modifier = Modifier.width(50.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(Modifier.align(Alignment.CenterVertically)) {
                    Text(
                        text = if (song.title.length > 20) "${song.title.take(20)}..." else song.title,
                        color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (song.artist.length > 20) "${song.artist.take(20)}..." else song.artist,
                        color = Color.LightGray, fontSize = 15.sp
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(modifier = Modifier.size(34.dp), onClick = {
                    if (img == 0) {
                        addToFavorite()
                    } else {
                        removeFavorite()
                    }
                }) {
                    Icon(
                        imageVector = if (img != 0) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Add to favorites",
                        tint = Color.White,
                    )
                }
                IconButton(
                    onClick = { onStart() },
                    modifier = Modifier
                        .background(
                            Color.White,
                            RoundedCornerShape(30.dp)
                        )
                        .size(30.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause
                        else Icons.Default.PlayArrow,
                        contentDescription = "PlayPause",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}