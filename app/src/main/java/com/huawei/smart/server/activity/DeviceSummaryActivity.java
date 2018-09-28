package com.huawei.smart.server.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.BitmapRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.RedfishClientManager;
import com.huawei.smart.server.adapter.DeviceSummaryMenuListAdapter;
import com.huawei.smart.server.model.Device;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.constants.HealthState;
import com.huawei.smart.server.redfish.constants.PowerState;
import com.huawei.smart.server.redfish.model.ComputerSystem;
import com.huawei.smart.server.redfish.model.EthernetInterface;
import com.huawei.smart.server.redfish.model.EthernetInterfaceCollection;
import com.huawei.smart.server.redfish.model.Manager;
import com.huawei.smart.server.redfish.model.MemoryCollection;
import com.huawei.smart.server.redfish.model.Processor;
import com.huawei.smart.server.redfish.model.Processors;
import com.huawei.smart.server.redfish.model.Resource;
import com.huawei.smart.server.redfish.model.ResourceId;
import com.huawei.smart.server.utils.BundleBuilder;
import com.huawei.smart.server.utils.StringUtils;
import com.huawei.smart.server.widget.DeviceMenuSheetView;
import com.huawei.smart.server.widget.LabeledTextView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import okhttp3.Response;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_ID;

/**
 * 设备详情
 */
public class DeviceSummaryActivity extends BaseActivity {

    static final String LOG_TAG = "DeviceSummary";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DeviceSummaryActivity.class.getSimpleName());
    @BindView(R.id.thumbnail)
    ImageView thumbnail;

    @BindView(R.id.bottom_sheet)
    BottomSheetLayout bottomSheetLayout;

    @BindView(R.id.container)
    RecyclerView mRecyclerView;

    @BindView(R.id.model)
    TextView modelTextView;

    @BindView(R.id.power_state)
    TextView powerStateTextView;

    @BindView(R.id.health_state_icon)
    ImageView healthStateImageView;


    // display list item binding start
    @BindView(R.id.ds_list_item_model) LabeledTextView model;
    @BindView(R.id.ds_list_item_ipv4) LabeledTextView ipv4;
    @BindView(R.id.ds_list_item_ipv6) LabeledTextView ipv6;
    @BindView(R.id.ds_list_item_location) LabeledTextView location;
    @BindView(R.id.ds_list_item_health_state) LabeledTextView healthState;
    @BindView(R.id.ds_list_item_power_state) LabeledTextView powerState;
    @BindView(R.id.ds_list_item_serial_number) LabeledTextView serialNumber;
    @BindView(R.id.ds_list_item_asset_tag) LabeledTextView assetTag;
    @BindView(R.id.ds_list_item_firmware_version) LabeledTextView firmwareVersion;
    @BindView(R.id.ds_list_item_bios_version) LabeledTextView biosVersion;
    @BindView(R.id.ds_list_item_cpu) LabeledTextView cpu;
    @BindView(R.id.ds_list_item_memory) LabeledTextView memory;
    // display list item binding end

    private DeviceMenuSheetView mSelectSwitchableDeviceSheet;

    private ComputerSystem computer;
    private Manager manager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "Create Device Summary Activity");
        setContentView(R.layout.activity_device_summary);
        getOrCreateRedfishClient();
        initialize(getDevice().getAlias(), getDevice().getHostname(), true);
        initializeView();
        loadViewData();
    }

    private void initializeView() {
        // 初始化菜单栏
        mRecyclerView.setAdapter(new DeviceSummaryMenuListAdapter(DeviceSummaryActivity.this));
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
    }

    @Override
    protected void onResume() {
        Log.i(LOG_TAG, "Resume Device Summary Activity");
        super.onResume();
    }

    public void onRefresh(RefreshLayout refreshLayout) {
        fetchViewData();
    }

    private void loadViewData() {
        showLoadingDialog();
        this.redfish.initialize(new OkHttpResponseListener() { // 获取页面数据
            @Override
            public void onResponse(Response response) {
                LOG.info("Initialize redfish session successfully");
                fetchViewData();
            }

            @Override
            public void onError(ANError anError) {
                dismissLoadingDialog();
                final Throwable cause = anError.getCause();
                LOG.error("Failed to initialize connection session for redfish", cause);
                if (anError.getResponse() == null || (cause != null && cause instanceof SocketTimeoutException)) {
                    final DeviceSummaryActivity $activity = DeviceSummaryActivity.this;
                    new MaterialDialog.Builder($activity)
                        .title(R.string.msg_auth_failed_title)
                        .content(R.string.msg_access_api_timeout)
                        .positiveText(R.string.button_sure)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ActivityUtils.finishActivity($activity);
                            }
                        })
                        .show();
                } else {
                    RedfishResponseListener.handleFANError(DeviceSummaryActivity.this, anError);
                }
            }
        });
    }

    private void fetchViewData() {
        //        this.showLoadingDialog(this);
        updateModelPicture();
        updateIpTextViews();
        // updateProcessView();
        updateManagerResourceView();
        updateSystemResourceView();
    }

    private void updateModelPicture() {
        redfish.getDeviceThumbnail(new BitmapRequestListener() {

            @Override
            public void onResponse(Bitmap response) {
                thumbnail.setImageBitmap(response);
            }

            @Override
            public void onError(ANError anError) {

            }
        });
    }

    private void updateSystemResourceView() {
        redfish.systems().get(RRLB.<ComputerSystem>create(DeviceSummaryActivity.this)
            .callback(new RedfishResponseListener.Callback<ComputerSystem>() {
                public void onResponse(Response okHttpResponse, final ComputerSystem computer) {
                    DeviceSummaryActivity.this.computer = computer;
                    getDefaultRealmInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            final Device managed = realm.where(Device.class).equalTo("id", device.getId()).findFirst();
                            final String productAlias = computer.getOem() == null ? null : computer.getOem().getProductAlias();
                            final String model = computer.getModel();
                            String alias = StringUtils.defaultIfBlank(productAlias, model);
                            if (!TextUtils.isEmpty(alias)) {
                                managed.setAlias(alias);
                            }
                            // 更新产品序列号
                            managed.setSerialNo(computer.getSerialNumber());
                            managed.setSwitchable(true); // 添加到切换列表中
                            realm.copyToRealmOrUpdate(managed);
                            device = realm.copyFromRealm(managed);
                        }
                    });

                    // Log.i(LOG_TAG, "Get Computer System response: " + computer);
                    modelTextView.setText(StringUtils.defaultString(computer.getModel(), "-"));
                    healthStateImageView.setImageResource(computer.getStatus().getHealth().getIconResId());

                    model.setText(StringUtils.defaultIfBlank(computer.getModel(), "-"));
                    final PowerState powerState = computer.getPowerState();
                    if (powerState != null) {
                        // Header
                        updatePowerStateImage(powerState.getIconResId(), powerState.getDisplayResId());
                        // display list item
                        DeviceSummaryActivity.this.powerState.setText(powerState.getDisplayResId());
                        DeviceSummaryActivity.this.powerState.setDrawableEnd(powerState.getIconResId());
                    } else {
                        DeviceSummaryActivity.this.powerState.dismiss();
                    }

                    final HealthState health = computer.getStatus() != null ? computer.getStatus().getHealth() : null;
                    if (health != null) {
                        healthStateImageView.setImageResource(health.getIconResId());
                        healthState.setText(health.getDisplayResId());
                        healthState.setDrawableEnd(health.getIconResId());
                    } else {
                        healthState.dismiss();
                    }

                    serialNumber.setText(StringUtils.defaultString(computer.getSerialNumber(), "-"));
                    assetTag.setText(StringUtils.defaultString(computer.getAssetTag(), "-"));
                    biosVersion.setText(StringUtils.defaultString(computer.getBiosVersion(), "-"));

                    final Integer totalSystemMemoryGiB = computer.getMemorySummary().getTotalSystemMemoryGiB();
                    if (totalSystemMemoryGiB != null) {
                        memory.setText(totalSystemMemoryGiB + "GB");
                    }
//                    updateMemoryView();
                    updateProcessView();
                }

                public void onError(ANError anError) {
                    finishLoadingViewData(true);
                }
            }).build());
    }

    private void updatePowerStateImage(int imageId, int textId) {
        Drawable drawableLeft = ContextCompat.getDrawable(this,
            imageId);

        powerStateTextView.setCompoundDrawablesWithIntrinsicBounds(drawableLeft,
            null, null, null);
        powerStateTextView.setCompoundDrawablePadding(10);
        powerStateTextView.setText(getResources().getString(textId));
    }

    private void updateProcessView() {
        redfish.systems().getProcessors(RRLB.<Processors>create(DeviceSummaryActivity.this).callback(
            new RedfishResponseListener.Callback<Processors>() {
                @Override
                public void onResponse(Response okHttpResponse, final Processors processors) {
                    if (processors.getCount() > 0) {
                        final Resource resource = processors.getMembers().get(0);
                        redfish.systems().getProcessor(resource.getOdataId(), RRLB.<Processor>create(DeviceSummaryActivity.this)
                            .callback(new RedfishResponseListener.Callback<Processor>() {
                                @Override
                                public void onResponse(Response okHttpResponse, Processor processor) {
                                    cpu.setText(processors.getCount() + "*" + processor.getModel(), true);
                                    finishLoadingViewData(true);
                                }

                                @Override
                                public void onError(ANError anError) {
                                    finishLoadingViewData(true);
                                }
                            }).build());
                    }
                }

                @Override
                public void onError(ANError anError) {
                    finishLoadingViewData(true);
                }
            }
        ).build());
    }

    @Deprecated
    private void updateMemoryView() {
        // 获取内存数据
        redfish.systems().getMemoryCollection(null, new RedfishResponseListener<MemoryCollection>(
                DeviceSummaryActivity.this,
                new RedfishResponseListener.Callback<MemoryCollection>() {
                    @Override
                    public void onResponse(Response okHttpResponse, MemoryCollection response) {
                        final Integer totalSystemMemoryGiB = computer.getMemorySummary().getTotalSystemMemoryGiB();
                        Integer amount = response.getMembers().size();
                        if (amount > 0) {
                            int singleMemoryGiB = totalSystemMemoryGiB / amount;
                            memory.setText(amount + " * " + singleMemoryGiB + "GB");
                        }
                        finishLoadingViewData(true);
                    }

                    @Override
                    public void onError(ANError anError) {
                        finishLoadingViewData(true);
                    }
                }
            )
        );
    }

    private void updateManagerResourceView() {
        redfish.managers().get(RRLB.<Manager>create(DeviceSummaryActivity.this)
            .callback(new RedfishResponseListener.Callback<Manager>() {
                @Override
                public void onResponse(Response okHttpResponse, Manager manager) {
                    DeviceSummaryActivity.this.manager = manager;
                    location.setText(manager.getOem() == null ? null : manager.getOem().getDeviceLocation(), true);
                    firmwareVersion.setText(StringUtils.defaultString(manager.getFirmwareVersion(), "-"));
                }

                @Override
                public void onError(ANError anError) {
                    finishLoadingViewData(true);
                }
            }).build());
    }

    private void updateIpTextViews() {
        // 获取 IPv4/IPv6
        redfish.managers().getEthernetInterfaces(RRLB.<EthernetInterfaceCollection>create(this).callback(
            new RedfishResponseListener.Callback<EthernetInterfaceCollection>() {
                @Override
                public void onResponse(Response okHttpResponse, EthernetInterfaceCollection response) {
                    ResourceId resourceId = response.getMembers().get(0);
                    redfish.managers().getEthernetInterface(resourceId.getOdataId(), RRLB.<EthernetInterface>create(DeviceSummaryActivity.this)
                        .callback(
                            new RedfishResponseListener.Callback<EthernetInterface>() {
                                @Override
                                public void onResponse(Response okHttpResponse, EthernetInterface response) {
                                    final EthernetInterface.IPv4Address iPv4 = response.getIPv4();
                                    if (ipv4 != null) {
                                        ipv4.setText(StringUtils.defaultString(iPv4.getAddress(), "-"));
                                    }
                                    final EthernetInterface.IPv6Address iPv6 = response.getIPv6();
                                    if (ipv6 != null) {
                                        if (!TextUtils.isEmpty(iPv6.getAddress())) {
                                            ipv6.setText(iPv6.getAddress() + (iPv6.getPrefixLength() != null ? "/" + String.valueOf(iPv6.getPrefixLength()) : ""));
                                        } else {
                                            ipv6.setText("-");
                                        }
                                    }
                                }
                            }
                        ).build());
                }
            }
        ).build());
    }

    @OnClick(R.id.title_bar_actions)
    public void onSwitchDevice() {
        if (mSelectSwitchableDeviceSheet == null) {
            mSelectSwitchableDeviceSheet =
                new DeviceMenuSheetView(this, R.string.ds_action_switch_device);
            final RealmResults<Device> devices = getDefaultRealmInstance().where(Device.class)
                .notEqualTo("id", device.getId())
                .equalTo("switchable", true)
                .sort("warning", Sort.DESCENDING, "lastUpdatedOn", Sort.DESCENDING).findAll();
            final DeviceMenuSheetView.SwitchableDeviceListAdapter adapter = new DeviceMenuSheetView.SwitchableDeviceListAdapter(
                this, getDefaultRealmInstance(), devices);
            mSelectSwitchableDeviceSheet.setAdapter(adapter);
        }
        bottomSheetLayout.showWithSheetView(mSelectSwitchableDeviceSheet);
    }


    @OnClick(value = {R.id.health_state_icon, R.id.ds_list_item_health_state})
    public void goToHealthEventActivity() {
        Bundle bundle = BundleBuilder.instance().with(BUNDLE_KEY_DEVICE_ID, this.getDevice().getId()).build();
        ActivityUtils.startActivity(bundle, HealthEventActivity.class);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public Device getDevice() {
        return device;
    }

    @Override
    protected void onDestroy() {
        RedfishClientManager.getInstance().destroy(this.device.getId(), null);
        super.onDestroy();
    }
}
