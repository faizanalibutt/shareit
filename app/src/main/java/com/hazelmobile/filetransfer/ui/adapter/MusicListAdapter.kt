package com.hazelmobile.filetransfer.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.model.SongHolder

class MusicListAdapter(private val musicItem: List<SongHolder>, val context: Context) :
    RecyclerView.Adapter<MusicListAdapter.MusicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        return MusicViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.music_item_layout,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return musicItem.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val songHolder: SongHolder = musicItem[position]
        holder.bind(songHolder)
    }

    inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var musicName: TextView? = null
        private var musicDateSize: TextView? = null

        init {
            musicName = itemView.findViewById(R.id.audioTitle)
            musicDateSize = itemView.findViewById(R.id.audioSize)
        }

        fun bind(songHolder: SongHolder) {
            musicName?.text = songHolder.displayName
            musicDateSize?.text = songHolder.datesize
        }
    }
}