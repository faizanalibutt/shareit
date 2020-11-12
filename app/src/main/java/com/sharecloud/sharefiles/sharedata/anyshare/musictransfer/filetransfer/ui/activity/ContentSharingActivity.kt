package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.code4rox.adsmanager.AdmobUtils
import com.genonbeta.android.framework.widget.PowerfulActionMode
import com.google.android.material.tabs.TabLayout
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.`object`.Editable
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.`object`.Shareable
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.Activity
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.App
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.EditableListFragment
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.EditableListFragmentImpl
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback.Callback
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.adapter.SmartFragmentPagerAdapter
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.callback.PowerfulActionModeSupport
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.callback.SharingActionModeCallback
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.fragment.*
import kotlinx.android.synthetic.main.activity_content_sharing.*
import kotlinx.android.synthetic.main.banner_ads_layout_tag.*
import kotlinx.android.synthetic.main.content_sharing.*
import kotlinx.android.synthetic.main.file_transfer_general_button.*

@Suppress("UNCHECKED_CAST")
class ContentSharingActivity : Activity(),
    PowerfulActionModeSupport {

    private lateinit var mSelectionCallback: SharingActionModeCallback<Shareable>
    private var mBackPressedListener: Activity.OnBackPressedListener? = null
    private lateinit var mMode: PowerfulActionMode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_sharing)
        setSupportActionBar(toolbar)

        // setting action bar at runtime
        supportActionBar.let {
            it?.setDisplayHomeAsUpEnabled(true)
            it?.setDisplayShowTitleEnabled(false)
            //it?.setHomeAsUpIndicator(R.drawable.ic_back_24dp)
        }

        mMode = activity_content_sharing_action_mode
        mMode.setOnSelectionTaskListener { started, _ ->
            toolbar.visibility = if (!started) View.VISIBLE else View.GONE
        }

        mSelectionCallback =
            SharingActionModeCallback(
                null
            )
        val selectorConnection: PowerfulActionMode.SelectorConnection<Shareable> =
            PowerfulActionMode.SelectorConnection(mMode, mSelectionCallback)

        val pagerAdapter = object :
            SmartFragmentPagerAdapter(this@ContentSharingActivity, supportFragmentManager) {
            override fun onItemInstantiated(item: Companion.StableItem) {

                val fragmentImpl: EditableListFragmentImpl<Editable> =
                    item.getInitiatedItem() as EditableListFragmentImpl<Editable>

                fragmentImpl.setSelectorConnection(selectorConnection as PowerfulActionMode.SelectorConnection<Editable>)
                fragmentImpl.selectionCallback =
                    mSelectionCallback as EditableListFragment.SelectionCallback<Editable>

                if (content_sharing_viewPager.currentItem == item.getCurrentPosition())
                    attachListeners(fragmentImpl)

            }

        }

        pagerAdapter.add(
            SmartFragmentPagerAdapter.Companion.StableItem(
                0,
                ApplicationListFragment::class.java,
                null
            )
        )

        pagerAdapter.add(
            SmartFragmentPagerAdapter.Companion.StableItem(
                1,
                ImageListFragment::class.java,
                null
            )
        )

        pagerAdapter.add(
            SmartFragmentPagerAdapter.Companion.StableItem(
                2,
                MusicListFragment::class.java,
                null
            )
        )

        pagerAdapter.add(
            SmartFragmentPagerAdapter.Companion.StableItem(
                3,
                VideoListFragment::class.java,
                null
            )
        )

        val fileExplorerArgs = Bundle()
        fileExplorerArgs.putBoolean(FileExplorerFragment.ARG_SELECT_BY_CLICK, true)
        pagerAdapter.add(
            SmartFragmentPagerAdapter.Companion.StableItem(
                4,
                FileExplorerFragment::class.java,
                fileExplorerArgs
            ).setTitle(getString(R.string.text_files))
        )


        pagerAdapter.createTabs(activity_content_sharing_tab_layout, icons = false, text = true)
        content_sharing_viewPager.adapter = pagerAdapter
        content_sharing_viewPager.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(activity_content_sharing_tab_layout)
        )

        val tabLayout = activity_content_sharing_tab_layout
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                content_sharing_viewPager.currentItem = tab.position

                val fragment: EditableListFragmentImpl<Editable> =
                    pagerAdapter.getItem(tab.position) as EditableListFragmentImpl<Editable>

                attachListeners(fragment)

                if (fragment.adapterImpl != null)
                    Handler(Looper.getMainLooper()).postDelayed(
                        { fragment.adapterImpl.notifyAllSelectionChanges() },
                        200
                    )
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        val selectObserver = Observer<Boolean> { select -> selectionCallback(select) }
        Callback.getColor().observe(this, selectObserver)

        val isSingleAd = FirebaseRemoteConfig.getInstance().getBoolean("is_show_single_ad")
        if (!isSingleAd) {
            val admobUtils = AdmobUtils(this)
            admobUtils.loadBannerAd(banner_ad_view)
        }

    }

    private fun selectionCallback(select: Boolean) {

        val sendButton = button
        val tabLayout = activity_content_sharing_tab_layout
        val whiteColor = ContextCompat.getColor(this@ContentSharingActivity, R.color.white)
        val colorPrimary = ContextCompat.getColor(this@ContentSharingActivity, R.color.colorPrimary)
        val colorBlack =
            ContextCompat.getColor(this@ContentSharingActivity, R.color.black_transparent)

        if (select) {

            sendButton.setTextColor(whiteColor)
            sendButton.setBackgroundResource(R.drawable.background_content_share_button_select)
            ViewCompat.setBackgroundTintList(
                sendButton,
                ContextCompat.getColorStateList(
                    this@ContentSharingActivity,
                    R.color.text_button_text_color_selector_blue
                )
            )

            tabLayout.setBackgroundResource(R.color.colorPrimary)
            tabLayout.setTabTextColors(
                whiteColor,
                whiteColor
            )
            tabLayout.setSelectedTabIndicatorColor(whiteColor)

            mMode.setBackgroundColor(colorPrimary)
            mMode.setTitleTextColor(whiteColor)
            mMode.overflowIcon =
                resources.getDrawable(R.drawable.ic_action_overflow_menu_icon_white_24db)

        } else {

            sendButton.setTextColor(
                ContextCompat.getColor(
                    this@ContentSharingActivity,
                    R.color.text_color_gray
                )
            )
            sendButton.setBackgroundResource(R.drawable.background_content_share_button)
            ViewCompat.setBackgroundTintList(
                sendButton,
                ContextCompat.getColorStateList(
                    this@ContentSharingActivity,
                    R.color.text_button_text_color_selector
                )
            )

            tabLayout.setBackgroundResource(R.color.white)
            tabLayout.setTabTextColors(
                colorBlack,
                colorPrimary
            )
            tabLayout.setSelectedTabIndicatorColor(colorPrimary)
            if (mMode.hasActive(mSelectionCallback)) {
                mMode.finish(mSelectionCallback)
                Callback.setColor(false)
            }

            /*mMode.setBackgroundColor(whiteColor)
            mMode.setTitleTextColor(colorBlack)
            mMode.overflowIcon = mMode.overflowIcon*/

        }

    }

    override fun onBackPressed() {
        if (mBackPressedListener == null || !mBackPressedListener!!.onBackPressed()) {
            if (mMode.hasActive(mSelectionCallback)) {
                mMode.finish(mSelectionCallback)
                Callback.setColor(false)
            } else
                super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (App.bp != null && !(App.bp!!.handleActivityResult(
                requestCode, resultCode, data
            ))
        )
            super.onActivityResult(requestCode, resultCode, data)
    }

    fun attachListeners(fragment: EditableListFragmentImpl<Editable>) {
        mSelectionCallback.updateProvider(fragment as EditableListFragmentImpl<Shareable>)
        mBackPressedListener = if (fragment is OnBackPressedListener)
            fragment
        else
            null
    }

    override fun getPowerfulActionMode(): PowerfulActionMode {
        return mMode
    }

    fun btnOnClick(view: View) {
        /*mSelectionCallback.onActionMenuItemSelected(this, mMode, null)*/
        mSelectionCallback.sendFiles()
        finish()
    }

}
