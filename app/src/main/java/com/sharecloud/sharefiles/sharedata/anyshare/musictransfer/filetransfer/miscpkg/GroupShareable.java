package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.miscpkg;

import android.net.Uri;

import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.EditableListAdapter;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.Shareable;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.TextUtils;

public class GroupShareable extends Shareable implements GroupEditable {
        public int viewType = EditableListAdapter.VIEW_TYPE_DEFAULT;
        public String representativeText;

        public GroupShareable() {
            super();
        }

        public GroupShareable(int viewType, String representativeText) {
            this.viewType = viewType;
            this.representativeText = representativeText;
        }

        public GroupShareable(long id, String friendlyName, String fileName, String mimeType, long date, long size, Uri uri) {
            super(id, friendlyName, fileName, mimeType, date, size, uri);
        }

        @Override
        public int getRequestCode() {
            return 0;
        }

        @Override
        public int getViewType() {
            return viewType;
        }

        @Override
        public String getRepresentativeText() {
            return representativeText;
        }

        @Override
        public void setRepresentativeText(CharSequence representativeText) {
            this.representativeText = String.valueOf(representativeText);
        }

        public boolean isGroupRepresentative() {
            return representativeText != null;
        }

        @Override
        public void setDate(long date) {
            this.date = date;
        }

        @Override
        public void setSize(long size) {
            this.size = size;
        }

        @Override
        public boolean setSelectableSelected(boolean selected) {
            return !isGroupRepresentative() && super.setSelectableSelected(selected);
        }

        @Override
        public boolean searchMatches(String searchWord) {
            if (isGroupRepresentative())
                return TextUtils.searchWord(representativeText, searchWord);

            return super.searchMatches(searchWord);
        }
    }