package com.hazelmobile.filetransfer.util;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.WorkerThread;
import androidx.core.content.ContextCompat;

import com.genonbeta.android.framework.util.Interrupter;
import com.hazelmobile.filetransfer.files.AppConfig;
import com.hazelmobile.filetransfer.ui.adapter.NetworkDeviceListAdapter;
import com.hazelmobile.filetransfer.widget.ExtensionsUtils;

import java.util.List;

/**
 * created by: veli
 * date: 15/04/18 18:37
 */
public class ConnectionUtils {
    public static final String TAG = ConnectionUtils.class.getSimpleName();

    private Context mContext;
    private WifiManager mWifiManager;
    private BluetoothAdapter bluetoothAdapter;
    private HotspotUtils mHotspotUtils;
    private LocationManager mLocationManager;
    private ConnectivityManager mConnectivityManager;

    private ConnectionUtils(Context context) {
        mContext = context;

        if (mContext != null) {
            mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mLocationManager = (LocationManager) mContext.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            mHotspotUtils = HotspotUtils.getInstance(mContext);
            mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
    }

    public static ConnectionUtils getInstance(Context context) {
        return new ConnectionUtils(context);
    }

    public static String getCleanNetworkName(String networkName) {
        if (networkName == null)
            return "";

        return networkName.replace("\"", "");
    }

    public boolean canAccessLocation() {
        return hasLocationPermission(getContext()) && isLocationServiceEnabled();
    }

    public boolean canReadScanResults() {
        return getWifiManager().isWifiEnabled() && (Build.VERSION.SDK_INT < 23 || canAccessLocation());
    }

    public boolean disableCurrentNetwork() {
        // TODO: Networks added by other applications will possibly reconnect even if we disconnect them
        // This is because we are only allowed to manipulate the connections that we added.
        // And if it is the case, then the return value of disableNetwork will be false.
        return isConnectedToAnyNetwork()
                && getWifiManager().disconnect()
                && getWifiManager().disableNetwork(getWifiManager().getConnectionInfo().getNetworkId());
    }

    public String getHotspotLocalIpAddress() {
        String ipAddress = "192.168.43.1";
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        int address = dhcpInfo.serverAddress;
        ipAddress = ((address & 0xFF)
                + "." + ((address >> 8) & 0xFF)
                + "." + ((address >> 16) & 0xFF)
                + "." + ((address >> 24) & 0xFF));
        return ipAddress;
    }

    @WorkerThread
    public String establishHotspotConnection(final Interrupter interrupter,
                                             final NetworkDeviceListAdapter.HotspotNetwork hotspotNetwork,
                                             final ConnectionCallback connectionCallback) {
        final int pingTimeout = 1000; // ms
        final long startTime = System.currentTimeMillis();

        String remoteAddress = null;
        boolean connectionToggled = false;

        while (true) {
            int passedTime = (int) (System.currentTimeMillis() - startTime);

            if (!getWifiManager().isWifiEnabled()) {
                Log.d(TAG, "establishHotspotConnection(): Wifi is off. Making a request to turn it on");

                if (!getWifiManager().setWifiEnabled(true)) {
                    Log.d(TAG, "establishHotspotConnection(): Wifi was off. The request has failed. Exiting.");
                    break;
                }
            } else if (!isConnectedToNetwork(hotspotNetwork) && !connectionToggled) {
                Log.d(TAG, "establishHotspotConnection(): Requested network toggle");
                toggleConnection(hotspotNetwork);

                connectionToggled = true;
            } else {
                Log.d(TAG, "establishHotspotConnection(): Waiting to connect to the server");
                final DhcpInfo routeInfo = getWifiManager().getDhcpInfo();

                if (routeInfo != null && routeInfo.gateway > 0) {
                    final String testedRemoteAddress = NetworkUtils.convertInet4Address(routeInfo.gateway);

                    Log.d(TAG, String.format("establishHotspotConnection(): DhcpInfo: gateway: %s dns1: %s dns2: %s ipAddr: %s serverAddr: %s netMask: %s",
                            testedRemoteAddress,
                            NetworkUtils.convertInet4Address(routeInfo.dns1),
                            NetworkUtils.convertInet4Address(routeInfo.dns2),
                            NetworkUtils.convertInet4Address(routeInfo.ipAddress),
                            NetworkUtils.convertInet4Address(routeInfo.serverAddress),
                            NetworkUtils.convertInet4Address(routeInfo.netmask)));

                    Log.d(TAG, "establishHotspotConnection(): There is DHCP info provided waiting to reach the address " + testedRemoteAddress);

                    if (NetworkUtils.ping(testedRemoteAddress)) {
                        Log.d(TAG, "establishHotspotConnection(): AP has been reached. Returning OK state.");
                        remoteAddress = testedRemoteAddress;
                        break;
                    } else {
                        Log.d(TAG, "establishHotspotConnection(): Connection check ping failed");
                    }
                } else
                    Log.d(TAG, "establishHotspotConnection(): No DHCP provided. Looping...");
            }

            if (connectionCallback.onTimePassed(1000, passedTime) || interrupter.interrupted()) {
                ExtensionsUtils.getLogInfo(ExtensionsUtils.getBLUETOOTH_TAG(),
                        "establishHotspotConnection(): Timed out or onTimePassed returned true. Exiting...");
                Log.d(TAG, "establishHotspotConnection(): Timed out or onTimePassed returned true. Exiting...");
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }

        return remoteAddress;
    }

    public boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public Context getContext() {
        return mContext;
    }

    public ConnectivityManager getConnectivityManager() {
        return mConnectivityManager;
    }

    public HotspotUtils getHotspotUtils() {
        return mHotspotUtils;
    }

    public LocationManager getLocationManager() {
        return mLocationManager;
    }

    public WifiManager getWifiManager() {
        return mWifiManager;
    }

    public void openBluetooth() {
        if (getBluetoothAdapter() == null) {
            Toast.makeText(mContext, "This device does not support bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!getBluetoothAdapter().isEnabled()) {
            getBluetoothAdapter().enable();
        }
    }


    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    public boolean isConnectionSelfNetwork() {
        WifiInfo wifiInfo = getWifiManager().getConnectionInfo();

        return wifiInfo != null
                && getCleanNetworkName(wifiInfo.getSSID()).startsWith(AppConfig.PREFIX_ACCESS_POINT);
    }

    public boolean isConnectedToAnyNetwork() {
        NetworkInfo info = getConnectivityManager().getActiveNetworkInfo();

        return info != null
                && info.getType() == ConnectivityManager.TYPE_WIFI
                && info.isConnected();
    }

    public boolean isConnectedToNetwork(NetworkDeviceListAdapter.HotspotNetwork hotspotNetwork) {
        if (!isConnectedToAnyNetwork())
            return false;

        if (hotspotNetwork.BSSID != null)
            return hotspotNetwork.BSSID.equals(getWifiManager().getConnectionInfo().getBSSID());

        return hotspotNetwork.SSID.equals(getCleanNetworkName(getWifiManager().getConnectionInfo().getSSID()));
    }

    public boolean isLocationServiceEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public boolean isMobileDataActive() {
        return mConnectivityManager.getActiveNetworkInfo() != null
                && mConnectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * 创建WifiConfiguration的类型
     */
    public static final int WIFICIPHER_NOPASS = 1;
    public static final int WIFICIPHER_WEP = 2;
    public static final int WIFICIPHER_WPA = 3;

    /**
     * 创建WifiConfiguration
     *
     * @param ssid
     * @param password
     * @param type
     * @return
     */
    public static WifiConfiguration createWifiCfg(String ssid, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        config.SSID = "\"" + ssid + "\"";

        if (type == WIFICIPHER_NOPASS) {
//            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.wepTxKeyIndex = 0;

//            无密码连接WIFI时，连接不上wifi，需要注释两行代码
//            config.wepKeys[0] = "";
//            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }

    public boolean addNetwork(WifiConfiguration wf) {
        //断开当前的连接
        disableCurrentNetwork();

        //连接新的连接
        try {
            /*int netId = mWifiManager.addNetwork(wf);
            boolean enable = mWifiManager.enableNetwork(netId, true);
            Toast.makeText(mContext, "wifi network added", Toast.LENGTH_SHORT).show();
            return enable;*/

            boolean enable = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
                for (WifiConfiguration hotspotWifi : list) {
                    if (hotspotWifi.SSID != null && hotspotWifi.SSID.equalsIgnoreCase(wf.SSID)) {
                        mWifiManager.disconnect();
                        enable = mWifiManager.enableNetwork(hotspotWifi.networkId, true);
                        mWifiManager.reconnect();
                    }
                }
            } else {
                int netId = mWifiManager.addNetwork(wf);
                enable = mWifiManager.enableNetwork(netId, true);
            }

            return enable;
        } catch (Exception exp) {
            Toast.makeText(mContext, "wifi network not added", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public boolean toggleConnection(NetworkDeviceListAdapter.HotspotNetwork hotspotNetwork) {
        if (!isConnectedToNetwork(hotspotNetwork)) {
            if (isConnectedToAnyNetwork())
                disableCurrentNetwork();

            WifiConfiguration config = new WifiConfiguration();

            config.SSID = String.format("\"%s\"", hotspotNetwork.SSID);

            switch (hotspotNetwork.keyManagement) {
                case 0: // OPEN
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    break;
                case 1: // WEP64
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

                    if (hotspotNetwork.password != null
                            && hotspotNetwork.password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = hotspotNetwork.password;
                    } else {
                        //fail("Please type hex pair for the password");
                    }
                    break;
                case 2: // WEP128
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

                    if (hotspotNetwork.password != null
                            && hotspotNetwork.password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = hotspotNetwork.password;
                    } else {
                        //fail("Please type hex pair for the password");
                    }
                    break;
                case 3: // WPA_TKIP
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                    if (hotspotNetwork.password != null
                            && hotspotNetwork.password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = hotspotNetwork.password;
                    } else {
                        config.preSharedKey = '"' + hotspotNetwork.password + '"';
                    }
                    break;
                case 4: // WPA2_AES
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

                    if (hotspotNetwork.password != null
                            && hotspotNetwork.password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = hotspotNetwork.password;
                    } else {
                        config.preSharedKey = '"' + hotspotNetwork.password + '"';
                    }
                    break;
            }

            /**
             * to add hotspot wifi;
             */
            /*int netId = getWifiManager().addNetwork(config);
            getWifiManager().disconnect();
            getWifiManager().enableNetwork(netId, true);
            return getWifiManager().reconnect();*/

            try {
                int netId = getWifiManager().addNetwork(config);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    List<WifiConfiguration> list = getWifiManager().getConfiguredNetworks();
                    for (WifiConfiguration hotspotWifi : list) {
                        if (hotspotWifi.SSID != null && hotspotWifi.SSID.equalsIgnoreCase(config.SSID)) {
                            getWifiManager().disconnect();
                            getWifiManager().enableNetwork(hotspotWifi.networkId, true);
                            return getWifiManager().reconnect();
                        }
                    }
                } else {
                    getWifiManager().disconnect();
                    getWifiManager().enableNetwork(netId, true);
                    return getWifiManager().reconnect();
                }
            } catch (Exception exp) {
                disableCurrentNetwork();
                return false;
            }

        }
        disableCurrentNetwork();
        return false;
    }

    public interface ConnectionCallback {
        boolean onTimePassed(int delimiter, long timePassed);
    }
}
