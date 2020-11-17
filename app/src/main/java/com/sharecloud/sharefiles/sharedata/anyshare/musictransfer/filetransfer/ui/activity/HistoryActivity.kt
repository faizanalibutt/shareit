package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity

import android.content.Intent
import android.os.Bundle
import com.dev.bytes.adsmanager.BannerPlacements
import com.dev.bytes.adsmanager.loadBannerAd

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.Activity
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.App
import kotlinx.android.synthetic.main.banner_ads_layout_tag.*

class HistoryActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val isSingleAd = FirebaseRemoteConfig.getInstance().getBoolean("is_show_single_ad")
        if (!isSingleAd) {
            ad_container_banner.loadBannerAd(BannerPlacements.BANNER_AD)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((application as? App)?.bp != null && ((application as? App)?.bp?.handleActivityResult(
                requestCode, resultCode, data
            ) == false)
        )
            super.onActivityResult(requestCode, resultCode, data)
    }

}
