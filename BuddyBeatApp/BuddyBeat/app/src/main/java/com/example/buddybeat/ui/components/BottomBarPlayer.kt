package com.example.buddybeat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.buddybeat.ui.CurrentSong

@Composable
fun HomeBottomBar(
    modifier: Modifier = Modifier,
    song: CurrentSong,
    onBarClick: () -> Unit,
    prevSong: () -> Unit,
    nextSong: () -> Unit,
    progress: Float,
    onProgress: (Float) -> Unit,
    isPlaying: Boolean,
    onStart: () -> Unit,
    incrementSpeed: () -> Unit,
    decrementSpeed: () -> Unit
) {

    var offsetX by remember { mutableFloatStateOf(0f) }

    AnimatedVisibility(
        visible = true,
        modifier = modifier.height(100.dp)
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
            HomeBottomBarItem(
                song = song,
                onBarClick = onBarClick,
                progress = progress,
                onProgress = onProgress,
                isPlaying = isPlaying,
                onStart = onStart,
                onNext = nextSong,
                incrementSpeed = incrementSpeed,
                decrementSpeed = decrementSpeed
            )
        }
    }
}


@Composable
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
}


@Composable
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
}

@Composable
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
}

@Composable
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
}