package com.huawei.smart.server.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.AddDeviceActivity;
import com.huawei.smart.server.activity.DeviceSummaryActivity;
import com.huawei.smart.server.model.Device;
import com.huawei.smart.server.widget.BadgeIndicatorView;

import org.slf4j.LoggerFactory;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmRecyclerViewAdapter;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_ID;
import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_PASSWORD;

public class DeviceListAdapter extends RealmRecyclerViewAdapter<Device, DeviceListAdapter.ViewHolder> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DeviceListAdapter.class.getSimpleName());

    private final Realm realm;
    private Context context;
    private OrderedRealmCollection<Device> mList;

    public DeviceListAdapter(Context context, Realm realm, OrderedRealmCollection<Device> mList) {
        super(mList, true, true);
        this.context = context;
        this.realm = realm;
        this.mList = mList;
    }

    public void updateItems(OrderedRealmCollection<Device> items) {
        this.mList = items;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.list_device_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        Device deviceInfo = mList.get(position);
        holder.name.setText(deviceInfo.getAlias());
        holder.label.setText(deviceInfo.getHostname());
        holder.indicatorCount.setBadgeCount(deviceInfo.getWarning() == null ? 0 : deviceInfo.getWarning());
        holder.deviceTypeBtn.setImageResource(Device.ConnectType.from(deviceInfo.getConnectType()).getIconResId());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView deviceTypeBtn;
        TextView name, label;
        BadgeIndicatorView indicatorCount;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            label = itemView.findViewById(R.id.label);
            indicatorCount = itemView.findViewById(R.id.indicator_counter);
            deviceTypeBtn = itemView.findViewById(R.id.device_type);

            View main = itemView.findViewById(R.id.main);
            main.setOnClickListener(this);
            main.setOnLongClickListener(this);

            itemView.findViewById(R.id.delete_button).setOnClickListener(this);
            itemView.findViewById(R.id.edit_button).setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            final Device device = mList.get(getAdapterPosition());
            final Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_KEY_DEVICE_ID, device.getId());
            switch (view.getId()) {
                case R.id.main:
                    if (!device.getRememberPwd() || TextUtils.isEmpty(device.getPassword())) {
                        new MaterialDialog.Builder(context)
                            .content(device.getAlias() + "(" + device.getHostname() + ")")
                            .inputRangeRes(1, -1, R.color.colorComment)
                            .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                            .negativeText(R.string.button_cancel)
                            .positiveText(R.string.button_sure)
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
                    break;
                case R.id.delete_button:
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            final String hostname = device.getHostname();
                            RealmObject.deleteFromRealm(device);
                            LOG.info("Delete Device {} done", hostname);
                        }
                    });
                    notifyDataSetChanged();

                    break;
                case R.id.edit_button:
                    ActivityUtils.startActivity(bundle, AddDeviceActivity.class);
                    break;

            }
        }


        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    }

}