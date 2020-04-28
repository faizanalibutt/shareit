package com.code4rox.adsmanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.Objects;

public class AdmobUtils {

    NativeAdListener nativeAdListener;
    private Context context;
    private InterstitialAd mInterstitialAd;
    private AdmobInterstitialListener admobListener;
    private boolean bannerSize = false;
    private boolean check = false;
    private UnifiedNativeAd nativeAd;
    private FrameLayout frameLayout;
    private int nativeAdLayout;
    private InterAdsIdType interAdsIdType;
    private NativeAdsIdType nativeAdsIdType;
    private AdmobBannerListener admobBannerListener;


    public AdmobUtils(Context context, AdmobInterstitialListener admobListener, InterAdsIdType interAdsIdType) {
        this.context = context;
        this.interAdsIdType = interAdsIdType;
        this.admobListener = admobListener;
        mInterstitialAd = newInterstitialAd();

    }

    public AdmobUtils(Context context, AdmobInterstitialListener admobListener, int i) {
        this.context = context;
        this.admobListener = admobListener;
        mInterstitialAd = newMainInterstitialAd();
    }

    // Just
    public AdmobUtils(Context context) {
        this.context = context;
    }

    public void loadInterstitial(AdmobInterstitialListener admobListener , InterAdsIdType interAdsIdType) {
        this.interAdsIdType = interAdsIdType;
        this.admobListener = admobListener;
        mInterstitialAd = newInterstitialAd();
    }

    public void loadNativeAd(FrameLayout frameLayout, int nativeAdLayout, NativeAdsIdType nativeAdsIdType) {
        this.nativeAdLayout = nativeAdLayout;
        this.frameLayout = frameLayout;
        this.nativeAdsIdType = nativeAdsIdType;
        this.nativeAd = loadNativeAd();
    }

    public void loadNativeAd(FrameLayout frameLayout, int nativeAdLayout, NativeAdsIdType nativeAdsIdType, int actionBtn) {
        this.nativeAdLayout = nativeAdLayout;
        this.frameLayout = frameLayout;
        this.nativeAdsIdType = nativeAdsIdType;
        this.nativeAd = loadNativeAd(actionBtn);
    }

    public void setNativeAdListener(NativeAdListener nativeAdListener) {
        this.nativeAdListener = nativeAdListener;
    }

    public void loadBannerAd(final AdView adView) {
        if (!TinyDB.getInstance(context).getBoolean(Constants.IS_PREMIUM)) {
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            //Set the Banner Id
//           adView.setAdSize(AdSize.LARGE_BANNER);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    if (adView.getVisibility() == View.GONE) {
                        adView.setVisibility(View.VISIBLE);
                    }
                    if(admobBannerListener != null){
                        admobBannerListener.onBannerAdLoaded();
                    }
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    if(admobBannerListener != null){
                        admobBannerListener.onBannerAdFailedLoaded();
                    }
                }
            });
            adView.loadAd(adRequest);
        }
    }

    public void setAdmobBannerListener(AdmobBannerListener admobBannerListener){
        this.admobBannerListener = admobBannerListener;
    }

    public void loadRectBannerAd(final View adView) {
        if (!TinyDB.getInstance(context).getBoolean(Constants.IS_PREMIUM) &&
                FirebaseRemoteConfig.getInstance().getBoolean(Constants.FIRE_IS_RECT_BANNER_SHOW)) {

            AdView mAdView = new AdView(context);
            if (!FirebaseRemoteConfig.getInstance().getBoolean(Constants.FIRE_IS_SMALL_RECT_BANNER)) {
                mAdView.setAdSize(AdSize.MEDIUM_RECTANGLE);
            } else {
                mAdView.setAdSize(AdSize.LARGE_BANNER);
            }
            mAdView.setAdUnitId(context.getResources().getString(R.string.adjust_native_fb));
            ((RelativeLayout) adView).addView(mAdView);
            AdRequest adRequest = new AdRequest.Builder().build();

            //Set the Banner Id
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    if (adView.getVisibility() == View.GONE) {
                        adView.setVisibility(View.VISIBLE);
                    }
                }
            });
            mAdView.loadAd(adRequest);

        }
    }

    public InterstitialAd getInterstitialAd() {
        return mInterstitialAd;
    }

    public boolean showInterstitialAd() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            Log.d("finish3", "onFinish: ");

            return true;
        } else {
            return false;
        }
    }

    private InterstitialAd newInterstitialAd() {
        if (!TinyDB.getInstance(context).getBoolean(Constants.IS_PREMIUM)) {

            InterstitialAd interstitialAd = new InterstitialAd(context);
            String adId = "";
            if (interAdsIdType == InterAdsIdType.SPLASH_INTER_AM) {
                adId = context.getString(R.string.splash_inter_am);
            } else if (interAdsIdType == InterAdsIdType.INTER_AM) {
                adId = context.getString(R.string.inter_am);
            }
            interstitialAd.setAdUnitId(adId);
            interstitialAd.loadAd(new AdRequest.Builder().build());
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    FirebaseAnalytics.getInstance(context).logEvent("am_click_inter", new Bundle());

                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    FirebaseAnalytics.getInstance(context).logEvent("am_show_inter", new Bundle());
                    Log.d("Interstitial", "onAdOpen: ");
                    TinyDB.getInstance(context).putBoolean(Constants.CHECK_INTER_AD_SHOW, true);

                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    if (admobListener != null) {
                        admobListener.onInterstitialAdLoaded();
                    }
                }

                @Override
                public void onAdClosed() {

                    mInterstitialAd = newInterstitialAd();
                    Log.d("Timer", "Admob +");

                    if (admobListener != null) {
                        admobListener.onInterstitialAdClose();
                    }
                    TinyDB.getInstance(context).putBoolean(Constants.CHECK_INTER_AD_SHOW, false);
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    if (admobListener != null) {
                        admobListener.onInterstitialAdFailed();
                    }
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Log.d("Interstitial", "onAdImpression: ");
                }
            });
            return interstitialAd;
        } else {
            return null;
        }
    }

    private InterstitialAd newMainInterstitialAd() {
        if (!TinyDB.getInstance(context).getBoolean(Constants.IS_PREMIUM)) {
            InterstitialAd interstitialAd = new InterstitialAd(context);
            interstitialAd.setAdUnitId(context.getString(R.string.inter_am));
            interstitialAd.loadAd(new AdRequest.Builder().build());
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    mInterstitialAd = newMainInterstitialAd();
                    admobListener.onInterstitialAdClose();
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Log.d("Interstitial", "onAdImpression: ");
                }
            });
            return interstitialAd;
        } else {
            return null;
        }
    }

    private UnifiedNativeAd loadNativeAd() {
        return loadNativeAd(View.VISIBLE);
    }

    private UnifiedNativeAd loadNativeAd(int actionBtn) {

        if ((nativeAdLayout == -1 && frameLayout == null) || TinyDB.getInstance(context).getBoolean(Constants.IS_PREMIUM)) {
            return null;
        }

        final UnifiedNativeAd[] nativeAd = {null};

        AdLoader.Builder builder;
        String nativeAdId = "";

        if (nativeAdsIdType == NativeAdsIdType.SPLASH_NATIVE_AM) {
            nativeAdId = context.getResources().getString(R.string.splash_native_am);
        } else if (nativeAdsIdType == NativeAdsIdType.MM_NATIVE_AM) {
            nativeAdId = context.getResources().getString(R.string.mm_native_am);
        } else if (nativeAdsIdType == NativeAdsIdType.ADJUST_NATIVE_AM) {
            nativeAdId = context.getResources().getString(R.string.adjust_native_am);
        } else if (nativeAdsIdType == NativeAdsIdType.EXIT_NATIVE_AM) {
            nativeAdId = context.getResources().getString(R.string.exit_native_am);
        }

        builder = new AdLoader.Builder(context, nativeAdId);

        // OnUnifiedNativeAdLoadedListener implementation.
        builder.forUnifiedNativeAd(unifiedNativeAd -> {
            // You must call destroy on old ads when you are done with them,
            // otherwise you will have a memory leak.
            if (nativeAd[0] != null) {
                nativeAd[0].destroy();
            }
            nativeAd[0] = unifiedNativeAd;
           /* FrameLayout frameLayout =
                    findViewById(R.id.fl_adplaceholder);*/
            UnifiedNativeAdView adView = (UnifiedNativeAdView) ((Activity) context).getLayoutInflater()
                    .inflate(nativeAdLayout, null);

            populateUnifiedNativeAdView(unifiedNativeAd, adView, actionBtn);
            frameLayout.removeAllViews();
            frameLayout.addView(adView);
            /*if (unifiedNativeAd.getIcon() == null && frameLayout.findViewById(R.id.thumbnail) != null) {
                frameLayout.findViewById(R.id.thumbnail).setVisibility(View.GONE);
            }*/

            if (nativeAdListener != null) {
                nativeAdListener.onNativeAdLoaded();
            }
        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (nativeAdListener != null) {
                    Log.i("Admob", "Admob Error is: " + errorCode);
                    nativeAdListener.onNativeAdError();
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                FirebaseAnalytics.getInstance(context).logEvent("am_show_native", new Bundle());
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                FirebaseAnalytics.getInstance(context).logEvent("am_click_native", new Bundle());
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());

//        videoStatus.setText("");

        return nativeAd[0];

    }

    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView, int actionBtn) {
        // Set the media view. Media content will be automatically populated in the media view once
        // adView.setNativeAd() is called.
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
//        adView.setPriceView(adView.findViewById(R.id.ad_price));
//        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
//        adView.setStoreView(adView.findViewById(R.id.ad_store));
//        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null || actionBtn == View.INVISIBLE) {
            adView.getCallToActionView().setVisibility(View.GONE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
                adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

       /* if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
*/
        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        VideoController vc = nativeAd.getVideoController();

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {
      /*      videoStatus.setText(String.format(Locale.getDefault(),
                    "Video status: Ad contains a %.2f:1 video asset.",
                    vc.getAspectRatio()));*/

            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
                   /* refresh.setEnabled(true);
                    videoStatus.setText("Video status: Video playback has ended.");*/
                    super.onVideoEnd();
                }
            });
        } else {
         /*   videoStatus.setText("Video status: Ad does not contain a video asset.");
            refresh.setEnabled(true);*/
        }


    }

    public Dialog initDialogNative(int nativeDialogLayout, int customStyle) {

        Dialog dialogNative;
        dialogNative = new Dialog(context, customStyle);
        dialogNative.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(dialogNative.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialogNative.setContentView(nativeDialogLayout);
        dialogNative.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialogNative.getWindow().setDimAmount(0.5f);
        dialogNative.setCancelable(false);
        return dialogNative;
    }


    public interface AdmobInterstitialListener {
        void onInterstitialAdClose();
        void onInterstitialAdLoaded();
        void onInterstitialAdFailed();
    }

    public interface NativeAdListener {
        void onNativeAdLoaded();
        void onNativeAdError();
    }

    public interface AdmobBannerListener {
        void onBannerAdLoaded();
        void onBannerAdFailedLoaded();
    }


    public void destroyNativeAd(){
        if(nativeAd != null){
            nativeAd.destroy();
        }
    }



   /*     <LinearLayout
    android:id="@+id/ad_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:visibility="visible"
    >

        <com.google.android.gms.ads.AdView xmlns:ads="http://schemas.android.com/apk/res-auto"
    android:id="@+id/banner_ad_view"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_gravity="center_horizontal"
    android:visibility="gone"
    ads:adSize="BANNER"
    ads:adUnitId="@string/banner_ad_unit_id" />



    </LinearLayout>*/


    //native ad xml

   /*  <FrameLayout
    android:id="@+id/fl_adplaceholder"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
  */
}

