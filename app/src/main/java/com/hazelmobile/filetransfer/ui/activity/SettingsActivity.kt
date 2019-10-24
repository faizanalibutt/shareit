package com.hazelmobile.filetransfer.ui.activity

import android.os.Bundle
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.app.Activity

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar.let {
            it?.setDisplayHomeAsUpEnabled(true)
            it?.setHomeAsUpIndicator(R.drawable.ic_back_24dp)
        }

    }

}