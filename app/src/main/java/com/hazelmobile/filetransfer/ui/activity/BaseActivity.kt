package com.hazelmobile.filetransfer.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    interface OnBackPressedListener {
        fun onBackPressed(): Boolean
    }

}