package com.hazelmobile.filetransfer.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hazelmobile.filetransfer.GlideApp;
import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.files.FileUtils;
import com.hazelmobile.filetransfer.pictures.EditableListAdapter;
import com.hazelmobile.filetransfer.pictures.Shareable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1 || showSystemApps) {
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

        Collections.sort(list, getDefaultComparator());

        return list;
    }

    @NonNull
    @Override
    public EditableListAdapter.EditableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EditableListAdapter.EditableViewHolder(getInflater().inflate(R.layout.apps_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final EditableListAdapter.EditableViewHolder holder, final int position) {
        try {
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
        } catch (Exception e) {
        }
    }

    public static class PackageHolder extends Shareable {
        public static final String FORMAT = ".apk";
        public static final String MIME_TYPE = FileUtils.getFileContentType(FORMAT);

        public ApplicationInfo appInfo;
        public String version;
        public String packageName;
        public String appSize;

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
}
