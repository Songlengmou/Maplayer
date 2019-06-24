package com.duomap.maplayer.myclass;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017-12-24.
 */

public class WriteNoteHelper {
    private int blogContentID = 0;
    private String contentText="", thumbnail="";
    private int typeContent = ToolsClass.NOTETYPE_TEXT;
    private int orderBy;
    private String writeDate, editDate;

    private int blogInfoID, lineID, mapNoteID;
    private int isUpMerge;


    public WriteNoteHelper() {
    }

    public WriteNoteHelper(int blogContentID, String contentText, String thumbnail, int typeContent, int orderBy, int isUpMerge) {
        this.blogContentID = blogContentID;
        this.contentText = contentText;
        this.thumbnail = thumbnail;
        this.typeContent = typeContent;
        this.orderBy = orderBy;
        this.isUpMerge = isUpMerge;
    }

    public int getBlogInfoID() {
        return blogInfoID;
    }

    public void setBlogInfoID(int blogInfoID) {
        this.blogInfoID = blogInfoID;
    }

    public int getLineID() {
        return lineID;
    }

    public void setLineID(int lineID) {
        this.lineID = lineID;
    }

    public int getMapNoteID() {
        return mapNoteID;
    }

    public void setMapNoteID(int mapNoteID) {
        this.mapNoteID = mapNoteID;
    }

    public int getBlogContentID() {
        return blogContentID;
    }

    public void setBlogContentID(int blogContentID) {
        this.blogContentID = blogContentID;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getTypeContent() {
        return typeContent;
    }

    public void setTypeContent(int typeContent) {
        this.typeContent = typeContent;
    }

    public int getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(int orderBy) {
        this.orderBy = orderBy;
    }

    public String getWriteDate() {
        return writeDate;
    }

    public void setWriteDate(String writeDate) {
        this.writeDate = writeDate;
    }

    public String getEditDate() {
        return editDate;
    }

    public void setEditDate(String editDate) {
        this.editDate = editDate;
    }

    public int getIsUpMerge() {
        return isUpMerge;
    }

    public void setIsUpMerge(int isUpMerge) {
        this.isUpMerge = isUpMerge;
    }

    //根据LineID，创建一个新的BlogInfo，并返回BlogInfoID；
    public int createBlogInfo(Context context, int lineID){
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(context);
        String lineName = mapNoteDBHelper.getLineName(lineID);

        String nowDate = ToolsClass.getNowDate();
        mapNoteDBHelper.insertBlogInfo(lineID, lineName, nowDate, nowDate);
        int blogInfoID = mapNoteDBHelper.getLastID("DM_BlogInfo");
        return blogInfoID;
    }


    //若是新生成的Blog，则从LineInfo中先复制出路线上的记录，生成初始化的Blog
    //BlogInfoID是通过createBlogInfo(Context context, int lineID)，新生成的；
    public List<WriteNoteHelper> createBlogContentList(Context context, int lineID, int justCreateBlogInfoID){
        List<WriteNoteHelper> list = new ArrayList<WriteNoteHelper>();
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(context);
        WriteNoteHelper writeNoteHelper = null;
        int orderBy = 0;
        int mapNoteID = 0;
        int typeID = 0;
        String content = "";
        String thumbnail = "";
        String nowDate = "";
        int blogContentID = 0;
        int isUpMerge = 0;

        String sql = "select ID, NoteType, NoteContent, Thumbnail from DM_MapNote where LineID="+lineID+" order by NoteType DESC, ID";
        Cursor cursor = mapNoteDBHelper.getReadableDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()){
            //将在Line上录制的内容，按照时间的先后顺序，复制到BlogContent表中；
            mapNoteID = cursor.getInt(0);
            typeID = cursor.getInt(1);
            content = cursor.getString(2);
            thumbnail = cursor.getString(3);
            nowDate = ToolsClass.getNowDate();
            isUpMerge =0;
            mapNoteDBHelper.insertBlogContent(justCreateBlogInfoID, lineID, mapNoteID,typeID, content, thumbnail, orderBy, nowDate, nowDate, isUpMerge);
            blogContentID = mapNoteDBHelper.getLastID("DM_BlogContent");
            writeNoteHelper = new WriteNoteHelper(blogContentID, content, thumbnail, typeID, orderBy, isUpMerge);
            list.add(writeNoteHelper);
            orderBy++;
        }
        return list;
    }


    //已知BlogInfoID，获取BlogContent中的内容清单；
    public List<WriteNoteHelper> getBlogContentListFormBlogInfoID(Context context, int blogInfoID){
        List<WriteNoteHelper> list = new ArrayList<WriteNoteHelper>();
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(context);
        String sql = "select ID, Content, TypeID, OrderBy, IsUpMerge, Thumbnail from DM_BlogContent where BlogInfoID="+blogInfoID+" order by OrderBy";
        Cursor cursor = mapNoteDBHelper.getReadableDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()){
            int blogContentID = cursor.getInt(0);
            String content = cursor.getString(1);
            int typeID = cursor.getInt(2);
            int orderBy = cursor.getInt(3);
            int isUpMerge = cursor.getInt(4);
            String thumbnail = cursor.getString(5);
            WriteNoteHelper writeNoteHelper = new WriteNoteHelper(blogContentID, content, thumbnail, typeID, orderBy, isUpMerge);
            list.add(writeNoteHelper);
        }
        return list;
    }



    //    sql = "CREATE TABLE DM_BlogContent(ID INTEGER PRIMARY KEY, BlogInfoID INTEGER, LineID INTEGER, MapNoteID INTEGER, ";
    //    sql += " TypeID INTEGER, Content TEXT, OrderBy INTEGER, WriteDate TIMESTAMP, EditDate TIMESTAMP)";
    public WriteNoteHelper getBlogContentFromID(Context context, int blogContentID){
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(context);
        String sql = "select ID, BlogInfoID, LineID, MapNoteID, TypeID, Content, OrderBy, WriteDate, EditDate, IsUpMerge from DM_BlogContent where ID="+blogContentID+" order by OrderBy";
        Cursor cursor = mapNoteDBHelper.getReadableDatabase().rawQuery(sql, null);
        cursor.moveToNext();

        WriteNoteHelper writeNoteHelper = new WriteNoteHelper();
        writeNoteHelper.setBlogContentID(cursor.getInt(0));
        writeNoteHelper.setBlogInfoID(cursor.getInt(1));
        writeNoteHelper.setLineID(cursor.getInt(2));
        writeNoteHelper.setMapNoteID(cursor.getInt(3));
        writeNoteHelper.setTypeContent(cursor.getInt(4));
        writeNoteHelper.setContentText(cursor.getString(5));
        writeNoteHelper.setOrderBy(cursor.getInt(6));
        writeNoteHelper.setWriteDate(cursor.getString(7));
        writeNoteHelper.setEditDate(cursor.getString(8));
        writeNoteHelper.setIsUpMerge(cursor.getInt(9));
        return writeNoteHelper;
    }

    /**
     * 粘贴复制的ListView Item里的内容，在目标位置里向上粘贴；
     * @param context
     * @param list List<WriteNoteHelper>
     * @param operPosition 被复制的ListView Item所在的位置；
     * @param selPosition 要粘贴到的Item位置；
     */
    public void operPasteCopyList(Context context, List<WriteNoteHelper> list, int operPosition,int selPosition){
        MapNoteDBHelper mapNoteDBHelper = null;
        //先将复制的数据插入到数据库里；

        list.add(selPosition, list.get(operPosition));
        for(int i=0; i<list.size(); i++){
            mapNoteDBHelper = new MapNoteDBHelper(context);
            mapNoteDBHelper.updateOrderByFromBlogContentID(list.get(i).getBlogContentID(), i);
        }
    }

}
