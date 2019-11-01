package com.hazelmobile.filetransfer.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.genonbeta.android.framework.util.Interrupter;
import com.google.android.material.snackbar.Snackbar;
import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.app.Activity;
import com.hazelmobile.filetransfer.database.AccessDatabase;
import com.hazelmobile.filetransfer.library.RippleBackground;
import com.hazelmobile.filetransfer.object.NetworkDevice;
import com.hazelmobile.filetransfer.pictures.AppUtils;
import com.hazelmobile.filetransfer.service.CommunicationService;
import com.hazelmobile.filetransfer.ui.UIConnectionUtils;
import com.hazelmobile.filetransfer.ui.UITask;
import com.hazelmobile.filetransfer.ui.callback.NetworkDeviceSelectedListener;
import com.hazelmobile.filetransfer.ui.fragment.BarcodeConnectFragmentDemo;
import com.hazelmobile.filetransfer.ui.fragment.HotspotManagerFragment;
import com.hazelmobile.filetransfer.util.ConnectionUtils;
import com.hazelmobile.filetransfer.util.NetworkDeviceLoader;

import static com.hazelmobile.filetransfer.ui.activity.PermissionsActivity.EXTRA_CLOSE_PERMISSION_SCREEN;

public class ReceiverActivity extends Activity
        implements SnackbarSupport {

    public static final String EXTRA_DEVICE_ID = "extraDeviceId";
    public static final String EXTRA_REQUEST_TYPE = "extraRequestType";
    public static final String EXTRA_ACTIVITY_SUBTITLE = "extraActivitySubtitle";
    public static final String EXTRA_CONNECTION_ADAPTER = "extraConnectionAdapter";
    public static final int REQUEST_CHOOSE_DEVICE = 100;

    private RequestType mRequestType = RequestType.RETURN_RESULT;
    private final IntentFilter mFilter = new IntentFilter();

    private ImageView user_image;
    private TextView textView;

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

        getSupportFragmentManager().beginTransaction().add
                (R.id.activity_connection_establishing_content_view, new HotspotManagerFragment()).commit();

        mFilter.addAction(CommunicationService.ACTION_DEVICE_ACQUAINTANCE);
        mFilter.addAction(CommunicationService.ACTION_INCOMING_TRANSFER_READY);

        initViews();

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

        user_image = findViewById(R.id.userProfileImage);
        textView = findViewById(R.id.text1);

        setProfilePicture();
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
    public void onBackPressed() {
        setResult(RESULT_OK, new Intent()
                .putExtra(EXTRA_CLOSE_PERMISSION_SCREEN, true));
        finish();
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHOOSE_DEVICE)
            if (resultCode == RESULT_OK && data != null) {
                try {
                    NetworkDevice device = new NetworkDevice(data.getStringExtra(BarcodeScannerActivityDemo.EXTRA_DEVICE_ID));
                    AppUtils.getDatabase(ReceiverActivity.this).reconstruct(device);
                    NetworkDevice.Connection connection = new NetworkDevice.Connection(device.deviceId, data.getStringExtra(BarcodeScannerActivityDemo.EXTRA_CONNECTION_ADAPTER));
                    AppUtils.getDatabase(ReceiverActivity.this).reconstruct(connection);

                    mDeviceSelectionListener.onNetworkDeviceSelected(device, connection);
                } catch (Exception e) {
                    // do nothing
                }
            }
    }


    @Override
    public Snackbar createSnackbar(int resId, Object... objects) {
        return null;
    }

    public enum RequestType {
        RETURN_RESULT,
        MAKE_ACQUAINTANCE
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mRequestType.equals(RequestType.RETURN_RESULT)) {
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
                    ViewTransferActivity.startInstance(ReceiverActivity.this,
                            intent.getLongExtra(CommunicationService.EXTRA_GROUP_ID, -1));
                    BarcodeConnectFragmentDemo.showMessage("yes I'm here");
                    finish();
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
                        Log.d(BarcodeConnectFragmentDemo.TAG, "sending file started");
                    }

                    @Override
                    public void updateTaskStopped() {
                        Log.d(BarcodeConnectFragmentDemo.TAG, "sending file stopped due to some reason");
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

   /* public interface DeviceSelectionSupport {
        void setDeviceSelectedListener(NetworkDeviceSelectedListener listener);
    }*/

}
