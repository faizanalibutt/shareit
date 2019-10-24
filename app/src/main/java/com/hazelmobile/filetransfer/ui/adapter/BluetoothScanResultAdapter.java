package com.hazelmobile.filetransfer.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hazelmobile.filetransfer.R;
import com.hazelmobile.filetransfer.model.Bluetooth;

import java.util.List;

/**
 * Wifi Scan Result Adapter
 * <p>
 * Created by mayubao on 2016/11/28.
 * Contact me 345269374@qq.com
 */
public class BluetoothScanResultAdapter extends CommonAdapter<Bluetooth> {

    public BluetoothScanResultAdapter(Context context, List<Bluetooth> dataList) {
        super(context, dataList);
    }

    @Override
    public View convertView(int position, View convertView) {
        ScanResultHolder viewHolder = null;

        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.item_wifi_scan_result, null);
            viewHolder = new ScanResultHolder();
            viewHolder.iv_device = convertView.findViewById(R.id.iv_device);
            viewHolder.tv_name = convertView.findViewById(R.id.tv_name);
            viewHolder.tv_mac = convertView.findViewById(R.id.tv_mac);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ScanResultHolder) convertView.getTag();
        }

        Bluetooth scanResult = getDataList().get(position);
        if (scanResult != null) {
            viewHolder.tv_name.setText(scanResult.getData());
        }

        return convertView;
    }

    static class ScanResultHolder {
        ImageView iv_device;
        TextView tv_name;
        TextView tv_mac;
    }
}