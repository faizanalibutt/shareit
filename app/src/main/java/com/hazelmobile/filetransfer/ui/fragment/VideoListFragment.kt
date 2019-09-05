package com.hazelmobile.filetransfer.ui.fragment

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.ui.viewmodel.VideoListViewModel
import com.hazelmobile.filetransfer.utils.callback.TitleSupport

class VideoListFragment : Fragment(), TitleSupport {

    override fun getTitle(context: Context): CharSequence {
        return "Videos"
    }

    companion object {
        fun newInstance() = VideoListFragment()
    }

    private lateinit var viewModel: VideoListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(VideoListViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
