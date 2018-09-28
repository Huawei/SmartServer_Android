package com.huawei.smart.server.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.KeyboardUtils;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.SearchHistoryListAdapter;
import com.huawei.smart.server.model.SearchHistory;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;
import com.huawei.smart.server.widget.HWMEditText;

import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * 搜索
 */
public class DeviceSearchActivity extends BaseActivity {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DeviceSearchActivity.class.getSimpleName());

    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.historyList) EnhanceRecyclerView historyRecyclerView;
    @BindView(R.id.cancelButton) TextView cancelButton;
    @BindView(R.id.searchBar) HWMEditText searchBar;
    @BindView(R.id.recent_search_bar) View recentSearchBar;

    private SearchHistoryListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_search);
        this.initialize();
        initView();
    }

    private void initView() {
        final RealmResults<SearchHistory> searchHistories = getDefaultRealmInstance().where(SearchHistory.class)
            .sort("createdOn", Sort.DESCENDING).findAll();
        emptyAndListViewSwitch(searchHistories.size() == 0);
        this.adapter = new SearchHistoryListAdapter(this, getDefaultRealmInstance(), searchHistories);
        historyRecyclerView.setAdapter(adapter);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        historyRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(this));
        initEmptyView();
        historyRecyclerView.setEmptyView(emptyView);
        initSearchBar();
    }

    private void initEmptyView() {
        emptyView.setText(getResources().getString(R.string.ds_label_no_search_history));
    }

    private void initSearchBar() {
        Intent intent = getIntent();
        String searchKeyword = intent.getStringExtra(HWConstants.BUNDLE_KEY_SEARCH_KEYWORD);
        searchBar.setText(searchKeyword);
        searchBar.setSelection(searchKeyword.length());
        searchBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    search(searchBar.getText().toString());
                }
                return false;
            }
        });

        searchBar.setDrawableRightListener(new HWMEditText.DrawableRightListener() {
            @Override
            public void onDrawableRightClick(View view) {
                searchBar.setText("");
            }
        });
    }

    public void search(String searchContent) {
        if (!TextUtils.isEmpty(searchContent)) {
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setSearchContent(searchContent);
            insert(searchHistory);
        }
        KeyboardUtils.hideSoftInput(this);

        Intent data = new Intent();
        data.putExtra(HWConstants.BUNDLE_KEY_SEARCH_KEYWORD, searchContent);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected void onPause() {
        overridePendingTransition(0, 0);
        super.onPause();
    }

    @OnClick(R.id.cancelButton)
    public void onCancelButtonClick() {
        this.goBack();
    }

    private void insert(final SearchHistory record) {
        getDefaultRealmInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    if (record.getId() == null) {
                        SearchHistory exists = realm.where(SearchHistory.class)
                            .equalTo("searchContent", record.getSearchContent()).findFirst();
                        record.setId(exists != null ? exists.getId() : UUID.randomUUID().toString());
                        record.setCreatedOn(new Date());
                    }
                    realm.copyToRealmOrUpdate(record);
                } catch (Exception e) {
                    LOG.error("Failed to persist search history to db", e);
                }
            }
        });
    }

    @OnClick(R.id.clear_history)
    public void deleteAllHistory() {
        getDefaultRealmInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    realm.delete(SearchHistory.class);
                    emptyAndListViewSwitch(true);
                } catch (Exception e) {
                    LOG.error("Failed to delete search history from db", e);
                }
            }
        });
    }

    private void emptyAndListViewSwitch(boolean emptyViewVisible) {
        emptyView.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
        recentSearchBar.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
        historyRecyclerView.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
    }
}
