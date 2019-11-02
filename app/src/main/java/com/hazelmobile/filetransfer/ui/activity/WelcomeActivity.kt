package com.hazelmobile.filetransfer.ui.activity

import android.content.Intent
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.transition.TransitionManager
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.`object`.NetworkDevice
import com.hazelmobile.filetransfer.app.Activity
import com.hazelmobile.filetransfer.pictures.AppUtils
import kotlinx.android.synthetic.main.activity_side_menu.*
import kotlinx.android.synthetic.main.activity_welcome.*


class WelcomeActivity : Activity() {

    private lateinit var colorsList: IntArray

    private var userProfileColor: Int = -1
    private var updateColor = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        setWelcomePageDisallowed(true)
        defaultPreferences.edit().putBoolean("introduction_shown", true).apply()
        colorsList = resources.getIntArray(R.array.colorsList)

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
                startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun setProfilePicture() {
        val localDevice: NetworkDevice = AppUtils.getLocalDevice(applicationContext)
        textView.text = localDevice.nickname
        loadProfilePictureInto(localDevice.nickname, user_image)
        val color =
            AppUtils.getDefaultPreferences(this@WelcomeActivity)
                .getInt("device_name_color", -1)

        if (user_image.drawable is ShapeDrawable && (color != -1 or R.color.white)) {
            val shapeDrawable: ShapeDrawable = user_image.drawable as ShapeDrawable
            shapeDrawable.paint.color = color
        } else {
            user_image.setBackgroundResource(R.drawable.background_user_icon_default)
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
