package com.huawei.smart.server.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.model.Power;
import com.huawei.smart.server.utils.StringUtils;

import java.util.List;

public class PowerListAdapter extends BaseListItemAdapter<Power.PowerSupply, PowerListAdapter.ViewHolder> {

    public PowerListAdapter(Context context, List<Power.PowerSupply> powerSupplies) {
        super(context, powerSupplies);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.list_power_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Power.PowerSupply supply = this.items.get(position);
        holder.name.setText(supply.getName());
        holder.status.setText(supply.getStatus().getHealth().getDisplayResId());
        final Drawable drawable = context.getResources().getDrawable(supply.getStatus().getHealth().getIconResId());
        holder.status.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

        holder.manufacturer.setText(supply.getManufacturer());
        holder.partNo.setText(supply.getPartNumber());
        holder.capacityWatts.setText(supply.getPowerCapacityWatts() + "");
        if (supply.getPowerSupplyType() != null) {
            holder.supplyType.setText(supply.getPowerSupplyType().getDisplayResId());
        }
        holder.firmwareVersion.setText(supply.getFirmwareVersion());
        holder.model.setText(supply.getModel());
        holder.serialNo.setText(StringUtils.defaultIfBlank(supply.getSerialNumber(), "N/A"));
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView name, status, manufacturer, partNo, capacityWatts, supplyType, firmwareVersion, model, serialNo;

        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            status = view.findViewById(R.id.status);
            manufacturer = view.findViewById(R.id.manufacturer);
            partNo = view.findViewById(R.id.part_no);
            capacityWatts = view.findViewById(R.id.capacity_watts);
            supplyType = view.findViewById(R.id.supply_type);
            firmwareVersion = view.findViewById(R.id.firmware_version);
            model = view.findViewById(R.id.model);
            serialNo = view.findViewById(R.id.serial_no);
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