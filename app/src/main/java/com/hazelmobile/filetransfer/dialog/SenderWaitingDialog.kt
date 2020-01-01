package com.hazelmobile.filetransfer.dialog

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.app.Activity
import com.hazelmobile.filetransfer.callback.Callback
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
                else -> {
                }
            }
            mRootView.dialog_sender_title.text = activity.getString(
                R.string.layout_dialog_receiver_title, anyObject
            )
            val selectObserver =
                Observer<Any> { any -> selectionCallback(any, mRootView.dialog_sender_title) }
            Callback.getDialogInfo().observe(activity, selectObserver)
        }.onFailure {
            // here you can send developer message
        }

    }

    private fun selectionCallback(dialogInfo: Any, dialogSenderTitle: TextView) {
        when (dialogInfo) {
            is String -> dialogSenderTitle.text = dialogInfo
            is Boolean -> if (!activity.isFinishing)
            {
                dismiss()
            }
            else -> {}
        }
    }

}