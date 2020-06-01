package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.callback;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.code4rox.adsmanager.TinyDB;
import com.genonbeta.android.framework.widget.PowerfulActionMode;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.EditableListFragment;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.EditableListFragmentImpl;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.config.Keyword;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.Shareable;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity.PreparationsActivity;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.activity.ShareActivity;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.fragment.ShareableListFragment;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.LogUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.widget.EditableListAdapterImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * created by: veli
 * date: 14/04/18 15:59
 */
public class SharingActionModeCallback<T extends Shareable> extends EditableListFragment.SelectionCallback<T> {
    public SharingActionModeCallback(EditableListFragmentImpl<T> fragment) {
        super(fragment);
    }

    @Override
    public boolean onPrepareActionMenu(Context context, PowerfulActionMode actionMode) {
        super.onPrepareActionMenu(context, actionMode);
        return true;
    }

    @Override
    public boolean onCreateActionMenu(Context context, PowerfulActionMode actionMode, Menu menu) {
        super.onCreateActionMenu(context, actionMode, menu);
        actionMode.getMenuInflater().inflate(R.menu.action_mode_share, menu);
        return true;
    }

    @Override
    public boolean onActionMenuItemSelected(Context context, PowerfulActionMode actionMode, MenuItem item) {
        int id = item.getItemId();

        List<T> selectedItemList = new ArrayList<>(getFragment().getSelectionConnection().getSelectedItemList());

        if (selectedItemList.size() > 0
                 && (id == R.id.action_mode_share_trebleshot || id == R.id.action_mode_share_all_apps)) {
            Intent shareIntent = new Intent()
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .setAction((selectedItemList.size() > 1 ? ShareActivity.ACTION_SEND_MULTIPLE : ShareActivity.ACTION_SEND)/*(false)
                            ? (selectedItemList.size() > 1 ? Intent.ACTION_SEND_MULTIPLE : Intent.ACTION_SEND)
                            : (selectedItemList.size() > 1 ? ShareActivity.ACTION_SEND_MULTIPLE : ShareActivity.ACTION_SEND)*/);

            if (selectedItemList.size() > 1) {
                ShareableListFragment.MIMEGrouper mimeGrouper = new ShareableListFragment.MIMEGrouper();
                ArrayList<Uri> uriList = new ArrayList<>();
                ArrayList<CharSequence> nameList = new ArrayList<>();

                for (T sharedItem : selectedItemList) {
                    uriList.add(sharedItem.uri);
                    nameList.add(sharedItem.fileName);

                    if (!mimeGrouper.isLocked())
                        mimeGrouper.process(sharedItem.mimeType);
                }

                shareIntent.setType(mimeGrouper.toString())
                        .putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
                        .putCharSequenceArrayListExtra(ShareActivity.EXTRA_FILENAME_LIST, nameList)
                        .putExtra(Keyword.EXTRA_SEND, true);
            } else if (selectedItemList.size() == 1) {
                T sharedItem = selectedItemList.get(0);

                shareIntent.setType(sharedItem.mimeType)
                        .putExtra(Intent.EXTRA_STREAM, sharedItem.uri)
                        .putExtra(ShareActivity.EXTRA_FILENAME_LIST, sharedItem.fileName)
                        .putExtra(Keyword.EXTRA_SEND, true);
            }

            try {
                getFragment().getContext().startActivity(shareIntent/*(false)*//*item.getItemId() == R.id.action_mode_share_all_apps*//*
                        ? Intent.createChooser(shareIntent, getFragment().getContext().getString(R.string.text_fileShareAppChoose))
                        : shareIntent*/);
            } catch (Throwable e) {
                e.printStackTrace();
                Toast.makeText(getFragment().getActivity(), R.string.mesg_somethingWentWrong, Toast.LENGTH_SHORT).show();

                return false;
            }
        } else
            return super.onActionMenuItemSelected(context, actionMode, item);

        return true;
    }

    public boolean sendFiles() {
        List<T> selectedItemList = new ArrayList<>(getFragment().getSelectionConnection().getSelectedItemList());
        LogUtils.getLogTask("Sharing", String.format("sendFiles(): selection list size is: %s", selectedItemList.size()));

        if (selectedItemList.size() > 0) {
            Intent shareIntent;
            if (selectedItemList.size() < 1000)
            {
                shareIntent = new Intent()
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .setAction((selectedItemList.size() > 1 ? ShareActivity.ACTION_SEND_MULTIPLE : ShareActivity.ACTION_SEND));
            } else {
                shareIntent = new Intent(getFragment().getContext(), PreparationsActivity.class);
            }

            if (selectedItemList.size() > 1) {
                ShareableListFragment.MIMEGrouper mimeGrouper = new ShareableListFragment.MIMEGrouper();
                ArrayList<String> uriLists = new ArrayList<>();
                ArrayList<Uri> uriList = new ArrayList<>();
                ArrayList<CharSequence> nameList = new ArrayList<>();

                for (T sharedItem : selectedItemList) {
                    uriList.add(sharedItem.uri);
                    uriLists.add(sharedItem.uri.toString());
                    nameList.add(sharedItem.fileName);

                    if (!mimeGrouper.isLocked())
                        mimeGrouper.process(sharedItem.mimeType);
                }

                if (uriList.size() < 1000) {
                    shareIntent.setType(mimeGrouper.toString())
                            .putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
                            .putCharSequenceArrayListExtra(ShareActivity.EXTRA_FILENAME_LIST, nameList)
                            .putExtra(Keyword.EXTRA_SEND, true);
                } else {
                    TinyDB tinyDB = TinyDB.getInstance(getFragment().getContext());
                    tinyDB.putListString("selection_list_uri", uriLists);
                    tinyDB.putListChar("selection_list_nameList", nameList);
                    shareIntent.putExtra(Keyword.EXTRA_SEND, true);
                }
            } else if (selectedItemList.size() == 1) {
                T sharedItem = selectedItemList.get(0);

                shareIntent.setType(sharedItem.mimeType)
                        .putExtra(Intent.EXTRA_STREAM, sharedItem.uri)
                        .putExtra(ShareActivity.EXTRA_FILENAME_LIST, sharedItem.fileName)
                        .putExtra(Keyword.EXTRA_SEND, true);
            }

            try {
                if (selectedItemList.size() < 1000)
                    getFragment().getContext().startActivity(shareIntent);
                else
                    getFragment().getContext().startActivity(shareIntent);
            } catch (Throwable e) {
                e.printStackTrace();
                Toast.makeText(getFragment().getActivity(), R.string.mesg_somethingWentWrong, Toast.LENGTH_SHORT).show();

                return false;
            }
        } else
            return false;


        return true;
    }

    public static class SelectionDuo<T extends Shareable> {
        private EditableListFragmentImpl<T> mFragment;
        private EditableListAdapterImpl<T> mAdapter;

        public SelectionDuo(EditableListFragmentImpl<T> fragment, EditableListAdapterImpl<T> adapter) {
            mFragment = fragment;
            mAdapter = adapter;
        }

        public EditableListAdapterImpl<T> getAdapter() {
            return mAdapter;
        }

        public EditableListFragmentImpl<T> getFragment() {
            return mFragment;
        }
    }
}
