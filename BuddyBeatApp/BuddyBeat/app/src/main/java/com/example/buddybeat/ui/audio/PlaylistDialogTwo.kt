package com.example.buddybeat.ui.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.LiveData
import com.example.buddybeat.data.models.PlaylistWithSongs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogTwo(
    shouldShowDialogTwo: MutableState<Boolean>,
    shouldShowDialogOne: MutableState<Boolean>,
    allPlaylist: List<PlaylistWithSongs>,
    insertPlaylist: (Long) -> Unit
) {
    if (shouldShowDialogTwo.value) {
        Dialog(onDismissRequest = { shouldShowDialogTwo.value = false }) {
            Box(
                modifier = Modifier
                    .background(Color.Black, shape = RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { shouldShowDialogTwo.value = false }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            "ADD TO PLAYLIST",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        // Placeholder to maintain centering
                        Spacer(modifier = Modifier.width(24.dp))
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {shouldShowDialogOne.value = true},
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1551E9))
                    ) {
                        Text("NEW PLAYLIST", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.height(120.dp)) {
                        itemsIndexed(allPlaylist.drop(1)) { index, playlist ->
                            Button(
                                onClick = {
                                    insertPlaylist(playlist.playlist.playlistId)
                                    shouldShowDialogTwo.value = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),  // Taller height for a more rectangular shape
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFC4E0E4
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)  // More rectangular
                            ) {
                                Text(playlist.playlist.title, color = Color.Black)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    /*Button(
                        onClick = { /* Handle favorites */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),  // Taller height for a more rectangular shape
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)  // More rectangular
                    ) {
                        Text("FAVORITES", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { /* Handle custom playlist */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),  // Taller height for a more rectangular shape
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)  // More rectangular
                    ) {
                        Text("CUSTOM PLAYLIST", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(18.dp))*/

                    /*Button(
                        onClick = { shouldShowDialog.value = false },
                        modifier = Modifier.fillMaxWidth(0.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6FA5E6))
                    ) {
                        Text("DONE", color = Color.White)
                    }*/
                }
            }
        }
    }
}

/*@Preview(showBackground = true)
@Composable
fun PreviewDialogTwo() {
    DialogTwo(handleCloseDialog = {})
}*/
