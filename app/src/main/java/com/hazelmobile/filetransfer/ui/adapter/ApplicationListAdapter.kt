package com.hazelmobile.filetransfer.ui.adapter

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.genonbeta.android.framework.util.FileUtils
import com.hazelmobile.filetransfer.GlideApp
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.model.PackageHolder
import com.hazelmobile.filetransfer.pictures.EditableListAdapter
import com.hazelmobile.filetransfer.widget.getLogInfo
import kotlinx.android.synthetic.main.apps_item_layout.view.*
import java.io.File
import java.security.cert.Extension
import java.util.*

open class ApplicationListAdapter(private val mContext: Context, val preferences: SharedPreferences) :
    EditableListAdapter<PackageHolder, EditableListAdapter.EditableViewHolder>(mContext) {

    override fun onLoad(): MutableList<PackageHolder> {
        val appList = ArrayList<PackageHolder>()

        for (packageInfo: PackageInfo in mContext.packageManager.getInstalledPackages(PackageManager.GET_META_DATA)) {
            val appInfo: ApplicationInfo = packageInfo.applicationInfo
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 1) {

                val packageHolder = PackageHolder(
                    appInfo.loadLabel(mContext.packageManager).toString(),
                    appInfo,
                    packageInfo.versionName,
                    packageInfo.packageName,
                    File(appInfo.sourceDir),
                    FileUtils.sizeExpression(File(appInfo.sourceDir).length(), false)
                )

                if (filterItem(packageHolder))
                    appList.add(packageHolder)
            }
        }

        Collections.sort(appList, defaultComparator)

        return appList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditableViewHolder {
        return EditableViewHolder(inflater.inflate(R.layout.list_application, parent, false))
    }

    override fun onBindViewHolder(@NonNull holder: EditableViewHolder, position: Int) {
        try {
            val parentView = holder.view
            val packageHolder: PackageHolder = getItem(position)
            parentView.appName.text = packageHolder.appfriendlyName
            parentView.appSize.text = packageHolder.appSize
            GlideApp.with(context)
                .load(packageHolder.appInfo)
                .override(80)
                .centerCrop()
                .into(parentView.appIcon)
        } catch (exp: Exception) {
            getLogInfo(exp.message!!)
        }
    }

}