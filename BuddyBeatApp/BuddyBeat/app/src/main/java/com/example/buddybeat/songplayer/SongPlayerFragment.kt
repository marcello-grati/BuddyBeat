package com.example.buddybeat.songplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.Snackbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.buddybeat.MainActivity
import com.example.buddybeat.MainFragment
import com.example.buddybeat.MainViewModel
import com.example.buddybeat.R
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.databinding.FragmentSongPlayerBinding
import kotlinx.coroutines.runBlocking

private const val ARG_PARAM1 = "songId"


class SongPlayerFragment(val parentActivity : MainActivity) : Fragment() {
    private var songId: Long = 0
    private lateinit var song : Song
    //private val songs : LiveData<MutableList<Song>> = songRepo.getAllSongs()

    private val viewModel : MainViewModel by viewModels()

    private var _binding: FragmentSongPlayerBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            songId = it.getLong(ARG_PARAM1)
        }
        song = runBlocking {viewModel.findSongById(songId)}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongPlayerBinding.inflate(inflater, container, false)
        binding.textSongTitle.text = song.title.toString()
        binding.textSongArtist.text = song.artist.toString()
        binding.backButton.setOnClickListener {
            goBack()
        }
        binding.buttonPlay.setOnClickListener {
            play()
        }
        binding.buttonNext.setOnClickListener {
            next()
        }
        binding.buttonPrevious.setOnClickListener {
            previous()
        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(parentActivity: MainActivity, param1: Long) =
            SongPlayerFragment(parentActivity).apply {
                arguments = Bundle().apply {
                    putLong(ARG_PARAM1, param1)
                }
            }
    }

    private fun goBack() {
        parentActivity.supportFragmentManager.popBackStack()
    }

    private fun play(){
        Toast.makeText(context, "play $song", Toast.LENGTH_SHORT).show()
    }

    private fun next(){
        Toast.makeText(context, "next", Toast.LENGTH_SHORT).show()
    }

    private fun previous(){
        Toast.makeText(context, "previous", Toast.LENGTH_SHORT).show()
    }
}