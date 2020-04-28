//package com.code4rox.adsmanager;
//
//import android.app.Activity;
//import android.content.Context;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.FrameLayout;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.firebase.analytics.FirebaseAnalytics;
//import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
//import com.mopub.common.MoPub;
//import com.mopub.common.SdkConfiguration;
//import com.mopub.common.SdkInitializationListener;
//import com.mopub.mobileads.MoPubErrorCode;
//import com.mopub.mobileads.MoPubInterstitial;
//import com.mopub.mobileads.MoPubView;
//import com.mopub.nativeads.AdapterHelper;
//import com.mopub.nativeads.FacebookAdRenderer;
//import com.mopub.nativeads.GooglePlayServicesAdRenderer;
//import com.mopub.nativeads.MediaViewBinder;
//import com.mopub.nativeads.MoPubNative;
//import com.mopub.nativeads.MoPubNativeAdLoadedListener;
//import com.mopub.nativeads.MoPubNativeAdPositioning;
//import com.mopub.nativeads.MoPubRecyclerAdapter;
//import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
//import com.mopub.nativeads.MoPubVideoNativeAdRenderer;
//import com.mopub.nativeads.NativeAd;
//import com.mopub.nativeads.NativeErrorCode;
//import com.mopub.nativeads.RequestParameters;
//import com.mopub.nativeads.ViewBinder;
//
//import java.util.Date;
//import java.util.EnumSet;
//
//public class MopubUtils {
//
//    private Context mContext;
//    private MoPubInterstitial mMoPubInterstitial;
//    private MoPubInterstitial mMoPubInterstitialTorchOn;
//    private MopubInterstitialListener mopubListener;
//    private MopubInterstitialListener mopubListenerTorchOn;
//    private InterAdsIdType interAdsIdType;
//    private InterAdsIdType interAdsIdTypeTorchOn;
//    private MoPubNative moPubNative;
//    private boolean isMoPubNativeLoaded = false;
//    private MoPubRecyclerAdapter myMoPubAdapter;
//    private long sTime = 0L;
//
//    //..............Constructor......................//
//    public MopubUtils(Context context) {
//        mContext = context;
//    }
//
//    //..............Configuration......................//
//    public interface MopubSDKInitializationListener {
//        void onSDKInitializationFinished();
//    }
//
//    public void sdkConfiguration(MopubSDKInitializationListener initializationListener) {
//        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(mContext.getResources().getString(R.string.ad_app_id_mp)).build();
//        MoPub.initializeSdk(mContext, sdkConfiguration, new SdkInitializationListener() {
//            @Override
//            public void onInitializationFinished() {
//                if (initializationListener != null) {
//                    initializationListener.onSDKInitializationFinished();
//
//                }
//            }
//        });
//    }
//
//    //..............Interstitial......................//
//    public interface MopubInterstitialListener {
//        void onInterstitialAdClose();
//
//        void onInterstitialAdLoaded();
//
//        void onInterstitialAdFailed();
//    }
//
//    public void loadInterstitial(MopubInterstitialListener mopubListener, InterAdsIdType interAdsIdType) {
//        this.interAdsIdType = interAdsIdType;
//        this.mopubListener = mopubListener;
//        mMoPubInterstitial = newInterstitialAd();
//    }
//
//    private MoPubInterstitial newInterstitialAd() {
//        if (!TinyDB.getInstance(mContext).getBoolean(Constants.IS_PREMIUM)) {
//
//            String adId = "";
//            if (interAdsIdType == InterAdsIdType.SPLASH_INTER_MP) {
//                adId = mContext.getString(R.string.splash_inter_mp);
//            } else if (interAdsIdType == InterAdsIdType.INTER_MP) {
//                adId = mContext.getString(R.string.inter_mp);
//            } else if (interAdsIdType == InterAdsIdType.TORCH_ON_INTER_MP) {
//                adId = mContext.getString(R.string.torch_on_inter_mp);
//            }
//
//            MoPubInterstitial interstitialAd = new MoPubInterstitial((Activity) mContext, adId);
//            interstitialAd.load();
//            interstitialAd.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
//
//                @Override
//                public void onInterstitialLoaded(MoPubInterstitial interstitial) {
//                    if (mopubListener != null) {
//                        mopubListener.onInterstitialAdLoaded();
//                    }
//                }
//
//                @Override
//                public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
//                    if (mopubListener != null) {
//                        mopubListener.onInterstitialAdFailed();
//
//                    }
//                }
//
//                @Override
//                public void onInterstitialShown(MoPubInterstitial interstitial) {
//                    FirebaseAnalytics.getInstance(mContext).logEvent("mp_show_inter", new Bundle());
//                }
//
//                @Override
//                public void onInterstitialClicked(MoPubInterstitial interstitial) {
//                    FirebaseAnalytics.getInstance(mContext).logEvent("mp_click_inter", new Bundle());
//                }
//
//                @Override
//                public void onInterstitialDismissed(MoPubInterstitial interstitial) {
//                    mMoPubInterstitial = newInterstitialAd();
//                    if (mopubListener != null) {
//                        mopubListener.onInterstitialAdClose();
//                    }
//                    TinyDB.getInstance(mContext).putBoolean(Constants.CHECK_INTER_AD_SHOW, false);
//                }
//            });
//            return interstitialAd;
//        } else {
//            return null;
//        }
//    }
//
//    public MoPubInterstitial getMoPubInterstitial() {
//        return mMoPubInterstitial;
//    }
//
//    public boolean showInterstitialAd() {
//
//        long cTime = new Date().getTime();
//        long fTime = cTime - sTime;
//        int ffTime = (int) ((fTime / 1000) % 60);
//
//        if (mMoPubInterstitial != null && mMoPubInterstitial.isReady() &&  ffTime > FirebaseRemoteConfig.getInstance().getLong(Constants.FIREBASE_INTER_SHOW_TIME_IN_SEC)) {
//            mMoPubInterstitial.show();
//            Log.d("finish3", "onFinish: ");
//            sTime = new Date().getTime();
//
//            return true;
//        } else {
//            return false;
//        }
//    }
//    public boolean showSplashInterstitialAd() {
//
//        if (mMoPubInterstitial != null && mMoPubInterstitial.isReady()) {
//            mMoPubInterstitial.show();
//            Log.d("finish3", "onFinish: ");
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//
//    public void loadInterstitialTorchOn(MopubInterstitialListener mopubListener, InterAdsIdType interAdsIdType) {
//        this.interAdsIdTypeTorchOn = interAdsIdType;
//        this.mopubListenerTorchOn = mopubListener;
//        mMoPubInterstitialTorchOn = newInterstitialAdTorchOn();
//    }
//
//    private MoPubInterstitial newInterstitialAdTorchOn() {
//        if (!TinyDB.getInstance(mContext).getBoolean(Constants.IS_PREMIUM)) {
//
//            String adId = "";
//            if (interAdsIdType == InterAdsIdType.SPLASH_INTER_MP) {
//                adId = mContext.getString(R.string.splash_inter_mp);
//            } else if (interAdsIdType == InterAdsIdType.INTER_MP) {
//                adId = mContext.getString(R.string.inter_mp);
//            } else if (interAdsIdType == InterAdsIdType.TORCH_ON_INTER_MP) {
//                adId = mContext.getString(R.string.torch_on_inter_mp);
//            }
//
//            MoPubInterstitial interstitialAd = new MoPubInterstitial((Activity) mContext, adId);
//            interstitialAd.load();
//            interstitialAd.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
//
//                @Override
//                public void onInterstitialLoaded(MoPubInterstitial interstitial) {
//                    if (mopubListenerTorchOn != null) {
//                        mopubListenerTorchOn.onInterstitialAdLoaded();
//                    }
//                }
//
//                @Override
//                public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
//                    if (mopubListenerTorchOn != null) {
//                        mopubListenerTorchOn.onInterstitialAdFailed();
//
//                    }
//                }
//
//                @Override
//                public void onInterstitialShown(MoPubInterstitial interstitial) {
//                    FirebaseAnalytics.getInstance(mContext).logEvent("mp_show_inter", new Bundle());
//                }
//
//                @Override
//                public void onInterstitialClicked(MoPubInterstitial interstitial) {
//                    FirebaseAnalytics.getInstance(mContext).logEvent("mp_click_inter", new Bundle());
//                }
//
//                @Override
//                public void onInterstitialDismissed(MoPubInterstitial interstitial) {
//                    mMoPubInterstitialTorchOn = newInterstitialAdTorchOn();
//                    Log.d("Timer", "Admob +");
//
//                    if (mopubListenerTorchOn != null) {
//                        mopubListenerTorchOn.onInterstitialAdClose();
//                    }
//                    TinyDB.getInstance(mContext).putBoolean(Constants.CHECK_INTER_AD_SHOW, false);
//                }
//            });
//            return interstitialAd;
//        } else {
//            return null;
//        }
//    }
//
//    public MoPubInterstitial getMoPubInterstitialTorchOn() {
//        return mMoPubInterstitialTorchOn;
//    }
//
//    public boolean showInterstitialAdTorchOn() {
//        if (mMoPubInterstitialTorchOn != null && mMoPubInterstitialTorchOn.isReady() && !TinyDB.getInstance(mContext).getBoolean(Constants.IS_PREMIUM)) {
//            mMoPubInterstitialTorchOn.show();
//            Log.d("finish3", "onFinish: ");
//
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//
//    //..............Native......................//
//    public interface NativeAdListener {
//        void onNativeAdLoaded(FrameLayout frameLayout);
//
//        void onNativeAdError(FrameLayout frameLayout);
//    }
//
//    public interface NativeAdListenerHome {
//        void onNativeAdLoadedHome(View v, boolean isLoaded, int view);
//
//        void onNativeAdErrorHome(boolean isLoaded, int view);
//    }
//
//    public MoPubNative loadNativeAd(FrameLayout frameLayout, int nativeMopubStaticAdLayout, int nativeMopubVideoAdLayout, int nativeFBAdLayout, int nativeAdmobAdLayout, NativeAdsIdType nativeAdsIdType, NativeAdListener nativeAdListener) {
//
//        if (frameLayout == null || TinyDB.getInstance(mContext).getBoolean(Constants.IS_PREMIUM)) {
//            return null;
//        }
//
//        ViewBinder viewBinder = new ViewBinder.Builder(nativeMopubStaticAdLayout)
//                .mainImageId(R.id.native_main_image)
//                .iconImageId(R.id.ad_app_icon)
//                .titleId(R.id.ad_headline)
//                .textId(R.id.ad_body)
//                .callToActionId(R.id.ad_call_to_action)
//                .privacyInformationIconImageId(R.id.native_privacy_information_icon_image)
//                .build();
//
//        // Set up a renderer for AdMob ads.
//        final GooglePlayServicesAdRenderer googlePlayServicesAdRenderer = new GooglePlayServicesAdRenderer(
//                new MediaViewBinder.Builder(nativeAdmobAdLayout)
//                        .titleId(R.id.ad_headline)
//                        .textId(R.id.ad_body)
//                        .mediaLayoutId(R.id.native_main_image)
//                        .iconImageId(R.id.ad_app_icon)
//                        .callToActionId(R.id.ad_call_to_action)
//                        .privacyInformationIconImageId(R.id.native_privacy_information_icon_image)
//                        .build());
//
//        // Set up a renderer for Facebook video ads.
//        final FacebookAdRenderer facebookAdRenderer = new FacebookAdRenderer(
//                new FacebookAdRenderer.FacebookViewBinder.Builder(nativeFBAdLayout)
//                        .titleId(R.id.ad_headline)
//                        .textId(R.id.ad_body)
//                        .mediaViewId(R.id.native_main_image)
//                        .adIconViewId(R.id.ad_app_icon)
//                        .callToActionId(R.id.ad_call_to_action)
//                        .adChoicesRelativeLayoutId(R.id.native_privacy_information_icon_image)
//                        .build());
//
//        // Set up a renderer for a video native ad.
//        MoPubVideoNativeAdRenderer moPubVideoNativeAdRenderer = new MoPubVideoNativeAdRenderer(
//                new MediaViewBinder.Builder(nativeMopubVideoAdLayout)
//                        .titleId(R.id.ad_headline)
//                        .textId(R.id.ad_body)
//                        .mediaLayoutId(R.id.native_main_image)
//                        .iconImageId(R.id.ad_app_icon)
//                        .callToActionId(R.id.ad_call_to_action)
//                        .privacyInformationIconImageId(R.id.native_privacy_information_icon_image)
//                        .build());
//
//        MoPubNative.MoPubNativeNetworkListener moPubNativeNetworkListener = new MoPubNative.MoPubNativeNetworkListener() {
//
//            @Override
//            public void onNativeLoad(NativeAd nativeAd) {
//                AdapterHelper adapterHelper = new AdapterHelper(mContext, 0, 2);
//
//                View v = adapterHelper.getAdView(null, null, nativeAd, new ViewBinder.Builder(0).build());
//                //nativeAd.setMoPubNativeEventListener(moPubNativeEventListener);
//                // Add the ad view to our view hierarchy
//                frameLayout.addView(v);
//
//                nativeAdListener.onNativeAdLoaded(frameLayout);
//
//                isMoPubNativeLoaded = true;
//            }
//
//            @Override
//            public void onNativeFail(NativeErrorCode errorCode) {
//                nativeAdListener.onNativeAdError(frameLayout);
//                isMoPubNativeLoaded = false;
//            }
//        };
//
//        String nativeAdId = "";
//
//        if (nativeAdsIdType == NativeAdsIdType.SPLASH_NATIVE_MP) {
//            nativeAdId = mContext.getResources().getString(R.string.splash_native_mp);
//        } else if (nativeAdsIdType == NativeAdsIdType.MM_NATIVE_MP) {
//            nativeAdId = mContext.getResources().getString(R.string.mm_native_mp);
//        } else if (nativeAdsIdType == NativeAdsIdType.ADJUST_NATIVE_MP) {
//            nativeAdId = mContext.getResources().getString(R.string.adjust_native_mp);
//        } else if (nativeAdsIdType == NativeAdsIdType.EXIT_NATIVE_MP) {
//            nativeAdId = mContext.getResources().getString(R.string.exit_native_mp);
//        }
//
//        moPubNative = new MoPubNative(mContext, nativeAdId, moPubNativeNetworkListener);
//
//        MoPubStaticNativeAdRenderer moPubStaticNativeAdRenderer = new MoPubStaticNativeAdRenderer(viewBinder);
//        moPubNative.registerAdRenderer(moPubStaticNativeAdRenderer);
//        moPubNative.registerAdRenderer(googlePlayServicesAdRenderer);
//        moPubNative.registerAdRenderer(facebookAdRenderer);
//        moPubNative.registerAdRenderer(moPubVideoNativeAdRenderer);
//
//        final String keywords = "";
//        final String userDataKeywords = "";
//
//        final EnumSet<RequestParameters.NativeAdAsset> desiredAssets = EnumSet.of(
//                RequestParameters.NativeAdAsset.TITLE,
//                RequestParameters.NativeAdAsset.TEXT,
//                RequestParameters.NativeAdAsset.ICON_IMAGE,
//                RequestParameters.NativeAdAsset.MAIN_IMAGE,
//                RequestParameters.NativeAdAsset.CALL_TO_ACTION_TEXT);
//
//        RequestParameters mRequestParameters = new RequestParameters.Builder()
//                .keywords(keywords)
//                .userDataKeywords(userDataKeywords)
//                .desiredAssets(desiredAssets)
//                .build();
//
//        moPubNative.makeRequest(mRequestParameters);
//
//        return moPubNative;
//    }
//
//    public MoPubNative loadNativeAdForHome(int view, int nativeMopubStaticAdLayout, int nativeMopubVideoAdLayout, int nativeFBAdLayout, int nativeAdmobAdLayout, NativeAdsIdType nativeAdsIdType, NativeAdListenerHome nativeAdListenerHome) {
//
//        if (TinyDB.getInstance(mContext).getBoolean(Constants.IS_PREMIUM)) {
//            return null;
//        }
//
//        ViewBinder viewBinder = new ViewBinder.Builder(nativeMopubStaticAdLayout)
//                .mainImageId(R.id.native_main_image)
//                .iconImageId(R.id.ad_app_icon)
//                .titleId(R.id.ad_headline)
//                .textId(R.id.ad_body)
//                .callToActionId(R.id.ad_call_to_action)
//                .build();
//
//        // Set up a renderer for AdMob ads.
//        final GooglePlayServicesAdRenderer googlePlayServicesAdRenderer = new GooglePlayServicesAdRenderer(
//                new MediaViewBinder.Builder(nativeAdmobAdLayout)
//                        .titleId(R.id.ad_headline)
//                        .textId(R.id.ad_body)
//                        .mediaLayoutId(R.id.native_main_image)
//                        .iconImageId(R.id.ad_app_icon)
//                        .callToActionId(R.id.ad_call_to_action)
//                        .privacyInformationIconImageId(R.id.native_privacy_information_icon_image)
//                        .build());
//
//        // Set up a renderer for Facebook video ads.
//        final FacebookAdRenderer facebookAdRenderer = new FacebookAdRenderer(
//                new FacebookAdRenderer.FacebookViewBinder.Builder(nativeFBAdLayout)
//                        .titleId(R.id.ad_headline)
//                        .textId(R.id.ad_body)
//                        .mediaViewId(R.id.native_main_image)
//                        .adIconViewId(R.id.ad_app_icon)
//                        .callToActionId(R.id.ad_call_to_action)
//                        .adChoicesRelativeLayoutId(R.id.native_privacy_information_icon_image)
//                        .build());
//
//        // Set up a renderer for a video native ad.
//        MoPubVideoNativeAdRenderer moPubVideoNativeAdRenderer = new MoPubVideoNativeAdRenderer(
//                new MediaViewBinder.Builder(nativeMopubVideoAdLayout)
//                        .titleId(R.id.ad_headline)
//                        .textId(R.id.ad_body)
//                        .mediaLayoutId(R.id.native_main_image)
//                        .iconImageId(R.id.ad_app_icon)
//                        .callToActionId(R.id.ad_call_to_action)
//                        .build());
//
//        MoPubNative.MoPubNativeNetworkListener moPubNativeNetworkListener = new MoPubNative.MoPubNativeNetworkListener() {
//
//            @Override
//            public void onNativeLoad(NativeAd nativeAd) {
//                AdapterHelper adapterHelper = new AdapterHelper(mContext, 0, 2);
//
//                View v = adapterHelper.getAdView(null, null, nativeAd, new ViewBinder.Builder(0).build());
//                //nativeAd.setMoPubNativeEventListener(moPubNativeEventListener);
//                // Add the ad view to our view hierarchy
//
//                nativeAdListenerHome.onNativeAdLoadedHome(v, true, view);
//
//                isMoPubNativeLoaded = true;
//            }
//
//            @Override
//            public void onNativeFail(NativeErrorCode errorCode) {
//                nativeAdListenerHome.onNativeAdErrorHome(false, view);
//                isMoPubNativeLoaded = false;
//            }
//        };
//
//        String nativeAdId = "";
//
//        if (nativeAdsIdType == NativeAdsIdType.SPLASH_NATIVE_MP) {
//            nativeAdId = mContext.getResources().getString(R.string.splash_native_mp);
//        } else if (nativeAdsIdType == NativeAdsIdType.MM_NATIVE_MP) {
//            nativeAdId = mContext.getResources().getString(R.string.mm_native_mp);
//        } else if (nativeAdsIdType == NativeAdsIdType.ADJUST_NATIVE_MP) {
//            nativeAdId = mContext.getResources().getString(R.string.adjust_native_mp);
//        } else if (nativeAdsIdType == NativeAdsIdType.EXIT_NATIVE_MP) {
//            nativeAdId = mContext.getResources().getString(R.string.exit_native_mp);
//        }
//
//        moPubNative = new MoPubNative(mContext, nativeAdId, moPubNativeNetworkListener);
//
//        MoPubStaticNativeAdRenderer moPubStaticNativeAdRenderer = new MoPubStaticNativeAdRenderer(viewBinder);
//        moPubNative.registerAdRenderer(moPubStaticNativeAdRenderer);
//        moPubNative.registerAdRenderer(googlePlayServicesAdRenderer);
//        moPubNative.registerAdRenderer(facebookAdRenderer);
//        moPubNative.registerAdRenderer(moPubVideoNativeAdRenderer);
//
//        final String keywords = "";
//        final String userDataKeywords = "";
//
//        final EnumSet<RequestParameters.NativeAdAsset> desiredAssets = EnumSet.of(
//                RequestParameters.NativeAdAsset.TITLE,
//                RequestParameters.NativeAdAsset.TEXT,
//                RequestParameters.NativeAdAsset.ICON_IMAGE,
//                RequestParameters.NativeAdAsset.MAIN_IMAGE,
//                RequestParameters.NativeAdAsset.CALL_TO_ACTION_TEXT);
//
//        RequestParameters mRequestParameters = new RequestParameters.Builder()
//                .keywords(keywords)
//                .userDataKeywords(userDataKeywords)
//                .desiredAssets(desiredAssets)
//                .build();
//
//        moPubNative.makeRequest(mRequestParameters);
//
//        return moPubNative;
//    }
//
//    public MoPubNative getNativeAd() {
//        return moPubNative;
//    }
//
//    public boolean getNativeAdLoadedStatus() {
//        return isMoPubNativeLoaded;
//    }
//
//    public void setNativeAdLoadedStatus(boolean b) {
//        isMoPubNativeLoaded = b;
//    }
//
//    public MoPubRecyclerAdapter getMoPubRecyclerAdapter() {
//        return myMoPubAdapter;
//    }
//
//    //..............Banner......................//
//    public void loadBannerAd(final MoPubView moPubView) {
//        if (!TinyDB.getInstance(mContext).getBoolean(Constants.IS_PREMIUM)) {
//
//            moPubView.setAdUnitId(mContext.getResources().getString(R.string.banner_mp));
//
//            moPubView.setBannerAdListener(new MoPubView.BannerAdListener() {
//
//                @Override
//                public void onBannerLoaded(MoPubView banner) {
//                    if (moPubView.getVisibility() == View.GONE) {
//                        moPubView.setVisibility(View.VISIBLE);
//                    }
//                }
//
//                @Override
//                public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
//                    if (moPubView.getVisibility() == View.VISIBLE) {
//                        moPubView.setVisibility(View.GONE);
//                    }
//                }
//
//                @Override
//                public void onBannerClicked(MoPubView banner) {
//
//                }
//
//                @Override
//                public void onBannerExpanded(MoPubView banner) {
//
//                }
//
//                @Override
//                public void onBannerCollapsed(MoPubView banner) {
//
//                }
//            });
//
//            moPubView.loadAd();
//        } else {
//            if (moPubView.getVisibility() == View.VISIBLE) {
//                moPubView.setVisibility(View.GONE);
//            }
//        }
//    }
//
//    //...................MoPubRecyclerAdapter Native..................//
//    public void loadNativeRecyclerViewAd(RecyclerView rv, RecyclerView.Adapter adapter, int repeatingPosition, int nativeMopubStaticAdLayout, int nativeMopubVideoAdLayout, int nativeFBAdLayout, int nativeAdmobAdLayout, NativeAdsIdType nativeAdsIdType, NativeAdListener nativeAdListener) {
//        MoPubNativeAdPositioning.MoPubClientPositioning adPositioning = MoPubNativeAdPositioning.clientPositioning();
//        adPositioning.addFixedPosition(1);
//        adPositioning.enableRepeatingPositions(repeatingPosition);
//        myMoPubAdapter = new MoPubRecyclerAdapter((Activity) mContext, adapter, adPositioning);
//
//        ViewBinder myViewBinder = new ViewBinder.Builder(nativeMopubStaticAdLayout)
//                .mainImageId(R.id.native_main_image)
//                .titleId(R.id.ad_headline)
//                .textId(R.id.ad_body)
//                .callToActionId(R.id.ad_call_to_action)
//                .build();
//
//        // Set up a renderer for AdMob ads.
//        final GooglePlayServicesAdRenderer googlePlayServicesAdRenderer = new GooglePlayServicesAdRenderer(
//                new MediaViewBinder.Builder(nativeAdmobAdLayout)
//                        .titleId(R.id.ad_headline)
//                        .textId(R.id.ad_body)
//                        .mediaLayoutId(R.id.native_main_image)
//                        .iconImageId(R.id.ad_app_icon)
//                        .callToActionId(R.id.ad_call_to_action)
//                        .privacyInformationIconImageId(R.id.native_privacy_information_icon_image)
//                        .build());
//
//        // Set up a renderer for Facebook video ads.
//        final FacebookAdRenderer facebookAdRenderer = new FacebookAdRenderer(
//                new FacebookAdRenderer.FacebookViewBinder.Builder(nativeFBAdLayout)
//                        .titleId(R.id.ad_headline)
//                        .textId(R.id.ad_body)
//                        .mediaViewId(R.id.native_main_image)
//                        .adIconViewId(R.id.ad_app_icon)
//                        .callToActionId(R.id.ad_call_to_action)
//                        .adChoicesRelativeLayoutId(R.id.native_privacy_information_icon_image)
//                        .build());
//
//        // Set up a renderer for a video native ad.
//        MoPubVideoNativeAdRenderer moPubVideoNativeAdRenderer = new MoPubVideoNativeAdRenderer(
//                new MediaViewBinder.Builder(nativeMopubVideoAdLayout)
//                        .titleId(R.id.ad_headline)
//                        .textId(R.id.ad_body)
//                        .mediaLayoutId(R.id.native_main_image)
//                        .iconImageId(R.id.ad_app_icon)
//                        .callToActionId(R.id.ad_call_to_action)
//                        .build());
//
//        MoPubStaticNativeAdRenderer myRenderer = new MoPubStaticNativeAdRenderer(myViewBinder);
//
//        myMoPubAdapter.registerAdRenderer(myRenderer);
//        myMoPubAdapter.registerAdRenderer(googlePlayServicesAdRenderer);
//        myMoPubAdapter.registerAdRenderer(facebookAdRenderer);
//        myMoPubAdapter.registerAdRenderer(moPubVideoNativeAdRenderer);
//
//        rv.setAdapter(myMoPubAdapter);
//        myMoPubAdapter.setAdLoadedListener(new MoPubNativeAdLoadedListener() {
//            @Override
//            public void onAdLoaded(int position) {
//                nativeAdListener.onNativeAdLoaded(null);
//                isMoPubNativeLoaded = true;
//            }
//
//            @Override
//            public void onAdRemoved(int position) {
//                nativeAdListener.onNativeAdError(null);
//                isMoPubNativeLoaded = false;
//            }
//        });
//
//        myMoPubAdapter.loadAds(mContext.getResources().getString(R.string.lighter_list_native_mp));
//    }
//
//    public void destroyNative() {
//        if (moPubNative != null) {
//            moPubNative.destroy();
//        }
//    }
//
//
//}
