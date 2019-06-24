package com.duomap.maplayer;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.duomap.maplayer.myclass.CookiesClass;
import com.duomap.maplayer.myclass.ExitApplication;
import com.duomap.maplayer.myclass.MapNoteDBHelper;
import com.duomap.maplayer.Util.ToastUtils;
import com.duomap.maplayer.myclass.ToolsClass;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;


public class WelcomeActivity extends Activity {
    private GestureDetector mGestureDetector;
    boolean isPass = false;

    String urlLogin = ToolsClass.WEB_URL_PATH + "app_login_chk.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ExitApplication.getInstance().addActivity(this);

        isPass = chkUserIsOld();
        new Thread(){
            public void run() {
                //停顿1秒后，跳转；
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
        gotoNextActivity();

        //1 初始化  手势识别器
        mGestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {// e1: 第一次按下的位置   e2   当手离开屏幕 时的位置  velocityX  沿x 轴的速度  velocityY： 沿Y轴方向的速度
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
                    gotoNextActivity();

                    return true;
                }

//                if((e2.getRawX() - e1.getRawX()) >200){  //向左滑动 表示 上一页
//                    return true;//消费掉当前事件  不让当前事件继续向下传递
//                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    //重写activity的触摸事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //2.让手势识别器生效
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private boolean chkUserIsOld(){
        boolean isPass = true;
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(this);
        String sql = "select ID, UserID from DM_UserInfo Order by ID";
        Cursor cursor = mapNoteDBHelper.getReadableDatabase().rawQuery(sql, null);
        Boolean isExsit = cursor.moveToNext();
        if(!isExsit){
            ContentValues cv = new ContentValues();
            cv.put("UniqueID", ToolsClass.getUniqueId(WelcomeActivity.this));
            cv.put("LoginDateLocal", ToolsClass.getNowDate());
            cv.put("WriteDate", ToolsClass.getNowDate());
            mapNoteDBHelper.getWritableDatabase().insert("DM_UserInfo", null, cv);
            isPass = false;
        }else{
            int userInfoId =  cursor.getInt(0);
            String userId = cursor.getString(1);
            if(TextUtils.isEmpty(userId)){
                ContentValues cv =  new ContentValues();
                String[] args = {String.valueOf(userInfoId)};
                cv.put("UniqueID", ToolsClass.getUniqueId(WelcomeActivity.this));
                cv.put("LoginDateLocal", ToolsClass.getNowDate());
                cv.put("WriteDate", ToolsClass.getNowDate());
                mapNoteDBHelper.getWritableDatabase().update("DM_UserInfo", cv, "ID=?", args);
                isPass = false;
            }
        }

        ToolsClass.USERINFO_ID = mapNoteDBHelper.getFirstID("DM_UserInfo");
        ToolsClass.USERINFO_USERID = mapNoteDBHelper.getStringFromID("DM_UserInfo", "UserID", ToolsClass.USERINFO_ID);
        ToolsClass.USERINFO_UNIQUEID = mapNoteDBHelper.getStringFromID("DM_UserInfo", "UniqueID", ToolsClass.USERINFO_ID);
        ToolsClass.USERINFO_WRITEDATE = mapNoteDBHelper.getStringFromID("DM_UserInfo", "WriteDate", ToolsClass.USERINFO_ID);
        ToolsClass.FILE_HEADICON = mapNoteDBHelper.getStringFromString("DM_UserInfo", "HeadIcon", " where ID="+ ToolsClass.USERINFO_ID);

        return isPass;
    }

    private void gotoNextActivity(){
        if(isPass){
            login();
        }else{
            Intent intent = new Intent();
            intent.setClass(WelcomeActivity.this, DoorActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void login(){
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("id1", ToolsClass.USERINFO_USERID);
        params.add("id2", ToolsClass.USERINFO_UNIQUEID);

        asyncHttpClient.post(urlLogin, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                int isPassServer = Integer.valueOf(ToolsClass.getJsonInt(response, "ispass"));
                if(isPassServer==1){
                    ToolsClass.USERINFO_SESSIONID  = CookiesClass.getSessionIDFromHeaders(headers);
                    ToastUtils.showShortToast(WelcomeActivity.this, "登录成功！");

                    Intent intent = new Intent();
                    intent.setClass(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    ToastUtils.showShortToast(WelcomeActivity.this, "您的用户信息有误！");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });


//        intent.setClass(WelcomeActivity.this, MainActivity.class);
    }

}
