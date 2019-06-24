package com.duomap.maplayer.Util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.duomap.maplayer.myclass.ToolsClass;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;


/**
 * Created by Administrator on 2018-01-14.
 */

public class UpdateUtils{
    private Context context;

    public UpdateUtils(Context context) {
        this.context = context;
    }



    //从服务器上获取最新App版本信息，并跟本地信息进行比较；
    public void getVersionServer(){
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.addHeader("Cookie", ToolsClass.USERINFO_SESSIONID);
        String path = ToolsClass.WEB_URL_UPDATE +"?" + ToolsClass.getRandomFileName();
        asyncHttpClient.get(context, path, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                int ispass = ToolsClass.getJsonInt(response, "ispass");
                int ishave = ToolsClass.getJsonInt(response, "ishave");
                if(ispass==1 && ishave==1) {
                    String verServer = ToolsClass.getJsonString(response, "version");
                    ToolsClass.hasNewVersion = compareVersion(ToolsClass.APP_VERSION, verServer);
                    if(ToolsClass.hasNewVersion){
                        ToastUtils.showShortToast(context, "服务器有最新版本，再次点击可下载！");
                    }else{
                        ToastUtils.showShortToast(context, "当前版本已为最新版本！");
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                ToolsClass.hasNewVersion = false;
                ToastUtils.showShortToast(context, "未连接到服务器，请检查网络！");
            }
        });
    }

    public boolean compareVersion(String verLocal, String verServer){
        if(TextUtils.isEmpty(verServer)){
            return false;
        }
        long lVerLocal =  Long.valueOf(verLocal.replace(".", ""));
        long lVerServer = Long.valueOf(verServer.replace(".", ""));
        if(lVerServer>lVerLocal){
            return true;
        }else{
            return false;
        }
    }



//    public void getNewApp(){
//        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
//        asyncHttpClient.addHeader("Cookie", ToolsClass.USERINFO_SESSIONID);
//        String path = ToolsClass.WEB_URL_GETNEWAPP +"?" + ToolsClass.getRandomFileName();
//        asyncHttpClient.get(context, path, new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                String response = new String(responseBody);
//                ToastUtils.showShortToast(context, response);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//
//            }
//        });
//    }

    /**
     * 通过浏览器下载APK包
     */
    public void downloadForWebView() {
        String url = ToolsClass.WEB_URL_GETNEWAPP +"?" + ToolsClass.getRandomFileName();
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
