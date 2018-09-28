package com.huawei.smart.server.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.NetworkPortActivity;
import com.huawei.smart.server.redfish.model.NetworkAdapter;
import com.huawei.smart.server.redfish.model.ResourceId;
import com.huawei.smart.server.widget.LabeledTextView;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_ID;
import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_NETWORK_PORT_LIST;

public class NetworkAdapterListAdapter extends BaseListItemAdapter<NetworkAdapter, NetworkAdapterListAdapter.NetworkAdapterItemViewHolder> {

    private final String deviceId;

    public NetworkAdapterListAdapter(Context context, String deviceId, List<NetworkAdapter> networkAdapters) {
        super(context, networkAdapters);
        this.deviceId = deviceId;
    }

    @Override
    public NetworkAdapterItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new NetworkAdapterItemViewHolder(LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.list_network_adapter_item, null));
    }

    @Override
    public void onBindViewHolder(NetworkAdapterItemViewHolder holder, int position) {
        final NetworkAdapter adapter = items.get(position);
        try {
            final NetworkAdapter.Oem oem = adapter.getOem();
            holder.name.setText(oem.getName());
            if (adapter.getStatus() != null && adapter.getStatus().getState() != null) {
                holder.status.setText(adapter.getStatus().getHealth().getDisplayResId());
                final Drawable drawable = context.getResources().getDrawable(adapter.getStatus().getHealth().getIconResId());
                holder.status.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }

            holder.manufacturer.setText(oem.getCardManufacturer());
            holder.model.setText(oem.getCardModel());
            holder.chipManufacturer.setText(adapter.getManufacturer());
            holder.chipModel.setText(adapter.getModel());
            holder.pos.setText(oem.getPosition());

            if (adapter.getControllers() == null || adapter.getControllers().size() == 0) {
                holder.networkAdapterPorts.setVisibility(View.GONE);
            }
            holder.networkAdapterPorts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<ResourceId> odataList = new ArrayList<>();
                    for (NetworkAdapter.Controller c : adapter.getControllers()) {
                        odataList.addAll(c.getLink().getNetworkPorts());
                    }

                    Bundle bundle = new Bundle();
                    bundle.putSerializable(BUNDLE_KEY_NETWORK_PORT_LIST, odataList);
                    bundle.putSerializable(BUNDLE_KEY_DEVICE_ID, deviceId);
                    ActivityUtils.startActivity(bundle, NetworkPortActivity.class);
                }
            });
        } catch (Exception e) {
            Log.e("hardware/net-adaptor", "un-expect exception caught", e);
        }
    }

    @Getter
    public class NetworkAdapterItemViewHolder extends RecyclerView.ViewHolder {

        TextView name, status;
        private LabeledTextView model, chipManufacturer, chipModel, pos, networkAdapterPorts, manufacturer;

        public NetworkAdapterItemViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            status = view.findViewById(R.id.status);
            manufacturer = view.findViewById(R.id.manufacturer);
            model = view.findViewById(R.id.model);
            chipManufacturer = view.findViewById(R.id.chip_manufacturer);
            chipModel = view.findViewById(R.id.chip_model);
            pos = view.findViewById(R.id.position);
            networkAdapterPorts = view.findViewById(R.id.network_adapter_ports);
        }
    }

}
