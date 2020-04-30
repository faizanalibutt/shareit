package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.fragment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.callback.IconSupport
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.callback.TitleSupport


class PremiumFragment : Fragment(), IconSupport, TitleSupport {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_premium, container, false)
    }

    override fun getIconRes(): Int {
        return R.drawable.ic_settings_grey_24dp
    }

    override fun getTitle(context: Context): CharSequence {
        return context.getString(R.string.text_settings)
    }

}
