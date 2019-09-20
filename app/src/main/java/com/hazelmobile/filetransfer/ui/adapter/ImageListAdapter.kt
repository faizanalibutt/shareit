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
import com.hazelmobile.filetransfer.model.ImageHolder

class ImageListAdapter(private val imageList: List<ImageHolder>, val context: Context) : RecyclerView.Adapter<ImageListAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.image_item_layout,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageHolder = imageList[position]
        holder.bind(imageHolder)
    }


    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var imageDate: TextView? = null
        private var imageCount: TextView? = null
        private var imageIcon: ImageView? = null

        init {
            imageDate = itemView.findViewById(R.id.imageDate)
            imageCount = itemView.findViewById(R.id.imageAlbumLength)
            imageIcon = itemView.findViewById(R.id.imageIcon)
        }


        fun bind(imageHolder: ImageHolder) {
            imageDate?.text = imageHolder.dateTakenString
            imageCount?.text = imageHolder.imageCount

            GlideApp.with(context)
                .load(imageHolder.imageThumbnail)
                .override(300)
                .centerCrop()
                .into(imageIcon!!)
        }

    }
}