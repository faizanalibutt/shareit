@file:JvmName("ExtensionsUtils")
package com.hazelmobile.filetransfer.widget

import android.content.Context
import android.util.Log
import android.widget.Toast

fun getLogInfo(logInfo: String?) {
    Log.d("file_transfer_debug", logInfo)//ExtensionsUtils.getLogInfo(" ");
}

fun getToast(logInfo: String?, context: Context) {
    Toast.makeText(context, logInfo, Toast.LENGTH_SHORT).show()
}