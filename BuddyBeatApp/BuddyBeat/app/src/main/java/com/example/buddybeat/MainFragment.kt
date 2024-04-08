package com.example.buddybeat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybeat.data.models.Playlist
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.databinding.FragmentMainBinding
import com.example.buddybeat.playlist.PlaylistFragment
import com.google.android.material.snackbar.Snackbar

class MainFragment(parentActivity: MainActivity) : Fragment(), InsertPlaylistFragment.InsertPlaylistListener{

    private val viewModel : MainViewModel by viewModels()

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
        val item : PlaylistWithSongs = v.tag as PlaylistWithSongs
        val playlistFragment = PlaylistFragment.newInstance(parentActivity, item.playlist.playlistId)
        parentActivity.replaceFragment(playlistFragment,"playlist")
    }

    private var adapter = MainAdapter(onClickListener)

    override fun onDialogPositiveClick(dialog: DialogFragment, name:String, description:String) {
        viewModel.insert(Playlist(title = name, description = description))
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fab.setOnClickListener {
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
            val newFragment = InsertPlaylistFragment(this)
            newFragment.show(parentFragmentManager, "choice")}
        viewModel.getAllPlaylists().observe(viewLifecycleOwner) {
            binding.playlists.visibility = View.VISIBLE
            binding.playlists.layoutManager = LinearLayoutManager(requireContext())
            adapter.setValues(viewModel.getAllPlaylists().value!!)
            binding.playlists.adapter = adapter
        }
        val mIth = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val deleted = (viewHolder.itemView.tag as PlaylistWithSongs).playlist
                    viewModel.delete(deleted)
                    Snackbar.make(binding.playlists, "Playlist removed", Snackbar.LENGTH_LONG)
                        .setAction("Cancel", View.OnClickListener {
                            viewModel.insert(deleted)
                        }).show()
                }
            }).attachToRecyclerView(binding.playlists)
    }



}