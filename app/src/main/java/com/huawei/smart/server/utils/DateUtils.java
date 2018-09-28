package com.huawei.smart.server.utils;

import android.text.TextUtils;

public class DateUtils {

    /**
     * convert 2018-07-10T20:39:30+07:00 -> 2018-07-10 20:39:30
     *
     * @param date
     * @return
     */
    public static String displayWithoutTimezone(String date) {
        if (!TextUtils.isEmpty(date)) {
            date = date.substring(0, 19);
            return date.replace("T", " ");
        }
        return null;
    }

}
