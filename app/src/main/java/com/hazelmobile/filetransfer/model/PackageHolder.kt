package com.hazelmobile.filetransfer.model

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import com.genonbeta.android.framework.util.FileUtils
import java.io.File

/***
 * @param friendlyName give app name
 * @param appInfo gives every application info stored in phone.
 * @param appSize gives app size
 * @param packageName give app packageinfo
 */

data class PackageHolder(
    val friendlyName: String,
    val appInfo: ApplicationInfo,
    val appSize: String,
    val packageName: String
) {

    companion object {
        val FORMAT = ".apk"
        val MIME_TYPE = FileUtils.getFileContentType(FORMAT)
    }
}