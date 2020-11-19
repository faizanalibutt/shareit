package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.dialog

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.marginTop
import com.dev.bytes.adsmanager.ADUnitPlacements
import com.dev.bytes.adsmanager.loadNativeAd
import com.facebook.ads.NativeAdsManager

/*import com.code4rox.adsmanager.NativeAdsIdType*/
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.Activity
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback.Callback
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.AppUtils
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.NetworkUtils
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.TinyDB
import kotlinx.android.synthetic.main.layout_exit_rating_dialog.view.*

class ExitDialogue(val activity: Activity, val title: String, val adsVisible: Boolean) :
    AlertDialog.Builder(activity, R.style.Widget_Hazel_AppCompat_DialogTheme) {

    init {
        kotlin.runCatching {
            val mRootView =
                LayoutInflater.from(activity).inflate(R.layout.layout_exit_rating_dialog, null)

            setView(mRootView)
            setTitle(title)

            if (adsVisible && NetworkUtils.isOnline(activity)) {
                /*val admobUtils = AdmobUtils(activity)
                admobUtils.loadNativeAd(
                    mRootView.fl_adplaceholder,
                    R.layout.ad_unified_3,
                    NativeAdsIdType.EXIT_NATIVE_AM
                )
                admobUtils.setNativeAdListener(object : AdmobUtils.NativeAdListener {
                    override fun onNativeAdLoaded() {
                        mRootView.rating_group.visibility = View.VISIBLE
                        mRootView.view.visibility = View.VISIBLE
                    }

                    override fun onNativeAdError() {
                        mRootView.view.visibility = View.GONE
                    }
                })*/
                activity.loadNativeAd(
                    mRootView.fl_adplaceholder,
                    R.layout.ad_unified_3,
                    ADUnitPlacements.EXIT_NATIVE_AD,
                    true,
                    AMCallback = {
                        mRootView.view.visibility = View.VISIBLE
                    },
                    onError = {
                        mRootView.view.visibility = View.GONE
                        mRootView.rating_group.visibility = View.GONE
                    },
                    isShowAdView = true
                )
            }

            setPositiveButton(title) { _, _ ->
                if (adsVisible) {
                    activity.finish()
                } else {
                    val url = activity.getString(R.string.app_link)
                    val builderTab = CustomTabsIntent.Builder()
                    val customTabsIntent = builderTab.build()
                    customTabsIntent.launchUrl(context, Uri.parse(url))
                }
            }

            setNegativeButton("Cancel", null)

            if (adsVisible && AppUtils.getDefaultPreferences(activity)
                    .getBoolean("hide_rating", false)
            ) {
                mRootView.rating_bar_value.visibility = View.GONE
                mRootView.view.visibility = View.GONE
                mRootView.dialog_desc.visibility = View.GONE
                mRootView.exit_desc.visibility = View.VISIBLE
            } else if (adsVisible && !TinyDB.getInstance(activity)
                    .getBoolean(activity.getString(R.string.is_premium))
            ) {
                mRootView.view.visibility = View.VISIBLE
            }

            mRootView.rating_bar_value.onRatingBarChangeListener =
                RatingBar.OnRatingBarChangeListener { _, rating, _ ->
                    Callback.setRating(rating)
                }

        }.onFailure {
            // here you can send developer message
        }
    }
}