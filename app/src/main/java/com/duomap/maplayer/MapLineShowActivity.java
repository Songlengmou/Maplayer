package com.duomap.maplayer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.duomap.maplayer.Util.LogUtils;
import com.duomap.maplayer.Util.ToastUtils;
import com.duomap.maplayer.myclass.AMapHelper;
import com.duomap.maplayer.myclass.ExitApplication;
import com.duomap.maplayer.myclass.GradientHelper;
import com.duomap.maplayer.myclass.MapNoteDBHelper;
import com.duomap.maplayer.myclass.ToolsClass;
import com.duomap.maplayer.myclass.WriteNoteHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapLineShowActivity extends Activity
        implements View.OnClickListener {
    private MapView mMapView = null;
    public static AMap aMap;
    GradientHelper mGradientHelper = new GradientHelper(500, Color.argb(255, 135, 81, 168), Color.argb(255, 246, 100, 135));

    private int selLineID=0;
    private ListView lvMyLines;
    private Button btnCloseLeftMenu;
    private Button btnLineList, btnWriteNote, btnMapShot;
    private LinearLayout layoutMyLines;

    private List<Map<String, Object>> listMarker = null;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            final List<Map<String, Object>> list = new MapNoteDBHelper(MapLineShowActivity.this).getMyLineList();
            for(Map<String, Object> map : list){
                String lineId = map.get("lineid").toString();
                MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(MapLineShowActivity.this);
                String iconPath = mapNoteDBHelper.getLineIconPath(Integer.valueOf(lineId));
                Bitmap bitmap = ToolsClass.getLoacalBitmap(iconPath, false);
                map.put("lineicon", bitmap);
                LogUtils.e(iconPath);
            }

//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_20180101);

            SimpleAdapter adapter = new SimpleAdapter(MapLineShowActivity.this, list, R.layout.listview_item_map_line,
                    new String[]{"lineid", "linename", "linetime", "region", "thumbnail", "lineicon"},
                    new int[]{R.id.tvLineID, R.id.tvLineTitle, R.id.tvLineTime, R.id.tvLinePosition, R.id.tvPicPath, R.id.ivLineLogo});

            //用于将Bitmap传入控件
            adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (view instanceof ImageView && data instanceof Bitmap) {
                        ImageView iv = (ImageView) view;
                        iv.setImageBitmap((Bitmap) data);
                        return true;
                    } else
                        return false;
                }
            });

            lvMyLines.setAdapter(adapter);
//            lvMyLines.setSelection(0);

            lvMyLines.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                    aMap.clear();

                    selLineID = Integer.parseInt((String)list.get(position).get("lineid"));

                    Toast.makeText(MapLineShowActivity.this, selLineID+"", Toast.LENGTH_SHORT).show();
                    LatLng sLatLng = showLine(selLineID);
                    showMarker(selLineID);

                    AMapHelper aMapHelper = new AMapHelper();
                    aMapHelper.moveToCenter(aMap, 19, sLatLng);

                    layoutMyLines.setVisibility(View.INVISIBLE);
                    btnWriteNote.setVisibility(View.VISIBLE);
                }
            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_line_show);
        ExitApplication.getInstance().addActivity(this);

        iniView();
        mHandler.post(mRunnable);
        iniListener();

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.maplineshow);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        iniMap();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mHandler.post(mRunnable);
    }

    @Override
    public void onBackPressed() {
        switch (layoutMyLines.getVisibility()){
            case View.VISIBLE:
                if(selLineID==0){
                    aMap = null;
                    finish();
                }else{
                    layoutMyLines.setVisibility(View.GONE);
                }
                break;

            case View.INVISIBLE:
            case View.GONE:
                aMap = null;
                finish();
                break;
        }
    }

    private void iniView(){
        layoutMyLines = (LinearLayout) findViewById(R.id.layoutMyLines);
        btnCloseLeftMenu = (Button) findViewById(R.id.btnCloseLeftMenu);
        btnLineList =(Button) findViewById(R.id.btnLineList);
        btnWriteNote = (Button) findViewById(R.id.btnWriteNote);
        lvMyLines = (ListView) findViewById(R.id.lvMyLines);
        btnMapShot = (Button) findViewById(R.id.btnMapShot);
    }

    private void iniMap(){
        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.setOnMarkerClickListener(markerClickListener);
    }

    private void iniListener(){
        btnCloseLeftMenu.setOnClickListener(this);
        btnLineList.setOnClickListener(this);
        btnWriteNote.setOnClickListener(this);
        btnMapShot.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnCloseLeftMenu:
                layoutMyLines.setVisibility(View.INVISIBLE);
                break;

            case R.id.btnLineList:
                layoutMyLines.setVisibility(View.VISIBLE);
                break;

            case R.id.btnWriteNote:
                Intent intent = new Intent();
                intent.putExtra("BlogInfoID", "0");
                intent.putExtra("LineID", String.valueOf(selLineID));
                intent.setClass(MapLineShowActivity.this, WriteNoteActivity.class);
                MapLineShowActivity.this.startActivity(intent);
                break;

            case R.id.btnMapShot:
                /**
                 * 对地图进行截屏
                 */
                aMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
                    @Override
                    public void onMapScreenShot(Bitmap bitmap) {}
                    @Override
                    public void onMapScreenShot(Bitmap bitmap, int status) {
                        AMapHelper.createMapShot(bitmap, status);
                        AMapHelper.insertMapshotInMapNote(MapLineShowActivity.this, selLineID);
                        ToastUtils.showShortToast(MapLineShowActivity.this, "截取地图成功！");
                    }
                });
                break;
        }
    }


    public LatLng showLine(int lineID){
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(MapLineShowActivity.this);
        List<LatLng> mLatLngs = mapNoteDBHelper.getMyLinePos(lineID);

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.width(12);
        polylineOptions.useGradient(true);
        polylineOptions.color(mGradientHelper.getGradient());
        polylineOptions.zIndex(10);
        aMap.addPolyline(polylineOptions.addAll(mLatLngs));

        return mLatLngs.get(0);
    }

    public void showMarker(int lineID){
        Map<String,Object> map = null;
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(MapLineShowActivity.this);
        listMarker = mapNoteDBHelper.getMyLineMarker(lineID);

        for(int i = 0;i < listMarker.size();i++){
            map = listMarker.get(i);
            LatLng sLatLng = new LatLng(Double.parseDouble(map.get("Latitude").toString()), Double.parseDouble(map.get("Longitude").toString()));
            Marker marker = aMap.addMarker(new MarkerOptions().position(sLatLng));
            map.put("markerID", marker.getId());
        }
    }


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

            String sPicPath = new MapNoteDBHelper(MapLineShowActivity.this).getNoteContent(MapNoteID);
            Toast.makeText(getApplicationContext(), sPicPath, Toast.LENGTH_SHORT).show();

            ArrayList<String> listPath = new ArrayList<String>();
            listPath.add(sPicPath);
            Intent intent = new Intent();
            intent.putExtra("PicPath", sPicPath);
            intent.putStringArrayListExtra("PathList", listPath);
            intent.setClass(MapLineShowActivity.this, PicShowActivity.class);
            MapLineShowActivity.this.startActivity(intent);

            return false;
        }
    };



}
