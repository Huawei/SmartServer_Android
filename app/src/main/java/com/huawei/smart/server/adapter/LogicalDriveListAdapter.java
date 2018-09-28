package com.huawei.smart.server.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.DriveActivity;
import com.huawei.smart.server.activity.LogicalDriveActivity;
import com.huawei.smart.server.redfish.model.Drive;
import com.huawei.smart.server.redfish.model.LogicalDrive;
import com.huawei.smart.server.redfish.model.ResourceId;
import com.huawei.smart.server.utils.StringUtils;
import com.huawei.smart.server.widget.LabeledTextView;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_ID;
import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DRIVE_LIST;

public class LogicalDriveListAdapter extends BaseListItemAdapter<LogicalDrive, LogicalDriveListAdapter.LogicalDriveItemViewHolder> {

    private final String deviceId;

    public LogicalDriveListAdapter(LogicalDriveActivity context, List<LogicalDrive> items) {
        super(context, items);
        this.deviceId = context.getDeviceId();
    }

    @Override
    public void resetItems(List<LogicalDrive> items) {
        super.resetItems(items);
    }

    @Override
    public LogicalDriveItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new LogicalDriveItemViewHolder(LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.list_logical_drive_item, null));
    }

    @Override
    public void onBindViewHolder(LogicalDriveItemViewHolder holder, int position) {
        LogicalDrive logicalDrive = items.get(position);
        holder.name.setText(logicalDrive.getName());
        if (logicalDrive.getStatus().getState() != null) {
            holder.status.setText(logicalDrive.getStatus().getState().getLabelResId());
//            final Drawable drawable = context.getResources().getDrawable(logicalDrive.getStatus().getHealth().getIconResId());
//            holder.status.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
        holder.stripSize.setText(StorageControllerListAdapter.byte2FitMemorySize(logicalDrive.getOptimumIOSizeBytes()));
        holder.capacity.setText(StorageControllerListAdapter.byte2FitMemorySize(logicalDrive.getCapacityBytes()));
        final LogicalDrive.Oem oem = logicalDrive.getOem();
        if (oem != null) {
            holder.volumeName.setText(StringUtils.defaultIfEmpty(logicalDrive.getOem().getVolumeName(), "-"));
            holder.raidLevel.setText(StringUtils.defaultIfEmpty(oem.getVolumeRaidLevel(), "-"));
            if (oem.getSSDCachingEnable() != null) {
                holder.sscdCaching.setText(oem.getSSDCachingEnable() ? context.getString(R.string.Enable) : context.getString(R.string.Disable));
            }

            holder.defaultReadPolicy.setText(StringUtils.defaultIfEmpty(oem.getDefaultReadPolicy(), "-"));
            holder.currentReadPolicy.setText(StringUtils.defaultIfEmpty(oem.getCurrentReadPolicy(), "-"));
            holder.defaultWritePolicy.setText(StringUtils.defaultIfEmpty(oem.getDefaultWritePolicy(), "-"));
            holder.currentWritePolicy.setText(StringUtils.defaultIfEmpty(oem.getCurrentWritePolicy(), "-"));
            holder.defaultIoPolicy.setText(StringUtils.defaultIfEmpty(oem.getDefaultCachePolicy(), "-"));
            holder.currentIoPolicy.setText(StringUtils.defaultIfEmpty(oem.getCurrentCachePolicy(), "-"));
            holder.diskCachePolicy.setText(StringUtils.defaultIfEmpty(oem.getDriveCachePolicy(), "-"));
            holder.accessPolicy.setText(StringUtils.defaultIfEmpty(oem.getAccessPolicy(), "-"));

            if (oem.getSSDCachecadeVolume() != null) {
                holder.l2Cache.setText(oem.getSSDCachecadeVolume() ?
                    context.getString(R.string.yes) : context.getString(R.string.no));
            }

            if (oem.getInitializationMode() != null) {
                holder.initState.setText(oem.getInitializationMode().getLabelResId());
            }

            if (oem.getBGIEnable() != null) {
                holder.bgiEnabled.setText(oem.getBGIEnable() ?
                    context.getString(R.string.Enable) : context.getString(R.string.Disable));
            }

            if (oem.getConsistencyCheck() != null) {
                holder.consistencyCheck.setText(oem.getConsistencyCheck() ?
                    context.getString(R.string.Enable) : context.getString(R.string.Stopped));
            }
            if (oem.getBootEnable() != null) {
                holder.bootDisk.setText(oem.getBootEnable() ?
                    context.getString(R.string.yes) : context.getString(R.string.no));
            }
        }

        // Bind Disk/Volume click
        final List<Drive> drives = logicalDrive.getLinks().getDrives();
        boolean hasDrives = drives != null && drives.size() > 0;
        holder.showDisks.setVisibility(hasDrives ? View.VISIBLE : View.GONE);
        holder.showDisks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<ResourceId> odataList = new ArrayList<>();
                odataList.addAll(drives);
                Bundle bundle = new Bundle();
                bundle.putSerializable(BUNDLE_KEY_DRIVE_LIST, odataList);
                bundle.putSerializable(BUNDLE_KEY_DEVICE_ID, deviceId);
                ActivityUtils.startActivity(bundle, DriveActivity.class);
            }
        });
    }

    @Getter
    public class LogicalDriveItemViewHolder extends RecyclerView.ViewHolder {
        LabeledTextView volumeName, raidLevel, capacity, stripSize, sscdCaching, defaultReadPolicy, currentReadPolicy,
            defaultWritePolicy, currentWritePolicy, defaultIoPolicy, currentIoPolicy, diskCachePolicy,
            accessPolicy, initState, bgiEnabled, l2Cache, consistencyCheck, bootDisk, showDisks;
        private TextView name, status;


        public LogicalDriveItemViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            status = view.findViewById(R.id.status);
            volumeName = view.findViewById(R.id.volumeName);
            raidLevel = view.findViewById(R.id.raidLevel);
            capacity = view.findViewById(R.id.capacity);
            stripSize = view.findViewById(R.id.stripSize);
            sscdCaching = view.findViewById(R.id.sscdCaching);
            defaultReadPolicy = view.findViewById(R.id.defaultReadPolicy);
            currentReadPolicy = view.findViewById(R.id.currentReadPolicy);
            defaultWritePolicy = view.findViewById(R.id.defaultWritePolicy);
            currentWritePolicy = view.findViewById(R.id.currentWritePolicy);
            defaultIoPolicy = view.findViewById(R.id.defaultIoPolicy);
            currentIoPolicy = view.findViewById(R.id.currentIoPolicy);
            diskCachePolicy = view.findViewById(R.id.diskCachePolicy);
            accessPolicy = view.findViewById(R.id.accessPolicy);
            initState = view.findViewById(R.id.initState);
            bgiEnabled = view.findViewById(R.id.bgiEnabled);
            l2Cache = view.findViewById(R.id.l2Cache);
            consistencyCheck = view.findViewById(R.id.consistencyCheck);
            bootDisk = view.findViewById(R.id.bootDisk);
            showDisks = view.findViewById(R.id.showDisks);
        }
    }

}
