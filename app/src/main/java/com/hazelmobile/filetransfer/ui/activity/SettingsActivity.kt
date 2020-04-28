package com.hazelmobile.filetransfer.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import com.code4rox.adsmanager.AdmobUtils
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.`object`.NetworkDevice
import com.hazelmobile.filetransfer.app.Activity
import com.hazelmobile.filetransfer.util.AppUtils
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
        val admobUtils = AdmobUtils(this)
        admobUtils.loadBannerAd(banner_ad_view)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.shareUserName ->
                if (checkPermissionsState())
                    startActivity(Intent(this@SettingsActivity, WelcomeActivity::class.java)
                        .putExtra("reverse_settings", true))
                else
                    requestRequiredPermissions(false)
            R.id.privacyPolicy -> {
                val url = "https://www.freeprivacypolicy.com/privacy/view/50c5621471755f1548917ebbe5e90160"
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(this, Uri.parse(url))
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

}