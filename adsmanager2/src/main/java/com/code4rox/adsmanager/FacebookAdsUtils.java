//package com.code4rox.adsmanager;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.facebook.ads.Ad;
//import com.facebook.ads.AdError;
//import com.facebook.ads.AdIconView;
//import com.facebook.ads.AdOptionsView;
//import com.facebook.ads.AdSettings;
//import com.facebook.ads.AdSize;
//import com.facebook.ads.AdView;
//import com.facebook.ads.InterstitialAd;
//import com.facebook.ads.InterstitialAdListener;
//import com.facebook.ads.NativeAd;
//import com.facebook.ads.NativeAdLayout;
//import com.facebook.ads.NativeAdListener;
//import com.google.firebase.analytics.FirebaseAnalytics;
//import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
//public class FacebookAdsUtils {
//
//    FacebookNativeAdListener facebookNativeAdListener;
//    private Context context;
//    private FacebookInterstitialListner facebookListner;
//    private boolean bannerSize = false;
//    private boolean check = false;
//    private AdView mAdView;
//    private InterstitialAd facebookInterstitialAd = null;
//
//
//    private NativeAd facebookNativeAd;
//    private NativeAdLayout nativeAdLayout;
//    private int fbNativeAdLayout = -1;
//    private InterAdsIdType interAdsIdType;
//    private NativeAdsIdType nativeAdsIdType;
//
//    public FacebookAdsUtils(Context context, FacebookInterstitialListner facebookListner, InterAdsIdType interAdsIdType) {
//        this.context = context;
//        this.facebookListner = facebookListner;
//        this.interAdsIdType = interAdsIdType;
//        facebookInterstitialAd = fbInterstitialAd();
//    }
//
//    // Just
//    public FacebookAdsUtils(Context context) {
//        this.context = context;
//    }
//
//    public void loadFacebookNativeAd(NativeAdLayout nativeAdLayout, int fbNativeAdLayout, NativeAdsIdType nativeAdsIdType) {
//        this.nativeAdLayout = nativeAdLayout;
//        this.fbNativeAdLayout = fbNativeAdLayout;
//        this.nativeAdsIdType = nativeAdsIdType;
//        this.facebookNativeAd = loadFacebookNativeAd();
//    }
//
//    public void setNativeFbListener(FacebookNativeAdListener facebookNativeAdListener) {
//        this.facebookNativeAdListener = facebookNativeAdListener;
//    }
//
//    public boolean loadFbNativeAd() {
//        if (facebookNativeAd != null && facebookNativeAd.isAdLoaded()) {
//            inflateAd(nativeAdLayout, fbNativeAdLayout);
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    private NativeAd loadFacebookNativeAd() {
//        // Instantiate a NativeAd object.
//        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
//        // now, while you are testing and replace it later when you have signed up.
//        // While you are using this temporary code you will only get test ads and if you release
//        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
//        if (BuildConfig.DEBUG) {
//            AdSettings.addTestDevice(context.getResources().getString(R.string.fb_test_id));
//        }
//
//        if (!TinyDB.getInstance(context).getBoolean(Constants.IS_PREMIUM)) {
//
//            String nativeAdId = "";
//
//            if (nativeAdsIdType == NativeAdsIdType.SPLASH_NATIVE_FB) {
//                nativeAdId = context.getResources().getString(R.string.splash_inter_fb);
//            } else if (nativeAdsIdType == NativeAdsIdType.MM_NATIVE_FB) {
//                nativeAdId = context.getResources().getString(R.string.mm_native_fb);
//            } else if (nativeAdsIdType == NativeAdsIdType.ADJUST_NATIVE_FB) {
//                nativeAdId = context.getResources().getString(R.string.adjust_native_fb);
//            }
//            NativeAd facebookNativeAd = new NativeAd(context, nativeAdId);
////        facebookNativeAd = new NativeAd(this, "YOUR_PLACEMENT_ID");
//
//
//            facebookNativeAd.setAdListener(new NativeAdListener() {
//                @Override
//                public void onMediaDownloaded(Ad ad) {
//                    // Native ad finished downloading all assets
//                    Log.e("ADs", "Native ad finished downloading all assets.");
//                }
//
//
//                @Override
//                public void onError(Ad ad, AdError adError) {
//                    // Native ad failed to load
////                refreshAd(mFlAdplaceholder);
//                    if (facebookNativeAdListener != null) {
//                        facebookNativeAdListener.onFbNativeAdError();
//                    }
//                    Log.e("ADs", "Native ad failed to load: " + adError.getErrorMessage());
//                }
//
//                @Override
//                public void onAdLoaded(Ad ad) {
//                    // Native ad is loaded and ready to be displayed
//                    // Race condition, load() called again before last ad was displayed
//                    if (facebookNativeAdListener != null) {
//                        facebookNativeAdListener.onFbNativeAdLoaded();
//                    }
//                    inflateAd(nativeAdLayout, fbNativeAdLayout);
//                    Log.d("ads", "Native ad is loaded and ready to be displayed!");
//                    FirebaseAnalytics.getInstance(context).logEvent("fb_show_native", new Bundle());
//
//                }
//
//
//                @Override
//                public void onAdClicked(Ad ad) {
//                    // Native ad clicked
//                    Log.d("ads", "Native ad clicked!");
//                    FirebaseAnalytics.getInstance(context).logEvent("fb_click_native", new Bundle());
//
//                }
//
//                @Override
//                public void onLoggingImpression(Ad ad) {
//                    // Native ad impression
//                    Log.d("ads", "Native ad impression logged!");
//                }
//            });
//
//            // Request an ad
//            facebookNativeAd.loadAd();
//            return facebookNativeAd;
//
//        } else {
//            return null;
//        }
//
//    }
//
//    private void inflateAd(NativeAdLayout nativeAdLayout, int fbNativeAdLayout) {
//
//
//        if (facebookNativeAd != null && nativeAdLayout != null && fbNativeAdLayout != -1) {
//
//            facebookNativeAd.unregisterView();
//
//            // Add the Ad view into the ad container.
//            //nativeAdLayout = findViewById(R.id.native_ad_container);
//            LayoutInflater inflater = LayoutInflater.from(context);
//            // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
//            LinearLayout adView = (LinearLayout) inflater.inflate(fbNativeAdLayout, nativeAdLayout, false);
//            nativeAdLayout.addView(adView);
//
//            // Add the AdOptionsView
//            LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
//            AdOptionsView adOptionsView = new AdOptionsView(context, facebookNativeAd, nativeAdLayout);
//            adChoicesContainer.addView(adOptionsView, 0);
//
//            // Create native UI using the ad metadata.
//            AdIconView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
//            TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
//            com.facebook.ads.MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
////            TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
//            TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
////            TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
//            Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);
//
//            // Set the Text.
//            nativeAdTitle.setText(facebookNativeAd.getAdvertiserName());
//            nativeAdBody.setText(facebookNativeAd.getAdBodyText());
////            nativeAdSocialContext.setText(facebookNativeAd.getAdSocialContext());
//            nativeAdCallToAction.setVisibility(facebookNativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
//            nativeAdCallToAction.setText(facebookNativeAd.getAdCallToAction());
////            sponsoredLabel.setText(facebookNativeAd.getSponsoredTranslation());
//
//            // Create a list of clickable views
//            List<View> clickableViews = new ArrayList<>();
//            clickableViews.add(nativeAdTitle);
//            clickableViews.add(nativeAdCallToAction);
//
//            // Register the Title and CTA button to listen for clicks.
//            facebookNativeAd.registerViewForInteraction(
//                    adView,
//                    nativeAdMedia,
//                    nativeAdIcon,
//                    clickableViews);
//
//        }
//    }
//
//    public void loadBannerAd(LinearLayout adContainer) {
//        if (!TinyDB.getInstance(context).getBoolean(Constants.IS_PREMIUM)) {
//
//            // AdSettings.addTestDevice("f14b043c-f22a-46a9-9669-2611d8d5ca98");
//            mAdView = new AdView(context, context.getResources().getString(R.string.banner_fb), AdSize.BANNER_HEIGHT_50);
//     /*   if(BuildConfig.DEBUG){
//            AdSettings.addTestDevice("47926836-7dd8-4f0d-bb71-2c56ecd4ca5b");
//            mAdView = new AdView(this, "YOUR_PLACEMENT_ID", AdSize.BANNER_HEIGHT_50);
//        }else{*/
//            // mAdView = new AdView(this, getResources().getString(R.string.fb_banner_ad_unit_id), AdSize.BANNER_HEIGHT_50);
////        }
//            adContainer.addView(mAdView);
//            // Request an ad
//            mAdView.loadAd();
//        }
//    }
//
//    public void loadRecBannerAd(LinearLayout adContainer) {
//        if (!TinyDB.getInstance(context).getBoolean(Constants.IS_PREMIUM) && FirebaseRemoteConfig.getInstance().getBoolean(Constants.FIRE_IS_RECT_BANNER_SHOW)) {
//
//            if (!FirebaseRemoteConfig.getInstance().getBoolean(Constants.FIRE_IS_SMALL_RECT_BANNER)) {
//
////                AdSettings.addTestDevice("f14b043c-f22a-46a9-9669-2611d8d5ca98");
//                mAdView = new AdView(context, context.getResources().getString(R.string.banner_fb), AdSize.RECTANGLE_HEIGHT_250);
//
//                //  mAdView = new AdView(context, "586817545143311_586818221809910", AdSize.RECTANGLE_HEIGHT_250);
//            } else {
////                AdSettings.addTestDevice("f14b043c-f22a-46a9-9669-2611d8d5ca98");
//                mAdView = new AdView(context, context.getResources().getString(R.string.banner_fb), AdSize.BANNER_HEIGHT_90);
////                mAdView = new AdView(context, "586817545143311_586818221809910", AdSize.BANNER_HEIGHT_90);
//            }
//
//
//     /*   if(BuildConfig.DEBUG){
//            AdSettings.addTestDevice("47926836-7dd8-4f0d-bb71-2c56ecd4ca5b");
//            mAdView = new AdView(this, "YOUR_PLACEMENT_ID", AdSize.BANNER_HEIGHT_50);
//        }else{*/
//            // mAdView = new AdView(this, getResources().getString(R.string.fb_banner_ad_unit_id), AdSize.BANNER_HEIGHT_50);
////        }
//            adContainer.addView(mAdView);
//            // Request an ad
//            mAdView.loadAd();
//        }
//    }
//
//
//    public void loadExitRecBannerAd(LinearLayout adContainer) {
//        if (!TinyDB.getInstance(context).getBoolean(Constants.IS_PREMIUM)) {
//
////            mAdView = new AdView(context, "586817545143311_586818221809910", AdSize.RECTANGLE_HEIGHT_250);
////            AdSettings.addTestDevice("f14b043c-f22a-46a9-9669-2611d8d5ca98");
//            mAdView = new AdView(context, context.getResources().getString(R.string.banner_fb), AdSize.RECTANGLE_HEIGHT_250);
//
//
//
//     /*   if(BuildConfig.DEBUG){
//            AdSettings.addTestDevice("47926836-7dd8-4f0d-bb71-2c56ecd4ca5b");
//            mAdView = new AdView(this, "YOUR_PLACEMENT_ID", AdSize.BANNER_HEIGHT_50);
//        }else{*/
//            // mAdView = new AdView(this, getResources().getString(R.string.fb_banner_ad_unit_id), AdSize.BANNER_HEIGHT_50);
////        }
//            adContainer.addView(mAdView);
//            // Request an ad
//            mAdView.loadAd();
//        }
//    }
//
//
//    public InterstitialAd getInterstitialAd() {
//        return facebookInterstitialAd;
//    }
//
//
//    public AdView getFacebookBanner() {
//        if (mAdView != null) {
//            return mAdView;
//        } else {
//            return null;
//        }
//    }
//
//    public boolean showFbInterstitialAd() {
//        if (facebookInterstitialAd != null && facebookInterstitialAd.isAdLoaded() && !facebookInterstitialAd.isAdInvalidated()) {
//            facebookInterstitialAd.show();
//            Log.d("finish2", "onFinish: ");
//
//            return true;
//        } else {
//            Log.d("finish3", "onFinish: ");
//            return false;
//
//        }
//
//    }
//
//    private InterstitialAd fbInterstitialAd() {
//
//
//        if (!TinyDB.getInstance(context).getBoolean(Constants.IS_PREMIUM)) {
////            InterstitialAd interstitialAd = new InterstitialAd(context, "YOUR_PLACEMENT_ID");
//            if (BuildConfig.DEBUG) {
//                AdSettings.addTestDevice(context.getResources().getString(R.string.fb_test_id));
//            }
//            String adId = "";
//            if (interAdsIdType == InterAdsIdType.SPLASH_INTER_FB) {
//                adId = context.getString(R.string.splash_inter_fb);
//            } else if (interAdsIdType == InterAdsIdType.INTER_FB) {
//                adId = context.getString(R.string.inter_fb);
//            }
//            InterstitialAd interstitialAd = new InterstitialAd(context, adId);
//
////        val interstitialAd: com.facebook.ads.InterstitialAd =
//                /*if (BuildConfig.DEBUG)
//        {
//            AdSettings.addTestDevice("47926836-7dd8-4f0d-bb71-2c56ecd4ca5b")
//            com.facebook.ads.InterstitialAd(this, "YOUR_PLACEMENT_ID"*//*getResources().getString(R.string.fb_interstitial_ad_unit_id)*//*)
//        }
//        else
//        {*/
//            //      com.facebook.ads.InterstitialAd(this, resources.getString(R.string.fb_interstitial_ad_unit_id))
////        }
//
//
//            interstitialAd.setAdListener(new InterstitialAdListener() {
//                @Override
//                public void onInterstitialDisplayed(Ad ad) {
//                    FirebaseAnalytics.getInstance(context).logEvent("fb_show_inter", new Bundle());
//                    TinyDB.getInstance(context).putBoolean(Constants.CHECK_INTER_AD_SHOW, true);
//
//                }
//
//                @Override
//                public void onInterstitialDismissed(Ad ad) {
//                    facebookInterstitialAd = fbInterstitialAd();
//                    if (facebookListner != null) {
//                        facebookListner.onFbInterstitialAdClose();
//                    }
//                    TinyDB.getInstance(context).putBoolean(Constants.CHECK_INTER_AD_SHOW, false);
//
//
//                }
//
//                @Override
//                public void onError(Ad ad, AdError adError) {
//                    int i = 0 ;
//
//                    if (facebookListner != null) {
//                        facebookListner.onFbInterstitialAdFailed();
//                    }
//                }
//
//                @Override
//                public void onAdLoaded(Ad ad) {
//                    int i = 0 ;
//                    if (facebookListner != null) {
//                        facebookListner.onFbInterstitialAdLoaded();
//                    }
//
//                }
//
//                @Override
//                public void onAdClicked(Ad ad) {
//                    FirebaseAnalytics.getInstance(context).logEvent("fb_click_inter", new Bundle());
//                    if (facebookListner != null) {
//                        facebookListner.onFbInterstitialAdClick();
//                    }
//                }
//
//                @Override
//                public void onLoggingImpression(Ad ad) {
//                    if (facebookListner != null) {
//                        facebookListner.onFbInterstitialAdImpression();
//                    }
//                }
//
//
//            });
//            interstitialAd.loadAd();
//            return interstitialAd;
//        } else {
//            return null;
//        }
//
//    }
//
//
//    public Dialog initDialogNative(int nativeDialogLayout, int customStyle) {
//
//        Dialog dialogNative;
//        dialogNative = new Dialog(context, customStyle);
//        dialogNative.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        Objects.requireNonNull(dialogNative.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
//        dialogNative.setContentView(nativeDialogLayout);
//        dialogNative.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        dialogNative.getWindow().setDimAmount(0.5f);
//        dialogNative.setCancelable(false);
//        return dialogNative;
//
//    }
//
//
//    public void destroyNativeAd(){
//        if(facebookNativeAd != null){
//            facebookNativeAd.destroy();
//        }
//    }
//
//    public interface FacebookInterstitialListner {
//        void onFbInterstitialAdClose();
//
//        void onFbInterstitialAdLoaded();
//
//        void onFbInterstitialAdFailed();
//
//        void onFbInterstitialAdClick();
//
//        void onFbInterstitialAdImpression();
//    }
//
//    public interface FacebookNativeAdListener {
//        void onFbNativeAdLoaded();
//
//        void onFbNativeAdError();
//    }
//
//
//    //facebook ad dependency
//    // implementation 'com.facebook.android:audience-network-sdk:5.1.0'
//
//    // Xml code
//   /*
//    <LinearLayout
//    android:id="@+id/banner_container"
//    android:layout_width="match_parent"
//    android:layout_height="wrap_content"
//    android:orientation="vertical"/>
//            */
//
//
//    //native ad xml
//
//   /*
//    <ScrollView
//    android:layout_width="match_parent"
//    android:layout_height="match_parent">
//
//        <com.facebook.ads.NativeAdLayout
//    android:id="@+id/native_ad_container"
//    android:layout_width="match_parent"
//    android:layout_height="wrap_content"
//    android:orientation="vertical" />
//    </ScrollView>*/
//}
//
