package com.hazelmobile.filetransfer.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hazelmobile.filetransfer.GlideApp
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.model.VideoHolder

class VideoListAdapter(private val videoItem: List<VideoHolder>, val context: Context) :
    RecyclerView.Adapter<VideoListAdapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.video_item_layout,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return videoItem.size
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val videoHolder: VideoHolder = videoItem[position]
        holder.bind(videoHolder)
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var videoName: TextView? = null
        private var videoSize: TextView? = null
        private var videoDuration: TextView? = null
        private var videoThumbnail: ImageView? = null

        init {
            videoName = itemView.findViewById(R.id.videoTitle)
            videoSize = itemView.findViewById(R.id.videoSize)
            videoDuration = itemView.findViewById(R.id.videoDuration)
            videoThumbnail = itemView.findViewById(R.id.videoThumbnail)
        }

        fun bind(videoHolder: VideoHolder) {
            videoName?.text = videoHolder.videoTitle
            videoSize?.text = videoHolder.videoSize
            videoDuration?.text = videoHolder.videoDuration
            videoThumbnail.let {
                GlideApp.with(context)
                    .load(videoHolder.videoUri)
                    .centerCrop()
                    .into(it!!)
            }
        }

    }
}