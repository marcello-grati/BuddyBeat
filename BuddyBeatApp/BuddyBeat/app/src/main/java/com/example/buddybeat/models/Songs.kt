package com.example.buddybeat.models

data class SongsList(
    var songs: List<Song>
)

data class Song(
    val number: Int,
    val name: String,
    val description: String,
    val author: String,
    val duration: String
)