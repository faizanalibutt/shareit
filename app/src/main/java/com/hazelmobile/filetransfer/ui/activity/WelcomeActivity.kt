package com.hazelmobile.filetransfer.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.transition.TransitionManager
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.pictures.AppUtils
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

    }

    override fun onUserProfileUpdated() {
        super.onUserProfileUpdated()
        setUserProfile()
    }

    private fun setUserProfile() {

        val localDevice = AppUtils.getLocalDevice(applicationContext)
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
    }
}
