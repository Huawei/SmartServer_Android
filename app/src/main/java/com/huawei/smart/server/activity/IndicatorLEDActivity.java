package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.constants.IndicatorState;
import com.huawei.smart.server.redfish.model.Chassis;
import com.huawei.smart.server.widget.LabeledSwitch;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.suke.widget.SwitchButton;

import org.slf4j.LoggerFactory;

import butterknife.BindView;
import okhttp3.Response;

public class IndicatorLEDActivity extends BaseActivity {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(IndicatorLEDActivity.class.getSimpleName());

    boolean initialized = false;

    IndicatorState indicatorState;
    boolean lit = false;
    boolean blinking = false;

    @BindView(R.id.value)
    TextView indicatorValue;

    @BindView(R.id.image)
    ImageView indicatorImage;

    @BindView(R.id.indicator_switch)
    LabeledSwitch indicatorSwitch;

    @BindView(R.id.indicator_blink_switch)
    LabeledSwitch indicatorBlinkSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indicator_led);
        this.initialize(R.string.ds_label_menu_indicator_LED, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupCheckedEvents();
        showLoadingDialog();
        onRefresh(null);
    }


    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        getRedfishClient().chassis().get(RRLB.<Chassis>create(this).callback(
            new RedfishResponseListener.Callback<Chassis>() {
                @Override
                public void onResponse(Response okHttpResponse, Chassis chassis) {
                    indicatorState = chassis.getIndicatorState();
                    updateIndicatorState();
                    finishLoadingViewData(true);
                    initialized = true;
                }
            }).build());
    }

    private void setupCheckedEvents() {
        indicatorSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked ^ lit) {
                    showLoadingDialog();
                    final IndicatorState updated = isChecked ? IndicatorState.Lit : IndicatorState.Off;
                    getRedfishClient().chassis().updateIndicatorState(updated, RRLB.<Chassis>create(IndicatorLEDActivity.this)
                        .callback(new RedfishResponseListener.Callback<Chassis>() {
                                      @Override
                                      public void onResponse(Response okHttpResponse, Chassis chassis) {
                                          indicatorState = chassis.getIndicatorState();
                                          updateIndicatorState();
                                          dismissLoadingDialog();
                                      }

                                      @Override
                                      public void onError(ANError anError) {
                                          super.onError(anError);
                                          IndicatorLEDActivity.this.onRefresh(null);
                                      }
                                  }

                        ).build());
                }
            }
        });


        indicatorBlinkSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (lit) {
                    final IndicatorState updated = isChecked ? IndicatorState.Blinking : IndicatorState.Lit;
                    if (!updated.equals(indicatorState)) {
                        showLoadingDialog();
                        getRedfishClient().chassis().updateIndicatorState(updated, RRLB.<Chassis>create(IndicatorLEDActivity.this)
                            .callback(new RedfishResponseListener.Callback<Chassis>() {
                                @Override
                                public void onResponse(Response okHttpResponse, Chassis chassis) {
                                    indicatorState = chassis.getIndicatorState();
                                    updateIndicatorState();
                                    dismissLoadingDialog();
                                }

                                @Override
                                public void onError(ANError anError) {
                                    super.onError(anError);
                                    IndicatorLEDActivity.this.onRefresh(null);
                                }
                            }).build());
                    }
                }
            }
        });

        indicatorBlinkSwitch.setOnContainerClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!indicatorSwitch.isChecked()) {
                    showToast("请先开启定位灯", Toast.LENGTH_SHORT, Gravity.CENTER);
                }
            }
        });
    }

    /**
     * 更新定位灯状态
     */
    private void updateIndicatorState() {
        indicatorValue.setText(indicatorState.getDisplayResId());
        indicatorImage.setImageDrawable(ContextCompat.getDrawable(this, indicatorState.getIconResId()));
        if (IndicatorState.Off.equals(indicatorState) || IndicatorState.Unknown.equals(indicatorState)) {
            lit = false;
            blinking = false;
            indicatorSwitch.setChecked(false);
            indicatorBlinkSwitch.setEnabled(true);
            indicatorBlinkSwitch.setChecked(false);
            indicatorBlinkSwitch.setEnabled(false);
            indicatorImage.clearAnimation();
        } else {
            lit = true;
            blinking = IndicatorState.Blinking.equals(indicatorState);
            indicatorSwitch.setChecked(true);
            indicatorBlinkSwitch.setEnabled(true);
            indicatorBlinkSwitch.setChecked(blinking);
            if (blinking) {
                Animation startAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blinking_animation);
                indicatorImage.startAnimation(startAnimation);
            } else {
                indicatorImage.clearAnimation();
            }
        }
    }
}
