package com.hazelmobile.filetransfer.ui.fragment


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.utils.callback.IconSupport
import com.hazelmobile.filetransfer.utils.callback.TitleSupport

class BrowseFragment : Fragment(), IconSupport, TitleSupport {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_browse, container, false)
    }

    override fun getIconRes(): Int {
        return R.drawable.ic_browse_hv_blue_24dp
    }

    override fun getTitle(context: Context): CharSequence {
        return context.getString(R.string.text_Browser)
    }

}
