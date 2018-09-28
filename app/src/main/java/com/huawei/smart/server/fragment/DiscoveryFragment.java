package com.huawei.smart.server.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.AddDeviceActivity;

import butterknife.OnClick;


public class DiscoveryFragment extends ScanDeviceFragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_discovery, container, false);
        this.initialize(view, R.string.title_discovery, false);
        return view;
    }

    @OnClick(R.id.add_device_item)
    public void addDeviceItemClick() {
        startActivity(new Intent(getActivity(), AddDeviceActivity.class));
    }

    @OnClick(R.id.scan_qr_code)
    public void addScanQRCodeCLick() {
        scanOneDCode();
    }

}
