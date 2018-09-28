package com.huawei.smart.server.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huawei.smart.server.R;
import com.huawei.smart.server.model.HealthEvent;
import com.huawei.smart.server.redfish.constants.Severity;
import com.huawei.smart.server.utils.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import lombok.Getter;

public class HealthEventAdapter extends RealmRecyclerViewAdapter<HealthEvent, HealthEventAdapter.HealthEventViewHolder> {

    static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    static {
//        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private Context context;
    private Severity severity = null;
    private OrderedRealmCollection<HealthEvent> healthEvents;

    public HealthEventAdapter(Context context, OrderedRealmCollection<HealthEvent> healthEvents) {
        super(healthEvents, true, true);
        this.context = context;
        this.healthEvents = healthEvents;           // original data list
    }

    @Override
    public HealthEventViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new HealthEventViewHolder(LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.list_health_event_item, null));
    }

    @Override
    public void onBindViewHolder(HealthEventViewHolder viewHolder, int position) {
        HealthEvent healthEvent = healthEvents.get(position);
        viewHolder.createdOn.setText(DateUtils.displayWithoutTimezone(healthEvent.getCreated()));
        viewHolder.subject.setText(healthEvent.getEventSubject());
        viewHolder.message.setText(healthEvent.getMessage());
        viewHolder.eventId.setText(healthEvent.getEventId());

        // 设置图标
        final Severity severity = Severity.valueOf(healthEvent.getSeverity());
        final Drawable drawable = context.getResources().getDrawable(severity.getIconResId());
        viewHolder.subject.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }

    @Override
    public int getItemCount() {
        return healthEvents != null ? healthEvents.size() : 0;
    }

    public void setHealthEvents(OrderedRealmCollection<HealthEvent> healthEvents) {
        this.healthEvents = healthEvents;
        this.notifyDataSetChanged();
    }

    @Getter
    public class HealthEventViewHolder extends RecyclerView.ViewHolder {

        private TextView eventId, message, subject, createdOn;

        public HealthEventViewHolder(View itemView) {
            super(itemView);
            eventId = itemView.findViewById(R.id.event_id);
            subject = itemView.findViewById(R.id.subject);
            message = itemView.findViewById(R.id.message);
            createdOn = itemView.findViewById(R.id.created_on);
        }
    }

}
