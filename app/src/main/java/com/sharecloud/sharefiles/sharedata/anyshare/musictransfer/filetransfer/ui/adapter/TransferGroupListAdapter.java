package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.code4rox.adsmanager.AdmobUtils;
import com.code4rox.adsmanager.NativeAdsIdType;
import com.genonbeta.android.database.SQLQuery;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.R;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.database.AccessDatabase;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.miscpkg.GroupEditable;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.ShowingAssignee;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.TransferGroup;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.AppUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.FileUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.NetworkUtils;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.TinyDB;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.widget.GroupEditableListAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * created by: Veli
 * date: 9.11.2017 23:39
 */

public class TransferGroupListAdapter
        extends GroupEditableListAdapter<TransferGroupListAdapter.PreloadedGroup, GroupEditableListAdapter.GroupViewHolder> {
    final private List<Long> mRunningTasks = new ArrayList<>();

    private AccessDatabase mDatabase;
    private SQLQuery.Select mSelect;
    private NumberFormat mPercentFormat;
    private int userProfileColor;
    private int[] colorsList;

    @ColorInt
    private int mColorPending;
    private int mColorDone;
    private int mColorError;

    public TransferGroupListAdapter(Context context, AccessDatabase database) {
        super(context, MODE_GROUP_BY_DATE);

        mDatabase = database;
        mPercentFormat = NumberFormat.getPercentInstance();
        mColorPending = ContextCompat.getColor(context, AppUtils.getReference(context, R.attr.colorControlNormal));
        mColorDone = ContextCompat.getColor(context, AppUtils.getReference(context, R.attr.colorAccent));
        mColorError = ContextCompat.getColor(context, AppUtils.getReference(context, R.attr.colorError));

        setSelect(new SQLQuery.Select(AccessDatabase.TABLE_TRANSFERGROUP));

        colorsList = context.getResources().getIntArray(R.array.colorsList);
    }

    @Override
    protected void onLoad(GroupLister<PreloadedGroup> lister) {
        List<Long> activeList = new ArrayList<>(mRunningTasks);

        if (getContext() != null && NetworkUtils.isOnline(getContext())
                && !TinyDB.getInstance(getContext()).getBoolean(getContext().getString(R.string.is_premium)))
            lister.offerObliged(this, new AdsModel());
        for (PreloadedGroup group : mDatabase.castQuery(getSelect(), PreloadedGroup.class)) {
            mDatabase.calculateTransactionSize(group.groupId, group.index);

            StringBuilder assigneesText = new StringBuilder();

            for (ShowingAssignee showingAssignee : group.index.assignees) {
                if (assigneesText.length() > 0)
                    assigneesText.append(", ");

                assigneesText.append(showingAssignee.device.nickname);
            }

            if (assigneesText.length() == 0 && group.isServedOnWeb)
                assigneesText.append(getContext().getString(R.string.text_transferSharedOnBrowser));

            group.assignees = assigneesText.length() > 0
                    ? assigneesText.toString()
                    : getContext().getString(R.string.text_emptySymbol);

            group.isRunning = activeList.contains(group.groupId);
            group.totalCount = group.index.incomingCount + group.index.outgoingCount;
            group.totalBytes = group.index.incoming + group.index.outgoing;
            group.totalBytesCompleted = group.index.incomingCompleted + group.index.outgoingCompleted;
            group.totalCountCompleted = group.index.incomingCountCompleted + group.index.outgoingCountCompleted;

            group.totalPercent = group.totalBytesCompleted == 0 || group.totalBytes == 0
                    ? 0.0 : Long.valueOf(group.totalBytesCompleted).doubleValue() / Long.valueOf(group.totalBytes).doubleValue();

            if (!group.assignees.equals(getContext().getString(R.string.text_emptySymbol))
                    && !(group.totalPercent < 1.00)) {
                lister.offerObliged(this, group);
            }

        }
    }

    @Override
    protected PreloadedGroup onGenerateRepresentative(String representativeText) {
        return new PreloadedGroup(representativeText);
    }

    public SQLQuery.Select getSelect() {
        return mSelect;
    }

    public TransferGroupListAdapter setSelect(SQLQuery.Select select) {
        if (select != null)
            mSelect = select;

        return this;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_REPRESENTATIVE)
            return new GroupViewHolder(getInflater().inflate(R.layout.layout_list_title_no_padding_ext, parent, false), R.id.layout_list_title_text);
        else if (viewType == VIEW_TYPE_ADS_LINEAR)
            return new GroupViewHolder(getInflater().inflate(R.layout.ad_unified_4, parent, false), R.id.ad_call_to_action);

        return new GroupViewHolder(getInflater().inflate(R.layout.list_transfer_group_ext, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupViewHolder holder, int position) {
        try {
            final PreloadedGroup object = getItem(position);

            if (!holder.tryBinding(object)) {
                final View parentView = holder.getView();
                @ColorInt
                int appliedColor;
                int percentage = (int) (object.totalPercent * 100);
                ProgressBar progressBar = parentView.findViewById(R.id.progressBar);
                ImageView image = parentView.findViewById(R.id.image);
                View statusLayoutWeb = parentView.findViewById(R.id.statusLayoutWeb);
                TextView text1 = parentView.findViewById(R.id.text);
                TextView text2 = parentView.findViewById(R.id.text2);
                TextView text3 = parentView.findViewById(R.id.text3);
                TextView text4 = parentView.findViewById(R.id.text4);

                parentView.setSelected(object.isSelectableSelected());

                if (object.index.hasIssues)
                    appliedColor = mColorError;
                else
                    appliedColor = object.totalCount == object.totalCountCompleted
                            ? mColorDone
                            : mColorPending;

                if (object.isRunning) {
                    image.setImageResource(R.drawable.ic_pause_white_24dp);
                } else {
                    if ((object.index.outgoingCount == 0 && object.index.incomingCount == 0)
                            || (object.index.outgoingCount > 0 && object.index.incomingCount > 0))
                        image.setImageResource(object.index.outgoingCount > 0
                                ? R.drawable.ic_compare_arrows_white_24dp
                                : R.drawable.ic_error_outline_white_24dp);
                    else {
                        image.setImageResource(object.index.outgoingCount > 0
                                ? R.drawable.ic_arrow_up_white_24dp
                                : R.drawable.ic_arrow_down_white_24dp);
                        text3.setText(
                                object.index.outgoingCount > 0 ?
                                        String.format("Sent to %s", object.assignees)
                                        : String.format("Received from %s", object.assignees)
                        );

                        SpannableString spanString = new SpannableString(text3.getText());
                        ForegroundColorSpan fcsGreen = new ForegroundColorSpan(Color.GREEN);
                        ForegroundColorSpan fcsRed = new ForegroundColorSpan(Color.RED);
                        ForegroundColorSpan fcsBlue = new ForegroundColorSpan(Color.BLUE);

                        if (object.index.outgoingCount > 0)
                            spanString.setSpan(fcsGreen, 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        else
                            spanString.setSpan(fcsRed, 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        spanString.setSpan(
                                fcsBlue,
                                object.index.outgoingCount > 0 ? 8 : 14,
                                text3.getText().length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );

                        text3.setText(spanString);
                    }
                }

                userProfileColor = new Random().nextInt(colorsList.length);

                AppUtils.loadProfilePictureInto(object.assignees, image, parentView.getContext());

                if (image.getDrawable() instanceof ShapeDrawable) {
                    ShapeDrawable shapeDrawable = (ShapeDrawable) image.getDrawable();
                    shapeDrawable.getPaint().setColor(colorsList[userProfileColor]);
                }

                statusLayoutWeb.setVisibility(object.index.outgoingCount > 0 && object.isServedOnWeb
                        ? View.VISIBLE : View.GONE);
                text1.setText(AppUtils.getLocalDeviceName(holder.getView().getContext()));
                text2.setText(getContext().getString(R.string.transfer_file_size, FileUtils.sizeExpression(object.totalBytes, false)));
                //text3.setText(mPercentFormat.format(object.totalPercent));
                text4.setText(getContext().getString(R.string.text_transferStatusFiles, object.totalCountCompleted, object.totalCount));
                progressBar.setMax(100);
                progressBar.setProgress(percentage <= 0 ? 1 : percentage);

                //ImageViewCompat.setImageTintList(image, ColorStateList.valueOf(appliedColor));
                /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    Drawable wrapDrawable = DrawableCompat.wrap(progressBar.getProgressDrawable());

                    DrawableCompat.setTint(wrapDrawable, appliedColor);
                    progressBar.setProgressDrawable(DrawableCompat.unwrap(wrapDrawable));
                } else*/
                //progressBar.setProgressTintList(ColorStateList.valueOf(appliedColor));
            } else if (holder.getItemViewType() == GroupEditableListAdapter.VIEW_TYPE_ADS_LINEAR
                    && NetworkUtils.isOnline(holder.getView().getContext())) {
                AdmobUtils admobUtils = new AdmobUtils(holder.getView().getContext());
                admobUtils.loadNativeAd((FrameLayout) holder.getView(),
                        R.layout.ad_unified_4, NativeAdsIdType.ADJUST_NATIVE_AM);
                admobUtils.setNativeAdListener(new AdmobUtils.NativeAdListener() {
                    @Override
                    public void onNativeAdLoaded() {

                    }

                    @Override
                    public void onNativeAdError() {

                    }
                });
            }
        } catch (Exception e) {
        }
    }

    public void updateActiveList(long[] activeList) {
        synchronized (mRunningTasks) {
            mRunningTasks.clear();

            for (long groupId : activeList)
                mRunningTasks.add(groupId);
        }
    }

    public static class PreloadedGroup
            extends TransferGroup
            implements GroupEditable {
        public int viewType;
        public String representativeText;

        public Index index = new Index();
        public String assignees;

        public int totalCount;
        public int totalCountCompleted;
        public long totalBytes;
        public long totalBytesCompleted;
        public double totalPercent;
        public boolean isRunning;

        public PreloadedGroup() {
        }

        public PreloadedGroup(String representativeText) {
            this.viewType = TransferGroupListAdapter.VIEW_TYPE_REPRESENTATIVE;
            this.representativeText = representativeText;
        }

        @Override
        public boolean applyFilter(String[] filteringKeywords) {
            for (String keyword : filteringKeywords)
                if (assignees.toLowerCase().contains(keyword.toLowerCase()))
                    return true;

            return false;
        }

        @Override
        public boolean comparisonSupported() {
            return true;
        }

        @Override
        public String getComparableName() {
            return getSelectableTitle();
        }

        @Override
        public long getComparableDate() {
            return dateCreated;
        }

        @Override
        public long getComparableSize() {
            return totalCount;
        }

        @Override
        public long getId() {
            return groupId;
        }

        @Override
        public void setId(long id) {
            this.groupId = id;
        }

        @Override
        public String getSelectableTitle() {
            return String.format("%s (%s)", assignees, FileUtils.sizeExpression(totalBytes, false));
        }

        @Override
        public int getRequestCode() {
            return 0;
        }

        @Override
        public int getViewType() {
            return viewType;
        }

        @Override
        public String getRepresentativeText() {
            return representativeText;
        }

        @Override
        public void setRepresentativeText(CharSequence representativeText) {
            this.representativeText = String.valueOf(representativeText);
        }

        @Override
        public boolean isGroupRepresentative() {
            return representativeText != null;
        }

        @Override
        public void setDate(long date) {
            this.dateCreated = date;
        }

        @Override
        public boolean setSelectableSelected(boolean selected) {
            return !isGroupRepresentative() && super.setSelectableSelected(selected);
        }

        @Override
        public void setSize(long size) {
            this.totalCount = ((Long) size).intValue();
        }
    }

    public static class AdsModel extends PreloadedGroup {

        public int viewType;

        public AdsModel() {
            this.viewType = GroupEditableListAdapter.VIEW_TYPE_ADS_LINEAR;
        }

        @Override
        public String getRepresentativeText() {
            return "ADS_VIEW";
        }

        @Override
        public int getViewType() {
            return viewType;
        }

        @Override
        public long getComparableDate() {
            return System.currentTimeMillis();
        }

        @Override
        public boolean comparisonSupported() {
            return getViewType() != GroupEditableListAdapter.VIEW_TYPE_ADS_LINEAR && super.comparisonSupported();
        }

        @Override
        public boolean isSelectableSelected() {
            return false;
        }
    }
}
