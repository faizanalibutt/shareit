package com.hazelmobile.filetransfer.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.model.PackageHolder
import com.hazelmobile.filetransfer.pictures.AppUtils
import com.hazelmobile.filetransfer.pictures.EditableListAdapter
import com.hazelmobile.filetransfer.pictures.EditableListFragment
import com.hazelmobile.filetransfer.ui.adapter.ApplicationListAdapter
import com.hazelmobile.filetransfer.util.callback.TitleSupport
import com.hazelmobile.filetransfer.widget.getToast
import kotlinx.android.synthetic.main.apps_item_layout.*
import kotlinx.android.synthetic.main.apps_item_layout.view.*
import kotlinx.android.synthetic.main.fragment_application_list.*
import kotlinx.android.synthetic.main.generic_view_selection_rounded_custom.view.*

class ApplicationListFragment : EditableListFragment<PackageHolder, EditableListAdapter.EditableViewHolder, ApplicationListAdapter>(), TitleSupport {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setFilteringSupported(true)
        setHasOptionsMenu(false)
    }

    override fun onSetListAdapter(adapter: ApplicationListAdapter?): Boolean {
        return if (super.onSetListAdapter(adapter)) {
            myAppsText.text = getApplicationListSize(adapter)
            true
        } else
            false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDefaultViewingGridSize(4, 8)
        setUseDefaultPaddingDecoration(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setEmptyImage(R.drawable.ic_android_head_white_24dp)
        setEmptyText(getString(R.string.text_listEmptyApp))

    }

    private fun getApplicationListSize(adapter: ApplicationListAdapter?) = "My Apps ( ${adapter!!.onLoad().size} )"

    override fun getTitle(context: Context): CharSequence {
        return context.getString(R.string.text_application)
    }

    override fun onDefaultClickAction(holder: EditableListAdapter.EditableViewHolder?): Boolean {
        return if (selectionConnection != null) selectionConnection.setSelected(holder) else performLayoutClickOpen(holder)
    }

    override fun onAdapter(): ApplicationListAdapter {
        val quickActions: AppUtils.QuickActions<EditableListAdapter.EditableViewHolder> = AppUtils.QuickActions { clazz ->
            registerLayoutViewClicks(clazz)
            clazz.view.appTick.setOnClickListener {
                if (selectionConnection != null) {
                    selectionConnection.setSelected(clazz.adapterPosition)
                }
            }
            clazz.view.appName.setOnClickListener {
                getToast("yes its color", context!!)
            }
        }

        return object : ApplicationListAdapter(context!!, AppUtils.getDefaultPreferences(context)) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditableViewHolder {
                return AppUtils.quickAction(super.onCreateViewHolder(parent, viewType), quickActions)
            }
        }
    }

    override fun onListView(mainContainer: View?, listViewContainer: ViewGroup?): RecyclerView {

        val adaptedView: View = layoutInflater.inflate(R.layout.fragment_application_list, null, false)
        listViewContainer!!.addView(adaptedView)

        return super.onListView(mainContainer, adaptedView.findViewById(R.id.appsList) as ViewGroup)
    }


}
