package com.hazelmobile.filetransfer.ui.fragment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.ui.callback.IconSupport
import com.hazelmobile.filetransfer.ui.callback.TitleSupport

class BrowseFragment : Fragment(), IconSupport, TitleSupport {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_browse, container, false)
    }

    override fun getIconRes(): Int {
        return R.drawable.ic_search_black_24dp
    }

    override fun getTitle(context: Context): CharSequence {
        return context.getString(R.string.text_Browser)
    }

}
