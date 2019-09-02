package com.hazelmobile.filetransfer.ui.activity

import android.os.Bundle
import com.hazelmobile.filetransfer.R

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }

}