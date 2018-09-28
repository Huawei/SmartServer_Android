package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndParsedRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.MenuSheetView;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.model.Device;
import com.huawei.smart.server.redfish.RedfishClient;
import com.huawei.smart.server.redfish.model.ComputerSystem;
import com.huawei.smart.server.utils.StringUtils;
import com.huawei.smart.server.validator.DomainValidator;
import com.huawei.smart.server.validator.InetAddressValidator;
import com.huawei.smart.server.validator.ValidationException;
import com.huawei.smart.server.widget.LabeledEditTextView;
import com.suke.widget.SwitchButton;

import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import okhttp3.Response;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_ID;

/**
 * 添加设备
 */
public class AddDeviceActivity extends BaseActivity {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AddDeviceActivity.class.getSimpleName());

    @BindView(R.id.ad_button_add)
    Button addDeviceButton;
    // bind view items
    @BindView(R.id.ad_tv_connect_type)
    LabeledEditTextView connectType;
    @BindView(R.id.ad_tv_hostname)
    LabeledEditTextView hostname;
    @BindView(R.id.ad_tv_port)
    LabeledEditTextView port;
    @BindView(R.id.ad_tv_username)
    LabeledEditTextView username;
    @BindView(R.id.ad_tv_password)
    LabeledEditTextView password;
    @BindView(R.id.ad_switch_remember_pwd)
    SwitchButton rememberPwd;
    @BindView(R.id.bottom_sheet)
    BottomSheetLayout bottomSheetLayout;
    MenuSheetView selectConnectTypeSheet;
    private Device device = new Device();
    private boolean add = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        // get device
        String deviceId = getExtraString(BUNDLE_KEY_DEVICE_ID);
        add = TextUtils.isEmpty(deviceId);
        final int title = add ? R.string.title_add_device : R.string.title_edit_device;
        this.initialize(title, true);
        this.device = prepareDeviceInstance(deviceId);
        this.initializeViewData();
    }


    /**
     * 初始化界面表单值
     */
    private void initializeViewData() {
        this.hostname.setText(this.device.getHostname());
        this.port.setText(this.device.getPort() + "");
        this.username.setText(this.device.getUsername());
        this.password.setText(this.device.getPassword());
        this.password.setTogglePassword(add);
        this.rememberPwd.setChecked(this.device.getRememberPwd());
        this.connectType.setText(Device.ConnectType.from(this.device.getConnectType()).getDisplayResId());
    }

    /**
     * 初始化要编辑的 Device 对象
     *
     * @param deviceId
     * @return
     */
    private Device prepareDeviceInstance(String deviceId) {
        if (!TextUtils.isEmpty(deviceId)) {
            return getDefaultRealmInstance().where(Device.class).equalTo("id", deviceId).findFirst();
        } else {
            final Device device = new Device();
            device.setPort(443);
            device.setConnectType(Device.ConnectType.WIFI.name());
            device.setRememberPwd(false);
            return device;
        }
    }

    @OnClick(R.id.ad_tv_connect_type)
    void selectConnectType() {
        KeyboardUtils.hideSoftInput(this);
        initSelectConnectTypeSheet();
        bottomSheetLayout.showWithSheetView(selectConnectTypeSheet);
    }

    private void initSelectConnectTypeSheet() {
        if (selectConnectTypeSheet == null) {
            selectConnectTypeSheet =
                new MenuSheetView(this, MenuSheetView.MenuType.LIST, R.string.ad_as_connect_type_title,
                    new MenuSheetView.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            item.setChecked(true);
                            switch (item.getItemId()) {
                                case R.id.ad_connect_type_network:
                                    connectType.setText(R.string.ad_connect_type_network);
                                    device.setConnectType(Device.ConnectType.WIFI.name());
                                    break;
                                case R.id.ad_connect_type_bluetooth:
                                    connectType.setText(R.string.ad_connect_type_bluetooth);
                                    device.setConnectType(Device.ConnectType.Bluetooth.name());
                                    break;
                                case R.id.ad_connect_type_mobile:
                                    connectType.setText(R.string.ad_connect_type_mobile);
                                    device.setConnectType(Device.ConnectType.Mobile.name());
                                    break;
                            }

                            if (bottomSheetLayout.isSheetShowing()) {
                                bottomSheetLayout.dismissSheet();
                            }
                            return true;
                        }
                    });

            selectConnectTypeSheet.inflateMenu(R.menu.ad_as_connect_type);
            selectConnectTypeSheet.setListItemLayoutRes(R.layout.sheet_list_item);
        }
    }

    @OnClick(R.id.ad_button_add)
    void addDevice() {
        try {
            validateUserInput();        // 验证用户输入
            showLoadingDialog();    // 显示加载框

            final String hostname = AddDeviceActivity.this.hostname.getText().toString();
            final int port = Integer.parseInt(AddDeviceActivity.this.port.getText().toString());
            final String username = AddDeviceActivity.this.username.getText().toString();
            final String password = AddDeviceActivity.this.password.getText().toString();

            // 获取设备别名
            final RedfishClient redfish = new RedfishClient(hostname, port, username, password);
            redfish.initialize(new OkHttpResponseListener() {
                @Override
                public void onResponse(Response response) {
                    redfish.systems().get(new OkHttpResponseAndParsedRequestListener<ComputerSystem>() {
                        @Override
                        public void onResponse(Response okHttpResponse, ComputerSystem system) {
                            final String productAlias = system.getOem() == null ? null : system.getOem().getProductAlias();
                            final String model = system.getModel();
                            String alias = StringUtils.defaultIfBlank(productAlias, model); // use productAlias first, then model, final "设备"
                            final String serialNumber = system.getSerialNumber();
                            insert(device, alias, serialNumber);
                            redfish.destroy(null);
                        }

                        @Override
                        public void onError(ANError anError) {
                            insert(device, null, null);
                            redfish.destroy(null);
                        }

                    });
                }

                @Override
                public void onError(ANError anError) {
                    insert(device, null, null);
                }
            });
        } catch (ValidationException e) {
            this.showToast(e.getMessage(), Toast.LENGTH_SHORT, Gravity.CENTER);
        }
    }

    /**
     * submit new device to realm DB
     */
    private void insert(final Device device, final String alias, final String serialNo) {
        getDefaultRealmInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    String defaultDeviceName = getResources().getString(R.string.ad_default_device_name);
                    device.setAlias(TextUtils.isEmpty(alias) ? defaultDeviceName : alias);
                    device.setRememberPwd(Boolean.valueOf(rememberPwd.isChecked()));
                    device.setLastUpdatedOn(new Date());
                    device.setSerialNo(serialNo);

                    device.setHostname(AddDeviceActivity.this.hostname.getText().toString());
                    device.setPort(Integer.parseInt(AddDeviceActivity.this.port.getText().toString()));
                    device.setUsername(AddDeviceActivity.this.username.getText().toString());
                    device.setPassword(Boolean.valueOf(rememberPwd.isChecked()) ?
                        AddDeviceActivity.this.password.getText().toString() : null);

                    if (device.getId() == null) {
                        device.setId(UUID.randomUUID().toString());
                        device.setWarning(0);
                        device.setCreatedOn(new Date());

                        LOG.info("Add device {} successfully", device.getHostname());
                    } else {
                        LOG.info("Edit device {} successfully", device.getHostname());
                    }

                    if (RealmObject.isValid(device)) {
                        realm.copyToRealmOrUpdate(device);
                        dismissLoadingDialog();
                        gotoDeviceListActivity();
                    }
                } catch (Exception e) {
                    dismissLoadingDialog();
                    LOG.info("Failed to save device to db", e);
                    ToastUtils.showLong(R.string.ad_failed_to_add_device);
                }
            }
        });
    }

    private void validateUserInput() throws ValidationException {
        if (TextUtils.isEmpty(device.getConnectType())) {
            throw new ValidationException(this.connectType, getString(R.string.ad_connect_type_is_empty));
        }

        // hostname
        final String hostnameTxt = this.hostname.getText().toString();
        if (TextUtils.isEmpty(hostnameTxt)) {
            throw new ValidationException(this.hostname, getString(R.string.ad_hostname_is_empty));
        } else if (!(DomainValidator.getInstance().isValid(hostnameTxt)
            || InetAddressValidator.getInstance().isValid(hostnameTxt)))
        {
            throw new ValidationException(this.hostname, getString(R.string.ad_hostname_exists));
        }

        final RealmQuery<Device> query = getDefaultRealmInstance().where(Device.class)
            .equalTo("hostname", hostnameTxt).notEqualTo("id", this.device.getId());
        if (query.count() > 0) {
            throw new ValidationException(this.hostname, getString(R.string.ad_hostname_exists));
        }


        // port
        final String portTxt = this.port.getText().toString();
        if (TextUtils.isEmpty(portTxt)) {
            throw new ValidationException(this.port, getString(R.string.ad_port_is_empty));
        } else if (!TextUtils.isDigitsOnly(portTxt)
            || (Integer.parseInt(portTxt) <= 0 || Integer.parseInt(portTxt) > 65535))
        {
            throw new ValidationException(this.port, getString(R.string.ad_port_is_illegal));
        }

        final String usernameTxt = this.username.getText().toString();
        if (TextUtils.isEmpty(usernameTxt)) {
            throw new ValidationException(this.port, getString(R.string.ad_username_is_empty));
        }


        final String passwordTxt = this.password.getText().toString();
        if (TextUtils.isEmpty(passwordTxt)) {
            throw new ValidationException(this.port, getString(R.string.ad_password_is_empty));
        }

    }

    /**
     * return back to device list activity
     */
    private void gotoDeviceListActivity() {
        this.finish();
    }

}
