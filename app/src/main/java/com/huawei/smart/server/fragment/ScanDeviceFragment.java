package com.huawei.smart.server.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ActivityUtils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.AnyOrientationCaptureActivity;
import com.huawei.smart.server.activity.CustomerScannerActivity;
import com.huawei.smart.server.activity.DeviceSummaryActivity;
import com.huawei.smart.server.model.Device;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_ID;
import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_PASSWORD;


public abstract class ScanDeviceFragment extends BaseFragment {

    protected void scanOneDCode() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        // 设置要扫描的条码类型，ONE_D_CODE_TYPES：一维码，QR_CODE_TYPES-二维码
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setCaptureActivity(CustomerScannerActivity.class);
        integrator.setPrompt(getString(R.string.scan_prompt_put_image_in_place)); //底部的提示文字，设为""可以置空
        integrator.setCameraId(0); //前置或者后置摄像头
        integrator.setBeepEnabled(true); //扫描成功的「哔哔」声，默认开启
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            final String serialNumber = result.getContents();
            if (serialNumber == null) {
                activity.showToast(R.string.scan_action_cancel, Toast.LENGTH_SHORT, Gravity.CENTER);
            } else {
                scanQRCallback(serialNumber);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void scanQRCallback(String serialNumber) {
        final Device device = activity.getDefaultRealmInstance().where(Device.class)
            .equalTo("serialNo", serialNumber).findFirst();
        if (device != null) { // 假如查询到对应的设备
            final Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_KEY_DEVICE_ID, device.getId());
            if (!device.getRememberPwd() || TextUtils.isEmpty(device.getPassword())) {
                new MaterialDialog.Builder(activity)
                    .content(device.getAlias() + "(" + device.getHostname() + ")")
                    .inputRangeRes(1, -1, R.color.colorComment)
                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    .input(R.string.ds_dialog_input_device_pwd_hint, 0, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            bundle.putString(BUNDLE_KEY_DEVICE_PASSWORD, input.toString());
                            ActivityUtils.startActivity(bundle, DeviceSummaryActivity.class);
                        }
                    }).show();
            } else {
                ActivityUtils.startActivity(bundle, DeviceSummaryActivity.class);
            }
        } else {
            new MaterialDialog.Builder(activity)
                .title(R.string.scan_device_is_not_available)
                .content(getString(R.string.ds_label_serial_number) + ":" + serialNumber)
                .positiveText(R.string.button_sure)
                .show();
        }
    }
}
