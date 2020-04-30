package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.callback;

import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice;

/**
 * created by: veli
 * date: 16/04/18 03:18
 */
public interface NetworkDeviceSelectedListener {
    boolean onNetworkDeviceSelected(NetworkDevice networkDevice, NetworkDevice.Connection connection);

    boolean isListenerEffective();
}
