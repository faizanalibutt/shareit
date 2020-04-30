package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object;

import com.genonbeta.android.framework.object.Selectable;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.Comparable;

/**
 * created by: Veli
 * date: 18.01.2018 20:57
 */

public interface Editable extends Comparable, Selectable {
    boolean applyFilter(String[] filteringKeywords);

    long getId();

    void setId(long id);
}
