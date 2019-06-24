package com.duomap.maplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.duomap.maplayer.Util.PermissionUtils;
import com.duomap.maplayer.Util.ToastUtils;
import com.duomap.maplayer.Util.UpdateUtils;
import com.duomap.maplayer.myclass.ExitApplication;
import com.duomap.maplayer.myclass.ToolsClass;

import java.io.IOException;

public class SettingActivity extends Activity
        implements View.OnClickListener {
    TextView tvVersion;
    Button btnChkPermission, btnSetPermission, btnChkVersion;
    boolean hasNoPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ExitApplication.getInstance().addActivity(this);

        ToolsClass.hasNewVersion = false;

        iniView();
        iniListener();
    }

    private void iniView(){
        tvVersion = (TextView) findViewById(R.id.tvVersion_Setting);
        btnChkVersion = (Button) findViewById(R.id.btnChkVersion_Setting);
        btnChkPermission = (Button) findViewById(R.id.btnChkPermission_Setting);
        btnSetPermission = (Button) findViewById(R.id.btnSetPermission_Setting);

        tvVersion.setText(ToolsClass.APP_VERSION);
    }

    private void iniListener(){
        btnChkVersion.setOnClickListener(this);
        btnChkPermission.setOnClickListener(this);
        btnSetPermission.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.btnChkVersion_Setting:
                UpdateUtils updateUtils = new UpdateUtils(this);
                if(ToolsClass.hasNewVersion){
                    updateUtils.downloadForWebView();
                } else {
                    updateUtils.getVersionServer();
                }
                break;

            case R.id.btnChkPermission_Setting:
                String strPermission = chkHasNoPermission();
                if(TextUtils.isEmpty(strPermission)){
                    ToastUtils.showShortToast(this, "您的权限都已打开！");
                    btnSetPermission.setVisibility(View.GONE);
                } else {
                    ToastUtils.showLongToast(this, strPermission+"权限未打开！请点击『去设置权限』按钮！");
                    btnSetPermission.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.btnSetPermission_Setting:
                PermissionUtils.getAppDetailSettingIntent(this);
                break;
        }
    }


    private String chkHasNoPermission(){
        String strToast="";
        if(!PermissionUtils.isPermission(this, PermissionUtils.PERMISSION_LOCATION)){
            strToast += "[定位]";
        }

        if(!PermissionUtils.isPermission(this, PermissionUtils.PERMISSION_CAMERA)){
            strToast += "[相机]";
        }

        if(!PermissionUtils.isPermission(this, PermissionUtils.PERMISSION_STORAGE)){
            strToast += "[读写手机存储]";
        }
        return strToast;
    }

}
