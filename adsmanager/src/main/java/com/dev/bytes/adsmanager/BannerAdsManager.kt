package com.dev.bytes.adsmanager

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import com.dev.bytes.R
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import timber.log.Timber
import com.facebook.ads.AdListener as AdListenerFB
import com.facebook.ads.AdSize as AdSizeFB
import com.facebook.ads.AdView as AdViewFB

private fun ViewGroup.getAdaptiveAdSize(): AdSize {
    val outMetrics = Resources.getSystem().displayMetrics

    val density = outMetrics.density

    var adWidthPixels = width.toFloat()
    if (adWidthPixels == 0f) {
        adWidthPixels = outMetrics.widthPixels.toFloat()
    }

    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
}

fun ViewGroup.loadBannerAd(ADUnit: BannerADUnit) {

    if (context.checkIfPremium() /*|| !isBannerEnabledRemotely()*/) {
        visibility = View.GONE
        return
    }
    when (ADUnit.priority) {
        AdsPriority.ADMOB, AdsPriority.ADMOB_FACEBOOK -> loadBannerAdAM(ADUnit)

        AdsPriority.FACEBOOK, AdsPriority.FACEBOOK_ADMOB -> loadBannerAdFB(ADUnit)
    }
}

fun ViewGroup.loadBannerAdAM(adUnit: BannerADUnit) {
    removeAllViews()
    val bannerAdaptive = AdView(context)
    bannerAdaptive.adSize = adUnit.adSizeAM ?: getAdaptiveAdSize()
    bannerAdaptive.adUnitId = context.getString(adUnit.adUnitIDAM)
    bannerAdaptive.loadAd(AdRequest.Builder().build())
    bannerAdaptive.adListener = object : AdListener() {
        override fun onAdLoaded() {
            this@loadBannerAdAM.visibility = View.VISIBLE
        }

        override fun onAdFailedToLoad(p0: Int) {
            Timber.e("onFailed AM Banner errorCode $p0")
            if (adUnit.priority == AdsPriority.ADMOB_FACEBOOK)
                loadBannerAdFB(adUnit)
        }
    }
    val params = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    addView(bannerAdaptive, params)
}

fun ViewGroup.loadBannerAdFB(ADUnit: BannerADUnit) {
    removeAllViews()

    ADUnit.adUnitIDFB?.let { adUnitId ->
        AdViewFB(context, context.getString(adUnitId), ADUnit.adSizeFB ?: AdSizeFB.BANNER_HEIGHT_50)
    }?.also { adView ->
        addView(adView)

        val adListener = object : FBAdListener() {

            override fun onError(p0: Ad?, p1: AdError?) {
                Timber.e("onError fb Banner ${p1?.errorMessage} ${p1?.errorCode}")
                if (ADUnit.priority == AdsPriority.FACEBOOK_ADMOB)
                    loadBannerAdAM(ADUnit)
            }

            override fun onAdLoaded(p0: Ad?) {
                this@loadBannerAdFB.visibility = View.VISIBLE
            }

        }

        adView.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build())
    }
}

open class FBAdListener : AdListenerFB {
    override fun onAdClicked(p0: Ad?) {}

    override fun onError(p0: Ad?, p1: AdError?) {}

    override fun onAdLoaded(p0: Ad?) {}

    override fun onLoggingImpression(p0: Ad?) {}
}

fun Context.checkIfPremium() = TinyDB(this).getBoolean(getString(R.string.is_premium))

const val IS_BOTTOM_BANNER_SHOW = "is_bottom_banner_show" // only change string value if needed
fun isBannerEnabledRemotely() = FirebaseRemoteConfig.getInstance()
    .getBoolean(IS_BOTTOM_BANNER_SHOW)

fun String.isEnabledRemotely(): Boolean {
    FirebaseRemoteConfig.getInstance().all.forEach {
        Timber.e("all_values ${it.key} ${it.value.asString()}") // TODO check all values configured remotely
    }
    val value = FirebaseRemoteConfig.getInstance()
        .getBoolean(this)
    Timber.e("key:$this value $value")
    return value
}