package com.example.buddybeat.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.ui.CurrentSong

@Composable
fun FilledCardExample(
    stepFreq: String,
    bpm: String,
    ratio: String
) {
    Row {
        val color = Color(0xFF82B0E6)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = color
            ),
            modifier = Modifier
                .size(width = 100.dp, height = 60.dp)
                .padding(top = 5.dp, bottom = 5.dp),
        ) {
            Text(
                text = "StepFreq: \n $stepFreq",
                modifier = Modifier
                    .width(100.dp)
                    .height(60.dp)
                    .wrapContentHeight(),
                textAlign = TextAlign.Center,
            )
        }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = color
            ),
            modifier = Modifier
                .size(width = 100.dp, height = 60.dp)
                .padding(top = 5.dp, bottom = 5.dp, start = 5.dp, end = 5.dp),
        ) {
            Text(
                text = "Bpm: \n $bpm",
                modifier = Modifier
                    .width(100.dp)
                    .height(60.dp)
                    .wrapContentHeight(),
                textAlign = TextAlign.Center,
            )
        }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = color
            ),
            modifier = Modifier
                .size(width = 100.dp, height = 60.dp)
                .padding(top = 5.dp, bottom = 5.dp),
        ) {
            Text(
                text = "Ratio: \n $ratio",
                modifier = Modifier
                    .width(100.dp)
                    .height(60.dp)
                    .wrapContentHeight(),
                textAlign = TextAlign.Center,
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Queue(
    queue : SnapshotStateList<Song>,
    audioList: SnapshotStateList<Song>,
    removeFromQueue1 : (Song) -> Unit,
    removeFromQueue2 : (Song) -> Unit,
    showQueue : (Boolean) -> Unit,
    currentSong : CurrentSong,
    onNavigateUp : () -> Unit
){
    BackHandler(enabled = true) {
        onNavigateUp()
    }
    Scaffold (topBar = {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart
        ) {
            IconButton(onClick = { onNavigateUp() }, ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "",
                    modifier = Modifier.size(30.dp)
                )
            }
            /*Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Absolute.Center) {
                Text(
                    text = "QUEUE",
                    color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
            }*/
        }
    }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxHeight() )
        {
            Text(
                "Now Playing:",
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 15.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            NowPlaying(
                audio = currentSong
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column (
                modifier = Modifier.padding(horizontal = 10.dp)
            ){
                LazyColumn {
                    arrayOf(queue, audioList).forEach { section ->
                        stickyHeader {
                            if (section.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                ) {
                                    Text(
                                        text = if (section == queue) "Next in queue selected by you:" else "Next in queue:",
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 5.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                        itemsIndexed(section) { index, audio ->
                            QueueItem(
                                audio = audio,
                                removeFromQueue = if (section == queue) removeFromQueue1 else removeFromQueue2,
                                showQueue = showQueue
                            )
                        }
                        item{ Spacer(modifier = Modifier.height(10.dp)) }
                    }
                }
            }
        }
    }
}


@Composable
fun NowPlaying(
    audio: CurrentSong
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFCDFDFD), shape = RoundedCornerShape(8.dp))
                .padding(vertical = 10.dp, horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "now playing", modifier = Modifier.size(25.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = if (audio.title.length > 22) "${audio.title.take(22)}..." else audio.title,
                    color = Color.Black, fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (audio.artist.length > 22) "${audio.artist.take(22)}..." else audio.artist,
                    color = Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = audio.bpm.toString())
        }
    }
}