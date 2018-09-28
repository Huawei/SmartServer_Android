package com.huawei.smart.server.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.FeedbackDetailActivity;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class FeedbackListAdapter extends RecyclerView.Adapter<FeedbackListAdapter.FeedbackItemViewHolder> {


    private List<Feedback> items;
    private Context mContext;

    public FeedbackListAdapter(Context context, List<Feedback> mDataList) {
        items = mDataList;
        mContext = context;
    }

    @Override
    public FeedbackItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final View inflated = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_feedback_item, null);
        return new FeedbackItemViewHolder(inflated);
    }

    @Override
    public void onBindViewHolder(FeedbackItemViewHolder holder, int position) {
        holder.initialize(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class FeedbackItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView title;
        private View layout;

        private FeedbackItemViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            layout = itemView.findViewById(R.id.container);
            layout.setOnClickListener(this);
        }


        public void initialize(Feedback feedback) {
            title.setText(feedback.titleID);
        }

        @Override
        public void onClick(View view) {
            final Feedback feedback = items.get(getAdapterPosition());
            Intent intent = new Intent(mContext, FeedbackDetailActivity.class);
            intent.putExtra(HWConstants.INTENT_KEY_FEEDBACK_DETAIL_TITLE, feedback.titleID);
            intent.putExtra(HWConstants.INTENT_KEY_FEEDBACK_DETAIL_CONTENT_URL, feedback.contentUrl);
            ActivityUtils.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }

    }

    @Getter
    @Setter
    @ToString
    @Builder
    public static class Feedback {
        private int titleID;
        private String contentUrl;
    }

}
