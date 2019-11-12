package com.hazelmobile.filetransfer.ui.fragment;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.pictures.AppUtils;
import com.hazelmobile.filetransfer.pictures.Keyword;
import com.hazelmobile.filetransfer.receiver.NetworkStatusReceiver;
import com.hazelmobile.filetransfer.service.CommunicationService;
import com.hazelmobile.filetransfer.ui.UIConnectionUtils;
import com.hazelmobile.filetransfer.ui.callback.IconSupport;
import com.hazelmobile.filetransfer.ui.callback.TitleSupport;
import com.hazelmobile.filetransfer.util.ConnectionUtils;
import com.hazelmobile.filetransfer.util.HotspotUtils;
import com.hazelmobile.filetransfer.util.NetworkUtils;
import com.hazelmobile.filetransfer.widget.ExtensionsUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

import static com.hazelmobile.filetransfer.service.CommunicationService.ACTION_HOTSPOT_STATUS;
import static com.hazelmobile.filetransfer.service.CommunicationService.EXTRA_HOTSPOT_DISABLING;
import static com.hazelmobile.filetransfer.service.CommunicationService.EXTRA_HOTSPOT_ENABLED;
import static com.hazelmobile.filetransfer.ui.fragment.SenderFragmentImpl.APP_NAME;
import static com.hazelmobile.filetransfer.ui.fragment.SenderFragmentImpl.MY_UUID;
import static com.hazelmobile.filetransfer.ui.fragment.SenderFragmentImpl.STATE_MESSAGE_RECEIVED;


/**
 * created by: veli
 * modified by: faizi
 * date: 11/04/18 20:53
 */

public class HotspotManagerFragment
        extends com.genonbeta.android.framework.app.Fragment
        implements TitleSupport, IconSupport {

    private static final int REQUEST_LOCATION_PERMISSION_FOR_HOTSPOT = 643;
    public static final int STATE_PROGRESS = 644;
    private IntentFilter mIntentFilter = new IntentFilter();
    private StatusReceiver mStatusReceiver = new StatusReceiver();
    private UIConnectionUtils mConnectionUtils;

    //private boolean openWifiOnce = false;
    private MenuItem mHelpMenuItem;
    private boolean mWaitForHotspot = false;
    private boolean mWaitForWiFi = false;
    private boolean mHotspotStartedExternally = false;
    private static final int STATE_BLUETOOTH_DISCOVERABLE_REQUESTING = 23;
    //private ProgressBar progressBar;
    private TextView dataTransferSpeed;
    private TextView dataTransferTime;
    private ServerClass serverClass;
    private SendReceive sendReceive;
    private JSONObject hotspotInformation;
    private MyHandler mHandle = new MyHandler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIntentFilter.addAction(ACTION_HOTSPOT_STATUS);
        mIntentFilter.addAction(NetworkStatusReceiver.WIFI_AP_STATE_CHANGED);
        toggleHotspot();

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.demo_layout_hotspot_manager, container, false);

        //progressBar = view.findViewById(R.id.progressBar);
        //progressBar.setMax(100);
        //progressBar.setProgress/*coming from service wait for that code*/(0);

        dataTransferTime = view.findViewById(R.id.dataTransferTime);
        dataTransferSpeed = view.findViewById(R.id.dataTransferSpeed);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*if (getActivity() != null) getActivity().registerReceiver(
                mMessageReceiver, new IntentFilter("ReceiverProgress"));*/

        if (UIConnectionUtils.isOreoAbove()) {
            getorUpdateBluetoothDiscoverable();
            mHandle.sendMessageDelayed(mHandle.obtainMessage(STATE_BLUETOOTH_DISCOVERABLE_REQUESTING), 60000);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (getContext() != null) getContext().registerReceiver(mStatusReceiver, mIntentFilter);
        updateState();

        if (mWaitForHotspot)
            toggleHotspot();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null) getContext().unregisterReceiver(mStatusReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (UIConnectionUtils.isOreoAbove()) {
            /*if (getContext() != null) {
                Intent intent = new Intent(getContext(), CommunicationService.class);
                getContext().stopService(intent);
                getContext().unregisterReceiver(mMessageReceiver);
            }*/
                ConnectionUtils connectionUtils = ConnectionUtils.getInstance(getContext());
                if (connectionUtils.getBluetoothAdapter().isDiscovering())
                    connectionUtils.getBluetoothAdapter().cancelDiscovery();
                //connectionUtils.disableCurrentNetwork();
                mHandle.removeMessages(STATE_BLUETOOTH_DISCOVERABLE_REQUESTING);
                //mHandle.removeMessages(STATE_PROGRESS);
                //openWifiOnce = false;
                mHandle = null;
                hotspotInformation = null;

                if (sendReceive != null && sendReceive.bluetoothSocket != null)
                    sendReceive.bluetoothSocket.close();

                if (sendReceive != null) {
                    sendReceive.interrupt();
                    sendReceive = null;
                }

                if (serverClass != null && serverClass.serverSocket != null)
                    serverClass.serverSocket.close();

                if (serverClass != null) {
                    serverClass.interrupt();
                    serverClass = null;
                }

                Set<BluetoothDevice> bluetoothDeviceList = connectionUtils.getBluetoothAdapter().getBondedDevices();
                if (bluetoothDeviceList.size() > 0) {
                    for (BluetoothDevice bluetoothDevice : bluetoothDeviceList) {

                        try {
                        /*if (bluetoothDevice.getName().contains("TS") || bluetoothDevice.getName().contains("AndroidShare")) {
                            Method m = bluetoothDevice.getClass().getMethod("removeBond", (Class[]) null);
                            m.invoke(bluetoothDevice, (Object[]) null);
                            showMessage("SendReceive: Removed Device Name is: " + bluetoothDevice);
                        }*/
                            Method m = bluetoothDevice.getClass().getMethod("removeBond", (Class[]) null);
                            m.invoke(bluetoothDevice, (Object[]) null);
                            showMessage("SendReceive: Removed Device Name is: " + bluetoothDevice);
                        } catch (Exception e) {
                            showMessage("SendReceive: Removing has been failed." + e.getMessage());
                        }
                    }
                }
                connectionUtils.getBluetoothAdapter().disable();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("onDestroy(): " + e);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actions_hotspot_manager, menu);
        mHelpMenuItem = menu.findItem(R.id.show_help);

        showMenu();
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.show_help && getConnectionUtils().getHotspotUtils().getConfiguration() != null) {
            String hotspotName = getConnectionUtils().getHotspotUtils().getConfiguration().SSID;
            String friendlyName = AppUtils.getFriendlySSID(hotspotName);

            assert getActivity() != null;
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.mesg_hotspotCreatedInfo, hotspotName, friendlyName))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        } else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (REQUEST_LOCATION_PERMISSION_FOR_HOTSPOT == requestCode)
            toggleHotspot();
    }

    @Override
    public int getIconRes() {
        return R.drawable.ic_wifi_tethering_white_24dp;
    }

    @Override
    public CharSequence getTitle(Context context) {
        return context.getString(R.string.text_startHotspot);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            serverClass = new ServerClass(hotspotInformation);
            serverClass.start();
        }
    }

    private ConnectionUtils getConnectionUtils() {
        return getUIConnectionUtils().getConnectionUtils();
    }

    public UIConnectionUtils getUIConnectionUtils() {
        if (mConnectionUtils == null)
            mConnectionUtils = new UIConnectionUtils(ConnectionUtils.getInstance(getContext()), this);

        return mConnectionUtils;
    }

    private void toggleHotspot() {
        if (mHotspotStartedExternally)
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        else {
            if (getActivity() != null)
                getUIConnectionUtils().toggleHotspot(true, getActivity(), REQUEST_LOCATION_PERMISSION_FOR_HOTSPOT, mHotspotWatcher);
        }
    }

    private void getorUpdateBluetoothDiscoverable() {
        if (mHandle != null && getContext() != null && UIConnectionUtils.isOreoAbove()) {
            if (ConnectionUtils.getInstance(getContext()).getBluetoothAdapter().getScanMode() !=
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                //METHOD TO DISCOVERF WITHOUT KNOWING
                Method method;
                try {
                    ConnectionUtils.getInstance(getContext()).openBluetooth();
                    method = ConnectionUtils.getInstance(getContext()).getBluetoothAdapter().getClass().getMethod("setScanMode", int.class, int.class);
                    method.invoke(ConnectionUtils.getInstance(getContext()).getBluetoothAdapter(), BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 120);
                    showMessage("Method invoke successfully");
                    serverClass = new ServerClass(hotspotInformation);
                    serverClass.start();
                } catch (Exception e) {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(discoverableIntent, 0);
                    e.printStackTrace();
                }
            } else {
                showMessage("Scanner is Available");
                serverClass = new ServerClass(hotspotInformation);
                serverClass.start();
            }
        }
    }

    private static void showMessage(String message) {
        Log.d(ConnectionUtils.TAG, message);
    }

    private void updateViewsWithBlank() {
        mHotspotStartedExternally = false;

        updateViews(null,
                getString(R.string.text_qrCodeHotspotDisabledHelp),
                null,
                null,
                R.string.text_startHotspot);
    }

    private void updateViewsStartedExternally() {
        mHotspotStartedExternally = true;

        updateViews(null, getString(R.string.text_hotspotStartedExternallyNotice),
                null, null, R.string.butn_stopHotspot);
    }


    private void updateViews(String networkName, String password, int keyManagement) {
        mHotspotStartedExternally = false;

        try {
            JSONObject object = new JSONObject()
                    .put(Keyword.NETWORK_NAME, networkName)
                    .put(Keyword.NETWORK_PASSWORD, password)
                    .put(Keyword.NETWORK_KEYMGMT, keyManagement);

            updateViews(object, getString(R.string.text_qrCodeAvailableHelp), networkName, password, R.string.butn_stopHotspot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateViews(@Nullable JSONObject codeIndex,
                             @Nullable String text1,
                             @Nullable String text2,
                             @Nullable String text3,
                             @StringRes int buttonText) {
        boolean showQRCode = codeIndex != null
                && codeIndex.length() > 0
                && getContext() != null;

        try {

            if (showQRCode) {
                {
                    int networkPin = AppUtils.getUniqueNumber();

                    codeIndex.put(Keyword.NETWORK_PIN, networkPin);

                    AppUtils.getDefaultPreferences(getContext()).edit()
                            .putInt(Keyword.NETWORK_PIN, networkPin)
                            .apply();
                    if (serverClass != null && UIConnectionUtils.isOreoAbove()) {
                        showMessage("HotspotInformation is: " + codeIndex);
                        serverClass.setHotspotInformation(codeIndex);
                    }

                    if (codeIndex.has(Keyword.NETWORK_NAME) && UIConnectionUtils.isOreoAbove()) {
                        ConnectionUtils.getInstance(getContext()).getBluetoothAdapter().setName(codeIndex.getString(Keyword.NETWORK_NAME));
                        showMessage("Bluetooth Name is: " + codeIndex.getString(Keyword.NETWORK_NAME));
                    }
                }


               /* MultiFormatWriter formatWriter = new MultiFormatWriter();
                BitMatrix bitMatrix = formatWriter.encode(codeIndex.toString(), BarcodeFormat.QR_CODE, 400, 400);
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.createBitmap(bitMatrix);*/

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMenu() {
        if (mHelpMenuItem != null)
            mHelpMenuItem.setVisible(getConnectionUtils().getHotspotUtils().getConfiguration() != null
                    && getConnectionUtils().getHotspotUtils().isEnabled());
    }

    private void updateState() {
        boolean isEnabled = getUIConnectionUtils().getConnectionUtils().getHotspotUtils().isEnabled();
        WifiConfiguration wifiConfiguration = getConnectionUtils().getHotspotUtils().getConfiguration();

        showMenu();

        if (!isEnabled) {
            ExtensionsUtils.getLogInfo("hotspot disabled");
            updateViewsWithBlank();
        } else if (getConnectionUtils().getHotspotUtils() instanceof HotspotUtils.HackAPI
                && wifiConfiguration != null) {
            updateViews(wifiConfiguration.SSID, wifiConfiguration.preSharedKey, NetworkUtils.getAllowedKeyManagement(wifiConfiguration));
        } else if (Build.VERSION.SDK_INT >= 26) {
            AppUtils.startForegroundService(getActivity(),
                    new Intent(getActivity(), CommunicationService.class)
                            .setAction(CommunicationService.ACTION_REQUEST_HOTSPOT_STATUS));
            ExtensionsUtils.getLogInfo("hotspot status sending");
        }
    }

    private class StatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkStatusReceiver.WIFI_AP_STATE_CHANGED.equals(intent.getAction())) {
                updateState();
            } else if (ACTION_HOTSPOT_STATUS.equals(intent.getAction())) {
                if (intent.getBooleanExtra(EXTRA_HOTSPOT_ENABLED, false))
                    updateViews(intent.getStringExtra(CommunicationService.EXTRA_HOTSPOT_NAME),
                            intent.getStringExtra(CommunicationService.EXTRA_HOTSPOT_PASSWORD),
                            intent.getIntExtra(CommunicationService.EXTRA_HOTSPOT_KEY_MGMT, 0));
                else if (getConnectionUtils().getHotspotUtils().isEnabled()
                        && !intent.getBooleanExtra(EXTRA_HOTSPOT_DISABLING, false)) {
                    updateViewsStartedExternally();
                }
            }
        }
    }

    private UIConnectionUtils.RequestWatcher mHotspotWatcher = new UIConnectionUtils.RequestWatcher() {
        @Override
        public void onResultReturned(boolean result, boolean shouldWait) {
            mWaitForHotspot = shouldWait;
        }
    };

    private UIConnectionUtils.RequestWatcher mWiFiWatcher = new UIConnectionUtils.RequestWatcher() {
        @Override
        public void onResultReturned(boolean result, boolean shouldWait) {
            mWaitForWiFi = shouldWait;
        }
    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String duration = intent.getStringExtra(Keyword.DATA_TRANSFER_TIME);
            String speed = intent.getStringExtra(Keyword.DATA_TRANSFER_SPEED);
            dataTransferTime.setText(duration);
            dataTransferSpeed.setText(speed);
            //int progress = intent.getIntExtra("Status", -1);
            //progressBar.setProgress(progress);
        }
    };

    @SuppressLint("HandlerLeak")
    public class MyHandler extends Handler {


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == STATE_BLUETOOTH_DISCOVERABLE_REQUESTING) {
                getorUpdateBluetoothDiscoverable();
                this.sendMessageDelayed(this.obtainMessage(STATE_BLUETOOTH_DISCOVERABLE_REQUESTING), 60000);
            }

            /*else if (msg.what == STATE_PROGRESS) {
                // update progress here.
                try {
                    progress = (int) msg.obj;
                    progressBar.setProgress(progress);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/
        }

    }

    private class SendReceive extends Thread {

        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;


        SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    new SenderFragmentImpl().mHandler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    showMessage("SendReceive: " + e);
                    break;
                }
            }
            showMessage("SendReceive: I'm still on.");
        }

        void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private class ServerClass extends Thread {

        private BluetoothServerSocket serverSocket;

        private JSONObject hotspotInformation;

        JSONObject getHotspotInformation() {
            return hotspotInformation;
        }

        void setHotspotInformation(JSONObject hotspotInformation) {
            this.hotspotInformation = hotspotInformation;
        }

        ServerClass(JSONObject hotspotInformations) {
            try {
                serverSocket = ConnectionUtils.getInstance(getContext()).getBluetoothAdapter().listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID);
                hotspotInformation = hotspotInformations;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket socket;

            while (true) {
                try {
                    /*Message message=Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);*/
                    socket = serverSocket.accept();
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage("SendReceive: socket not accepting new port for connection " + e);
                    break;
                    /*Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);*/
                }

                showMessage("HotspotInformation is: " + getHotspotInformation());
                if (sendReceive == null && socket != null && getHotspotInformation() != null) {
                    /*Message message=Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);*/

                    sendReceive = new SendReceive(socket);
                    sendReceive.write(getHotspotInformation().toString().getBytes());
                    sendReceive.start();
                    showMessage("SendReceive: send message to obtain information of hotspot");
                    if (mHandle != null)
                        mHandle.removeMessages(STATE_BLUETOOTH_DISCOVERABLE_REQUESTING);

                    break;
                }
            }

            showMessage("Server: I'm still on.");
        }

    }

}
