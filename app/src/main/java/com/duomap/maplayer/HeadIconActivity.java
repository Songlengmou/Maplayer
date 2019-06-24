package com.duomap.maplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.duomap.maplayer.Util.FileUtils;
import com.duomap.maplayer.Util.MediaStoreUtils;
import com.duomap.maplayer.Util.ToastUtils;
import com.duomap.maplayer.myclass.ExitApplication;
import com.duomap.maplayer.myclass.MapNoteDBHelper;
import com.duomap.maplayer.myclass.ToolsClass;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import cz.msebera.android.httpclient.Header;

import static com.duomap.maplayer.Util.MediaStoreUtils.PHOTO_REQUEST_CUT;
import static com.duomap.maplayer.Util.MediaStoreUtils.REQUEST_PICTURE;

public class HeadIconActivity extends Activity
        implements View.OnClickListener {
    Button btnPhoto, btnAlbum, btnConfirm;
    ImageView ivHeadIcon;
    private String uploadHeadiconUrl = ToolsClass.WEB_URL_PATH + "upload_headicon.php";
    private Uri uriTempFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_head_icon);
        ExitApplication.getInstance().addActivity(this);

        iniView();
        iniListener();
    }

    private void iniView(){
        ivHeadIcon = (ImageView) findViewById(R.id.ivHeadIcon_HeadIcon);

        btnPhoto = (Button) findViewById(R.id.btnPhoto_HeadIcon);
        btnAlbum = (Button) findViewById(R.id.btnAlbum_HeadIcon);
        btnConfirm = (Button) findViewById(R.id.btnConfirm_HeadIcon);

        if(!TextUtils.isEmpty(ToolsClass.FILE_HEADICON)){
            ivHeadIcon.setImageURI(Uri.parse(ToolsClass.FILE_HEADICON));
        }
    }

    private void iniListener(){
        btnPhoto.setOnClickListener(this);
        btnAlbum.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnPhoto_HeadIcon:
                break;

            case R.id.btnAlbum_HeadIcon:
                startActivityForResult(MediaStoreUtils.getPickImageIntent(), REQUEST_PICTURE);
                break;

            case R.id.btnConfirm_HeadIcon:
                try {
                    postFile(uploadHeadiconUrl, uriTempFile.getPath(), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if (requestCode == REQUEST_PICTURE){
                startPhotoZoom(data.getData(), 360);
            } else if (requestCode == PHOTO_REQUEST_CUT){
                btnConfirm.setVisibility(View.VISIBLE);
                ivHeadIcon.setImageURI(uriTempFile);
            }
        }
    }

    private void startPhotoZoom(Uri uri, int size) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);

        /**
         * 此方法返回的图片只能是小图片（sumsang测试为高宽160px的图片）
         * 故只保存图片Uri，调用时将Uri转换为Bitmap，此方法还可解决miui系统不能return data的问题
         */
        //intent.putExtra("return-data", true);

        //裁剪后的图片Uri路径，uritempFile为Uri类变量
        uriTempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/DuoMap/" + "headicon_temp.png");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriTempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }



    public void postFile(String url, String filePath, RequestParams requestParams) throws IOException {
        File file = new File(filePath);
        if(file.exists() && file.length()>0){
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
            asyncHttpClient.addHeader("Cookie", ToolsClass.USERINFO_SESSIONID);

            RequestParams params = new RequestParams();
            params.put("uploadfile", file);
            asyncHttpClient.post(url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    super.onProgress(bytesWritten, totalSize);
                }

                @Override
                public void onRetry(int retryNo) {
                    super.onRetry(retryNo);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
//                    ToastUtils.showShortToast(HeadIconActivity.this, response);
                    int ispass = ToolsClass.getJsonInt(response, "ispass");
                    String message = ToolsClass.getJsonString(response, "message");
                    int isdone = ToolsClass.getJsonInt(response, "isdone");
                    if (ispass==1 && isdone==1){
                        String fileHeadIcon = ToolsClass.PATH_HEADICON + message;
                        FileUtils.moveFile(uriTempFile.getPath(), fileHeadIcon);
                        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(HeadIconActivity.this);
                        mapNoteDBHelper.updateHeadicon(fileHeadIcon, ToolsClass.USERINFO_USERID);
                        Toast.makeText(HeadIconActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
                        MainActivity.ivHeadIcon.setImageURI(Uri.parse(fileHeadIcon));
                        ToolsClass.FILE_HEADICON = fileHeadIcon;
                        finish();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                }
            });
        }else{
            Toast.makeText(HeadIconActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
        }
    }
}
