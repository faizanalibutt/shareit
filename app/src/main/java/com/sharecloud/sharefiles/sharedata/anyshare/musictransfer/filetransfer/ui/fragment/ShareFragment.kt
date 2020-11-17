package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.fragment


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.bytes.adsmanager.ADUnitPlacements
import com.dev.bytes.adsmanager.loadNativeAd

//import com.code4rox.adsmanager.NativeAdsIdType
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.config.Keyword
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity.ContentSharingActivity
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity.PreparationsActivity
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.callback.IconSupport
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.callback.TitleSupport
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.NetworkUtils
import kotlinx.android.synthetic.main.fragment_share.*
import kotlinx.android.synthetic.main.fragment_share.receive_button
import kotlinx.android.synthetic.main.fragment_share.send_button
import kotlinx.android.synthetic.main.fragment_share_new.*

class ShareFragment : BaseFragment(), IconSupport, TitleSupport {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_share_new, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        send_button.setOnClickListener {
            startActivity(Intent(view.context, ContentSharingActivity::class.java))
            send_button.isEnabled = false
        }

        receive_button.setOnClickListener {
            startActivity(
                Intent(view.context, PreparationsActivity::class.java)
                    .putExtra(Keyword.EXTRA_RECEIVE, true)
            )
            receive_button.isEnabled = false
        }

        /*if (NetworkUtils.isOnline(view.context)) {
            val admobUtils = AdmobUtils(view.context)
            admobUtils.loadNativeAd(fl_adplaceholder, R.layout.ad_unified, NativeAdsIdType.ADJUST_NATIVE_AM)
            admobUtils.setNativeAdListener(object : AdmobUtils.NativeAdListener {
                override fun onNativeAdLoaded() {

                }
                override fun onNativeAdError() {

                }
            })
        }*/

        view.context.loadNativeAd(
            fl_adplaceholder, R.layout.ad_unified,
            ADUnitPlacements.MAIN_MM_NATIVE_AD, false
        )

    }

    override fun onResume() {
        super.onResume()
        send_button.isEnabled = true
        receive_button.isEnabled = true
    }

    override fun getIconRes(): Int {
        return R.drawable.ic_share_black_24dp
    }

    override fun getTitle(context: Context): CharSequence {
        return context.getString(R.string.text_Share)
    }


}