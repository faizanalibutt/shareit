package com.hazelmobile.filetransfer.app;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author HANZALA
 */

public class BaseItemHolder<T> extends RecyclerView.ViewHolder {


    protected T mObject;
    protected int mPosition;
    protected int mSize;

    public BaseItemHolder(View itemView) {
        super(itemView);


    }

    public void bindData(T object, int position, int size) {
        this.mObject = object;
        this.mPosition = position;
        this.mSize = size;

    }


}