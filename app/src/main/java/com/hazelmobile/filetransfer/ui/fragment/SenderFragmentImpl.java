package com.hazelmobile.filetransfer.ui.fragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ShapeDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.genonbeta.android.framework.util.Interrupter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.ResultPoint;
import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.app.Activity;
import com.hazelmobile.filetransfer.bluetooth.BluetoothDataTransferThread;
import com.hazelmobile.filetransfer.bluetooth.ClientThread;
import com.hazelmobile.filetransfer.bluetooth.MyHandler;
import com.hazelmobile.filetransfer.bluetooth.ActionType;
import com.hazelmobile.filetransfer.callback.Callback;
import com.hazelmobile.filetransfer.config.Keyword;
import com.hazelmobile.filetransfer.database.AccessDatabase;
import com.hazelmobile.filetransfer.dialog.SenderWaitingDialog;
import com.hazelmobile.filetransfer.library.RippleBackground;
import com.hazelmobile.filetransfer.model.Bluetooth;
import com.hazelmobile.filetransfer.object.NetworkDevice;
import com.hazelmobile.filetransfer.ui.UIConnectionUtils;
import com.hazelmobile.filetransfer.ui.UITask;
import com.hazelmobile.filetransfer.ui.activity.SenderActivity;
import com.hazelmobile.filetransfer.ui.adapter.NetworkDeviceListAdapter;
import com.hazelmobile.filetransfer.ui.adapter.SenderListAdapter;
import com.hazelmobile.filetransfer.ui.callback.IconSupport;
import com.hazelmobile.filetransfer.ui.callback.NetworkDeviceSelectedListener;
import com.hazelmobile.filetransfer.ui.callback.TitleSupport;
import com.hazelmobile.filetransfer.util.AppUtils;
import com.hazelmobile.filetransfer.util.ConnectionUtils;
import com.hazelmobile.filetransfer.util.ListUtils;
import com.hazelmobile.filetransfer.util.LogUtils;
import com.hazelmobile.filetransfer.util.NetworkDeviceLoader;
import com.hazelmobile.filetransfer.widget.ExtensionsUtils;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.hazelmobile.filetransfer.bluetooth.MyHandler.MSG_TO_SHOW_SCAN_RESULT;
import static com.hazelmobile.filetransfer.ui.activity.PreparationsActivity.REQUEST_PERMISSION_CAMERA;

/*
 * created by: veli
 * modified by: faizi
 * date: 12/04/18 17:21
 */

public class SenderFragmentImpl
        extends com.genonbeta.android.framework.app.Fragment
        implements TitleSupport, UITask, IconSupport, SenderActivity.DeviceSelectionSupport {

    public static final String TAG = "BarcodeConnectFragment";
    public static final String APP_NAME = "com.hazelmobile.filetransfer";
    public static final UUID MY_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803405c9a66");

    private DecoratedBarcodeView mBarcodeView;
    private UIConnectionUtils mConnectionUtils;
    private NetworkDeviceSelectedListener mDeviceSelectedListener;

    private boolean isSocketClosed = false;
    private int btm_margin = 0;
    private boolean isConnected = false;

    private ListView lv_send;
    private ViewGroup user_retry;
    private ImageView user_image;
    private TextView textView, sender_status;
    private BluetoothDataTransferThread bluetoothDataTransferThread;
    private BottomSheetBehavior standardBottomSheetBehavior;
    private ClientThread clientThread;
    private List<Object> mGenericList;
    private SenderListAdapter senderListAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private RippleBackground pulse;
    private SenderWaitingDialog senderWaitingDialog;
    private MyHandler mHandler = new MyHandler();
    private View mView;

    private IntentFilter mIntentFilter = new IntentFilter();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConnectionUtils = new UIConnectionUtils(ConnectionUtils.getInstance(getContext()), this);
        bluetoothAdapter = mConnectionUtils.getConnectionUtils().getBluetoothAdapter();
        mGenericList = new ArrayList<>();

        mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.demo_fragment_impl_sender, container, false);
        mBarcodeView = view.findViewById(R.id.layout_barcode_connect_barcode_view);
        lv_send = view.findViewById(R.id.lv_send);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        pulse = view.findViewById(R.id.content);
        pulse.startRippleAnimation();

        user_image = view.findViewById(R.id.userProfileImage);
        user_retry = view.findViewById(R.id.userProfileImageRetry);
        textView = view.findViewById(R.id.text1);
        sender_status = view.findViewById(R.id.sender_status);
        setProfilePicture();

        LinearLayout standardBottomSheet = view.findViewById(R.id.standardBottomSheet);
        standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet);

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) pulse.getLayoutParams();
        btm_margin = lp.bottomMargin;

        BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                float h = bottomSheet.getHeight();
                float off = h * slideOffset;
                int finalVal = (int) (off * 0.85F);
                AppUtils.setMargins(pulse, 0, 0, 0,
                        finalVal + AppUtils.pxToDp(standardBottomSheetBehavior.getPeekHeight() + btm_margin));
            }
        };

        standardBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);

        updateUI();
        setSnackbarContainer(view.findViewById(R.id.qr_container));
        setSnackbarLength(Snackbar.LENGTH_INDEFINITE);

        final Observer<Object> senderAction = new Observer<Object>() {
            @Override
            public void onChanged(Object action) {

                if (action instanceof JSONObject)
                    connectToHotspot(((JSONObject) action));
                else if (action instanceof ScanResult)
                    connectToHotspot(((ScanResult) action));
                else if (action instanceof ActionType) {
                    if (ActionType.DISCOVERY.equals(action))
                        retryDiscovery();
                    else if (ActionType.CLOSE_DIALOG.equals(action))
                        closeDialog();
                    else if (ActionType.UNKNOWN.equals(action))
                        LogUtils.getWTF(SenderFragmentImpl.class.getSimpleName(),
                                "That's the worst thing a LiveData have...FUCK CODER");
                }

            }
        };

        Callback.getSenderAction().observe(this, senderAction);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null && !isConnected) {
            getContext().registerReceiver(mReceiver, mIntentFilter);
            if (bluetoothAdapter != null && !bluetoothAdapter.isDiscovering() && bluetoothAdapter.isEnabled())
                bluetoothAdapter.startDiscovery();
        }
        // it goes behind the interface so don't need to call it every time.
        //updateState();
        //if (mPreviousScanResult != null)
        //    handleBarcode(mPreviousScanResult);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (getContext() != null && !isConnected) {
                getContext().unregisterReceiver(mReceiver);
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        mBarcodeView.pauseAndWait();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {

            closeDialog();
            Callback.setSenderAction(ActionType.UNKNOWN);
            Callback.getSenderAction().removeObservers(this);
            isThreadAlive = false;
            isSocketClosed = false;
            isConnected = false;
            mHandler.removeHanlderMessages();

            if (bluetoothDataTransferThread != null) {
                bluetoothDataTransferThread.cancel();
                bluetoothDataTransferThread = null;
            }

            if (clientThread != null) {
                clientThread.cancel();
                clientThread = null;
            }

            if (mGenericList != null && mGenericList.size() > 0) {
                mGenericList.clear();
            }

            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();

            /*Set<BluetoothDevice> bluetoothDeviceList = bluetoothAdapter.getBondedDevices();
            if (bluetoothDeviceList.size() > 0) {
                for (BluetoothDevice bluetoothDevice : bluetoothDeviceList) {
                    try {
                        if (bluetoothDevice.getName().contains("TS") || bluetoothDevice.getName().contains("AndroidShare")) {
                            Method m = bluetoothDevice.getClass().getMethod(String.format("%s", "removeBond"), (Class[]) null);
                            m.invoke(bluetoothDevice, (Object[]) null);
                            showMessage("BluetoothDataTransferThread: Removed Device Name is: " + bluetoothDevice);
                        }
                    } catch (Exception e) {
                        showMessage("BluetoothDataTransferThread: Removing has been failed." + e.getMessage());
                    }
                }
            }*/

            bluetoothAdapter.disable();
        } catch (Exception e) {
            ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                    "ClientSocket: onDestroy(): " + e.getMessage());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //inflater.inflate(R.menu.actions_barcode_scanner, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.show_help) {
            assert getActivity() != null;
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.text_scanQRCodeHelp)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        } else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public int getIconRes() {
        return 0/*R.drawable.ic_qrcode_white_24dp*/;
    }

    @NotNull
    @Override
    public CharSequence getTitle(Context context) {
        return context.getString(R.string.text_scanQrCode);
    }

    @Override
    public void updateTaskStarted(Interrupter interrupter) {
        if (isSocketClosed) updateState(true, interrupter);
    }

    @Override
    public void updateTaskStopped() {
        if (isSocketClosed) updateState(false, null);
    }

    private void retryDiscovery() {
        if (getContext() != null) {

            try {
                getContext().unregisterReceiver(mReceiver);
            } catch (IllegalArgumentException e) {
                ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                        "ClientSocket: Unregistering WIFI ADN BLUETOOTH \n" + e.getMessage());
            }

            try {
                getContext().registerReceiver(mReceiver, mIntentFilter);
            } catch (Exception e) {
                ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                        "ClientSocket: Registering WIFI ADN BLUETOOTH \n" + e.getMessage());
            }

        }
    }

    private void bluetoothDiscoveryStatus(boolean enableReceiver) {
        if (getContext() != null) {
            try {
                if (!enableReceiver)
                    getContext().unregisterReceiver(mReceiver);
                else
                    getContext().registerReceiver(mReceiver, mIntentFilter);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeDialog() {
        if (senderWaitingDialog != null && senderWaitingDialog.isShowing()) {
            senderWaitingDialog.dismiss();
            senderWaitingDialog = null;
        }
    }

    private void connectToHotspot(ScanResult scanResult) {
        try {

            if (bluetoothAdapter != null) {
                if (bluetoothAdapter.isDiscovering())
                    bluetoothAdapter.cancelDiscovery();
                mHandler.removeMessages(MSG_TO_SHOW_SCAN_RESULT);
                bluetoothDiscoveryStatus(false);
                Callback.setSenderAction(ActionType.UNKNOWN);
                isConnected = true;
            }

            NetworkDeviceListAdapter.HotspotNetwork hotspotNetwork = new NetworkDeviceListAdapter.HotspotNetwork();
            final int accessPin = 0;
            hotspotNetwork.SSID = scanResult.SSID;
            hotspotNetwork.qrConnection = true;
            hotspotNetwork.password = "";
            hotspotNetwork.keyManagement = 0;
            makeAcquaintance(hotspotNetwork, accessPin);
        } catch (Exception e) {
            showMessage("BluetoothDataTransferThread: Connecting to Open Network " + e.getMessage());
        }
    }

    private void connectToHotspot(final JSONObject hotspotInformation) {

        final DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //updateState();
            }
        };

        try {

            NetworkDeviceListAdapter.HotspotNetwork hotspotNetwork = new NetworkDeviceListAdapter.HotspotNetwork();
            final int accessPin = hotspotInformation.has(Keyword.NETWORK_PIN)
                    ? hotspotInformation.getInt(Keyword.NETWORK_PIN)
                    : -1;

            LogUtils.getLogWarning("Client", String.format("HotspotInformation Pin Value is: %s", accessPin));

            if (hotspotInformation.has(Keyword.NETWORK_NAME)) {
                hotspotNetwork.SSID = hotspotInformation.getString(Keyword.NETWORK_NAME);
                hotspotNetwork.qrConnection = true;
                boolean passProtected = hotspotInformation.has(Keyword.NETWORK_PASSWORD);

                if (passProtected) {
                    hotspotNetwork.password = hotspotInformation.getString(Keyword.NETWORK_PASSWORD);
                    hotspotNetwork.keyManagement = hotspotInformation.getInt(Keyword.NETWORK_KEYMGMT);
                }

                makeAcquaintance(hotspotNetwork, accessPin);
            } else if (hotspotInformation.has(Keyword.NETWORK_ADDRESS_IP)) {
                final String bssid = hotspotInformation.getString(Keyword.NETWORK_ADDRESS_BSSID);
                final String ipAddress = hotspotInformation.getString(Keyword.NETWORK_ADDRESS_IP);

                WifiInfo wifiInfo = mConnectionUtils.getConnectionUtils().getWifiManager().getConnectionInfo();

                if (wifiInfo != null
                        && wifiInfo.getBSSID() != null
                        && wifiInfo.getBSSID().equals(bssid))
                    makeAcquaintance(ipAddress, accessPin);
                else {
                    assert getActivity() != null;
                    new AlertDialog.Builder(getActivity())
                            .setMessage(R.string.mesg_errorNotSameNetwork)
                            .setNegativeButton(R.string.butn_close, null)
                            .setPositiveButton(R.string.butn_skip, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    makeAcquaintance(ipAddress, accessPin);
                                }
                            })
                            .setOnDismissListener(dismissListener)
                            .show();
                }
            } else {
                throw new JSONException("Failed to attain known variables.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getOrUpdateScanResult() {

        // please note google restricts startscan() call to every 2 minute for api 28 and above.
        if (getContext() != null) {

            // FOR DEVICES HAVING OREO BELOW
            WifiManager wifiManager = ConnectionUtils.getInstance(getContext()).getWifiManager();
            if (wifiManager.isWifiEnabled()) {
                boolean success = wifiManager.startScan();
                if (success) {
                    //showMessage("BluetoothDataTransferThread: Wifi Scan results are " + wifiManager.getScanResults());
                    mGenericList.addAll(ListUtils.filterWithNoPassword(wifiManager.getScanResults()));
                    //showMessage("mScanResultList: GENERIC List Results after Duplicates removed   " + mGenericList);
                    senderListAdapter = new SenderListAdapter(getContext(), mGenericList,
                            ((SenderActivity) Objects.requireNonNull(getActivity())));
                    lv_send.setAdapter(senderListAdapter);
                }
                lv_send.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Object object = mGenericList.get(position);

                        if (isConnected)
                            retryConnection();

                        openDialog(object);

                        if (object instanceof ScanResult)
                            connectToHotspot(((ScanResult) object));
                        else if (object instanceof Bluetooth) {
                            if (bluetoothAdapter != null) {
                                if (bluetoothAdapter.isDiscovering())
                                    bluetoothAdapter.cancelDiscovery();
                                mHandler.removeMessages(MSG_TO_SHOW_SCAN_RESULT);
                                bluetoothDiscoveryStatus(false);

                                try {
                                    Set<BluetoothDevice> bluetoothDeviceList = bluetoothAdapter.getBondedDevices();
                                    if (bluetoothDeviceList.size() > 0) {
                                        for (BluetoothDevice bluetoothDevice : bluetoothDeviceList) {
                                            try {
                                                if (bluetoothDevice.getAddress().equalsIgnoreCase(((Bluetooth) object).getDevice().getAddress())) {
                                                    BluetoothDevice bondedDevice = bluetoothAdapter.getRemoteDevice(bluetoothDevice.getAddress());
                                                    clientThread = new ClientThread(bondedDevice, bluetoothAdapter, mView, mHandler);
                                                    clientThread.start();
                                                    isConnected = true;
                                                    return;
                                                }
                                            } catch (Exception e) {
                                                showMessage("ClientThread(): Reconnect has been failed." + e.getMessage());
                                            }
                                        }
                                    }
                                } catch (Exception exp) {
                                    ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(), " " + exp.getMessage());
                                }

                                clientThread = new ClientThread(((Bluetooth) object).getDevice(), bluetoothAdapter, mView, mHandler);
                                clientThread.start();
                                isConnected = true;
                            }
                        }

                    }
                });
            } else {
                ConnectionUtils.getInstance(getContext()).openWifi();
            }

            // FOR DEVICES HAVING OREO ABOVE
            ConnectionUtils.getInstance(getContext()).openBluetooth();
        }
    }

    private void getWifiScanResults(WifiManager wifiManager) {

        if (mGenericList.size() > 0) {

            try {
                for (Object object : mGenericList) {
                    try {
                        if (object instanceof ScanResult)
                            mGenericList.remove(object);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                mGenericList.clear();
            }

        }

        showMessage("BluetoothDataTransferThread: Above API Level 23 Wifi Scan results are " + wifiManager.getScanResults());
        mGenericList.addAll(ListUtils.filterWithNoPassword(wifiManager.getScanResults()));
        //showMessage("mScanResultList: GENERIC List Results after Duplicates removed   " + mGenericList);

        if (senderListAdapter != null) {
            senderListAdapter = null;
            senderListAdapter = new SenderListAdapter(getContext(), mGenericList,
                    ((SenderActivity) Objects.requireNonNull(getActivity())));
            lv_send.setAdapter(senderListAdapter);
        } else {
            senderListAdapter = new SenderListAdapter(getContext(), mGenericList,
                    ((SenderActivity) Objects.requireNonNull(getActivity())));
            lv_send.setAdapter(senderListAdapter);
        }

        if (standardBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HALF_EXPANDED
                && senderListAdapter != null
                && mGenericList.size() > 0
                && !isSocketClosed) {
            //user_retry.setVisibility(View.VISIBLE);
            standardBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }
    }

    private void makeAcquaintance(Object object, int accessPin) {
        mConnectionUtils.makeAcquaintance((Activity) Objects.requireNonNull
                (getActivity()), SenderFragmentImpl.this, object, accessPin, mRegisteredListener);
    }

    private void openDialog(Object object) {
        if (!Objects.requireNonNull(getActivity()).isFinishing()) {
            senderWaitingDialog = new SenderWaitingDialog
                    ((Activity) Objects.requireNonNull(getActivity()), object);
            Callback.setDialogInfo(Objects.requireNonNull(getContext())
                    .getString(R.string.mesg_waiting));
            senderWaitingDialog.show();

            senderWaitingDialog.setCanceledOnTouchOutside(false);
        }
    }

    // with qr try connection
    public void retryConnection() {

        try {

            closeDialog();

            if (bluetoothDataTransferThread != null) {
                bluetoothDataTransferThread.cancel();
                bluetoothDataTransferThread = null;
            }

            if (clientThread != null) {
                clientThread.cancel();
                clientThread = null;
            }

            if (mGenericList != null && mGenericList.size() > 0)
                mGenericList.clear();

            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();

            standardBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            ExtensionsUtils.getLog_D(ExtensionsUtils.getBLUETOOTH_TAG(),
                    "ClientSocket: SHEET_STATE " + standardBottomSheetBehavior.getState());

        } catch (Exception e) {
            ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                    "ClientSocket: onRetry(): " + e.getMessage());
        }

    }

    public void setDeviceSelectedListener(NetworkDeviceSelectedListener listener) {
        mDeviceSelectedListener = listener;
    }

    private void setProfilePicture() {
        NetworkDevice localDevice = AppUtils.getLocalDevice(getActivity());
        textView.setText(localDevice.nickname);
        ((SenderActivity) Objects.requireNonNull(getActivity())).loadProfilePictureInto(localDevice.nickname, user_image);
        int color = AppUtils.getDefaultPreferences(getActivity()).getInt("device_name_color", -1);

        if (user_image.getDrawable() instanceof ShapeDrawable && color != -1) {
            ShapeDrawable shapeDrawable = (ShapeDrawable) user_image.getDrawable();
            shapeDrawable.getPaint().setColor(color);
        } else {
            user_image.setBackgroundResource(R.drawable.background_user_icon_default);
        }
    }

    public static void showMessage(String message) {
        Log.d(ConnectionUtils.TAG, "\n" + message + "\n");
    }

    // #retry
    private void updateUI() {
        mBarcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                try {
                    openDialog("");
                    connectToHotspot(new JSONObject(result.getResult().getText()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
            }
        });
        getOrUpdateScanResult();
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TO_SHOW_SCAN_RESULT), 12000);
        user_retry.setOnClickListener(
                v -> {
                    if (!isSocketClosed) {
                        //retryConnection();
                        isSocketClosed = true;
                        bluetoothDiscoveryStatus(false);
                        standardBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        bluetoothAdapter.disable();
                        updateState();
                        return;
                    }
                    isSocketClosed = false;
                    bluetoothAdapter.enable();
                    bluetoothDiscoveryStatus(true);
                    user_image.setVisibility(View.VISIBLE);
                    updateState();
                });
    }

    private void updateState(boolean isConnecting, final Interrupter interrupter) {
        if (!isAdded()) {
            mBarcodeView.pauseAndWait();
            return;
        }

        if (isConnecting) {
            // Keep showing barcode view
            mBarcodeView.pauseAndWait();
            //setConductItemsShowing(false);
        } else {
            mBarcodeView.resume();
            updateState();
        }

        //mTaskContainer.setVisibility(!isConnecting ? View.VISIBLE : View.GONE);

        /*mTaskInterruptButton.setOnClickListener(isConnecting ? new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interrupter.interrupt();
            }
        } : null);*/
    }

    private void updateState() {

        if (!isAdded())
            return;

        assert getContext() != null;
        final boolean hasCameraPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        if (!hasCameraPermission) {
            mBarcodeView.pauseAndWait();
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                    new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
        } else {
            if (isSocketClosed) {
                closeDialog();
                user_image.setVisibility(View.GONE);
                mBarcodeView.resume();
                //mConductText.setText(R.string.text_scanQRCodeHelp);
                //createSnackbar(R.string.text_send_status).show();
            }
        }
        if (isSocketClosed) {
            if (pulse.isRippleAnimationRunning())
                pulse.stopRippleAnimation();
            mBarcodeView.setVisibility(hasCameraPermission ? View.VISIBLE : View.GONE);
            user_image.setVisibility(View.GONE);
        } else {
            pulse.startRippleAnimation();
            mBarcodeView.pauseAndWait();
            user_image.setVisibility(View.VISIBLE);
            mBarcodeView.setVisibility(hasCameraPermission ? View.GONE : View.VISIBLE);
        }
    }

    private UIConnectionUtils.RequestWatcher mPermissionWatcher = new UIConnectionUtils.RequestWatcher() {
        @Override
        public void onResultReturned(boolean result, boolean shouldWait) {
            if (isResumed()) // isResumed
                updateState();
            else {
                mBarcodeView.pauseAndWait();
            }

            // We don't want to keep this when the result is ok
            // or not asked to wait
            //if (!shouldWait || result)
            //    mPreviousScanResult = null;
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                if (!mConnectionUtils.getConnectionUtils().isLocationServiceEnabled()) {
                    sender_status.setText(String.format("%s", "Location is disabled, Kindly open it to start the Process"));
                } else {
                    sender_status.setText(R.string.text_send_status);
                }
            }

            String action = intent.getAction();
            int state;
            WifiManager wifiManager = ConnectionUtils.getInstance(getContext()).getWifiManager();

            // When discovery finds a device
            assert action != null;
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:

                    state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_ON) {
                        bluetoothAdapter.startDiscovery();
                        sender_status.setText(R.string.text_send_status);
                    }
                    if (state == BluetoothAdapter.STATE_OFF)
                        sender_status.setText(String.format("%s", "Bluetooth is disabled, Kindly open it to start the Process"));
                    break;
                case BluetoothDevice.ACTION_FOUND:

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //showMessage("Others device name is " + device.getName() + " " + device.getAddress());
                    if (device.getName() != null &&
                            (device.getName().startsWith("TS") ||
                                    device.getName().startsWith("AndroidShare"))) {

                        showMessage("Tshot device name is " + device.getName());
                        ExtensionsUtils.getLog_D(ExtensionsUtils.getBLUETOOTH_TAG(),
                                "Tshot device name is " + device.getName());
                        for (Object device1 : mGenericList) {
                            if (device1 instanceof Bluetooth &&
                                    ((Bluetooth) device1).getDevice().getAddress() != null &&
                                    device.getAddress().equals(((Bluetooth) device1).getDevice().getAddress()) &&
                                    device.getName().equals(((Bluetooth) device1).getDevice().getName())) {
                                if (standardBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HALF_EXPANDED && !isSocketClosed) {
                                    standardBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                                }
                                return;
                            }
                        }
                        mGenericList.add(new Bluetooth(device, device.getName()
                                + "\n" + device.getAddress()
                        ));
                        if (standardBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HALF_EXPANDED && !isSocketClosed)
                            standardBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                        if (senderListAdapter != null)
                            senderListAdapter.notifyDataSetChanged();
                        else {
                            senderListAdapter = new SenderListAdapter(getContext(), mGenericList,
                                    ((SenderActivity) Objects.requireNonNull(getActivity())));
                            lv_send.setAdapter(senderListAdapter);
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:

                    //showMessage("Bluetooth discovery finished");
                    ConnectionUtils.getInstance(context).getBluetoothAdapter().startDiscovery();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:

                    //showMessage("Bluetooth discovery started");
                    break;

                case WifiManager.WIFI_STATE_CHANGED_ACTION:

                    state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                    ExtensionsUtils.getLog_W(ExtensionsUtils.getTHREAD_TAG(),
                            String.format("wifi state is monitored: %s", state));

                    if (state == WifiManager.WIFI_STATE_ENABLED) {
                        wifiManager.startScan();
                        sender_status.setText(R.string.text_send_status);
                    }
                    if (state == WifiManager.WIFI_STATE_DISABLED)
                        sender_status.setText(String.format("%s", "Wifi is disabled, Kindly open it to start the Process"));

                    break;

                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        boolean success = intent.getBooleanExtra(
                                WifiManager.EXTRA_RESULTS_UPDATED, false);
                        if (success)
                            getWifiScanResults(wifiManager);
                        else
                            getWifiScanResults(wifiManager);
                    } else
                        getWifiScanResults(wifiManager);
                    break;

                default:
                    break;
            }

        }
    };

    private NetworkDeviceLoader.OnDeviceRegisteredListener mRegisteredListener = new NetworkDeviceLoader.OnDeviceRegisteredListener() {
        @Override
        public void onDeviceRegistered(AccessDatabase database, final NetworkDevice device, final NetworkDevice.Connection connection) {
            if (mDeviceSelectedListener != null)
                mDeviceSelectedListener.onNetworkDeviceSelected(device, connection);
        }
    };

}
