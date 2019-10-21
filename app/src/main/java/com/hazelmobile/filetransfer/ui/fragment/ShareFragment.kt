package com.hazelmobile.filetransfer.ui.fragment


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.ui.activity.ContentSharingActivity
import com.hazelmobile.filetransfer.ui.activity.ReceiverActivity
import com.hazelmobile.filetransfer.util.callback.IconSupport
import com.hazelmobile.filetransfer.util.callback.TitleSupport
import kotlinx.android.synthetic.main.fragment_share.*

class ShareFragment : BaseFragment(), IconSupport, TitleSupport {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_share, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        send_button.setOnClickListener {
            startActivity(Intent(context, ContentSharingActivity::class.java))
        }

        receive_button.setOnClickListener {
            startActivity(Intent(context, ReceiverActivity::class.java))
        }
    }

    override fun getIconRes(): Int {
        return R.drawable.ic_share_black_24dp
    }

    override fun getTitle(context: Context): CharSequence {
        return context.getString(R.string.text_Share)
    }


}