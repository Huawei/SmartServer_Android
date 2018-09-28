package com.huawei.smart.server.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.BootSettingActivity;
import com.huawei.smart.server.activity.CollectionActivity;
import com.huawei.smart.server.activity.DeviceSummaryActivity;
import com.huawei.smart.server.activity.FirmwareActivity;
import com.huawei.smart.server.activity.HardwareActivity;
import com.huawei.smart.server.activity.HealthEventActivity;
import com.huawei.smart.server.activity.IndicatorLEDActivity;
import com.huawei.smart.server.activity.LocationActivity;
import com.huawei.smart.server.activity.NetworkSettingActivity;
import com.huawei.smart.server.activity.PowerConsumptionActivity;
import com.huawei.smart.server.activity.PowerToggleActivity;
import com.huawei.smart.server.activity.RealtimeStateActivity;
import com.huawei.smart.server.activity.GenerateReportActivity;
import com.huawei.smart.server.utils.BundleBuilder;
import com.huawei.smart.server.widget.BadgeIndicatorView;

import java.util.ArrayList;
import java.util.List;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_ID;

public class DeviceSummaryMenuListAdapter extends RecyclerView.Adapter<DeviceSummaryMenuListAdapter.MenuItemViewHolder> {

    public static final int ITEM_VIEW_TYPE_MENU = 1;
    public static final int ITEM_VIEW_TYPE_ITEM = 2;
    public static final int ITEM_VIEW_TYPE_SECTION = 3;

    private List<MenuItem> menuItems = new ArrayList<>();
    private List<MenuItem> expendMenuList;
    private List<MenuItem> foldMenuList;

    private DeviceSummaryActivity mContext;

    public DeviceSummaryMenuListAdapter(DeviceSummaryActivity context) {
        expendMenuList = MenuItem.getMenuList(false);
        foldMenuList = MenuItem.getMenuList(true);
        menuItems.addAll(expendMenuList);
        mContext = context;
    }

    @Override
    public MenuItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final View inflated = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.segment_device_summary_menu_item, null);
        return new MenuItemViewHolder(inflated);
    }

    @Override
    public void onBindViewHolder(MenuItemViewHolder holder, int position) {
        holder.initialize(menuItems.get(position));
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public enum MenuItem {

        HealthEvent(R.string.ds_label_menu_health, R.mipmap.ic_healthy_event, HealthEventActivity.class, false),
        //SystemLog(R.string.ds_label_menu_system_log, R.mipmap.ic_log, SystemLogActivity.class, false),
        Network(R.string.ds_label_menu_network_setting, R.mipmap.ic_network_setting, NetworkSettingActivity.class, false),
        Hardware(R.string.ds_label_menu_hardware, R.mipmap.ic_hardware, HardwareActivity.class, false),
        RealtimeState(R.string.ds_label_menu_realtime_state, R.mipmap.ic_realtime_state, RealtimeStateActivity.class, false),
        Location(R.string.ds_label_menu_location, R.mipmap.ic_location, LocationActivity.class, false),
        PowerConsumption(R.string.ds_label_menu_power_consumption, R.mipmap.ic_power_consumption, PowerConsumptionActivity.class, false),
        BootSetting(R.string.ds_label_menu_boot_setting, R.mipmap.ic_start_setting, BootSettingActivity.class, false),
        Expend(R.string.ds_label_menu_expend, R.mipmap.ic_more, null, false),
        Power(R.string.ds_label_menu_power, R.mipmap.ic_power_toggle, PowerToggleActivity.class, true),
        IndicatorLED(R.string.ds_label_menu_indicator_LED, R.mipmap.ic_locate_lamp, IndicatorLEDActivity.class, true),
        Firmware(R.string.ds_label_menu_firmware, R.mipmap.ic_firmware, FirmwareActivity.class, true),
        Report(R.string.ds_label_menu_report, R.mipmap.ic_report, GenerateReportActivity.class, true),
        Collector(R.string.ds_label_menu_collector, R.mipmap.ic_collection, CollectionActivity.class, true),
        Folder(R.string.ds_label_menu_folder, R.mipmap.ic_folder, null, true);


        int iconResId;
        int labelTxtResId;
        Class<? extends Activity> activityClass;
        boolean folded;

        MenuItem(int labelTxtResId, int iconResId, Class<? extends Activity> activityClass, boolean folded) {
            this.iconResId = iconResId;
            this.labelTxtResId = labelTxtResId;
            this.activityClass = activityClass;
            this.folded = folded;
        }

        public static List<MenuItem> getMenuList(boolean folded) {
            List<MenuItem> menus = new ArrayList<>();
            for (MenuItem menu : MenuItem.values()) {
                if (menu.isFolded() == folded) {
                    menus.add(menu);
                }
            }
            return menus;
        }

        public int getIconResId() {
            return iconResId;
        }

        public int getLabelTxtResId() {
            return labelTxtResId;
        }

        public Class<? extends Activity> getActivityClass() {
            return activityClass;
        }

        public boolean isFolded() {
            return folded;
        }
    }

    public class MenuItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView label;
        private ImageView icon;
        private LinearLayout layout;

        private MenuItemViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            icon = itemView.findViewById(R.id.icon);
            layout = itemView.findViewById(R.id.container);
            layout.setOnClickListener(this);
        }

        public TextView getLabel() {
            return label;
        }

        public ImageView getIcon() {
            return icon;
        }

        public void initialize(MenuItem menu) {
            icon.setBackgroundResource(menu.getIconResId());
            label.setText(menu.getLabelTxtResId());
        }

        public void setBadge(int badgeValue) {
            BadgeIndicatorView badge = new BadgeIndicatorView(mContext);
            badge.setTargetView(icon);
            badge.setBadgeCount(badgeValue);
        }

        @Override
        public void onClick(View view) {
            final MenuItem menu = menuItems.get(getAdapterPosition());
            switch (menu) {
                case Expend:
                    menuItems.clear();
                    menuItems.addAll(expendMenuList);
                    menuItems.remove(MenuItem.Expend);
                    menuItems.addAll(foldMenuList);
                    notifyDataSetChanged();
                    break;
                case Folder:
                    menuItems.clear();
                    menuItems.addAll(expendMenuList);
                    notifyDataSetChanged();
                    break;
                default:
                    if (menu.getActivityClass() != null) {
                        Bundle bundle = BundleBuilder.instance().with(BUNDLE_KEY_DEVICE_ID, mContext.getDevice().getId()).build();
                        ActivityUtils.startActivity(bundle, menu.getActivityClass());
                    }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }


    }

}
