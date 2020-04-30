package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.genonbeta.android.coolsocket.CoolSocket;
import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.google.android.material.snackbar.Snackbar;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback.Callback;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.config.Keyword;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.database.AccessDatabase;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.dialog.SenderWaitingDialog;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.service.CommunicationService;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.service.WorkerService;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.UITask;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity.PreparationsActivity;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.adapter.NetworkDeviceListAdapter;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.AppUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.CommunicationBridge;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.ConnectionUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.HotspotUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.LogUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.NetworkDeviceLoader;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * created by: veli
 * date: 15/04/18 18:44
 */
public class UIConnectionUtils {
    public static final String TAG = "UIConnectionUtils";

    private SnackbarSupport mSnackbarSupport;
    private boolean mWirelessEnableRequested = false;
    private ConnectionUtils mConnectionUtils;

    public UIConnectionUtils(ConnectionUtils connectionUtils, SnackbarSupport snackbarSupport) {
        mConnectionUtils = connectionUtils;
        mSnackbarSupport = snackbarSupport;
    }

    public static void showConnectionRejectionInformation(final Activity activity,
                                                          final NetworkDevice device,
                                                          final JSONObject clientResponse,
                                                          final DialogInterface.OnClickListener retryButtonListener) {
        try {
            if (clientResponse.has(Keyword.ERROR)) {
                if (clientResponse.getString(Keyword.ERROR).equals(Keyword.ERROR_NOT_ALLOWED))
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (!activity.isFinishing())
                                new AlertDialog.Builder(activity)
                                        .setTitle(R.string.mesg_notAllowed)
                                        .setMessage(activity.getString(R.string.text_notAllowedHelp, device.nickname, AppUtils.getLocalDeviceName(activity)))
                                        .setNegativeButton(R.string.butn_close, null)
                                        .setPositiveButton(R.string.butn_retry, retryButtonListener)
                                        .show();
                        }
                    });
            } else
                showUnknownError(activity, retryButtonListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void showUnknownError(final Activity activity, final DialogInterface.OnClickListener retryButtonListener) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (!activity.isFinishing())
                    new AlertDialog.Builder(activity)
                            .setMessage(R.string.mesg_somethingWentWrong)
                            .setNegativeButton(R.string.butn_close, null)
                            .setPositiveButton(R.string.butn_retry, retryButtonListener)
                            .show();
            }
        });
    }

    public ConnectionUtils getConnectionUtils() {
        return mConnectionUtils;
    }

    public SnackbarSupport getSnackbarSupport() {
        return mSnackbarSupport;
    }

    public void makeAcquaintance(final com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.Activity activity, final UITask task, final Object object, final int accessPin,
                                 final NetworkDeviceLoader.OnDeviceRegisteredListener registerListener) {
        WorkerService.RunningTask runningTask = new WorkerService.RunningTask() {
            private boolean mConnected = false;
            private String mRemoteAddress;

            @Override
            public void onRun() {
                final DialogInterface.OnClickListener retryButtonListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        makeAcquaintance(activity, task, object, accessPin, registerListener);
                    }
                };

                try {
                    if (object instanceof NetworkDeviceListAdapter.HotspotNetwork) {
                        mRemoteAddress = getConnectionUtils().establishHotspotConnection(getInterrupter(),
                                (NetworkDeviceListAdapter.HotspotNetwork) object,
                                new ConnectionUtils.ConnectionCallback() {
                                    @Override
                                    public boolean onTimePassed(int delimiter, long timePassed) {
                                        return timePassed >= 30000;
                                    }
                                });
                    } else if (object instanceof String)
                        mRemoteAddress = (String) object;

                    if (mRemoteAddress != null) {
                        mConnected = setupConnection(activity, mRemoteAddress, accessPin, new NetworkDeviceLoader.OnDeviceRegisteredListener() {
                            @Override
                            public void onDeviceRegistered(final AccessDatabase database, final NetworkDevice device, final NetworkDevice.Connection connection) {
                                // we may be working with direct IP scan
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (registerListener != null)
                                            registerListener.onDeviceRegistered(database, device, connection);
                                    }
                                });
                            }
                        }, retryButtonListener) != null;
                    }

                    if (!mConnected && !getInterrupter().interruptedByUser())
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (!activity.isFinishing()) {
                                    Callback.setDialogInfo("Connection Failed, Please try again");
                                    Callback.setDialogInfo(true);
                                    getSnackbarSupport().createSnackbar(R.string.mesg_connectionFailure)
                                            .setAction(R.string.butn_retry, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    makeAcquaintance(activity, task, object, accessPin, registerListener);
                                                    if(!activity.isFinishing()) {
                                                        SenderWaitingDialog senderWaitingDialog = new SenderWaitingDialog(activity, "");
                                                        if (!activity.isFinishing())
                                                            senderWaitingDialog.show();
                                                        Callback.setDialogInfo(activity.getString(R.string.mesg_waiting));
                                                    }
                                                }
                                            })
                                            .setDuration(Snackbar.LENGTH_INDEFINITE)
                                            .show();
                                }
                            }
                        });
                    else
                        Callback.setDialogInfo("Connected, Preparing to Send Files...");
                } catch (Exception e) {

                } finally {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (task != null && !activity.isFinishing())
                                task.updateTaskStopped();
                        }
                    });
                }
                // We can't add dialog outside of the else statement as it may close other dialogs as well
            }
        }.setTitle(activity.getString(R.string.mesg_completing))
                .setIconRes(R.drawable.ic_compare_arrows_white_24dp_static);

        runningTask.run(activity);

        if (task != null)
            task.updateTaskStarted(runningTask.getInterrupter());
    }

    public boolean notifyWirelessRequestHandled() {
        boolean returnedState = mWirelessEnableRequested;

        mWirelessEnableRequested = false;

        return returnedState;
    }

    @WorkerThread
    public NetworkDevice setupConnection(final Activity activity,
                                         final String ipAddress,
                                         final int accessPin,
                                         final NetworkDeviceLoader.OnDeviceRegisteredListener listener,
                                         final DialogInterface.OnClickListener retryButtonListener) {
        return CommunicationBridge.connect(AppUtils.getDatabase(activity), NetworkDevice.class, new CommunicationBridge.Client.ConnectionHandler() {
            @Override
            public void onConnect(CommunicationBridge.Client client) {
                try {

                    LogUtils.getLogInformation("Client",
                            String.format("setUpConnection(): CommunicationBridge.connect(): AccessPin is %s"
                                    , accessPin));
                    client.setSecureKey(accessPin);

                    CoolSocket.ActiveConnection activeConnection = client.connectWithHandshake(ipAddress, false);
                    LogUtils.getLogDebug("Client",
                            String.format("setUpConnection(): CommunicationBridge.connect(): activeConnection is %s", activeConnection));

                    NetworkDevice device = client.loadDevice(activeConnection);
                    LogUtils.getLogDebug("Client",
                            String.format("setUpConnection(): CommunicationBridge.connect(): NetworkDevice is %s", device));

                    activeConnection.reply(new JSONObject()
                            .put(Keyword.REQUEST, Keyword.REQUEST_ACQUAINTANCE)
                            .toString());
                    LogUtils.getLogInformation("Client",
                            String.format("setUpConnection(): CommunicationBridge.connect():" +
                                    " Requesting for Making connection to Server %s", Keyword.REQUEST_ACQUAINTANCE));

                    JSONObject receivedReply = new JSONObject(activeConnection.receive().response);

                    LogUtils.getLogInformation("Client",
                            String.format("setUpConnection(): CommunicationBridge.connect():" +
                                    " receivedReply is %s", receivedReply));

                    LogUtils.getLogInformation("Client",
                            String.format("setUpConnection(): CommunicationBridge.connect():" +
                                            " Before getting response from server NetworkDevice: isTrusted: %s isRestricted: %s tempSecureKey: %s",
                                    device.isTrusted,
                                    device.isRestricted,
                                    device.tmpSecureKey));

                    if (receivedReply.has(Keyword.RESULT)
                            && receivedReply.getBoolean(Keyword.RESULT)
                            && device.deviceId != null) {
                        final NetworkDevice.Connection connection = NetworkDeviceLoader.processConnection(AppUtils.getDatabase(activity), device, ipAddress);

                        device.lastUsageTime = System.currentTimeMillis();
                        device.tmpSecureKey = accessPin;
                        device.isRestricted = false;
                        device.isTrusted = true;

                        AppUtils.getDatabase(activity).publish(device);

                        LogUtils.getLogWarning("Client",
                                String.format("setUpConnection(): CommunicationBridge.connect():" +
                                                " After getting response from server NetworkDevice: isTrusted: %s isRestricted: %s tempSecureKey: %s",
                                        device.isTrusted,
                                        device.isRestricted,
                                        device.tmpSecureKey));

                        if (listener != null)
                            listener.onDeviceRegistered(AppUtils.getDatabase(activity), device, connection);
                    } else
                        showConnectionRejectionInformation(activity, device, receivedReply, retryButtonListener);

                    client.setReturn(device);
                } catch (Exception e) {
                    e.printStackTrace();
                    //Callback.setDialogInfo(e.getMessage());
                    LogUtils.getLogInformation("Client",
                            String.format("setUpConnection(): CommunicationBridge.connect():" +
                                    " while making connection to server error occurs:\n %s", e.getMessage()));
                }

            }
        });
    }

    public void showConnectionOptions(final Activity activity, final int locationPermRequestId, final RequestWatcher watcher) {
        if (!getConnectionUtils().getWifiManager().isWifiEnabled())
            getSnackbarSupport().createSnackbar(R.string.mesg_suggestSelfHotspot)
                    .setAction(R.string.butn_enable, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mWirelessEnableRequested = true;
                            turnOnWiFi(activity, locationPermRequestId, watcher);
                        }
                    })
                    .show();
        else if (validateLocationPermission(activity, locationPermRequestId, watcher)) {
            watcher.onResultReturned(true, false);

            getSnackbarSupport().createSnackbar(R.string.mesg_scanningSelfHotspot)
                    .setAction(R.string.butn_wifiSettings, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                    })
                    .show();
        }

        watcher.onResultReturned(true, false);
    }

    public boolean toggleHotspot(boolean conditional,
                                 final FragmentActivity activity,
                                 final int locationPermRequestId,
                                 final RequestWatcher watcher) {
        if (!HotspotUtils.isSupported())
            return false;

        DialogInterface.OnClickListener defaultNegativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                watcher.onResultReturned(false, false);
            }
        };

        if (conditional) {
            if (Build.VERSION.SDK_INT >= 26 && !validateLocationPermission(activity, locationPermRequestId, watcher))
                return false;

            if (Build.VERSION.SDK_INT >= 23
                    && !Settings.System.canWrite(getConnectionUtils().getContext())) {
                new AlertDialog.Builder(getConnectionUtils().getContext())
                        .setMessage(R.string.mesg_errorHotspotPermission)
                        .setNegativeButton(R.string.butn_cancel, defaultNegativeListener)
                        .setPositiveButton(R.string.butn_settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                        .setData(Uri.parse("package:" + getConnectionUtils().getContext().getPackageName()))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                                watcher.onResultReturned(false, true);
                            }
                        })
                        .show();

                return false;
            } else if (Build.VERSION.SDK_INT < 26
                    && !getConnectionUtils().getHotspotUtils().isEnabled()
                    && getConnectionUtils().isMobileDataActive()) {
                new AlertDialog.Builder(getConnectionUtils().getContext())
                        .setMessage(R.string.mesg_warningHotspotMobileActive)
                        .setNegativeButton(R.string.butn_cancel, defaultNegativeListener)
                        .setPositiveButton(R.string.butn_skip, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // no need to call watcher due to recycle
                                toggleHotspot(false, activity, locationPermRequestId, watcher);
                            }
                        })
                        .show();

                return false;
            }
        }

        WifiConfiguration wifiConfiguration = getConnectionUtils().getHotspotUtils().getConfiguration();

        if (!getConnectionUtils().getHotspotUtils().isEnabled()
                || (wifiConfiguration != null && AppUtils.getHotspotName(getConnectionUtils().getContext()).equals(wifiConfiguration.SSID))) {}
            /*getSnackbarSupport().createSnackbar(getConnectionUtils().getHotspotUtils().isEnabled()
                    ? R.string.mesg_stoppingSelfHotspot
                    : R.string.mesg_startingSelfHotspot)
                    .show();*/

        AppUtils.startForegroundService(getConnectionUtils().getContext(), new Intent(getConnectionUtils().getContext(), CommunicationService.class)
                .setAction(CommunicationService.ACTION_TOGGLE_HOTSPOT)); // scan devices or on hotspot

        watcher.onResultReturned(true, false);

        return true;
    }

    public boolean turnOnWiFi(final Activity activity, final int requestId, final RequestWatcher watcher) {
        if (getConnectionUtils().getWifiManager().setWifiEnabled(true)) {
            getSnackbarSupport().createSnackbar(R.string.mesg_turningWiFiOn).show();
            watcher.onResultReturned(true, false);
            return true;
        } else
            new AlertDialog.Builder(getConnectionUtils().getContext())
                    .setMessage(R.string.mesg_wifiEnableFailed)
                    .setNegativeButton(R.string.butn_close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            watcher.onResultReturned(false, false);
                        }
                    })
                    .setPositiveButton(R.string.butn_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            watcher.onResultReturned(false, true);
                        }
                    })
                    .show();

        return false;
    }

    public boolean validateLocationPermission(final Activity activity, final int requestId, final RequestWatcher watcher) {
        if (Build.VERSION.SDK_INT < 23)
            return true;

        final DialogInterface.OnClickListener defaultNegativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                watcher.onResultReturned(false, false);
            }
        };

        if (!getConnectionUtils().hasLocationPermission(getConnectionUtils().getContext())) {
            new AlertDialog.Builder(getConnectionUtils().getContext())
                    .setMessage(R.string.mesg_locationPermissionRequiredSelfHotspot)
                    .setNegativeButton(R.string.butn_cancel, defaultNegativeListener)
                    .setPositiveButton(R.string.butn_allow, new DialogInterface.OnClickListener() {
                        @SuppressLint("NewApi")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            watcher.onResultReturned(false, true);
                            // No, I am not going to add an if statement since when it is not needed
                            // the main method returns true.
                            activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION}, requestId);
                        }
                    })
                    .show();

            return false;
        }

        if (!getConnectionUtils().isLocationServiceEnabled()) {
            new AlertDialog.Builder(getConnectionUtils().getContext())
                    .setMessage(R.string.mesg_locationDisabledSelfHotspot)
                    .setNegativeButton(R.string.butn_cancel, defaultNegativeListener)
                    .setPositiveButton(R.string.butn_locationSettings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            watcher.onResultReturned(false, true);
                            activity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    /*.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)*/, PreparationsActivity.LOCATION_SERVICE_RESULT);
                        }
                    })
                    .show();

            return false;
        }

        watcher.onResultReturned(true, false);

        return true;
    }

    public interface RequestWatcher {
        void onResultReturned(boolean result, boolean shouldWait);
    }

    public static boolean isOreoAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean isOSAbove(int value) {
        return Build.VERSION.SDK_INT >= value;
    }

    public static boolean isOSBelow(int value) {
        return Build.VERSION.SDK_INT <= value;
    }
}
