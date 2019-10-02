package com.hazelmobile.filetransfer.ui.fragment


import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.genonbeta.android.framework.util.FileUtils

import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.model.PackageHolder
import com.hazelmobile.filetransfer.pictures.AppUtils
import com.hazelmobile.filetransfer.pictures.EditableListAdapter
import com.hazelmobile.filetransfer.pictures.EditableListFragment
import com.hazelmobile.filetransfer.ui.adapter.ApplicationListAdapter
import com.hazelmobile.filetransfer.util.callback.TitleSupport
import kotlinx.android.synthetic.main.fragment_application_list.appsList
import kotlinx.android.synthetic.main.fragment_application_list.view.*
import kotlinx.android.synthetic.main.generic_view_selection.view.*
import kotlinx.android.synthetic.main.list_image.view.*
import java.io.File
import java.util.*

class ApplicationListFragment : EditableListFragment<PackageHolder, EditableListAdapter.EditableViewHolder, ApplicationListAdapter>(), TitleSupport {


    override fun onDefaultClickAction(holder: EditableListAdapter.EditableViewHolder?): Boolean {
        return if (selectionConnection != null) selectionConnection.setSelected(holder) else performLayoutClickOpen(holder)
    }

    override fun onAdapter(): ApplicationListAdapter {
        val quickActions: AppUtils.QuickActions<EditableListAdapter.EditableViewHolder> = AppUtils.QuickActions {clazz ->
            registerLayoutViewClicks(clazz)
            clazz.view.selector.setOnClickListener {
                if (selectionConnection != null) {
                    selectionConnection.setSelected(clazz.adapterPosition)
                }
            }
        }

        return object : ApplicationListAdapter(context!!, AppUtils.getDefaultPreferences(context)) {
            override  fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditableViewHolder {
                return AppUtils.quickAction(super.onCreateViewHolder(parent, viewType), quickActions)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setFilteringSupported(true)
        setHasOptionsMenu(false)
        setDefaultViewingGridSize(4, 8)
        setUseDefaultPaddingDecoration(false)
    }

    /*override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_application_list, container, false)
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setEmptyImage(R.drawable.ic_android_head_white_24dp)
        setEmptyText(getString(R.string.text_listEmptyApp))
        /*val appList = ArrayList<PackageHolder>()

        for (packageInfo in context!!.packageManager.getInstalledPackages(PackageManager.GET_META_DATA)) {
            val appInfo = packageInfo.applicationInfo
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 1) {
                val packageHolder = PackageHolder(
                    appInfo.loadLabel(context!!.packageManager).toString(),
                    appInfo,
                    FileUtils.sizeExpression(File(appInfo.sourceDir).length(), false),
                    packageInfo.packageName
                )
                appList.add(packageHolder)
            }
        }

        view.myAppsText.text = "My Apps ( ${appList.size} )"

        appsList.apply {
            appsList.layoutManager = GridLayoutManager(context, 4)
            val applicationListAdapter = ApplicationListAdapter(appList, context)
            appsList.adapter = applicationListAdapter
        }*/
    }

    override fun getTitle(context: Context): CharSequence {
        return context.getString(R.string.text_application)
    }


}
