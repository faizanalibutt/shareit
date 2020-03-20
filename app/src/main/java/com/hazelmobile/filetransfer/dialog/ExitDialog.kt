package com.hazelmobile.filetransfer.dialog

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.app.Activity

class ExitDialog(val activity: Activity, val title: String) : AlertDialog.Builder(activity, R.style.Widget_Hazel_AppCompats_DialogTheme) {

    init {
        kotlin.runCatching {
            val mRootView =
                LayoutInflater.from(activity).inflate(R.layout.layout_exit_rating_dialog, null)
            setView(mRootView)

            setTitle(title)

            setPositiveButton(context.getString(R.string.butn_exit)) { _, _ ->
                activity.finish()
            }

            setNegativeButton("Cancel", null)

        }.onFailure {
            // here you can send developer message
        }
    }
}