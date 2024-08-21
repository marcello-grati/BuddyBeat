package com.example.buddybeat.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.buddybeat.data.models.Song

@Composable
fun FilledCardExample(
    stepFreq: String,
    bpm: String,
    ratio: String
) {
    Row {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier
                .size(width = 100.dp, height = 100.dp)
                .padding(top = 5.dp, bottom = 5.dp),
        ) {
            Text(
                text = "StepFreq: \n $stepFreq",
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .wrapContentHeight(),
                textAlign = TextAlign.Center,
            )
        }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier
                .size(width = 100.dp, height = 100.dp)
                .padding(top = 5.dp, bottom = 5.dp, start = 5.dp, end = 5.dp),
        ) {
            Text(
                text = "Bpm: \n $bpm",
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .wrapContentHeight(),
                textAlign = TextAlign.Center,
            )
        }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier
                .size(width = 100.dp, height = 100.dp)
                .padding(top = 5.dp, bottom = 5.dp),
        ) {
            Text(
                text = "Ratio: \n $ratio",
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .wrapContentHeight(),
                textAlign = TextAlign.Center,
            )
        }
    }
}


@Composable
fun Queue(
    audioList: List<Song>,
    onNavigateUp : () -> Unit
){
    Scaffold (topBar = {
        IconButton(onClick = { onNavigateUp()} ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "",
                modifier = Modifier.size(30.dp)
            )
        }
    }){
        /*Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {*/

        Column {
            LazyColumn(
                contentPadding = it,
                modifier = Modifier.weight(1.0f),
            ) {
                itemsIndexed(audioList) { index, audio ->
                    QueueItem(
                        audio = audio
                    )
                }
            }
        }
    }
}