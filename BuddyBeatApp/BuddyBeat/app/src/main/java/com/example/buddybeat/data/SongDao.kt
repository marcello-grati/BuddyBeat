package com.example.buddybeat.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.buddybeat.data.models.Song

@Dao
interface SongDao {

    @Query("SELECT * FROM song_table ORDER BY title ASC")
    fun getAll(): LiveData<MutableList<Song>>

    @Query("SELECT * FROM song_table WHERE id LIKE :id")
    fun findSongById(id: Int): Song

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(song: Song)

    @Delete
    suspend fun delete(song: Song)
}