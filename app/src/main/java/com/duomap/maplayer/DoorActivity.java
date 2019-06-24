package com.duomap.maplayer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.duomap.maplayer.Util.PermissionUtils;
import com.duomap.maplayer.Util.ToastUtils;
import com.duomap.maplayer.myclass.CookiesClass;
import com.duomap.maplayer.myclass.ExitApplication;
import com.duomap.maplayer.myclass.MapNoteDBHelper;
import com.duomap.maplayer.myclass.ToolsClass;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.IOException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.util.TextUtils;

public class DoorActivity extends Activity
        implements View.OnClickListener {
    private TextView tvNotice;
    private Button btnEnter, btnSet;
    private String handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door);
        ExitApplication.getInstance().addActivity(this);

        iniView();
        iniListener();

        String strLocation = "[定位]：\n　　有定位权限，App才能帮您记下您的跺图路线哦！\n\n";
        String showLocation = PermissionUtils.isPermission(this, PermissionUtils.PERMISSION_LOCATION) ? "" : strLocation;

        String strCamera = "[相机]：\n　　在旅途中发现好看的风景，跺图App也能帮您记录下哦！\n\n";
        String showCamera = PermissionUtils.isPermission(this, PermissionUtils.PERMISSION_CAMERA) ? "" : strCamera;

        String strStorage = "[读写手机存储]：\n　　好看的照片当然要存在手机里！需要这个权限哦！\n\n";
        String showStorage = PermissionUtils.isPermission(this, PermissionUtils.PERMISSION_STORAGE) ? "" : strStorage;

        String strNotice = "小提示：\n";
        strNotice += "　　跺图App需要以下权限哦！\n\n";
        strNotice += showLocation + showCamera + showStorage;
        strNotice += "-------华丽丽的分割线-------\n";
        strNotice += "　　跺图App初来乍到，好多手机大佬是将以上权限默认拒绝的，如果发现App不能正常使用，请点击『去设置』按钮，在“权限管理”中设置！";
        tvNotice.setText(strNotice);
    }

    private void iniView(){
        tvNotice = (TextView) findViewById(R.id.tvNotice_Door);
        btnEnter = (Button) findViewById(R.id.btnEnter_Door);
        btnSet = (Button) findViewById(R.id.btnSet_Door);
    }

    private void iniListener(){
        btnEnter.setOnClickListener(this);
        btnSet.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEnter_Door:
                loginAndGetSessionId();
                break;

            case R.id.btnSet_Door:
                PermissionUtils.getAppDetailSettingIntent(this);
                break;
        }
    }

    /**
     * 登录并获取SessionId
     * 普通登录时，服务器要对比UserId和UniqueId，还有WriteDate
     */
    private void loginAndGetSessionId(){
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
//        String urlPath = ToolsClass.WEB_URL_PATH + "app_login_chk.php";
        String urlPath = ToolsClass.WEB_URL_PATH + "app_sign_up.php";
        ToastUtils.showShortToast(this, "dao zheli le ");

        CookiesClass cookiesClass = new CookiesClass();
        cookiesClass.createRandomEppokHandler();
        handler = cookiesClass.createUserIdSecretHandler();

        params.add("eppokhandler", ToolsClass.APP_EPPOK_HANDLER);
        params.add("uniqueid", ToolsClass.USERINFO_UNIQUEID);
        params.add("writedate", ToolsClass.USERINFO_WRITEDATE);
        params.add("raneppokhandler", ToolsClass.APP_RANDOM_EPPOK_HANDLER);
        params.add("version", ToolsClass.APP_VERSION);
        params.add("handler", handler);


        asyncHttpClient.post(urlPath, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                int isCreate = Integer.valueOf(ToolsClass.getJsonString(response, "iscreate"));
                if(isCreate==1){
                    ToolsClass.USERINFO_USERID = ToolsClass.getJsonString(response, "userid");
                    MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(DoorActivity.this);
                    mapNoteDBHelper.updateUserId(ToolsClass.USERINFO_USERID, ToolsClass.USERINFO_UNIQUEID);

                    Toast.makeText(DoorActivity.this, "首次登陆成功！", Toast.LENGTH_SHORT).show();

                    //记录Session，并进入MainActivity；
                    ToolsClass.USERINFO_SESSIONID  = CookiesClass.getSessionIDFromHeaders(headers);
                    Intent intent = new Intent();
                    intent.setClass(DoorActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }




//                //如果本地数据库中的userid是空值的话，还要从服务器上自动生成UserID；
//                new MapNoteDBHelper(DoorActivity.this).updateUserId("UserID", ToolsClass.USERINFO_UNIQUEID);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(DoorActivity.this, "连接不成功，请检查网络！", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
