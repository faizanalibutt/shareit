package com.hazelmobile.filetransfer.bluetooth;

import android.bluetooth.BluetoothSocket;

import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.widget.ExtensionsUtils;

import java.io.IOException;
import java.io.InputStream;

public class SendReceive extends Thread {

        private BluetoothSocket bluetoothSocket = null;
        private final InputStream inputStream;

        SendReceive(BluetoothSocket socket) {

            InputStream tempIn = null;

            try {
                bluetoothSocket = socket;
                tempIn = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                        "ClientSocket: SendReceive: constructor fed up " + e.getMessage() + "\n");
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
                            "ClientSocket: SendReceive: RECEIVING BYTES FROM SERVER" + "\n");
                    success = true;
                } catch (IOException e) {

                    ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                            "ClientSocket: SendReceive: bytes receiving and error occurs " + e.getMessage() + "\n");

                    try {
                        if (!success) {
                            bytes = inputStream.read(buffer);
                            mHandler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                            ExtensionsUtils.getLog_I(ExtensionsUtils.getBLUETOOTH_TAG(),
                                    "ClientSocket: SendReceive: RECEIVING BYTES FROM SERVER" + "\n");
                            success = true;
                        }
                    } catch (IOException e1) {
                        ExtensionsUtils.getLog_W(ExtensionsUtils.getBLUETOOTH_TAG(),
                                "ClientSocket: SendReceive: bytes receiving and error occurs " + e.getMessage() + "\n");
                    }


                    // enable camera here
                    if (!success) {
                        closeDialog();
                        createSnackbar(R.string.text_qrPromptRequired).show();
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
                                "ClientSocket: SendReceive: Could not close the connect socket " + ex.getMessage() + "\n");
                    }
                }
            }
            ExtensionsUtils.getLog_D(ExtensionsUtils.getBLUETOOTH_TAG(),
                    "ClientSocket: SendReceive: I'm still on. Loop has been broken " + "\n");
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

    }