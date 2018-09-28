package com.huawei.smart.server.listener;

import android.view.View;

public class RecycleViewListener {

    public interface onItemClickListener {
        void onItemClick(View v, int position);
    }

    public interface onItemLongClickListener {
        void onItemLongClick(View v, int position);
    }
}
