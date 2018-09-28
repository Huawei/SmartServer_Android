package com.huawei.smart.server.lock;

import java.security.MessageDigest;
import java.util.Locale;

import android.text.TextUtils;

public class Encryptor {

    private static String bytes2Hex(byte[] bytes) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (byte aByte : bytes) {
            stmp = (Integer.toHexString(aByte & 0XFF));
            if (stmp.length() == 1) {
                hs.append("0").append(stmp);
            } else {
                hs.append(stmp);
            }
        }
        return hs.toString().toLowerCase(Locale.ENGLISH);
    }

    public static String getSHA1(String text) {
        String sha1 = null;
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        MessageDigest sha1Digest;
        try {
            sha1Digest = MessageDigest.getInstance("SHA-1");
        } catch (Exception e) {
            return null;
        }
        byte[] textBytes = text.getBytes();
        sha1Digest.update(textBytes, 0, text.length());
        byte[] sha1hash = sha1Digest.digest();
        return bytes2Hex(sha1hash);
    }
}
