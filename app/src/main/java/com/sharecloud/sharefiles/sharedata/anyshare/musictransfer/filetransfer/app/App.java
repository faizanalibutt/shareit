package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.dev.bytes.adsmanager.InterAdPair;
import com.dev.bytes.adsmanager.aoa.AppOpenManager;
import com.dev.bytes.adsmanager.aoa.delay.InitialDelay;
import com.genonbeta.android.framework.preference.DbSharablePreferences;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.BuildConfig;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity.MainActivity;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.BillingUtilsKt;

import timber.log.Timber;

import static com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.AppUtils.getDefaultPreferences;

;

/**
 * created by: Veli
 * date: 25.02.2018 01:23
 */

public class App extends Application {
    public static final String TAG = App.class.getSimpleName();
    public static final String ACTION_REQUEST_PREFERENCES_SYNC = "com.genonbeta.intent.action.REQUEST_PREFERENCES_SYNC";

    public InterAdPair splashInterstitial = null;
    private MainCallback mMainCallback;

    public interface MainCallback {
        void onAdDismissed();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null)
                if (ACTION_REQUEST_PREFERENCES_SYNC.equals(intent.getAction())) {
                    SharedPreferences preferences = getDefaultPreferences(context).getWeakManager();

                    if (preferences instanceof DbSharablePreferences) {
                        try {
                            ((DbSharablePreferences) preferences).sync();
                        } catch (Exception exp) {
                            exp.getMessage();
                        }
                    }
                }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());

        getApplicationContext().registerReceiver(mReceiver, new IntentFilter(ACTION_REQUEST_PREFERENCES_SYNC));
        initBP();
        new AppOpenManager(this, InitialDelay.NONE, getString(R.string.aoa_am), new AdRequest.Builder().build(),
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, () -> {
            if (mMainCallback != null) mMainCallback.onAdDismissed();
            return null;
        });
    }

    public void setMainCallback(MainCallback mainCallback) {
        mMainCallback = mainCallback;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        getApplicationContext().unregisterReceiver(mReceiver);
    }

    public BillingProcessor bp;

    void initBP() {
        bp = BillingUtilsKt.initBilling(getApplicationContext(),
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAojYOQlv8IDWuyc5MLafXlrqAPywJtADlDlg3bK0vARt/yqQWE2+T0+6MgPNinmRvb14cdpKwUjL2r5SxE8/lEOsTMpW6kmToBV0aeXtQpGHDz+MzXOqEfoL/tCX4/so8X9DoqxWY0lxtjSCHIV+9xGpybe8bKnn7c+hQyZ3a7Cn5pkvVdMr0tJ/TD5lfG9sgTF6EOuGdNTOumx/XgpMKTd/r3BG8Ix44ipcZo2WP+rkAAr8qu5bJ7OMtybIMKi3B9e7/habDdftImVpb3BcR+Iofr7yKDXogQi8CQzknS51CmzUc4M9hGGDgKQVbCX6IEgYTe2eJDIbw0/dsccVB8QIDAQAB",
                () -> {
                    start(getApplicationContext());
                    return null;
                },
                () -> {
                    onPurchased();
                    return null;
                }
        );
    }

    void onPurchased() {
        boolean isPurchased = bp.isPurchased(BillingUtilsKt.getProductKey());
        if (isPurchased) {
            start(getApplicationContext());
        }
    }

    void start(Context context) {
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(mainIntent);
    }
}
