package com.hazelmobile.filetransfer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.`object`.NetworkDevice
import com.hazelmobile.filetransfer.app.Activity
import com.hazelmobile.filetransfer.util.AppUtils
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : Activity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar.let {
            it?.setDisplayHomeAsUpEnabled(true)
        }
        setUsername()
        init()

    }

    private fun init() {
        shareUserName.setOnClickListener(this@SettingsActivity)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.shareUserName ->
                if (checkPermissionsState())
                    startActivity(Intent(this@SettingsActivity, WelcomeActivity::class.java)
                        .putExtra("reverse_settings", true))
                else
                    requestRequiredPermissions(false)
        }
    }

    private fun setUsername() {
        val localDevice: NetworkDevice = AppUtils.getLocalDevice(applicationContext)
        user_name.text = localDevice.nickname
    }

    override fun onRestart() {
        super.onRestart()
        setUsername()
    }

}