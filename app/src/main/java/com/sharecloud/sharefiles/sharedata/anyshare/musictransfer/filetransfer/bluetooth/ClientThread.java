package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.bluetooth.ActionType;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback.Callback;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.callback.SnackbarSupport;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.fragment.SenderFragmentImpl;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.LogUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.widget.ExtensionsUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import static com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.bluetooth.MyHandler.STATE_CONNECTED;
import static com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.bluetooth.MyHandler.STATE_CONNECTION_FAILED;

public class ClientThread extends Thread implements SnackbarSupport {

    private final BluetoothSocket mSocket;
    private MyHandler mHandler;
    private View mView;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDataTransferThread bluetoothTransferThread;

    public ClientThread(BluetoothDevice device, BluetoothAdapter bluetoothAdapter, View rootView, MyHandler handler) {

        mView = rootView;
        mHandler = handler;
        mBluetoothAdapter = bluetoothAdapter;
        BluetoothSocket tmp = null;

        try {
            tmp = device.createInsecureRfcommSocketToServiceRecord(
                    SenderFragmentImpl.MY_UUID);
        } catch (IOException e) {
            LogUtils.getLogInformation("Socket Type:" + "create() failed", e.getMessage());
        }
        mSocket = tmp;
    }

    public void run() {

        mBluetoothAdapter.cancelDiscovery();
        try {
            mSocket.connect();

            ExtensionsUtils.getLog_D(ExtensionsUtils.getBLUETOOTH_TAG(),
                    "ClientSocket: run(): mSocket after connecting call: " + "\n" + mSocket);

            getHandlerMessage(STATE_CONNECTED);

        } catch (Exception e) {

            try {
                mSocket.close();
            } catch (IOException ex) {
                ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                        "Could not close the client mSocket \n" + ex.getMessage());
            }

            ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                    "ClientSocket: client fed up with exception " + e.getMessage() + "\n");

            getHandlerMessage(STATE_CONNECTION_FAILED);

            ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                    "ClientSocket: client sending message connection failed \n " + e.getMessage() + "\n");

            Callback.setSenderAction(ActionType.CLOSE_DIALOG);
            Objects.requireNonNull(createSnackbar(R.string.text_qrPromptRequired)).show();
            return;
        }

        manageMyConnectedSocket(mSocket);

    }

    private void getHandlerMessage(int stateConnected) {
        Message message = Message.obtain();
        message.what = stateConnected;
        if (mHandler != null) {
            mHandler.sendMessage(message);
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket socket) {

        ExtensionsUtils.getLog_D(ExtensionsUtils.getBLUETOOTH_TAG(),
                "ClientSocket: client connected and sent message " + "\n");
        if (bluetoothTransferThread == null) {
            bluetoothTransferThread = new BluetoothDataTransferThread(socket, mView, mHandler);
            bluetoothTransferThread.start();
        }

    }

    // Closes the connect mSocket and causes the thread to finish.
    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                    String.format("Could not close the connect mSocket %s", e.getMessage()));
        }
    }

    @Override
    public Snackbar createSnackbar(int resId, @NotNull Object... objects) {
        return Snackbar.make(mView,
                mView.getContext().getString(resId, objects), Snackbar.LENGTH_INDEFINITE);
    }

}