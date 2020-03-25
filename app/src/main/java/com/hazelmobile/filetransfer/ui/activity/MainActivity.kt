package com.hazelmobile.filetransfer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.app.Activity
import com.hazelmobile.filetransfer.service.CommunicationService
import com.hazelmobile.filetransfer.ui.UIConnectionUtils
import com.hazelmobile.filetransfer.ui.fragment.HomeFragment
import com.hazelmobile.filetransfer.util.AppUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    //private var mExitPressTime: Long = 0
    private var mHomeFragment: HomeFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        slide_menu.setOnClickListener {
            startActivity(Intent(this@MainActivity, SideMenuActivity::class.java))
        }

        mHomeFragment =
            supportFragmentManager.findFragmentById(R.id.activitiy_home_fragment) as HomeFragment?

    }

    override fun onBackPressed() {
        /*if (System.currentTimeMillis() - mExitPressTime < 2000)
            super.onBackPressed()
        else {
            mExitPressTime = System.currentTimeMillis()
            Toast.makeText(this, R.string.mesg_secureExit, Toast.LENGTH_SHORT).show()
        }*/
        if (mHomeFragment!!.onBackPressed())
            return

        showRateExitDialogue(this@MainActivity)
    }

    companion object {
        val REQUEST_PERMISSION_ALL = 1
    }

    override fun onDestroy() {
        exitApp()
        super.onDestroy()
    }

    override fun onRestart() {
        super.onRestart()
        Thread {
            try {
                Thread.sleep(1500)
            } catch (exp: InterruptedException) {}

            if (!UIConnectionUtils.isOreoAbove() || (UIConnectionUtils.isOreoAbove() && Settings.System.canWrite(this)))
                AppUtils.startForegroundService(this, Intent(this@MainActivity, CommunicationService::class.java)
                    .setAction(CommunicationService.ACTION_FORCEFULLY_TOGGLE_HOTSPOT))
        }.start()
    }

}
