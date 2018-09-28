package com.huawei.smart.server.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.DeviceSummaryMenuListAdapter;

public class DeviceInfoItemDecoration extends RecyclerView.ItemDecoration {

    private final Paint mPaint;
    private int mHeightDp;
    private Context mContext;

    public DeviceInfoItemDecoration(Context context) {
        this(context, ContextCompat.getColor(context, R.color.colorDivider), 1f);
    }

    public DeviceInfoItemDecoration(Context context, int color, float heightDp) {
        mContext = context;
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);
        mHeightDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightDp, context.getResources().getDisplayMetrics());
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        int margin = mContext.getResources().getDimensionPixelSize(R.dimen.device_info_item_margin_right);
        if (position < state.getItemCount()) {
            switch (parent.getAdapter().getItemViewType(position)) {
                case DeviceSummaryMenuListAdapter.ITEM_VIEW_TYPE_MENU:
                    outRect.setEmpty();
                    break;
                case DeviceSummaryMenuListAdapter.ITEM_VIEW_TYPE_SECTION:
                    outRect.set(0, mHeightDp, 0, mHeightDp);
                    break;
                case DeviceSummaryMenuListAdapter.ITEM_VIEW_TYPE_ITEM:
                    outRect.set(margin, 0, margin, mHeightDp);
                    break;
                default:
                    outRect.setEmpty();
                    break;
            }
        }

    }

    private boolean hasDividerOnBottom(View view, RecyclerView parent, RecyclerView.State state) {
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        return position < state.getItemCount() && parent.getAdapter().getItemViewType(position) != DeviceSummaryMenuListAdapter.ITEM_VIEW_TYPE_MENU;
    }


    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (hasDividerOnBottom(view, parent, state)) {
                c.drawRect(view.getLeft(), view.getBottom(), view.getRight(), view.getBottom() + mHeightDp, mPaint);
            }
        }
    }
}
