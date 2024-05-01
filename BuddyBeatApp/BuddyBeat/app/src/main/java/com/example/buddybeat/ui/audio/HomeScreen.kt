package com.example.buddybeat.ui.audio

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.ui.components.AudioItem

@Composable
fun HomeScreen(
    onItemClick: (Int) -> Unit,
    audioList: List<Song>,
) {
    Scaffold {
        LazyColumn(
            contentPadding = it
        ) {
            itemsIndexed(audioList) { index, audio ->
                AudioItem(
                    audio = audio,
                    onItemClick = {
                        onItemClick(index)
                    }
                )
            }
        }
    }
}