package com.hazelmobile.filetransfer.ui.activity

import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.ui.adapter.SmartFragmentPagerAdapter
import com.hazelmobile.filetransfer.ui.fragment.*

import kotlinx.android.synthetic.main.activity_content_sharing.*
import kotlinx.android.synthetic.main.content_sharing.*

class ContentSharingActivity : BaseActivity() {

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

        val pagerAdapter = SmartFragmentPagerAdapter(
            this@ContentSharingActivity,
            supportFragmentManager
        )

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

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.share_menu, menu)
        return true
    }

}
