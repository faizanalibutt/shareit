package com.hazelmobile.filetransfer.ui.activity

import android.content.Intent
import android.graphics.drawable.ShapeDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.snackbar.Snackbar
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.`object`.NetworkDevice
import com.hazelmobile.filetransfer.app.Activity
import com.hazelmobile.filetransfer.ui.callback.SnackbarSupport
import com.hazelmobile.filetransfer.util.AppUtils
import kotlinx.android.synthetic.main.activity_side_menu.*

class SideMenu : Activity(), View.OnClickListener, SnackbarSupport {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_side_menu)
        setProfilePicture()
        init()
    }

    private fun init() {
        menu_histroy.setOnClickListener(this@SideMenu)
        //menu_help.setOnClickListener(this@SideMenu)
        menu_settings.setOnClickListener(this@SideMenu)
        //menu_feedback.setOnClickListener(this@SideMenu)
        menu_rateus.setOnClickListener(this@SideMenu)
        menu_privacy.setOnClickListener(this@SideMenu)
        //menu_about.setOnClickListener(this@SideMenu)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.menu_histroy -> startActivity(Intent(this@SideMenu, HistoryActivity::class.java))
            //R.id.menu_help -> createSnackbar(R.string.menu_generic_text)?.show()
            R.id.menu_settings -> startActivity(Intent(this@SideMenu, SettingsActivity::class.java))
            //R.id.menu_feedback -> createSnackbar(R.string.menu_generic_text)?.show()
            R.id.menu_rateus -> createSnackbar(R.string.menu_generic_text)?.show()
            R.id.menu_privacy -> {
                val url = "https://fiverr.com/faizistudio"
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build ()
                customTabsIntent.launchUrl(this, Uri.parse(url))
            }
            //R.id.menu_about -> createSnackbar(R.string.menu_generic_text)?.show()
            else -> return
        }
    }

    override fun createSnackbar(resId: Int, vararg objects: Any): Snackbar? {
        return Snackbar.make(container, getString(resId, objects), Snackbar.LENGTH_SHORT)
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
