package com.example.buddybeat.data.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

/*Entities of Room Database*/
@Entity(tableName = "song_table", indices = [Index(value=["uri"], unique = true)])
data class Song(
    @PrimaryKey(autoGenerate = true)
    val songId : Long = 0,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "artist")
    val artist: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "duration")
    val duration: Int,
    @ColumnInfo(name = "uri")
    val uri: String,
    @ColumnInfo(name = "bpm")
    var bpm: Int = -1)
{
    override fun toString(): String = "$title - $artist"
}


@Entity(tableName = "playlist_table")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val playlistId : Long = 0,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String)
{
    override fun toString(): String = title + "\n"
}

@Entity(primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long
)

data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "songId",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val songs: MutableList<Song>
)