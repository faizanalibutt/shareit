package com.hazelmobile.filetransfer.util

import android.app.Dialog
import android.content.Context
import android.text.format.DateUtils
import android.view.Window
import com.genonbeta.android.framework.util.date.ElapsedTime
import com.hazelmobile.filetransfer.R

object TimeUtils {

    fun formatDateTime(context: Context, millis: Long): CharSequence {
        return DateUtils.formatDateTime(
            context,
            millis,
            DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE
        )
    }

    fun getDuration(milliseconds: Long): String {
        val string = StringBuilder()

        val calculator = ElapsedTime.ElapsedTimeCalculator(milliseconds / 1000)

        val hours = calculator.crop(3600)
        val minutes = calculator.crop(60)
        val seconds = calculator.leftTime

        if (hours > 0) {
            if (hours < 10)
                string.append("0")

            string.append(hours)
            string.append(":")
        }

        if (minutes < 10)
            string.append("0")

        string.append(minutes)
        string.append(":")

        if (seconds < 10)
            string.append("0")

        string.append(seconds)

        return string.toString()
    }

    fun getFriendlyElapsedTime(context: Context, estimatedTime: Long): String {
        val elapsedTime = ElapsedTime(estimatedTime)
        val appendList = ArrayList<String>()

        if (elapsedTime.years > 0)
            appendList.add(context.getString(R.string.text_yearCountShort, elapsedTime.years))

        if (elapsedTime.months > 0)
            appendList.add(context.getString(R.string.text_monthCountShort, elapsedTime.months))

        if (elapsedTime.years == 0L) {
            if (elapsedTime.days > 0)
                appendList.add(context.getString(R.string.text_dayCountShort, elapsedTime.days))

            if (elapsedTime.months == 0L) {
                if (elapsedTime.hours > 0)
                    appendList.add(
                        context.getString(
                            R.string.text_hourCountShort,
                            elapsedTime.hours
                        )
                    )

                if (elapsedTime.days == 0L) {
                    if (elapsedTime.minutes > 0)
                        appendList.add(
                            context.getString(
                                R.string.text_minuteCountShort,
                                elapsedTime.minutes
                            )
                        )

                    if (elapsedTime.hours == 0L)
                    // always applied
                        appendList.add(
                            context.getString(
                                R.string.text_secondCountShort,
                                elapsedTime.seconds
                            )
                        )
                }
            }
        }

        val stringBuilder = StringBuilder()

        for (appendItem in appendList) {
            if (stringBuilder.length > 0)
                stringBuilder.append(" ")

            stringBuilder.append(appendItem)
        }

        return stringBuilder.toString()
    }

    fun getTimeAgo(context: Context, time: Long): String {
        val differ = ((System.currentTimeMillis() - time) / 1000).toInt()

        if (differ == 0)
            return context.getString(R.string.text_timeJustNow)
        else if (differ < 60)
            return context.resources.getQuantityString(R.plurals.text_secondsAgo, differ, differ)
        else if (differ < 3600)
            return context.resources.getQuantityString(
                R.plurals.text_minutesAgo,
                differ / 60,
                differ / 60
            )

        return context.getString(R.string.text_longAgo)
    }

    fun initCustomDialog(context: Context, layout: Int): Dialog? {

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(layout)
        return dialog

    }

}