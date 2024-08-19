package com.example.buddybeat.ui.audio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.ui.CurrentSong
import com.example.buddybeat.ui.components.FilledCardExample
import com.example.buddybeat.ui.components.HomeBottomBar
import com.example.buddybeat.ui.components.LinearDeterminateIndicator
import com.example.buddybeat.ui.components.SongItem

@Composable
fun PlaylistScreen(
    onItemClick: (Int) -> Unit,
    audioList: List<Song>,
    loading: Boolean,
    currentProgress: Float,
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
    text1: String,
    text2: String,
    text3: String,
    onNavigateUp: () -> Unit
) {
    Scaffold (
        bottomBar = {
            if(song.title!="")
                HomeBottomBar(
                    song = song,
                    onBarClick = onBarClick,
                    prevSong = prevSong,
                    nextSong = nextSong,
                    progress = progress,
                    onProgress = onProgress,
                    isPlaying = isPlaying,
                    onStart = onStart,
                    incrementSpeed = incrementSpeed,
                    decrementSpeed = decrementSpeed
                )
    }){
        Column {
            if(!loading)
                LinearDeterminateIndicator(currentProgress, Color.Black, Color.White)
            Row (modifier = Modifier.align(Alignment.CenterHorizontally)){
                FilledCardExample(text1, text2, text3)
            }
            LazyColumn(
                contentPadding = it,
                modifier = Modifier.weight(1.0f),
            ) {
                itemsIndexed(audioList) { index, audio ->
                    SongItem(
                        audio = audio,
                        onItemClick = {
                            onItemClick(index)
                        },
                        isPlaying = isPlaying
                    )
                }
            }
        }
    }
}