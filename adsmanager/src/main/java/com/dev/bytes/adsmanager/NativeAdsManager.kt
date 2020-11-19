package com.dev.bytes.adsmanager

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Keep
import androidx.annotation.LayoutRes
import com.dev.bytes.BuildConfig
import com.dev.bytes.R
import com.facebook.ads.*
/*import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout*/
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import kotlinx.android.synthetic.main.ad_unified_splash.view.*
/*import org.jetbrains.anko.childrenRecursiveSequence*/
import timber.log.Timber
import com.dev.bytes.adsmanager.events.logEvent
import com.facebook.ads.MediaView as FBMediaView

@Keep
data class NativeAdPair(var nativeAM: UnifiedNativeAd? = null, var nativeFB: NativeAd? = null) {
    fun populate(context: Context, @LayoutRes adLayout: Int, frameLayout: FrameLayout?) {
        when {
            context.checkIfPremium() -> return
            nativeAM != null -> {
                context.inflateUnifiedAd(adLayout)?.let { layout ->
                    frameLayout?.post {
                        frameLayout.removeAllViews()
                        nativeAM!!.populateNativeAdView(layout)
                        frameLayout.addView(layout)
                        frameLayout.visibility = View.VISIBLE
                    }
                }
            }
            nativeFB != null -> {
                context.inflateNativeAdFB(adLayout)?.let { layout ->
                    frameLayout?.post {
                        frameLayout.removeAllViews()
                        nativeFB!!.populateNativeAdLayoutFB(layout)
                        frameLayout.addView(layout)
                        frameLayout.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    fun isLoaded() = nativeAM != null || nativeFB != null
}

/**
 * load native add
 * @param frameLayout ad container
 * @param adLayout ad layout to inflate (without UnifiedAdView or NativeAdLayout)
 * for now you have to set single layout for both networks e.g parent must not be (UnifiedAdView or NativeAdLayout)
 * and id has both media views(com.google.android.gms.ads.formats.MediaView / com.facebook.ads.MediaView) and AdIconView/ImageView
 * visibility is handled respectively
 * @param ADUnit ENUM implements ADUnitType (AD Id and priority will be defined in it)
 * @param isShimmer to enable shimmer effect
 * @param AMCallback to get instance of [UnifiedNativeAd] for future use e.g if in recyclerView
 * @param FBCallback to get instance of [NativeAd] for future use e.g if in recyclerView
 * */
fun Context.loadNativeAd(
    frameLayout: FrameLayout?,
    @LayoutRes adLayout: Int,
    ADUnit: ADUnitType,
    isShimmer: Boolean = false,
    AMCallback: ((UnifiedNativeAd) -> Unit)? = null,
    FBCallback: ((NativeAd) -> Unit)? = null,
    onError: (() -> Unit)? = null,
    remoteConfigKey: String? = null,
    isShowAdView: Boolean = false
) {
    if (checkIfPremium() || (remoteConfigKey != null && !remoteConfigKey.isEnabledRemotely())) {
        frameLayout?.visibility = View.GONE
        frameLayout?.postDelayed({ onError?.invoke() }, 300)
        return
    }
    when (ADUnit.priority) {
        AdsPriority.ADMOB, AdsPriority.ADMOB_FACEBOOK ->
            loadNativeAM(frameLayout, adLayout, ADUnit, AMCallback, FBCallback, onError, isShowAdView)

        AdsPriority.FACEBOOK, AdsPriority.FACEBOOK_ADMOB ->
            loadNativeFB(frameLayout, adLayout, ADUnit, FBCallback, AMCallback, onError)
    }
    if (isShimmer && frameLayout != null /*&& frameLayout?.childCount == 0*/) {
        frameLayout.removeAllViews()
        //inflateShimmerView(adLayout)?.let { frameLayout.addView(it);frameLayout.visibility = View.VISIBLE }
    }
}

private fun Context.loadNativeAM(
    frameLayout: FrameLayout?, adLayout: Int, ADUnit: ADUnitType,
    AMSuccessCallBack: ((UnifiedNativeAd) -> Unit)? = null,
    FBCallback: ((NativeAd) -> Unit)? = null,
    onError: (() -> Unit)? = null,
    isShowAdView: Boolean = false
) {
    val builder =
        AdLoader.Builder(this, getString(ADUnit.adUnitIDAM)).withNativeAdOptions(
            NativeAdOptions.Builder()
                .setMediaAspectRatio(ADUnit.mediaAspectRatio)
                .setAdChoicesPlacement(ADUnit.adChoicesPlacement)
                .build()
        )
    val adLoader =
        builder.forUnifiedNativeAd { ad ->
            inflateUnifiedAd(adLayout)?.let { adLayout ->
                frameLayout?.post {
                    frameLayout.removeAllViews()
                    ad.populateNativeAdView(adLayout)
                    frameLayout.addView(adLayout)
                    frameLayout.visibility = View.VISIBLE
                    logEvent("ad_impr_native_AM")
                }
            }
            AMSuccessCallBack?.invoke(ad)
        }.withAdListener(
            object : AdListener() {
                override fun onAdFailedToLoad(errorCode: LoadAdError) {
                    //Timber.e("onADFailed AM ${errorCode.zzdo()}")
                    if (ADUnit.priority == AdsPriority.ADMOB_FACEBOOK)
                        loadNativeFB(frameLayout, adLayout, ADUnit, FBCallback, onError = onError)
                    else {
                        onError?.invoke()
                        if (!isShowAdView)
                            return
                        frameLayout?.visibility = View.GONE
                        frameLayout?.removeAllViews()
                    }
                }

                override fun onAdClosed() {
                    Timber.e("onAdClosed ")
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Timber.e("onAdLoaded ")
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    Timber.e("onAdOpened ")
                }

            }).build()
    adLoader?.loadAd(AdRequest.Builder().build())
}

fun Context.inflateUnifiedAd(adLayout: Int): UnifiedNativeAdView? = if (adLayout == -1) null else
    (this as? Activity)?.layoutInflater?.inflate(adLayout, null)
        ?.let { UnifiedNativeAdView(this).apply { addView(it) } }

fun UnifiedNativeAd.populateNativeAdView(adView: UnifiedNativeAdView) {

    adView.ad_media?.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
    adView.ad_media?.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
        override fun onChildViewRemoved(p0: View?, p1: View?) = Unit

        override fun onChildViewAdded(parent: View?, child: View?) {
            if (child is ImageView) {
                child.adjustViewBounds = true
                child.scaleType = ImageView.ScaleType.CENTER_CROP
            }
            Timber.w("parent ${parent?.javaClass} child ${child?.javaClass}")
        }
    })
    adView.mediaView = adView.ad_media
    adView.ad_media_fb?.visibility = View.GONE

    // Register the view used for each individual asset.
    adView.headlineView = adView.ad_headline
    adView.bodyView = adView.ad_body
    adView.callToActionView = adView.ad_call_to_action
    adView.iconView = adView.ad_icon
    adView.ad_icon_fb?.visibility = View.GONE

//    adView.advertiserView = adView.ad_advertiser

    // Some assets are guaranteed to be in every UnifiedNativeAd.
    (adView.headlineView as? TextView)?.text = this.headline
    (adView.bodyView as? TextView)?.text = this.body
    (adView.callToActionView as? TextView)?.text = this.callToAction

    // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
    // check before trying to display them.

    if (this.icon == null) {
        adView.iconView?.visibility = View.GONE
    } else {
        (adView.iconView as? ImageView)?.setImageDrawable(this.icon.drawable)
        adView.iconView?.visibility = View.VISIBLE
    }


    adView.bodyView?.visibility = if (this.body.isNullOrBlank()) View.GONE else View.VISIBLE
    (adView.bodyView as? TextView?)?.text = this.body

    if (this.advertiser == null) {
        adView.advertiserView?.visibility = View.GONE
    } else {
        (adView.advertiserView as? TextView)?.text = this.advertiser
        adView.advertiserView?.visibility = View.VISIBLE
    }

    // Assign native ad object to the native view.
    adView.setNativeAd(this)
}

private fun Context.loadNativeFB(
    frameLayout: FrameLayout?, adLayout: Int, ADUnit: ADUnitType,
    FBSuccessCallBack: ((NativeAd) -> Unit)? = null,
    AMCallback: ((UnifiedNativeAd) -> Unit)? = null,
    onError: (() -> Unit)? = null

): NativeAd? {
    if (BuildConfig.DEBUG) {
        AdSettings.addTestDevice(getString(R.string.fb_test_id))
    }
    return if (!checkIfPremium()) {

        val facebookNativeAd = ADUnit.adUnitIDFB?.let { NativeAd(this, getString(it)) }

        // Request an ad
        facebookNativeAd?.loadAd(
            facebookNativeAd.buildLoadAdConfig().withAdListener(object : FBNativeAdListener() {

                override fun onAdLoaded(p0: Ad?) {
                    if (facebookNativeAd != p0) return
                    inflateNativeAdFB(adLayout)?.let { adLayout ->
                        frameLayout?.post {
                            frameLayout.removeAllViews()
                            facebookNativeAd.populateNativeAdLayoutFB(adLayout)
                            frameLayout.addView(adLayout)
                            frameLayout.visibility = View.VISIBLE
                            logEvent("ad_impr_native_FB")
                        }
                    }
                    FBSuccessCallBack?.invoke(facebookNativeAd)
                }

                override fun onError(p0: Ad?, p1: AdError?) {
                    super.onError(p0, p1)
                    if (ADUnit.priority == AdsPriority.FACEBOOK_ADMOB) {
                        loadNativeAM(frameLayout, adLayout, ADUnit, AMCallback, onError = onError)
                        Timber.e("onError native FB ${p1?.errorCode} ${p1?.errorMessage}")
                    } else {
                        onError?.invoke()
                        frameLayout?.visibility = View.GONE
                        frameLayout?.removeAllViews()
                    }
                }
            }).build()
        )
        facebookNativeAd
    } else null

}

fun NativeAd.populateNativeAdLayoutFB(adView: NativeAdLayout) {

    this.unregisterView()

    // Add the AdOptionsView
    val adChoicesContainer: LinearLayout = adView.ad_choices_container
    val adOptionsView = AdOptionsView(adView.context, this, adView)
    adChoicesContainer.addView(adOptionsView, 0)

    // Create native UI using the ad metadata.
    val nativeAdIcon = adView.ad_icon_fb?.apply { visibility = View.VISIBLE }
    val nativeAdHeadline: TextView = adView.ad_headline
    adView.ad_icon?.visibility = View.INVISIBLE // in-case of both views available
    val nativeAdMedia = adView.ad_media_fb?.apply { visibility = View.VISIBLE }
    adView.ad_media?.visibility = View.GONE // in-case of both views available
    val nativeAdCallToAction = adView.ad_call_to_action

    // Set the Text.
    nativeAdHeadline.text = this.advertiserName
    adView.ad_body?.text = this.adBodyText
    nativeAdCallToAction.visibility =
        if (this.hasCallToAction()) View.VISIBLE else View.INVISIBLE
    nativeAdCallToAction.text = this.adCallToAction

    // Create a list of clickable views
    val clickableViews = arrayListOf<View>(nativeAdHeadline, nativeAdCallToAction)

    // Register the Title and CTA button to listen for clicks.
    registerViewForInteraction(
        adView,
        nativeAdMedia,
        nativeAdIcon,
        clickableViews
    )
}

fun Context.inflateNativeAdFB(adLayout: Int): NativeAdLayout? = if (adLayout == -1) null else
    (this as? Activity)?.layoutInflater?.inflate(adLayout, null)
        ?.let { NativeAdLayout(this).apply { addView(it) } }

/*fun Context.inflateShimmerView(adLayout: Int): ShimmerFrameLayout? =
    (this as? Activity)?.layoutInflater?.inflate(adLayout, null)
        ?.let { layout ->
            layout.childrenRecursiveSequence().iterator().forEach { view ->
                when (view) {
                    !is ViewGroup -> {
                        view.apply { setBackgroundColor(Color.GRAY) }
                        when (view) {
//                            is TextView -> view.text = ""
                            is ImageView -> view.setImageDrawable(null)
                        }
                    }
                    is AdIconView, is FBMediaView, is MediaView -> view.setBackgroundColor(Color.GRAY)
                    is ConstraintLayout -> view.setBackgroundColor(Color.TRANSPARENT)
                }
            }
            ShimmerFrameLayout(this).apply {
                addView(layout)
                this.setShimmer(
                    Shimmer.ColorHighlightBuilder().setBaseColor(Color.GRAY)
                        .setHighlightColor(Color.WHITE).build()
                )
                this.stopShimmer()
                this.showShimmer(false)
            }
        }*/

abstract class FBNativeAdListener : NativeAdListener {
    override fun onAdClicked(p0: Ad?) {}

    override fun onMediaDownloaded(p0: Ad?) {}

    override fun onError(p0: Ad?, p1: AdError?) {
        Timber.e("onError ${p1?.errorMessage} ${p1?.errorCode}")
    }

    override fun onAdLoaded(p0: Ad?) {}

    override fun onLoggingImpression(p0: Ad?) {}
}