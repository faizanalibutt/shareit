package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.miscpkg;

import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.Editable;

public interface GroupEditable extends Editable {
        int getViewType();

        int getRequestCode();

        String getRepresentativeText();

        void setRepresentativeText(CharSequence representativeText);

        boolean isGroupRepresentative();

        void setDate(long date);

        void setSize(long size);

    }