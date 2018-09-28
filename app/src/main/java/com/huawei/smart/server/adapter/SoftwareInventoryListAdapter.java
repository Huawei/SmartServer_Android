package com.huawei.smart.server.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.huawei.smart.server.redfish.model.SoftwareInventory;
import com.huawei.smart.server.utils.StringUtils;
import com.huawei.smart.server.widget.LabeledTextView;

import java.util.HashMap;
import java.util.List;

import static com.huawei.smart.server.activity.FirmwareActivity.Mapper;

public class SoftwareInventoryListAdapter extends BaseListItemAdapter<SoftwareInventory, SoftwareInventoryListAdapter.SoftwareInventoryViewHolder> {


    public SoftwareInventoryListAdapter(Context context, List<SoftwareInventory> items) {
        super(context, items);
    }

    @Override
    public SoftwareInventoryViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final LabeledTextView labeledTextView = new LabeledTextView(viewGroup.getContext());
        return new SoftwareInventoryViewHolder(labeledTextView);
    }

    @Override
    public void onBindViewHolder(SoftwareInventoryViewHolder holder, int position) {
        holder.initialize(items.get(position), position != (items.size() - 1));
    }

    public void updateItems(List<SoftwareInventory> items) {
        HashMap<String, SoftwareInventory> mapped = new HashMap<>();
        for (SoftwareInventory software : items) {
            mapped.put(software.getOdataId(), software);
        }

        for (SoftwareInventory software : this.items) {
            final SoftwareInventory managed = mapped.get(software.getOdataId());
            software.setName(StringUtils.defaultIfBlank(Mapper.get(managed.getName()), managed.getName()));
            software.setVersion(managed.getVersion());
        }
        notifyDataSetChanged();
    }

    public class SoftwareInventoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private final LabeledTextView view;

        private SoftwareInventoryViewHolder(View labeledTextView) {
            super(labeledTextView);
            this.view = (LabeledTextView) labeledTextView;
        }

        public void initialize(SoftwareInventory item, boolean showDivider) {
            this.view.setLabelText(item.getName());
            this.view.setText(item.getVersion());
            this.view.setDivider(showDivider ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View view) {

        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }

    }

}
