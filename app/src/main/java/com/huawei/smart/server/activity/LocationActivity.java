package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.fragment.NetworkFragment;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.Manager;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;

/**
 * 设备位置
 */
public class LocationActivity extends BaseActivity {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NetworkFragment.class.getSimpleName());

    static Pattern REGEX = Pattern.compile("^[a-zA-Z0-9`~!@#$%^&*()-_=+\\\\|\\[{}\\];:'\",<.>/?]{0,64}$");

    @BindView(R.id.location)
    TextView location;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        this.initialize(R.string.ds_label_menu_location, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoadingDialog();
        onRefresh(null);
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        this.getRedfishClient().managers().get(new RedfishResponseListener<Manager>(this,
            new RedfishResponseListener.Callback<Manager>() {
                @Override
                public void onResponse(Response okHttpResponse, Manager manager) {
                    if (manager.getOem() != null) {
                        location.setText(manager.getOem().getDeviceLocation());
                    }
                    finishLoadingViewData(true);
                }
            }));
    }

    /**
     * update device activity_location
     */
    @OnClick(R.id.submit)
    public void onSaveLocation() {
        final String location = this.location.getText().toString();

        final Matcher matcher = REGEX.matcher(location);
        if (!matcher.matches()) {
            showToast(R.string.loc_setup_msg_illegal_location, Toast.LENGTH_SHORT, Gravity.CENTER);
        } else {
            Manager patch = new Manager();
            patch.setOem(new Manager.Oem());
            patch.getOem().setDeviceLocation(location);

            showLoadingDialog();
            LOG.info("Start update device location");
            this.getRedfishClient().managers().update(patch, RRLB.<Manager>create(this).callback(
                new RedfishResponseListener.Callback<Manager>() {
                    @Override
                    public void onResponse(Response okHttpResponse, Manager response) {
                        dismissLoadingDialog();
                        showToast(R.string.msg_action_success, Toast.LENGTH_SHORT, Gravity.CENTER);
                        LOG.info("Update device location done");
                    }
                }).build());
        }
    }
}
