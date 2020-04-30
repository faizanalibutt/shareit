package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.widget;

import com.genonbeta.android.framework.widget.ListAdapterImpl;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.Editable;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.exception.NotReadyException;

import java.util.List;

/**
 * created by: veli
 * date: 14/04/18 00:51
 */
public interface EditableListAdapterImpl<T extends Editable> extends ListAdapterImpl<T> {
    boolean filterItem(T item);

    T getItem(int position) throws NotReadyException;

    void notifyAllSelectionChanges();

    void notifyItemChanged(int position);

    void notifyItemRangeChanged(int positionStart, int itemCount);

    void syncSelectionList();

    void syncSelectionList(List<T> itemList);
}
