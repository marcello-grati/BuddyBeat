package com.example.buddybeat.playlist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybeat.MainActivity
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.databinding.PlaylistItemBinding
import com.example.buddybeat.songplayer.SongPlayerFragment
import com.google.android.material.snackbar.Snackbar

class PlaylistAdapter(private var parentActivity: MainActivity) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    lateinit var songs: MutableList<Song>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(
            PlaylistItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return songs.count()
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val song = songs[position]
        holder.view.numberTextView.text = position.toString()
        holder.view.songTitle.text = song.title.toString()
        holder.view.songDescription.text = buildString {
            append(song.artist)
            append(" - ")
            append(song.duration)
        }
        holder.itemView.setOnClickListener {
            Snackbar.make(it, "${song.description}", Snackbar.LENGTH_SHORT).show()
            val songFragment = SongPlayerFragment.newInstance(song.title, song.description)
            parentActivity.replaceFragment(songFragment,"details")
        }
    }

    fun setValues(songs: MutableList<Song>) {
        this.songs = songs
        //notifica all'adapter che i dati sono cambiati
        notifyDataSetChanged()
    }

    inner class PlaylistViewHolder(itemView: PlaylistItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        val view = itemView
    }
}