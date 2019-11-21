package com.hazelmobile.filetransfer.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.genonbeta.android.framework.util.Interrupter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.hazelmobile.filetransfer.BluetoothConnector;
import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.app.Activity;
import com.hazelmobile.filetransfer.database.AccessDatabase;
import com.hazelmobile.filetransfer.dialog.SenderWaitingDialog;
import com.hazelmobile.filetransfer.library.RippleBackground;
import com.hazelmobile.filetransfer.model.Bluetooth;
import com.hazelmobile.filetransfer.object.NetworkDevice;
import com.hazelmobile.filetransfer.pictures.AppUtils;
import com.hazelmobile.filetransfer.pictures.Keyword;
import com.hazelmobile.filetransfer.ui.UIConnectionUtils;
import com.hazelmobile.filetransfer.ui.UITask;
import com.hazelmobile.filetransfer.ui.activity.DemoSenderActivity;
import com.hazelmobile.filetransfer.ui.activity.SenderActivity;
import com.hazelmobile.filetransfer.ui.adapter.NetworkDeviceListAdapter;
import com.hazelmobile.filetransfer.ui.adapter.SenderListAdapter;
import com.hazelmobile.filetransfer.ui.callback.IconSupport;
import com.hazelmobile.filetransfer.ui.callback.NetworkDeviceSelectedListener;
import com.hazelmobile.filetransfer.ui.callback.TitleSupport;
import com.hazelmobile.filetransfer.util.ConnectionUtils;
import com.hazelmobile.filetransfer.util.ListUtils;
import com.hazelmobile.filetransfer.util.NetworkDeviceLoader;
import com.hazelmobile.filetransfer.widget.ExtensionsUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/*
 * created by: veli
 * modified by: faizi
 * date: 12/04/18 17:21
 */

public class DemoSenderFragmentImpl
        extends com.genonbeta.android.framework.app.Fragment
        implements TitleSupport, UITask, IconSupport, SenderActivity.DeviceSelectionSupport {

    public static final String TAG = "BarcodeConnectFragment";
    private static final int MSG_TO_FILE_SENDER_UI = 0X88;
    private static final int MSG_TO_SHOW_SCAN_RESULT = 0X99;
    private static final int STATE_LISTENING = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_CONNECTED = 3;
    private static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    static final String APP_NAME = "BTChat";
    //private ImageView retryButton;
    public static final UUID MY_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803405c9a66");

    //private DecoratedBarcodeView mBarcodeView;
    private UIConnectionUtils mConnectionUtils;
    private ViewGroup mConductContainer;
    private TextView mConductText, status;
    private ImageView mConductImage;
    private ImageView mTextModeIndicator;
    private Button mConductButton;
    private Button mTaskInterruptButton;
    private View mTaskContainer;
    private IntentFilter mIntentFilter = new IntentFilter();
    private IntentFilter buletoothIntentFilter = new IntentFilter();
    private IntentFilter wifiIntentFilter = new IntentFilter();
    private NetworkDeviceSelectedListener mDeviceSelectedListener;
    private boolean mPermissionRequestedCamera = false;
    private boolean mPermissionRequestedLocation = false;
    private boolean mShowAsText = false;

    private ListView lv_send;
    private ImageView user_image, user_retry;
    private TextView textView;
    private SendReceive sendReceive;
    private BottomSheetBehavior standardBottomSheetBehavior;
    private ClientClass clientClass;
    private List<Object> mGenericList;
    private SenderListAdapter senderListAdapter;
    private TextView sheetText;
    private RippleBackground pulse;
    private int btm_margin = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConnectionUtils = new UIConnectionUtils(ConnectionUtils.getInstance(getContext()), this);

        mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);


        buletoothIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        buletoothIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        buletoothIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        buletoothIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {*/
        wifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
        wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);


        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.demo_fragment_impl_sender, container, false);

        //mConductContainer = view.findViewById(R.id.layout_barcode_connect_conduct_container);
        //mTextModeIndicator = view.findViewById(R.id.layout_barcode_connect_mode_text_indicator);
        //mConductButton = view.findViewById(R.id.layout_barcode_connect_conduct_button);
        //mBarcodeView = view.findViewById(R.id.layout_barcode_connect_barcode_view);
        //mConductText = view.findViewById(R.id.layout_barcode_connect_conduct_text);
        //mConductImage = view.findViewById(R.id.layout_barcode_connect_conduct_image);
        //mTaskContainer = view.findViewById(R.id.container_task);
        //mTaskInterruptButton = view.findViewById(R.id.task_interrupter_button);
        //lvb_result = view.findViewById(R.id.lvb_result);

        lv_send = view.findViewById(R.id.lv_send);
        status = view.findViewById(R.id.status);
        mGenericList = new ArrayList<>();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pulse = view.findViewById(R.id.content);
        pulse.startRippleAnimation();

        user_image = view.findViewById(R.id.userProfileImage);
        user_retry = view.findViewById(R.id.userProfileImageRetry);
        textView = view.findViewById(R.id.text1);
        setProfilePicture();

        LinearLayout standardBottomSheet = view.findViewById(R.id.standardBottomSheet);
        standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet);

        sheetText = view.findViewById(R.id.sheetText);

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) pulse.getLayoutParams();
        btm_margin = lp.bottomMargin;

        /*sheetText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (standardBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    standardBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    sheetText.setText("Close sheet");
                } else {
                    standardBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                    sheetText.setText("Expand sheet");
                }
            }
        });*/

        BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        sheetText.setText("State Hidden");
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        sheetText.setText("State Half Expanded");
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        sheetText.setText("State Collapsed");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        sheetText.setText("State Dragging");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        sheetText.setText("State Expanded");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        sheetText.setText("State Settling");
                        break;
                    default:
                        sheetText.setText("State Default");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                float h = bottomSheet.getHeight();
                float off = h * slideOffset;
                int finalVal = (int) (off * 0.85F);
                Log.e(
                        getClass().toString(), "$slideOffset $off $finalVal $h ${standardBottomSheetBehavior.peekHeight}"
                );
                /*  map_container.updateLayoutParams {
                      height = h - finalVal - standardBottomSheetBehavior.peekHeight
                  }
  */
                //accelerometer_view.alpha = 1 - slideOffset;
//                map_container.translationY =-off
                setMargins(pulse, 0, 0, 0, finalVal + pxToDp(standardBottomSheetBehavior.getPeekHeight() + btm_margin));


                // map_container.setPadding(0,0,0, pxToDp(off.toInt()))

/*                when (standardBottomSheetBehavior.state) {
                    BottomSheetBehavior.STATE_EXPANDED -> "STATE_EXPANDED" + bottomSheet.layoutParams.height
                    BottomSheetBehavior.STATE_COLLAPSED -> "STATE_COLLAPSED" + bottomSheet.layoutParams.height
                    BottomSheetBehavior.STATE_DRAGGING -> "STATE_DRAGGING" + bottomSheet.layoutParams.height
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> "STATE_HALF_EXPANDED" + bottomSheet.layoutParams.height
                    BottomSheetBehavior.STATE_HIDDEN -> "STATE_HIDDEN" + bottomSheet.layoutParams.height
                    BottomSheetBehavior.STATE_SETTLING -> "STATE_SETTLING" + bottomSheet.layoutParams.height}*/

                /*val fraction = (slideOffset + 1f) / 2f
                val color = ArgbEvaluatorCompat.getInstance().evaluate(fraction, startColor, endColor);
                slideView.setBackgroundColor(color)*/
            }
        };

        standardBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);
        //standardBottomSheetBehavior.setSaveFlags(BottomSheetBehavior.SAVE_ALL);

        updateUI();
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    private int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            getContext().registerReceiver(mReceiver, mIntentFilter);
            getContext().registerReceiver(bReceiver, buletoothIntentFilter);
            getContext().registerReceiver(wReceiver, wifiIntentFilter);
            BluetoothAdapter bluetoothAdapter = ConnectionUtils.getInstance(getContext()).getBluetoothAdapter();
            if (!bluetoothAdapter.isDiscovering() && bluetoothAdapter.isEnabled()) {
                ConnectionUtils.getInstance(getContext()).getBluetoothAdapter().startDiscovery();
            }
        }
        //updateState();
        //if (mPreviousScanResult != null)
        //    handleBarcode(mPreviousScanResult);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null) {
            getContext().unregisterReceiver(mReceiver);
            getContext().unregisterReceiver(bReceiver);
            getContext().unregisterReceiver(wReceiver);
            if (ConnectionUtils.getInstance(getContext()).getBluetoothAdapter().isDiscovering()) {
                ConnectionUtils.getInstance(getContext()).getBluetoothAdapter().cancelDiscovery();
            }
        }
        //mBarcodeView.pauseAndWait();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {

            removeHanlderMessages();

            if (sendReceive != null && sendReceive.bluetoothSocket != null)
                sendReceive.bluetoothSocket.close();
            if (sendReceive != null) {
                sendReceive.interrupt();
                sendReceive = null;
            }
            if (clientClass != null && clientClass.socket != null) clientClass.socket.close();
            if (clientClass != null) {
                clientClass.interrupt();
                clientClass = null;
            }
            if (mGenericList != null && mGenericList.size() > 0) {
                mGenericList.clear();
            }
            ConnectionUtils connectionUtils = ConnectionUtils.getInstance(getContext());
            if (connectionUtils.getBluetoothAdapter().isDiscovering())
                connectionUtils.getBluetoothAdapter().cancelDiscovery();

            Set<BluetoothDevice> bluetoothDeviceList = connectionUtils.getBluetoothAdapter().getBondedDevices();
            if (bluetoothDeviceList.size() > 0) {
                for (BluetoothDevice bluetoothDevice : bluetoothDeviceList) {
                    try {
                        if (bluetoothDevice.getName().contains("TS") || bluetoothDevice.getName().contains("AndroidShare")) {
                            Method m = bluetoothDevice.getClass().getMethod("removeBond", (Class[]) null);
                            m.invoke(bluetoothDevice, (Object[]) null);
                            showMessage("SendReceive: Removed Device Name is: " + bluetoothDevice);
                        }
                    } catch (Exception e) {
                        showMessage("SendReceive: Removing has been failed." + e.getMessage());
                    }
                }
            }

            connectionUtils.getBluetoothAdapter().disable();
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("onDestroy(): " + e);
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
        } /*else if (id == R.id.change_mode) {
            mShowAsText = !mShowAsText;
            mTextModeIndicator.setVisibility(mShowAsText ? View.VISIBLE : View.GONE);
            item.setIcon(mShowAsText ? R.drawable.ic_qrcode_white_24dp : R.drawable.ic_short_text_white_24dp);

            createSnackbar(mShowAsText ? R.string.mesg_qrScannerTextMode : R.string.mesg_qrScannerDefaultMode)
                    .show();

            updateState();
        }*/ else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions.length > 0)
            for (int permIterator = 0; permIterator < permissions.length; permIterator++) {
                if (Manifest.permission.CAMERA.equals(permissions[permIterator]) &&
                        grantResults[permIterator] == PackageManager.PERMISSION_GRANTED) {
                    updateState();
                    mPermissionRequestedCamera = false;
                }
            }
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
        updateState(true, interrupter);
    }

    @Override
    public void updateTaskStopped() {
        updateState(false, null);
    }

    private void cancelDiscovery() {
        if (getContext() != null) {
            try {
                getContext().unregisterReceiver(wReceiver);
                getContext().unregisterReceiver(bReceiver);
                getContext().registerReceiver(wReceiver, wifiIntentFilter);
                getContext().registerReceiver(bReceiver, buletoothIntentFilter);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectToHotspot(ScanResult scanResult) {
        try {
            NetworkDeviceListAdapter.HotspotNetwork hotspotNetwork = new NetworkDeviceListAdapter.HotspotNetwork();
            final int accessPin = 0;
            hotspotNetwork.SSID = scanResult.SSID;
            hotspotNetwork.qrConnection = true;
            hotspotNetwork.password = "";
            hotspotNetwork.keyManagement = 0;
            makeAcquaintance(hotspotNetwork, accessPin);
        } catch (Exception e) {
            showMessage("SendReceive: Connecting to Open Network " + e.getMessage());
        }
    }

    private void connectToHotspot(final JSONObject hotspotInformation) {

        final DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateState();
            }
        };

        try {

            NetworkDeviceListAdapter.HotspotNetwork hotspotNetwork = new NetworkDeviceListAdapter.HotspotNetwork();
            final int accessPin = hotspotInformation.has(Keyword.NETWORK_PIN)
                    ? hotspotInformation.getInt(Keyword.NETWORK_PIN)
                    : -1;

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
            assert getActivity() != null;
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.text_unrecognizedQrCode)
                    .setMessage(hotspotInformation.toString())
                    .setNegativeButton(R.string.butn_close, null)
                    .setPositiveButton(R.string.butn_show, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /*TextStreamObject textObject = new TextStreamObject(
                                    AppUtils.getUniqueNumber(), hotspotInformation.toString());
                            AppUtils.getDatabase(getContext()).publish(textObject);

                            Toast.makeText(getContext(), R.string.mesg_textStreamSaved, Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(getContext(), TextEditorActivity.class)
                                    .setAction(TextEditorActivity.ACTION_EDIT_TEXT)
                                    .putExtra(TextEditorActivity.EXTRA_CLIPBOARD_ID, textObject.id));*/
                        }
                    })
                    .setNeutralButton(R.string.butn_copyToClipboard, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (getContext() != null) {
                                ClipboardManager manager = (ClipboardManager) getContext().getSystemService(
                                        Service.CLIPBOARD_SERVICE);
                                manager.setPrimaryClip(ClipData.newPlainText("copiedText", hotspotInformation.toString()));
                                Toast.makeText(getContext(), R.string.mesg_textCopiedToClipboard, Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setOnDismissListener(dismissListener)
                    .show();
        }
    }

    private void getOrUpdateWifiScanResult() {

        // google restricts startscan() call to every 2 minute for api 28 and above.
        if (getContext() != null) {
            // FOR DEVICES HAVING OREO BELOW
            WifiManager wifiManager = ConnectionUtils.getInstance(getContext()).getWifiManager();
            if (wifiManager.isWifiEnabled()) {
                boolean success = wifiManager.startScan();
                if (success) {
                    //showMessage("SendReceive: Wifi Scan results are " + wifiManager.getScanResults());
                    mGenericList.addAll(ListUtils.filterWithNoPassword(wifiManager.getScanResults()));
                    showMessage("mScanResultList: GENERIC List Results after Duplicates removed   " + mGenericList);
                    senderListAdapter = new SenderListAdapter(getContext(), mGenericList, ((DemoSenderActivity) getActivity()));
                    lv_send.setAdapter(senderListAdapter);
                }
                lv_send.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Object object = mGenericList.get(position);

                        new SenderWaitingDialog((Activity) Objects.requireNonNull(getActivity()), object).show();

                        if (object instanceof ScanResult)
                            connectToHotspot(((ScanResult) object));
                        else if (object instanceof Bluetooth) {
                            clientClass = new ClientClass(((Bluetooth) object).getDevice());
                            clientClass.start();
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
                    if (object instanceof ScanResult) {
                        mGenericList.remove(object);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        //showMessage("SendReceive: Above API Level 23 Wifi Scan results are " + wifiManager.getScanResults());

        mGenericList.addAll(ListUtils.filterWithNoPassword(wifiManager.getScanResults()));

        showMessage("mScanResultList: GENERIC List Results after Duplicates removed   " + mGenericList);

        if (senderListAdapter != null) {
            senderListAdapter = null;
            senderListAdapter = new SenderListAdapter(getContext(), mGenericList,
                    ((DemoSenderActivity) Objects.requireNonNull(getActivity())));
            lv_send.setAdapter(senderListAdapter);
        }

        if (standardBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HALF_EXPANDED &&
                senderListAdapter != null &&
                mGenericList.size() > 0) {
            //user_retry.setVisibility(View.VISIBLE);
            standardBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }
    }

    private void makeAcquaintance(Object object, int accessPin) {
        mConnectionUtils.makeAcquaintance(Objects.requireNonNull
                (getActivity()), DemoSenderFragmentImpl.this, object, accessPin, mRegisteredListener);
    }

    private void removeHanlderMessages() {
        mHandler.removeMessages(MSG_TO_SHOW_SCAN_RESULT);
        mHandler.removeMessages(STATE_CONNECTED);
        mHandler.removeMessages(STATE_CONNECTING);
        mHandler.removeMessages(STATE_CONNECTION_FAILED);
        mHandler.removeMessages(STATE_MESSAGE_RECEIVED);
        mHandler.removeMessages(STATE_LISTENING);

        mHandler = null;
    }

    private void setConductItemsShowing(boolean showing) {
        mConductContainer.setVisibility(showing ? View.VISIBLE : View.GONE);
    }

    public void setDeviceSelectedListener(NetworkDeviceSelectedListener listener) {
        mDeviceSelectedListener = listener;
    }

    private void setProfilePicture() {
        NetworkDevice localDevice = AppUtils.getLocalDevice(getActivity());
        textView.setText(localDevice.nickname);
        ((DemoSenderActivity) Objects.requireNonNull(getActivity())).loadProfilePictureInto(localDevice.nickname, user_image);
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

    private void updateUI() {
        getOrUpdateWifiScanResult();
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TO_SHOW_SCAN_RESULT), 12000);
    }

    private void updateState(boolean isConnecting, final Interrupter interrupter) {
        /*if (!isAdded()) {
            mBarcodeView.pauseAndWait();
            return;
        }

        if (isConnecting) {
            // Keep showing barcode view
            mBarcodeView.pauseAndWait();
            setConductItemsShowing(false);
        } else {
            mBarcodeView.resume();
            updateState();
        }*/

        //mTaskContainer.setVisibility(!isConnecting ? View.VISIBLE : View.GONE);

        /*mTaskInterruptButton.setOnClickListener(isConnecting ? new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interrupter.interrupt();
            }
        } : null);*/
    }

    private void updateState() {
    }/*{
        if (!isAdded())
            return;

        final boolean wifiEnabled = mConnectionUtils.getConnectionUtils().getWifiManager().isWifiEnabled();
        assert getContext() != null;
        final boolean hasCameraPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        final boolean hasLocationPermission = Build.VERSION.SDK_INT < 26 // With Android Oreo, to gather Wi-Fi information, minimal access to location is needed
                || mConnectionUtils.getConnectionUtils().canAccessLocation();
        final boolean state = (wifiEnabled || mShowAsText) && hasCameraPermission && hasLocationPermission;

        if (!state) {
            //mBarcodeView.pauseAndWait();

            if (!hasCameraPermission) {
                //mConductImage.setImageResource(R.drawable.ic_camera_white_144dp);
                mConductText.setText(R.string.text_cameraPermissionRequired);
                mConductButton.setText(R.string.butn_ask);

                mConductButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getActivity() != null)
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
                    }
                });

                if (!mPermissionRequestedCamera && getActivity() != null)
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);

                mPermissionRequestedCamera = true;
            } else if (!hasLocationPermission) {
                //mConductImage.setImageResource(R.drawable.ic_perm_device_information_white_144dp);
                mConductText.setText(R.string.mesg_locationPermissionRequiredAny);
                mConductButton.setText(R.string.butn_enable);

                mConductButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mConnectionUtils.validateLocationPermission(getActivity(), REQUEST_PERMISSION_LOCATION, mPermissionWatcher);
                    }
                });

                if (!mPermissionRequestedLocation && getActivity() != null)
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_CAMERA);

                mPermissionRequestedLocation = true;
            } else {
                ///mConductImage.setImageResource(R.drawable.ic_signal_wifi_off_white_144dp);
                mConductText.setText(R.string.text_scanQRWifiRequired);
                mConductButton.setText(R.string.butn_enable);

                mConductButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mConnectionUtils.turnOnWiFi(getActivity(), REQUEST_TURN_WIFI_ON, mPermissionWatcher);
                    }
                });
            }
        } else {
            //mBarcodeView.resume();
            mConductText.setText(R.string.text_scanQRCodeHelp);
        }

        setConductItemsShowing(!state);
        //mBarcodeView.setVisibility(state ? View.VISIBLE : View.GONE);
    }*/

    public static double getRandomDoubleBetweenRange(double min, double max) {
        return (Math.random() * ((max - min) + 1)) + min;
    }

    // #bReceiver
    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            assert action != null;
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:

                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_ON) {
                        //showMessage("ACTION_STATE_CHANGED: STATE_ON");
                        ConnectionUtils.getInstance(getContext()).getBluetoothAdapter().startDiscovery();
                        //showMessage("Bluetooth Discovery Started.....");
                    }
                    break;
                case BluetoothDevice.ACTION_FOUND:

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //showMessage("Others device name is " + device.getName() + " " + device.getAddress());
                    if (device.getName() != null &&
                            (device.getName().startsWith("TS") ||
                                    device.getName().startsWith("AndroidShare"))) {

                        showMessage("Tshot device name is " + device.getName());
                        for (Object device1 : mGenericList) {
                            if (device1 instanceof Bluetooth &&
                                    ((Bluetooth) device1).getDevice().getAddress() != null &&
                                    device.getAddress().equals(((Bluetooth) device1).getDevice().getAddress())) {
                                if (standardBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HALF_EXPANDED) {
                                    standardBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                                }
                                return;
                            }
                        }
                        mGenericList.add(new Bluetooth(device, device.getName()
                                + "\n" + device.getAddress()
                        ));
                        if (standardBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HALF_EXPANDED) {
                            standardBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                        }
                        if (senderListAdapter != null)
                            senderListAdapter.notifyDataSetChanged();
                        else {
                            senderListAdapter = new SenderListAdapter(getContext(), mGenericList,
                                    ((DemoSenderActivity) Objects.requireNonNull(getActivity())));
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
                default:
                    break;
            }
        }
    };

    private UIConnectionUtils.RequestWatcher mPermissionWatcher = new UIConnectionUtils.RequestWatcher() {
        @Override
        public void onResultReturned(boolean result, boolean shouldWait) {
            if (isResumed()) // isResumed
                updateState();
            else {
                //mBarcodeView.pauseAndWait();
            }

            // We don't want to keep this when the result is ok
            // or not asked to wait
            //if (!shouldWait || result)
            //    mPreviousScanResult = null;
        }
    };

    // #wReceiver
    private final BroadcastReceiver wReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            WifiManager wifiManager = ConnectionUtils.getInstance(getContext()).getWifiManager();
            assert action != null;
            switch (action) {
                case WifiManager.WIFI_STATE_CHANGED_ACTION:

                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiManager.WIFI_STATE_ENABLED)
                        wifiManager.startScan();
                    break;

                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        boolean success = intent.getBooleanExtra(
                                WifiManager.EXTRA_RESULTS_UPDATED, false);
                        if (success)
                            getWifiScanResults(wifiManager);
                    } else
                        getWifiScanResults(wifiManager);
                    break;
            }

        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())
                    || ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())
                    || LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction()))
                updateState();
        }
    };

    private NetworkDeviceLoader.OnDeviceRegisteredListener mRegisteredListener = new NetworkDeviceLoader.OnDeviceRegisteredListener() {
        @Override
        public void onDeviceRegistered(AccessDatabase database, final NetworkDevice device, final NetworkDevice.Connection connection) {
            if (mDeviceSelectedListener != null)
                mDeviceSelectedListener.onNetworkDeviceSelected(device, connection);
        }
    };

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == MSG_TO_SHOW_SCAN_RESULT) {
                cancelDiscovery();
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TO_SHOW_SCAN_RESULT), 30000);
            }

            switch (msg.what) {
                case STATE_LISTENING:
                    sheetText.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    sheetText.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    sheetText.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    sheetText.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    sheetText.setText(tempMsg);
                    try {
                        JSONObject hotspotInformation = new JSONObject(tempMsg);
                        connectToHotspot(hotspotInformation);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    public class ClientClass extends Thread {

        private BluetoothConnector.BluetoothSocketWrapper socket;
        private BluetoothConnector bluetoothConnector;

        ClientClass(BluetoothDevice device1) {
            //socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            bluetoothConnector = new BluetoothConnector(device1, false,
                    ConnectionUtils.getInstance(getContext()).getBluetoothAdapter(), null);
        }

        public void run() {

            try {

                socket  = bluetoothConnector.connect();
                ExtensionsUtils.getLogInfo(ExtensionsUtils.getBLUETOOTH_TAG(), "ClientSocket: socket coming from Bluetooth_Connector" + "\n");
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                if (mHandler != null) {
                    mHandler.sendMessage(message);
                }

                ExtensionsUtils.getLogInfo(ExtensionsUtils.getBLUETOOTH_TAG(), "ClientSocket: client connected and sent message" + "\n");

                sendReceive = new SendReceive(socket.getUnderlyingSocket());
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                if (mHandler != null) {
                    mHandler.sendMessage(message);
                }
            }

            ExtensionsUtils.getLogInfo(ExtensionsUtils.getBLUETOOTH_TAG(), "ClientSocket: I'm still on...loop has been broken " + "\n");

        }
    }

    private class SendReceive extends Thread {
        private BluetoothSocket bluetoothSocket = null;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        SendReceive(BluetoothSocket socket) {

            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                bluetoothSocket = socket;
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
                    mHandler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                    ExtensionsUtils.getLogInfo(ExtensionsUtils.getBLUETOOTH_TAG(), "ClientSocket: receiving bytes from server " + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    ExtensionsUtils.getLogInfo(ExtensionsUtils.getBLUETOOTH_TAG(), "ClientSocket: bytes receiving and error occurs " + e.getMessage() + "\n");
                    break;
                }
            }
            ExtensionsUtils.getLogInfo(ExtensionsUtils.getBLUETOOTH_TAG(), "ClientSocket: SendReceive: I'm still on. Loop has been broken " + "\n");
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
