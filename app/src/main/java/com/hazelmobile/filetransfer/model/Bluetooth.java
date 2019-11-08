package com.hazelmobile.filetransfer.model;

import android.bluetooth.BluetoothDevice;

public class Bluetooth {

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    private BluetoothDevice device;
    private String data;

    public Bluetooth(BluetoothDevice bluetoothDevice, String data) {
        super();
        this.data = data;
        this.device = bluetoothDevice;
    }
}
