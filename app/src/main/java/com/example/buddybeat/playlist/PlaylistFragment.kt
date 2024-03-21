package com.example.buddybeat.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buddybeat.MainActivity
import com.example.buddybeat.databinding.FragmentPlaylistBinding
import com.example.buddybeat.playlist.adapter.PlaylistAdapter
import com.example.buddybeat.songplayer.SongPlayerFragment

class PlaylistFragment : Fragment(), PlaylistAdapter.OnItemClickListener {
    private  var adapter = PlaylistAdapter(arrayListOf(),this@PlaylistFragment)
    private lateinit var viewModel : PlaylistViewModel

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(PlaylistViewModel::class.java)

        binding.songs.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getSongs()
        observeViewModel()
    }

    private fun observeViewModel(){
        viewModel.songs.observe(viewLifecycleOwner) {
            viewModel.songs.value?.let {
                binding.songs.visibility = View.VISIBLE
                adapter.update(viewModel.songs.value!!)
                binding.songs.layoutManager = LinearLayoutManager(requireContext())
                binding.songs.adapter = adapter
            }
        }
    }

    override fun onItemClick(position: Int) {
        val bundle = Bundle()
        bundle.putString("song", viewModel.songs.value!![position].name)


        val songFragment = SongPlayerFragment()
        songFragment.setArguments(bundle)

        (requireActivity() as MainActivity).replaceFragment(songFragment,"details")
    }
}