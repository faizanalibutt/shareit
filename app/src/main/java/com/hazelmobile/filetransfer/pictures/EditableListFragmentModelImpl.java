package com.hazelmobile.filetransfer.pictures;

/**
 * created by: veli
 * date: 8/24/18 1:36 PM
 */
public interface EditableListFragmentModelImpl<V extends EditableListAdapter.EditableViewHolder> {
    void setLayoutClickListener(EditableListFragment.LayoutClickListener<V> clickListener);
}
