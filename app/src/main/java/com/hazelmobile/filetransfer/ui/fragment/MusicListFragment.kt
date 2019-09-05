package com.hazelmobile.filetransfer.ui.fragment

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.ui.viewmodel.MusicListViewModel
import com.hazelmobile.filetransfer.utils.callback.TitleSupport

class MusicListFragment : Fragment(), TitleSupport {

    override fun getTitle(context: Context): CharSequence {
        return "Audio"
    }

    companion object {
        fun newInstance() = MusicListFragment()
    }

    private lateinit var viewModel: MusicListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.music_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MusicListViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
