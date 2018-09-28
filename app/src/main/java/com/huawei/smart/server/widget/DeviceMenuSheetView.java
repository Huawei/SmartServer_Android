package com.huawei.smart.server.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.HWApplication;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.DeviceSummaryActivity;
import com.huawei.smart.server.model.Device;
import com.huawei.smart.server.utils.DensityUtils;
import com.huawei.smart.server.utils.WidgetUtils;

import org.slf4j.LoggerFactory;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_ID;
import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_PASSWORD;

public class DeviceMenuSheetView extends LinearLayout {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(HWApplication.class.getSimpleName());

    private final int originalListPaddingTop;

    TextView titleView, emptyView;
    EnhanceRecyclerView listView;
    SwitchableDeviceListAdapter adapter;

    public DeviceMenuSheetView(final Context context, @StringRes final int titleRes) {
        super(context);
        inflate(context, R.layout.sheet_switchable_device, this);

        titleView = findViewById(R.id.title);
        emptyView = findViewById(R.id.empty);

        listView = findViewById(R.id.list);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.addItemDecoration(WidgetUtils.newDividerItemDecoration(context));

        originalListPaddingTop = listView.getPaddingTop();
        setTitle(context.getResources().getString(titleRes));

        ViewCompat.setElevation(this, DensityUtils.dip2px(getContext(), 16f));
    }

    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            titleView.setText(title);
        } else {
            titleView.setVisibility(GONE);
            // Add some padding to the top to account for the missing title
            final int paddingTop = originalListPaddingTop + DensityUtils.dip2px(getContext(), 8f);
            listView.setPadding(listView.getPaddingLeft(), paddingTop, listView.getPaddingRight(), listView.getPaddingBottom());
        }
    }

    public void setAdapter(SwitchableDeviceListAdapter adapter) {
        this.adapter = adapter;
        this.listView.setAdapter(adapter);
        this.listView.setEmptyView(emptyView);
        adapter.notifyDataSetChanged();
    }


    /**
     * 切换设备菜单栏
     */
    public static class SwitchableDeviceListAdapter extends RealmRecyclerViewAdapter<Device, SwitchableDeviceListAdapter.ViewHolder> {

        static final String LOG_TAG = "Device List";

        private final Realm realm;
        private Context context;
        private OrderedRealmCollection<Device> mList;

        public SwitchableDeviceListAdapter(Context context, Realm realm, OrderedRealmCollection<Device> devices) {
            super(devices, true, true);
            this.context = context;
            this.realm = realm;
            this.mList = devices;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(context, R.layout.list_switchable_device_item, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.itemView.setTag(position);
            Device device = mList.get(position);
            holder.label.setText(device.getAlias() + "(" + device.getHostname() + ")");
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

            TextView label;
            ImageView icon;

            ViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.icon);
                label = itemView.findViewById(R.id.label);

                label.setOnClickListener(this);
                label.setOnLongClickListener(this);
                icon.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                final Device device = mList.get(getAdapterPosition());
                final Bundle bundle = new Bundle();
                bundle.putString(BUNDLE_KEY_DEVICE_ID, device.getId());
                switch (view.getId()) {
                    case R.id.label:
                        if (!device.getRememberPwd() || TextUtils.isEmpty(device.getPassword())) {
                            new MaterialDialog.Builder(context)
                                .content(device.getAlias() + "(" + device.getHostname() + ")")
                                .inputRangeRes(1, -1, R.color.colorComment)
                                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                                .input(R.string.ds_dialog_input_device_pwd_hint, 0, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                        bundle.putString(BUNDLE_KEY_DEVICE_PASSWORD, input.toString());
                                        ActivityUtils.finishActivity(DeviceSummaryActivity.class);
                                        ActivityUtils.startActivity(bundle, DeviceSummaryActivity.class);
                                    }
                                }).show();
                        } else {
                            ActivityUtils.finishActivity(DeviceSummaryActivity.class);
                            ActivityUtils.startActivity(bundle, DeviceSummaryActivity.class);
                        }
                        break;
                    case R.id.icon:
                        LOG.info("Remove switchable device: {}", device.getHostname());
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                device.setSwitchable(false);
                                realm.copyToRealmOrUpdate(device);
                            }
                        });
                        notifyDataSetChanged();
                        LOG.info("Remove switchable device successfully", device.getHostname());
                        break;
                    default:
                        break;

                }
            }

            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        }
    }

}