package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.miscpkg;

import android.net.Uri;

public class GalleryGroupShareable extends GroupShareable {
        public String albumName;

        public GalleryGroupShareable() {
            super();
        }

        public GalleryGroupShareable(int viewType, String representativeText) {
            super(viewType, representativeText);
        }

        public GalleryGroupShareable(long id, String friendlyName, String fileName, String albumName, String mimeType, long date, long size, Uri uri) {
            super(id, friendlyName, fileName, mimeType, date, size, uri);
            this.albumName = albumName;
        }
    }