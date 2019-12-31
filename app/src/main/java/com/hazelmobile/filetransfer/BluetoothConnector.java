package com.hazelmobile.filetransfer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.hazelmobile.filetransfer.ui.fragment.SenderFragmentImpl;
import com.hazelmobile.filetransfer.widget.ExtensionsUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothConnector {

    private BluetoothSocketWrapper bluetoothSocket;
    private BluetoothDevice device;
    private boolean secure;
    private BluetoothAdapter adapter;
    private List<UUID> uuidCandidates;
    private int candidate;


    /**
     * @param device the device
     * @param secure if connection should be done via a secure socket
     * @param adapter the Android BT adapter
     * @param uuidCandidates a list of UUIDs. if null or empty, the Serial PP id is used
     */
    public BluetoothConnector(BluetoothDevice device, boolean secure, BluetoothAdapter adapter,
            List<UUID> uuidCandidates) {
        this.device = device;
        this.secure = secure;
        this.adapter = adapter;
        this.uuidCandidates = uuidCandidates;

        if (this.uuidCandidates == null || this.uuidCandidates.isEmpty()) {
            this.uuidCandidates = new ArrayList<>();
            this.uuidCandidates.add(SenderFragmentImpl.MY_UUID);
        }
    }

    public BluetoothSocketWrapper connect() throws IOException {
        boolean success = false;
        while (selectSocket()) {
            adapter.cancelDiscovery();

            try {
                bluetoothSocket.connect();
                success = true;
                break;
            } catch (IOException e) {
                //try the fallback
                try {
                    //bluetoothSocket = new FallbackBluetoothSocket(bluetoothSocket.getUnderlyingSocket());
                    Thread.sleep(200);
                    //bluetoothSocket.connect();
                    success = true;
                    break;
                } /*catch (FallbackException e1) {
                    Log.w(ExtensionsUtils.getBLUETOOTH_TAG(), "Could not initialize FallbackBluetoothSocket classes.", e);
                } */catch (InterruptedException e1) {
                    Log.w(ExtensionsUtils.getBLUETOOTH_TAG(), e1.getMessage(), e1);
                } /*catch (IOException e1) {
                    Log.w(ExtensionsUtils.getBLUETOOTH_TAG(), "Fallback failed. Cancelling.", e1);
                }*/
            }
        }

        if (!success) {
            throw new IOException("Could not connect to device: "+ device.getAddress());
        }

        return bluetoothSocket;
    }

    private boolean selectSocket() throws IOException {
        if (candidate >= uuidCandidates.size()) {
            return false;
        }

        BluetoothSocket tmp;
        UUID uuid = uuidCandidates.get(candidate++);

        Log.i("BT", "Attempting to connect to Protocol: "+ uuid);
        if (secure) {
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } else {
            tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
        }
        bluetoothSocket = new NativeBluetoothSocket(tmp);

        return true;
    }

    public interface BluetoothSocketWrapper {

        InputStream getInputStream() throws IOException;

        OutputStream getOutputStream() throws IOException;

        String getRemoteDeviceName();

        void connect() throws IOException;

        String getRemoteDeviceAddress();

        void close() throws IOException;

        BluetoothSocket getUnderlyingSocket();

    }


    public static class NativeBluetoothSocket implements BluetoothSocketWrapper {

        private BluetoothSocket socket;

        public NativeBluetoothSocket(BluetoothSocket tmp) {
            this.socket = tmp;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return socket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return socket.getOutputStream();
        }

        @Override
        public String getRemoteDeviceName() {
            return socket.getRemoteDevice().getName();
        }

        @Override
        public void connect() throws IOException {
            socket.connect();
        }

        @Override
        public String getRemoteDeviceAddress() {
            return socket.getRemoteDevice().getAddress();
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }

        @Override
        public BluetoothSocket getUnderlyingSocket() {
            return socket;
        }

    }

    public class FallbackBluetoothSocket extends NativeBluetoothSocket {

        private BluetoothSocket fallbackSocket;

        public FallbackBluetoothSocket(BluetoothSocket tmp) throws FallbackException {
            super(tmp);
            try
            {
              Class<?> clazz = tmp.getRemoteDevice().getClass();
              Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
              Method m = clazz.getMethod("createRfcommSocket", paramTypes);
              Object[] params = new Object[] {Integer.valueOf(2)};
              fallbackSocket = (BluetoothSocket) m.invoke(tmp.getRemoteDevice(), params);
            }
            catch (Exception e)
            {
                Log.w(ExtensionsUtils.getBLUETOOTH_TAG(), "Bluetooth Connect: " + e.getMessage());
                throw new FallbackException(e);
            }
        }

        @Override
        public InputStream getInputStream() {
            try {
                return fallbackSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public OutputStream getOutputStream() {
            try {
                return fallbackSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void connect() {
            try {
                fallbackSocket.connect();
            } catch (IOException e) {
                Log.w(ExtensionsUtils.getBLUETOOTH_TAG(), "Bluetooth Connect: " + e.getMessage());
            }
        }


        @Override
        public void close() {
            try {
                fallbackSocket.close();
            } catch (IOException e) {
                Log.w(ExtensionsUtils.getBLUETOOTH_TAG(), "Bluetooth Connect: " + e.getMessage());
            }
        }

    }

    public static class FallbackException extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public FallbackException(Exception e) {
            super(e);
        }

    }
}