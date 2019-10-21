package com.hazelmobile.filetransfer.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.genonbeta.android.framework.widget.PowerfulActionMode
import com.google.android.material.tabs.TabLayout
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.adapter.ApplicationListFragment
import com.hazelmobile.filetransfer.adapter.MusicListFragment
import com.hazelmobile.filetransfer.adapter.SelectionCallbackGlobal
import com.hazelmobile.filetransfer.adapter.VideoListFragment
import com.hazelmobile.filetransfer.files.FileExplorerFragment
import com.hazelmobile.filetransfer.files.SharingActionModeCallback
import com.hazelmobile.filetransfer.pictures.*
import com.hazelmobile.filetransfer.ui.adapter.SmartFragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_content_sharing.*
import kotlinx.android.synthetic.main.content_sharing.*

@Suppress("UNCHECKED_CAST")
class ContentSharingActivity : BaseActivity(), PowerfulActionModeSupport {

    private lateinit var mSelectionCallback: SharingActionModeCallback<Shareable>
    private var mBackPressedListener: OnBackPressedListener? = null
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
        mMode.setOnSelectionTaskListener { started, _ -> toolbar.visibility = if (!started) View.VISIBLE else View.GONE }

        mSelectionCallback = SharingActionModeCallback(null)
        val selectorConnection: PowerfulActionMode.SelectorConnection<Shareable> = PowerfulActionMode.SelectorConnection(mMode, mSelectionCallback)

        val pagerAdapter = object :
            SmartFragmentPagerAdapter(this@ContentSharingActivity, supportFragmentManager) {
            override fun onItemInstantiated(item: Companion.StableItem) {

                val fragmentImpl: EditableListFragmentImpl<Editable> = item.getInitiatedItem() as EditableListFragmentImpl<Editable>

                fragmentImpl.setSelectorConnection(selectorConnection as PowerfulActionMode.SelectorConnection<Editable>)
                fragmentImpl.selectionCallback = mSelectionCallback as EditableListFragment.SelectionCallback<Editable>

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

                val fragment: EditableListFragmentImpl<Editable> = pagerAdapter.getItem(tab.position) as EditableListFragmentImpl<Editable>

                attachListeners(fragment)

                if (fragment.adapterImpl != null)
                    Handler(Looper.getMainLooper()).postDelayed({ fragment.adapterImpl.notifyAllSelectionChanges() }, 200)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        val selectObserver = Observer<Boolean> { select -> selectionCallback(select) }
        SelectionCallbackGlobal.getColor().observe(this, selectObserver)

    }

    private fun selectionCallback(select: Boolean) {

        val sendButton = content_send_button
        val tabLayout = activity_content_sharing_tab_layout
        val whiteColor = ContextCompat.getColor(this@ContentSharingActivity, R.color.white)
        val colorPrimary = ContextCompat.getColor(this@ContentSharingActivity, R.color.colorPrimary)
        val colorBlack = ContextCompat.getColor(this@ContentSharingActivity, R.color.black_transparent)

        if (select) {

            sendButton.setTextColor(whiteColor)
            sendButton.setBackgroundResource(R.drawable.background_content_share_button_select)
            ViewCompat.setBackgroundTintList(
                sendButton,
                ContextCompat.getColorStateList(this@ContentSharingActivity, R.color.text_button_text_color_selector_blue)
            )

            tabLayout.setBackgroundResource(R.color.colorPrimary)
            tabLayout.setTabTextColors(
                whiteColor,
                whiteColor
            )
            tabLayout.setSelectedTabIndicatorColor(whiteColor)

            mMode.setBackgroundColor(colorPrimary)
            mMode.setTitleTextColor(whiteColor)
            mMode.overflowIcon = resources.getDrawable(R.drawable.ic_action_over_flow_icon_white_24dp)

        } else {

            sendButton.setTextColor(ContextCompat.getColor(this@ContentSharingActivity, R.color.text_color_gray))
            sendButton.setBackgroundResource(R.drawable.background_content_share_button)
            ViewCompat.setBackgroundTintList(
                sendButton,
                ContextCompat.getColorStateList(this@ContentSharingActivity, R.color.text_button_text_color_selector)
            )

            tabLayout.setBackgroundResource(R.color.white)
            tabLayout.setTabTextColors(
                colorBlack,
                colorPrimary
            )
            tabLayout.setSelectedTabIndicatorColor(colorPrimary)

            /*mMode.setBackgroundColor(whiteColor)
            mMode.setTitleTextColor(colorBlack)
            mMode.overflowIcon = mMode.overflowIcon*/

        }

    }

    override fun onBackPressed() {
        if (mBackPressedListener == null || !mBackPressedListener!!.onBackPressed()) {
            if (mMode.hasActive(mSelectionCallback)) {
                mMode.finish(mSelectionCallback)
                SelectionCallbackGlobal.setColor(false)
            } else
                super.onBackPressed()
        }
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

}
