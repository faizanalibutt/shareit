package com.hazelmobile.filetransfer.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.callback.Callback;
import com.hazelmobile.filetransfer.util.AppUtils;
import com.hazelmobile.filetransfer.app.EditableListAdapter;
import com.hazelmobile.filetransfer.app.EditableListFragment;
import com.hazelmobile.filetransfer.ui.adapter.VideoListAdapter;
import com.hazelmobile.filetransfer.ui.callback.TitleSupport;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class VideoListFragment
        extends EditableListFragment<VideoListAdapter.VideoHolder, EditableListAdapter.EditableViewHolder, VideoListAdapter>
        implements TitleSupport {

    private TextView videoSize;
    private CheckBox selectAll;

    public CheckBox getChckBox() {
        return selectAll;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        videoSize = view.findViewById(R.id.myVideosText);

        setEmptyImage(R.drawable.ic_video_library_white_24dp);
        setEmptyText(getString(R.string.text_listEmptyVideo));

        selectAll = view.findViewById(R.id.selectAll);
        selectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                new SelectionCallback<>(isChecked, VideoListFragment.this);
                if (isChecked) {
                    Callback.setColor(true);
                }
            }
        });


        final Observer<Boolean> selectObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean select) {
                if (select != null && !select) {
                    selectAll.setChecked(false);
                }
            }
        };
        Callback.getColor().observe(VideoListFragment.this, selectObserver);


    }

    @Override
    public void onResume() {
        super.onResume();

        Objects.requireNonNull(getContext()).getContentResolver()
                .registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, getDefaultContentObserver());
    }

    @Override
    public void onPause() {
        super.onPause();

        Objects.requireNonNull(getContext()).getContentResolver()
                .unregisterContentObserver(getDefaultContentObserver());
    }

    @Override
    public boolean onSetListAdapter(VideoListAdapter adapter) {
        if (super.onSetListAdapter(adapter)) {
            videoSize.setText(getApplicationListSize(adapter));
            return true;
        } else
            return false;
    }

    private String getApplicationListSize(VideoListAdapter adapter) {
        return "Video(" + adapter.onLoad().size() + ")";
    }

    @Override
    public VideoListAdapter onAdapter() {
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

        return new VideoListAdapter(getActivity()) {
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
        return context.getString(R.string.text_video);
    }

    @Override
    protected RecyclerView onListView(View mainContainer, ViewGroup listViewContainer) {

        View adaptedView = getLayoutInflater().inflate(R.layout.video_list_fragment, null, false);
        listViewContainer.addView(adaptedView);

        return super.onListView(mainContainer, (ViewGroup) adaptedView.findViewById(R.id.videosList));
    }
}
