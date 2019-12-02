package com.hazelmobile.filetransfer.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ShapeDrawable;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.genonbeta.android.framework.util.Interrupter;
import com.google.android.material.snackbar.Snackbar;
import com.hazelmobile.filetransfer.Callback;
import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.app.Activity;
import com.hazelmobile.filetransfer.database.AccessDatabase;
import com.hazelmobile.filetransfer.library.RippleBackground;
import com.hazelmobile.filetransfer.object.NetworkDevice;
import com.hazelmobile.filetransfer.pictures.AppUtils;
import com.hazelmobile.filetransfer.receiver.NetworkStatusReceiver;
import com.hazelmobile.filetransfer.service.CommunicationService;
import com.hazelmobile.filetransfer.ui.UIConnectionUtils;
import com.hazelmobile.filetransfer.ui.UITask;
import com.hazelmobile.filetransfer.ui.callback.NetworkDeviceSelectedListener;
import com.hazelmobile.filetransfer.ui.fragment.HotspotManagerFragment;
import com.hazelmobile.filetransfer.ui.fragment.SenderFragmentImpl;
import com.hazelmobile.filetransfer.util.ConnectionUtils;
import com.hazelmobile.filetransfer.util.NetworkDeviceLoader;

import static com.hazelmobile.filetransfer.service.CommunicationService.ACTION_HOTSPOT_STATUS;
import static com.hazelmobile.filetransfer.service.CommunicationService.EXTRA_HOTSPOT_DISABLING;
import static com.hazelmobile.filetransfer.service.CommunicationService.EXTRA_HOTSPOT_ENABLED;

public class ReceiverActivity extends Activity
        implements SnackbarSupport {

    public static final String EXTRA_DEVICE_ID = "extraDeviceId";
    public static final String EXTRA_REQUEST_TYPE = "extraRequestType";
    public static final String EXTRA_CONNECTION_ADAPTER = "extraConnectionAdapter";

    private RequestType mRequestType = RequestType.RETURN_RESULT;
    private final IntentFilter mFilter = new IntentFilter();

    private ImageView user_image;
    private TextView textView, receiver_status;
    private boolean mHotspotClosed = false;
    private UIConnectionUtils mConnectionUtils;
    private TextView hotspot_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);

        if (getIntent().hasExtra(EXTRA_REQUEST_TYPE))
            try {
                mRequestType = RequestType.valueOf(getIntent().getStringExtra(EXTRA_REQUEST_TYPE));
            } catch (Exception e) {
                // do nothing
            }

        if (!UIConnectionUtils.isOreoAbove()) {
            getUIConnectionUtils().getConnectionUtils().getBluetoothAdapter().disable();
        }

        getSupportFragmentManager().beginTransaction().add
                (R.id.activity_connection_establishing_content_view, new HotspotManagerFragment()).commit();

        mFilter.addAction(CommunicationService.ACTION_DEVICE_ACQUAINTANCE);
        mFilter.addAction(CommunicationService.ACTION_INCOMING_TRANSFER_READY);
        mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(ACTION_HOTSPOT_STATUS);
        mFilter.addAction(NetworkStatusReceiver.WIFI_AP_STATE_CHANGED);
        mFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);

        initViews();

    }

    private ConnectionUtils getConnectionUtils() {
        return getUIConnectionUtils().getConnectionUtils();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mHotspotClosed)
            if (UIConnectionUtils.isOreoAbove())
                AppUtils.startForegroundService(this, new Intent(this, CommunicationService.class)
                        .setAction(CommunicationService.ACTION_TOGGLE_HOTSPOT));
            else
                getUIConnectionUtils().getConnectionUtils().getHotspotUtils().disable();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
            finish();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public Snackbar createSnackbar(int resId, Object... objects) {
        return null;
    }

    public UIConnectionUtils getUIConnectionUtils() {
        if (mConnectionUtils == null) {
            mConnectionUtils = new UIConnectionUtils(ConnectionUtils.getInstance(this), this);
        }

        return mConnectionUtils;
    }

    private void initViews() {
        final ImageView back = findViewById(R.id.receiver_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final RippleBackground pulse = findViewById(R.id.content);
        pulse.startRippleAnimation();
        hotspot_name = findViewById(R.id.receiver_status_name);
        hotspot_name.setText(HotspotManagerFragment.HOTSPOT_NAME);

        user_image = findViewById(R.id.userProfileImage);
        textView = findViewById(R.id.text1);
        receiver_status = findViewById(R.id.receiver_status);

        setProfilePicture();

        final Observer<String> selectObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String hotspot_nam) {
                hotspot_name.setText(hotspot_nam);
            }
        };
        Callback.getHotspotName().observe(this, selectObserver);
    }

    private void setProfilePicture() {
        NetworkDevice localDevice = AppUtils.getLocalDevice(ReceiverActivity.this);
        textView.setText(localDevice.nickname);
        loadProfilePictureInto(localDevice.nickname, user_image);
        int color = AppUtils.getDefaultPreferences(ReceiverActivity.this).getInt("device_name_color", -1);

        if (user_image.getDrawable() instanceof ShapeDrawable && color != -1) {
            ShapeDrawable shapeDrawable = (ShapeDrawable) user_image.getDrawable();
            shapeDrawable.getPaint().setColor(color);
        } else {
            user_image.setBackgroundResource(R.drawable.background_user_icon_default);
        }
    }

    private void updateHotspotState() {
        boolean isEnabled = getUIConnectionUtils().getConnectionUtils().getHotspotUtils().isEnabled();

        if (!isEnabled) {
            /*enableHostspot(hotspotButton);*/
            receiver_status.setText(R.string.text_receive_status);
        } else if (Build.VERSION.SDK_INT >= 26) {
            AppUtils.startForegroundService(this,
                    new Intent(this, CommunicationService.class)
                            .setAction(CommunicationService.ACTION_REQUEST_HOTSPOT_STATUS));
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equalsIgnoreCase(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        /*case BluetoothAdapter.STATE_TURNING_ON:*/
                        receiver_status.setText(R.string.text_receive_status);
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        if (UIConnectionUtils.isOreoAbove())
                            receiver_status.setText("Bluetooth is disabled, Kindly open it to start the Process");
                        break;
                }
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equalsIgnoreCase(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                switch (state) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        /*case WifiManager.WIFI_STATE_ENABLING:*/
                        receiver_status.setText(R.string.text_receive_status);
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        receiver_status.setText("Wifi is disabled, Kindly open it to start the Process");
                        break;
                }
            } else if (NetworkStatusReceiver.WIFI_AP_STATE_CHANGED.equals(intent.getAction()))
                updateHotspotState();
            else if (ACTION_HOTSPOT_STATUS.equals(intent.getAction())) {
                if (intent.getBooleanExtra(EXTRA_HOTSPOT_ENABLED, false)) {
                    /*isHotspot = true;*/
                    receiver_status.setText(R.string.text_receive_status);
                } else if (getConnectionUtils().getHotspotUtils().isEnabled()
                        && !intent.getBooleanExtra(EXTRA_HOTSPOT_DISABLING, false)) {
                    receiver_status.setText("Hotspot is disabled, Kindly open it to start the Process");
                }
            } else if (mRequestType.equals(RequestType.RETURN_RESULT)) {
                if (CommunicationService.ACTION_DEVICE_ACQUAINTANCE.equals(intent.getAction())
                        && intent.hasExtra(CommunicationService.EXTRA_DEVICE_ID)
                        && intent.hasExtra(CommunicationService.EXTRA_CONNECTION_ADAPTER_NAME)) {
                    NetworkDevice device = new NetworkDevice(intent.getStringExtra(CommunicationService.EXTRA_DEVICE_ID));
                    NetworkDevice.Connection connection = new NetworkDevice.Connection(device.deviceId, intent.getStringExtra(CommunicationService.EXTRA_CONNECTION_ADAPTER_NAME));

                    try {
                        AppUtils.getDatabase(ReceiverActivity.this).reconstruct(device);
                        AppUtils.getDatabase(ReceiverActivity.this).reconstruct(connection);

                        mDeviceSelectionListener.onNetworkDeviceSelected(device, connection);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (mRequestType.equals(RequestType.MAKE_ACQUAINTANCE)) {
                if (CommunicationService.ACTION_INCOMING_TRANSFER_READY.equals(intent.getAction())
                        && intent.hasExtra(CommunicationService.EXTRA_GROUP_ID)) {
                    mHotspotClosed = true;
                    ViewTransferActivity.startInstance(ReceiverActivity.this,
                            intent.getLongExtra(CommunicationService.EXTRA_GROUP_ID, -1));
                    SenderFragmentImpl.showMessage("EstablishConnection(): Going to Transfer Activity Finally");
                    finish();
                }
            } else if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                if (!getUIConnectionUtils().getConnectionUtils().isLocationServiceEnabled()) {
                    receiver_status.setText("Location is disabled, Kindly open it to start the Process");
                } else {
                    receiver_status.setText(R.string.text_receive_status);
                }
            }
        }
    };

    private final NetworkDeviceSelectedListener mDeviceSelectionListener = new NetworkDeviceSelectedListener() {
        @Override
        public boolean onNetworkDeviceSelected(NetworkDevice networkDevice, NetworkDevice.Connection connection) {
            if (mRequestType.equals(RequestType.RETURN_RESULT)) {
                setResult(RESULT_OK, new Intent()
                        .putExtra(EXTRA_DEVICE_ID, networkDevice.deviceId)
                        .putExtra(EXTRA_CONNECTION_ADAPTER, connection.adapterName));

                finish();
            } else {
                ConnectionUtils connectionUtils = ConnectionUtils.getInstance(ReceiverActivity.this);
                UIConnectionUtils uiConnectionUtils = new UIConnectionUtils(connectionUtils, ReceiverActivity.this);

                UITask uiTask = new UITask() {
                    @Override
                    public void updateTaskStarted(Interrupter interrupter) {
                        Log.d(SenderFragmentImpl.TAG, "sending file started");
                    }

                    @Override
                    public void updateTaskStopped() {
                        Log.d(SenderFragmentImpl.TAG, "sending file stopped due to some reason");
                    }
                };

                NetworkDeviceLoader.OnDeviceRegisteredListener registeredListener = new NetworkDeviceLoader.OnDeviceRegisteredListener() {
                    @Override
                    public void onDeviceRegistered(AccessDatabase database, final NetworkDevice device, final NetworkDevice.Connection connection) {
                        createSnackbar(R.string.mesg_completing).show();
                    }
                };

                uiConnectionUtils.makeAcquaintance(ReceiverActivity.this, uiTask,
                        connection.ipAddress, -1, registeredListener);
            }

            return true;
        }

        @Override
        public boolean isListenerEffective() {
            return true;
        }
    };

    public enum RequestType {
        RETURN_RESULT,
        MAKE_ACQUAINTANCE
    }

}
