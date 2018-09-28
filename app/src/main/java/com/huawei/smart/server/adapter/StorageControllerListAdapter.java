package com.huawei.smart.server.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.constant.MemoryConstants;
import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.DriveActivity;
import com.huawei.smart.server.activity.LogicalDriveActivity;
import com.huawei.smart.server.redfish.constants.ResourceState;
import com.huawei.smart.server.redfish.model.Drive;
import com.huawei.smart.server.redfish.model.ResourceId;
import com.huawei.smart.server.redfish.model.Storage;
import com.huawei.smart.server.redfish.model.StorageController;
import com.huawei.smart.server.widget.LabeledTextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_ID;
import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DRIVE_LIST;
import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_NETWORK_PORT_LIST;
import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_VOLUME_ID;

public class StorageControllerListAdapter extends BaseListItemAdapter<Storage, StorageControllerListAdapter.ViewHolder> {

    static final String LOG_TAG = "Storage Controller";
    static DecimalFormat df = new DecimalFormat("#.##");
    String deviceId;

    public StorageControllerListAdapter(Context context, String deviceId, List<Storage> storages) {
        super(context, storages);
        this.deviceId = deviceId;
    }

    public static String byte2FitMemorySize(final Long byteNum) {
        if (byteNum != null) {
            if (byteNum < 0) {
                return "shouldn't be less than zero!";
            } else if (byteNum < MemoryConstants.KB) {
                return df.format(byteNum) + "B";
            } else if (byteNum < MemoryConstants.MB) {
                return df.format(byteNum * 1.0D/ MemoryConstants.KB) + "KB";
            } else if (byteNum < MemoryConstants.GB) {
                return df.format(byteNum * 1.0D/ MemoryConstants.MB) + "MB";
            } else if (byteNum < MemoryConstants.GB * 1024L){
                return df.format(byteNum * 1.0D/ MemoryConstants.GB) + "GB";
            } else {
                return df.format(byteNum * 1.0D/ MemoryConstants.GB / 1024.0) + "TB";
            }
        }

        return "-";
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.list_storage_controller_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Storage storage = items.get(position);
        try {
            StorageController controller = storage.getStorageControllers().get(0);
            final StorageController.Oem oem = controller.getOem();

            holder.name.setText(controller.getName());
            if (controller.getStatus().getState() != null) {
                holder.status.setText(controller.getStatus().getHealth().getDisplayResId());
                final Drawable drawable = context.getResources().getDrawable(controller.getStatus().getHealth().getIconResId());
                holder.status.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }

            holder.model.setText(controller.getModel());
            holder.firmwareVersion.setText(controller.getFirmwareVersion());

            final List<String> supportedRAIDLevels = oem.getSupportedRAIDLevels();
            final String joined = TextUtils.join(",", supportedRAIDLevels);
            holder.supportRaidLevel.setText(TextUtils.isEmpty(joined) ? "" : "RAID(" + joined.replaceAll("RAID", "") + ")");
            holder.configureVersion.setText(oem.getConfigurationVersion());
            if (oem.getMemorySizeMiB() != null) {
                holder.memorySizeMB.setText(oem.getMemorySizeMiB() + " MB");
            }
            if (controller.getSpeedGbps() != null) {
                holder.speed.setText(controller.getSpeedGbps() + " G");
            }
            holder.sasAddr.setText(oem.getSASAddress());

            final String min = byte2FitMemorySize(oem.getMinStripeSizeBytes());
            final String max = byte2FitMemorySize(oem.getMaxStripeSizeBytes());
            holder.stripeRange.setText(min + "-" + max);

            if (oem.getMaintainPDFailHistory() != null) {
                final ResourceState maintainable = oem.getMaintainPDFailHistory() ? ResourceState.Enabled : ResourceState.Disabled;
                holder.maintainPDFailHistory.setText(maintainable.getLabelResId());
//                holder.maintainPDFailHistory.setDrawableEnd(maintainable.getIconResId());
            }

            if (oem.getCopyBackState() != null) {
                final ResourceState copyBackEnabled = oem.getCopyBackState() ? ResourceState.Enabled : ResourceState.Disabled;
                holder.copyBack.setText(copyBackEnabled.getLabelResId());
//                holder.copyBack.setDrawableEnd(copyBackEnabled.getIconResId());
            }

            if (oem.getSmarterCopyBackState() != null) {
                final ResourceState smartCopyBackEnabled = oem.getSmarterCopyBackState() ? ResourceState.Enabled : ResourceState.Disabled;
                holder.smartCopyBack.setText(smartCopyBackEnabled.getLabelResId());
//                holder.smartCopyBack.setDrawableEnd(smartCopyBackEnabled.getIconResId());
            }

            if (oem.getJBODState() != null) {
                final ResourceState JBOD = oem.getJBODState() ? ResourceState.Enabled : ResourceState.Disabled;
                holder.JOBD.setText(JBOD.getLabelResId());
//                holder.JOBD.setDrawableEnd(JBOD.getIconResId());
            }

            holder.bbuName.setText(TextUtils.isEmpty(oem.getCapacitanceName()) ? HWConstants.DFT_NULL_VALUE : oem.getCapacitanceName());

            if (oem.getCapacitanceStatus() != null) {
                holder.bbuState.setText(oem.getCapacitanceStatus().getHealth().getDisplayResId());
//                holder.bbuState.setDrawableEnd(oem.getCapacitanceStatus().getState().getIconResId());
            }

            // Bind Volume click
            boolean hasVolumes = storage.getVolumes() != null && !TextUtils.isEmpty(storage.getVolumes().getOdataId());
            holder.showVolumes.setVisibility(hasVolumes ? View.VISIBLE : View.GONE);
            holder.showVolumes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(BUNDLE_KEY_VOLUME_ID, storage.getVolumes().getOdataId());
                    bundle.putSerializable(BUNDLE_KEY_DEVICE_ID, deviceId);
                    ActivityUtils.startActivity(bundle, LogicalDriveActivity.class);
                }
            });

            // Bind Disk/Volume click
            boolean hasDrives = storage.getDrives() != null && storage.getDrives().size() > 0;
            holder.showDisks.setVisibility(hasDrives ? View.VISIBLE : View.GONE);
            holder.showDisks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<ResourceId> odataList = new ArrayList<>();
                    odataList.addAll(storage.getDrives());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(BUNDLE_KEY_DRIVE_LIST, odataList);
                    bundle.putSerializable(BUNDLE_KEY_DEVICE_ID, deviceId);
                    ActivityUtils.startActivity(bundle, DriveActivity.class);
                }
            });
        } catch (Exception e) {
            Log.e("hardware/storage", "un-expect exception caught", e);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        //    @BindView(R.id.container) LabeledTextView container;
        //    @BindView(R.id.name) LabeledTextView name;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.status) TextView status;
        @Nullable
        @BindView(R.id.model)
        LabeledTextView model;
        @BindView(R.id.firmware_version) LabeledTextView firmwareVersion;
        //        @BindView(R.id.support_ext_device) LabeledTextView supportExtDevice;
        @BindView(R.id.support_raid_level) LabeledTextView supportRaidLevel;
        //        @BindView(R.id.mode) LabeledTextView mode;
        @BindView(R.id.configure_version) LabeledTextView configureVersion;
        @BindView(R.id.memory_size_MB) LabeledTextView memorySizeMB;
        @BindView(R.id.speed) LabeledTextView speed;
        @BindView(R.id.sas_addr) LabeledTextView sasAddr;
        @BindView(R.id.stripe_range) LabeledTextView stripeRange;
        //        @BindView(R.id.cache_pinned) LabeledTextView cachePinned;
        @BindView(R.id.maintain_pd_fail_history) LabeledTextView maintainPDFailHistory;
        @BindView(R.id.copy_back) LabeledTextView copyBack;
        @BindView(R.id.smart_copy_back) LabeledTextView smartCopyBack;
        @BindView(R.id.JOBD) LabeledTextView JOBD;
        @BindView(R.id.bbu_name) LabeledTextView bbuName;
        @BindView(R.id.bbu_state) LabeledTextView bbuState;
        @BindView(R.id.show_volumes) LabeledTextView showVolumes;
        @BindView(R.id.show_disks) LabeledTextView showDisks;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }

        @Override
        public void onClick(View v) {

        }
    }
}