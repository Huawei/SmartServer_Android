package com.huawei.smart.server.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.R;
import com.huawei.smart.server.RedfishClientManager;
import com.huawei.smart.server.lock.AppLock;
import com.huawei.smart.server.model.Device;
import com.huawei.smart.server.redfish.RedfishClient;
import com.huawei.smart.server.utils.AppStatus;

import org.slf4j.LoggerFactory;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * @author coa.ke on 4/2/18
 */
public class SplashActivity extends AppCompatActivity {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SplashActivity.class.getSimpleName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("Try to destroy leaked redfish sessions");
                try (Realm realm = Realm.getDefaultInstance()) {
                    final RealmResults<Device> devices = realm.where(Device.class).isNotNull("token").findAll();
                    for (Device device : devices) {
                        if (!TextUtils.isEmpty(device.getToken()) && !TextUtils.isEmpty(device.getSessionOdataId())) {
                            LOG.info("Try to destroy redfish session for device: " + device.getHostname());
                            final RedfishClient client = RedfishClientManager.getInstance().getOrCreate(device);
                            client.destroy(null);
                        }
                    }
                }
            }
        }).run();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AppStatus.isFirstInstall()) {
                    startLockSetActivityForFreshInstall();
                } else {
                    ActivityUtils.startActivity(MainActivity.class);
                }
                finish();
            }
        }, 800);
    }

    private void startLockSetActivityForFreshInstall() {
        Log.i("HWApplication", "First install, show lock setting screen");
        Intent intent = new Intent(this, AppLockActivity.class);
        intent.putExtra(AppLock.TYPE, AppLock.ENABLE_PASSLOCK);
        intent.putExtra(AppLock.MESSAGE, getString(R.string.lock_label_initial_passcode));
        intent.putExtra(AppLock.CONFIRM_MESSAGE, getString(R.string.lock_label_initial_passcode_confirm));
        intent.putExtra(AppLock.CANCELABLE, false);
        intent.putExtra(AppLock.INITIALIZE, true);
        ActivityUtils.startActivity(intent);
    }
}
