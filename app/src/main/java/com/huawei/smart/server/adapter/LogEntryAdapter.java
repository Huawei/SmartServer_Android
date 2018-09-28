package com.huawei.smart.server.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.model.LogEntry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import lombok.Getter;

public class LogEntryAdapter extends BaseListItemAdapter<LogEntry, LogEntryAdapter.LogEntryViewHolder> {

    static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
    Animation rotation;

    public LogEntryAdapter(Context context, List<LogEntry> items) {
        super(context, items);
        rotation = AnimationUtils.loadAnimation(context, R.anim.rotation);
    }


    @Override
    public LogEntryViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new LogEntryViewHolder(LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.list_log_entry_item, null));
    }

    @Override
    public void onBindViewHolder(LogEntryViewHolder viewHolder, int position) {
        final LogEntry entry = this.items.get(position);
        viewHolder.createdOn.setText(dateFormat.format(entry.getCreated()));
        viewHolder.subject.setText(entry.getEventSubject());
        viewHolder.message.setText(entry.getMessage());
        viewHolder.eventId.setText(entry.getEventId());
        viewHolder.status.setText(entry.getStatus().getDisplayResId());
        viewHolder.suggestion.setText(entry.getSuggest());
        if (TextUtils.isEmpty(entry.getSuggest())) {
            viewHolder.suggestionContainer.setVisibility(View.GONE);
        }

        // 设置图标
        final Drawable drawable = context.getResources().getDrawable(entry.getSeverity().getIconResId());
        viewHolder.subject.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }



    @Getter
    public class LogEntryViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{

        final TextView eventId, message, subject, createdOn, status, suggestion;

        View suggestionContainer;
        ImageView more;
        boolean expend = false;

        public LogEntryViewHolder(View itemView) {
            super(itemView);
            eventId = itemView.findViewById(R.id.event_id);
            subject = itemView.findViewById(R.id.subject);
            message = itemView.findViewById(R.id.message);
            createdOn = itemView.findViewById(R.id.created_on);
            status = itemView.findViewById(R.id.status);
            suggestion = itemView.findViewById(R.id.suggestion);
            suggestionContainer = itemView.findViewById(R.id.suggestion_container);
            more = itemView.findViewById(R.id.more);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            final LogEntry entry = items.get(getAdapterPosition());
            if (!TextUtils.isEmpty(entry.getSuggest())) {
                if (expend) {
                    suggestion.setSingleLine(true);
                    more.animate().rotation(0).start();
//                    more.startAnimation(rotate(0F, 180F));
                } else {
                    suggestion.setSingleLine(false);
                    more.animate().rotation(180).start();
//                    more.startAnimation(rotate(180F, 360F));
                }

                expend = !expend;
            }
        }

        private RotateAnimation rotate(float from, float to) {
            final RotateAnimation rotateAnim = new RotateAnimation(from, to,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            rotateAnim.setDuration(100);
            rotateAnim.setFillAfter(true);
            return rotateAnim;
        }

    }

}
