package com.huawei.smart.server.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.huawei.smart.server.R;

/**
 * @author coa.ke on 3/21/18
 */
public class EnhanceRecyclerView extends RecyclerView {

    private View emptyView;
    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            checkIfEmpty();
        }
    };

    public EnhanceRecyclerView(Context context) {
        super(context);
    }

    public EnhanceRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EnhanceRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void checkIfEmpty() {
        if (emptyView != null) {
            emptyView.setVisibility(getAdapter().getItemCount() > 0 ? GONE : VISIBLE);
            EnhanceRecyclerView.this.setVisibility(getAdapter().getItemCount() > 0 ? VISIBLE : GONE);
        }
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(emptyObserver);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
        }
    }

    public void setEmptyView(@Nullable View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }

    /**
     * Not finished
     */
    @Deprecated
    public void useDefaultEmptyView() {
        this.emptyView = View.inflate(this.getContext(), R.layout.widget_empty_list_view, null);
        final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ((ViewGroup) this.getParent()).addView(this.emptyView, layoutParams);
        checkIfEmpty();
    }
}
