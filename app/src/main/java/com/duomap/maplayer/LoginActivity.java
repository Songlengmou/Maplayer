package com.duomap.maplayer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.duomap.maplayer.myclass.ExitApplication;
import com.duomap.maplayer.myclass.MapNoteDBHelper;
import com.duomap.maplayer.myclass.ToolsClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;



public class LoginActivity extends Activity
        implements View.OnClickListener {
    private LinearLayout llUserIdArea, llPwdArea, llPwdComfirmArea, llLoginBtnArea, llSignupBtnArea;
    private EditText txtUserName, txtPWD, txtPWDComfirm;
    private Button btnLogin, btnSign, btnSignComfirm, btnCancelSign;
    private TextView tvPwd, tvShowTest;

//    private int isLogin = 1; //1为登录，0为注册；
    String userId, pwd, loginDateOnline, nickName;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int iChkUser = chkUser();
            switch (iChkUser){
                case 1:
                case 2:
                    llUserIdArea.setVisibility(View.VISIBLE);
                    tvShowTest.setText("您是新安装用户，如果您有绑定Email的账户，请直接输入Email和密码进行登录！如果没有，请点击注册新用户！");
                    break;

                case 3:
                    llUserIdArea.setVisibility(View.GONE);
                    tvShowTest.setText("您离上次登录时间已超过7天，请直接输入密码重新登陆！");
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ExitApplication.getInstance().addActivity(this);

        iniView();
        iniListener();
        mHandler.post(mRunnable);
    }

    private void iniView(){
        llUserIdArea = (LinearLayout) findViewById(R.id.llUserIdArea);
        llPwdArea = (LinearLayout) findViewById(R.id.llPwdArea);
        llPwdComfirmArea = (LinearLayout) findViewById(R.id.llPwdComfirmArea);
        llLoginBtnArea = (LinearLayout) findViewById(R.id.llLoginBtnArea);
        llSignupBtnArea = (LinearLayout) findViewById(R.id.llSignupBtnArea);
        txtUserName = (EditText) findViewById(R.id.txtUserName);
        txtPWD = (EditText) findViewById(R.id.txtPWD);
        txtPWDComfirm = (EditText) findViewById(R.id.txtPWDComfirm);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnSign = (Button) findViewById(R.id.btnSign);
        btnSignComfirm = (Button) findViewById(R.id.btnSignComfirm);
        btnCancelSign = (Button) findViewById(R.id.btnCancelSign);
        tvPwd = (TextView) findViewById(R.id.tvPwd);
        tvShowTest = (TextView) findViewById(R.id.tvShowTest);
    }

    private void iniListener(){
        btnLogin.setOnClickListener(this);
        btnSign.setOnClickListener(this);
        btnSignComfirm.setOnClickListener(this);
        btnCancelSign.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.btnLogin:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int result=0;
                        try {
                            result = login();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if(result==1) {
                            Intent intent = new Intent();
                            intent.setClass(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                }).start();
                break;

            case R.id.btnSign:
                boolean hasUserId = getHasUserId();

                if(!hasUserId) {
                    tvPwd.setText("密　　码：");
                    llUserIdArea.setVisibility(View.GONE);
                    llPwdComfirmArea.setVisibility(View.VISIBLE);

                    llLoginBtnArea.setVisibility(View.GONE);
                    llSignupBtnArea.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(LoginActivity.this, "您好！您的用户名已存在，现在还不能注册新的用户名！", Toast.LENGTH_SHORT).show();
                }
//                tvShowTest.setText((ToolsClass.getAndroidID(LoginActivity.this)+"\n"+ToolsClass.getSN()+"\n"+ToolsClass.getUniqueId(LoginActivity.this)).toLowerCase());
                break;

            case R.id.btnSignComfirm:
                try {
                    sign();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btnCancelSign:
                tvPwd.setText("密　码：");
                llUserIdArea.setVisibility(View.VISIBLE);
                llPwdComfirmArea.setVisibility(View.GONE);

                llLoginBtnArea.setVisibility(View.VISIBLE);
                llSignupBtnArea.setVisibility(View.GONE);
                break;
        }
    }

    private int login() throws IOException {
        String sUserName = txtUserName.getText().toString();
        String sUserPwd = txtPWD.getText().toString();

        if(sUserName==null || sUserName.length()<=0){
            Looper.prepare();
            Toast.makeText(LoginActivity.this, "Please input UserName!", Toast.LENGTH_SHORT).show();
            Looper.loop();
            return 0;
        }

        if(sUserPwd==null || sUserPwd.length()<=0){
            Looper.prepare();
            Toast.makeText(LoginActivity.this, "Please input password!", Toast.LENGTH_SHORT).show();
            Looper.loop();
            return 0;
        }

        //建立网络连接
        String urlstr= ToolsClass.WEB_URL_PATH + "app_login_chk.php";
        URL urlLoginChk = new URL(urlstr);
        HttpURLConnection http = (HttpURLConnection) urlLoginChk.openConnection();

        //向网页中写入“POST”数据
        String params = "username="+sUserName+"&userpwd="+ToolsClass.stringToMD5(sUserPwd);
        http.setDoOutput(true);
        http.setRequestMethod("POST");
        OutputStream out = http.getOutputStream();
        out.write(params.getBytes());

        //读取网页返回的文件头内容；
        String responseCookie = http.getHeaderField("Set-Cookie");
        if (responseCookie != null) {
            ToolsClass.USERINFO_SESSIONID  = responseCookie.substring(0, responseCookie.indexOf(";"));
        }

        //读取网页返回的数据
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
        String line = "";
        StringBuilder sb = new StringBuilder();
        while(null != (line = bufferedReader.readLine())){
            sb.append(line);
        }
        String echoString = sb.toString();

        return 1;
    }

    private int sign() throws IOException{
        String sUserPwd = txtPWD.getText().toString();
        String sUserPwdComfirm = txtPWDComfirm.getText().toString();
        if(sUserPwd.length()<6){
            Toast.makeText(LoginActivity.this, "密码不得小与6位数!", Toast.LENGTH_SHORT).show();
            return 0;
        }

        if(!sUserPwd.equals(sUserPwdComfirm)){
            Toast.makeText(LoginActivity.this, "两次输入的密码不相同!", Toast.LENGTH_SHORT).show();
            return 0;
        }

        //建立网络连接
        String strUrlSign = ToolsClass.WEB_URL_PATH + "app_sign_up.php";
        URL url = new URL(strUrlSign);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        //向网页中写入POST数据
        String params = "uniqueid="+ToolsClass.USERINFO_UNIQUEID+"&userpwd="+ToolsClass.stringToMD5(sUserPwd);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        OutputStream outputStream = httpURLConnection.getOutputStream();
        outputStream.write(params.getBytes());

        return 1;
    }

    private int chkUser(){
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(this);
        String sql = "select UserID, PWD, LoginDateOnline, NickName from DM_UserInfo";
        Cursor cursor = mapNoteDBHelper.getReadableDatabase().rawQuery(sql, null);
        Boolean isExsit = cursor.moveToNext();
        if(!isExsit){
//            tvShowTest.setText("您是新安装用户，如果您有绑定Email的账户，请直接输入Email和密码进行登录！如果没有，请点击注册新用户！");
            //数据库里没有用户数据信息，返回1；
            return 1;
        }

        userId = cursor.getString(0);
        pwd = cursor.getString(1);
        loginDateOnline = cursor.getString(2);
        nickName = cursor.getString(3);
        if(userId==null||userId.equals("")){
            //数据库里的用户信息，没有UserId的记录，返回2；
            return 2;
        }

        long timeLoginDiff = ToolsClass.getTimeDiffer(loginDateOnline, ToolsClass.getNowDate());
        if(timeLoginDiff>ToolsClass.SEVENDAY_MILLISECOND){
            //当前日期跟上一次本地登录日期，相差大于7天时，要重新登录，返回3；
            return 3;
        }

        //通过软件运行是的用户自动检测；
        return 0;
    }

    private boolean getHasUserId(){
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(this);
        String sql = "select UserID from DM_UserInfo";
        Cursor cursor = mapNoteDBHelper.getReadableDatabase().rawQuery(sql, null);
        Boolean isExsit = cursor.moveToNext();
        if(!isExsit){
            return false;
        }

        String userId = cursor.getString(0);
        if(userId==null||userId.equals("")){
            return false;
        }

        return true;
    }

}
