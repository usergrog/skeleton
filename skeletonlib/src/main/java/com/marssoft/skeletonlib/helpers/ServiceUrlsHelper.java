package com.marssoft.skeletonlib.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by alexey on 13-Apr-16.
 */
public class ServiceUrlsHelper {
    public static final java.lang.String HTTP_PLAY_URL = "https://play.google.com/store/apps/details?id=";
    public static final String MARKET_PLAY_URL = "market://details?id=";

    public static boolean handleServiceUrl(Context context, String url) {
        if (url.startsWith("tel:")
                || url.startsWith("sms:")
                || url.startsWith("mailto:")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
            return true;
        } else if ((url.startsWith("market:") || url.startsWith(HTTP_PLAY_URL))
                && isGooglePlayInstalled(context)) {
            if (url.startsWith(HTTP_PLAY_URL)) {
                url = MARKET_PLAY_URL + url.substring(HTTP_PLAY_URL.length());
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    private static boolean isPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    private static boolean isGooglePlayInstalled(Context context){
        PackageManager pm = context.getPackageManager();
        boolean appInstalled = false;
        try {
            PackageInfo info = pm.getPackageInfo("com.android.vending", PackageManager.GET_ACTIVITIES);
            String label = (String) info.applicationInfo.loadLabel(pm);
            appInstalled = (!TextUtils.isEmpty(label) && label.startsWith("Google Play"));
        } catch(PackageManager.NameNotFoundException e) {
            appInstalled = false;
        }
        return appInstalled;
    }
}
