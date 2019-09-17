package com.hazelmobile.filetransfer.util.callback

import androidx.annotation.DrawableRes

interface IconSupport {
    @DrawableRes
    fun getIconRes(): Int
}