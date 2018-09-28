package com.huawei.smart.server.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.model.Drive;
import com.huawei.smart.server.utils.StringUtils;
import com.huawei.smart.server.widget.LabeledTextView;

import java.util.List;

import lombok.Getter;

public class DriveListAdapter extends BaseListItemAdapter<Drive, DriveListAdapter.DriveItemViewHolder> {

    public DriveListAdapter(Context context, List<Drive> items) {
        super(context, items);
    }

    @Override
    public void resetItems(List<Drive> items) {
        super.resetItems(items);
    }

    @Override
    public DriveItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new DriveItemViewHolder(LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.list_drive_item, null));
    }

    @Override
    public void onBindViewHolder(DriveItemViewHolder holder, int position) {
        Drive drive = items.get(position);
        holder.name.setText(drive.getName());
        if (drive.getStatus().getHealth() != null) {
            holder.status.setText(drive.getStatus().getHealth().getDisplayResId());
            final Drawable drawable = context.getResources().getDrawable(drive.getStatus().getHealth().getIconResId());
            holder.status.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
        holder.protocol.setText(drive.getProtocol());

        holder.manufacturer.setText(StringUtils.defaultIfEmpty(drive.getManufacturer(), "-"));
        holder.model.setText(StringUtils.defaultIfEmpty(drive.getModel(), "-"));
        holder.sn.setText(StringUtils.defaultIfEmpty(drive.getSerialNumber(), "-"));
        holder.revision.setText(StringUtils.defaultIfEmpty(drive.getRevision(), "-"));
        holder.media.setText(StringUtils.defaultIfEmpty(drive.getMediaType(), "-"));
        holder.capacity.setText(StorageControllerListAdapter.byte2FitMemorySize(drive.getCapacityBytes()));
        holder.speedGbs.setText(drive.getCapableSpeedGbs() == null ? "-" : drive.getCapableSpeedGbs() + "Gbps");
        holder.negotiatedSpeedGbs.setText(drive.getNegotiatedSpeedGbs() == null ? "-" : drive.getNegotiatedSpeedGbs() + "Gbps");
        holder.powerState.setText("-");
        if (drive.getHotspareType() != null) {
            holder.hotSpareType.setText(drive.getHotspareType().getLabelResId());
        }

        if (drive.getIndicatorLED() != null) {
            holder.indicatorLED.setText(drive.getIndicatorLED().getLabelResId());
        }

        final Drive.Oem oem = drive.getOem();
        if (oem != null) {
            holder.temperature.setText(oem.getTemperatureCelsius() == null ? "-" : oem.getTemperatureCelsius() + "℃");
            final List<String> sasAddress = oem.getSASAddress();
            holder.sas0.setText(sasAddress.size() > 0 ? sasAddress.get(0) : "-");
            holder.sas1.setText(sasAddress.size() > 1 ? sasAddress.get(1) : "-");
            if (oem.getRebuildState() != null) {
                holder.rebuildState.setText(oem.getRebuildState().getLabelResId());
            }
            if (oem.getPatrolState() != null) {
                holder.patrolState.setText(oem.getPatrolState().getLabelResId());
            }
            holder.powerOnHours.setText(oem.getHoursOfPoweredUp() != null ? oem.getHoursOfPoweredUp() + "h" : "-");
            holder.fwStatus.setText(StringUtils.defaultIfEmpty(oem.getFirmwareStatus(), "-"));
        }
    }

    @Getter
    public class DriveItemViewHolder extends RecyclerView.ViewHolder {
        LabeledTextView protocol, manufacturer, model, sn, revision, media, temperature, fwStatus,
            sas0, sas1, capacity, speedGbs, negotiatedSpeedGbs, powerState, hotSpareType,
            rebuildState, patrolState, indicatorLED, powerOnHours;
        private TextView name, status;

        public DriveItemViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            status = view.findViewById(R.id.status);
            protocol = view.findViewById(R.id.protocol);
            manufacturer = view.findViewById(R.id.manufacturer);
            model = view.findViewById(R.id.model);
            sn = view.findViewById(R.id.sn);
            revision = view.findViewById(R.id.revision);
            media = view.findViewById(R.id.media);
            temperature = view.findViewById(R.id.temperature);
            fwStatus = view.findViewById(R.id.fwStatus);
            sas0 = view.findViewById(R.id.sas0);
            sas1 = view.findViewById(R.id.sas1);
            capacity = view.findViewById(R.id.capacity);
            speedGbs = view.findViewById(R.id.speedGbs);
            negotiatedSpeedGbs = view.findViewById(R.id.negotiatedSpeedGbs);
            powerState = view.findViewById(R.id.powerState);
            hotSpareType = view.findViewById(R.id.hotSpareType);
            rebuildState = view.findViewById(R.id.rebuildState);
            patrolState = view.findViewById(R.id.patrolState);
            indicatorLED = view.findViewById(R.id.indicatorLED);
            powerOnHours = view.findViewById(R.id.powerOnHours);
        }
    }

}
