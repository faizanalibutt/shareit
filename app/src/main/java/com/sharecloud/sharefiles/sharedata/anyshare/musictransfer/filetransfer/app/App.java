package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.code4rox.adsmanager.AdmobUtils;
import com.code4rox.adsmanager.InterAdsIdType;
import com.genonbeta.android.framework.preference.DbSharablePreferences;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.BuildConfig;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity.MainActivity;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.BillingUtilsKt;

import timber.log.Timber;

import static com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.AppUtils.getDefaultPreferences;

/**
 * created by: Veli
 * date: 25.02.2018 01:23
 */

public class App extends Application {
    public static final String TAG = App.class.getSimpleName();
    public static final String ACTION_REQUEST_PREFERENCES_SYNC = "com.genonbeta.intent.action.REQUEST_PREFERENCES_SYNC";

    public AdmobUtils getMainAdmobUtils() {
        return mainAdmobUtils;
    }

    private AdmobUtils mainAdmobUtils;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null)
                if (ACTION_REQUEST_PREFERENCES_SYNC.equals(intent.getAction())) {
                    SharedPreferences preferences = getDefaultPreferences(context).getWeakManager();

                    if (preferences instanceof DbSharablePreferences) {
                        try {
                            ((DbSharablePreferences) preferences).sync();
                        } catch (Exception exp) {exp.getMessage();}
                    }
                }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());

        mainAdmobUtils = new AdmobUtils(this);
        mainAdmobUtils.loadInterstitial(null, InterAdsIdType.SPLASH_INTER_AM);
        //initializeSettings();
        getApplicationContext().registerReceiver(mReceiver, new IntentFilter(ACTION_REQUEST_PREFERENCES_SYNC));

        initBP();

        /*if (!Keyword.Flavor.googlePlay.equals(AppUtils.getBuildFlavor())
                && !UpdateUtils.hasNewVersion(getApplicationContext())
                && (System.currentTimeMillis() - UpdateUtils.getLastTimeCheckedForUpdates(getApplicationContext())) >= AppConfig.DELAY_CHECK_FOR_UPDATES) {
            GitHubUpdater updater = UpdateUtils.getDefaultUpdater(getApplicationContext());
            UpdateUtils.checkForUpdates(getApplicationContext(), updater, false, null);
        }*/
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        getApplicationContext().unregisterReceiver(mReceiver);
    }

    /*private void initializeSettings() {
        SharedPreferences defaultPreferences = AppUtils.getDefaultLocalPreferences(this);
        NetworkDevice localDevice = AppUtils.getLocalDevice(getApplicationContext());
        boolean nsdDefined = defaultPreferences.contains("nsd_enabled");
        boolean refVersion = defaultPreferences.contains("referral_version");

        PreferenceManager.setDefaultValues(this, R.xml.preferences_defaults_main, false);

        if (!refVersion)
            defaultPreferences.edit()
                    .putInt("referral_version", localDevice.versionNumber)
                    .apply();

        // Some pre-kitkat devices were soft rebooting when this feature was turned on.
        // So we will disable it for them and they will still be able to enable it.
        if (!nsdDefined)
            defaultPreferences.edit()
                    .putBoolean("nsd_enabled", Build.VERSION.SDK_INT >= 19)
                    .apply();

        PreferenceUtils.syncDefaults(getApplicationContext());

        if (defaultPreferences.contains("migrated_version")) {
            int migratedVersion = defaultPreferences.getInt("migrated_version", localDevice.versionNumber);

            if (migratedVersion < localDevice.versionNumber) {
                // migrating to a new version

                if (migratedVersion <= 67)
                    AppUtils.getViewingPreferences(getApplicationContext()).edit()
                            .clear()
                            .apply();

                defaultPreferences.edit()
                        .putInt("migrated_version", localDevice.versionNumber)
                        .putInt("previously_migrated_version", migratedVersion)
                        .apply();
            }
        } else
            defaultPreferences.edit()
                    .putInt("migrated_version", localDevice.versionNumber)
                    .apply();
    }*/

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
