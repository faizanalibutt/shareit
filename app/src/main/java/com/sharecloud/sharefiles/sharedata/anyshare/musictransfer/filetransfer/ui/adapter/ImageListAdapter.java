package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.GlideApp;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.miscpkg.GalleryGroupShareable;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.NetworkUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.TimeUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.TinyDB;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.widget.GalleryGroupEditableListAdapter;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.widget.GroupEditableListAdapter;

;
//import com.code4rox.adsmanager.NativeAdsIdType;

/**
 * created by: Veli
 * date: 18.11.2017 13:32
 */

public class ImageListAdapter
        extends GalleryGroupEditableListAdapter<ImageListAdapter.ImageHolder, GroupEditableListAdapter.GroupViewHolder> {
    private ContentResolver mResolver;
    private int mSelectedInset;

    public ImageListAdapter(Context context) {
        super(context, MODE_GROUP_BY_ALBUM);
        mResolver = context.getContentResolver();
        mSelectedInset = (int) context.getResources().getDimension(R.dimen.space_list_grid);
    }

    @Override
    protected void onLoad(GroupLister<ImageHolder> lister) throws Exception {
        Cursor cursor = mResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                int titleIndex = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
                int displayIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int albumIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int dateAddedIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                int sizeIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int typeIndex = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);

                do {
                    ImageHolder holder = new ImageHolder(
                            cursor.getLong(idIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(displayIndex),
                            cursor.getString(albumIndex),
                            cursor.getString(typeIndex),
                            cursor.getLong(dateAddedIndex) * 1000,
                            cursor.getLong(sizeIndex),
                            Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + cursor.getInt(idIndex)));

                    holder.dateTakenString = TimeUtils.INSTANCE.getFriendlyElapsedTime(getContext(), holder.date);

                    lister.offerObliged(this, holder);
                }
                while (cursor.moveToNext());
            }

            cursor.close();
            if (getContext() != null && NetworkUtils.isOnline(getContext())
                    && !TinyDB.getInstance(getContext()).getBoolean(getContext().getString(R.string.is_premium)))
                lister.offerObliged(this, new AdsModel());
        }
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_REPRESENTATIVE)
            return new GroupViewHolder(getInflater().inflate(R.layout.layout_list_title, parent, false), R.id.layout_list_title_text);
       /* else if (viewType == VIEW_TYPE_ADS_GRID)
            return new GroupViewHolder(getInflater().inflate(R.layout.ad_unified_7, parent, false), R.id.ad_call_to_action);*/

        return new GroupViewHolder(getInflater().inflate(isGridLayoutRequested()
                ? R.layout.list_image_grid_ext
                : R.layout.list_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        try {
            final View parentView = holder.getView();
            final ImageHolder object = getItem(position);

            if (!holder.tryBinding(object)) {
                ViewGroup container = parentView.findViewById(R.id.container);
                ImageView image = parentView.findViewById(R.id.image);
                TextView text1 = parentView.findViewById(R.id.text);
                TextView text2 = parentView.findViewById(R.id.text2);

                text1.setText(object.friendlyName);
                text2.setText(object.dateTakenString);

                parentView.setSelected(object.isSelectableSelected());

                GlideApp.with(getContext())
                        .load(object.uri)
                        .override(300)
                        .centerCrop()
                        .into(image);
            } else if (holder.getItemViewType() == GroupEditableListAdapter.VIEW_TYPE_ADS_GRID) {
                /*AdmobUtils admobUtils = new AdmobUtils(holder.getView().getContext());
                admobUtils.loadNativeAd((FrameLayout) holder.getView(),
                        R.layout.ad_unified_7, NativeAdsIdType.ADJUST_NATIVE_AM);
                admobUtils.setNativeAdListener(new AdmobUtils.NativeAdListener() {
                    @Override
                    public void onNativeAdLoaded() {

                    }

                    @Override
                    public void onNativeAdError() {

                    }
                });*/
            }
        } catch (Exception e) {

        }
    }

    /*@Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads);
        else {
            try {
                if (holder.getView() != null && payloads.get(0) != null)
                    holder.setSelected(((boolean) payloads.get(0)));
            } catch (Exception e) {
                Log.e("NEW_ONBIND", ":::", e);
            }
        }
    }*/

    @Override
    protected ImageHolder onGenerateRepresentative(String representativeText) {
        return new ImageHolder(representativeText);
    }

    @Override
    public boolean isGridSupported() {
        return true;
    }

    public static class ImageHolder extends GalleryGroupShareable {
        public String dateTakenString;

        public ImageHolder() {
        }

        public ImageHolder(String representativeText) {
            super(VIEW_TYPE_REPRESENTATIVE, representativeText);
        }

        public ImageHolder(long id, String title, String fileName, String albumName, String mimeType, long date, long size, Uri uri) {
            super(id, title, fileName, albumName, mimeType, date, size, uri);
        }
    }

    public static class AdsModel extends ImageHolder {

        public int viewType;

        public AdsModel() {
            this.viewType = GroupEditableListAdapter.VIEW_TYPE_ADS_GRID;
            albumName = "Camera";
        }

        @Override
        public String getRepresentativeText() {
            return "ADS_VIEW";
        }

        @Override
        public int getViewType() {
            return viewType;
        }

        @Override
        public boolean isGroupRepresentative() {
            return false;
        }

        @Override
        public long getComparableDate() {
            return System.currentTimeMillis();
        }

        @Override
        public String getComparableName() {
            return "ADS";
        }

        @Override
        public boolean comparisonSupported() {
            return getViewType() != GroupEditableListAdapter.VIEW_TYPE_ADS_GRID && super.comparisonSupported();
        }

        @Override
        public boolean isSelectableSelected() {
            return false;
        }

        // Don't let ADS to be selected
        @Override
        public boolean setSelectableSelected(boolean selected) {
            return false;
        }
    }
}
