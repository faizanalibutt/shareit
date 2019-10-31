package com.hazelmobile.filetransfer.ui.activity

import android.content.Intent
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.view.View
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.`object`.NetworkDevice
import com.hazelmobile.filetransfer.app.Activity
import com.hazelmobile.filetransfer.pictures.AppUtils
import kotlinx.android.synthetic.main.activity_side_menu.*

class SideMenu : Activity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_side_menu)
        setProfilePicture()
        init()
    }

    private fun init() {
        menu_help.setOnClickListener(this@SideMenu)
        menu_settings.setOnClickListener(this@SideMenu)
        menu_feedback.setOnClickListener(this@SideMenu)
        menu_rateus.setOnClickListener(this@SideMenu)
        menu_privacy.setOnClickListener(this@SideMenu)
        menu_about.setOnClickListener(this@SideMenu)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.menu_help -> TODO("waiting for the design")
            R.id.menu_settings -> startActivity(Intent(this@SideMenu, SettingsActivity::class.java))
            R.id.menu_feedback -> TODO("waiting for the design")
            R.id.rateus -> TODO("waiting for the design")
            R.id.privacy -> TODO("waiting for the design")
            R.id.about -> TODO("waiting for the design")
            else -> return
        }
    }

    fun closeMenu(view: View) {
        finish()
    }

    private fun setProfilePicture() {
        val localDevice: NetworkDevice = AppUtils.getLocalDevice(applicationContext)
        textView.text = localDevice.nickname
        loadProfilePictureInto(localDevice.nickname, user_image)
        val color = AppUtils.getDefaultPreferences(this@SideMenu).getInt("device_name_color", -1)

        if (user_image.drawable is ShapeDrawable && (color != -1 or R.color.white)) {
            val shapeDrawable: ShapeDrawable = user_image.drawable as ShapeDrawable
            shapeDrawable.paint.color = color
        } else {
            user_image.setBackgroundResource(R.drawable.background_user_icon_default)
        }
    }

    override fun onRestart() {
        super.onRestart()
        setProfilePicture()
    }

    fun setProfileImage(view: View) {
        if (checkPermissionsState()) {
            startActivity(Intent(this@SideMenu, WelcomeActivity::class.java))
        } else
            requestRequiredPermissions(false)
    }
}
