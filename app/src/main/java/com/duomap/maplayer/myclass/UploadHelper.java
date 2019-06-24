package com.duomap.maplayer.myclass;

import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.duomap.maplayer.WriteNoteActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Administrator on 2018-01-12.
 */

public class UploadHelper {
    //上传信息记录
    private int progressCount;
    private int progressPos;
    private String progressHead;
    private String progressBody;

    public UploadHelper() {
    }

    public UploadHelper(int progressCount, int progressPos, String progressHead, String progressBody) {
        this.progressCount = progressCount;
        this.progressPos = progressPos;
        this.progressHead = progressHead;
        this.progressBody = progressBody;
    }

    public String toStringProgress(){
        String symbolColon = "：";
        if(TextUtils.isEmpty(progressBody)){
            symbolColon = "";
        }
        return "("+ progressPos +"/"+ progressCount +")"+ progressHead + symbolColon + progressBody;
    }



    public void showInTextView(TextView tv){
        tv.setText(toStringProgress());
    }

    public int getProgressCount() {
        return progressCount;
    }

    public void setProgressCount(int progressCount) {
        this.progressCount = progressCount;
    }

    public int getProgressPos() {
        return progressPos;
    }

    public void setProgressPos(int progressPos) {
        this.progressPos = progressPos;
    }

    public String getProgressHead() {
        return progressHead;
    }

    public void setProgressHead(String progressHead) {
        this.progressHead = progressHead;
    }

    public String getProgressBody() {
        return progressBody;
    }

    public void setProgressBody(String progressBody) {
        this.progressBody = progressBody;
    }

}
