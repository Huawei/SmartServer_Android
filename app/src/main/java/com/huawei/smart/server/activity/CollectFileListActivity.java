package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.blankj.utilcode.util.FileUtils;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.CollectFileListAdapter;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class CollectFileListActivity extends BaseActivity {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CollectFileListActivity.class.getSimpleName());

    @BindView(R.id.collect_file_list_view)
    EnhanceRecyclerView mRecyclerView;
    @BindView(R.id.empty_view) View emptyView;
    CollectFileListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_file_list);
        this.initialize(R.string.collect_download_file_list, true);
        this.initializeView();
    }

    private void initializeView() {
        final List<File> items = new ArrayList<>();
        adapter = new CollectFileListAdapter(this, items);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(this));
        mRecyclerView.setEmptyView(emptyView);

        onRefresh(null);
    }

    private List<File> getCollectedFiles() {
        final String downloadToFolderPath = CollectionActivity.getDownloadToFolderPath();
        final List<File> files = FileUtils.listFilesInDir(downloadToFolderPath);
        return files != null ? files : new ArrayList<File>();
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        adapter.resetItems(getCollectedFiles());
        this.finishRefreshing(true);
    }
}
