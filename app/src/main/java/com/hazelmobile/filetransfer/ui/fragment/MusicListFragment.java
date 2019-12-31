package com.hazelmobile.filetransfer.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.callback.Callback;
import com.hazelmobile.filetransfer.util.AppUtils;
import com.hazelmobile.filetransfer.app.EditableListAdapter;
import com.hazelmobile.filetransfer.app.EditableListFragment;
import com.hazelmobile.filetransfer.ui.adapter.MusicListAdapter;
import com.hazelmobile.filetransfer.ui.callback.TitleSupport;

import org.jetbrains.annotations.NotNull;

public class MusicListFragment
        extends EditableListFragment<MusicListAdapter.SongHolder, EditableListAdapter.EditableViewHolder, MusicListAdapter>
        implements TitleSupport {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setEmptyImage(R.drawable.ic_library_music_white_24dp);
        setEmptyText(getString(R.string.text_listEmptyMusic));
    }

    @Override
    public void onResume() {
        super.onResume();

        getContext().getContentResolver()
                .registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, getDefaultContentObserver());
    }

    @Override
    public void onPause() {
        super.onPause();

        getContext()
                .getContentResolver()
                .unregisterContentObserver(getDefaultContentObserver());
    }

    @Override
    public MusicListAdapter onAdapter() {
        final AppUtils.QuickActions<EditableListAdapter.EditableViewHolder> quickActions = new AppUtils.QuickActions<EditableListAdapter.EditableViewHolder>() {
            @Override
            public void onQuickActions(final EditableListAdapter.EditableViewHolder clazz) {
                registerLayoutViewClicks(clazz);

                clazz.getView().findViewById(R.id.selector).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (getSelectionConnection() != null) {
                                    getSelectionConnection().setSelected(clazz.getAdapterPosition());
                                    Callback.setColor(true);
                                } else {
                                    Callback.setColor(false);
                                }
                            }
                        });
            }
        };

        return new MusicListAdapter(getActivity()) {
            @NonNull
            @Override
            public EditableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return AppUtils.quickAction(super.onCreateViewHolder(parent, viewType), quickActions);
            }
        };
    }

    @Override
    public boolean onDefaultClickAction(EditableListAdapter.EditableViewHolder holder) {
        if (getSelectionConnection() != null) {
            Callback.setColor(true);
            return getSelectionConnection().setSelected(holder);
        } else {
            Callback.setColor(false);
            return performLayoutClickOpen(holder);
        }
    }

    @NotNull
    @Override
    public CharSequence getTitle(Context context) {
        return context.getString(R.string.text_music);
    }
}
