package com.hazelmobile.filetransfer.model

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import com.genonbeta.android.framework.util.FileUtils
import com.hazelmobile.filetransfer.pictures.Shareable
import java.io.File
import java.lang.reflect.Executable

/***
 * @param friendlyName give app name
 * @param appInfo gives every application info stored in phone.
 * @param appSize gives app size
 * @param packageName give app packageinfo
 * @param executable give file object
 * @param version tells app Version
 */

data class PackageHolder(
    val appfriendlyName: String,
    val appInfo: ApplicationInfo,
    val version: String,
    val packageName: String,
    val executable: File,
    val appSize: String
) : Shareable(
    appInfo.packageName.hashCode().toLong(),
    appfriendlyName,
    "${appfriendlyName}_version.apk}",
    MIME_TYPE,
    executable.lastModified(),
    executable.length(),
    Uri.fromFile(executable)
) {

    companion object {
        const val FORMAT = ".apk"
        val MIME_TYPE: String = FileUtils.getFileContentType(FORMAT)
    }
}