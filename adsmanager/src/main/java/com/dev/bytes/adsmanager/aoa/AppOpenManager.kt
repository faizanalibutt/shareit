package com.dev.bytes.adsmanager.aoa

import com.dev.bytes.adsmanager.aoa.base.BaseManager
import com.dev.bytes.adsmanager.aoa.delay.DelayType
import com.dev.bytes.adsmanager.aoa.delay.InitialDelay
import android.app.Application
import android.util.StatsLog.logEvent
import androidx.annotation.NonNull
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.dev.bytes.adsmanager.TinyDB
import com.dev.bytes.adsmanager.events.logEvent
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import timber.log.Timber
import kotlin.math.absoluteValue

/**
 * @AppOpenManager = A class that handles all of the App Open Ad operations.
 *
 * Constructor arguments:
 * @param application Required to keep a track of App's state.
 * @param adUnitId Pass your created AdUnitId
 * @param initialDelay for Initial Delay
 *
 * @param adRequest = Pass a customised AdRequest if you have any.
 * @see AdRequest
 *
 * @param orientation Ad's Orientation, Can be PORTRAIT or LANDSCAPE (Default is Portrait)
 * @see AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
 * @see AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE
 *
 */
class AppOpenManager constructor(
    @NonNull application: Application,
    @NonNull initialDelay: InitialDelay,
    @NonNull var adUnitId: String,
    override var adRequest: AdRequest = AdRequest.Builder().build(),
    override var orientation: Int = AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
    val onAdDismissed: (() -> Unit)? = null
) : BaseManager(application),
    LifecycleObserver {

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        this.initialDelay = initialDelay
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        if (initialDelay != InitialDelay.NONE) saveInitialDelayTime()
        showAdIfAvailable()
    }

    // Let's fetch the Ad
    private fun fetchAd() {
        if (isAdAvailable()) return
        loadAd()
        Timber.e("A pre-cached Ad was not available, loading one. AOA")
    }

    // Show the Ad if the conditions are met.
    private fun showAdIfAvailable() {
        if (!isShowingAd && isAdAvailable() && isInitialDelayOver() && currentActivity != null)
        /*if (currentActivity != null)*/ {
            appOpenAd?.show(currentActivity, getFullScreenContentCallback())
            var count_aoa =
                TinyDB.getInstance(getApplication().applicationContext).getInt("show_aoa")
            TinyDB.getInstance(getApplication().applicationContext).putInt("show_aoa", ++count_aoa)
            Timber.e("${(count_aoa + 1)}")
            if ((count_aoa + 1) > 2)
                when {
                    (count_aoa + 1) % 3 == 0 -> {
                        getApplication().applicationContext.logEvent(
                            "3_time_interstitial_show",
                            "$count_aoa"
                        )
                        Timber.e("app_open_ad_event ${(count_aoa + 1)}")
                    }
                    (count_aoa + 1) % 5 == 0 -> {
                        getApplication().applicationContext.logEvent(
                            "5_time_interstitial_show",
                            "$count_aoa"
                        )
                        Timber.e("app_open_ad_event ${(count_aoa + 1)}")
                    }
                    (count_aoa + 1) % 10 == 0 -> {
                        getApplication().applicationContext.logEvent(
                            "10_time_interstitial_show",
                            "$count_aoa"
                        )
                        Timber.e("app_open_ad_event ${(count_aoa + 1)}")
                    }
                }
        } else {
            Timber.e("AOA ad not available")
            if (!isInitialDelayOver()) Timber.e("AOA The Initial Delay period is not over yet.")
            /**
             *If the next session happens after the delay period is over
             * & under 4 Hours, we can show a cached Ad.
             * However the above will only work for DelayType.HOURS.
             */
            if (initialDelay.delayPeriodType != DelayType.DAYS ||
                initialDelay.delayPeriodType == DelayType.DAYS &&
                isInitialDelayOver()
            ) fetchAd()
            onAdDismissed?.invoke()
        }
    }

    private fun loadAd() {
        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAppOpenAdLoaded(loadedAd: AppOpenAd) {
                this@AppOpenManager.appOpenAd = loadedAd
                this@AppOpenManager.loadTime = getCurrentTime()
                Timber.e("Ad Loaded AOA")
            }

            override fun onAppOpenAdFailedToLoad(loadError: LoadAdError) {
//                onAdDismissed?.invoke()
                Timber.e("AOA Ad Failed To Load, Reason: ${loadError.responseInfo}")
            }
        }
        AppOpenAd.load(getApplication(), adUnitId, adRequest, orientation, loadCallback)
    }

    // Handling the visibility of App Open Ad
    private fun getFullScreenContentCallback(): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                fetchAd()
                onAdDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                onAdDismissed?.invoke()
                Timber.e("AOA Ad Failed To Show Full-Screen Content: ${adError?.message}")
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }
        }
    }
}
