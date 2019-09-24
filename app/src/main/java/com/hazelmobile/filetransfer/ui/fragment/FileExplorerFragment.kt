/*
package com.hazelmobile.filetransfer.ui.fragment

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.ui.viewmodel.FileExplorerViewModel
import com.hazelmobile.filetransfer.util.callback.TitleSupport

class FileExplorerFragment : Fragment(), TitleSupport {

    override fun getTitle(context: Context): CharSequence {
        return "Files"
    }

    companion object {
        fun newInstance() = FileExplorerFragment()
        @JvmStatic
        val ARG_SELECT_BY_CLICK = "argSelectByClick"
    }

    private lateinit var viewModel: FileExplorerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.file_explorer_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FileExplorerViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
*/
