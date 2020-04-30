package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback;

import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice;

import java.util.List;

public interface OnDeviceSelectedListener {
    void onDeviceSelected(NetworkDevice.Connection connection, List<NetworkDevice.Connection> availableInterfaces);
}
