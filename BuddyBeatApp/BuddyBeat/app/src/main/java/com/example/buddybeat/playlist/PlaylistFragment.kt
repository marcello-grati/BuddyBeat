package com.example.buddybeat.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buddybeat.MainActivity
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.databinding.FragmentPlaylistBinding
import com.example.buddybeat.playlist.adapter.PlaylistAdapter

class PlaylistFragment(parentActivity: MainActivity) : Fragment() {

    private var adapter = PlaylistAdapter(parentActivity)
    private val viewModel: PlaylistViewModel by viewModels()

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.allSongs.observe(viewLifecycleOwner) {
            binding.songs.visibility = View.VISIBLE
            binding.songs.layoutManager = LinearLayoutManager(requireContext())
            adapter.setValues(viewModel.allSongs.value!!)
            binding.songs.adapter = adapter
        }
    }

    fun addMusic(mockSongs : ArrayList<Song>){
        for (item in mockSongs){
            viewModel.insert(item)
        }
    }

}