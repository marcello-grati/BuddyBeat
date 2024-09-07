package com.example.buddybeat.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.buddybeat.R
import com.example.buddybeat.data.models.Song

/*Song Item in the Queue*/
@Composable
fun QueueItem(
    audio: Song,
    removeFromQueue : (Song) -> Unit,
    showQueue : (Boolean) -> Unit
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
                Text(text = if(audio.bpm!=0 && audio.bpm!=-1) audio.bpm.toString() else "  -  ")
            }
            var expanded by remember { mutableStateOf(false) }
            IconButton(onClick = {
                expanded = !expanded
                showQueue(!expanded)
            }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.Black
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded=false
                        showQueue(true)},
                    modifier = Modifier.background(Color(0xFFF0F0F0))
                ){
                    DropdownMenuItem(
                        text = { Text("Remove From Queue") },
                        onClick = {
                            removeFromQueue(audio)
                            expanded=false
                        }
                    )
                }
            }
        }
    }
}