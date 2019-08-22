package com.hazelmobile.filetransfer.utils.callback

import androidx.annotation.DrawableRes

interface IconSupport {
    @DrawableRes
    fun getIconRes(): Int
}