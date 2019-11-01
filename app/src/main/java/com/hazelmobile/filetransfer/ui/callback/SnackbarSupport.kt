package com.hazelmobile.filetransfer.ui.callback

import com.google.android.material.snackbar.Snackbar

interface SnackbarSupport {

    fun createSnackbar(resId: Int, vararg objects: Any): Snackbar?

}