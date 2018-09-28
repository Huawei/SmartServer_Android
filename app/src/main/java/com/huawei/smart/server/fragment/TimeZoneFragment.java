package com.huawei.smart.server.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.huawei.smart.server.HWApplication;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.NetworkSettingActivity;
import com.huawei.smart.server.redfish.ManagerClient;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.EthernetInterface;
import com.huawei.smart.server.redfish.model.Manager;
import com.huawei.smart.server.widget.LabeledEditTextView;
import com.huawei.smart.server.widget.SimpleMenuSheetView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;


public class TimeZoneFragment extends NetworkSettingActivity.BaseNetworkFragment {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TimeZoneFragment.class.getSimpleName());

    @BindView(R.id.bottom_sheet) BottomSheetLayout bottomSheetLayout;

    @BindView(R.id.zone) LabeledEditTextView zoneView;
    @BindView(R.id.timezone) LabeledEditTextView timezoneView;

    LinkedHashMap<String, String> areas;
    LinkedHashMap<String, LinkedHashMap<String, String>> timezones;
    SimpleMenuSheetView selectZoneSheetView;
    SimpleMenuSheetView selectTimezoneSheetView;
    String zone;            // selected zone
    String timezone;        // selected timezone

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_zone, container, false);
        this.initialize(view);
        loadTimezones();
        return view;
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        loadingTimezone();
        finishRefreshing(true);
    }

    private void loadTimezones() {
        try {
            final InputStream zoneInputStream = getResources().openRawResource(R.raw.zone);
            JavaType zoneTypeRef = TypeFactory.defaultInstance().constructMapType(LinkedHashMap.class, String.class, String.class);
            this.areas = HWApplication.mapper.readValue(zoneInputStream, zoneTypeRef);

            final InputStream timezoneInputStream = getResources().openRawResource(R.raw.timezone);
            JavaType timezoneTypeRef = TypeFactory.defaultInstance().constructMapType(LinkedHashMap.class, String.class, LinkedHashMap.class);
            this.timezones = HWApplication.mapper.readValue(timezoneInputStream, timezoneTypeRef);
        } catch (IOException e) {
            // should not happen
            LOG.error("Failed to create select zone sheet view", e);
        }
    }

    @Override
    public void initializeView(EthernetInterface ethernetInterface) {
        // do-nothing
    }


    @Override
    public void onResume() {
        super.onResume();
        loadingTimezone();
    }

    public void loadingTimezone() {
        // load datetime offset from server
        final ManagerClient managerClient = activity.getRedfishClient().managers();
        managerClient.get(RRLB.<Manager>create(activity).callback(
            new RedfishResponseListener.Callback<Manager>() {
                @Override
                public void onResponse(Response okHttpResponse, Manager manager) {
                    final String dateTimeLocalOffset = manager.getDateTimeLocalOffset();
                    if (dateTimeLocalOffset.startsWith("GMT-") || dateTimeLocalOffset.startsWith("GMT+")) {
                        zone = "GMT";       // set current select zone
                    } else {
                        zone = dateTimeLocalOffset.substring(0, dateTimeLocalOffset.indexOf("/"));
                    }

                    zoneView.setText(areas.get(zone));
                    timezone = dateTimeLocalOffset;
                    timezoneView.setText(timezones.get(zone).get(timezone));
                    if (isUIActive()) {
                        activity.finishLoadingViewData(true);
                    }
                }
            }).build());
    }

    @OnClick(R.id.submit)
    public void updateDatetimeLocalOffset() {
        if (!TextUtils.isEmpty(timezone)) {
            Manager updated = new Manager();
            updated.setDateTimeLocalOffset(timezone);
            activity.showLoadingDialog();
            LOG.info("Start update timezone");
            activity.getRedfishClient().managers().update(updated, RRLB.<Manager>create(activity).callback(
                new RedfishResponseListener.Callback<Manager>() {
                    @Override
                    public void onResponse(Response okHttpResponse, Manager response) {
                        activity.showToast(R.string.msg_action_success, Toast.LENGTH_SHORT, Gravity.CENTER);
                        activity.dismissLoadingDialog();
                        LOG.info("Update timezone done");
                    }
                }
            ).build());
        } else {
            activity.showToast(R.string.ns_time_zone_msg_timezone_illegal, Toast.LENGTH_SHORT, Gravity.CENTER);
        }
    }

    @OnClick(R.id.zone)
    public void selectZone() {
        bottomSheetLayout.showWithSheetView(getSelectZoneSheetView());
    }

    @OnClick(R.id.timezone)
    public void selectTimezone() {
        bottomSheetLayout.showWithSheetView(getSelectTimezoneSheetView());
    }

    @NonNull
    private SimpleMenuSheetView getSelectZoneSheetView() {
        if (selectZoneSheetView == null) {
            // build sheet view
            selectZoneSheetView = new SimpleMenuSheetView<SimpleMenuSheetView.HashSource>(
                getContext(), R.string.ns_time_zone_label_select_zone);
            selectZoneSheetView.updateDataSource(SimpleMenuSheetView.HashSource.convert(areas));
            selectZoneSheetView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    final SimpleMenuSheetView.HashSource<String, String> source =
                        (SimpleMenuSheetView.HashSource) adapterView.getItemAtPosition(position);
                    onZoneChanged(source.get());
                    if (bottomSheetLayout.isSheetShowing()) {
                        bottomSheetLayout.dismissSheet();
                    }
                }
            });
        }
        return selectZoneSheetView;
    }

    private void onZoneChanged(Map.Entry<String, String> entry) {
        final String selectZone = entry.getKey();
        if (!selectZone.equals(zone)) {
            // 假如区域有变换，重新选择时区
            final Set<Map.Entry<String, String>> entries = timezones.get(selectZone).entrySet();
            final Map.Entry<String, String> next = entries.iterator().next();
            timezoneView.setText(next.getValue());
            timezone = next.getKey();

            zone = selectZone;       // set current select zone
            zoneView.setText(entry.getValue());
        }
    }

    @NonNull
    private SimpleMenuSheetView getSelectTimezoneSheetView() {
        if (selectTimezoneSheetView == null) {
            // build sheet view
            selectTimezoneSheetView = new SimpleMenuSheetView<SimpleMenuSheetView.HashSource>(
                getContext(), R.string.ns_time_zone_label_select_timezone);
            selectTimezoneSheetView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    final SimpleMenuSheetView.HashSource<String, String> source =
                        (SimpleMenuSheetView.HashSource) adapterView.getItemAtPosition(position);
                    timezone = source.get().getKey();       // set current select timezone
                    timezoneView.setText(source.get().getValue());
                    if (bottomSheetLayout.isSheetShowing()) {
                        bottomSheetLayout.dismissSheet();
                    }
                }
            });
        }

        if (!TextUtils.isEmpty(zone)) {
            final LinkedHashMap<String, String> availableTimeZones = this.timezones.get(zone);
            final List<SimpleMenuSheetView.HashSource<String, String>> convert = SimpleMenuSheetView.HashSource.convert(availableTimeZones);
            selectTimezoneSheetView.updateDataSource(convert);
        }
        return selectTimezoneSheetView;
    }

}
