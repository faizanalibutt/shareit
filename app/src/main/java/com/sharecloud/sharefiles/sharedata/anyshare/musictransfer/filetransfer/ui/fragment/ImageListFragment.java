package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.fragment;

/**
 * Created by gabm on 11/01/18.
 */

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.callback.Callback;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.GalleryGroupEditableListFragment;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.adapter.ImageListAdapter;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.callback.TitleSupport;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.AppUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.widget.GroupEditableListAdapter;

import org.jetbrains.annotations.NotNull;

public class ImageListFragment
        extends GalleryGroupEditableListFragment<ImageListAdapter.ImageHolder, GroupEditableListAdapter.GroupViewHolder, ImageListAdapter>
        implements TitleSupport {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFilteringSupported(true);
        setDefaultOrderingCriteria(ImageListAdapter.MODE_SORT_ORDER_DESCENDING);
        setDefaultSortingCriteria(ImageListAdapter.MODE_SORT_BY_DATE);
        setDefaultViewingGridSize(3, 4);
        setUseDefaultPaddingDecoration(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setEmptyImage(R.drawable.ic_photo_white_24dp);
        setEmptyText(getString(R.string.text_listEmptyImage));
    }

    @Override
    public void onResume() {
        super.onResume();

        getContext().getContentResolver()
                .registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, getDefaultContentObserver());
    }

    @Override
    public void onPause() {
        super.onPause();

        getContext().getContentResolver()
                .unregisterContentObserver(getDefaultContentObserver());
    }

    @Override
    public ImageListAdapter onAdapter() {
        final AppUtils.QuickActions<GroupEditableListAdapter.GroupViewHolder> quickActions =
                new AppUtils.QuickActions<GroupEditableListAdapter.GroupViewHolder>() {
            @Override
            public void onQuickActions(final GroupEditableListAdapter.GroupViewHolder clazz) {
                if (!clazz.isRepresentative()) {
                    registerLayoutViewClicks(clazz);

                    View visitView = clazz.getView().findViewById(R.id.visitView);
                    visitView.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (getSelectionConnection() != null) {
                                        getSelectionConnection().setSelected(clazz.getAdapterPosition());
                                        Callback.setColor(true);
                                    } else{
                                        performLayoutClick(clazz);
                                        Callback.setColor(false);
                                    }

                                }
                            });

                    visitView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            performLayoutClickOpen(clazz);
                            return true;
                        }
                    });

                    clazz.getView().findViewById(getAdapter().isGridLayoutRequested()
                            ? R.id.selectorContainer : R.id.selector)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (getSelectionConnection() != null) {
                                        getSelectionConnection().setSelected(clazz.getAdapterPosition());
                                        Callback.setColor(true);
                                    } else
                                        Callback.setColor(false);
                                }
                            });
                }
            }
        };

        return new ImageListAdapter(getActivity()) {
            @NonNull
            @Override
            public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                GroupViewHolder holder = super.onCreateViewHolder(parent, viewType);

                if (viewType == GroupEditableListAdapter.VIEW_TYPE_ADS_GRID) {
                    registerLayoutViewClicks(holder);
                    return holder;
                }

                return AppUtils.quickAction(super.onCreateViewHolder(parent, viewType), quickActions);
            }
        };
    }

    @Override
    public boolean onDefaultClickAction(GroupEditableListAdapter.GroupViewHolder holder) {
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
        return context.getString(R.string.text_photo);
    }

    @Override
    protected RecyclerView onListView(View mainContainer, ViewGroup listViewContainer) {

        View adaptedView = getLayoutInflater().inflate(R.layout.fragment_image_list, null, false);
        listViewContainer.addView(adaptedView);

        return super.onListView(mainContainer, (ViewGroup) adaptedView.findViewById(R.id.imagesList));
    }
}
