package com.hazelmobile.filetransfer.ui.adapter

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.MenuItem
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.hazelmobile.filetransfer.ui.callback.IconSupport
import com.hazelmobile.filetransfer.ui.callback.TitleSupport

open class SmartFragmentPagerAdapter(private var context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm/*, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT*/) {


    private var mItems: MutableList<StableItem> = ArrayList()

    open fun onItemInstantiated(item: StableItem) {}

    fun add(fragment: StableItem) {
        mItems.add(fragment)
    }

    fun add(position: Int, fragment: StableItem) {
        mItems.add(position, fragment)
    }

    fun createTabs(tabLayout: TabLayout, icons: Boolean, text: Boolean) {
        if (count > 0) {
            for (iterator in 0 until count) {
                val stableItem = getStableItem(iterator)
                val fragment = getItem(iterator)
                val tab = tabLayout.newTab()

                if (fragment is IconSupport && icons)
                    tab.setIcon(fragment.getIconRes())

                if (!stableItem.iconOnly && text)
                    if (stableItem.title != null)
                        tab.text = stableItem.title
                    else if (fragment is TitleSupport)
                        tab.text = fragment.getTitle(context)

                tabLayout.addTab(tab)
            }
        }
    }

    fun createTabs(bottomNavigationView: BottomNavigationView) {
        if (count > 0) {
            for (iterator in 0 until count) {
                val stableItem = getStableItem(iterator)
                val fragment = getItem(iterator)
                val menuTitle: CharSequence

                menuTitle = when {
                    stableItem.title != null && stableItem.title!!.isNotEmpty() -> stableItem.title!!
                    fragment is TitleSupport -> fragment.getTitle(context)
                    else -> iterator.toString()
                }

                val menuItem: MenuItem = bottomNavigationView.menu
                    .add(0, iterator, iterator, menuTitle)

                if (fragment is IconSupport)
                    menuItem.setIcon(fragment.getIconRes())
            }
        }
    }

    @NonNull
    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val fragment: Fragment = super.instantiateItem(container, position) as Fragment
        val stableItem = getStableItem(position)
        stableItem.mInitiatedItem = fragment
        stableItem.mCurrentPosition = position
        // no use of it till now but may be its usable in future
        onItemInstantiated(stableItem)

        return fragment
    }

    @NonNull
    override fun getItem(position: Int): Fragment {

        val stableItem = getStableItem(position)

        var instantiatedItem: Fragment? = null

        if (stableItem.mInitiatedItem != null) {
            instantiatedItem = stableItem.getInitiatedItem()
            instantiatedItem.arguments = stableItem.arguments
        } else {
            instantiatedItem = Fragment.instantiate(context, stableItem.clazzName)
            instantiatedItem.arguments = stableItem.arguments
        }

        return instantiatedItem
    }

    override fun getItemId(position: Int): Long {
        return getStableItem(position).itemId
    }

    override fun getCount(): Int {
        return mItems.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val fragment = getItem(position)
        return if (fragment is TitleSupport) fragment.getTitle(context) else super.getPageTitle(
            position
        )
    }

    private fun getStableItem(position: Int): StableItem {
        return mItems[position]
    }

    fun getFragments(): List<StableItem> {
        return mItems
    }

    companion object {

        // to get every fragment information
        class StableItem() : Parcelable {

            constructor(itemId: Long, clazzName: String, arguments: Bundle?) : this() {
                this.itemId = itemId
                this.clazzName = clazzName
                this.arguments = arguments
            }

            constructor(
                itemId: Long,
                clazz: Class<out Fragment>, @NonNull arguments: Bundle?
            ) : this(
                itemId,
                clazz.name,
                arguments
            )

            constructor(parcel: Parcel) : this(
                parcel.readLong(),
                parcel.readString()!!,
                parcel.readBundle(/*ClassLoader.getSystemClassLoader()*/)!!
            ) {
                setTitle(parcel.readString()!!)
                setIconOnly(parcel.readInt() == 1)
            }

            override fun writeToParcel(dest: Parcel?, flags: Int) {
                dest?.writeLong(itemId)
                dest?.writeString(clazzName)
                dest?.writeBundle(arguments)
                dest?.writeString(title)
                dest?.writeInt(if (iconOnly) 1 else 0)
            }

            override fun describeContents(): Int {
                return 0
            }

            fun getCurrentPosition(): Int {
                return mCurrentPosition
            }

            fun getInitiatedItem(): Fragment {
                return mInitiatedItem!!
            }

            fun setIconOnly(iconOnly: Boolean): StableItem {
                this.iconOnly = iconOnly
                return this
            }

            fun setTitle(title: String?): StableItem {
                this.title = title
                return this
            }

            var itemId: Long = -1
            var clazzName: String = ""
            var arguments: Bundle? = null
            var title: String? = null
            var iconOnly: Boolean = false
            var mInitiatedItem: Fragment? = null
            var mCurrentPosition: Int = -1

            companion object CREATOR : Parcelable.Creator<StableItem> {
                override fun createFromParcel(parcel: Parcel): StableItem {
                    return StableItem(parcel)
                }

                override fun newArray(size: Int): Array<StableItem?> {
                    return arrayOfNulls(size)
                }
            }

        }

    }

}