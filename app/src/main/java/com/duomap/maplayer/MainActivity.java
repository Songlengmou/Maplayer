package com.duomap.maplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.duomap.maplayer.Util.LogUtils;
import com.duomap.maplayer.Util.MediaStoreUtils;
import com.duomap.maplayer.Util.PermissionUtils;
import com.duomap.maplayer.Util.ToastUtils;
import com.duomap.maplayer.myclass.AMapHelper;
import com.duomap.maplayer.myclass.ExitApplication;
import com.duomap.maplayer.myclass.MapNoteDBHelper;
import com.duomap.maplayer.Util.UpdateUtils;
import com.duomap.maplayer.myclass.ToolsClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author admin
 */
public class MainActivity extends Activity
        implements View.OnClickListener {
    private MapView mMapView = null;
    private LinearLayout layoutLeftMenu;
    private Button btnCloseLeftMenu, btnStart, btnSet, btnStop, btnTakePhoto;
    private Button btnMyLies, btnBlogList, btnBlogPublishedList, btnToSetting;
    public static TextView tvPos;
    public static ImageView ivHeadIcon;
    private ImageView ivPhotoShow;
    private LinearLayout llBeforeDuoMap, llAfterDuoMap;

    private RegeocodeAddress nowAddress;

    //从定位点获取的LatLng坐标集，用来在地图上画出折线图；
    public static AMap aMap;
    public static AMapLocationClient locationClient = null;
    public static AMapLocationClientOption locationOption = null;
    public static List<Map<String, Object>> listMarker = new ArrayList<Map<String, Object>>();

    private Intent intentService = null;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//定位一次，且将视角移动到地图中心点。

            aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
            aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
            aMap.getUiSettings().setMyLocationButtonEnabled(true); //设置默认定位按钮是否显示，非必需设置。
            aMap.getUiSettings().setRotateGesturesEnabled(false);
            aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ExitApplication.getInstance().addActivity(this);

        iniView();
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mMapView.getMap();
        }

        mHandler.post(mRunnable);
        iniListener();
    }

    private void iniView() {
        layoutLeftMenu = (LinearLayout) findViewById(R.id.layoutLeftMenu);
        btnCloseLeftMenu = (Button) findViewById(R.id.btnCloseLeftMenu);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnTakePhoto = (Button) findViewById(R.id.btnTakePhoto);
        btnSet = (Button) findViewById(R.id.btnSet);
        tvPos = (TextView) findViewById(R.id.tvPos);

        ivHeadIcon = (ImageView) findViewById(R.id.ivHeadIcon_Main);
        ivPhotoShow = (ImageView) findViewById(R.id.ivPhotoShow);

        btnMyLies = (Button) findViewById(R.id.btnMyLies);
        btnBlogList = (Button) findViewById(R.id.btnBlogList);
        btnBlogPublishedList = (Button) findViewById(R.id.btnBlogPublishedList);
        btnToSetting = (Button) findViewById(R.id.btnToSetting);

        llBeforeDuoMap = (LinearLayout) findViewById(R.id.llBeforeDuomap);
        llAfterDuoMap = (LinearLayout) findViewById(R.id.llAfterDuoMap);

        if (!TextUtils.isEmpty(ToolsClass.FILE_HEADICON)) {
            ivHeadIcon.setImageURI(Uri.parse(ToolsClass.FILE_HEADICON));
        }

        btnStart.setText("开始跺图");

        //判断是否有定位权限；
        if (!PermissionUtils.isPermission(this, PermissionUtils.PERMISSION_LOCATION)) {
            ToastUtils.showShortToast(this, "没有存储权限！");
        }
    }

    private void iniListener() {
        layoutLeftMenu.setOnClickListener(this);

        btnMyLies.setOnClickListener(this);
        btnBlogList.setOnClickListener(this);
        btnBlogPublishedList.setOnClickListener(this);
        btnToSetting.setOnClickListener(this);

        btnCloseLeftMenu.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnTakePhoto.setOnClickListener(this);
        btnSet.setOnClickListener(this);

        ivHeadIcon.setOnClickListener(this);
        ivPhotoShow.setOnClickListener(this);
        aMap.setOnMarkerClickListener(markerClickListener);
        aMap.setOnMapClickListener(mapClickListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivHeadIcon_Main:
                ToolsClass.gotoActivity(MainActivity.this, HeadIconActivity.class);
                break;
            case R.id.btnCloseLeftMenu:
                layoutLeftMenu.setVisibility(View.INVISIBLE);
                break;
            case R.id.btnStop:
                showDialogStop();
                break;
            case R.id.btnSet:
                layoutLeftMenu.setVisibility(View.VISIBLE);
                break;
            case R.id.btnMyLies:
                ToolsClass.gotoActivity(MainActivity.this, MapLineShowActivity.class);
                break;
            //我的旅记
            case R.id.btnBlogList:
                ToolsClass.gotoActivity(MainActivity.this, BlogListShowActivity.class);
                break;
            case R.id.btnBlogPublishedList:
                break;
            case R.id.btnToSetting:
                ToolsClass.gotoActivity(MainActivity.this, SettingActivity.class);
                break;
            case R.id.btnTakePhoto:
                ToolsClass.gotoActivity(MainActivity.this, PhotoTakeActivity.class);
                break;
            case R.id.ivPhotoShow:
                ivPhotoShow.setVisibility(View.INVISIBLE);
                break;
            case R.id.btnStart:
                intentService = new Intent(this, MapNoteService.class);
                this.startService(intentService);
                llBeforeDuoMap.setVisibility(View.INVISIBLE);
                llAfterDuoMap.setVisibility(View.VISIBLE);

                GeocodeSearch geocoderSearch = new GeocodeSearch(MainActivity.this);
                LatLonPoint latLonPoint = AMapHelper.getNowLatLonPoint(aMap);
                RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
                geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                    // 逆地理编码回调
                    @Override
                    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
                        if (rCode == 1000) {
                            nowAddress = result.getRegeocodeAddress();
                        } else {
                        }
                    }

                    @Override
                    public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
                    }
                });
                geocoderSearch.getFromLocationAsyn(query);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        switch (layoutLeftMenu.getVisibility()) {
            case View.VISIBLE:
                layoutLeftMenu.setVisibility(View.GONE);
                break;
            case View.INVISIBLE:
            case View.GONE:
                showDialogExit();
                break;
            default:
                break;
        }
    }

    /**
     * 监听对话框里面的button点击事件
     */
    DialogInterface.OnClickListener listenerExit = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch (i) {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    //销毁所有Activity；
                    ExitApplication.getInstance().exit();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };

    DialogInterface.OnClickListener listenerStop = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch (i) {
                case AlertDialog.BUTTON_POSITIVE:
                    MainActivity.this.stopService(intentService);
                    llBeforeDuoMap.setVisibility(View.VISIBLE);
                    llAfterDuoMap.setVisibility(View.INVISIBLE);

                    // 对地图进行截屏
                    aMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
                        @Override
                        public void onMapScreenShot(Bitmap bitmap) {
                        }

                        @Override
                        public void onMapScreenShot(Bitmap bitmap, int status) {
                            AMapHelper.createMapShot(bitmap, status);
                            AMapHelper.insertMapshotInMapNote(MainActivity.this, MapNoteService.nowLineID);
                        }
                    });

                    //在LineInfo中保存物理地址；
                    String address = getRegion(nowAddress);
                    updateAddress(address);

                    //显示停止跺图后的路线统计信息；
                    AlertDialog stopped = new AlertDialog.Builder(MainActivity.this).create();
                    stopped.setTitle("跺图网提示");
                    stopped.setMessage("已经停止跺图！" + address);
                    stopped.setButton("确定", listenerStopped);
                    stopped.show();
                    break;

                case AlertDialog.BUTTON_NEGATIVE:
                    break;
                default:
                    break;
            }
        }
    };

    DialogInterface.OnClickListener listenerStopped = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
        }
    };

    // 定义 Marker 点击事件监听
    AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {
        // marker 对象被点击时回调的接口
        // 返回 true 则表示接口已响应事件，否则返回false
        @Override
        public boolean onMarkerClick(Marker marker) {
            Map<String, Object> map = null;
            int MapNoteID = 0;
            for (int i = 0; i < listMarker.size(); i++) {
                map = listMarker.get(i);
                if (map.get("markerID").toString() == marker.getId()) {
                    MapNoteID = Integer.parseInt(map.get("MapNoteID").toString());
                }
            }
            String sPicPath = new MapNoteDBHelper(MainActivity.this).getNoteContent(MapNoteID);

            Intent intent = new Intent();
            intent.putExtra("PicPath", sPicPath);
            intent.setClass(MainActivity.this, PicShowActivity.class);
            MainActivity.this.startActivity(intent);
            return false;
        }
    };


    AMap.OnMapClickListener mapClickListener = new AMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            layoutLeftMenu.setVisibility(View.GONE);
        }
    };

    //获取物理位置字符串
    private String getRegion(RegeocodeAddress regeocodeAddress) {
        return regeocodeAddress.getProvince() + regeocodeAddress.getCity() + regeocodeAddress.getDistrict();
    }

    //将物理地址更新到数据库LineInfo中
    private void updateAddress(String address) {
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(MainActivity.this);
        mapNoteDBHelper.updateLineAddress(String.valueOf(MapNoteService.nowLineID), address);
    }

    //按下返回键时，显示是否确认退出APP的Dialog提示；
    private void showDialogExit() {
        // 创建退出对话框
        AlertDialog isExit = new AlertDialog.Builder(this).create();
        // 设置对话框标题
        isExit.setTitle("跺图网提示");
        // 设置对话框消息
        isExit.setMessage("确定要退出吗");
        // 添加选择按钮并注册监听
        isExit.setButton("确定", listenerExit);
        isExit.setButton2("取消", listenerExit);
        // 显示对话框
        isExit.show();
    }

    //按下“停止跺图”按钮是，显示是否确认停止的Dialog提示；
    private void showDialogStop() {
        AlertDialog isStop = new AlertDialog.Builder(this).create();
        isStop.setTitle("跺图网提示");
        isStop.setMessage("确认要停止跺图吗？");
        isStop.setButton("确定", listenerStop);
        isStop.setButton2("取消", listenerStop);
        isStop.show();
    }
}
