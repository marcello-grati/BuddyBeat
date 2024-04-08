package com.example.buddybeat.playlist

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybeat.InsertPlaylistFragment
import com.example.buddybeat.MainActivity
import com.example.buddybeat.data.models.Playlist
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.databinding.FragmentPlaylistBinding
import com.example.buddybeat.playlist.adapter.PlaylistAdapter
import com.example.buddybeat.songplayer.SongPlayerFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking

private const val ARG_PARAM1 = "playlistId"


class PlaylistFragment(parentActivity: MainActivity) : Fragment() {
    private var playlistId: Long = 0
    private lateinit var playlist: Playlist
    private val PICK_AUDIO = 2
    //private val songs : LiveData<MutableList<Song>> = songRepo.getAllSongs()

    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
        val item = v.tag as Song
        Snackbar.make(v, "${item.title}", Snackbar.LENGTH_SHORT).show()
        val songFragment = SongPlayerFragment.newInstance(parentActivity, item.songId)
        parentActivity.replaceFragment(songFragment, "song")
    }

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private var adapter = PlaylistAdapter(onClickListener)

    val viewModel: PlaylistViewModel by viewModels {
        PlaylistViewModel.PlaylistViewModelFactory(
            parentActivity.application,
            playlistId
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistId = it.getLong(ARG_PARAM1)
        }
        playlist = runBlocking { viewModel.findPlaylistById(playlistId) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        binding.albumTitle.text = playlist.title.toString()
        binding.albumAutor.text = playlist.description.toString()
        binding.fabsong.setOnClickListener {
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
            insertNewSong()
        }
        viewModel.playlist.observe(viewLifecycleOwner) {
            binding.songs.visibility = View.VISIBLE
            binding.songs.layoutManager = LinearLayoutManager(requireContext())
            adapter.setValues(viewModel.playlist.value!!)
            binding.songs.adapter = adapter
        }
        val mIth = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val deleted = viewHolder.itemView.tag as Song
                    viewModel.deleteSong(deleted)
                    Snackbar.make(binding.songs, "Song removed", Snackbar.LENGTH_LONG)
                        .setAction("Cancel", View.OnClickListener {
                            viewModel.reInsertInPlaylist(deleted)
                        }).show()
                }
            }).attachToRecyclerView(binding.songs)
        return binding.root
    }

    private fun insertNewSong() {
        //val filesIntent = Intent(Intent.ACTION_GET_CONTENT)
        //filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        //filesIntent.setType("audio/*") //use image*/ for photos, etc.

        //startActivityForResult(filesIntent, REQUEST_CODE_FOR_ON_ACTIVITY_RESULT)
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        try {
            startActivityForResult(intent, PICK_AUDIO)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No Activity found to pick audio", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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

    private fun obtainData(urii: Uri?) {

        val audioUri: Uri? = urii
        val contentResolver: ContentResolver? = context?.contentResolver
        if (audioUri != null) {
            try {
                val projection = arrayOf(
                    MediaStore.Files.FileColumns.TITLE,
                    MediaStore.Audio.Artists.ARTIST,
                    MediaStore.Audio.Media.DURATION
                )
                val cursor: Cursor? = contentResolver?.query(audioUri, projection, null, null, null)
                //Log.d("cursor", cursor?.columnNames)
                if (cursor?.moveToFirst() == true) {
                    val songTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val songArtist = cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)
                    val songDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                    val title = cursor.getString(songTitle)
                    var artist = cursor.getString(songArtist)
                    var duration = cursor.getLong(songDuration)
                    if (artist == "<unknown>")
                        artist = "Unknown Artist"
                    val uri = audioUri.toString()
                    cursor.close()
                    val song = Song(
                        title = title,
                        artist = artist,
                        uri = uri,
                        description = "$title $artist",
                        duration = duration.toString()
                    )
                    viewModel.insertInPlaylist(song)
                    Toast.makeText(context, song.toString(), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No file audio", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Impossible to retrieve file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Impossible to retrieve file", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(parentActivity: MainActivity, param1: Long) =
            PlaylistFragment(parentActivity).apply {
                arguments = Bundle().apply {
                    putLong(ARG_PARAM1, param1)
                }
            }
    }
}