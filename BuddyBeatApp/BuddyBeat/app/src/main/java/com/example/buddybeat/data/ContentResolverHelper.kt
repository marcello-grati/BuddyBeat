package com.example.buddybeat.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.WorkerThread
import com.example.buddybeat.data.models.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ContentResolverHelper @Inject
constructor(@ApplicationContext val context: Context) {
    private var mCursor: Cursor? = null

    private val projection: Array<String> = arrayOf(
        MediaStore.Audio.AudioColumns._ID,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.Audio.AudioColumns.DURATION,
        MediaStore.Audio.AudioColumns.TITLE
    )

    //private var selectionClause: String? = "${MediaStore.Audio.AudioColumns.IS_MUSIC} != 0"
    // if we want to remove "<unknown>" artist
    private var selectionClause: String? = "${MediaStore.Audio.AudioColumns.IS_MUSIC} != 0" + " AND ${MediaStore.Audio.AudioColumns.ARTIST} != '<unknown>'"

    private val sortOrder = "${MediaStore.Audio.AudioColumns.DISPLAY_NAME} ASC"


    @WorkerThread
    fun getAudioData(): List<Song> {
        return getCursorData()
    }


    private fun getCursorData(): MutableList<Song> {
        val audioList = mutableListOf<Song>()

        mCursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selectionClause,
            null,
            sortOrder
        )


        mCursor?.use { cursor ->
            val idColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
            val artistColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
            val titleColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)

            cursor.apply {
                if (count == 0) {
                    Log.e("Cursor", "getCursorData: Cursor is Empty")
                } else {
                    while (cursor.moveToNext()) {
                        val id = getLong(idColumn)
                        val artist = getString(artistColumn)
                        val duration = getInt(durationColumn)
                        val title = getString(titleColumn)
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        audioList += Song(
                            title = title, artist = artist, duration = duration,
                            uri = uri.toString(), description = "$title $artist"
                        )

                    }
                }
            }
        }
        return audioList
    }
}