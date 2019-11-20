@file:JvmName("ExtensionsUtils")
package com.hazelmobile.filetransfer.widget

import android.content.Context
import android.util.Log
import android.widget.Toast

fun getLogInfo(TAG: String, logInfo: String?) {
    Log.d(TAG, logInfo)//ExtensionsUtils.getLogInfo(" ");
}

fun getToast(logInfo: String?, context: Context) {
    Toast.makeText(context, logInfo, Toast.LENGTH_SHORT).show()
}