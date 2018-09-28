package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.SoftwareInventoryListAdapter;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.ActionResponse;
import com.huawei.smart.server.redfish.model.FirmwareInventory;
import com.huawei.smart.server.redfish.model.SoftwareInventory;
import com.huawei.smart.server.task.LoadSoftwareInventoryListTask;
import com.huawei.smart.server.utils.StringUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;

public class FirmwareActivity extends BaseActivity {

    public static final HashMap<String, String> Mapper = new HashMap<>();
    static {
//        Mapper.put("ActiveBMC", "Active iBMC");
//        Mapper.put("BackupBMC", "Backup iBMC");
//        Mapper.put("Bios", "BIOS");
//        Mapper.put("ActiveUboot", "Active Uboot");
//        Mapper.put("BackupUboot", "Backup Uboot");
    }

    @BindView(R.id.container)
    EnhanceRecyclerView firmwareRecyclerView;

    SoftwareInventoryListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware);
        this.initialize(R.string.ds_label_menu_firmware, true);
        initializeView();
    }

    private void initializeView() {
        this.adapter = new SoftwareInventoryListAdapter(this, new ArrayList<SoftwareInventory>());
        firmwareRecyclerView.setAdapter(adapter);
        firmwareRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        firmwareRecyclerView.setEmptyView(findViewById(R.id.empty_view));
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoadingDialog();
        onRefresh(null);
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        /**
        final List<SoftwareInventory> members = new ArrayList<>();
        for (int i =0 ; i<100; i++) {
            final SoftwareInventory e = new SoftwareInventory();
            e.setName("Test " + i);
            e.setVersion("version " + i);
            e.setOdataId("Test " + i);
            members.add(e);
        }
        adapter.resetItems(members);*/
        /***/
        getRedfishClient().updateService().getFirmwareInventory(RRLB.<FirmwareInventory>create(this)
            .callback(new RedfishResponseListener.Callback<FirmwareInventory>() {
                @Override
                public void onResponse(Response okHttpResponse, FirmwareInventory firmwareInventory) {
                    final List<SoftwareInventory> members = firmwareInventory.getMembers();
                    for (SoftwareInventory member : members) {
                        final String odataId = member.getOdataId();
                        final String name = odataId.substring(odataId.lastIndexOf("/") + 1);
                        member.setName(StringUtils.defaultIfBlank(Mapper.get(name), name));
                        member.setVersion(getResources().getString(R.string.msg_loading));
                    }
                    adapter.resetItems(members);
                    new LoadSoftwareInventoryListTask(activity, adapter).submit(members);
                }
            }).build());
    }

    @OnClick(R.id.rollback)
    public void onRollback() {
        new MaterialDialog.Builder(this)
            .content(R.string.firmware_prompt_rollback)
            .positiveText(R.string.button_sure)
            .negativeText(R.string.button_cancel)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    FirmwareActivity.this.showLoadingDialog();
                    getRedfishClient().managers().rollback(RRLB.<ActionResponse>create(FirmwareActivity.this)
                        .callback(new RedfishResponseListener.Callback<ActionResponse>() {
                            @Override
                            public void onResponse(Response okHttpResponse, ActionResponse response) {
                                FirmwareActivity.this.dismissLoadingDialog();
                                FirmwareActivity.this.showToast(R.string.msg_action_success, Toast.LENGTH_SHORT,  Gravity.CENTER);
                            }
                        }).build());
                }
            })
            .show();
    }

    @OnClick(R.id.reset)
    public void onReset() {
        new MaterialDialog.Builder(this)
            .content(R.string.firmware_prompt_reset)
            .positiveText(R.string.button_sure)
            .negativeText(R.string.button_cancel)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    FirmwareActivity.this.showLoadingDialog();
                    getRedfishClient().managers().reset(RRLB.<ActionResponse>create(FirmwareActivity.this)
                        .callback(new RedfishResponseListener.Callback<ActionResponse>() {
                            @Override
                            public void onResponse(Response okHttpResponse, ActionResponse response) {
                                FirmwareActivity.this.dismissLoadingDialog();
                                FirmwareActivity.this.showToast(R.string.msg_action_success, Toast.LENGTH_SHORT,  Gravity.CENTER);
                            }
                        }).build());
                }
            })
            .show();
    }
}
