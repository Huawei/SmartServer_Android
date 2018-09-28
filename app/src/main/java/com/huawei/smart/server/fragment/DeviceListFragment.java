package com.huawei.smart.server.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.BuildConfig;
import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.AddDeviceActivity;
import com.huawei.smart.server.activity.DeviceSearchActivity;
import com.huawei.smart.server.adapter.DeviceListAdapter;
import com.huawei.smart.server.model.Device;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;
import com.huawei.smart.server.widget.HWMEditText;
import com.huawei.smart.server.widget.RightDrawableOnTouchListener;
import com.huawei.smart.server.widget.SwipeToDeleteLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_SEARCH_KEYWORD;


public class DeviceListFragment extends ScanDeviceFragment {

    @Nullable
    @BindView(R.id.deviceList) EnhanceRecyclerView deviceRecyclerView;
    @BindView(R.id.search) TextView searchBar;
    @BindView(R.id.empty_view) TextView emptyView;

    private DeviceListAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_device_list, container, false);
        this.initialize(view, R.string.title_device_list, false);
        initializeView();
        initialDeviceList();
        return view;
    }

    private void initializeView() {
        this.searchBar.setOnTouchListener(new RightDrawableOnTouchListener(searchBar) {
            @Override
            public boolean onDrawableTouch(final MotionEvent event) {
                searchBar.setText("");
                adapter.updateItems(getDevices(null));
                return true;
            }
        });
//        searchBar.setDrawableRightListener(new HWMEditText.DrawableRightListener() {
//            @Override
//            public void onDrawableRightClick(View view) {
//                searchBar.setText("");
//                adapter.updateItems(getDevices(null));
//            }
//        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        final Drawable[] compoundDrawables = this.searchBar.getCompoundDrawables();

        this.adapter.notifyDataSetChanged();
    }

    private void initialDeviceList() {
        addDevicesForTest();
        final RealmResults<Device> devices = getDevices(searchBar.getText().toString());
        this.adapter = new DeviceListAdapter(getActivity(), getDefaultRealmInstance(), devices);
        deviceRecyclerView.setAdapter(adapter);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        deviceRecyclerView.addOnItemTouchListener(new SwipeToDeleteLayout.OnSwipeItemTouchListener(getActivity()));
        deviceRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(getContext()));
        initEmptyView();
        deviceRecyclerView.setEmptyView(emptyView);
    }

    private void initEmptyView() {
        Drawable drawableTop = ContextCompat.getDrawable(getContext(),
                R.mipmap.empty_device);

        emptyView.setCompoundDrawablesWithIntrinsicBounds(null,
                drawableTop, null, null);
        emptyView.setCompoundDrawablePadding(24);
        emptyView.setText(getResources().getString(R.string.ds_label_no_device));
    }

    private RealmResults<Device> getDevices(String keyword) {
        final RealmQuery<Device> query = getDefaultRealmInstance().where(Device.class)
            .sort("warning", Sort.DESCENDING, "lastUpdatedOn", Sort.DESCENDING);
        if (!TextUtils.isEmpty(keyword)) {
            return query.contains("alias", keyword, Case.INSENSITIVE)
                .or().contains("hostname", keyword, Case.INSENSITIVE).findAll();
        } else {
            return query.findAll();
        }
    }

    private void addDevicesForTest() {
    }


    @Optional
    @OnClick(R.id.title_bar_action_icon)
    public void onAddDeviceIconClicked() {
        ActivityUtils.startActivity(AddDeviceActivity.class);
    }

    @Optional
    @OnClick(R.id.title_bar_scan_icon)
    public void onScanDevice() {
        scanOneDCode();
    }

    @OnClick(R.id.search)
    public void onSearchIconClicked() {
        Intent intent = new Intent(getContext(), DeviceSearchActivity.class);
        intent.putExtra(HWConstants.BUNDLE_KEY_SEARCH_KEYWORD, searchBar.getText().toString());
        startActivityForResult(intent, HWConstants.START_FOR_RESULT_FOR_DEVICE_SEARCH);
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HWConstants.START_FOR_RESULT_FOR_DEVICE_SEARCH) {
            if (resultCode == Activity.RESULT_OK) {
                String searchText = data.getStringExtra(BUNDLE_KEY_SEARCH_KEYWORD);
                searchBar.setText(searchText);
                adapter.updateItems(getDevices(searchBar.getText().toString()));
            }
        }
    }
}
