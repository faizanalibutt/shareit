package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.code4rox.adsmanager.AdmobUtils;
import com.code4rox.adsmanager.NativeAdsIdType;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.GlideApp;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.app.EditableListAdapter;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.exception.NotReadyException;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.Shareable;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.FileUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.NetworkUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.TinyDB;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.widget.GroupEditableListAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ApplicationListAdapter
        extends EditableListAdapter<ApplicationListAdapter.PackageHolder, EditableListAdapter.EditableViewHolder> {
    private SharedPreferences mPreferences;
    private PackageManager mManager;

    public ApplicationListAdapter(Context context, SharedPreferences preferences) {
        super(context);
        mPreferences = preferences;
        mManager = context.getPackageManager();
    }

    @Override
    public List<PackageHolder> onLoad() {
        List<PackageHolder> list = new ArrayList<>();

        boolean showSystemApps = mPreferences.getBoolean("show_system_apps", false);

        for (PackageInfo packageInfo : getContext().getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA)) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;

            if (list.size() == 4 && getContext() != null && NetworkUtils.isOnline(getContext())
                    && !TinyDB.getInstance(getContext()).getBoolean(getContext().getString(R.string.is_premium)))
                list.add(new AdsModel());
            else if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1 || showSystemApps) {
                PackageHolder packageHolder = new PackageHolder(appInfo.loadLabel(mManager).toString(),
                        appInfo,
                        packageInfo.versionName,
                        packageInfo.packageName,
                        new File(appInfo.sourceDir),
                        FileUtils.sizeExpression(new File(appInfo.sourceDir).length(), false));

                if (filterItem(packageHolder))
                    list.add(packageHolder);
            }
        }

        if (getContext() != null && NetworkUtils.isOnline(getContext())
                && !TinyDB.getInstance(getContext()).getBoolean(getContext().getString(R.string.is_premium))) {
            if (list.size() == 0)
                list.add(new AdsModel());
            else if (list.size() < 4) {
                int size = list.size();
                list.add(size, new AdsModel());
            }
        }

        //Collections.sort(list, getDefaultComparator());

        return list;
    }

    @NonNull
    @Override
    public EditableListAdapter.EditableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == GroupEditableListAdapter.VIEW_TYPE_ADS_GRID)
            return new EditableListAdapter.EditableViewHolder(getInflater().inflate(R.layout.ad_unified_4_ext, parent, false));

        return new EditableListAdapter.EditableViewHolder(getInflater().inflate(R.layout.apps_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final EditableListAdapter.EditableViewHolder holder, final int position) {
        try {
            if (holder.getItemViewType() == GroupEditableListAdapter.VIEW_TYPE_ADS_GRID
                    && NetworkUtils.isOnline(getContext())) {

                AdmobUtils admobUtils = new AdmobUtils(holder.getView().getContext());
                admobUtils.loadNativeAd((FrameLayout) holder.getView(),
                        R.layout.ad_unified_4_ext, NativeAdsIdType.ADJUST_NATIVE_AM);
                admobUtils.setNativeAdListener(new AdmobUtils.NativeAdListener() {
                    @Override
                    public void onNativeAdLoaded() {

                    }

                    @Override
                    public void onNativeAdError() {

                    }
                });
            } else {
                final View parentView = holder.getView();
                final PackageHolder object = getItem(position);
                ImageView image = parentView.findViewById(R.id.appIcon);
                TextView text1 = parentView.findViewById(R.id.appName);
                TextView text2 = parentView.findViewById(R.id.appSize);

                text1.setText(object.friendlyName);
                text2.setText(object.appSize);

                parentView.setSelected(object.isSelectableSelected());

                GlideApp.with(getContext())
                        .load(object.appInfo)
                        .override(160)
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
                    ? ((AdsModel) getItem(position)).getViewType()
                    : super.getItemViewType(position);
        } catch (NotReadyException | ClassCastException e) {
            e.printStackTrace();
            return VIEW_TYPE_DEFAULT;
        }
    }

    public static class PackageHolder extends Shareable {
        public static final String FORMAT = ".apk";
        public static final String MIME_TYPE = FileUtils.getFileContentType(FORMAT);

        public ApplicationInfo appInfo;
        public String version;
        public String packageName;
        public String appSize;

        public PackageHolder() {
        }

        public PackageHolder(String friendlyName, ApplicationInfo appInfo, String version,
                             String packageName, File executableFile, String appSize) {
            super(appInfo.packageName.hashCode(),
                    friendlyName,
                    friendlyName + "_" + version + ".apk",
                    MIME_TYPE,
                    executableFile.lastModified(),
                    executableFile.length(),
                    Uri.fromFile(executableFile));

            this.appInfo = appInfo;
            this.version = version;
            this.packageName = packageName;
            this.appSize = appSize;
        }

    }

    public static class AdsModel extends PackageHolder {

        public int viewType;

        public AdsModel() {
            this.viewType = GroupEditableListAdapter.VIEW_TYPE_ADS_GRID;
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
