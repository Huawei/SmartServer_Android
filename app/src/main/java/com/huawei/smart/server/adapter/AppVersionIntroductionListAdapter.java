package com.huawei.smart.server.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.FeedbackDetailActivity;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class AppVersionIntroductionListAdapter extends RecyclerView.Adapter<AppVersionIntroductionListAdapter.AppVersionIntroductionViewHolder> {


    private List<Version> items;
    private Context mContext;

    public AppVersionIntroductionListAdapter(Context context, List<Version> mDataList) {
        items = mDataList;
        mContext = context;
    }

    @Override
    public AppVersionIntroductionViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final View inflated = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_feedback_item, null);
        return new AppVersionIntroductionViewHolder(inflated);
    }

    @Override
    public void onBindViewHolder(AppVersionIntroductionViewHolder holder, int position) {
        holder.initialize(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class AppVersionIntroductionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView title;
        private View layout;

        private AppVersionIntroductionViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            layout = itemView.findViewById(R.id.container);
            layout.setOnClickListener(this);
        }


        public void initialize(Version feedback) {
            title.setText(feedback.getTitle());
        }

        @Override
        public void onClick(View view) {
            final Version version = items.get(getAdapterPosition());
            Intent intent = new Intent(mContext, FeedbackDetailActivity.class);
            intent.putExtra(HWConstants.INTENT_KEY_FEEDBACK_DETAIL_TITLE_STRING, version.getTitle());
            intent.putExtra(HWConstants.INTENT_KEY_FEEDBACK_DETAIL_CONTENT_URL, version.getContentUrl());
            mContext.startActivity(intent);
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
    public static class Version {
        private String title;
        private String contentUrl;
        private String releaseDate;
    }

}
