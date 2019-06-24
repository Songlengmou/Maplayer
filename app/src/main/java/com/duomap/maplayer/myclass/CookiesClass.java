package com.duomap.maplayer.myclass;

import java.util.Random;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Administrator on 2017-12-12.
 */

public class CookiesClass {
//    //当用户第一次运行时，从服务器获取到的唯一32位APP的ID，并记录到数据库中；
//    public static String appID="";
//
//    //当用户的APP登录时，记录登录时的Session值；
//    public static String sessionID="";


    //通过AsyncHttpClient返回的Header[]，获取其中的SessionId；
    public static String  getSessionIDFromHeaders(Header[] headers){
        for(int i=0; i<headers.length; i++){
            if(headers[i].getName().equals("Set-Cookie")){
                String setCookie = headers[i].getValue();
                return setCookie.substring(0, setCookie.indexOf(";"));
            }
        }
        return "";
    }


    //申请服务器创建UserID随机生成的一个校验数字；
    public void createRandomEppokHandler(){
        Random rand = new Random();
        ToolsClass.APP_RANDOM_EPPOK_HANDLER = String.valueOf(rand.nextInt(10000));
    }

    public String createUserIdSecretHandler(){
        String appEppokHandler = ToolsClass.APP_EPPOK_HANDLER;
        String uniqueId = ToolsClass.USERINFO_UNIQUEID;
        String writeDate = ToolsClass.USERINFO_WRITEDATE;
        String ranEppokHandler = ToolsClass.APP_RANDOM_EPPOK_HANDLER;
        String appVersion = ToolsClass.APP_VERSION;

        return ToolsClass.stringToMD5(appEppokHandler + uniqueId + writeDate + ranEppokHandler + appVersion);
    }
}

