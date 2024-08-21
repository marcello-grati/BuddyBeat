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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import coil.compose.AsyncImage
import com.example.buddybeat.R
import com.example.buddybeat.ui.CurrentSong

@Composable
fun HomeBottomBar(
    song: CurrentSong,
    onBarClick: () -> Unit,
    prevSong: () -> Unit,
    nextSong: () -> Unit,
    progress: Float,
    onProgress: (Float) -> Unit,
    isPlaying: Boolean,
    onStart: () -> Unit,
    incrementSpeed: () -> Unit,
    decrementSpeed: () -> Unit,
    favoriteContains: LiveData<Int>,
    addToFavorite: () -> Unit,
    removeFavorite: () -> Unit
) {

    var offsetX by remember { mutableFloatStateOf(0f) }

    AnimatedVisibility(
        visible = true,
        //modifier = modifier.height(100.dp)
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
                onProgress = onProgress,
                isPlaying = isPlaying,
                onStart = onStart,
                onNext = nextSong,
                incrementSpeed = incrementSpeed,
                decrementSpeed = decrementSpeed,
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
    onProgress: (Float) -> Unit,
    isPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit,
    incrementSpeed: () -> Unit,
    decrementSpeed: () -> Unit,
    favoriteContains: LiveData<Int>,
    addToFavorite: () -> Unit,
    removeFavorite: () -> Unit)
{
    val img by favoriteContains.observeAsState(initial = 0)
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            //add progress TODO
            /*Slider(
                value = progress,
                onValueChange = { onProgress(it) },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth()
            )*/
            LinearDeterminateIndicator(currentProgress = progress/100, color = Color.White, trackColor = Color.DarkGray)
            // add duration TODO
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF000000))
                .padding(vertical = 15.dp, horizontal = 5.dp)
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
                        text = if (song.title.length > 15) "${song.title.take(15)}..." else song.title,
                        color = Color.White, fontSize = 13.sp
                    )
                    Text(
                        text = if (song.artist.length > 15) "${song.artist.take(15)}..." else song.artist,
                        color = Color.LightGray, fontSize = 13.sp
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(
                    onClick = { decrementSpeed() }, modifier = Modifier
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
                    onClick = { incrementSpeed() }, modifier = Modifier
                        .background(
                            Color.White,
                            RoundedCornerShape(30.dp)
                        )
                        .size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase speed",
                        tint = Color.Black
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(modifier = Modifier.size(34.dp),onClick = {
                    if(img==0) {
                        addToFavorite()
                    }
                    else{
                        removeFavorite()
                    }
                }) {
                    Icon(
                        imageVector = if(img!=0) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
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
                /*Card(
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
                    }*/
            }
        }
    }
}


/*@Composable
fun HomeBottomBarItem(
    song: CurrentSong,
    onBarClick: () -> Unit,
    progress: Float,
    onProgress: (Float) -> Unit,
    isPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit,
    incrementSpeed: () -> Unit,
    decrementSpeed: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(100.dp)
            .clickable(onClick = { onBarClick() })

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalAlignment = Alignment.Start
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .height(20.dp)
            ) {
                //add progress TODO
                Slider(
                    value = progress,
                    onValueChange = { onProgress(it) },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
                // add duration TODO
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArtistInfo(title = song.title, artist = song.artist, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(10.dp))
                MediaPlayerController(
                    isAudioPlaying = isPlaying,
                    onStart = onStart,
                    onNext = onNext,
                    incrementSpeed = incrementSpeed,
                    decrementSpeed = decrementSpeed
                )
            }
        }
    }
}*/


/*@Composable
fun MediaPlayerController(
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit,
    incrementSpeed: () -> Unit,
    decrementSpeed: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerIconItem(
            icon = if (isAudioPlaying) Icons.Default.Pause
            else Icons.Default.PlayArrow
        ) {
            onStart()
        }
        Spacer(modifier = Modifier.size(8.dp))
        PlayerIconItem(
            icon = Icons.Default.SkipNext
        ) {
            onNext()
        }
        Spacer(modifier = Modifier.size(8.dp))
        Column {
            PlayerIconItem(
                icon = Icons.Default.Add,
                modifier = Modifier.size(30.dp)
            ) {
                incrementSpeed()
            }
            Spacer(modifier = Modifier.size(2.dp))
            PlayerIconItem(
                icon = Icons.Default.Remove,
                modifier = Modifier.size(30.dp)
            ) {
                decrementSpeed()
            }
        }
    }
}*/

/*@Composable
fun ArtistInfo(
    modifier: Modifier = Modifier,
    title: String,
    artist: String
) {
    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            modifier = Modifier.size(30.dp),
            contentDescription = null
        )

        Spacer(modifier = Modifier.size(10.dp))
        Column(modifier = Modifier.fillMaxWidth())
        {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Clip,
                maxLines = 1
            )
            Spacer(modifier = Modifier.size(0.dp))
            Text(
                text = artist,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.titleSmall,
                overflow = TextOverflow.Clip,
                maxLines = 1
            )
        }
    }
}*/

/*@Composable
fun PlayerIconItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    borderStroke: BorderStroke? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        border = borderStroke,
        modifier = modifier
            .clip(CircleShape)
            .clickable {
                onClick()
            },
        contentColor = color,
        color = backgroundColor
    ) {
        Box(
            modifier = modifier.padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }
    }
}*/

