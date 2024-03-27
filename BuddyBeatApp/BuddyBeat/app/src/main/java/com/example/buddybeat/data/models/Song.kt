package com.example.buddybeat.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "song_table") //, indices = [Index(value=["uri"], unique = true)])
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    @ColumnInfo(name = "title")
    val title: String?,
    @ColumnInfo(name = "artist")
    val artist: String?,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "duration")
    val duration: String?,
    @ColumnInfo(name = "uri")
    val uri: String?)
{
    override fun toString(): String = title + "\n" + artist
}