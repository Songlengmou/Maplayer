package com.duomap.maplayer.myclass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amap.api.maps.model.LatLng;
import com.duomap.maplayer.MainActivity;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by Administrator on 2017-12-14.
 */

public class MapNoteDBHelper extends SQLiteOpenHelper {
    //数据库名称
    private static final String DATABASE_NAME = "MapNote.db";

    //版本号,则是升级之后的,升级方法请看onUpgrade方法里面的判断
    private static final int DATABASE_VERSION = 2;

    public MapNoteDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        if(oldVersion<2){
            String sql_upgrade = "alter table DM_LineInfo add RegionEnd Text";//增加一个列sex
            db.execSQL(sql_upgrade);
        }

    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String sql = null;

        //创建UserInfo表
        sql = "CREATE TABLE DM_UserInfo(ID INTEGER PRIMARY KEY, UserID TEXT, UserName TEXT, PWD TEXT, SessionID TEXT, NickName TEXT, UniqueID TEXT, WriteDate TIMESTAMP, MobileNo TEXT, Email TEXT, ";
        sql += " LoginDateLocal TIMESTAMP, LoginDateOnline TIMESTAMP, HeadIcon TEXT, Gender INTEGER, BirthDay TIMESTAMP)";
        db.execSQL(sql);

        //创建LineInfo表
        sql ="CREATE TABLE DM_LineInfo( " +
                " ID INTEGER PRIMARY KEY, " +
                " LineName TEXT, " +
                " Thumbnail Text, " +
                " Region Text, " +
                " RegionEnd Text, " +
                " LatStart Text, " +
                " LngStart Text, " +
                " LatEnd Text, " +
                " LngEnd Text, " +
                " WriteDate TIMESTAMP, " +
                " EditDate TIMESTAMP, " +
                " UserName TEXT)";
        db.execSQL(sql);

        //创建LocationRecord表
        sql = "CREATE TABLE DM_LocationRecord( " +
                " ID INTEGER PRIMARY KEY, " +
                " LineID INTEGER, " +
                " MapID INTEGER, " +
                " Latitude TEXT, " +
                " Longitude TEXT, " +
                " WriteDate TIMESTAMP)";
        db.execSQL(sql);

        //创建Rectification表，即路线优化后的定位点记录
//        sql= "CREATE TABLE DM_RectificationRecord(ID INTEGER PRIMARY KEY, LineID INTEGER, Latitude REAL, Longitude REAL, WriteDate TIMESTAMP)";
        sql= "CREATE TABLE DM_RectificationRecord(ID INTEGER PRIMARY KEY, LineID INTEGER, MapID INTEGER, Latitude TEXT, Longitude TEXT, WriteDate TIMESTAMP)";
        db.execSQL(sql);

        //创建MapNote表
        //NoteType：
        //  1、文本：正常原内容显示；
        //  2、图片：文件所在地址，文件夹“DuoMap\DCIM\Photo\”；
        //  3、音频：文件所在地址，文件夹“DuoMap\DCIM\audio\”；
        //  4、视频：文件所在地址，文件夹“DuoMap\DCIM\video\”；
        //  5、地图截图：文件所在地址，文件夹“DuoMap\DCIM\MapShot\”
        sql = "CREATE TABLE DM_MapNote( " +
                " ID INTEGER PRIMARY KEY, " +
                " LineID INTEGER, " +
                " NoteType INTEGER, " +
                " NoteContent TEXT, " +
                " Thumbnail Text, " +
                " WriteDate TIMESTAMP, " +
                " EditDate TIMESTAMP)";
        db.execSQL(sql);

        //创建MapNote_PicDescription表
        //MapNote中记录类型为图片的，对图片进行描述，含文本描述1，和音频描述3；
        sql = "CREATE TABLE DM_MapNote_PicDescription(ID INTEGER PRIMARY KEY, MapNoteID INTEGER, NoteType INTEGER, NoteContent TEXT, WriteDate TIMESTAMP, EditDate TIMESTAMP)";
        db.execSQL(sql);

        /**
         * 创建MapNote坐标点记录表
         */
//        sql = "CREATE TABLE DM_MapNote_LatLng(ID INTEGER PRIMARY KEY, LineID INTEGER, MapNoteID INTEGER, Latitude REAL, Longitude REAL, WriteDate TIMESTAMP)";
        sql = "CREATE TABLE DM_MapNote_LatLng(ID INTEGER PRIMARY KEY, LineID INTEGER, MapNoteID INTEGER, MapID INTEGER, Latitude TEXT, Longitude TEXT, WriteDate TIMESTAMP)";
        db.execSQL(sql);

        /**
         * 创建文章BlogInfo标题表
         */
        sql = "CREATE TABLE DM_BlogInfo( " +
                " ID INTEGER PRIMARY KEY, " +
                " LineID INTEGER, " +
                " BlogTitle TEXT, " +
                " Thumbnail Text, " +
                " Region Text, " +
                " StartArea Text, " +
                " EndArea Text, " +
                " WriteDate TIMESTAMP, " +
                " EditDate TIMESTAMP, " +
                " ServerBlogInfoID INTEGER, " +
                " ServerBlogSessionID TEXT)";
        db.execSQL(sql);

        /**
         * 创建文章内容表BlogContent
         */
        sql = "CREATE TABLE DM_BlogContent(ID INTEGER PRIMARY KEY, BlogInfoID INTEGER, LineID INTEGER, MapNoteID INTEGER, TypeID INTEGER, Content TEXT, ";
        sql += " Thumbnail Text, OrderBy INTEGER, WriteDate TIMESTAMP, EditDate TIMESTAMP, IsUpMerge INTEGER, FileServerID INTEGER, FileServerName TEXT)";
        db.execSQL(sql);
    }


    /**
     * UserInfo 相关
     * @param
     * @return
     */
    public void updateUserId(String userId, String uniqueId){
        ContentValues cv =  new ContentValues();
        String[] args = {uniqueId};
        if(userId != null){cv.put("UserID", userId);}
        getWritableDatabase().update("DM_UserInfo", cv, "UniqueID=?", args);
    }
    public void updateHeadicon(String headicon, String userId){
        ContentValues cv =  new ContentValues();
        String[] args = {userId};
        if(userId != null){cv.put("HeadIcon", headicon);}
        getWritableDatabase().update("DM_UserInfo", cv, "UserID=?", args);
    }


    public int getLastID(String tableName){
        String sql = "select ID from "+ tableName +" order by ID DESC";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getInt(0);
    }

    public int getFirstID(String tableName){
        int id = 0;
        String sql = "select ID from "+ tableName +" order by ID";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        id = cursor.getInt(0);
        return id;
    }

    public String getStringFromID(String tableName, String colName, int id){
        String sql = "select "+colName+" from "+ tableName +" where ID="+id;
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getString(0);
    }

    public String getStringFromString(String tableName, String colName, String strWhere){
        String sql = "select "+colName+" from "+ tableName +" "+strWhere;
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getString(0);
    }


    /**
     * 表LineInfo相关操作
     * @param lineName
     * @param writeDate
     * @param editDate
     * @param userName
     */
    public void insertNewLine(String lineName, String writeDate, String editDate, String userName){
        ContentValues cv = new ContentValues();
        cv.put("LineName", lineName);
        cv.put("WriteDate", writeDate);
        cv.put("EditDate", editDate);
        cv.put("UserName", userName);
        getWritableDatabase().insert("DM_LineInfo", null, cv);
    }

    public void updateNewLine(String lineName, String writeDate, String editDate, String userName, String id){
        ContentValues cv =  new ContentValues();
        String[] args = {id};
        if(lineName != null){cv.put("LineName", lineName);}
        if(writeDate != null){cv.put("WriteDate", writeDate);}
        if(editDate != null){cv.put("EditDate", editDate);}
        if(userName != null){cv.put("UserName", userName);}
        getWritableDatabase().update("DM_LineInfo", cv, "ID=?", args);
    }

    public void updateLineAddress(String id, String address){
        ContentValues cv =  new ContentValues();
        String[] args = {id};
        if(address != null){cv.put("Region", address);}
        getWritableDatabase().update("DM_LineInfo", cv, "ID=?", args);
    }

    //获取最新建的路线ID号
    public int getLastLineID(){
        String sql = "select ID from DM_LineInfo order by ID DESC";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getInt(0);
    }


    public String getLineName(int lineID){
        String sql = "select LineName from DM_LineInfo where ID=" + lineID;
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getString(0);
    }



    public List<Map<String, Object>> getMyLineList(){
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = null;

        String sql = "select ID, LineName, WriteDate, Region, Thumbnail from DM_LineInfo order by ID DESC";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()){
            map = new HashMap<String, Object>();
            map.put("lineid", cursor.getString(0));
            map.put("linename", cursor.getString(1));
            map.put("linetime", cursor.getString(2));
            map.put("region", cursor.getString(3));
            map.put("thumbnail", cursor.getString(4));
            list.add(map);
        }
        return list;
    }

    public List<LatLng> getMyLinePos(int lineID){
        List<LatLng> mLatLng = new ArrayList<LatLng>();
        String sql = "select Latitude, Longitude from DM_LocationRecord where LineID="+ lineID +" order by ID";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()){
            mLatLng.add(new LatLng(cursor.getDouble(0), cursor.getDouble(1)));
        }
        return mLatLng;
    }

    public List<Map<String, Object>> getMyLineMarker(int lineID){
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = null;

        String sql = "select MapNoteID, Latitude, Longitude from DM_MapNote_LatLng where LineID="+lineID+" order by ID";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()){
            map = new HashMap<String, Object>();
            map.put("MapNoteID", cursor.getString(0));
            map.put("Latitude", cursor.getString(1));
            map.put("Longitude", cursor.getString(2));
            list.add(map);
        }
        return list;
    }


    //    sql = "CREATE TABLE DM_LocationRecord(ID INTEGER PRIMARY KEY, LineID INTEGER, Latitude REAL, Longitude REAL, WriteDate TIMESTAMP)";
    public void insertLocationRecord(Integer lineID, int mapID, Double lat, Double lng, String writeDate){
        ContentValues cv = new ContentValues();
        cv.put("LineID", lineID);
        cv.put("MapID", mapID);
        cv.put("Latitude", lat);
        cv.put("Longitude", lng);
        cv.put("WriteDate", writeDate);
        getWritableDatabase().insert("DM_LocationRecord", null, cv);
    }

    //    sql= "CREATE TABLE DM_Rectification(ID INTEGER PRIMARY KEY, LineID INTEGER, Latitude REAL, Longitude REAL, WriteDate TIMESTAMP)";
    public void insertRectificationRecord(Integer lineID, int mapID, Double lat, Double lng, String writeDate){
        ContentValues cv = new ContentValues();
        cv.put("LineID", lineID);
        cv.put("MapID", mapID);
        cv.put("Latitude", lat);
        cv.put("Longitude", lng);
        cv.put("WriteDate", writeDate);
        getWritableDatabase().insert("DM_RectificationRecord", null, cv);
    }


    /**
     * 表MapNote相关操作
     */
    public void insertMapNote(Integer lineID, Integer noteType, String noteContent, String thumbnail, String writeDate){
        ContentValues cv = new ContentValues();
        cv.put("LineID", lineID);
        cv.put("NoteType", noteType);
        cv.put("NoteContent", noteContent);
        cv.put("Thumbnail", thumbnail);
        cv.put("WriteDate", writeDate);
        getWritableDatabase().insert("DM_MapNote", null, cv);
    }

    public int getLastMapNoteID(){
        String sql = "select ID from DM_MapNote order by ID DESC";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getInt(0);
    }

    public String getNoteContent(int MapNoteID){
        String sql = "select NoteContent from DM_MapNote where ID=" + MapNoteID;
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getString(0);
    }

    public String getLineIconPath(int lineId){
        String sql = "select NoteContent from DM_MapNote where LineID="+lineId +" and NoteType in (2,4) order by WriteDate";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getString(0);
    }


//    sql = "CREATE TABLE DM_MapNote_LatLng(ID INTEGER PRIMARY KEY, LineID INTEGER, MapNoteID INTEGER, Latitude REAL, Longitude REAL, WriteDate TIMESTAMP)";
    public void insertMapNoteLatLng(Integer lineID, Integer mapNoteID, int mapID, Double lat, Double lng, String writeDate){
        ContentValues cv = new ContentValues();
        cv.put("LineID", lineID);
        cv.put("MapNoteID", mapNoteID);
        cv.put("MapID", mapID);
        cv.put("Latitude", lat);
        cv.put("Longitude", lng);
        cv.put("WriteDate", writeDate);
        getWritableDatabase().insert("DM_MapNote_LatLng", null, cv);
    }

    /**
     * 表MapNotePicDescription相关操作
     * @param mapNoteID
     * @param noteType
     * @param noteContent
     * @param writeDate
     * @param editDate
     */
    public void insertMapNotePicDescription(Integer mapNoteID, Integer noteType, String noteContent, String writeDate, String editDate){
        ContentValues cv = new ContentValues();
        cv.put("MapNoteID", mapNoteID);
        cv.put("NoteType", noteType);
        cv.put("NoteContent", noteContent);
        cv.put("WriteDate", writeDate);
        getWritableDatabase().insert("DM_MapNote", null, cv);
    }

    /**
     *表BlogInfo相关操作；
     * @param lineID
     * @param blogTitle
     * @param writeDate
     * @param editDate
     */
    public void insertBlogInfo(Integer lineID, String blogTitle, String writeDate, String editDate){
        ContentValues cv = new ContentValues();
        cv.put("LineID", lineID);
        cv.put("BlogTitle", blogTitle);
        cv.put("WriteDate", writeDate);
        cv.put("EditDate", editDate);
        getWritableDatabase().insert("DM_BlogInfo", null, cv);
    }

    public int getLineIDFromBlogID(int blogInfoID){
        String sql = "select LineID from DM_BlogInfo where ID=" + blogInfoID;
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getInt(0);
    }

    public String getBlogTitle(int blogInfoID){
        String sql = "select BlogTitle from DM_BlogInfo where ID=" + blogInfoID;
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getString(0);
    }

    public int getServerBlogInfoId(int blogInfoID){
        String sql = "select ServerBlogInfoID from DM_BlogInfo where ID=" + blogInfoID;
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getInt(0);
    }

    public List<Map<String, Object>> getBlogInfoList(){
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = null;

        String sql = "select ID, BlogTitle, WriteDate from DM_BlogInfo order by ID DESC";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()){
            map = new HashMap<String, Object>();
            map.put("BlogInfoID", cursor.getString(0));
            map.put("BlogTitle", cursor.getString(1));
            map.put("WriteDate", cursor.getString(2));
            list.add(map);
        }
        return list;
    }

    public void updateBlogTitle(int blogInfoId, String editBlogTilte){
        ContentValues cv =  new ContentValues();
        String[] args = {String.valueOf(blogInfoId)};
        cv.put("BlogTitle", editBlogTilte);
        getWritableDatabase().update("DM_BlogInfo", cv, "ID=?", args);
    }

    public void updateServerInfoFromBlogInfoID(int blogInfoID, int serverBlogInfoId, String serverBlogSessionId){
        ContentValues cv =  new ContentValues();
        String[] args = {String.valueOf(blogInfoID)};
        cv.put("ServerBlogInfoId", serverBlogInfoId);
        cv.put("ServerBlogSessionId", serverBlogSessionId);
        getWritableDatabase().update("DM_BlogInfo", cv, "ID=?", args);
    }


    /**
     * 表BlogContent的相关操作
     * @param blogInfoID
     * @param lineID
     * @param mapNoteID
     * @param typeID
     * @param content
     * @param orderBy
     * @param writeDate
     * @param editDate
     */
    public void insertBlogContent(Integer blogInfoID, Integer lineID, Integer mapNoteID, Integer typeID, String content, String thumb, int orderBy, String writeDate, String editDate, int isUpMerge){
        ContentValues cv = new ContentValues();
        cv.put("BlogInfoID", blogInfoID);
        cv.put("LineID", lineID);
        cv.put("MapNoteID", mapNoteID);
        cv.put("TypeID", typeID);
        cv.put("Content", content);
        cv.put("Thumbnail", thumb);
        cv.put("OrderBy", orderBy);
        cv.put("WriteDate", writeDate);
        cv.put("EditDate", editDate);
        cv.put("IsUpMerge", isUpMerge);
        getWritableDatabase().insert("DM_BlogContent", null, cv);
    }

    public void insertBlogContent(WriteNoteHelper writeNoteHelper){
        ContentValues cv = new ContentValues();
        cv.put("BlogInfoID", writeNoteHelper.getBlogInfoID());
        cv.put("LineID", writeNoteHelper.getLineID());
        cv.put("MapNoteID", writeNoteHelper.getMapNoteID());
        cv.put("TypeID", writeNoteHelper.getTypeContent());
        cv.put("Content", writeNoteHelper.getContentText());
        cv.put("OrderBy", writeNoteHelper.getOrderBy());
        cv.put("WriteDate", writeNoteHelper.getWriteDate());
        cv.put("EditDate", writeNoteHelper.getEditDate());
        cv.put("IsUpMerge", writeNoteHelper.getIsUpMerge());
        getWritableDatabase().insert("DM_BlogContent", null, cv);
    }

    public void deleteBlogContentFromID(int blogContentID){
        String[] args = {String.valueOf(blogContentID)};
        getWritableDatabase().delete("DM_BlogContent", "ID=?", args);
    }

    public void updateOrderByFromBlogContentID(int blogContentID, int setOrderBy){
        ContentValues cv =  new ContentValues();
        String[] args = {String.valueOf(blogContentID)};
        cv.put("OrderBy", setOrderBy);
        getWritableDatabase().update("DM_BlogContent", cv, "ID=?", args);
    }

    public void updateIsUpMergeFromBlogContentID(int blogContentID, int isUpMerge){
        ContentValues cv =  new ContentValues();
        String[] args = {String.valueOf(blogContentID)};
        cv.put("IsUpMerge", isUpMerge);
        getWritableDatabase().update("DM_BlogContent", cv, "ID=?", args);
    }

    public void updateServerInfoFromBlogContentID(int blogContentID, int fileServerID, String fileServerName){
        ContentValues cv =  new ContentValues();
        String[] args = {String.valueOf(blogContentID)};
        cv.put("FileServerID", fileServerID);
        cv.put("FileServerName", fileServerName);
        getWritableDatabase().update("DM_BlogContent", cv, "ID=?", args);
    }

    public String getBlogContentDescription(int blogInfoID){
        StringBuilder stringBuilder = new StringBuilder();
        String sql = "select Content from DM_BlogContent where TypeID=1 order by OrderBy";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()){
            stringBuilder.append(cursor.getString(0));
        }
        return stringBuilder.toString();
    }

    public int getOrderByInBlogContent(int blogInfoID){
        String sql = "select OrderBy from DM_BlogContent where BlogInfoID=" + blogInfoID +" order by OrderBy DESC";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getInt(0)+1;
    }

    public StringBuilder getBlogForPublish(int blogInfoId){
        StringBuilder stringBuilder = new StringBuilder();
        String sql = "select Content, TypeID, FileServerID, IsUpMerge from DM_BlogContent where BlogInfoID="+ blogInfoId +" order by OrderBy";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        boolean isStart = true;
        while (cursor.moveToNext()){
            int typeId = cursor.getInt(1);
            int isUpMerge = cursor.getInt(3);
            switch (typeId){
                case 1:
                    if(isStart) {
                        stringBuilder.append("<P>");
                        isStart = false;
                    }
                    if(isUpMerge==0){
                        stringBuilder.append("</P><P>");
                    }
                    stringBuilder.append(cursor.getString(0));
                    break;
                case 2:
                case 4:
                    stringBuilder.append("{<[IMG ID="+cursor.getString(2)+"]>}");
                    break;
            }

        }
        return stringBuilder;
    }

    public String getShareIconPath(int blogInfoId){
        String sql = "select Content from DM_BlogContent where BlogInfoID="+blogInfoId +" and TypeID in (2,4) order by OrderBy";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getString(0);
    }

}
