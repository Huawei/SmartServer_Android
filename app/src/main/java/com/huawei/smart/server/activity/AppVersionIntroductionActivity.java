package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.AppVersionIntroductionListAdapter;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 */
public class AppVersionIntroductionActivity extends BaseActivity {

    @BindView(R.id.container)
    EnhanceRecyclerView mRecyclerView;

    List<AppVersionIntroductionListAdapter.Version> mDataList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_version_introduction);
        this.initialize(R.string.about_us_label_features, true);
        initializeView();
    }

    private void initializeView() {
        // addPowerSupplies data list
        mDataList = new ArrayList<>();
        mDataList.add(AppVersionIntroductionListAdapter.Version.builder()
            .title(getString(R.string.app_version_1))
            .contentUrl("file:///android_asset/app_version1.html").build());

        // setup list view
        mRecyclerView.setAdapter(new AppVersionIntroductionListAdapter(this, mDataList));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(this));
    }


}
