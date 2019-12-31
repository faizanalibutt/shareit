package com.hazelmobile.filetransfer.ui.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.util.FileUtils;
import com.hazelmobile.filetransfer.app.EditableListAdapter;
import com.hazelmobile.filetransfer.object.Shareable;
import com.hazelmobile.filetransfer.util.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicListAdapter
        extends EditableListAdapter<MusicListAdapter.SongHolder, EditableListAdapter.EditableViewHolder> {

    private ContentResolver mResolver;

    public MusicListAdapter(Context context) {
        super(context);
        mResolver = context.getContentResolver();
    }

    @Override
    public List<MusicListAdapter.SongHolder> onLoad() {

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
                while (songCursor.moveToNext());
            }

            songCursor.close();
        }

        Collections.sort(audioList, getDefaultComparator());

        return audioList;
    }

    @NonNull
    @Override
    public EditableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EditableViewHolder(getInflater().inflate(R.layout.music_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EditableViewHolder holder, int position) {
        try {

            final SongHolder object = getItem(position);
            final View parentView = holder.getView();

            TextView text1 = parentView.findViewById(R.id.audioTitle);
            TextView text2 = parentView.findViewById(R.id.audioSize);
            text1.setText(object.song);
            text2.setText(object.datesize);

            parentView.setSelected(object.isSelectableSelected());

        } catch (Exception e) {

        }
    }

    public static class SongHolder extends Shareable {

        public String datesize;
        public String song;

        public SongHolder(long id, String displayName, String artist, String song, String mimeType, long date, long size, Uri uri, String datesize) {
            super(id, song + " - " + artist, displayName, mimeType, date, size, uri);
            this.datesize = datesize;
            this.song = song;
        }
    }


}
