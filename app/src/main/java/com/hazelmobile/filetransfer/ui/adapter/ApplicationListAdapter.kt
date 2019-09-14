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
import com.hazelmobile.filetransfer.model.PackageHolder

class ApplicationListAdapter(private val appItem: List<PackageHolder>, val context: Context) :
    RecyclerView.Adapter<ApplicationListAdapter.ApplicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        return ApplicationViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.apps_item_layout,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return appItem.size
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val packageHolder: PackageHolder = appItem[position]
        holder.bind(packageHolder)
    }

    inner class ApplicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var appName: TextView? = null
        private var appSize: TextView? = null
        private var appIcon: ImageView? = null

        init {
            appName = itemView.findViewById(R.id.appName)
            appSize = itemView.findViewById(R.id.appSize)
            appIcon = itemView.findViewById(R.id.appIcon)
        }

        fun bind(packageHolder: PackageHolder) {
            appName?.text = packageHolder.friendlyName
            appSize?.text = packageHolder.appSize

            GlideApp.with(context)
                .load(packageHolder.appInfo)
                .override(80)
                .centerCrop()
                .into(appIcon!!)
        }
    }

}