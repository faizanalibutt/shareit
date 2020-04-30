package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity

import android.os.Bundle
import com.code4rox.adsmanager.AdmobUtils
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.Activity
import kotlinx.android.synthetic.main.banner_ads_layout_tag.*

class HistoryActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val admobUtils = AdmobUtils(this)
        admobUtils.loadBannerAd(banner_ad_view)
    }
}
