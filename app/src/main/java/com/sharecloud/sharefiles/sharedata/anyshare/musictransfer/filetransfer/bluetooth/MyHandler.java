package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.bluetooth;

import android.os.Handler;
import android.os.Message;

import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.bluetooth.ActionType;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MyHandler extends Handler {

    public static final int MSG_TO_SHOW_SCAN_RESULT = 0X99;
    public static final int STATE_LISTENING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTION_FAILED = 4;
    public static final int STATE_MESSAGE_RECEIVED = 5;

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        if (msg.what == MSG_TO_SHOW_SCAN_RESULT) {
            com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback.Callback.setSenderAction(ActionType.DISCOVERY);
            sendMessageDelayed(obtainMessage(MSG_TO_SHOW_SCAN_RESULT), 15000);
        }

        switch (msg.what) {
            case STATE_LISTENING:
                com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback.Callback.setDialogInfo("Listening");
                break;
            case STATE_CONNECTING:
                com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback.Callback.setDialogInfo("Connecting");
                break;
            case STATE_CONNECTED:
                com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback.Callback.setDialogInfo("Connected");
                break;
            case STATE_CONNECTION_FAILED:
                com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback.Callback.setDialogInfo("Connection Failed");
                break;
            case STATE_MESSAGE_RECEIVED:
                byte[] readBuff = (byte[]) msg.obj;
                String tempMsg = new String(readBuff, 0, msg.arg1);
                com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback.Callback.setDialogInfo(tempMsg);
                try {
                    JSONObject hotspotInformation = new JSONObject(tempMsg);
                    LogUtils.getLogWarning("Client", String.format("Message Received From Server: %s", hotspotInformation));
                    com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback.Callback.setSenderAction(hotspotInformation);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void removeHanlderMessages() {
        removeMessages(MSG_TO_SHOW_SCAN_RESULT);
        removeMessages(STATE_CONNECTED);
        removeMessages(STATE_CONNECTING);
        removeMessages(STATE_CONNECTION_FAILED);
        removeMessages(STATE_MESSAGE_RECEIVED);
        removeMessages(STATE_LISTENING);
    }
}
