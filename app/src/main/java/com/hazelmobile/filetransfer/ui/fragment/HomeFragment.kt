package com.hazelmobile.filetransfer.ui.fragment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.ui.activity.BaseActivity
import com.hazelmobile.filetransfer.ui.adapter.SmartFragmentPagerAdapter
import com.hazelmobile.filetransfer.utils.callback.TitleSupport
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), TitleSupport, BaseActivity.OnBackPressedListener {

    private lateinit var mAdapter: SmartFragmentPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(context!!)
    }

    private fun initializeViews(context: Context) {

        mAdapter = SmartFragmentPagerAdapter(context, childFragmentManager)

        mAdapter.add(SmartFragmentPagerAdapter.Companion.StableItem(0, ShareFragment::class.java, Bundle()))
        mAdapter.add(SmartFragmentPagerAdapter.Companion.StableItem(1, BrowseFragment::class.java, Bundle()))
        mAdapter.add(SmartFragmentPagerAdapter.Companion.StableItem(2, PremiumFragment::class.java, Bundle()))


        mAdapter.createTabs(bottomNavigationView)
        mViewPager.adapter = mAdapter

        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {}
        })

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            mViewPager.currentItem = menuItem.order
            true
        }

    }
    override fun getTitle(context: Context): CharSequence {
        return context.getString(R.string.text_home)
    }

    override fun onBackPressed(): Boolean {

        val activeItem = mAdapter.getItem(mViewPager.currentItem)

        if (activeItem is BaseActivity.OnBackPressedListener && activeItem.onBackPressed())
            return true

        if (mViewPager.currentItem > 0) {
            mViewPager.setCurrentItem(0, true)
            return true
        }

        return false
    }

}
