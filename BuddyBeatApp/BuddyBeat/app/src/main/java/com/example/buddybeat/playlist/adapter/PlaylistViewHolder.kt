package com.example.buddybeat.playlist.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybeat.databinding.PlaylistItemBinding
import com.example.buddybeat.models.Song

class PlaylistViewHolder(val view: PlaylistItemBinding, private var listener: PlaylistAdapter.OnItemClickListener)
    : RecyclerView.ViewHolder(view.root), View.OnClickListener {

    fun setUpSong(model: Song) {
        view.numberTextView.text = model.number.toString()
        view.songTitle.text = model.name
        view.songDescription.text = buildString {
            append(model.author)
            append(" - ")
            append(model.duration)
        }
    }
    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        val position: Int = adapterPosition
        if(position!= RecyclerView.NO_POSITION){
            listener.onItemClick(position)
        }
    }
}