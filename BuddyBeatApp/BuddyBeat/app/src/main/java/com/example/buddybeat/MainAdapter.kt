package com.example.buddybeat


import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybeat.data.models.PlaylistWithSongs
import com.example.buddybeat.databinding.PlaylistItemBinding

class MainAdapter(private val onClickListener: OnClickListener) : RecyclerView.Adapter<MainAdapter.PlaylistViewHolder>() {

    private lateinit var playlists: MutableList<PlaylistWithSongs>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(
            PlaylistItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return playlists.count()
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.view.numberTextView.text = (position+1).toString()
        holder.view.songTitle.text = playlist.playlist.title.toString()
        holder.view.songDescription.text = playlist.playlist.description.toString()
        with(holder.itemView) {
            tag = playlist
            setOnClickListener(onClickListener)
        }
    }

    fun setValues(playlists: MutableList<PlaylistWithSongs>) {
        this.playlists = playlists
        notifyDataSetChanged()
    }

    inner class PlaylistViewHolder(itemView: PlaylistItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        val view = itemView
    }
}