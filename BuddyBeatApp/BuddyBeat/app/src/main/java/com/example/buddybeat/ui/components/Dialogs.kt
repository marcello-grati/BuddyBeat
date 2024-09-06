package com.example.buddybeat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.buddybeat.data.models.PlaylistWithSongs

@Composable
fun NewPlaylist(shouldShowDialog : MutableState<Boolean>, insertPlaylist : (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    if (shouldShowDialog.value) {
        Dialog(onDismissRequest = { shouldShowDialog.value = false  }) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFB1B2FF)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = { shouldShowDialog.value = false  },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Black
                            )
                        }
                    }
                    Text(
                        "GIVE YOUR PLAYLIST A NAME",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp), textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Playlist title", color = Color.DarkGray) },
                        singleLine = true,
                        modifier = Modifier.focusRequester(focusRequester),
                        textStyle = TextStyle(color = Color.DarkGray, fontSize = 17.sp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Black,
                            unfocusedIndicatorColor = Color.DarkGray,
                            cursorColor = Color.Black
                        )
                    )
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { shouldShowDialog.value = false
                            insertPlaylist(name)},
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("CREATE", color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun AreYouSure(
    shouldShowDialog: MutableState<Boolean>,
    onClick : () -> Unit,
    title : String,
    text : String
) {
    if (shouldShowDialog.value) {
        AlertDialog(
            icon = {
                Icon(Icons.Default.PlaylistRemove, contentDescription = "Remove from playlist")
            },
            title = {
                Text(text = title)
            },
            text = {
                Text(text = text)
            },
            onDismissRequest = {
                shouldShowDialog.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClick()//removeSong()
                        shouldShowDialog.value = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        shouldShowDialog.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun RenamePlaylist(
    shouldShowDialogFive: MutableState<Boolean>,
    updatePlaylist : (String) -> Unit,
    title : String
) {
    val focusRequester = remember { FocusRequester() }
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = title,
                selection = TextRange(title.length)
            )
        )
    }

    if (shouldShowDialogFive.value) {
        AlertDialog(
            icon = {
                Icon(Icons.Default.Edit, contentDescription = "Rename playlist")
            },
            title = {
                Text(text = "Rename Playlist:")
            },
            text = {
                TextField(
                    value = textFieldValueState,
                    onValueChange = { textFieldValueState = it },
                    placeholder = { Text("", color = Color.DarkGray) },
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester),
                    textStyle = TextStyle(color = Color.DarkGray, fontSize = 17.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Black,
                        unfocusedIndicatorColor = Color.DarkGray,
                        cursorColor = Color.Black
                    )
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            },
            onDismissRequest = {
                shouldShowDialogFive.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        updatePlaylist(textFieldValueState.text)
                        shouldShowDialogFive.value = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        shouldShowDialogFive.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddToPlaylist(
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {shouldShowDialogOne.value = true},
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242))
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
                }
            }
        }
    }
}