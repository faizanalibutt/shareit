package com.hazelmobile.filetransfer.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;

import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.genonbeta.android.framework.util.Interrupter;
import com.google.android.material.snackbar.Snackbar;
import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.database.AccessDatabase;
import com.hazelmobile.filetransfer.object.NetworkDevice;
import com.hazelmobile.filetransfer.ui.UIConnectionUtils;
import com.hazelmobile.filetransfer.ui.UITask;
import com.hazelmobile.filetransfer.ui.callback.NetworkDeviceSelectedListener;
import com.hazelmobile.filetransfer.ui.fragment.HotspotManagerFragmentDemo;
import com.hazelmobile.filetransfer.util.ConnectionUtils;
import com.hazelmobile.filetransfer.util.NetworkDeviceLoader;

import static com.hazelmobile.filetransfer.ui.activity.PermissionsActivity.EXTRA_CLOSE_PERMISSION_SCREEN;

//import com.hazelmobile.filetransfer.ui.fragment.BarcodeConnectFragmentDemo;


public class ConnectionManagerActivityDemo
        extends BaseActivity
        implements SnackbarSupport {

    public static final String EXTRA_DEVICE_ID = "extraDeviceId";
    public static final String EXTRA_REQUEST_TYPE = "extraRequestType";
    public static final String EXTRA_ACTIVITY_SUBTITLE = "extraActivitySubtitle";
    public static final String SEND = "send";
    public static final String RECEIVE = "receive";
    public static final String EXTRA_CONNECTION_ADAPTER = "extraConnectionAdapter";
    public static final int REQUEST_CHOOSE_DEVICE = 100;
    private BluetoothAdapter bluetoothAdapter;
    private RequestType mRequestType = RequestType.RETURN_RESULT;

    private final NetworkDeviceSelectedListener mDeviceSelectionListener = new NetworkDeviceSelectedListener() {
        @Override
        public boolean onNetworkDeviceSelected(NetworkDevice networkDevice, NetworkDevice.Connection connection) {
            if (mRequestType.equals(RequestType.RETURN_RESULT)) {
                setResult(RESULT_OK, new Intent()
                        .putExtra(EXTRA_DEVICE_ID, networkDevice.deviceId)
                        .putExtra(EXTRA_CONNECTION_ADAPTER, connection.adapterName));

                finish();
            } else {
                ConnectionUtils connectionUtils = ConnectionUtils.getInstance(ConnectionManagerActivityDemo.this);
                UIConnectionUtils uiConnectionUtils = new UIConnectionUtils(connectionUtils, ConnectionManagerActivityDemo.this);

                UITask uiTask = new UITask() {
                    @Override
                    public void updateTaskStarted(Interrupter interrupter) {
                        //Log.d(BarcodeConnectFragmentDemo.TAG, "sending file started");
                    }

                    @Override
                    public void updateTaskStopped() {
                        //Log.d(BarcodeConnectFragmentDemo.TAG, "sending file stopped due to some reason");
                    }
                };

                NetworkDeviceLoader.OnDeviceRegisteredListener registeredListener = new NetworkDeviceLoader.OnDeviceRegisteredListener() {
                    @Override
                    public void onDeviceRegistered(AccessDatabase database, final NetworkDevice device, final NetworkDevice.Connection connection) {
                        createSnackbar(R.string.mesg_completing).show();
                    }
                };

                uiConnectionUtils.makeAcquaintance(ConnectionManagerActivityDemo.this, uiTask,
                        connection.ipAddress, -1, registeredListener);
            }

            return true;
        }

        @Override
        public boolean isListenerEffective() {
            return true;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.demo_activity_connection_manager);

        if (getIntent() != null) {
            if (getIntent().hasExtra(ConnectionManagerActivityDemo.RECEIVE) && getIntent().getBooleanExtra(ConnectionManagerActivityDemo.RECEIVE, false)) {
                getSupportFragmentManager().beginTransaction().add(R.id.activity_connection_establishing_content_view, new HotspotManagerFragmentDemo()).commit();
            } else {
                startCodeScanner();
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHOOSE_DEVICE)
            if (resultCode == RESULT_OK && data != null) {
                try {
                    /*NetworkDevice device = new NetworkDevice(data.getStringExtra(BarcodeScannerActivityDemo.EXTRA_DEVICE_ID));
                    AppUtils.getDatabase(ConnectionManagerActivityDemo.this).reconstruct(device);
                    NetworkDevice.Connection connection = new NetworkDevice.Connection(device.deviceId, data.getStringExtra(BarcodeScannerActivityDemo.EXTRA_CONNECTION_ADAPTER));
                    AppUtils.getDatabase(ConnectionManagerActivityDemo.this).reconstruct(connection);*/

//                    mDeviceSelectionListener.onNetworkDeviceSelected(device, connection);
                } catch (Exception e) {
                    // do nothing
                }
            }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
            onBackPressed();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, new Intent()
                .putExtra(EXTRA_CLOSE_PERMISSION_SCREEN, true));
        finish();
        super.onBackPressed();
    }

    private void startCodeScanner() {
        /*startActivityForResult(new Intent(ConnectionManagerActivityDemo.this, BarcodeScannerActivityDemo.class),
                REQUEST_CHOOSE_DEVICE);*/
    }

    public enum RequestType {
        RETURN_RESULT,
        MAKE_ACQUAINTANCE
    }

    @Override
    public Snackbar createSnackbar(int resId, Object... objects) {
        return Snackbar.make(findViewById(R.id.activity_connection_establishing_content_view), getString(resId, objects), Snackbar.LENGTH_LONG);
    }

}
