package com.example.buddybeat.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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