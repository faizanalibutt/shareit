package com.hazelmobile.filetransfer.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.View
import com.genonbeta.android.framework.widget.PowerfulActionMode
import com.google.android.material.tabs.TabLayout
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.files.FileExplorerFragment
import com.hazelmobile.filetransfer.files.SharingActionModeCallback
import com.hazelmobile.filetransfer.pictures.*
import com.hazelmobile.filetransfer.ui.adapter.SmartFragmentPagerAdapter
import com.hazelmobile.filetransfer.ui.fragment.ApplicationListFragment
import com.hazelmobile.filetransfer.ui.fragment.MusicListFragment
import com.hazelmobile.filetransfer.ui.fragment.VideoListFragment
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

                // todo change this fuction for all adapters #14
                val fragmentImpl: EditableListFragmentImpl<Editable> = item.getInitiatedItem() as EditableListFragmentImpl<Editable>

                fragmentImpl.setSelectorConnection(selectorConnection as PowerfulActionMode.SelectorConnection<Editable>)
                fragmentImpl.setSelectionCallback(mSelectionCallback as EditableListFragment.SelectionCallback<Editable>)

                if (content_sharing_viewPager.currentItem == item.getCurrentPosition())
                    attachListeners(fragmentImpl)

            }

        }

        val fileExplorerArgs = Bundle()
        fileExplorerArgs.putBoolean(FileExplorerFragment.ARG_SELECT_BY_CLICK, true)
        pagerAdapter.add(
            SmartFragmentPagerAdapter.Companion.StableItem(
                0,
                FileExplorerFragment::class.java,
                fileExplorerArgs
            )
                .setTitle(getString(R.string.text_files))
        )

        /*pagerAdapter.add(
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
        )*/


        pagerAdapter.createTabs(activity_content_sharing_tab_layout, icons = false, text = true)
        content_sharing_viewPager.adapter = pagerAdapter
        content_sharing_viewPager.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(activity_content_sharing_tab_layout)
        )

        activity_content_sharing_tab_layout.setupWithViewPager(content_sharing_viewPager)

        // todo baby its need to be iimplemented commented #15
        /*val tabLayout = activity_content_sharing_tab_layout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                final EditableListFragment fragment = (EditableListFragment) pagerAdapter.getItem(tab.getPosition());

                attachListeners(fragment);

                if (fragment.getAdapterImpl() != null)
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fragment.getAdapterImpl().notifyAllSelectionChanges();
                        }
                    }, 200);
            }

            @Override
            public void onTabUnselected(final TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });*/

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.share_menu, menu)
        return true
    }

    override fun onBackPressed() {
        if (mBackPressedListener == null || !mBackPressedListener!!.onBackPressed()) {
            if (mMode.hasActive(mSelectionCallback))
                mMode.finish(mSelectionCallback)
            else
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
