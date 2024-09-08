package com.example.buddybeat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddybeat.R

/*HELP SCREEN*/
@Composable
fun HelpScreen(onNavigateUp: () -> Unit) {
    Scaffold() {
        Column(Modifier.background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIos,
                        contentDescription = "back",
                    )
                }
            }
            Text(
                text = "Help & Instructions",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 15.dp)
            )
            // Introduction to the App
            Text(
                text = stringResource(R.string.help_intro),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 15.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .padding(it),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Explanation of the BPM
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E8E5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "BPM (Beats Per Minute)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.help_bpm),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                // Explanation of the + and - buttons
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E8E5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "+ and - Buttons",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.help_buttons),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {},
                                    enabled = false,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        disabledContentColor = Color.Black
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RemoveCircleOutline,
                                        contentDescription = "Minus",
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {},
                                    enabled = false,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        disabledContentColor = Color.Black
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircleOutline,
                                        contentDescription = "Plus",
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                // Explanation of the Manual/Auto Mode Button
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E8E5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Manual/Auto Mode Buttons",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.help_auto),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            // Sample Manual and Auto Buttons
                            Row(
                                modifier = Modifier
                                    .padding(top = 16.dp) // Increased space between the buttons
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(text = "Manual", color = Color.White)
                                }
                                Button(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(text = "Auto", color = Color.White)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}


