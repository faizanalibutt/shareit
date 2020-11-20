package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.Activity
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.App
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.AppDelegate
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback.Callback
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.dialog.RemoveAdsDialog
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.service.CommunicationService
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.UIConnectionUtils
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.fragment.HomeFragment
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.AppUtils
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.TinyDB
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.purchaseRemoveAds
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_exit_rating_dialog.*
import kotlinx.android.synthetic.main.layout_exit_rating_dialog.view.*
import timber.log.Timber

class MainActivity : Activity() {

    //private var mExitPressTime: Long = 0
    private var mHomeFragment: HomeFragment? = null
    private val OPEN_COUNT_FOR_PURCHASE_DIALOG = "open_count_for_remove_ads"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dialog = showRateExitDialogue(this@MainActivity)

        (application as? App)?.splashInterstitial?.let { it1 ->
            if (it1.isLoaded())
                it1.showAd(this)
        }

        slide_menu.setOnClickListener {
            startActivity(Intent(this@MainActivity, SideMenuActivity::class.java))
        }

        main_pro_now.setOnClickListener {
            (application as? App)?.bp?.purchaseRemoveAds(this)
        }

        showRemoveAdsDialogue()

        mHomeFragment =
            supportFragmentManager.findFragmentById(R.id.activitiy_home_fragment) as HomeFragment?

    }

    private fun showRemoveAdsDialogue() {
        var openCount = TinyDB.getInstance(this).getInt(OPEN_COUNT_FOR_PURCHASE_DIALOG)
        TinyDB.getInstance(this).putInt(OPEN_COUNT_FOR_PURCHASE_DIALOG, ++openCount)

        if (!TinyDB.getInstance(this)
                .getBoolean(getString(R.string.is_premium)) && openCount % 2 != 0
        ) RemoveAdsDialog.show(this)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((application as? App)?.bp != null && ((application as? App)?.bp?.handleActivityResult(
                requestCode, resultCode, data
            ) == false)
        )
            super.onActivityResult(requestCode, resultCode, data)
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
