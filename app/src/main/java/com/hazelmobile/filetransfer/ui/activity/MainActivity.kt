package com.hazelmobile.filetransfer.ui.activity

import android.content.Intent
import android.os.Bundle
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.app.Activity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        slide_menu.setOnClickListener {
            startActivity(Intent(this@MainActivity, SideMenu::class.java))
        }

    }

    companion object {

        val REQUEST_PERMISSION_ALL = 1
    }

}
