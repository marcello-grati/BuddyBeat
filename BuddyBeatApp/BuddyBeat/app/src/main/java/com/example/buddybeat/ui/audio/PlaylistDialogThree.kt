package com.example.buddybeat.ui.audio

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp

@Composable
fun DialogThree(
    shouldShowDialogThree: MutableState<Boolean>,
    removeSong : () -> Unit
) {
    if (shouldShowDialogThree.value) {
        AlertDialog(
            icon = {
                Icon(Icons.Default.PlaylistRemove, contentDescription = "Remove from playlist")
            },
            title = {
                Text(text = "Remove from playlist")
            },
            text = {
                Text(text = "Do you want to remove the song from the playlist?")
            },
            onDismissRequest = {
                shouldShowDialogThree.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        removeSong()
                        shouldShowDialogThree.value = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        shouldShowDialogThree.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun DialogFour(
    shouldShowDialogFour: MutableState<Boolean>,
    removePlaylist : () -> Unit
) {
    if (shouldShowDialogFour.value) {
        AlertDialog(
            icon = {
                Icon(Icons.Default.PlaylistRemove, contentDescription = "Remove from playlist")
            },
            title = {
                Text(text = "Remove playlist")
            },
            text = {
                Text(text = "Do you want to remove the playlist?")
            },
            onDismissRequest = {
                shouldShowDialogFour.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        removePlaylist()
                        shouldShowDialogFour.value = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        shouldShowDialogFour.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DialogFive(
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
                Icon(Icons.Default.PlaylistRemove, contentDescription = "Remove from playlist")
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