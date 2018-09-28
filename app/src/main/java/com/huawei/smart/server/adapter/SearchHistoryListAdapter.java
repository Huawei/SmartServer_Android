package com.huawei.smart.server.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.DeviceSearchActivity;
import com.huawei.smart.server.model.SearchHistory;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

public class SearchHistoryListAdapter extends RealmRecyclerViewAdapter<SearchHistory, RecyclerView.ViewHolder> {

    private final Realm realm;
    private DeviceSearchActivity activity;
    private OrderedRealmCollection<SearchHistory> mList;

    public SearchHistoryListAdapter(final DeviceSearchActivity activity, final Realm realm, OrderedRealmCollection<SearchHistory> mList) {
        super(mList, true, true);
        this.activity = activity;
        this.realm = realm;
        this.mList = mList;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_search_history_content_item, parent, false);
        return new SearchHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SearchHistoryViewHolder viewHolder = (SearchHistoryViewHolder) holder;
        final SearchHistory searchHistory = mList.get(position);
        viewHolder.content.setText(searchHistory.getSearchContent());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.search(searchHistory.getSearchContent());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    class SearchHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView content;

        SearchHistoryViewHolder(View view) {
            super(view);
            this.content = view.findViewById(R.id.content);
        }

        public TextView getContent() {
            return content;
        }

        public void setContent(TextView content) {
            this.content = content;
        }

    }
}