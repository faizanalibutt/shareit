package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.widget;

import android.content.Context;

import androidx.annotation.NonNull;

import com.genonbeta.android.framework.util.listing.merger.StringMerger;

import java.util.List;

import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.miscpkg.GalleryGroupShareable;

/**
 * created by: Veli
 * date: 30.03.2018 14:58
 */
abstract public class GalleryGroupEditableListAdapter<T extends GalleryGroupShareable, V extends com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.widget.GroupEditableListAdapter.GroupViewHolder>
        extends com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.widget.GroupEditableListAdapter<T, V>
        implements GroupEditableListAdapter.GroupLister.CustomGroupLister<T> {
    public static final int MODE_GROUP_BY_ALBUM = MODE_GROUP_BY_DATE + 1;

    public GalleryGroupEditableListAdapter(Context context, int groupBy) {
        super(context, groupBy);
    }

    @Override
    public boolean onCustomGroupListing(GroupLister<T> lister, int mode, T object) {
        if (mode == MODE_GROUP_BY_ALBUM) {
            lister.offer(object, new StringMerger<T>(object.albumName));
            return true;
        }

        return false;
    }

    @Override
    public GroupLister<T> createLister(List<T> loadedList, int groupBy) {
        return super.createLister(loadedList, groupBy)
                .setCustomLister(this);
    }

    @NonNull
    @Override
    public String getSectionName(int position, T object) {
        if (!object.isGroupRepresentative())
            if (getGroupBy() == MODE_GROUP_BY_ALBUM)
                return object.albumName;

        return super.getSectionName(position, object);
    }
}
