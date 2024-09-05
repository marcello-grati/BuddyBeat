package com.example.buddybeat.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun FilledCardExample(
    stepFreq: String,
    bpm: String,
    ratio: String,
    colorUI : Color,
    mode : Long
) {
    Row {
        val color = colorUI.copy(alpha = 0.9f)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = color
            ),
            modifier = Modifier
                .size(width = 100.dp, height = 60.dp)
                .padding(top = 5.dp, bottom = 5.dp),
        ) {
            Text(
                text = if(mode==0L)"SPM:\n-" else "SPM:\n$stepFreq",
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
                text = "BPM:\n${if(bpm!="0" && bpm!="-1") bpm else "-"}",
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
                text = "Ratio:\n$ratio",
                modifier = Modifier
                    .width(100.dp)
                    .height(60.dp)
                    .wrapContentHeight(),
                textAlign = TextAlign.Center,
            )
        }
    }
}