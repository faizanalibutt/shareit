package com.hazelmobile.filetransfer.ui.activity

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.app.Activity
import com.hazelmobile.filetransfer.util.ConnectionUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        slide_menu.setOnClickListener {
            startActivity(Intent(this@MainActivity, SideMenu::class.java))
        }

        Snackbar.make(container, "${ConnectionUtils.getInstance(this).isBleAvailable}",
            Snackbar.LENGTH_INDEFINITE).show()


    }

    companion object {

        val REQUEST_PERMISSION_ALL = 1
    }

    override fun onDestroy() {
        exitApp()
        super.onDestroy()
    }

}
