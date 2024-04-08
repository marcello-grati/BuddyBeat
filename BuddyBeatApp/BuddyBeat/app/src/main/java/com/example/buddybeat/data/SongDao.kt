package com.example.buddybeat.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.buddybeat.data.models.Playlist
import com.example.buddybeat.data.models.PlaylistSongCrossRef
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.data.models.Song

@Dao
interface SongDao {

    @Query("SELECT * FROM song_table ORDER BY title ASC")
    fun getAllSongs(): LiveData<MutableList<Song>>

    @Query("SELECT songId FROM song_table")
    fun getAllIds(): List<Long>

    @Query("SELECT * FROM playlist_table")
    fun getAllPlaylists(): LiveData<MutableList<Playlist>>

    @Transaction
    @Query("SELECT * FROM song_table JOIN playlistsongcrossref ON song_table.songId = playlistsongcrossref.songId WHERE playlistsongcrossref.playlistId=:id ORDER BY title ASC")
    fun getSongsFromPlaylist(id:Long): LiveData<MutableList<Song>>

    @Transaction
    @Query("SELECT * FROM playlist_table")
    fun getPlaylistsWithSongs(): LiveData<MutableList<PlaylistWithSongs>>

    @Query("SELECT * FROM song_table WHERE songId LIKE :id")
    suspend fun findSongById(id: Long): Song

    @Query("SELECT * FROM playlist_table WHERE playlistId LIKE :id")
    suspend fun findPlaylistById(id: Long): Playlist

    @Query("SELECT songId FROM song_table WHERE uri LIKE :uri")
    suspend fun getIdSong(uri: String): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(song: Song)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSong(song: Song) : Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(ref: PlaylistSongCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: Playlist) : Long

    @Delete
    suspend fun delete(song: Song)

    @Delete
    suspend fun delete(playlist: Playlist)

    @Delete
    suspend fun delete(ref: PlaylistSongCrossRef)

    /*
    * TODO : add useful queries
    */
}