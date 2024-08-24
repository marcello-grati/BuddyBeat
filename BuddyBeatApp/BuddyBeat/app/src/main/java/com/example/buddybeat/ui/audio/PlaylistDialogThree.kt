package com.example.buddybeat.ui.audio

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

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