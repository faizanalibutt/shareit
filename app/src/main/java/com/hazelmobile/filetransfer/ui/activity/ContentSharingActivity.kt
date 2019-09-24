package com.hazelmobile.filetransfer.ui.activity

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.genonbeta.android.framework.widget.PowerfulActionMode
import com.google.android.material.tabs.TabLayout
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.files.FileExplorerFragment
import com.hazelmobile.filetransfer.files.SharingActionModeCallback
import com.hazelmobile.filetransfer.pictures.Editable
import com.hazelmobile.filetransfer.pictures.EditableListFragmentImpl
import com.hazelmobile.filetransfer.pictures.ImageListFragment
import com.hazelmobile.filetransfer.ui.adapter.SmartFragmentPagerAdapter
import com.hazelmobile.filetransfer.ui.fragment.*

import kotlinx.android.synthetic.main.activity_content_sharing.*
import kotlinx.android.synthetic.main.content_sharing.*
import kotlinx.android.synthetic.main.fragment_home.*

class ContentSharingActivity : BaseActivity() {

    private var mMode: PowerfulActionMode? = null
    //private lateinit var mSelectionCallback: SharingActionModeCallback
    private var mBackPressedListener: OnBackPressedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_sharing)
        setSupportActionBar(toolbar)

        // setting action bar at runtime
        supportActionBar.let {
            it?.setDisplayHomeAsUpEnabled(true)
            it?.setDisplayShowTitleEnabled(false)
            it?.setHomeAsUpIndicator(R.drawable.ic_back_24dp)
        }

        /*val pagerAdapter = SmartFragmentPagerAdapter(
            this@ContentSharingActivity,
            supportFragmentManager
        ) *//*{
            override fun onItemInstantiated(item: SmartFragmentPagerAdapter.Companion.StableItem) {

                val fragmentImpl = item.getInitiatedItem() as EditableListFragmentImpl<Editable>
                if (content_sharing_viewPager.getCurrentItem() == item.getCurrentPosition())
                    attachListeners(fragmentImpl)
            }
        }*/

        val pagerAdapter = object :
            SmartFragmentPagerAdapter(this@ContentSharingActivity, supportFragmentManager) {
            override fun onItemInstantiated(item: Companion.StableItem) {

                // todo change this fuction for all adapters #14
                var istrue = false
                if (content_sharing_viewPager.currentItem == 3 && !istrue) {
                    istrue = true
                    val fragmentImpl: EditableListFragmentImpl<Editable> =
                        item.getInitiatedItem() as EditableListFragmentImpl<Editable>
                    attachListeners(fragmentImpl)
                }


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
                VideoListFragment::class.java,
                null
            )
        )

        pagerAdapter.add(
            SmartFragmentPagerAdapter.Companion.StableItem(
                3,
                MusicListFragment::class.java,
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
            )
                .setTitle(getString(R.string.text_files))
        )

        pagerAdapter.createTabs(activity_content_sharing_tab_layout, icons = false, text = true)
        content_sharing_viewPager.adapter = pagerAdapter
        content_sharing_viewPager.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(activity_content_sharing_tab_layout)
        )

        activity_content_sharing_tab_layout.setupWithViewPager(content_sharing_viewPager)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.share_menu, menu)
        return true
    }

    override fun onBackPressed() {
        if (mBackPressedListener == null || !mBackPressedListener!!.onBackPressed()) {
            /*if (mMode!!.hasActive(mSelectionCallback))
                mMode!!.finish(mSelectionCallback)
            else*/
            super.onBackPressed()
        }
    }

    fun attachListeners(fragment: EditableListFragmentImpl<Editable>) {
        mBackPressedListener = if (fragment is OnBackPressedListener)
            fragment
        else
            null
    }

}
