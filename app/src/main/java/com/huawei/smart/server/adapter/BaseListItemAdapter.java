package com.huawei.smart.server.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.huawei.smart.server.redfish.model.Resource;

import java.util.Collections;
import java.util.List;

public abstract class BaseListItemAdapter<ViewModel extends Comparable, ViewHolder extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<ViewHolder> {

    protected Context context;
    protected List<ViewModel> items;


    public BaseListItemAdapter(Context context, List<ViewModel> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItems(List<ViewModel> items) {
        Collections.sort(items);
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void resetItems(List<ViewModel> items) {
        this.items.clear();
        Collections.sort(items);
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void clearItems() {
        this.items.clear();
        notifyDataSetChanged();
    }

    public void addItem(ViewModel item) {
        this.items.add(item);
        notifyDataSetChanged();
    }

}
