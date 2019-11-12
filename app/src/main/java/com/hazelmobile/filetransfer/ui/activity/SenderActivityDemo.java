package com.hazelmobile.filetransfer.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.genonbeta.android.framework.util.Interrupter;
import com.google.android.material.snackbar.Snackbar;
import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.app.Activity;
import com.hazelmobile.filetransfer.database.AccessDatabase;
import com.hazelmobile.filetransfer.object.NetworkDevice;
import com.hazelmobile.filetransfer.object.TransferGroup;
import com.hazelmobile.filetransfer.pictures.AppUtils;
import com.hazelmobile.filetransfer.service.WorkerService;
import com.hazelmobile.filetransfer.task.AddDeviceRunningTaskDemo;
import com.hazelmobile.filetransfer.ui.UIConnectionUtils;
import com.hazelmobile.filetransfer.ui.UITask;
import com.hazelmobile.filetransfer.ui.callback.NetworkDeviceSelectedListener;
import com.hazelmobile.filetransfer.ui.fragment.SenderFragmentImplDemo;
import com.hazelmobile.filetransfer.util.ConnectionUtils;
import com.hazelmobile.filetransfer.util.NetworkDeviceLoader;

import static com.hazelmobile.filetransfer.ui.activity.PreparationsActivity.EXTRA_CLOSE_PERMISSION_SCREEN;


public class SenderActivityDemo extends Activity
        implements SnackbarSupport, WorkerService.OnAttachListener {

    public static final String EXTRA_DEVICE_ID = "extraDeviceId";
    public static final String EXTRA_REQUEST_TYPE = "extraRequestType";
    public static final String EXTRA_ACTIVITY_SUBTITLE = "extraActivitySubtitle";
    public static final String EXTRA_CONNECTION_ADAPTER = "extraConnectionAdapter";
    public static final int REQUEST_CHOOSE_DEVICE = 100;

    private RequestType mRequestType = RequestType.RETURN_RESULT;
    private ImageView user_image;
    private TextView textView;
    private TransferGroup mGroup = null;
    private AddDeviceRunningTaskDemo mTask;
    public static final String EXTRA_GROUP_ID = "extraGroupId";
    private IntentFilter mFilter = new IntentFilter(AccessDatabase.ACTION_DATABASE_CHANGE);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        if (!checkGroupIntegrity())
            return;
        setContentView(R.layout.activity_sender_demo);
//        final RippleBackground pulse = findViewById(R.id.content);
//        pulse.startRippleAnimation();
//
//        user_image = findViewById(R.id.userProfileImage);
//        textView = findViewById(R.id.text1);
//        setProfilePicture();
        startCodeScannerFragment();
        final ImageView back = findViewById(R.id.sender_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkForTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mFilter);
        if (!checkGroupIntegrity())
            finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public Intent getIntent() {
        return super.getIntent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHOOSE_DEVICE)
            if (resultCode == RESULT_OK && data != null) {
                try {
                    NetworkDevice device = new NetworkDevice(data.getStringExtra(EXTRA_DEVICE_ID));
                    AppUtils.getDatabase(SenderActivityDemo.this).reconstruct(device);
                    NetworkDevice.Connection connection = new NetworkDevice.Connection(device.deviceId, data.getStringExtra(EXTRA_CONNECTION_ADAPTER));
                    AppUtils.getDatabase(SenderActivityDemo.this).reconstruct(connection);

                    mDeviceSelectionListener.onNetworkDeviceSelected(device, connection);
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

    @Override
    public void onAttachedToTask(WorkerService.RunningTask task) {

    }

    @Override
    protected void onPreviousRunningTask(@Nullable WorkerService.RunningTask task) {
        super.onPreviousRunningTask(task);

        if (task instanceof AddDeviceRunningTaskDemo) {
            mTask = ((AddDeviceRunningTaskDemo) task);
            mTask.setAnchorListener(this);
        }
    }

    @Override
    public Snackbar createSnackbar(int resId, Object... objects) {
        return Snackbar.make(findViewById(R.id.activity_connection_establishing_content_view), getString(resId, objects), Snackbar.LENGTH_LONG);
    }

    private void startCodeScannerFragment() {

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SenderFragmentImplDemo fragment = (SenderFragmentImplDemo) getSupportFragmentManager().findFragmentById(R.id.senderFragment);

        if (fragment != null)
            fragment.setDeviceSelectedListener(new NetworkDeviceSelectedListener() {
                @Override
                public boolean onNetworkDeviceSelected(NetworkDevice networkDevice, NetworkDevice.Connection connection) {

                    try {

                        AppUtils.getDatabase(SenderActivityDemo.this).reconstruct(networkDevice);
                        AppUtils.getDatabase(SenderActivityDemo.this).reconstruct(connection);
                        mDeviceSelectionListener.onNetworkDeviceSelected(networkDevice, connection);

                    } catch (Exception e) {
                        // do nothing
                    }

                    /*setResult(RESULT_OK, new Intent()
                            .putExtra(EXTRA_DEVICE_ID, networkDevice.deviceId)
                            .putExtra(EXTRA_CONNECTION_ADAPTER, connection.adapterName));
                    finish();*/

                    return true;
                }

                @Override
                public boolean isListenerEffective() {
                    return true;
                }
            });
    }

    public void doCommunicate(final NetworkDevice device, final NetworkDevice.Connection connection) {
        AddDeviceRunningTaskDemo task = new AddDeviceRunningTaskDemo(mGroup, device, connection);

        task.setTitle(getString(R.string.mesg_communicating))
                .setAnchorListener(this)
                .setContentIntent(this, getIntent())
                .run(this);

        attachRunningTask(task);
    }

    public boolean checkGroupIntegrity() {
        try {

            if (getDefaultPreferences().getLong("add_devices_to_transfer", -1) == -1)
                throw new Exception(getString(R.string.text_empty));

            mGroup = new TransferGroup(getDefaultPreferences().getLong("add_devices_to_transfer", -1));

            try {
                getDatabase().reconstruct(mGroup);
            } catch (Exception e) {
                throw new Exception(getString(R.string.mesg_notValidTransfer));
            }

            return true;
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }

        return false;
    }

    public interface DeviceSelectionSupport {
        void setDeviceSelectedListener(NetworkDeviceSelectedListener listener);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AccessDatabase.ACTION_DATABASE_CHANGE.equals(intent.getAction()))
                if (intent.hasExtra(AccessDatabase.EXTRA_TABLE_NAME)
                        && AccessDatabase.TABLE_TRANSFERGROUP.equals(intent.getStringExtra(AccessDatabase.EXTRA_TABLE_NAME)))
                    if (!checkGroupIntegrity())
                        finish();
        }
    };

    private final NetworkDeviceSelectedListener mDeviceSelectionListener = new NetworkDeviceSelectedListener() {
        @Override
        public boolean onNetworkDeviceSelected(NetworkDevice networkDevice, NetworkDevice.Connection connection) {
            if (mRequestType.equals(RequestType.RETURN_RESULT)) {

                try {
                    getDatabase().reconstruct(networkDevice);
                    getDatabase().reconstruct(connection);
                    doCommunicate(networkDevice, connection);
                } catch (Exception e) {
                    Toast.makeText(SenderActivityDemo.this,
                            R.string.mesg_somethingWentWrong, Toast.LENGTH_SHORT).show();
                }

                finish();
            } else {
                ConnectionUtils connectionUtils = ConnectionUtils.getInstance(SenderActivityDemo.this);
                UIConnectionUtils uiConnectionUtils = new UIConnectionUtils(connectionUtils, SenderActivityDemo.this);

                UITask uiTask = new UITask() {
                    @Override
                    public void updateTaskStarted(Interrupter interrupter) {
                        createSnackbar(R.string.text_sending).show();
                    }

                    @Override
                    public void updateTaskStopped() {
                        createSnackbar(R.string.mesg_fileSendError).show();
                    }
                };

                NetworkDeviceLoader.OnDeviceRegisteredListener registeredListener = new NetworkDeviceLoader.OnDeviceRegisteredListener() {
                    @Override
                    public void onDeviceRegistered(AccessDatabase database, final NetworkDevice device, final NetworkDevice.Connection connection) {
                        createSnackbar(R.string.mesg_completing).show();
                    }
                };

                uiConnectionUtils.makeAcquaintance(SenderActivityDemo.this, uiTask,
                        connection.ipAddress, -1, registeredListener);
            }

            return true;
        }

        @Override
        public boolean isListenerEffective() {
            return true;
        }
    };

    public void updateProgress(final int total, final int current) {
        /*if (isFinishing())
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressTextLeft.setText(String.valueOf(current));
                mProgressTextRight.setText(String.valueOf(total));
            }
        });

        mProgressBar.setProgress(current);
        mProgressBar.setMax(total);*/
    }

    public enum RequestType {
        RETURN_RESULT,
        MAKE_ACQUAINTANCE;

    }

}
