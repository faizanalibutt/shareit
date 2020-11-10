package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaDrm;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.genonbeta.android.framework.io.DocumentFile;
import com.genonbeta.android.framework.preference.DbSharablePreferences;
import com.genonbeta.android.framework.preference.SuperPreferences;
import com.genonbeta.android.framework.util.PreferenceUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.GlideApp;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.App;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.config.AppConfig;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.config.Keyword;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.database.AccessDatabase;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.dialog.RationalePermissionRequest;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.graphics.drawable.TextDrawable;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.UIConnectionUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AppUtils {
    public static final String TAG = AppUtils.class.getSimpleName();
    private static final String SETTINGS_PACKAGE = "com.android.settings";
    private static final String HOTSPOT_SETTINGS_CLASS = "com.android.settings.TetherSettings"/*"com.android.settings.Settings$TetherWifiSettingsActivity"*/;
    private static int mUniqueNumber = 0;
    private static AccessDatabase mDatabase;
    private static SuperPreferences mDefaultPreferences;
    private static SuperPreferences mDefaultLocalPreferences;
    private static SuperPreferences mViewingPreferences;

    public static void applyAdapterName(NetworkDevice.Connection connection) {
        if (connection.ipAddress == null) {
            Log.e(AppUtils.class.getSimpleName(), "Connection should be provided with IP address");
            return;
        }

        List<AddressedInterface> interfaceList = NetworkUtils.getInterfaces(true, AppConfig.DEFAULT_DISABLED_INTERFACES);

        for (AddressedInterface addressedInterface : interfaceList) {
            if (NetworkUtils.getAddressPrefix(addressedInterface.getAssociatedAddress())
                    .equals(NetworkUtils.getAddressPrefix(connection.ipAddress))) {
                connection.adapterName = addressedInterface.getNetworkInterface().getDisplayName();
                return;
            }
        }

        connection.adapterName = Keyword.Local.NETWORK_INTERFACE_UNKNOWN;
    }

    // #SERVER
    public static void applyDeviceToJSON(Context context, JSONObject object) throws JSONException {
        NetworkDevice device = getLocalDevice(context);
        JSONObject deviceInformation = new JSONObject();
        JSONObject appInfo = new JSONObject();

        deviceInformation.put(Keyword.DEVICE_INFO_SERIAL, device.deviceId);
        deviceInformation.put(Keyword.DEVICE_INFO_BRAND, device.brand);
        deviceInformation.put(Keyword.DEVICE_INFO_MODEL, device.model);
        deviceInformation.put(Keyword.DEVICE_INFO_USER, device.nickname);

        try {
            ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(context.openFileInput("profilePicture"));

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageBytes);
            deviceInformation.put(Keyword.DEVICE_INFO_PICTURE, Base64.encodeToString(imageBytes.toByteArray(), 0));
        } catch (Exception e) {
            // do nothing
        }

        appInfo.put(Keyword.APP_INFO_VERSION_CODE, device.versionNumber);
        appInfo.put(Keyword.APP_INFO_VERSION_NAME, device.versionName);

        object.put(Keyword.APP_INFO, appInfo);
        object.put(Keyword.DEVICE_INFO, deviceInformation);
    }

    public static void createFeedbackIntent(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_EMAIL, new String[]{AppConfig.EMAIL_DEVELOPER})
                .putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.text_appName));

        DocumentFile logFile = AppUtils.createLog(activity);

        if (logFile != null) {
            try {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .putExtra(Intent.EXTRA_STREAM, (FileUtils.getSecureUri(activity, logFile)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.butn_feedbackContact)));
    }

    public static boolean checkRunningConditions(Context context) {
        for (RationalePermissionRequest.PermissionRequest request : getRequiredPermissions(context))
            if (ActivityCompat.checkSelfPermission(context, request.permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    }

    public static void launchHotspotSettings(AppCompatActivity activity) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            ComponentName componentName = new ComponentName(SETTINGS_PACKAGE, HOTSPOT_SETTINGS_CLASS);
            intent.setComponent(componentName);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (ActivityNotFoundException ane) {ane.getMessage();}
    }

    public static DocumentFile createLog(Context context) {
        DocumentFile saveDirectory = FileUtils.getApplicationDirectory(context);
        String fileName = FileUtils.getUniqueFileName(saveDirectory, "trebleshot_log.txt", true);
        DocumentFile logFile = saveDirectory.createFile(null, fileName);
        ActivityManager activityManager = (ActivityManager) context.getSystemService(
                Service.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processList = activityManager
                .getRunningAppProcesses();

        try {
            String command = "logcat -d -v threadtime *:*";
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            OutputStream outputStream = context.getContentResolver()
                    .openOutputStream(logFile.getUri(), "w");

            if (outputStream == null)
                throw new IOException(String.format("Could not open %s", fileName));

            String readLine;

            while ((readLine = reader.readLine()) != null)
                for (ActivityManager.RunningAppProcessInfo processInfo : processList)
                    if (readLine.contains(String.valueOf(processInfo.pid))) {
                        outputStream.write((readLine + "\n").getBytes());
                        outputStream.flush();

                        break;
                    }

            outputStream.close();
            reader.close();

            return logFile;
        } catch (IOException e) {
            // do nothing
        }

        return null;
    }

    public static TextDrawable.IShapeBuilder getDefaultIconBuilder(Context context) {
        TextDrawable.IShapeBuilder builder = TextDrawable.builder();

        builder.beginConfig()
                .firstLettersOnly(true)
                .textMaxLength(1)
                .textColor(ContextCompat.getColor(context, R.color.white))
                .shapeColor(ContextCompat.getColor(context, AppUtils.getReference(context, R.attr.colorPassive)));

        return builder;
    }

    public static AccessDatabase getDatabase(Context context) {
        if (mDatabase == null)
            mDatabase = new AccessDatabase(context);

        return mDatabase;
    }

    /*public static Keyword.Flavor getBuildFlavor() {
        try {
            return Keyword.Flavor.valueOf(BuildConfig.FLAVOR);
        } catch (Exception e) {
            Log.e(TAG, "Current build flavor " + BuildConfig.FLAVOR + " is not specified in " +
                    "the vocab. Is this a custom build?");
            return Keyword.Flavor.unknown;
        }
    }*/

    public static SuperPreferences getDefaultPreferences(final Context context) {
        if (mDefaultPreferences == null) {
            DbSharablePreferences databasePreferences = new DbSharablePreferences(context, "__default", true)
                    .setUpdateListener(new DbSharablePreferences.AsynchronousUpdateListener() {
                        @Override
                        public void onCommitComplete() {
                            context.sendBroadcast(new Intent(App.ACTION_REQUEST_PREFERENCES_SYNC));
                        }
                    });

            mDefaultPreferences = new SuperPreferences(databasePreferences);
            mDefaultPreferences.setOnPreferenceUpdateListener(new SuperPreferences.OnPreferenceUpdateListener() {
                @Override
                public void onPreferenceUpdate(SuperPreferences superPreferences, boolean commit) {
                    PreferenceUtils.syncPreferences(superPreferences, getDefaultLocalPreferences(context).getWeakManager());
                }
            });
        }

        return mDefaultPreferences;
    }

    public static SuperPreferences getDefaultLocalPreferences(final Context context) {
        if (mDefaultLocalPreferences == null) {
            mDefaultLocalPreferences = new SuperPreferences(PreferenceManager.getDefaultSharedPreferences(context));

            mDefaultLocalPreferences.setOnPreferenceUpdateListener(new SuperPreferences.OnPreferenceUpdateListener() {
                @Override
                public void onPreferenceUpdate(SuperPreferences superPreferences, boolean commit) {
                    PreferenceUtils.syncPreferences(superPreferences, getDefaultPreferences(context).getWeakManager());
                }
            });
        }

        return mDefaultLocalPreferences;
    }

    public static String getDeviceSerial(Context context) {
        if (Build.VERSION.SDK_INT < 26)
            return Build.SERIAL;
        else {
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT < 29)
                    return Build.SERIAL;
                else
                    return getUniqueID();
            } else
                return "what should i do here :(";
        }
    }

    @NotNull
    public static String getUniqueID() {
        UUID wideVineUuid = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
        MediaDrm wvDrm = null;
        try {
            wvDrm = new MediaDrm(wideVineUuid);
            byte[] wideVineId = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID);
            return Arrays.toString(wideVineId);
        } catch (Exception e) {
            // Inspect exception
            return "what should i do here :(";
        } finally {
            if (UIConnectionUtils.isOSAbove(29)) {
                if (wvDrm != null) {
                    wvDrm.close();
                }
            } else {
                if (wvDrm != null) {
                    wvDrm.release();
                }
            }
        }
        // Close resources with close() or release() depending on platform API
        // Use ARM on Android P platform or higher, where MediaDrm has the close() method
    }

    public static String getFriendlySSID(String ssid) {
        return ssid
                .replace("\"", "")
                .substring(AppConfig.PREFIX_ACCESS_POINT.length())
                .replace("_", " ");
    }

    @NonNull
    public static String getHotspotName(Context context) {
        return AppConfig.PREFIX_ACCESS_POINT + AppUtils.getLocalDeviceName(context)
                .replaceAll(" ", "_");
    }

    public static String getLocalDeviceName(Context context) {
        String deviceName = getDefaultPreferences(context)
                .getString("device_name", null);

        return deviceName == null || deviceName.length() == 0
                ? Build.MODEL.toUpperCase()
                : deviceName;
    }

    public static String getForceLocalDeviceName(Context context) {
        return Build.MODEL.toUpperCase();
    }

    public static NetworkDevice getLocalDevice(Context context) {
        NetworkDevice device = new NetworkDevice(getDeviceSerial(context));

        device.brand = Build.BRAND;
        device.model = Build.MODEL;
        device.nickname = AppUtils.getLocalDeviceName(context);
        device.isRestricted = false;
        device.isLocalAddress = true;

        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getApplicationInfo().packageName, 0);

            device.versionNumber = packageInfo.versionCode;
            device.versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return device;
    }

    @AnyRes
    public static int getReference(Context context, @AttrRes int refId) {
        TypedValue typedValue = new TypedValue();

        if (!context.getTheme().resolveAttribute(refId, typedValue, true)) {
            TypedArray values = context.getTheme().obtainStyledAttributes(context.getApplicationInfo().theme,
                    new int[]{refId});

            return values.length() > 0
                    ? values.getResourceId(0, 0)
                    : 0;
        }

        return typedValue.resourceId;
    }

    public static List<RationalePermissionRequest.PermissionRequest> getRequiredPermissions(Context context) {
        List<RationalePermissionRequest.PermissionRequest> permissionRequests = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 16) {
            permissionRequests.add(new RationalePermissionRequest.PermissionRequest(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    R.string.text_requestPermissionStorage,
                    R.string.text_requestPermissionStorageSummary));
        }

        if (Build.VERSION.SDK_INT >= 26) {
            permissionRequests.add(new RationalePermissionRequest.PermissionRequest(context,
                    Manifest.permission.READ_PHONE_STATE,
                    R.string.text_requestPermissionReadPhoneState,
                    R.string.text_requestPermissionReadPhoneStateSummary));
        }

        return permissionRequests;
    }

    public static int getUniqueNumber() {
        return (int) (System.currentTimeMillis() / 1000) + (++mUniqueNumber);
    }

    public static SuperPreferences getViewingPreferences(Context context) {
        if (mViewingPreferences == null)
            mViewingPreferences = new SuperPreferences(context.getSharedPreferences(Keyword.Local.SETTINGS_VIEWING, Context.MODE_PRIVATE));

        return mViewingPreferences;
    }

    public static boolean isLatestChangeLogSeen(Context context) {
        SharedPreferences preferences = getDefaultPreferences(context);
        NetworkDevice device = getLocalDevice(context);
        int lastSeenChangelog = preferences.getInt("changelog_seen_version", -1);
        boolean dialogAllowed = preferences.getBoolean("show_changelog_dialog", true);

        return !preferences.contains("previously_migrated_version")
                || device.versionNumber == lastSeenChangelog
                || !dialogAllowed;
    }

    public static void publishLatestChangelogSeen(Context context) {
        NetworkDevice device = getLocalDevice(context);

        getDefaultPreferences(context).edit()
                .putInt("changelog_seen_version", device.versionNumber)
                .apply();
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static <T> T quickAction(T clazz, QuickActions<T> quickActions) {
        quickActions.onQuickActions(clazz);
        return clazz;
    }

    /*public static boolean toggleDeviceScanning(Context context) {
        if (DeviceScannerService.getDeviceScanner().isScannerAvailable()) {
            context.startService(new Intent(context, DeviceScannerService.class)
                    .setAction(DeviceScannerService.ACTION_SCAN_DEVICES));

            return true;
        }

        DeviceScannerService.getDeviceScanner().interrupt();

        return false;
    }

    public static void startWebShareActivity(Context context, boolean startWebShare) {
        Intent startIntent = new Intent(context, WebShareActivity.class);

        if (startWebShare)
            startIntent.putExtra(WebShareActivity.EXTRA_WEBSERVER_START_REQUIRED, true);

        context.startActivity(startIntent);
    }*/

    public static void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    public static void loadProfilePictureInto(String deviceName, ImageView imageView, Context context) {
        try {
            FileInputStream inputStream = context.openFileInput("profilePicture");
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            GlideApp.with(context)
                    .load(bitmap)
                    .circleCrop()
                    .into(imageView);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            imageView.setImageDrawable(AppUtils.getDefaultIconBuilder(context).buildRound(deviceName));
        }
    }

    public static void startForegroundService(Context context, Intent intent) {
        /*if (Build.VERSION.SDK_INT >= 26)
            context.startForegroundService(intent);
        else*/
        try {
             context.startService(intent);
        } catch (Exception e) {
            Log.e(TAG, "start service exception", e);
        }
    }

    public interface QuickActions<T> {
        void onQuickActions(T clazz);
    }
}