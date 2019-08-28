package com.hazelmobile.filetransfer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.hazelmobile.filetransfer.R

class SideMenu : BaseActivity() {

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
