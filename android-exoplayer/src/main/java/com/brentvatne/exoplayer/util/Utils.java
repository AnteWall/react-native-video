package com.brentvatne.exoplayer.util;

import android.text.TextUtils;

public class Utils {

    private Utils() {
    }

    public static String removeScheme(String url){
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return url.replace("http://", "").replace("https://", "");
    }
}