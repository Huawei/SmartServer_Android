package com.huawei.smart.server.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.constants.ProcessorType;
import com.huawei.smart.server.redfish.model.Processor;
import com.huawei.smart.server.utils.StringUtils;
import com.huawei.smart.server.widget.LabeledTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Getter;

public class ProcessorListAdapter extends BaseListItemAdapter<Processor, ProcessorListAdapter.ProcessorItemViewHolder> {

    public ProcessorListAdapter(Context context, List<Processor> memories) {
        super(context, memories);
    }

    @Override
    public ProcessorItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ProcessorItemViewHolder(LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.list_processor_item, null));
    }

    @Override
    public void onBindViewHolder(ProcessorItemViewHolder holder, int position) {
        try {
            Processor processor = items.get(position);

            holder.name.setText(processor.getName());
            if (processor.getStatus() != null) {
                holder.status.setText(processor.getStatus().getHealth().getDisplayResId());
                final Drawable drawable = context.getResources().getDrawable(processor.getStatus().getHealth().getIconResId());
                holder.status.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }

            holder.manufacturer.setText(processor.getManufacturer());
            holder.model.setText(processor.getModel());
            holder.processorId.setText(processor.getProcessorId() == null ? HWConstants.DFT_NULL_VALUE : processor.getProcessorId().getIdentificationRegisters());
            holder.speed.setText(processor.getOem().getFrequencyMHz() == null ? HWConstants.DFT_NULL_VALUE  : processor.getOem().getFrequencyMHz() + " MHz");
            holder.coresAndThreads.setText(String.format(Locale.US, "%s Cores/%s Threads",
                StringUtils.defaultString(processor.getTotalCores(), HWConstants.DFT_NULL_VALUE),
                StringUtils.defaultString(processor.getTotalThreads(), HWConstants.DFT_NULL_VALUE)));
            final Processor.Oem oem = processor.getOem();
            holder.L123Cache.setText(String.format(Locale.US, "%s/%s/%s KB",
                StringUtils.defaultString(oem.getL1CacheKiB(), HWConstants.DFT_NULL_VALUE),
                StringUtils.defaultString(oem.getL2CacheKiB(), HWConstants.DFT_NULL_VALUE),
                StringUtils.defaultString(oem.getL3CacheKiB(), HWConstants.DFT_NULL_VALUE)));
            holder.identificationRegister.setText(TextUtils.isEmpty(oem.getPartNumber()) ? HWConstants.DFT_NULL_VALUE : oem.getPartNumber());
        } catch (Exception e) {
            Log.e("hardware/processor", "un-expect exception caught", e);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void resetItems(List<Processor> items) {
        List<Processor> cpuList = new ArrayList<>();
        for (Processor processor : items) {
            if (processor.getProcessorType().equals(ProcessorType.CPU)) {
                cpuList.add(processor);
            }
        }
        super.resetItems(cpuList);
    }

    @Getter
    public class ProcessorItemViewHolder extends RecyclerView.ViewHolder {

        TextView name, status;
        LabeledTextView manufacturer, model, processorId, speed, coresAndThreads, L123Cache, identificationRegister;

        public ProcessorItemViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            status = view.findViewById(R.id.status);
            manufacturer = view.findViewById(R.id.manufacturer);
            model = view.findViewById(R.id.model);
            processorId = view.findViewById(R.id.processor_id);
            speed = view.findViewById(R.id.speed);
            coresAndThreads = view.findViewById(R.id.cores_and_threads);
            L123Cache = view.findViewById(R.id.L123_cache);
            identificationRegister = view.findViewById(R.id.identification_register);
        }
    }
}
