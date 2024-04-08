package com.example.buddybeat.playlist.adapter

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.databinding.PlaylistItemBinding

class PlaylistAdapter(private val onClickListener: OnClickListener) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

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
        holder.view.numberTextView.text = (position+1).toString()
        holder.view.songTitle.text = song.title.toString()
        holder.view.songDescription.text = buildString {
            append(song.artist)
            append(" - ")
            append(song.duration)
        }
        with(holder.itemView) {
            tag = song
            setOnClickListener(onClickListener)
        }
    }

    fun setValues(songs: MutableList<Song>) {
        this.songs = songs
        notifyDataSetChanged()
    }

    inner class PlaylistViewHolder(itemView: PlaylistItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        val view = itemView
    }
}