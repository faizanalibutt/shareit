package com.dev.bytes.adsmanager

import com.dev.bytes.BuildConfig
import com.dev.bytes.adsmanager.InterDelayTimer.INTERSTITIAL_DELAY_TIME
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.json.JSONException
import org.json.JSONObject

object RemoteConfigUtils {


    private const val FETCH_TIME_INTERVAL = 1L//1 * 60 * 60L   //  hours   minutes    seconds
    const val IS_SPLASH_NATIVE = "is_splash_native"
    const val IS_SPLASH_INTER = "is_splash_inter"
    const val IS_SETTING_BANNER = "is_setting_banner"
    const val IS_EXIT_NATIVE = "is_exit_native"
    const val IS_MAIN_NATIVE = "is_main_native"
    const val IS_MAIN_NATIVE_PRIOR = "is_main_native_prior"
    const val IS_COMPASS_APP = "compass_app"
    val compassObj = JSONObject()
    var compassObjRemoteConfig: JSONObject? = null


    init {
        try {
            compassObj.put(IS_SPLASH_NATIVE, true)
            compassObj.put(IS_SPLASH_INTER, true)
            compassObj.put(IS_SETTING_BANNER, true)
            compassObj.put(IS_EXIT_NATIVE, true)
            compassObj.put(IS_MAIN_NATIVE, true)
            compassObj.put(IS_MAIN_NATIVE_PRIOR, 3)
            compassObjRemoteConfig = JSONObject(
                FirebaseRemoteConfig.getInstance().getValue(RemoteConfigUtils.IS_COMPASS_APP)
                    .asString()
            )
            if (compassObjRemoteConfig?.has(RemoteConfigUtils.IS_MAIN_NATIVE) == false)
                compassObjRemoteConfig = compassObj
        } catch (jsonExp: JSONException) {
            jsonExp.printStackTrace()
            compassObjRemoteConfig = null
            compassObjRemoteConfig = compassObj
        }
    }


    fun createConfigSettings(): FirebaseRemoteConfig {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            //.setDeveloperModeEnabled(!BuildConfig.DEBUG)
            .setMinimumFetchIntervalInSeconds(FETCH_TIME_INTERVAL)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(
            mapOf(

//                    IS_BOTTOM_BANNER_SHOW to false,
                INTERSTITIAL_DELAY_TIME to 30,
                IS_SPLASH_NATIVE to false,
                IS_SPLASH_INTER to false,
                IS_SETTING_BANNER to false,
                IS_MAIN_NATIVE to false,
                IS_MAIN_NATIVE_PRIOR to 3,
                IS_EXIT_NATIVE to false,
                IS_COMPASS_APP to compassObj

            )
        )
        return remoteConfig
    }


}