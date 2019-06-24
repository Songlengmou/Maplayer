package com.duomap.maplayer.Util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import com.duomap.maplayer.myclass.ToolsClass;

/**
 * Created by Administrator on 2018-01-19.
 */

public class PermissionUtils {
    //获取拍照权限；
    public static final String PERMISSION_CAMERA = "android.permission.CAMERA";
    //获取定位权限；
    public static final String PERMISSION_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    //获取读写手机存储权限
    public static final String PERMISSION_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";

    /**
     * 判断是否有定位权限；
     */
    public static boolean isPermission(Context context, String strPermission){
        PackageManager pm = context.getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(strPermission, ToolsClass.APP_PACKAGE_NAME));

        return permission;
    }



    /**
     * 去设置
     */
    public static void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(localIntent);
    }
}
