package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.dev.bytes.adsmanager.BannerAdsManagerKt;
import com.dev.bytes.adsmanager.BannerPlacements;
import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.google.android.material.snackbar.Snackbar;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.Activity;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.App;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.config.Keyword;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.receiver.NetworkStatusReceiver;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.service.CommunicationService;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.service.WorkerService;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.task.OrganizeShareRunningTask;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.UIConnectionUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.AppUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.ConnectionUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.service.CommunicationService.ACTION_HOTSPOT_STATUS;
import static com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.service.CommunicationService.EXTRA_HOTSPOT_DISABLING;
import static com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.service.CommunicationService.EXTRA_HOTSPOT_ENABLED;
import static com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity.ShareActivity.ACTION_SEND;
import static com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity.ShareActivity.ACTION_SEND_MULTIPLE;
import static com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity.ShareActivity.EXTRA_FILENAME_LIST;

;

public class PreparationsActivity extends Activity
        implements SnackbarSupport, WorkerService.OnAttachListener {

    public static final String TASK_UPDATE = "taskINProgress";
    public static final int LOCATION_SERVICE_RESULT = 2;
    public static final String EXTRA_CLOSE_PERMISSION_SCREEN = "permissions";
    public static final String EXTRA_DEVICE_ID = "extraDeviceId";
    public static final String EXTRA_REQUEST_TYPE = "extraRequestType";
    public static final String EXTRA_ACTIVITY_SUBTITLE = "extraActivitySubtitle";
    public static final String EXTRA_CONNECTION_ADAPTER = "extraConnectionAdapter";
    private static final int LOCATION_PERMISSION_RESULT = 3;
    public boolean isAllEnabled;
    IntentFilter mIntentFilter = new IntentFilter();
    private ImageView bluetoothStatus, wifiStatus, gpsStatus, hotspotStatus;
    private AppCompatButton gpsButton, nextScreen, bluetoothButton, wifiButton, hotspotButton, button;
    private ProgressBar bluetoothPbr, wifiPbr, gpsPbr, hotspotPbr;
    private ProgressBar mProgressBar;
    private TextView mProgressTextLeft;
    private TextView mProgressTextRight;
    private LinearLayout showProgressDB;
    private Group groupBluetooth;
    private boolean isWifi, isBluetooth, isGps, isHotspot = false;
    private boolean isSender = false;
    private boolean isReceiver = false;
    private UIConnectionUtils mConnectionUtils;
    private boolean dbInsertion;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equalsIgnoreCase(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (state) {
                    /*case BluetoothAdapter.STATE_TURNING_ON:*/
                    case BluetoothAdapter.STATE_ON:
                        if ((isReceiver || isSender) && UIConnectionUtils.isOreoAbove())
                            enableBluetooth(bluetoothButton);
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        if (isReceiver && UIConnectionUtils.isOreoAbove())
                            disableBluetooth(bluetoothButton);
                        break;
                }
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equalsIgnoreCase(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                switch (state) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        /*case WifiManager.WIFI_STATE_ENABLING:*/
                        enableWifi(wifiButton);
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        disableWifi(wifiButton);
                        break;
                }
            } else if (NetworkStatusReceiver.WIFI_AP_STATE_CHANGED.equals(intent.getAction()))
                updateHotspotState();
            else if (ACTION_HOTSPOT_STATUS.equals(intent.getAction())) {
                if (intent.getBooleanExtra(EXTRA_HOTSPOT_ENABLED, false)) {
                    isHotspot = true;
                } else if (getConnectionUtils().getHotspotUtils().isEnabled()
                        && !intent.getBooleanExtra(EXTRA_HOTSPOT_DISABLING, false)) {
                    isHotspot = false;
                    disableHostspot(hotspotButton);
                }
            }
        }
    };
    private List<Uri> mFileUris;
    private List<CharSequence> mFileNames;

    public UIConnectionUtils getUIConnectionUtils() {
        if (mConnectionUtils == null)
            mConnectionUtils = new UIConnectionUtils(ConnectionUtils.getInstance(this), this);

        return mConnectionUtils;
    }

    private ConnectionUtils getConnectionUtils() {
        return getUIConnectionUtils().getConnectionUtils();
    }

    @Override
    public Snackbar createSnackbar(int resId, Object... objects) {
        return Snackbar.make(findViewById(R.id.container), getString(resId, objects), Snackbar.LENGTH_SHORT);
    }

    public static final int REQUEST_PERMISSION_CAMERA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbInsertion = false;

        mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ACTION_HOTSPOT_STATUS);
        mIntentFilter.addAction(NetworkStatusReceiver.WIFI_AP_STATE_CHANGED);

        setAction();
        init();

        // bluetooth check
        if (isReceiver && !UIConnectionUtils.isOreoAbove()) {
            isBluetooth = true;
            groupBluetooth.setVisibility(View.GONE);
            bluetoothButton.setVisibility(View.GONE);
            getConnectionUtils().getBluetoothAdapter().disable();
        } else if (getConnectionUtils().getBluetoothAdapter().enable()) {
            enableBluetooth(bluetoothButton);
        }
        // wifi check
        if (ConnectionUtils.getInstance(this).getWifiManager().isWifiEnabled()) {
            enableWifi(wifiButton);
        }
        // gps check
        if (ConnectionUtils.getInstance(this).isLocationServiceEnabled()
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            openGPS(gpsButton);
        }
        // hotspot check
        updateHotspotState();
    }

    private void setAction() {

        if (getIntent() != null) {
            isReceiver = getIntent().hasExtra(Keyword.EXTRA_RECEIVE)
                    && getIntent().getBooleanExtra(Keyword.EXTRA_RECEIVE, false);
            isSender = getIntent().hasExtra(Keyword.EXTRA_SEND)
                    && getIntent().getBooleanExtra(Keyword.EXTRA_SEND, false);

            if (isReceiver)
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.text_receivePreparations);
            if (isSender)
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.text_sendPreparations);

        }
        String action = getIntent() != null ? getIntent().getAction() : null;

        if (ACTION_SEND.equals(action)
                || ACTION_SEND_MULTIPLE.equals(action)
                || Intent.ACTION_SEND.equals(action)
                || Intent.ACTION_SEND_MULTIPLE.equals(action)) {

            ArrayList<Uri> fileUris = new ArrayList<>();
            ArrayList<CharSequence> fileNames = null;

            if (ACTION_SEND_MULTIPLE.equals(action)
                    || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                List<Uri> pendingFileUris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                fileNames = getIntent().hasExtra(EXTRA_FILENAME_LIST) ? getIntent().getCharSequenceArrayListExtra(EXTRA_FILENAME_LIST) : null;

                if (pendingFileUris != null) fileUris.addAll(pendingFileUris);
            } else {
                fileUris.add((Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM));

                if (getIntent().hasExtra(EXTRA_FILENAME_LIST)) {
                    fileNames = new ArrayList<>();
                    String fileName = getIntent().getStringExtra(EXTRA_FILENAME_LIST);

                    fileNames.add(fileName);
                }
            }

            if (fileUris.size() == 0) {
                Toast.makeText(this, R.string.text_listEmpty, Toast.LENGTH_SHORT).show();
                finish();
            } else {

                mFileUris = fileUris;
                mFileNames = fileNames;

                checkForTasks();
            }

        } else {
            if (isSender) {
                Toast.makeText(this, R.string.mesg_formatNotSupported, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void init() {
        setContentView(R.layout.activity_preparations);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        showProgressDB = findViewById(R.id.showDBProgress);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressTextLeft = findViewById(R.id.text1);
        mProgressTextRight = findViewById(R.id.text2);
        bluetoothStatus = findViewById(R.id.bluetoothStatus);
        bluetoothButton = findViewById(R.id.bluetoothClick);
        bluetoothPbr = findViewById(R.id.bluetoothPbr);
        wifiStatus = findViewById(R.id.wifiStatus);
        wifiButton = findViewById(R.id.wifiClick);
        wifiPbr = findViewById(R.id.wifiPbr);
        gpsStatus = findViewById(R.id.gpsStatus);
        gpsButton = findViewById(R.id.gpsClick);
        gpsPbr = findViewById(R.id.gpsPbr);
        hotspotStatus = findViewById(R.id.hotspotStatus);
        hotspotButton = findViewById(R.id.hotspotClick);
        hotspotPbr = findViewById(R.id.hotspotPbr);
        nextScreen = findViewById(R.id.button);
        groupBluetooth = findViewById(R.id.bluetoothGroup);
        nextScreen.setText(getString(R.string.next));
        nextScreen.setEnabled(false);
        enableButton();
        BannerAdsManagerKt.loadBannerAd(findViewById(R.id.ad_container_banner), BannerPlacements.BANNER_AD);
    }

    public void openBluetooth(View view) {
        ConnectionUtils.getInstance(this).openBluetooth();
        if (!ConnectionUtils.getInstance(this).getBluetoothAdapter().isEnabled()) {
            bluetoothStatus.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
            bluetoothPbr.setVisibility(View.VISIBLE);
            isBluetooth = false;
        } else {
            enableBluetooth(view);
        }
    }

    private void enableBluetooth(View view) {
        view.setVisibility(View.GONE);
        bluetoothStatus.setVisibility(View.VISIBLE);
        bluetoothPbr.setVisibility(View.GONE);
        isBluetooth = true;
        isAllEnabled = isWifi && isGps && isHotspot;
        enableButton();
    }

    private void disableBluetooth(View view) {
        view.setVisibility(View.VISIBLE);
        bluetoothStatus.setVisibility(View.GONE);
        bluetoothPbr.setVisibility(View.GONE);
        isBluetooth = false;
        isAllEnabled = false;
        enableButton();
    }

    public void openWifi(View view) {
        ConnectionUtils.getInstance(this).openWifi();
        if (!ConnectionUtils.getInstance(this).getWifiManager().isWifiEnabled()) {
            if (getConnectionUtils().getHotspotUtils().isEnabled()) {
                createSnackbar(R.string.text_hotspotStartedExternallyNotice).show();
            } else {
                view.setVisibility(View.GONE);
                wifiPbr.setVisibility(View.VISIBLE);
                wifiStatus.setVisibility(View.GONE);
                isWifi = false;
            }
        } else {
            enableWifi(view);
        }
    }

    private void enableWifi(View view) {
        isWifi = true;
        view.setVisibility(View.GONE);
        wifiPbr.setVisibility(View.GONE);
        wifiStatus.setVisibility(View.VISIBLE);
        isAllEnabled = isBluetooth && isGps && isHotspot;
        enableButton();
    }

    private void disableWifi(View view) {
        isWifi = false;
        view.setVisibility(View.VISIBLE);
        wifiPbr.setVisibility(View.GONE);
        wifiStatus.setVisibility(View.GONE);
        isAllEnabled = false;
        enableButton();
    }

    public void openGPS(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(PreparationsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (ConnectionUtils.getInstance(this).isLocationServiceEnabled()) {
                    enableGPS(view);
                    return;
                }
            }
            getUIConnectionUtils().validateLocationPermission(this, LOCATION_PERMISSION_RESULT, new UIConnectionUtils.RequestWatcher() {
                @Override
                public void onResultReturned(boolean result, boolean shouldWait) {
                    // to stay away from exceptions please let it be we will fix it by update.
                }
            });
        } else {
            // check for location service
            if (ConnectionUtils.getInstance(this).isLocationServiceEnabled()) {
                enableGPS(view);
            } else {
                view.setVisibility(View.GONE);
                gpsPbr.setVisibility(View.VISIBLE);
                gpsStatus.setVisibility(View.GONE);
                isGps = false;
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_SERVICE_RESULT);
            }
        }
    }

    private void enableGPS(View view) {
        view.setVisibility(View.GONE);
        gpsPbr.setVisibility(View.GONE);
        gpsStatus.setVisibility(View.VISIBLE);
        isGps = true;
        isAllEnabled = isBluetooth && isWifi && isHotspot;
        enableButton();
    }

    private void disableGPS(View view) {
        view.setVisibility(View.VISIBLE);
        gpsPbr.setVisibility(View.GONE);
        gpsStatus.setVisibility(View.GONE);
        isGps = false;
        isAllEnabled = false;
        enableButton();
    }

    public void openHotspot(View view) {
        AppUtils.launchHotspotSettings(this);
    }

    private void enableHostspot(View view) {
        view.setVisibility(View.GONE);
        hotspotPbr.setVisibility(View.GONE);
        hotspotStatus.setVisibility(View.VISIBLE);
        isHotspot = true;
        isAllEnabled = isBluetooth && isWifi && isGps;
        enableButton();
    }

    private void disableHostspot(View view) {
        view.setVisibility(View.VISIBLE);
        hotspotPbr.setVisibility(View.GONE);
        hotspotStatus.setVisibility(View.GONE);
        isHotspot = false;
        isAllEnabled = false;
        enableButton();
    }

    private boolean enableButton() {
        if (isAllEnabled) {
            nextScreen.setEnabled(true);
            nextScreen.setBackgroundResource(R.drawable.background_content_share_button_select);
            ViewCompat.setBackgroundTintList(
                    nextScreen,
                    ContextCompat.getColorStateList(
                            this,
                            R.color.text_button_text_color_selector_blue
                    )
            );
            nextScreen.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnOnClick(nextScreen);
            return isAllEnabled;
        } else {
            nextScreen.setEnabled(false);
            nextScreen.setBackgroundResource(R.drawable.background_content_share_button);
            ViewCompat.setBackgroundTintList(
                    nextScreen,
                    ContextCompat.getColorStateList(
                            this,
                            R.color.color_content_share_button
                    )
            );
            nextScreen.setTextColor(ContextCompat.getColor(this, R.color.black_transparent));
            return isAllEnabled;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (getApplication() != null && ((App) getApplication()).bp != null
                && !(((App) getApplication()).bp.handleActivityResult(requestCode, resultCode, data)))
            super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK || ConnectionUtils.getInstance(this).isLocationServiceEnabled()) {
            if (requestCode == LOCATION_SERVICE_RESULT) {
                if (ConnectionUtils.getInstance(this).isLocationServiceEnabled()) {
                    enableGPS(gpsButton);
                } else {
                    gpsButton.setVisibility(View.VISIBLE);
                    gpsStatus.setVisibility(View.GONE);
                    isGps = false;
                    /*"Please enable location service to proceed and select option # 1"*/
                    createSnackbar(R.string.mesg_locationDisabledSelfHotspot).show();
                }
            } /*else if (requestCode == REQUEST_CODE_CHOOSE_DEVICE
                    && data != null) {

                if (data.hasExtra(EXTRA_DEVICE_ID) && data.hasExtra(EXTRA_CONNECTION_ADAPTER)) {
                    String deviceId = data.getStringExtra(EXTRA_DEVICE_ID);
                    String connectionAdapter = data.getStringExtra(EXTRA_CONNECTION_ADAPTER);
                    setResult(RESULT_OK, new Intent()
                            .putExtra(EXTRA_DEVICE_ID, deviceId)
                            .putExtra(EXTRA_CONNECTION_ADAPTER, connectionAdapter)
                    );
                    finish();
                } else if (data.hasExtra(EXTRA_CLOSE_PERMISSION_SCREEN)) {
                    boolean isPermission = data.getBooleanExtra(EXTRA_CLOSE_PERMISSION_SCREEN, false);
                    if (isPermission) {
                        finish();
                    }
                }
            }*/
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_RESULT:
                if (!getConnectionUtils().hasLocationPermission(this)) {
                    // Permission is not granted
                    disableGPS(gpsButton);
                    createSnackbar(R.string.mesg_locationDisabledSelfHotspot).show();
                } else {
                    if (ConnectionUtils.getInstance(PreparationsActivity.this).isLocationServiceEnabled()) {
                        enableGPS(gpsButton);
                    } else {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_SERVICE_RESULT);
                    }
                }
                break;
//            case REQUEST_PERMISSION_CAMERA:
//                if (isAllEnabled)
//                    btnOnClick(nextScreen);
//                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);  // TODO: 7/27/20 here is ANR
    }

    private void updateHotspotState() {
        boolean isEnabled = getUIConnectionUtils().getConnectionUtils().getHotspotUtils().isEnabled();

        if (!isEnabled) {
            enableHostspot(hotspotButton);
        } else if (Build.VERSION.SDK_INT >= 26) {
            AppUtils.startForegroundService(this,
                    new Intent(this, CommunicationService.class)
                            .setAction(CommunicationService.ACTION_REQUEST_HOTSPOT_STATUS));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
            finish();
        else return super.onOptionsItemSelected(item);

        return true;
    }

    /* SHARE ACTIVITY CODE WILL BE ON THIS CLASS FOR SURE */

    public void btnOnClick(View view) {
        /*if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
            else
                Snackbar.make(view, R.string.text_cameraPermissionRequired, Snackbar.LENGTH_LONG).setAction(R.string.text_settings,v ->{}).show();
        } else {*/
        if (isReceiver) {
            startActivity(new Intent(PreparationsActivity.this, com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity.ReceiverActivity.class)
                    .putExtra(Keyword.EXTRA_RECEIVE, true)
                    .putExtra(EXTRA_ACTIVITY_SUBTITLE, getString(R.string.text_receive))
                    .putExtra(EXTRA_REQUEST_TYPE,
                            ReceiverActivity.RequestType.MAKE_ACQUAINTANCE.toString()));
            finish();
        } else if (isSender && getDefaultPreferences().getLong("add_devices_to_transfer", -1) != -1 && dbInsertion) {
            ViewTransferActivity.startInstance(PreparationsActivity.this, getDefaultPreferences().getLong("add_devices_to_transfer", -1));
            startActivity(new Intent(PreparationsActivity.this, com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity.SenderActivity.class)
                    .putExtra(Keyword.EXTRA_SEND, true)
                    .putExtra(com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity.SenderActivity.EXTRA_ACTIVITY_SUBTITLE, getString(R.string.text_receive))
                    .putExtra(com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity.SenderActivity.EXTRA_REQUEST_TYPE,
                            SenderActivity.RequestType.MAKE_ACQUAINTANCE.toString()));
            finish();
        } else {
            if (isAllEnabled)
                showProgressDB.setVisibility(View.VISIBLE);
        }
//        }
    }

    public void btnOnClick() {
        btnOnClick(nextScreen);
    }

    public void updateText(WorkerService.RunningTask runningTask, final String text) {
        if (isFinishing()) {
            LogUtils.getLogTask("Preparations", "updateText(): Activity about to close, DON'T SHOW NOTIFICATION");
            return;
        }

        LogUtils.getLogTask("Preparations", "updateText(): Activity about to close, BUT SHOW NOTIFICATION");
        runningTask.publishStatusText(text);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //createSnackbar(R.string.msg_merg_send, text).show();

            }
        });
    }

    @Override
    public void onAttachedToTask(WorkerService.RunningTask task) {

    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public void setDBProgress(boolean dbInsertion) {
        this.dbInsertion = dbInsertion;
    }

    public void updateProgress(final int total, final int current) {
        if (isFinishing())
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressTextLeft.setText(String.valueOf(current));
                mProgressTextRight.setText(String.valueOf(total));
            }
        });

        mProgressBar.setProgress(current);
        mProgressBar.setMax(total);
    }

    @Override
    protected void onPreviousRunningTask(@Nullable WorkerService.RunningTask task) {
        super.onPreviousRunningTask(task);

        OrganizeShareRunningTask mTask;
        if (task instanceof OrganizeShareRunningTask) {
            mTask = ((OrganizeShareRunningTask) task);
            mTask.setAnchorListener(this);
            LogUtils.getLogTask("Preparations", "onPreviousRunningTask(): Task is alreadycreated");
        } else {
            mTask = new OrganizeShareRunningTask(mFileUris, mFileNames);
            LogUtils.getLogTask("Preparations", "onPreviousRunningTask(): Task is created");
            mTask.setTitle(getString(R.string.mesg_organizingFiles))
                    .setAnchorListener(this)
                    .setContentIntent(this, getIntent())
                    .run(this);

            attachRunningTask(mTask);
        }
    }
}
