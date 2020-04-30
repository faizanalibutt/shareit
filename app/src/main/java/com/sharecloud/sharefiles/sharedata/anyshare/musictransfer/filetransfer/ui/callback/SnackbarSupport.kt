package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.callback

import com.google.android.material.snackbar.Snackbar

interface SnackbarSupport {

    fun createSnackbar(resId: Int, vararg objects: Any): Snackbar?

}