package com.hazelmobile.filetransfer.dialog

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.app.Activity

class RateusDialog(val activity: Activity) : AlertDialog(activity) {

    init {
        kotlin.runCatching {
            val mRootView =
                LayoutInflater.from(activity).inflate(R.layout.layout_rate_us_dialog, null)
            setView(mRootView)
            setTitle("Rate us")

        }.onFailure {
            // here you can send developer message
        }
    }
}