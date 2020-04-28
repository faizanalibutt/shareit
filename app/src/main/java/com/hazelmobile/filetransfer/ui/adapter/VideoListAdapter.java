package com.hazelmobile.filetransfer.ui.adapter;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.code4rox.adsmanager.AdmobUtils;
import com.code4rox.adsmanager.NativeAdsIdType;
import com.hazelmobile.filetransfer.GlideApp;
import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.exception.NotReadyException;
import com.hazelmobile.filetransfer.util.FileUtils;
import com.hazelmobile.filetransfer.app.EditableListAdapter;
import com.hazelmobile.filetransfer.object.Shareable;
import com.hazelmobile.filetransfer.util.NetworkUtils;
import com.hazelmobile.filetransfer.util.TimeUtils;
import com.hazelmobile.filetransfer.widget.GroupEditableListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * created by: Veli
 * date: 18.11.2017 13:32
 */

public class VideoListAdapter
        extends EditableListAdapter<VideoListAdapter.VideoHolder, EditableListAdapter.EditableViewHolder> {

    private ContentResolver mResolver;

    public VideoListAdapter(Context context) {
        super(context);
        mResolver = context.getContentResolver();
    }

    @NonNull
    @Override
    public EditableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == GroupEditableListAdapter.VIEW_TYPE_ADS_LINEAR)
            return new EditableViewHolder(getInflater().inflate(R.layout.ad_unified_6, parent, false));

        return new EditableViewHolder(getInflater().inflate(R.layout.video_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EditableViewHolder holder, int position) {
        try {

            if (holder.getItemViewType() == GroupEditableListAdapter.VIEW_TYPE_ADS_LINEAR) {
                AdmobUtils admobUtils = new AdmobUtils(holder.getView().getContext());
                admobUtils.loadNativeAd((FrameLayout) holder.getView(),
                        R.layout.ad_unified_6, NativeAdsIdType.ADJUST_NATIVE_AM);
                admobUtils.setNativeAdListener(new AdmobUtils.NativeAdListener() {
                    @Override
                    public void onNativeAdLoaded() {

                    }

                    @Override
                    public void onNativeAdError() {

                    }
                });
            } else {
                final VideoHolder object = this.getItem(position);
                final View parentView = holder.getView();

                ImageView image = parentView.findViewById(R.id.videoThumbnail);
                TextView text1 = parentView.findViewById(R.id.videoTitle);
                TextView text2 = parentView.findViewById(R.id.videoDuration);
                TextView text3 = parentView.findViewById(R.id.videoSize);

                text1.setText(object.friendlyName);
                text2.setText(object.duration);
                text3.setText(FileUtils.sizeExpression(object.size, false));

                parentView.setSelected(object.isSelectableSelected());

                GlideApp.with(getContext())
                        .load(object.uri)
                        .centerCrop()
                        .into(image);
            }

        } catch (Exception e) {
        }
    }

    @Override
    public int getItemViewType(int position) {
        try {
            return getItem(position) instanceof AdsModel
                    ? ((AdsModel)getItem(position)).getViewType()
                    : super.getItemViewType(position);
        } catch (NotReadyException | ClassCastException e) {
            e.printStackTrace();
            return VIEW_TYPE_DEFAULT;
        }
    }

    @Override
    public List<VideoHolder> onLoad() {

        List<VideoHolder> videoList = new ArrayList<>();

        Cursor cursor = mResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                int titleIndex = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
                int displayIndex = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
                //int albumIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                int lengthIndex = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
                int dateIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED);
                int sizeIndex = cursor.getColumnIndex(MediaStore.Video.Media.SIZE);
                int typeIndex = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE);

                do {
                    if (videoList.size() == 4 && NetworkUtils.isOnline(getContext()))
                        videoList.add(new AdsModel());
                    else {
                        videoList.add(new VideoHolder(
                                cursor.getInt(idIndex),
                                cursor.getString(titleIndex),
                                cursor.getString(displayIndex),
                                cursor.getString(typeIndex),
                                cursor.getLong(lengthIndex),
                                cursor.getLong(dateIndex) * 1000,
                                cursor.getLong(sizeIndex),
                                Uri.parse(MediaStore.Video.Media.EXTERNAL_CONTENT_URI + "/" + cursor.getInt(idIndex))));
                    }
                }
                while (cursor.moveToNext());
            }

            cursor.close();

            if (videoList.size() == 0)
                videoList.add(new AdsModel());
            else if (videoList.size() < 4) {
                int size = videoList.size();
                videoList.add(size, new AdsModel());
            }

        }

        //Collections.sort(videoList, getDefaultComparator());

        return videoList;
    }


    public static class VideoHolder extends Shareable {
        public String duration;

        public VideoHolder() {}

        public VideoHolder(long id, String friendlyName, String fileName, String mimeType, long duration, long date, long size, Uri uri) {
            super(id, friendlyName, fileName, mimeType, date, size, uri);
            this.duration = TimeUtils.INSTANCE.getDuration(duration);
        }

    }

    public static class AdsModel extends VideoHolder {

        public int viewType;

        public AdsModel() {
            this.viewType = GroupEditableListAdapter.VIEW_TYPE_ADS_LINEAR;
        }

        public int getViewType() {
            return viewType;
        }

        @Override
        public String getComparableName() {
            return "ADS_VIEW";
        }

        @Override
        public boolean comparisonSupported() {
            return getViewType() != GroupEditableListAdapter.VIEW_TYPE_ADS_LINEAR && super.comparisonSupported();
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
