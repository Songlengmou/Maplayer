package com.duomap.maplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.duomap.maplayer.myclass.ExitApplication;
import com.duomap.maplayer.Util.ToastUtils;
import com.duomap.maplayer.myclass.ToolsClass;

import java.util.ArrayList;

public class PicShowActivity extends Activity
        implements View.OnClickListener {
    private GestureDetector mGestureDetector;
    private ImageView ivPicShow;
    private Button btnTurnLeft_PicShow, btnTurnRight_PicShow, btnSave_PicShow;
    private String nowPicPathName;
    private ArrayList<String> listPicPath;
    private int pos;

    private float DownX, DownY, moveX, moveY;
    private long currentMS;

    Bitmap bitmapPic;
    int rotate;


    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            bitmapPic = getRotatePic(bitmapPic, rotate);
            ivPicShow.setImageBitmap(bitmapPic);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pic_show);
        ExitApplication.getInstance().addActivity(this);

        Intent intent = getIntent();
        nowPicPathName = intent.getStringExtra("PicPath");
        listPicPath = intent.getStringArrayListExtra("PathList");

        if(null!=listPicPath) {
            pos = getPosInListPath(listPicPath, nowPicPathName);
            //1 初始化  手势识别器
            mGestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
                // e1: 第一次按下的位置   e2   当手离开屏幕 时的位置  velocityX  沿x 轴的速度  velocityY： 沿Y轴方向的速度
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//                //判断竖直方向移动的大小
//                if(Math.abs(e1.getRawY() - e2.getRawY())>100){
//                    //Toast.makeText(getApplicationContext(), "动作不合法", 0).show();
//                    return true;
//                }
                    if(Math.abs(velocityX)<20){
                        //Toast.makeText(getApplicationContext(), "移动的太慢", 0).show();
                        return true;
                    }

                    if((e1.getRawX() - e2.getRawX()) >20){// 表示 向右滑动表示下一页
                        showNextPic();
                        return true;
                    }

                    if((e2.getRawX() - e1.getRawX()) >200){  //向左滑动 表示 上一页
                        showPrePic();
                        return true;//消费掉当前事件  不让当前事件继续向下传递
                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            });
        }


        iniView();
        iniListener();
        showPic();
    }


    //重写activity的触摸事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //2.让手势识别器生效
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private void iniView(){
        ivPicShow = (ImageView) findViewById(R.id.ivPicShow);
        btnTurnLeft_PicShow = (Button) findViewById(R.id.btnTurnLeft_PicShow);
        btnTurnRight_PicShow = (Button) findViewById(R.id.btnTurnRight_PicShow);
        btnSave_PicShow = (Button) findViewById(R.id.btnSave_PicShow);
    }

    private void iniListener(){
//        ivPicShow.setOnClickListener(this);
        btnTurnLeft_PicShow.setOnClickListener(this);
        btnTurnRight_PicShow.setOnClickListener(this);
        btnSave_PicShow.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnTurnLeft_PicShow:
                rotate = 270;
                mHandler.post(mRunnable);
                break;

            case R.id.btnTurnRight_PicShow:
                rotate = 90;
                mHandler.post(mRunnable);
                break;

            case R.id.btnSave_PicShow:
                String picPath = ToolsClass.getPathFromFile(nowPicPathName);
                String picName = ToolsClass.getNameFromFile(nowPicPathName);
                ToolsClass.savePicAndThumb(bitmapPic, picPath, picName);
                ToastUtils.showShortToast(PicShowActivity.this, nowPicPathName);
                break;
        }
    }

    private Bitmap getRotatePic(Bitmap bitmap, int rotate){
        Matrix matrix  = new Matrix();
        matrix.setRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),bitmap.getHeight(), matrix, true);
    }

    //获取前一个Activity点击的图片，在list中所在的位置；
    private int getPosInListPath(ArrayList<String> list, String picPath){
        for(int i=0; i<list.size(); i++){
            String path = list.get(i);
            if(path.equals(picPath)){
                return i;
            }
        }
        return -1;
    }

    private void showPrePic(){
        if(pos==0){
            ToastUtils.showShortToast(this, "已经是第一张图片！");
            return;
        } else {
            pos -= 1;
            nowPicPathName = listPicPath.get(pos);
            showPic();
        }
    }

    private void showNextPic(){
        if(pos==listPicPath.size()-1){
            ToastUtils.showShortToast(this, "已经是最后一张图片！");
            return;
        } else {
            pos += 1;
            nowPicPathName = listPicPath.get(pos);
            showPic();
        }
    }

    private void showPic(){
        ToolsClass toolsClass = new ToolsClass();
        bitmapPic = toolsClass.getLoacalBitmap(nowPicPathName, false);
        ivPicShow.setImageBitmap(bitmapPic);
    }
}
