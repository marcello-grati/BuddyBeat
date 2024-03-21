package com.example.buddybeat.playlist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybeat.databinding.PlaylistItemBinding
import com.example.buddybeat.models.Song
import com.example.buddybeat.playlist.PlaylistFragment

class PlaylistAdapter(private val songs: ArrayList<Song>, private val listener: PlaylistFragment)
    : RecyclerView.Adapter<PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(
            PlaylistItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), listener
        )
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.setUpSong(songs[position])

    }
    fun update(newList: List<Song>){
        songs.clear()
        songs.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return songs.count()
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}