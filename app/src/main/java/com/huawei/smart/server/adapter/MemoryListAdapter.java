package com.huawei.smart.server.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.model.Memory;

import java.util.List;

import lombok.Getter;

public class MemoryListAdapter extends BaseListItemAdapter<Memory, MemoryListAdapter.MemoryItemViewHolder> {

    public MemoryListAdapter(Context context, List<Memory> memories) {
        super(context, memories);
    }

    @Override
    public MemoryItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new MemoryItemViewHolder(LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.list_memory_item, null));
    }

    @Override
    public void onBindViewHolder(MemoryItemViewHolder holder, int position) {
        Memory memory = items.get(position);
        try {
//        name, status, type, capacity, speed, widthBit, rankCount, minVoltage, manufacturer, technology;
            holder.name.setText(memory.getDeviceLocator());
            if (memory.getStatus() != null) {
                holder.status.setText(memory.getStatus().getHealth().getDisplayResId());
                final Drawable drawable = context.getResources().getDrawable(memory.getStatus().getHealth().getIconResId());
                holder.status.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }

            holder.type.setText(memory.getMemoryDeviceType() == null ? HWConstants.DFT_NULL_VALUE : memory.getMemoryDeviceType());
            holder.capacity.setText(memory.getCapacityMiB() == null ? HWConstants.DFT_NULL_VALUE : memory.getCapacityMiB() + " MB");
            holder.speed.setText(memory.getOperatingSpeedMhz() == null ? HWConstants.DFT_NULL_VALUE : memory.getOperatingSpeedMhz() + " MHz");
            holder.widthBit.setText(memory.getDataWidthBits() == null ? HWConstants.DFT_NULL_VALUE : memory.getDataWidthBits() + " bit");
            holder.rankCount.setText(memory.getRankCount() == null ? HWConstants.DFT_NULL_VALUE : memory.getRankCount() + " rank");
            holder.minVoltage.setText(memory.getOem() != null && memory.getOem().getMinVoltageMillivolt() != null ?
                memory.getOem().getMinVoltageMillivolt() + " mV" : HWConstants.DFT_NULL_VALUE);
            holder.manufacturer.setText(memory.getManufacturer() == null ? HWConstants.DFT_NULL_VALUE : memory.getManufacturer());
            holder.serialNo.setText(memory.getSerialNumber() == null ? HWConstants.DFT_NULL_VALUE : memory.getSerialNumber());
            holder.partNo.setText(memory.getPartNumber() == null ? HWConstants.DFT_NULL_VALUE : memory.getPartNumber());
            holder.technology.setText(memory.getOem() != null && memory.getOem().getTechnology() != null ?
                memory.getOem().getTechnology() : HWConstants.DFT_NULL_VALUE);
            holder._position.setText(memory.getOem() != null && memory.getOem().getPosition() != null ?
                memory.getOem().getPosition() : HWConstants.DFT_NULL_VALUE);
        } catch (Exception e) {
            Log.e("hardware/memory", "un-expect exception caught", e);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Getter
    public class MemoryItemViewHolder extends RecyclerView.ViewHolder {

        private TextView name, status, type, capacity, speed, widthBit, rankCount,
            minVoltage, manufacturer, serialNo, partNo, technology, _position;

        public MemoryItemViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            status = view.findViewById(R.id.status);
            type = view.findViewById(R.id.type);
            capacity = view.findViewById(R.id.capacity);
            speed = view.findViewById(R.id.speed);
            widthBit = view.findViewById(R.id.width_bit);
            rankCount = view.findViewById(R.id.rank_count);
            minVoltage = view.findViewById(R.id.min_voltage);
            manufacturer = view.findViewById(R.id.manufacturer);
            serialNo = view.findViewById(R.id.serial_no);
            partNo = view.findViewById(R.id.part_no);
            technology = view.findViewById(R.id.technology);
            _position = view.findViewById(R.id.position);
        }
    }
}
