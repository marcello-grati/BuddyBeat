package com.example.buddybeat

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.databinding.MainActivityBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding
    private val PERMISSION_REQUEST : Int = 1
    //private val PICK_AUDIO = 2

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        get_all_songs()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container,
                MainFragment(this),"main").commit()
        //replaceFragment(MainFragment(this),"main")
    }

    fun replaceFragment(fragment: Fragment, tag: String?) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment, tag)
        transaction.addToBackStack(tag)
        transaction.commit()
    }


    private fun get_all_songs(){
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST)
            else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST)
        else addMusic()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_REQUEST->{
                if(grantResults.size>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                        addMusic()
                    }
                }
                else {
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addMusic() {
        val songList = ArrayList<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM
        )
        val selection = "${MediaStore.Audio.Media.ARTIST} != ?"
        val selectionArgs = arrayOf("<unknown>")
        applicationContext.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs, null).use { cursor ->
            val id =  cursor!!.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val title = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artist =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                val id = cursor.getLong(id)
                val title = cursor.getString(title)
                val artist = cursor.getString(artist)
                val duration = cursor.getInt(durationColumn)
                val album = cursor.getString(albumColumn)

                val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id)
                // Stores column values and the contentUri in a local object
                // that represents the media file.
                songList += Song(title=title, artist = artist, duration = duration.toString(), uri=contentUri.toString(), description = album)
            }
        }
        add(songList)
    }

    private fun add(songs:ArrayList<Song>) {
        viewModel.addMusic(songs)
    }

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(applicationContext).inflate(R.menu.menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }*/


    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add -> {
                if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST)
                    else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST)
                //if permission is granted
                else addMusic()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }*/

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add -> {
                create_playlist()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }*/

    /*private fun create_playlist(){
        viewModel.insert(Playlist(111L, "tutte", "tutte le canzoni"))
        viewModel.setPlaylist(111L)
        for (i in viewModel.getAllSongs().value!!) {
            val pl = PlaylistSongCrossRef(playlistId = 111L, songId = i.songId)
            viewModel.insert(pl)
        }
    }*/

    /*
    private fun addMusic(){
        val filesIntent = Intent(Intent.ACTION_GET_CONTENT)
        filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        filesIntent.setType("audio/*") //use image*/ for photos, etc.

        //startActivityForResult(filesIntent, REQUEST_CODE_FOR_ON_ACTIVITY_RESULT)
        //val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        try {
            startActivityForResult(filesIntent, PICK_AUDIO)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(applicationContext, "No Activity found to pick audio", Toast.LENGTH_SHORT).show()
        }
    }*/

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) { //se l'operazione è andata a buon fine
            if (requestCode == PICK_AUDIO) { //se il requestCode è PICK_AUDIO
                if (null != data) {
                    if (null != data.clipData) {
                        for (i in 0 until data.clipData!!.itemCount) {
                            val uri = data.clipData!!.getItemAt(i).uri
                            obtainData(uri)
                        }
                    } else {
                        val uri = data.data
                        obtainData(uri)
                    }
                }
            }
        }
    }

    private fun obtainData(urii: Uri?){

                val audioUri: Uri? = urii
                val contentResolver : ContentResolver = contentResolver
                if (audioUri != null) {
                    try {
                        val projection = arrayOf(MediaStore.Files.FileColumns.TITLE, MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Media.DURATION)
                        val cursor: Cursor? = contentResolver.query(audioUri, projection, null, null, null)
                        if(cursor?.moveToFirst() == true) {
                            val songTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                            val songArtist =  cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)
                            val durata =  cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                            val titolo = cursor.getString(songTitle)
                            var artista = cursor.getString(songArtist)
                            if(artista=="<unknown>")
                                artista = "Unknown Artist"
                            val uri = audioUri.toString()
                            cursor.close()
                            val song = Song(title=titolo, artist = artista, uri = uri, description = "$titolo $artista", duration = durata.toString())
                            add(song)
                            Toast.makeText(applicationContext, "Song added", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(applicationContext, "No file audio", Toast.LENGTH_SHORT).show()
                        }
                    }
                    catch (e: Exception) {
                        Toast.makeText(applicationContext,"Impossible to retrieve file", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(applicationContext, "Impossible to retrieve file", Toast.LENGTH_SHORT).show()
                }
            }*/


}