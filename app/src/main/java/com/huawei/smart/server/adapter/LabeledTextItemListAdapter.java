package com.huawei.smart.server.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.huawei.smart.server.redfish.model.Power;
import com.huawei.smart.server.widget.LabeledTextView;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class LabeledTextItemListAdapter extends BaseListItemAdapter<LabeledTextItemListAdapter.LabeledTextItem, LabeledTextItemListAdapter.LabeledTextItemViewHolder> {


    public LabeledTextItemListAdapter(Context context, List<LabeledTextItem> items) {
        super(context, items);
    }

    @Override
    public LabeledTextItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final LabeledTextView labeledTextView = new LabeledTextView(viewGroup.getContext());
        return new LabeledTextItemViewHolder(labeledTextView);
    }

    @Override
    public void onBindViewHolder(LabeledTextItemViewHolder holder, int position) {
        holder.initialize(items.get(position), position != (items.size() - 1));
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LabeledTextItem implements Comparable<LabeledTextItem> {
        String label;
        String value;
        int drawableEnd;
        int drawableStart;
        View.OnClickListener onClickListener;

        @Override
        public int compareTo(@NonNull LabeledTextItem labeledTextItem) {
            return 0;
        }
    }

    public class LabeledTextItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private final LabeledTextView view;

        private LabeledTextItemViewHolder(View labeledTextView) {
            super(labeledTextView);
            this.view = (LabeledTextView) labeledTextView;
        }


        public void initialize(LabeledTextItem item, boolean showDivider) {
            this.view.setLabelText(item.getLabel());
            this.view.setText(item.getValue());
            this.view.setDrawableEnd(item.getDrawableEnd());
            this.view.setDrawableStart(item.getDrawableStart());
            this.view.setDivider(showDivider ? View.VISIBLE : View.GONE);
            this.view.setBackgroundColor(Color.WHITE);

            if (item.getOnClickListener() != null) {
                this.view.setOnClickListener(item.getOnClickListener());
            }
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
