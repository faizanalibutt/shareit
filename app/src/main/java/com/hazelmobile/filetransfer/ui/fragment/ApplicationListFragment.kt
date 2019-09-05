package com.hazelmobile.filetransfer.ui.fragment


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.utils.callback.TitleSupport

/**
 * A simple [Fragment] subclass.
 */
class ApplicationListFragment : Fragment(), TitleSupport {

    override fun getTitle(context: Context): CharSequence {
        return "Apps"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_application_list, container, false)
    }


}
