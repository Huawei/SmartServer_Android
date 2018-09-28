package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.FeedbackListAdapter;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 反馈与建议
 */
public class FeedbackActivity extends BaseActivity {

    @BindView(R.id.container)
    EnhanceRecyclerView mRecyclerView;

    List<FeedbackListAdapter.Feedback> mDataList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        this.initialize(R.string.title_feedback, true);
        initializeView();
    }

    private void initializeView() {
        // addPowerSupplies data list
        mDataList = new ArrayList<>();
        mDataList.add(FeedbackListAdapter.Feedback.builder()
            .titleID(R.string.how_to_use_app_password)
            .contentUrl("file:///android_asset/how_to_use_app_password.html").build());
        mDataList.add(FeedbackListAdapter.Feedback.builder()
            .titleID(R.string.how_to_add_device)
            .contentUrl("file:///android_asset/how_to_add_device.html").build());
        mDataList.add(FeedbackListAdapter.Feedback.builder()
            .titleID(R.string.how_to_use_qr_code)
            .contentUrl("file:///android_asset/how_to_use_qr_code.html").build());
        mDataList.add(FeedbackListAdapter.Feedback.builder()
            .titleID(R.string.how_to_use_manage_functions)
            .contentUrl("file:///android_asset/how_to_use_manage_functions.html").build());
        mDataList.add(FeedbackListAdapter.Feedback.builder()
            .titleID(R.string.notes)
            .contentUrl("file:///android_asset/notes.html").build());

        // setup list view
        mRecyclerView.setAdapter(new FeedbackListAdapter(this, mDataList));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(this));
    }


    @OnClick(R.id.add_feedback)
    public void onAddFeedback() {
        ActivityUtils.startActivity(SubmitFeedbackActivity.class);
    }

}
