package com.hazelmobile.filetransfer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.code4rox.adsmanager.AdmobUtils
import com.code4rox.adsmanager.NativeAdsIdType
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.app.Activity
import com.hazelmobile.filetransfer.util.NetworkUtils
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val admobUtils = AdmobUtils(this@SplashActivity)
        
        admobUtils.loadNativeAd(adGroup, R.layout.ad_unified_splash_ext, NativeAdsIdType.SPLASH_NATIVE_AM)
        admobUtils.setNativeAdListener(object : AdmobUtils.NativeAdListener {
            override fun onNativeAdLoaded() {
                skipText.visibility = View.VISIBLE
            }
            override fun onNativeAdError() {}
        })

        val isOnline = NetworkUtils.isOnline(this@SplashActivity)
        val stuckLimit: Long = if (isOnline) 8000 else 3000
        var skip = 8
        Thread {
            for ((progress, _) in (1..
                    if (isOnline) 8 else 3).withIndex()) {
                try {
                    Thread.sleep(1000)
                } catch (e: Exception) {
                }
                runOnUiThread {
                    progressBar.max = if (isOnline) 8 else 3
                    progressBar.progress = progress + 1
                    skip -= 1
                    skipText.text = String.format("Skip ${skip}")
                }
            }
        }.start()

        if (isOnline)
            Handler().postDelayed({
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            },  stuckLimit)
        else
            Handler().postDelayed({
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            },  stuckLimit)
    }

}
