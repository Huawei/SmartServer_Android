package com.huawei.smart.server.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.model.NetworkPort;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

public class NetworkPortListAdapter extends BaseListItemAdapter<NetworkPort, NetworkPortListAdapter.NetworkPortItemViewHolder> {

    public NetworkPortListAdapter(Context context, List<NetworkPort> items) {
        super(context, items);
    }

    @Override
    public void resetItems(List<NetworkPort> items) {
        super.resetItems(items);
    }

    @Override
    public NetworkPortItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new NetworkPortItemViewHolder(LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.list_network_port_item, null));
    }

    @Override
    public void onBindViewHolder(NetworkPortItemViewHolder holder, int position) {
        NetworkPort port = items.get(position);
        holder.port_number.setText("Port " + port.getPhysicalPortNumber());
        if (port.getLinkStatus() != null) {
            holder.link_status.setText(port.getLinkStatus().getDisplayResId());
            final Drawable drawable = context.getDrawable(port.getLinkStatus().getIconResId());
            holder.link_status.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
        if (port.getAssociatedNetworkAddresses() != null && port.getAssociatedNetworkAddresses().size() > 0) {
            holder.address.setText(port.getAssociatedNetworkAddresses().get(0));
        }

        if (port.getOem() != null && port.getOem().getPortType() != null) {
            holder.port_type.setText(port.getOem().getPortType().getDisplayResId());
        }
    }

    @Getter
    public class NetworkPortItemViewHolder extends RecyclerView.ViewHolder {

        private TextView port_number, link_status, address, port_type;

        public NetworkPortItemViewHolder(View view) {
            super(view);
            port_number = view.findViewById(R.id.port_number);
            link_status = view.findViewById(R.id.link_status);
            address = view.findViewById(R.id.address);
            port_type = view.findViewById(R.id.port_type);
        }

    }

}
