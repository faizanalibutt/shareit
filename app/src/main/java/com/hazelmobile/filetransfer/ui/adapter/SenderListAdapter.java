package com.hazelmobile.filetransfer.ui.adapter;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.net.wifi.ScanResult;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.model.Bluetooth;
import com.hazelmobile.filetransfer.util.AppUtils;

import java.util.List;
import java.util.Random;

/**
 * Wifi Scan Result Adapter
 * <p>
 * Created by mayubao on 2016/11/28.
 * Contact me 345269374@qq.com
 */
public class SenderListAdapter extends CommonAdapter<Object> {

    private AppCompatActivity mActivity;
    private int userProfileColor;
    private int[] colorsList;

    public SenderListAdapter(Context context, List<Object> dataList, AppCompatActivity activity) {
        super(context, dataList);
        mActivity = activity;
        colorsList = activity.getResources().getIntArray(R.array.colorsList);
    }

    @Override
    public View convertView(int position, View convertView) {
        ScanResultHolder viewHolder;

        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.item_sender, null);
            viewHolder = new ScanResultHolder();
            viewHolder.iv_device = convertView.findViewById(R.id.iv_device);
            viewHolder.tv_name = convertView.findViewById(R.id.tv_name);
            viewHolder.tv_mac = convertView.findViewById(R.id.tv_mac);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ScanResultHolder) convertView.getTag();
        }

        userProfileColor = new Random().nextInt(colorsList.length);

        Object object = getDataList().get(position);
        if (object instanceof ScanResult) {
            viewHolder.tv_name.setText(((ScanResult) object).SSID);
            viewHolder.tv_mac.setText(((ScanResult) object).capabilities);
        } else if (object instanceof Bluetooth) {
            viewHolder.tv_name.setText(((Bluetooth) object).getDevice().getName());
            viewHolder.tv_mac.setText(((Bluetooth) object).getDevice().getAddress());
        }

        AppUtils.loadProfilePictureInto(viewHolder.tv_name.getText().toString(), viewHolder.iv_device, mContext);

        if (viewHolder.iv_device.getDrawable() instanceof ShapeDrawable) {
            ShapeDrawable shapeDrawable = (ShapeDrawable) viewHolder.iv_device.getDrawable();
            shapeDrawable.getPaint().setColor(colorsList[userProfileColor]);
        }

        return convertView;
    }

    static class ScanResultHolder {
        ImageView iv_device;
        TextView tv_name;
        TextView tv_mac;
    }
}