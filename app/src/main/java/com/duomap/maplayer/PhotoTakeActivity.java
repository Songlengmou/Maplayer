package com.duomap.maplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.duomap.maplayer.Util.DialogUtils;
import com.duomap.maplayer.Util.ImageUtils;
import com.duomap.maplayer.myclass.AMapHelper;
import com.duomap.maplayer.myclass.ExitApplication;
import com.duomap.maplayer.myclass.MapNoteDBHelper;
import com.duomap.maplayer.Util.ToastUtils;
import com.duomap.maplayer.myclass.ToolsClass;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class PhotoTakeActivity extends Activity
        implements OnClickListener,Camera.AutoFocusCallback {
    private LinearLayout llBefore, llAfter;
    private SurfaceView sfvCamera;
    private ImageView ivPreView;
    private Button btnTakePhoto, btnPhotoCancel, btnPhotoOK, btnChangeCamera;
    private Camera mCamera;
    private Double nowLat, nowLng;
    private Bitmap gBitmap;
    private int cameraPosition = 1;
    private SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_photo_take);
        ExitApplication.getInstance().addActivity(this);

        /**
         * 窗口布满全局
         */
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        iniView();
        iniListener();
    }

    private void iniView(){
        sfvCamera =(SurfaceView)findViewById(R.id.sfvCamera);

        llBefore = (LinearLayout) findViewById(R.id.llBefore_PhotoTake);
        llAfter = (LinearLayout) findViewById(R.id.llAfter_PhotoTake);
        ivPreView =(ImageView)findViewById(R.id.ivPreView);
        btnTakePhoto =(Button)findViewById(R.id.btnTakePhoto);
        btnPhotoCancel =(Button)findViewById(R.id.btnPhotoCancel);
        btnPhotoOK =(Button)findViewById(R.id.btnPhotoOK);
        btnChangeCamera = (Button) findViewById(R.id.btnChangeCamera_PhotoTake);
    }

    private void iniListener(){
        SurfaceHolder mHolder = sfvCamera.getHolder();
//        sfHolder.setFixedSize(1920, 1080);
        mHolder.setKeepScreenOn(true);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(new TakePictureSurfaceCallback());

        btnTakePhoto.setOnClickListener(this);
        btnPhotoCancel.setOnClickListener(this);
        btnPhotoOK.setOnClickListener(this);
        btnChangeCamera.setOnClickListener(this);
        sfvCamera.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.btnTakePhoto:
                if(mCamera != null){
                    llBefore.setVisibility(View.GONE);
                    llAfter.setVisibility(View.VISIBLE);
                    mCamera.takePicture(null, null , new TakePictureCallback() );

                    AMap aMap = MainActivity.aMap;
                    LatLng mLatLng = new AMapHelper().getNowLatLng(aMap);
                }
                break;

            case R.id.btnChangeCamera_PhotoTake:
                changeCamera();
                break;

            case R.id.btnPhotoCancel:
                llAfter.setVisibility(View.GONE);
                ivPreView.setVisibility(View.GONE);
                llBefore.setVisibility(View.VISIBLE);
                sfvCamera.setVisibility(View.VISIBLE);
                mCamera.startPreview();
                break;

            case R.id.btnPhotoOK:
                DialogUtils.createLoadingDialog(PhotoTakeActivity.this, "正在保存图片...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ivPreView.setDrawingCacheEnabled(true);
                        ivPreView.buildDrawingCache();
//                Bitmap mmbitmap = ivPreView.getDrawingCache();
                        String sPicName = ToolsClass.getRandomFileName() + ".png";

                        ToolsClass.PicInfo picInfo = new ToolsClass.PicInfo();
                        picInfo = ToolsClass.savePicAndThumb(ivPreView.getDrawingCache(), ToolsClass.gPicPath, sPicName);
                        ivPreView.setDrawingCacheEnabled(false);
//                ToastUtils.showShortToast(this, ToolsClass.gPicPath);

//                //生成照片的缩略图；
//                Bitmap bitmapThumbnail = ToolsClass.getImageThumbnail(sFileName, 120, 120);
//                String sFileThumbnailName = ToolsClass.saveBitmapFile(bitmapThumbnail, ToolsClass.gPicThumbnailPath, "thumb_"+sPicName);

                        AMap aMap = MainActivity.aMap;
                        AMapHelper aMapHelper = new AMapHelper();
                        LatLng sLatLng = aMapHelper.getNowLatLng(aMap);
//                Marker marker = aMap.addMarker(new MarkerOptions().position(sLatLng).title("图片").snippet("DefaultMarker"));
                        Marker marker = aMap.addMarker(new MarkerOptions().position(sLatLng));
                        aMapHelper.moveToCenter(aMap, 19, sLatLng);

                        //将照片信息存储到数据库中
                        MapNoteDBHelper dbHelper = new MapNoteDBHelper(PhotoTakeActivity.this);
                        String writeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        dbHelper.insertMapNote(MapNoteService.nowLineID, ToolsClass.NOTETYPE_PIC, picInfo.picFile, picInfo.thumbFile, writeDate);
                        int mapNoteID = dbHelper.getLastMapNoteID();
                        dbHelper.insertMapNoteLatLng(MapNoteService.nowLineID, mapNoteID, ToolsClass.MAPID_AMAP, sLatLng.latitude, sLatLng.longitude, writeDate);

                        //把Marker对应的信息放在MainActivity中的listMarker中；
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("markerID", marker.getId());
                        map.put("MapNoteID", mapNoteID);
                        MainActivity.listMarker.add(map);


//                Toast.makeText(getApplicationContext(), "MarkID: "+marker.getId() +", MapNoteID: "+mapNoteID, Toast.LENGTH_SHORT).show();
                        PhotoTakeActivity.this.finish();
                    }
                }).start();

                break;

            case R.id.sfvCamera:
                mCamera.autoFocus(null);
                break;
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        //要能执行Camera.autoFocus()，必须重写该方法；
    }

    //打开摄像头
    private final class TakePictureSurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            surfaceHolder = holder;
            try {
//                mCamera = Camera.open(); // 打开摄像头
//                mCamera.setPreviewDisplay(holder); //通过surfaceview显示取景画面
//                mCamera.setDisplayOrientation(getPreviewDegree(MainActivity.this));// 设置相机的方向
//                mCamera.startPreview(); // 开始预览



                mCamera = Camera.open();
                if (mCamera == null) {
                    int cametacount = Camera.getNumberOfCameras();
                    mCamera = Camera.open(cametacount - 1);
                }
                setCamera(mCamera);
                //设置预显示
                mCamera.setPreviewDisplay(holder);
                //开启预览
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                //setPreviewCallback(null)和stopPreview()，是防止返回到上一层时报错而闪退；
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }
    }

    private void changeCamera(){
        //切换前后摄像头
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (cameraPosition == 1) {
                //现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    mCamera.stopPreview();//停掉原来摄像头的预览
                    mCamera.release();//释放资源
                    mCamera = null;//取消原来摄像头
                    mCamera = Camera.open(i);//打开当前选中的摄像头
                    setCamera(mCamera);
                    try {
                        mCamera.setPreviewDisplay(surfaceHolder);//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCamera.startPreview();//开始预览
                    cameraPosition = 0;
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    mCamera.stopPreview();//停掉原来摄像头的预览
                    mCamera.release();//释放资源
                    mCamera = null;//取消原来摄像头
                    mCamera = Camera.open(i);//打开当前选中的摄像头
                    setCamera(mCamera);
                    try {
                        mCamera.setPreviewDisplay(surfaceHolder);//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mCamera.startPreview();//开始预览
                    cameraPosition = 1;
                    break;
                }
            }

        }




//        switch (cameraFacing){
//            case Camera.CameraInfo.CAMERA_FACING_FRONT:
//                ToastUtils.showLongToast(this,"front");
//                cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
//                break;
//
//            case Camera.CameraInfo.CAMERA_FACING_BACK:
//                ToastUtils.showLongToast(this,"back");
//                cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
//                break;
//        }
//
//        camera.setPreviewCallback(null);
//        camera.stopPreview();
//        camera.release();
//        camera = null;
//        camera = Camera.open(cameraFacing);
////        setCamera(camera);
//        try {
//            camera.setPreviewDisplay(surfaceHolder);//通过surfaceview显示取景画面
//        } catch (IOException e) {
//            LogUtils.e(e.toString());
//        }
//        camera.startPreview();//开始预览
    }


    private void setCamera(Camera camera){
        Camera.Parameters params = camera.getParameters();
        params.setJpegQuality(100);//照片质量

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        params.setPictureSize(dm.heightPixels, dm.widthPixels);//图片分辨率

        params.setRotation(90);//设置照相生成的图片的方向，
//                params.setPreviewFrameRate(5);//预览帧率
        camera.setParameters(params);
        camera.setDisplayOrientation(90);
    }


    private final class TakePictureCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                ToastUtils.showShortToast(PhotoTakeActivity.this, cameraPosition+"");
                if(cameraPosition==0){
                    bitmap = ImageUtils.rotate(bitmap, 180, 0, 0);
                }
                ivPreView.setVisibility(View.VISIBLE);
//                saveBitmapFile(bitmap);
                ivPreView.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
