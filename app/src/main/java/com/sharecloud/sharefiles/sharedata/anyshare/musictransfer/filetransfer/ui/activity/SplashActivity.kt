package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.code4rox.adsmanager.AdmobUtils
import com.code4rox.adsmanager.NativeAdsIdType
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.AppUtils
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.NetworkUtils
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.TinyDB
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.layout_gdp_view.*
import java.lang.IllegalStateException

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initFbRemoteConfig()
        getSplashViews()
    }

    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null

    private fun initFbRemoteConfig() {
        try {
            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build()
            mFirebaseRemoteConfig?.setConfigSettingsAsync(configSettings)
            mFirebaseRemoteConfig?.setDefaultsAsync(R.xml.firebase_config)
            mFirebaseRemoteConfig?.fetchAndActivate()?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d("Firebase", "Config params updated: $updated")
                }
            }
        } catch (iexp: IllegalStateException) {
            iexp.message
        }
    }

    private fun getSplashViews() {
        val isFirstLaunch =
            AppUtils.getDefaultPreferences(this@SplashActivity).getBoolean("is_First_Launch", false)
        if (!isFirstLaunch) {
            showGdpView()
        } else {
            showSplashView()
        }
    }


    private fun showGdpView() {

        appText.visibility = View.GONE
        appIconsplash.visibility = View.GONE
        progressBar.visibility = View.GONE
        layout_gdp.visibility = View.VISIBLE

        @Suppress("DEPRECATION")
        //this.tv_privacy.text = Html.fromHtml("<u>"+resources.getString(R.string.privacy_policy_translate)+"</u>")

        val colorPrimary = ContextCompat.getColor(this@SplashActivity, R.color.colorPrimary)
        val spanString: SpannableString? = SpannableString(appname.text)
        val appColor = ForegroundColorSpan(colorPrimary)
        spanString!!.setSpan(appColor, 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        appname.text = spanString

        val text = SpannableString(getString(R.string.privacy_policy))
        text.setSpan(UnderlineSpan(), 27, privacytext.length(), 0)
        text.setSpan(appColor, 27, privacytext.length(), 0)
        privacytext.text = text

        privacytext.setOnClickListener {
            val url =
                "https://www.freeprivacypolicy.com/privacy/view/50c5621471755f1548917ebbe5e90160"
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(this, Uri.parse(url))
        }

        AppUtils.getDefaultPreferences(this@SplashActivity).edit().putBoolean(
            "show_rating_dialog", true
        ).apply()

        startbutton.setOnClickListener {
            AppUtils.getDefaultPreferences(this@SplashActivity).edit().putBoolean(
                "is_First_Launch", true
            ).apply()
            showSplashView()
        }
    }

    fun showSplashView() {

        appText.visibility = View.VISIBLE
        appIconsplash.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        layout_gdp.visibility = View.GONE

        val admobUtils = AdmobUtils(this@SplashActivity)

        admobUtils.loadNativeAd(
            adGroup,
            R.layout.ad_unified_splash_ext,
            NativeAdsIdType.SPLASH_NATIVE_AM
        )
        admobUtils.setNativeAdListener(object : AdmobUtils.NativeAdListener {
            override fun onNativeAdLoaded() {
                skipText.visibility = View.VISIBLE
            }

            override fun onNativeAdError() {}
        })

        val isOnline = NetworkUtils.isOnline(this@SplashActivity) && (!TinyDB.getInstance(this)
            .getBoolean(getString(R.string.is_premium)))
        val stuckLimit: Long = if (isOnline) 8000 else 3000
        var skip = if (isOnline) 8 else 3
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
                    skipText.text = String.format("Skip $skip")
                }
            }
        }.start()


        Handler().postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }, stuckLimit)
    }

}
