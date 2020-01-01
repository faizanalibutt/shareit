package com.hazelmobile.filetransfer.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Message;
import android.view.View;

import com.hazelmobile.filetransfer.BluetoothConnectorUtils;
import com.hazelmobile.filetransfer.widget.ExtensionsUtils;

import java.io.IOException;

import static com.hazelmobile.filetransfer.bluetooth.MyHandler.STATE_CONNECTED;
import static com.hazelmobile.filetransfer.bluetooth.MyHandler.STATE_CONNECTION_FAILED;

public class ClientThread extends Thread {

        private BluetoothConnectorUtils.BluetoothSocketWrapper socket;
        private BluetoothConnectorUtils bluetoothConnectorUtils;
        private MyHandler mHandler;
        private View mView;
        private BluetoothDataTransferThread bluetoothTransferThread;

        public ClientThread(BluetoothDevice device1, BluetoothAdapter bluetoothAdapter, View rootView, MyHandler handler) {
            bluetoothConnectorUtils = new BluetoothConnectorUtils(device1, false,
                    bluetoothAdapter, null);
            mView = rootView;
            mHandler = handler;
        }

        public void run() {

            try {

                socket = bluetoothConnectorUtils.connect();

                ExtensionsUtils.getLog_D(ExtensionsUtils.getBLUETOOTH_TAG(),
                        "ClientSocket: socket coming from Bluetooth_Connector" + "\n");

                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                if (mHandler != null) {
                    mHandler.sendMessage(message);
                }

            } catch (IOException e) {

                try {
                    if (socket != null) socket.close();
                } catch (IOException ex) {
                    ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                            "Could not close the client socket \n" + ex.getMessage());
                }

                ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                        "ClientSocket: client fed up with exception " + e.getMessage() + "\n");

                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                if (mHandler != null) {
                    mHandler.sendMessage(message);
                }

                ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                        "ClientSocket: client sending message connection failed \n " + e.getMessage() + "\n");

            }

            if (socket != null)
                manageMyConnectedSocket(socket);

            ExtensionsUtils.getLog_D(ExtensionsUtils.getBLUETOOTH_TAG(),
                    "ClientSocket: I'm still on...send has been called " + "\n");

        }

        private void manageMyConnectedSocket(BluetoothConnectorUtils.BluetoothSocketWrapper socket) {

            ExtensionsUtils.getLog_D(ExtensionsUtils.getBLUETOOTH_TAG(),
                    "ClientSocket: client connected and sent message " + "\n");
            if (bluetoothTransferThread == null) {
                bluetoothTransferThread = new BluetoothDataTransferThread(socket.getUnderlyingSocket(), mView, mHandler);
                bluetoothTransferThread.start();
            }

        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                        String.format("Could not close the connect socket %s", e.getMessage()));
            }
        }

    }