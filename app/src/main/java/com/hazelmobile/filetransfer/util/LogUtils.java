package com.hazelmobile.filetransfer.util;

import android.util.Log;

import com.hazelmobile.filetransfer.service.CommunicationService;

public class LogUtils {

    public static void getLogInformation(String type, String message) {
        Log.i(CommunicationService.TAG_TRANSFER, String.format("%s: %s ", type, message));
    }

    public static void getLogDebug(String type, String message) {
        Log.d(CommunicationService.TAG_TRANSFER, String.format("%s: %s ", type, message));
    }

    public static void getLogWarning(String type, String message) {
        Log.w(CommunicationService.TAG_TRANSFER, String.format("%s: %s ", type, message));
    }

}
