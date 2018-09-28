package com.huawei.smart.server.widget;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huawei.smart.server.R;
import com.huawei.smart.server.utils.DensityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleMenuSheetView<T extends SimpleMenuSheetView.MenuItemSource> extends LinearLayout {

    private final int originalListPaddingTop;

    TextView titleView;
    AbsListView listView;
    ArrayAdapter adapter;

    public SimpleMenuSheetView(final Context context, @StringRes final int titleRes) {
        super(context);
        inflate(context, R.layout.list_sheet_view, this);

        listView = findViewById(R.id.list);
        titleView = findViewById(R.id.title);

        originalListPaddingTop = listView.getPaddingTop();
        setTitle(context.getResources().getString(titleRes));

        ViewCompat.setElevation(this, DensityUtils.dip2px(getContext(), 16f));
    }

    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            titleView.setText(title);
        } else {
            titleView.setVisibility(GONE);
            // Add some padding to the top to account for the missing title
            final int paddingTop = originalListPaddingTop + DensityUtils.dip2px(getContext(), 8f);
            listView.setPadding(listView.getPaddingLeft(), paddingTop, listView.getPaddingRight(), listView.getPaddingBottom());
        }
    }

    public void setAdapter(ArrayAdapter<T> adapter) {
        this.adapter = adapter;
        this.listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void updateDataSource(List<? extends MenuItemSource> dataSource) {
        if (this.adapter == null) {
            this.adapter = new ArrayAdapter(this.getContext(), R.layout.sheet_list_no_icon_item, R.id.label, dataSource);
            this.listView.setAdapter(adapter);
            this.adapter.notifyDataSetChanged();
        } else {
            this.adapter.clear();
            this.adapter.addAll(dataSource);
            this.listView.setAdapter(this.adapter);
            this.adapter.notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        listView.setOnItemClickListener(listener);
    }

    public static abstract class MenuItemSource {

        public abstract String getMenuText();

        public String getMenuIcon() {
            // TODO
            return null;
        }

        @Override
        public String toString() {
            return getMenuText();
        }
    }

    public static class HashSource<K, V> extends MenuItemSource {

        HashMap.Entry<K, V> entry;

        public HashSource(HashMap.Entry<K, V> entry) {
            this.entry = entry;
        }

        public static <K, V> List<HashSource<K, V>> convert(Map<K, V> map) {
            List<HashSource<K, V>> result = new ArrayList<HashSource<K, V>>();
            for (Map.Entry<K, V> entry : map.entrySet()) {
                result.add(new HashSource(entry));
            }
            return result;
        }

        @Override
        public String getMenuText() {
            return entry.getValue().toString();
        }

        public Map.Entry<K, V> get() {
            return entry;
        }
    }

}