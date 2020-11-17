package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent

import com.dev.bytes.adsmanager.BannerPlacements
import com.dev.bytes.adsmanager.loadBannerAd
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.`object`.NetworkDevice
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.Activity
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.App
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.AppUtils
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.banner_ads_layout_tag.*

class SettingsActivity : Activity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar.let {
            it?.setDisplayHomeAsUpEnabled(true)
        }
        setUsername()
        init()

    }

    private fun init() {
        shareUserName.setOnClickListener(this@SettingsActivity)
        privacyPolicy.setOnClickListener(this@SettingsActivity)
        ad_container_banner.loadBannerAd(BannerPlacements.BANNER_AD)
        switch_file.isChecked =
            AppUtils.getDefaultPreferences(this).getBoolean("show_system_apps", false)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.shareUserName ->
                if (checkPermissionsState())
                    startActivity(
                        Intent(this@SettingsActivity, WelcomeActivity::class.java)
                            .putExtra("reverse_settings", true)
                    )
                else
                    requestRequiredPermissions(false)
            R.id.privacyPolicy -> {
                val url =
                    "https://www.freeprivacypolicy.com/privacy/view/50c5621471755f1548917ebbe5e90160"
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(this, Uri.parse(url))
            }
            R.id.showHiddenFiles -> {
                if (switch_file.isChecked) {
                    switch_file.isChecked = false
                    AppUtils.getDefaultPreferences(this).edit()
                        .putBoolean("show_system_apps", false)
                        .apply()
                    return
                }
                switch_file.isChecked = true
                AppUtils.getDefaultPreferences(this).edit().putBoolean("show_system_apps", true)
                    .apply()
            }
        }
    }

    private fun setUsername() {
        val localDevice: NetworkDevice = AppUtils.getLocalDevice(applicationContext)
        user_name.text = localDevice.nickname
    }

    override fun onRestart() {
        super.onRestart()
        setUsername()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((application as? App)?.bp != null && ((application as? App)?.bp?.handleActivityResult(
                requestCode, resultCode, data
            ) == false)
        )
            super.onActivityResult(requestCode, resultCode, data)
    }

}