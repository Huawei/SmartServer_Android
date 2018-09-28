package com.huawei.smart.server;

import android.content.Context;
import android.content.SharedPreferences;

public class HWConstants {

    public static final int WRITE_STORAGE_REQUEST = 1;

    public static String KEY_UPGRADE_CHECK_URL = "UpgradeCheckUrl";

    public static int screenWidth = 480;
    public static int screenHeight = 800;
    public static float density = 1.0f;

    public static Integer USER_PREFERENCE_ID = 1;
    public static String DFT_NULL_VALUE = "N/A";



    /**
     * bundle keys start
     */
    public static String BUNDLE_KEY_DEVICE = "Device";
    public static String BUNDLE_KEY_DEVICE_ID = "DeviceId";
    public static String BUNDLE_KEY_DEVICE_PASSWORD = "DevicePassword";
    public static String BUNDLE_KEY_NETWORK_PORT_LIST = "NetworkPortList";
    public static String BUNDLE_KEY_DRIVE_LIST = "DriveList";
    public static String BUNDLE_KEY_VOLUME_ID = "VolumeId";
    public static String BUNDLE_KEY_SEARCH_KEYWORD = "SearchKeyWord";
    public static String BUNDLE_KEY_REPORT_HTML = "SearchKeyWord";
    /**
     * bundle keys end
     */


    public static String DEFAULT_DEVICE_ALIAS = "设备";


    public static int DEVICE_LIST_POSITION = 0;
    public static int DISCOVERY_POSITION = 1;
    public static int MY_PROFILE_POSITION = 2;
    public static String POSITION = "Position";

    /**
     * StartActivity for result
     */
    public static int START_FOR_RESULT_FOR_DEVICE_SEARCH = 1;

    public static String PREFERENCE_SETTINGS = "settings";
    public static String PREFERENCE_SETTING_THEME = "Theme";
    public static String PREFERENCE_SETTING_LANG = "Language";
    public static String LANG_ZH = "zh";


    public static String THEME_DARK = "dark";
    public static String THEME_GREEN = "green";
    public static String THEME;

    public static String INTENT_KEY_FEEDBACK_DETAIL_TITLE = "INTENT_KEY_FEEDBACK_DETAIL_TITLE";
    public static String INTENT_KEY_FEEDBACK_DETAIL_TITLE_STRING = "INTENT_KEY_FEEDBACK_DETAIL_TITLE_STRING";
    public static String INTENT_KEY_FEEDBACK_DETAIL_CONTENT_URL = "INTENT_KEY_FEEDBACK_DETAIL_CONTENT_URL";

    public static String getTheme(Context context) {
        if (THEME == null) {
            SharedPreferences preferences = context.getSharedPreferences(HWConstants.PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
            THEME = preferences.getString(HWConstants.PREFERENCE_SETTING_THEME, HWConstants.THEME_DARK);
        }
        return THEME;
    }

}
