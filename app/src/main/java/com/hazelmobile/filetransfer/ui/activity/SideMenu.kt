package com.hazelmobile.filetransfer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.app.Activity

class SideMenu : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_side_menu)
    }

    fun openSettings(view: View) {
        startActivity(Intent(this@SideMenu, SettingsActivity::class.java))
    }

    fun closeMenu(view: View) {
        finish()
    }
}
