package com.hazelmobile.filetransfer.dialog

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.RatingBar
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.app.Activity
import com.hazelmobile.filetransfer.callback.Callback
import kotlinx.android.synthetic.main.layout_exit_rating_dialog.view.*

class RateExitDialog(val activity: Activity, val title: String, val adsVisible: Boolean) :
    AlertDialog.Builder(activity, R.style.Widget_Hazel_AppCompat_DialogTheme) {

    init {
        kotlin.runCatching {
            val mRootView =
                LayoutInflater.from(activity).inflate(R.layout.layout_exit_rating_dialog, null)

            setView(mRootView)
            setTitle(title)

            if (adsVisible) {
                mRootView.rating_group.visibility = View.VISIBLE
            }

            setPositiveButton(title) { _, _ ->
                if (adsVisible) {
                    activity.finish()
                } else {
                    val url = "https://play.google.com"
                    val builderTab = CustomTabsIntent.Builder()
                    val customTabsIntent = builderTab.build()
                    customTabsIntent.launchUrl(context, Uri.parse(url))
                }
            }

            setNegativeButton("Cancel", null)

            mRootView.rating_bar_value.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { _, rating, _ ->
                    if (rating < 4)
                        Callback.setRating(rating)
                    else
                        Callback.setRating(rating)
                }

        }.onFailure {
            // here you can send developer message
        }
    }
}