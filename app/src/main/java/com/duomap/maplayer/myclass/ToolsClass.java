package com.duomap.maplayer.myclass;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import com.duomap.maplayer.MainActivity;
import com.duomap.maplayer.SettingActivity;
import com.loopj.android.http.AsyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by Administrator on 2017-12-12.
 */

public class ToolsClass {
    public static String APP_PACKAGE_NAME = "com.duomap.maplayer";
    public static String APP_VERSION = "1.0.0.180208";
    public static String APP_EPPOK_HANDLER = "E83pp01Ok15";
    public static String APP_RANDOM_EPPOK_HANDLER = "";
    public static boolean hasNewVersion = false;

    public static String FILE_HEADICON;
    public static String PATH_DUOMAP = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DuoMap/";
    public static String PATH_TEMP = PATH_DUOMAP + "temp/";
    public static String PATH_HEADICON = PATH_DUOMAP + "DCIM/Headicon/";
    public static String gPicPath =  PATH_DUOMAP + "DCIM/Photo/";
    public static String gMapShotPath =  PATH_DUOMAP + "DCIM/Mapshot/";
    public static final String WEB_URL_PATH = "http://www.duomap.com/app/";
    public static final String WEB_URL_SHOW_PATH = "http://www.duomap.com/app/smarty/duomap/";
    public static final String WEB_URL_UPDATE = WEB_URL_PATH +"chk_version.php";
    public static final String WEB_URL_GETNEWAPP = WEB_URL_PATH +"get_new_app.php";
    public static final int NOTETYPE_TEXT = 1;
    public static final int NOTETYPE_PIC = 2;
    public static final int NOTETYPE_MAPSHOT = 4;

    public static final int MAPID_GOOGLEMAP = 1;
    public static final int MAPID_AMAP = 2;

    public static final long SEVENDAY_MILLISECOND = 7*24*60*60*1000;
    public static final int THUMB_SIZE = 180;
    public static final int PIC_ZIP_SIZE = 600;

    public static int USERINFO_ID;
    public static String USERINFO_USERID;
    public static String USERINFO_UNIQUEID;
    public static String USERINFO_SESSIONID;
    public static String USERINFO_NICKNAME;
    public static String USERINFO_WRITEDATE;

    //获取AndroidID；
    public static String getAndroidID(Context context){
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    //获取Serial Number
    public static String getSN(){
        return Build.SERIAL;
    }

    //获取设备唯一标识
    public static String getUniqueId(Context context){
        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String id = androidID + Build.SERIAL;

        return stringToMD5(id);
    }

    /**
     * 将字符串转成MD5值
     */
    public static String stringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    public static String getRandomFileName() {
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        String str = simpleDateFormat.format(date);
        Random random = new Random();
        int rannum = (int) (random.nextDouble() * (99999 - 10000 + 1)) + 10000;// 获取5位随机数
        return str + rannum;// 当前时间
    }

    public static class PicInfo{
        public String picFile;
        public int picW, picH;
        public String thumbFile;
    }

    public static String createPicZip(String picFile){
        Bitmap bitmapPic = getLoacalBitmap(picFile, false);
        String zipName = "temp_" + getNameFromFile(picFile);

        PicInfo picInfoZip = getPicInfoAfterZoom(bitmapPic, PIC_ZIP_SIZE, PIC_ZIP_SIZE);
        Bitmap bitmapZip = getImageThumbnail(picFile, picInfoZip.picW, picInfoZip.picH);
        return saveBitmapFile(bitmapZip, PATH_TEMP, zipName);
    }


    public static PicInfo savePicAndThumb(Bitmap bitmap, String picPath, String picName){
        PicInfo picInfo = new PicInfo();
        picInfo.picFile = saveBitmapFile(bitmap, picPath, picName);

        PicInfo picThumbInfo = getPicInfoAfterZoom(bitmap, 120, 120);
        Bitmap bitmapThumb = getImageThumbnail(picInfo.picFile, picThumbInfo.picW, picThumbInfo.picH);
        picInfo.thumbFile = saveBitmapFile(bitmapThumb, picPath+"Thumbnail/", "thumb_"+picName);
        return picInfo;
    }


    public static String saveBitmapFile(Bitmap bitmap, String picPath, String picName){
        File temp = new File(picPath);//要保存文件先创建文件夹
        if (!temp.exists()) {
            temp.mkdirs();
        }
        ////重复保存时，覆盖原同名图片
        File file=new File(picPath + picName);//将要保存图片的路径和图片名称
        try {
            FileOutputStream out = new FileOutputStream(file);
//            BufferedOutputStream bos= new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return picPath + picName;
    }

    // 读取本地图片
    public static Bitmap getLoacalBitmap(String url, boolean bRotate) {
        try {
            FileInputStream fis = new FileInputStream(url);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bMap = BitmapFactory.decodeStream(fis, null, options);

            // Bitmap bMap = BitmapFactory.decodeStream(fis);

            if (bMap.getWidth() > bMap.getHeight() && bRotate) {
                // Create object of new Matrix.
                Matrix matrix = new Matrix();

                // set image rotation value to 90 degrees in matrix.
                matrix.postRotate(90);
                // matrix.postScale(0.5f, 0.5f);

                // Create bitmap with new values.
                Bitmap bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(), bMap.getHeight(), matrix, true);
                return bMapRotate;
            }

            // return BitmapFactory.decodeStream(fis);
            return bMap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }
    }

    /**
     * 根据指定的图像路径和大小来获取缩略图
     * 此方法有两点好处：
     *     1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
     *        第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
     *     2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
     *        用这个工具生成的图像不会被拉伸。
     * @param imagePath 图像的路径
     * @param width 指定输出图像的宽度
     * @param height 指定输出图像的高度
     * @return 生成的缩略图
     */
    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public static PicInfo getPicInfoAfterZoom(Bitmap bitmap, int toW, int toY){
        PicInfo picInfo = new PicInfo();
        int picW = bitmap.getWidth();
        int picH = bitmap.getHeight();
        if (picW > picH) {
            picInfo.picW = toW;
            picInfo.picH = picH * picInfo.picW / picW;
        }else{
            picInfo.picH = toY;
            picInfo.picW = picW * picInfo.picH / picH;
        }
        return picInfo;
    }


    //通过全路径文件名，获取文件所在文件夹的位置；
    public static String getPathFromFile(String sFile){
        return sFile.substring(0, sFile.lastIndexOf("/")+1);
    }

    //通过全路径文件命名，获取文件名称；
    public static String getNameFromFile(String sFile){
        return sFile.substring(sFile.lastIndexOf("/")+1, sFile.length());
    }


    /**
     * 获取当前时间
     */
    public static String getNowDate(){
        String writeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        return writeDate;
    }

    /**
     * 获取时间戳的毫秒数；
     */
    public static long getTimeMillis(String strTime){
        long returnMillis = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            returnMillis = simpleDateFormat.parse(strTime).getTime();
        } catch (Exception ParseException){

        }
        return returnMillis;
    }

    /**
     * 计算两个时间戳之间相差的毫秒数
     */
    public static long getTimeDiffer(String strTime1, String strTime2){
        long intStart = getTimeMillis(strTime1); //获取开始时间毫秒数
        long intEnd = getTimeMillis(strTime2);  //获取结束时间毫秒数
        return  intEnd-intStart;
    }

    public static String toUtf8(String str) {
        String result = null;
        try {
            result = new String(str.getBytes("UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }


    /**
     *获取Json中的String
     */
    public static String getJsonString(String json, String colName){
        JSONTokener jsonParser = new JSONTokener(json);
        JSONObject jsonObject = null;
        String result = "";
        try {
            jsonObject = (JSONObject) jsonParser.nextValue();
            result = jsonObject.getString(colName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取Json中的int
     */
    public static int getJsonInt(String json, String colName){
        JSONTokener jsonParser = new JSONTokener(json);
        JSONObject jsonObject = null;
        int result=0;
        try {
            jsonObject = (JSONObject) jsonParser.nextValue();
            result = jsonObject.getInt(colName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取HttpURLConnection http中的Session
     */
    public static String getSessionFromCookie(String strCookie){
        String strSession = "";
        if (strCookie != null) {
            strSession  = strCookie.substring(0, strCookie.indexOf(";"));
        }
        return strSession;
    }

    /**
     * View渐隐动画效果
     */
    public static void setHideAnimation(final View view, int duration) {
        Animation mHideAnimation=null;
        if (null == view || duration < 0){
            return;
        }

        if (null != mHideAnimation) {
            mHideAnimation.cancel();
        }
        // 监听动画结束的操作
        mHideAnimation = new AlphaAnimation(1.0f, 0.0f);
        mHideAnimation.setDuration(duration);
        mHideAnimation.setFillAfter(true);
        mHideAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {}

            @Override
            public void onAnimationRepeat(Animation arg0) {}

            @Override
            public void onAnimationEnd(Animation arg0) {
                view.setVisibility(View.GONE);
            }
        });
        view.startAnimation(mHideAnimation);
    }

    /**
     * View渐现动画效果
     */
    public static void setShowAnimation( final View view, int duration) {
        Animation mShowAnimation=null;
        if (null == view || duration < 0) {
            return;
        }
        if (null != mShowAnimation) {
            mShowAnimation.cancel();
        }
        mShowAnimation = new AlphaAnimation(0.0f, 1.0f);
        mShowAnimation.setDuration(duration);
        mShowAnimation.setFillAfter(true);
        mShowAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0)
            {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {}

            @Override
            public void onAnimationEnd(Animation arg0) {}
        });
        view.startAnimation(mShowAnimation);
    }

    public static void gotoActivity(Context context, Class<?> toClass){
        Intent intent = new Intent();
        intent.setClass(context, toClass);
        context.startActivity(intent);
    }
}
