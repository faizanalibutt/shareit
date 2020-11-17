package com.dev.bytes.adsmanager

import android.content.Context
import androidx.annotation.Keep
import com.dev.bytes.adsmanager.events.logEvent
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import timber.log.Timber
import java.util.*
import com.facebook.ads.InterstitialAd as InterstitialAdFB

@Keep
data class InterAdPair(var interAM: InterstitialAd? = null, var interFB: InterstitialAdFB? = null) {
    fun showAd(context: Context, isDelayEnabled: Boolean = false): Boolean {
        val isShow = when {
            context.checkIfPremium() -> false
            interAM?.isLoaded == true || interFB?.isAdLoaded == true -> {
                if (!isDelayEnabled || InterDelayTimer.isDelaySpent()) interAM?.show()
                    ?: interFB?.show()
                true
            }
            else -> false
        }
        context.logEvent("inter_show", "$isShow")
        return isShow
    }

    fun isLoaded() = interAM?.isLoaded == true || interFB?.isAdLoaded == true
}

@Keep
object InterDelayTimer {
    private var lastShowTimeInMillis = 0L
    const val INTERSTITIAL_DELAY_TIME = "pm_sm_interstitial_delay_time"
    fun isDelaySpent(): Boolean {
        val currentTime = Calendar.getInstance().timeInMillis
        val diff = (currentTime - lastShowTimeInMillis) / 1000L
        val requiredDelay =
            FirebaseRemoteConfig.getInstance().getLong(INTERSTITIAL_DELAY_TIME)
        return if (diff >= requiredDelay) {
            lastShowTimeInMillis = currentTime
            true
        } else false
    }
}

@Keep
fun Context.loadInterstitialAd(
    ADUnit: ADUnitType, reloadOnClosed: Boolean = false,
    onLoaded: ((InterAdPair) -> Unit)? = null, onClosed: (() -> Unit)? = null,
    remoteConfigKey: String? = null
) {
    if (checkIfPremium() || (remoteConfigKey != null && !remoteConfigKey.isEnabledRemotely())) {
        return
    }
    Timber.e("load inter priority ${ADUnit.priority}")
    when (ADUnit.priority) {
        AdsPriority.ADMOB, AdsPriority.ADMOB_FACEBOOK ->
            newAMInterstitialAd(ADUnit, reloadOnClosed, onLoaded, onClosed)
        AdsPriority.FACEBOOK, AdsPriority.FACEBOOK_ADMOB ->
            newFBInterstitial(ADUnit, reloadOnClosed, onLoaded, onClosed)
    }
}

fun Context.newAMInterstitialAd(
    ADUnit: ADUnitType, reloadOnClosed: Boolean,
    onLoaded: ((InterAdPair) -> Unit)?, onClosed: (() -> Unit)?
): InterstitialAd {
    val interstitialAd = InterstitialAd(this)
    interstitialAd.adUnitId = this.getString(ADUnit.adUnitIDAM)
    interstitialAd.loadAd(AdRequest.Builder().build())
    interstitialAd.adListener = object : AdListener() {
        override fun onAdClosed() {
            onClosed?.invoke()
            if (reloadOnClosed)
                loadInterstitialAd(ADUnit, reloadOnClosed, onLoaded, onClosed)
        }

        override fun onAdFailedToLoad(p0: Int) {
            Timber.e("onFailed Inter AM $p0")
            //onClosed?.invoke()
            if (ADUnit.priority == AdsPriority.ADMOB_FACEBOOK)
                newFBInterstitial(ADUnit, reloadOnClosed, onLoaded, onClosed)
        }

        override fun onAdLoaded() {
            Timber.e("InterstitialSplash: onAdLoaded")
            onLoaded?.invoke(InterAdPair(interstitialAd))
        }
    }
    return interstitialAd
}

fun Context.newFBInterstitial(
    ADUnit: ADUnitType, reloadOnClosed: Boolean,
    onLoaded: ((InterAdPair) -> Unit)?, onClosed: (() -> Unit)?
): InterstitialAdFB? {
    val interstitialAd = ADUnit.adUnitIDFB?.let { id -> InterstitialAdFB(this, getString(id)) }
    interstitialAd?.run {
        loadAd(
            buildLoadAdConfig()?.withAdListener(object : FBInterAdListener() {

                override fun onInterstitialDismissed(p0: Ad?) {
                    onClosed?.invoke()
                    if (reloadOnClosed)
                        loadInterstitialAd(ADUnit, reloadOnClosed, onLoaded, onClosed)
                }

                override fun onError(p0: Ad?, p1: AdError?) {
                    Timber.e("onFailed Inter FB ${p1?.errorMessage} ${p1?.errorCode}")
                    if (ADUnit.priority == AdsPriority.FACEBOOK_ADMOB)
                        newAMInterstitialAd(ADUnit, reloadOnClosed, onLoaded, onClosed)
                }

                override fun onAdLoaded(p0: Ad?): Unit =
                    onLoaded?.invoke(InterAdPair(interFB = interstitialAd)) ?: Unit

            })?.build()
        )
    }
    return interstitialAd
}

open class FBInterAdListener : InterstitialAdListener {
    override fun onInterstitialDisplayed(p0: Ad?) {}

    override fun onAdClicked(p0: Ad?) {}

    override fun onInterstitialDismissed(p0: Ad?) {}

    override fun onError(p0: Ad?, p1: AdError?) {}

    override fun onAdLoaded(p0: Ad?) {}

    override fun onLoggingImpression(p0: Ad?) {}

}