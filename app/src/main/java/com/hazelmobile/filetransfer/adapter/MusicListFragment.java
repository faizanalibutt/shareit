package com.hazelmobile.filetransfer.adapter;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.pictures.AppUtils;
import com.hazelmobile.filetransfer.pictures.EditableListAdapter;
import com.hazelmobile.filetransfer.pictures.EditableListFragment;
import com.hazelmobile.filetransfer.util.callback.TitleSupport;

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

        setEmptyImage(0);
        setEmptyText("");
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
                                    SelectionCallbackGlobal.setColor(true);
                                } else {
                                    SelectionCallbackGlobal.setColor(false);
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
            SelectionCallbackGlobal.setColor(true);
            return getSelectionConnection().setSelected(holder);
        } else {
            SelectionCallbackGlobal.setColor(false);
            return performLayoutClickOpen(holder);
        }
    }

    @Override
    public CharSequence getTitle(Context context) {
        return context.getString(R.string.text_music);
    }
}
