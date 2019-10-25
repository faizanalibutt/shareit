package com.hazelmobile.filetransfer.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.transition.TransitionManager
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.app.Activity
import com.hazelmobile.filetransfer.pictures.AppUtils
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        setWelcomePageDisallowed(true)
        defaultPreferences.edit()
            .putBoolean("introduction_shown", true)
            .apply()

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                notifyUserProfileChanged()
                userProfileImage.setBackgroundResource(R.drawable.background_user_icon)
                button.setTextColor(ContextCompat.getColor(this@WelcomeActivity, R.color.white))
                button.setBackgroundResource(R.drawable.background_content_share_button_select)
                ViewCompat.setBackgroundTintList(
                    button,
                    ContextCompat.getColorStateList(this@WelcomeActivity, R.color.text_button_text_color_selector_blue)
                )
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
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

    protected fun checkPermissionsState(): Boolean {
        if (Build.VERSION.SDK_INT < 23)
            return true

        return AppUtils.checkRunningConditions(this)
    }



    fun moveToMain(view: View) {
        startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
        finish()
    }

    fun setProfileImage(view: View) {
        if (checkPermissionsState())
            requestProfilePictureChange()
        else
            requestRequiredPermissions(false)
    }
}
