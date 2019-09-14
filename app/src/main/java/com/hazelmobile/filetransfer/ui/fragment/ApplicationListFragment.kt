package com.hazelmobile.filetransfer.ui.fragment


import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.genonbeta.android.framework.util.FileUtils

import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.model.PackageHolder
import com.hazelmobile.filetransfer.ui.adapter.ApplicationListAdapter
import com.hazelmobile.filetransfer.utils.callback.TitleSupport
import kotlinx.android.synthetic.main.fragment_application_list.*
import kotlinx.android.synthetic.main.fragment_application_list.appsList
import kotlinx.android.synthetic.main.fragment_application_list.view.*
import java.io.File
import java.util.*

class ApplicationListFragment : BaseFragment(), TitleSupport {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_application_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appList = ArrayList<PackageHolder>()

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
            layoutManager = GridLayoutManager(context, 4)
            val applicationListAdapter = ApplicationListAdapter(appList, context)
            appsList.adapter = applicationListAdapter
        }

    }

    override fun getTitle(context: Context): CharSequence {
        return context.getString(R.string.text_application)
    }


}
