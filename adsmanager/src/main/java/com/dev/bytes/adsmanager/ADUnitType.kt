package com.dev.bytes.adsmanager

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.dev.bytes.R
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.facebook.ads.AdSize as AdSizeFB

@Keep
interface ADUnitType {
    var adUnitIDAM: Int
    var adUnitIDFB: Int?
    var mediaAspectRatio: Int
    var adChoicesPlacement: Int
    var priority: AdsPriority
}

@Keep
interface BannerADUnit : ADUnitType {
    var adSizeAM: AdSize?
    var adSizeFB: AdSizeFB?
}

@Keep
enum class AdsPriority {
    ADMOB,
    FACEBOOK,
    ADMOB_FACEBOOK,
    FACEBOOK_ADMOB
}


fun String.getPriorityRemotely() = kotlin.runCatching {
    val p = FirebaseRemoteConfig.getInstance().getLong(this).toInt()
    AdsPriority.values()[p]
}.getOrNull()

/**
 * you can declare this enum by your own app logic
 * if priority is different for same Ad-ID you must declare multiple properties e.g
 * @property INTER_ON_ACTION_AD
 * */
@Keep
enum class ADUnitPlacements(
    @StringRes override var adUnitIDAM: Int, @StringRes override var adUnitIDFB: Int? = null,
    override var mediaAspectRatio: Int = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE,
    override var adChoicesPlacement: Int = NativeAdOptions.ADCHOICES_TOP_RIGHT,
    override var priority: AdsPriority = AdsPriority.ADMOB // by default it will call ADMob only
) : ADUnitType {

    //val isMainNativePriority = FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtils.IS_SETTING_BANNER)
    SPLASH_NATIVE_AD(
        R.string.splash_native_am,
        mediaAspectRatio = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_ANY
    ),
    MAIN_MM_NATIVE_AD(
        R.string.mm_native_am,
        R.string.mm_native_fb,
        mediaAspectRatio = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE,
        priority = AdsPriority.ADMOB/*when (RemoteConfigUtils.compassObjRemoteConfig?.getInt(RemoteConfigUtils.IS_MAIN_NATIVE_PRIOR)) {
            0 -> AdsPriority.ADMOB
            1 -> AdsPriority.FACEBOOK
            2 -> AdsPriority.ADMOB_FACEBOOK
            3 -> AdsPriority.FACEBOOK_ADMOB
            else -> AdsPriority.ADMOB
        }*/
    ),
    EXIT_NATIVE_AD(
        R.string.exit_native_am,
        R.string.exit_native_fb,
        mediaAspectRatio = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE,
        priority = AdsPriority.FACEBOOK_ADMOB
    ),
    COMMON_NATIVE_AD(
        R.string.mm_native_am,
        R.string.mm_native_fb
    ),
    SPLASH_INTERSTITIAL(
        R.string.splash_inter_am
    ),
    PEDO_START_STOP_INTERSTITIAL(
        R.string.mm_inter_am,
        R.string.mm_inter_fb
    ),
    PEDO_BACK_INTERSTITIAL(
        R.string.mm_inter_am,
        R.string.mm_inter_fb,
        priority = AdsPriority.FACEBOOK_ADMOB
    ),
    SPEEDO_START_STOP_INTERSTITIAL(
        R.string.mm_inter_am,
        R.string.mm_inter_fb
    ),
    SPEEDO_BACK_INTERSTITIAL(
        R.string.mm_inter_am,
        R.string.mm_inter_fb,
        priority = AdsPriority.FACEBOOK_ADMOB
    )
}

enum class BannerPlacements(
    @StringRes override var adUnitIDAM: Int, @StringRes override var adUnitIDFB: Int? = null,
    override var mediaAspectRatio: Int = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE,
    override var adChoicesPlacement: Int = NativeAdOptions.ADCHOICES_TOP_RIGHT,
    override var priority: AdsPriority = AdsPriority.ADMOB, // by default it will call ADMob only
    override var adSizeAM: AdSize? = null, // by default it will adaptive banner
    override var adSizeFB: AdSizeFB? = AdSizeFB.BANNER_HEIGHT_50
) : BannerADUnit {
    BANNER_AD(R.string.banner_am)
}