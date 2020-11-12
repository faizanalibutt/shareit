package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity

import android.content.Intent
import android.graphics.drawable.ShapeDrawable
import android.net.NetworkInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.transition.TransitionManager
import com.code4rox.adsmanager.AdmobUtils
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.Activity
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.App
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.AppUtils
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.NetworkDeviceLoader
import kotlinx.android.synthetic.main.activity_welcome.*
import kotlinx.android.synthetic.main.banner_ads_layout_tag.*


class WelcomeActivity : Activity() {

    private lateinit var colorsList: IntArray

    private var userProfileColor: Int = -1
    private var updateColor = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        setWelcomePageDisallowed(true)
        colorsList = resources.getIntArray(R.array.colorsList)
        val welcome = defaultPreferences.getBoolean("introduction_shown", false)
        if (!welcome)
        {
            defaultPreferences.edit().putString("device_name",
                AppUtils.getLocalDeviceName(this@WelcomeActivity)).apply()

            userProfileColor = (colorsList.indices).random()

            defaultPreferences.edit().putInt("device_name_color",
                colorsList[userProfileColor]).apply()

            if (userProfileImage.drawable is ShapeDrawable) {
                val shapeDrawable: ShapeDrawable =
                    userProfileImage.drawable as ShapeDrawable
                shapeDrawable.paint.color = colorsList[userProfileColor]
            }
            editText.setText(AppUtils.getLocalDeviceName(this@WelcomeActivity))
        }
        defaultPreferences.edit().putBoolean("introduction_shown", true).apply()
        setProfilePicture()

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                if (!updateColor || editText.text == null || editText.text.isEmpty())
                    notifyUserProfileChanged()

                if (editText.text == null || editText.text.isEmpty()) {
                    updateColor = false
                    userProfileImage.setBackgroundResource(R.drawable.background_user_icon_default)
                    button.isClickable = false
                    text.setTextColor(
                        ContextCompat.getColor(
                            this@WelcomeActivity,
                            R.color.button_text_color_selector
                        )
                    )
                    button.setBackgroundResource(R.drawable.background_content_share_button)
                    image.setImageResource(R.drawable.ic_tick_grey_24dp)
                    ViewCompat.setBackgroundTintList(
                        button,
                        ContextCompat.getColorStateList(
                            this@WelcomeActivity,
                            R.color.text_button_text_color_selector
                        )
                    )

                } else {

                    button.isClickable = true
                    userProfileColor = (colorsList.indices).random()

                    AppUtils.getDefaultPreferences(this@WelcomeActivity).edit()
                        .putString("device_name", editText.text.toString())
                        .apply()
                    AppUtils.getDefaultPreferences(this@WelcomeActivity).edit()
                        .putInt("device_name_color", colorsList[userProfileColor])
                        .apply()

                    if (userProfileImage.drawable is ShapeDrawable && !updateColor) {
                        val shapeDrawable: ShapeDrawable =
                            userProfileImage.drawable as ShapeDrawable
                        shapeDrawable.paint.color = colorsList[userProfileColor]
                        updateColor = true
                    }

                    text.setTextColor(
                        ContextCompat.getColor(
                            this@WelcomeActivity,
                            R.color.white
                        )
                    )
                    button.setBackgroundResource(R.drawable.background_content_share_button_select)
                    image.setImageResource(R.drawable.ic_tick_white_24dp)
                    ViewCompat.setBackgroundTintList(
                        button,
                        ContextCompat.getColorStateList(
                            this@WelcomeActivity,
                            R.color.text_button_text_color_selector_blue
                        )
                    )
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        button.setOnClickListener { view ->
            if (editText.text == null || editText.text.isEmpty()) {
                view.isClickable = false
            } else {
                view.isClickable = true
                val closeIt = intent.getBooleanExtra("reverse_menu", false) ||
                        intent.getBooleanExtra("reverse_settings", false)
                if (closeIt)
                    finish()
                else
                    startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                finish()
            }
        }

        if (editText.text == null || editText.text.isEmpty()) {
            button.isClickable = false
        } else {
            button.isClickable = true
            button.setBackgroundResource(R.drawable.background_content_share_button_select)
            image.setImageResource(R.drawable.ic_tick_white_24dp)
            text.setTextColor(
                ContextCompat.getColor(
                    this@WelcomeActivity,
                    R.color.white
                )
            )
            ViewCompat.setBackgroundTintList(
                button,
                ContextCompat.getColorStateList(
                    this@WelcomeActivity,
                    R.color.text_button_text_color_selector_blue
                )
            )
        }

        val admobUtils = AdmobUtils(this)
        admobUtils.loadBannerAd(banner_ad_view)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (App.bp != null && !(App.bp!!.handleActivityResult(
                requestCode, resultCode, data
            ))
        )
            super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setProfilePicture() {
        val device_name = defaultPreferences.getString("device_name", "")
        editText.setText(device_name)
        editText.setSelection(editText.text.length)
        loadProfilePictureInto(device_name, userProfileImage)
        val color = AppUtils.getDefaultPreferences(this@WelcomeActivity)
                .getInt("device_name_color", -1)

        if (userProfileImage.drawable is ShapeDrawable && (color != -1 or R.color.white)) {
            val shapeDrawable: ShapeDrawable = userProfileImage.drawable as ShapeDrawable
            shapeDrawable.paint.color = color
        } else {
            userProfileImage.setBackgroundResource(R.drawable.background_user_icon_default)
        }
    }

    override fun onUserProfileUpdated() {
        super.onUserProfileUpdated()
        setUserProfile()
    }

    private fun setUserProfile() {

        /*val localDevice = AppUtils.getLocalDevice(applicationContext)*/
        loadProfilePictureInto(editText.text.toString(), userProfileImage)
        TransitionManager.beginDelayedTransition(mProfileView)

    }


    fun setProfileImage(view: View) {
        if (checkPermissionsState())
            requestProfilePictureChange()
        else
            requestRequiredPermissions(false)
    }
}
