@file:JvmName("ExtensionsUtils")
package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.widget

import android.content.Context
import android.util.Log
import android.widget.Toast

val BLUETOOTH_TAG = "BLUETOOTH_TAG"
val THREAD_TAG = "THREAD_TAG"

fun getLog_D(TAG: String, logInfo: String?) {
    Log.d(TAG, logInfo)//ExtensionsUtils.getLog_D(" ");
}

fun getLog_W(TAG: String, logInfo: String?) {
    Log.w(TAG, logInfo)//ExtensionsUtils.getLog_D(" ");
}

fun getLog_I(TAG: String, logInfo: String?) {
    Log.i(TAG, logInfo)//ExtensionsUtils.getLog_D(" ");
}

fun getToast(logInfo: String?, context: Context) {
    Toast.makeText(context, logInfo, Toast.LENGTH_SHORT).show()
}