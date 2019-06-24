package com.duomap.maplayer.myclass;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;

import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.duomap.maplayer.MainActivity;
import com.duomap.maplayer.MapNoteService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017-12-22.
 */

public class AMapHelper {
//    public static String mapShotPath, mapShotPathThumbnail;
    public static ToolsClass.PicInfo picInfo = new ToolsClass.PicInfo();

    /**
     * 默认的连续定位参数
     *
     */
    public AMapLocationClientOption geLocationClientOptionDefault(){
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(5000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(false); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }


    /**
     * 获取当前位置
     *
     */
    public LatLng getNowLatLng(AMap aMap){
        Location tempLocation = aMap.getMyLocation();
        LatLng tempLatLng = new LatLng(tempLocation.getLatitude(), tempLocation.getLongitude());
        return tempLatLng;
    }


    public static LatLonPoint getNowLatLonPoint(AMap aMap){
        Location tempLocation = aMap.getMyLocation();
        LatLonPoint tempLatLonPoint = new LatLonPoint(tempLocation.getLatitude(), tempLocation.getLongitude());
        return tempLatLonPoint;
    }


    /**
     * 移动到指定坐标
     *
     */
    public void moveToCenter(AMap mAMap, int mZoom, LatLng mLatLng){
        mAMap.moveCamera(CameraUpdateFactory.zoomTo(mZoom));
//        final Marker marker = mAMap.addMarker(new MarkerOptions().position(mLatLng).title("My Location").snippet("DefaultMarker"));
        mAMap.moveCamera(CameraUpdateFactory.changeLatLng(mLatLng));
    }

    public static void createMapShot(Bitmap bitmap, int status){
        String sPicName = "shot_" + ToolsClass.getRandomFileName() + ".jpg";
        picInfo = ToolsClass.savePicAndThumb(bitmap, ToolsClass.gMapShotPath, sPicName);
    }

    public static void insertMapshotInMapNote(Context context, int lineId){
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(context);
        mapNoteDBHelper.insertMapNote(lineId, ToolsClass.NOTETYPE_MAPSHOT, picInfo.picFile, picInfo.thumbFile, ToolsClass.getNowDate());
    }

    public static LatLonPoint getLineFirstLatLonPoint(Context context, int lineId){
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(context);
        String sql = "select Latitude, Longitude from DM_LocationRecord where LineI="+lineId+" order by ID";
        Cursor cursor = mapNoteDBHelper.getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return new LatLonPoint(cursor.getDouble(0), cursor.getDouble(1));
    }

    public static LatLonPoint getLineLastLatLonPoint(Context context, int lineId){
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(context);
        String sql = "select Latitude, Longitude from DM_LocationRecord where LineI="+lineId+" order by ID DESC";
        Cursor cursor = mapNoteDBHelper.getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return new LatLonPoint(cursor.getDouble(0), cursor.getDouble(1));
    }

}
