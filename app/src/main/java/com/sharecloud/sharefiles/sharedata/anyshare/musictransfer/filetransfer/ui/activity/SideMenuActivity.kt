package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity

import android.content.Intent
import android.graphics.drawable.ShapeDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import com.code4rox.adsmanager.AdmobUtils
import com.google.android.material.snackbar.Snackbar
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.`object`.NetworkDevice
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.Activity
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.callback.SnackbarSupport
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.AppUtils
import kotlinx.android.synthetic.main.activity_side_menu.*
import kotlinx.android.synthetic.main.banner_ads_layout_tag.*

class SideMenuActivity : Activity(), View.OnClickListener, SnackbarSupport {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_side_menu)
        setProfilePicture()
        init()
    }

    override fun onRestart() {
        super.onRestart()
        setProfilePicture()
    }

    private fun init() {
        menu_histroy.setOnClickListener(this@SideMenuActivity)
        //menu_help.setOnClickListener(this@SideMenu)
        menu_settings.setOnClickListener(this@SideMenuActivity)
        //menu_feedback.setOnClickListener(this@SideMenu)
        menu_rateus.setOnClickListener(this@SideMenuActivity)
        menu_privacy.setOnClickListener(this@SideMenuActivity)
        //menu_about.setOnClickListener(this@SideMenu)
        val admobUtils = AdmobUtils(this)
        admobUtils.loadBannerAd(banner_ad_view)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.menu_histroy -> startActivity(Intent(this@SideMenuActivity, HistoryActivity::class.java))
            //R.id.menu_help -> createSnackbar(R.string.menu_generic_text)?.show()
            R.id.menu_settings -> startActivity(Intent(this@SideMenuActivity, SettingsActivity::class.java))
            //R.id.menu_feedback -> createSnackbar(R.string.menu_generic_text)?.show()
            R.id.menu_rateus -> {
                dialog = showRateExitDialogue(this@SideMenuActivity)
                dialog.show()
            }
            R.id.menu_privacy -> {
                val url = "https://www.freeprivacypolicy.com/privacy/view/50c5621471755f1548917ebbe5e90160"
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
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
        val color = AppUtils.getDefaultPreferences(this@SideMenuActivity).getInt("device_name_color", -1)

        if (user_image.drawable is ShapeDrawable && (color != -1 or R.color.white)) {
            val shapeDrawable: ShapeDrawable = user_image.drawable as ShapeDrawable
            shapeDrawable.paint.color = color
        } else {
            user_image.setBackgroundResource(R.drawable.background_user_icon_default)
        }
    }

    fun setProfileImage(view: View) {
        if (checkPermissionsState()) {
            startActivity(Intent(this@SideMenuActivity, WelcomeActivity::class.java)
                .putExtra("reverse_menu", true))
        } else
            requestRequiredPermissions(false)
    }

}
