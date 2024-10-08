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

/*Data Access Object*/
@Dao
interface SongDao {

    @Query("SELECT bpm FROM song_table WHERE songId LIKE :id")
    fun getBpm(id: Long): Int

    @Query("SELECT COUNT(*) FROM song_table")
    fun getItemCount(): LiveData<Int>

    @Query("UPDATE song_table SET bpm = :bpm WHERE songId = :id")
    suspend fun updateBpm(id: Long, bpm: Int)

    @Query("SELECT * FROM song_table ORDER BY title ASC")
    suspend fun getSongs(): List<Song>

    @Query("SELECT COUNT(*) FROM song_table WHERE bpm = -1")
    fun getCountBpm(): LiveData<Int>

    @Transaction
    @Query("SELECT * FROM playlist_table")
    fun getPlaylistsWithSongs(): LiveData<MutableList<PlaylistWithSongs>>

    @Query("SELECT COUNT(*) FROM PlaylistSongCrossRef WHERE playlistId LIKE :idPlaylist AND songId LIKE :idSong")
    fun containsSong(idPlaylist: Long, idSong: Long): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(song: Song): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(ref: PlaylistSongCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Delete
    suspend fun delete(song: Song)

    @Delete
    suspend fun delete(playlist: Playlist)

    @Delete
    suspend fun delete(ref: PlaylistSongCrossRef)

    @Query("UPDATE playlist_table SET title=:title WHERE playlistId = :id")
    suspend fun update(title: String, id: Long)
}