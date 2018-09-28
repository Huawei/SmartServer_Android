package com.huawei.smart.server.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.AboutUsActivity;
import com.huawei.smart.server.activity.FeedbackActivity;
import com.huawei.smart.server.activity.LanguageActivity;
import com.huawei.smart.server.activity.SecurityActivity;
import com.huawei.smart.server.activity.ThemeActivity;
import com.huawei.smart.server.model.Preference;
import com.suke.widget.SwitchButton;

import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.Realm;

import static com.huawei.smart.server.HWConstants.USER_PREFERENCE_ID;


public class ProfileFragment extends BaseFragment {

//    @BindView(R.id.event_push_switch)
//    SwitchButton eventPushSwitch;

    Preference preference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_profile, container, false);
        this.initialize(view, R.string.title_my_profile, false);
        initializeView();
        return view;
    }

    private void initializeView() {
        final Realm realm = getDefaultRealmInstance();
        preference = realm.where(Preference.class).equalTo("id", USER_PREFERENCE_ID).findFirst();

        /**
        eventPushSwitch.setChecked(Objects.requireNonNull(preference).getAcceptEventPush());
        eventPushSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, final boolean isChecked) {
                getDefaultRealmInstance().executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(@NonNull Realm realm) {
                        preference.setAcceptEventPush(isChecked);
                        realm.copyToRealmOrUpdate(preference);
                    }
                });
            }
        });*/
    }

    @OnClick(R.id.about_us_item)
    public void onAboutUsItemClicked() {
        ActivityUtils.startActivity(AboutUsActivity.class);
    }

    @OnClick(R.id.feedback_item)
    public void onFeedbackItemClicked() {
        ActivityUtils.startActivity(FeedbackActivity.class);
    }

    @OnClick(R.id.lock_item)
    public void onLockItemClicked() {
        ActivityUtils.startActivity(SecurityActivity.class);
    }

    @OnClick(R.id.theme_item)
    public void onThemeItemClicked() {
        ActivityUtils.startActivity(ThemeActivity.class);
    }

    @OnClick(R.id.lang_item)
    public void onLangItemClicked() {
        ActivityUtils.startActivity(LanguageActivity.class);
    }
}
