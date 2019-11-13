package com.hazelmobile.filetransfer.dialog

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.app.Activity
import com.hazelmobile.filetransfer.model.Bluetooth
import kotlinx.android.synthetic.main.layout_dialog_connection_info.view.*

class SenderWaitingDialog(val activity: Activity, var anyObject: Any) :
    AlertDialog(activity) {

    init {
        kotlin.runCatching {
            val mRootView =
                LayoutInflater.from(activity).inflate(R.layout.layout_dialog_connection_info, null)
            setView(mRootView)
            anyObject = when (anyObject) {
                is ScanResult -> (anyObject as ScanResult).SSID
                is Bluetooth -> (anyObject as Bluetooth).device.name
                else -> {}
            }
            mRootView.dialog_sender_title.text = activity.getString(
                R.string.layout_dialog_receiver_title, anyObject
            )
        }
    }

}