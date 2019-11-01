package com.hazelmobile.filetransfer.ui.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.google.android.material.snackbar.Snackbar;
import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.app.Activity;
import com.hazelmobile.filetransfer.pictures.Keyword;
import com.hazelmobile.filetransfer.ui.UIConnectionUtils;
import com.hazelmobile.filetransfer.util.ConnectionUtils;

import java.util.Objects;

import static com.hazelmobile.filetransfer.service.CommunicationService.ACTION_HOTSPOT_STATUS;
import static com.hazelmobile.filetransfer.service.CommunicationService.EXTRA_HOTSPOT_DISABLING;
import static com.hazelmobile.filetransfer.service.CommunicationService.EXTRA_HOTSPOT_ENABLED;

public class PermissionsActivity extends Activity implements SnackbarSupport {

    private static final int LOCATION_PERMISSION_RESULT = 0;
    private static final int LOCATION_SERVICE_RESULT = 1;
    public static final String EXTRA_CLOSE_PERMISSION_SCREEN = "permissions";
    private ImageView bluetoothStatus, wifiStatus, gpsStatus;
    private AppCompatButton gpsButton, nextScreen, bluetoothButton, wifiButton;
    private boolean isWifi, isBluetooth, isGps, isAllEnabled = false;
    private boolean isSender = false;
    private boolean isReceiver = false;

    private boolean mHotspotStartedExternally = false;

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
        return null;
    }

    private class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*if (NetworkStatusReceiver.WIFI_AP_STATE_CHANGED.equals(intent.getAction()))
                updateState();
            else */
            if (ACTION_HOTSPOT_STATUS.equals(intent.getAction())) {
                if (intent.getBooleanExtra(EXTRA_HOTSPOT_ENABLED, false))
                    return;
                else if (getConnectionUtils().getHotspotUtils().isEnabled()
                        && !intent.getBooleanExtra(EXTRA_HOTSPOT_DISABLING, false)) {

                }
            }
        }
    }

    // TODO: 10/23/2019 remove this in future #33
    public static final int REQUEST_CODE_CHOOSE_DEVICE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        if (ConnectionUtils.getInstance(this).getBluetoothAdapter().isEnabled()) {
            enableBluetooth(bluetoothButton);
        }
        if (ConnectionUtils.getInstance(this).getWifiManager().isWifiEnabled()) {
            enableWifi(wifiButton);
        }
        if (ConnectionUtils.getInstance(this).isLocationServiceEnabled()) {
            enableGPS(gpsButton);
        }

    }

    private void init() {
        setContentView(R.layout.activity_permissions);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        if (getIntent() != null) {
            isReceiver = getIntent().hasExtra(Keyword.EXTRA_RECEIVE)
                    && getIntent().getBooleanExtra(Keyword.EXTRA_RECEIVE, false);
            isSender = getIntent().hasExtra(Keyword.EXTRA_SEND)
                    && getIntent().getBooleanExtra(Keyword.EXTRA_SEND, false);

            if (isReceiver)
                getSupportActionBar().setTitle(R.string.text_receivePreparations);
            if (isSender)
                getSupportActionBar().setTitle(R.string.text_sendPreparations);

        }

        bluetoothStatus = findViewById(R.id.bluetoothStatus);
        /*wifiStatus = findViewById(R.id.wifiStatus);
        gpsStatus = findViewById(R.id.gpsStatus);
        nextScreen = findViewById(R.id.button);
        wifiButton = findViewById(R.id.wifiClick);
        bluetoothButton = findViewById(R.id.bluetoothClick);
        gpsButton = findViewById(R.id.gpsClick);*/
    }

    public void openBluetooth(View view) {
        ConnectionUtils.getInstance(this).openBluetooth();
        if (!ConnectionUtils.getInstance(this).getBluetoothAdapter().isEnabled()) {
            bluetoothStatus.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
            isBluetooth = false;
        } else {
            enableBluetooth(view);
        }
    }

    private void enableBluetooth(View view) {
        view.setVisibility(View.GONE);
        bluetoothStatus.setVisibility(View.VISIBLE);
        isBluetooth = true;
        isAllEnabled = isWifi && isGps;
        if (isAllEnabled) {
            nextScreen.setEnabled(true);
        }
    }

    public void openWifi(View view) {
        ConnectionUtils.getInstance(this).openWifi();
        if (!ConnectionUtils.getInstance(this).getWifiManager().isWifiEnabled()) {
            view.setVisibility(View.VISIBLE);
            wifiStatus.setVisibility(View.GONE);
            isWifi = false;
        } else {
            enableWifi(view);
        }
    }

    private void enableWifi(View view) {
        isWifi = true;
        view.setVisibility(View.GONE);
        wifiStatus.setVisibility(View.VISIBLE);
        isAllEnabled = isBluetooth && isGps;
        if (isAllEnabled) {
            nextScreen.setEnabled(true);
        }
    }

    public void openGPS(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(PermissionsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (ConnectionUtils.getInstance(this).isLocationServiceEnabled()) {
                    enableGPS(view);
                    return;
                }
            }
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_RESULT);
        } else {
            // check for location service
            if (ConnectionUtils.getInstance(this).isLocationServiceEnabled()) {
                enableGPS(view);
            } else {
                view.setVisibility(View.VISIBLE);
                gpsStatus.setVisibility(View.GONE);
                isGps = false;
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_SERVICE_RESULT);
            }
        }
    }

    private void enableGPS(View view) {
        view.setVisibility(View.GONE);
        gpsStatus.setVisibility(View.VISIBLE);
        isGps = true;
        isAllEnabled = isBluetooth && isWifi;
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
                    Toast.makeText(PermissionsActivity.this, "Please enable location service to proceed", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_CODE_CHOOSE_DEVICE
                    && data != null) {

                if (data.hasExtra(ReceiverActivity.EXTRA_DEVICE_ID) && data.hasExtra(ReceiverActivity.EXTRA_CONNECTION_ADAPTER)) {
                    String deviceId = data.getStringExtra(ReceiverActivity.EXTRA_DEVICE_ID);
                    String connectionAdapter = data.getStringExtra(ReceiverActivity.EXTRA_CONNECTION_ADAPTER);
                    setResult(RESULT_OK, new Intent()
                            .putExtra(ReceiverActivity.EXTRA_DEVICE_ID, deviceId)
                            .putExtra(ReceiverActivity.EXTRA_CONNECTION_ADAPTER, connectionAdapter)
                    );
                    finish();
                } else if (data.hasExtra(EXTRA_CLOSE_PERMISSION_SCREEN)) {
                    boolean isPermission = data.getBooleanExtra(EXTRA_CLOSE_PERMISSION_SCREEN, false);
                    if (isPermission) {
                        finish();
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_RESULT) {
            if (ContextCompat.checkSelfPermission(PermissionsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                gpsButton.setVisibility(View.VISIBLE);
                gpsStatus.setVisibility(View.GONE);
                isGps = false;
                Toast.makeText(PermissionsActivity.this, "Please allow location permission to proceed", Toast.LENGTH_SHORT).show();
            } else {
                if (ConnectionUtils.getInstance(PermissionsActivity.this).isLocationServiceEnabled()) {
                    enableGPS(gpsButton);
                } else {
                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_SERVICE_RESULT);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, intentFilter);
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
                    case BluetoothAdapter.STATE_TURNING_ON:
                        enableBluetooth(bluetoothButton);
                        break;
                }
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equalsIgnoreCase(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                switch (state) {
                    case WifiManager.WIFI_STATE_ENABLED:
                    case WifiManager.WIFI_STATE_ENABLING:
                        enableWifi(wifiButton);
                        break;
                }
            }
        }
    };

    public void shareIT(View view) {
        if (isReceiver) {
            startActivityForResult(new Intent(PermissionsActivity.this, ReceiverActivity.class)
                    .putExtra(Keyword.EXTRA_RECEIVE, true)
                    .putExtra(ReceiverActivity.EXTRA_ACTIVITY_SUBTITLE, getString(R.string.text_receive))
                    .putExtra(ReceiverActivity.EXTRA_REQUEST_TYPE,
                            ReceiverActivity.RequestType.MAKE_ACQUAINTANCE.toString()), REQUEST_CODE_CHOOSE_DEVICE);
        } else if (isSender) {
            startActivityForResult(new Intent(PermissionsActivity.this, ConnectionManagerActivityDemo.class)
                    .putExtra(Keyword.EXTRA_SEND, true)
                    .putExtra(ConnectionManagerActivityDemo.EXTRA_ACTIVITY_SUBTITLE, getString(R.string.text_receive))
                    .putExtra(ConnectionManagerActivityDemo.EXTRA_REQUEST_TYPE,
                            ConnectionManagerActivityDemo.RequestType.MAKE_ACQUAINTANCE.toString()), REQUEST_CODE_CHOOSE_DEVICE);
        }
    }

    private static class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }
}
