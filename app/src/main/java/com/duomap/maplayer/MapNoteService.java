package com.duomap.maplayer;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.duomap.maplayer.myclass.AMapHelper;
import com.duomap.maplayer.myclass.GradientHelper;
import com.duomap.maplayer.myclass.MapNoteDBHelper;
import com.duomap.maplayer.myclass.ToolsClass;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapNoteService extends Service {
    public static int nowLineID = 0;

    //从定位点获取的LatLng坐标集，用来在地图上画出折线图；
    List<LatLng> mLatLngs = new ArrayList<LatLng>();

    private TextView tvPos = MainActivity.tvPos;

    private AMap aMap = MainActivity.aMap;
    private AMapLocationClient locationClient = MainActivity.locationClient;
    private AMapLocationClientOption locationOption = MainActivity.locationOption;
    GradientHelper mGradientHelper = new GradientHelper(500, Color.argb(255, 135, 81, 168), Color.argb(255, 246, 100, 135));

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            locationClient.startLocation();
//        //开始添加纠正轨迹；
//        startTrace();
        }
    };

    public MapNoteService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();

        MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(5000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);

        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true); //设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        initLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
//        mapNoteDBHelper.getLastLineID()
        nowLineID = getNowLineID();
        MainActivity.tvPos.setText("nowLineID: "+ nowLineID);
        mHandler.post(mRunnable);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy(){
        locationClient.stopLocation();
        super.onDestroy();
    }



    /**
     * 初始化定位
     * @since 2.8.0
     * @author hongming.wang
     */
    private void initLocation(){
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = new AMapHelper().geLocationClientOptionDefault();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }


//    /**
//     * 开始定位
//     *
//     * @since 2.8.0
//     * @author hongming.wang
//     *
//     */
//    private void startLocation(){
//        //根据控件的选择，重新设置定位参数
//        //getDefaultOption();
//        // 设置定位参数
//        locationClient.setLocationOption(locationOption);
//        // 启动定位
//        locationClient.startLocation();
//    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
                StringBuffer sb = new StringBuffer();
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if(location.getErrorCode() == 0) {
                    sb.append("经度:" + location.getLongitude() + "   ");
                    sb.append("纬度: " + location.getLatitude());

                    //以下为自定义的；
                    //根据获得的点，在地图上画出折线；
                    List<LatLng> tempLatLngs = new ArrayList<LatLng>();
                    if(mLatLngs.size()>1){
                        tempLatLngs.add(mLatLngs.get(mLatLngs.size()-1-1));
                    }
                    tempLatLngs.add(new LatLng(location.getLatitude(),location.getLongitude()));
                    mLatLngs.add(new LatLng(location.getLatitude(),location.getLongitude()));
                    //aMap.addPolyline(new PolylineOptions().addAll(mLatLngs).width(10).color(Color.argb(255, 1, 1, 1)));

                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.width(12);
                    polylineOptions.useGradient(true);
//                    polylineOptions.color(Color.argb(255, 255, 1, 1));
                    polylineOptions.color(mGradientHelper.getGradient());
                    polylineOptions.zIndex(10);
                    aMap.addPolyline(polylineOptions.addAll(tempLatLngs));

                    //将新的定位点记录到数据库中；
                    MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(MapNoteService.this);
                    String writeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    mapNoteDBHelper.insertLocationRecord(nowLineID, ToolsClass.MAPID_AMAP, location.getLatitude(), location.getLongitude(), writeDate);

                }else {
                    //定位失败
                    sb.append("错误码:" + location.getErrorCode() );
                }
                tvPos.setText(sb.toString());

            } else {
                tvPos.setText("定位失败，loc is null");
            }
        }
    };

    private int getNowLineID(){
        //将新的路线信息记录到数据库中；
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(MapNoteService.this);
        String writeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String lineName = "New Line" + writeDate;
        mapNoteDBHelper.insertNewLine(lineName, writeDate, null, null);

        return mapNoteDBHelper.getLastLineID();
//        return 0;
    }
}
