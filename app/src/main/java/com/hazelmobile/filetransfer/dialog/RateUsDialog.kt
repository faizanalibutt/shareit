package com.hazelmobile.filetransfer.dialog

import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.view.LayoutInflater
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.app.Activity
import com.hazelmobile.filetransfer.callback.Callback
import kotlinx.android.synthetic.main.layout_rate_us_dialog.view.*

class RateUsDialog(val activity: Activity) : AlertDialog.Builder(activity) {

    init {
        kotlin.runCatching {
            val mRootView =
                LayoutInflater.from(activity).inflate(R.layout.layout_rate_us_dialog, null)
            setView(mRootView)

            setTitle("Rate us")

            setPositiveButton("Rate Us") { _, _ ->
                val url = "https://play.google.com"
                val builderTab = CustomTabsIntent.Builder()
                val customTabsIntent = builderTab.build()
                customTabsIntent.launchUrl(context, Uri.parse(url))
            }

            setNegativeButton("Cancel", null)

            val dialog = this.show()

            mRootView.rating_bar_value.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { _, rating, _ ->
                    if (rating < 4)
                        Callback.setRating(rating)
                    else
                        Callback.setRating(rating)
                //dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Rate Us"
                }

        }.onFailure {
            // here you can send developer message
        }
    }
}