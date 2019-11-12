package com.hazelmobile.filetransfer.ui.activity;

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
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.google.android.material.snackbar.Snackbar;
import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.app.Activity;
import com.hazelmobile.filetransfer.pictures.AppUtils;
import com.hazelmobile.filetransfer.pictures.Keyword;
import com.hazelmobile.filetransfer.receiver.NetworkStatusReceiver;
import com.hazelmobile.filetransfer.service.CommunicationService;
import com.hazelmobile.filetransfer.service.WorkerService;
import com.hazelmobile.filetransfer.task.OrganizeShareRunningTask;
import com.hazelmobile.filetransfer.ui.UIConnectionUtils;
import com.hazelmobile.filetransfer.util.ConnectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.hazelmobile.filetransfer.service.CommunicationService.ACTION_HOTSPOT_STATUS;
import static com.hazelmobile.filetransfer.service.CommunicationService.EXTRA_HOTSPOT_DISABLING;
import static com.hazelmobile.filetransfer.service.CommunicationService.EXTRA_HOTSPOT_ENABLED;
import static com.hazelmobile.filetransfer.ui.activity.ShareActivity.ACTION_SEND;
import static com.hazelmobile.filetransfer.ui.activity.ShareActivity.ACTION_SEND_MULTIPLE;
import static com.hazelmobile.filetransfer.ui.activity.ShareActivity.EXTRA_FILENAME_LIST;

public class PreparationsActivity extends Activity
        implements SnackbarSupport, WorkerService.OnAttachListener {

    private static final int LOCATION_PERMISSION_RESULT = 0;
    public static final int LOCATION_SERVICE_RESULT = 1;
    public static final String EXTRA_CLOSE_PERMISSION_SCREEN = "permissions";
    public static final String EXTRA_DEVICE_ID = "extraDeviceId";
    public static final String EXTRA_REQUEST_TYPE = "extraRequestType";
    public static final String EXTRA_ACTIVITY_SUBTITLE = "extraActivitySubtitle";
    public static final String EXTRA_CONNECTION_ADAPTER = "extraConnectionAdapter";

    private ImageView bluetoothStatus, wifiStatus, gpsStatus, hotspotStatus;
    private AppCompatButton gpsButton, nextScreen, bluetoothButton, wifiButton, hotspotButton;
    private ProgressBar bluetoothPbr, wifiPbr, gpsPbr, hotspotPbr;
    private boolean isWifi, isBluetooth, isGps, isAllEnabled, isHotspot = false;
    private boolean isSender = false;
    private boolean isReceiver = false;

    IntentFilter mIntentFilter = new IntentFilter();

    private UIConnectionUtils mConnectionUtils;

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

    // TODO: 10/23/2019 remove this in future #33
    public static final int REQUEST_CODE_CHOOSE_DEVICE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ACTION_HOTSPOT_STATUS);
        mIntentFilter.addAction(NetworkStatusReceiver.WIFI_AP_STATE_CHANGED);

        init();
        setAction();

        // bluetooth check
        if (isReceiver && !UIConnectionUtils.isOreoAbove()) {
            isBluetooth = true;
            bluetoothButton.setEnabled(false);
            getConnectionUtils().getBluetoothAdapter().disable();
        } else
            if (getConnectionUtils().getBluetoothAdapter().enable())
                enableBluetooth(bluetoothButton);
        // wifi check
        if (ConnectionUtils.getInstance(this).getWifiManager().isWifiEnabled()) {
            enableWifi(wifiButton);
        }
        // gps check
        if (ConnectionUtils.getInstance(this).isLocationServiceEnabled()) {
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

                fileUris.addAll(pendingFileUris);
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

                /*mProgressBar = findViewById(R.id.progressBar);
                mProgressTextLeft = findViewById(R.id.text1);
                mProgressTextRight = findViewById(R.id.text2);
                mTextMain = findViewById(R.id.textMain);
                mCancelButton = findViewById(R.id.cancelButton);

                mCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mTask != null)
                            mTask.getInterrupter().interrupt(true);
                    }
                });*/


                mFileUris = fileUris;
                mFileNames = fileNames;

                checkForTasks();
            }

        } /*else {
            Toast.makeText(this, R.string.mesg_formatNotSupported, Toast.LENGTH_SHORT).show();
            finish();
        }*/
    }

    private void init() {
        setContentView(R.layout.activity_preparations);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

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
        nextScreen.setText(getString(R.string.next));
        nextScreen.setEnabled(false);
        enableButton();
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

    public void openHotspot(View view) {
        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
    }

    private void enableHostspot(View view) {
        view.setVisibility(View.GONE);
        hotspotPbr.setVisibility(View.GONE);
        hotspotStatus.setVisibility(View.VISIBLE);
        isHotspot = true;
        isAllEnabled = isBluetooth && isWifi && isGps;
        enableButton();
    }

    private void enableButton() {
        if (isAllEnabled) {
            nextScreen.setEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
        if (requestCode == LOCATION_PERMISSION_RESULT) {
            if (!getConnectionUtils().hasLocationPermission(this)) {
                // Permission is not granted
                gpsButton.setVisibility(View.VISIBLE);
                gpsStatus.setVisibility(View.GONE);
                gpsPbr.setVisibility(View.GONE);
                isGps = false;
                createSnackbar(R.string.mesg_locationDisabledSelfHotspot).show();
            } else {
                if (ConnectionUtils.getInstance(PreparationsActivity.this).isLocationServiceEnabled()) {
                    enableGPS(gpsButton);
                } else {
                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_SERVICE_RESULT);
                }
            }
        } else if (requestCode == LOCATION_SERVICE_RESULT) {
            if (ConnectionUtils.getInstance(PreparationsActivity.this).isLocationServiceEnabled()) {
                enableGPS(gpsButton);
            } else {
                createSnackbar(R.string.mesg_locationDisabledSelfHotspot).show();
            }
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
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equalsIgnoreCase(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        /*case BluetoothAdapter.STATE_TURNING_ON:*/
                        enableBluetooth(bluetoothButton);
                        break;
                }
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equalsIgnoreCase(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                switch (state) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        /*case WifiManager.WIFI_STATE_ENABLING:*/
                        enableWifi(wifiButton);
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
                }
            }
        }
    };

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

    public void btnOnClick(View view) {
        if (isReceiver) {
            startActivity(new Intent(PreparationsActivity.this, ReceiverActivity.class)
                    .putExtra(Keyword.EXTRA_RECEIVE, true)
                    .putExtra(EXTRA_ACTIVITY_SUBTITLE, getString(R.string.text_receive))
                    .putExtra(EXTRA_REQUEST_TYPE,
                            ReceiverActivity.RequestType.MAKE_ACQUAINTANCE.toString()));
            finish();
        } else if (isSender && getDefaultPreferences().getLong("add_devices_to_transfer", -1) != -1) {
            ViewTransferActivity.startInstance(this, getDefaultPreferences().getLong("add_devices_to_transfer", -1));
            startActivity(new Intent(PreparationsActivity.this, /*SenderActivity*/SenderActivityDemo.class)
                    .putExtra(Keyword.EXTRA_SEND, true)
                    .putExtra(SenderActivity.EXTRA_ACTIVITY_SUBTITLE, getString(R.string.text_receive))
                    .putExtra(SenderActivity.EXTRA_REQUEST_TYPE,
                            SenderActivity.RequestType.MAKE_ACQUAINTANCE.toString()));
            finish();
        }
    }

    /* SHARE ACTIVITY CODE WILL BE ON THIS CLASS FOR SURE */

    private List<Uri> mFileUris;
    private List<CharSequence> mFileNames;
    private OrganizeShareRunningTask mTask;

    public void updateText(WorkerService.RunningTask runningTask, final String text) {
        if (isFinishing())
            return;

        runningTask.publishStatusText(text);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                createSnackbar(R.string.msg_merg_send, text).show();

            }
        });
    }

    @Override
    public void onAttachedToTask(WorkerService.RunningTask task) {

    }

    /*public ProgressBar getProgressBar() {
        return null;
    }*/

    public void updateProgress(final int total, final int current) {
        if (isFinishing())
            return;

        String FUCKED_SITUATION = "DON'T GO THERE OTHERWISE YOU KNOW WHAT WILL HAPPEN";

        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressTextLeft.setText(String.valueOf(current));
                mProgressTextRight.setText(String.valueOf(total));
            }
        });

        mProgressBar.setProgress(current);
        mProgressBar.setMax(total);*/
    }

    @Override
    protected void onPreviousRunningTask(@Nullable WorkerService.RunningTask task) {
        super.onPreviousRunningTask(task);

        if (task instanceof OrganizeShareRunningTask) {
            mTask = ((OrganizeShareRunningTask) task);
            mTask.setAnchorListener(this);
        } else {
            mTask = new OrganizeShareRunningTask(mFileUris, mFileNames);

            mTask.setTitle(getString(R.string.mesg_organizingFiles))
                    .setAnchorListener(this)
                    .setContentIntent(this, getIntent())
                    .run(this);

            attachRunningTask(mTask);
        }
    }
}
