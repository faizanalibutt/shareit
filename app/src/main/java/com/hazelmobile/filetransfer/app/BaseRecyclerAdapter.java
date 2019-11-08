package com.hazelmobile.filetransfer.app;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class BaseRecyclerAdapter<T, D extends BaseItemHolder> extends RecyclerView.Adapter<D> {

    protected final int mLayout;
    protected final Class<? extends BaseItemHolder> mHolderClass;
    protected List<T> mList;
    public static final int AD_TYPE = 1;
    public static final int CONTENT_TYPE = 0;

    public BaseRecyclerAdapter(@LayoutRes int layout, Class<? extends BaseItemHolder> holderClass) {
        mList = new ArrayList<>();
        mLayout = layout;
        mHolderClass = holderClass;
    }

    @Override
    public D onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayout, parent, false);
//        return new D(view);
        /*if (viewType == AD_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_ad_layout, parent, false);
        }*/
        try {
            return (D) mHolderClass.getConstructor(View.class).newInstance(view);
        } catch (Exception e) {
            Log.e("Generic adapter error", e + "");
        }
        return null;

    }

    @Override
    public void onBindViewHolder(BaseItemHolder holder, int position) {
        holder.bindData(mList.get(position), position, mList.size());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setData(@NonNull List<T> dataList) {
        mList = dataList;
        notifyDataSetChanged();
    }


    public void removeItem(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mList.size());

    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position) == null)
            return AD_TYPE;
        return CONTENT_TYPE;
    }
}
