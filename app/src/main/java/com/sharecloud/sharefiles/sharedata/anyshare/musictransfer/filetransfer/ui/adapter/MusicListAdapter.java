package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.code4rox.adsmanager.AdmobUtils;
import com.code4rox.adsmanager.NativeAdsIdType;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.EditableListAdapter;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.exception.NotReadyException;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.Shareable;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.FileUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.NetworkUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.TimeUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.widget.GroupEditableListAdapter;

import java.util.ArrayList;
import java.util.List;

public class MusicListAdapter
        extends EditableListAdapter<MusicListAdapter.SongHolder, EditableListAdapter.EditableViewHolder> {

    private ContentResolver mResolver;

    public MusicListAdapter(Context context) {
        super(context);
        mResolver = context.getContentResolver();
    }

    @Override
    public List<SongHolder> onLoad() {

        List<SongHolder> audioList = new ArrayList<>();

        Cursor songCursor = mResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Audio.Media.IS_MUSIC + "=?",
                new String[]{String.valueOf(1)},
                null);

        if (songCursor != null) {
            if (songCursor.moveToFirst()) {

                int idIndex = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int artistIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int songIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int nameIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                int dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
                int sizeIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
                int typeIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);

                do {
                    if (audioList.size() == 4 && NetworkUtils.isOnline(getContext()))
                        audioList.add(new AdsModel());
                    else {
                        audioList.add(new SongHolder(
                                songCursor.getLong(idIndex),
                                songCursor.getString(nameIndex),
                                songCursor.getString(artistIndex),
                                songCursor.getString(songIndex),
                                songCursor.getString(typeIndex),
                                songCursor.getLong(dateIndex) * 1000,
                                songCursor.getLong(sizeIndex),
                                Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + songCursor.getInt(idIndex)),
                                FileUtils.sizeExpression(songCursor.getLong(sizeIndex), false) + ", " +
                                        TimeUtils.INSTANCE.formatDateTime(getContext(), songCursor.getLong(dateIndex) * 1000)));
                    }
                }
                while (songCursor.moveToNext());
            }

            songCursor.close();

            if (NetworkUtils.isOnline(getContext())) {
                if (audioList.size() == 0)
                    audioList.add(new AdsModel());
                else if (audioList.size() < 4) {
                    int size = audioList.size();
                    audioList.add(size, new AdsModel());
                }
            }

        }

        //Collections.sort(audioList, getDefaultComparator());

        return audioList;
    }

    @NonNull
    @Override
    public EditableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == GroupEditableListAdapter.VIEW_TYPE_ADS_LINEAR)
            return new EditableViewHolder(getInflater().inflate(R.layout.ad_unified_5, parent, false));

        return new EditableViewHolder(getInflater().inflate(R.layout.music_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EditableViewHolder holder, int position) {
        try {

            if (holder.getItemViewType() == GroupEditableListAdapter.VIEW_TYPE_ADS_LINEAR) {
                AdmobUtils admobUtils = new AdmobUtils(holder.getView().getContext());
                admobUtils.loadNativeAd((FrameLayout) holder.getView(),
                        R.layout.ad_unified_5, NativeAdsIdType.ADJUST_NATIVE_AM);
                admobUtils.setNativeAdListener(new AdmobUtils.NativeAdListener() {
                    @Override
                    public void onNativeAdLoaded() {

                    }

                    @Override
                    public void onNativeAdError() {

                    }
                });
            } else {
                final SongHolder object = getItem(position);
                final View parentView = holder.getView();

                TextView text1 = parentView.findViewById(R.id.audioTitle);
                TextView text2 = parentView.findViewById(R.id.audioSize);
                text1.setText(object.song);
                text2.setText(object.datesize);

                parentView.setSelected(object.isSelectableSelected());
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

    public static class SongHolder extends Shareable {

        public String datesize;
        public String song;

        public SongHolder() {}

        public SongHolder(long id, String displayName, String artist, String song, String mimeType, long date, long size, Uri uri, String datesize) {
            super(id, song + " - " + artist, displayName, mimeType, date, size, uri);
            this.datesize = datesize;
            this.song = song;
        }

    }

    public static class AdsModel extends SongHolder {

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
