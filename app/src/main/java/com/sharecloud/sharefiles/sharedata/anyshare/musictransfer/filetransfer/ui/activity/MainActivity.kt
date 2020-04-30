package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.Activity
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.App
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback.Callback
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.service.CommunicationService
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.UIConnectionUtils
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.fragment.HomeFragment
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.AppUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_exit_rating_dialog.*
import kotlinx.android.synthetic.main.layout_exit_rating_dialog.view.*

class MainActivity : Activity() {

    //private var mExitPressTime: Long = 0
    private var mHomeFragment: HomeFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dialog = showRateExitDialogue(this@MainActivity)

        val loadingAd = (application as App).mainAdmobUtils
        loadingAd?.showInterstitialAd()

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

        dialog.show()
        dialog.setOnDismissListener {
            dialog = showRateExitDialogue(this@MainActivity)
        }
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
