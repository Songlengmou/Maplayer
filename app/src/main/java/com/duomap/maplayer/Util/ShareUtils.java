package com.duomap.maplayer.Util;

import android.content.Context;
import android.graphics.Bitmap;

import com.duomap.maplayer.Util.WXUtil;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by Administrator on 2018-01-11.
 */

public class ShareUtils {
    public static final int WXSENCE_RAFIKI = 0;
    public static final int WXSENCE_QUAN = 1;
    private Context context;
    private String urlPath;
    private String title;
    private String description;
    private Bitmap icon;

    public ShareUtils(){}

    public ShareUtils(Context context) {
        this.context = context;
    }

    public ShareUtils(Context context, String urlPath, String title, String description, Bitmap icon) {
        this.context = context;
        this.urlPath = urlPath;
        this.title = title;
        this.description = description;
        this.icon = icon;
    }

    //微信部分；
    private String WX_APP_ID="wx257e883db9232b24";
    private IWXAPI iwxapi;
    public void regToWx(){
        this.iwxapi = WXAPIFactory.createWXAPI(this.context, this.WX_APP_ID, false);
        this.iwxapi.registerApp(this.WX_APP_ID);
    }


    public void shareUrlToWx(int scene){
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = this.urlPath;

        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = this.title;
        msg.description = this.description;
        Bitmap thumb = this.icon;
        msg.thumbData = WXUtil.bmpToByteArray(thumb, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = WXUtil.buildTransaction("webpage");
        req.message = msg;
//                req.scene = isTimelineCb.isChecked()?SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        switch (scene){
            case WXSENCE_RAFIKI:
                req.scene = SendMessageToWX.Req.WXSceneSession;
                break;
            case WXSENCE_QUAN:
                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                break;
        }
        iwxapi.sendReq(req);
    }





    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }
}
