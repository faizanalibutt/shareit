package com.hazelmobile.filetransfer.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.callback.Callback;
import com.hazelmobile.filetransfer.ui.callback.SnackbarSupport;
import com.hazelmobile.filetransfer.widget.ExtensionsUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static com.hazelmobile.filetransfer.bluetooth.MyHandler.STATE_MESSAGE_RECEIVED;

public class BluetoothDataTransferThread extends Thread implements SnackbarSupport {

    private BluetoothSocket bluetoothSocket = null;
    private final InputStream inputStream;
    private MyHandler mHandler;
    private View mView;

    BluetoothDataTransferThread(BluetoothSocket socket, View view, MyHandler handler) {
        InputStream tempIn = null;
        mView = view;
        mHandler = handler;

        try {
            bluetoothSocket = socket;
            tempIn = bluetoothSocket.getInputStream();
        } catch (IOException e) {
            ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                    "ClientSocket: BluetoothDataTransferThread: constructor fed up " + e.getMessage() + "\n");
            //e.printStackTrace();
        }

        inputStream = tempIn;
        // here i will test if issue comes in one device repeatedly...till then continue...
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        boolean success = false;
        while (true) {

            try {
                bytes = inputStream.read(buffer);
                mHandler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                ExtensionsUtils.getLog_I(ExtensionsUtils.getBLUETOOTH_TAG(),
                        "ClientSocket: BluetoothDataTransferThread: RECEIVING BYTES FROM SERVER" + "\n");
                success = true;
            } catch (IOException e) {

                ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                        "ClientSocket: BluetoothDataTransferThread: bytes receiving and error occurs " + e.getMessage() + "\n");

                try {
                    if (!success) {
                        bytes = inputStream.read(buffer);
                        mHandler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                        ExtensionsUtils.getLog_I(ExtensionsUtils.getBLUETOOTH_TAG(),
                                "ClientSocket: BluetoothDataTransferThread: RECEIVING BYTES FROM SERVER" + "\n");
                        success = true;
                    }
                } catch (IOException e1) {
                    ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                            "ClientSocket: BluetoothDataTransferThread: bytes receiving and error occurs " + e.getMessage() + "\n");
                }


                // enable camera here
                if (!success) {
                    Callback.setSenderAction(ActionType.CLOSE_DIALOG);
                    Objects.requireNonNull(createSnackbar(R.string.text_qrPromptRequired)).show();
                }

                break;
            } finally {
                try {
                    inputStream.close();
                    bluetoothSocket.close();
                    // enable camera here.
                } catch (IOException ex) {
                    ex.printStackTrace();
                    ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                            "ClientSocket: BluetoothDataTransferThread: Could not close the connect socket " + ex.getMessage() + "\n");
                }
            }
        }

        ExtensionsUtils.getLog_D(ExtensionsUtils.getBLUETOOTH_TAG(),
                "ClientSocket: BluetoothDataTransferThread: I'm still on. Loop has been broken " + "\n");
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                    String.format("Could not close the connect socket %s", e));
        }
    }

    @Override
    public Snackbar createSnackbar(int resId, @NotNull Object... objects) {
        return Snackbar.make(mView,
                mView.getContext().getString(resId, objects), Snackbar.LENGTH_INDEFINITE);
    }
}