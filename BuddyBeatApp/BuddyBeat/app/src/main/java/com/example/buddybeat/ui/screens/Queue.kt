package com.example.buddybeat.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.ui.CurrentSong
import com.example.buddybeat.ui.components.QueueItem

/*Queue showing the next songs selected by the user and the next songs suggested by the system*/
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
            NowPlayingQueue(
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
                                        .fillMaxWidth().background(MaterialTheme.colorScheme.background)
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
fun NowPlayingQueue(
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
            Text(text = if(audio.bpm!=0 && audio.bpm!=-1) audio.bpm.toString() else "-")
        }
    }
}