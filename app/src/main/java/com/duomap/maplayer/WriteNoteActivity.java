package com.duomap.maplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.duomap.maplayer.myclass.ExitApplication;
import com.duomap.maplayer.myclass.MapNoteDBHelper;
import com.duomap.maplayer.Util.ShareUtils;
import com.duomap.maplayer.myclass.ToolsClass;
import com.duomap.maplayer.myclass.UploadHelper;
import com.duomap.maplayer.myclass.WriteNoteAdapter;
import com.duomap.maplayer.myclass.WriteNoteHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

/**
 * @author Administrator
 * desc:详情
 */
public class WriteNoteActivity extends Activity implements View.OnClickListener {
    private TextView tvProgress;
    private EditText etWriteNote;
    private Button btnSend;
    private ListView lvWriteNote;
    private int selBlogInfoID, selLineID;
    public String selBlogTitle;
    private int selListPosition;
    //记录操作的ListPosition
    private int opListPosition;
    private int opTypeId;

    private List<UploadQueue> listUploadQueue;
    private int isUploading;    //是否处于上传状态；当点击“发布”按钮是，标记为1；当最后上传文字内容结束后，标记为0；
    private int uploadingListId;
    private UploadHelper uploadHelper;

    private String uploadContentUrl = ToolsClass.WEB_URL_PATH + "upload_blog_content.php";
    private String uploadPicUrl = ToolsClass.WEB_URL_PATH + "upload_blog_pic.php";

    private List<WriteNoteHelper> listContent = new ArrayList<WriteNoteHelper>();

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (isUploading == 1) {
                //首先先判断是否有文件上传；
                uploadingListId = getUploadingListId(listUploadQueue);
                RequestParams params = null;

                if (uploadingListId == 10000) {
                    uploadHelper.setProgressPos(uploadHelper.getProgressCount());
                    uploadHelper.setProgressHead("正在上传文章！");
                    uploadHelper.setProgressBody("");
                    uploadHelper.showInTextView(tvProgress);

                    //uploadingListId为10000时，表明文件部分已经上传完成，以下上传文本内容部分；
                    MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(WriteNoteActivity.this);
                    params = new RequestParams();
                    params.put("bloginfoid", selBlogInfoID);
                    params.put("blogtitle", mapNoteDBHelper.getBlogTitle(selBlogInfoID));
                    params.put("blogcontent", mapNoteDBHelper.getBlogForPublish(selBlogInfoID).toString());
                    try {
                        postContent(uploadContentUrl, params);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        uploadHelper.setProgressPos(uploadHelper.getProgressPos() + 1);
                        uploadHelper.setProgressHead("正在上传第" + uploadHelper.getProgressPos() + "张图片");
                        uploadHelper.setProgressBody("");
                        uploadHelper.showInTextView(tvProgress);

                        UploadQueue uploadQueue = listUploadQueue.get(uploadingListId);
                        params = new RequestParams();
                        params.put("bloginfoid", selBlogInfoID);
                        params.put("blogcontentid", uploadQueue.blogContentId);
//                        postFile(uploadPicUrl, uploadQueue.picPath, params);
                        postFile(uploadPicUrl, ToolsClass.createPicZip(uploadQueue.picPath), params);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private void postContent(String url, RequestParams requestParams) throws IOException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.addHeader("Cookie", ToolsClass.USERINFO_SESSIONID);

        RequestParams params = requestParams;
        asyncHttpClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                int ispass = ToolsClass.getJsonInt(response, "ispass");
                String message = ToolsClass.getJsonString(response, "message");
                if (ispass == 1) {
                    int isdone = ToolsClass.getJsonInt(response, "isdone");
                    if (isdone == 1) {
                        int serverBlogInfoId = ToolsClass.getJsonInt(response, "serverbloginfoid");
                        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(WriteNoteActivity.this);
                        mapNoteDBHelper.updateServerInfoFromBlogInfoID(selBlogInfoID, serverBlogInfoId, "");

                        uploadHelper.setProgressHead("上传成功！");
                        uploadHelper.showInTextView(tvProgress);
                        ToolsClass.setHideAnimation(tvProgress, 3000);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            }
        });
    }

    public void postFile(String url, String filePath, RequestParams requestParams) throws Exception {
        File file = new File(filePath);
        if (file.exists() && file.length() > 0) {
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
            asyncHttpClient.addHeader("Cookie", ToolsClass.USERINFO_SESSIONID);

            RequestParams params = requestParams;
            params.put("uploadfile", file);
            asyncHttpClient.post(url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    super.onProgress(bytesWritten, totalSize);
                    int count = (int) ((bytesWritten * 1.0 / totalSize) * 100);

                    uploadHelper.setProgressBody(count + "%");
                    uploadHelper.showInTextView(tvProgress);
                }

                @Override
                public void onRetry(int retryNo) {
                    super.onRetry(retryNo);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    int ispass = ToolsClass.getJsonInt(response, "ispass");
                    String message = ToolsClass.getJsonString(response, "message");
                    if (ispass == 1) {
                        int isdone = ToolsClass.getJsonInt(response, "isdone");
                        if (isdone == 1) {
                            int fileServerID = ToolsClass.getJsonInt(response, "picuploadinfoid");
                            MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(WriteNoteActivity.this);
                            mapNoteDBHelper.updateServerInfoFromBlogContentID(listUploadQueue.get(uploadingListId).blogContentId, fileServerID, message);

                            listUploadQueue.get(uploadingListId).fileServerId = fileServerID;
                            mHandler.post(mRunnable);
                        }
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                }
            });
        } else {
            Toast.makeText(WriteNoteActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_write_note);
        ExitApplication.getInstance().addActivity(this);
        //启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent = getIntent();
        selBlogInfoID = Integer.parseInt(intent.getStringExtra("BlogInfoID"));
        selLineID = Integer.parseInt(intent.getStringExtra("LineID"));


        iniView();
        iniListener();

        listContent = getIniListViewShow();
        listAdapter(listContent);

        //注册上下文菜单；
        registerForContextMenu(lvWriteNote);
    }

    private void iniView() {
        tvProgress = (TextView) findViewById(R.id.tvProgress_WriteNote);
        etWriteNote = (EditText) findViewById(R.id.et_sendmessage);
        btnSend = (Button) findViewById(R.id.btn_send);
        lvWriteNote = (ListView) findViewById(R.id.lvWriteNote);
    }

    private void iniListener() {
        btnSend.setOnClickListener(this);
        lvWriteNote.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selListPosition = i;
                int typeId = listContent.get(i).getTypeContent();
                if (typeId == 2 || typeId == 4) {
                    Intent intent = new Intent();
                    intent.putExtra("PicPath", listContent.get(i).getContentText());
                    intent.putStringArrayListExtra("PathList", getPicPathList());
                    intent.setClass(WriteNoteActivity.this, PicShowActivity.class);
                    WriteNoteActivity.this.startActivity(intent);
                }
            }
        });
    }

    //获取当前Blog下所有图片路径的List
    private ArrayList<String> getPicPathList() {
        ArrayList<String> list = new ArrayList<String>();
        for (WriteNoteHelper writeNoteHelper : listContent) {
            int typeId = writeNoteHelper.getTypeContent();
            if (typeId == 2 || typeId == 4) {
                list.add(writeNoteHelper.getContentText());
            }
        }
        return list;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                send();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //四个参数的含义。1，group的id,2,item的id,3,是否排序，4，将要显示的内容
        menu.add(0, 1, 1, "修改标题");
        menu.add(0, 2, 2, "上传到服务器");
        menu.add(0, 3, 3, "分享微信好友");
        menu.add(0, 4, 4, "分享到朋友圈");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:// get prompts_edit_blog_title.xmlt_blog_title.xml view
                LayoutInflater li = LayoutInflater.from(this);
                View promptsView = li.inflate(R.layout.prompts_edit_blog_title, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                // set prompts_edit_blog_title.xmlt_blog_title.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);
                final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        // get user input and set it to result
                                        // edit text
                                        selBlogTitle = userInput.getText().toString();
                                        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(WriteNoteActivity.this);
                                        mapNoteDBHelper.updateBlogTitle(selBlogInfoID, selBlogTitle);
                                        getActionBar().setTitle(selBlogTitle);
                                    }
                                })
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                break;
            case 2:
                ToolsClass.setShowAnimation(tvProgress, 500);
                listUploadQueue = getUploadQueueList(selBlogInfoID);
                uploadHelper = new UploadHelper(listUploadQueue.size() + 1, 0, "准备上传！", "");
                isUploading = 1;
                mHandler.post(mRunnable);
                break;

            case 3:
                shareWX(ShareUtils.WXSENCE_RAFIKI);
                break;

            case 4:
                shareWX(ShareUtils.WXSENCE_QUAN);
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 封装点击分享微信（朋友、朋友圈）时，自动生成logo、标题和内容
     *
     * @param sence
     */
    private void shareWX(int sence) {
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(this);
        int serverBlogInfoId = mapNoteDBHelper.getServerBlogInfoId(selBlogInfoID);
        String iconPath = mapNoteDBHelper.getShareIconPath(selBlogInfoID);

        Bitmap bitmap = ToolsClass.getLoacalBitmap(iconPath, false);
        ToolsClass.PicInfo picInfo = ToolsClass.getPicInfoAfterZoom(bitmap, ToolsClass.THUMB_SIZE, ToolsClass.THUMB_SIZE);

        Bitmap iconBitmap = ToolsClass.getImageThumbnail(iconPath, picInfo.picW, picInfo.picH);
        String blogTitle = mapNoteDBHelper.getBlogTitle(selBlogInfoID);
        String blogContent = mapNoteDBHelper.getBlogContentDescription(selBlogInfoID);

        ShareUtils shareUtils = new ShareUtils(WriteNoteActivity.this);
        shareUtils.regToWx();
        shareUtils.setUrlPath(ToolsClass.WEB_URL_SHOW_PATH + "blog_show.php?blogid=" + serverBlogInfoId);
        shareUtils.setTitle(blogTitle);
        shareUtils.setDescription(blogContent);

        shareUtils.setIcon(iconBitmap);
        shareUtils.shareUrlToWx(sence);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 0, 0, "编辑");
        menu.add(0, 1, 1, "复制");
        menu.add(0, 2, 2, "剪切");
        menu.add(0, 3, 3, "向上粘贴");
        menu.add(0, 4, 4, "删除");
        menu.add(0, 5, 5, "上移一行");
        menu.add(0, 6, 6, "下移一行");
        menu.add(0, 7, 7, "↖向上合并自然段");
        menu.add(0, 8, 8, "取消向上合并自然段");
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        WriteNoteHelper downWriteNoteHelper = null;
        WriteNoteHelper upWriteNotHelper = null;
        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(this);
//        WriteNoteHelper writeNoteHelper = new WriteNoteHelper();
        selListPosition = ((AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo()).position;

        switch (menuItem.getItemId()) {
            //操作：复制
            case 1:
                //操作：剪切
            case 2:
                opListPosition = selListPosition;
                opTypeId = menuItem.getItemId();
                break;

            //操作：粘贴
            case 3:
                WriteNoteHelper fromWriteNoteHelper = new WriteNoteHelper();
                if (opTypeId == 1) {
                    String writeDate = ToolsClass.getNowDate();
                    fromWriteNoteHelper = fromWriteNoteHelper.getBlogContentFromID(WriteNoteActivity.this, listContent.get(opListPosition).getBlogContentID());
                    int orderBy = mapNoteDBHelper.getOrderByInBlogContent(fromWriteNoteHelper.getBlogInfoID());
                    fromWriteNoteHelper.setOrderBy(orderBy);
                    fromWriteNoteHelper.setWriteDate(writeDate);
                    fromWriteNoteHelper.setEditDate(writeDate);
                    mapNoteDBHelper.insertBlogContent(fromWriteNoteHelper);
                    fromWriteNoteHelper.setBlogContentID(mapNoteDBHelper.getLastID("DM_BlogContent"));

                    listContent.add(selListPosition, fromWriteNoteHelper);
                } else if (opTypeId == 2) {
                    listContent.add(selListPosition, listContent.get(opListPosition));
                    listContent.remove(opListPosition + 1);
                }

                //更新数据库表BlogContent里关联BlogInfoID的OrderBy；
                for (int i = 0; i < listContent.size(); i++) {
                    mapNoteDBHelper.updateOrderByFromBlogContentID(listContent.get(i).getBlogContentID(), i);
                }
                listAdapter(listContent);
                break;

            //操作：删除
            case 4:
                int blogContentID = listContent.get(selListPosition).getBlogContentID();
                mapNoteDBHelper.deleteBlogContentFromID(blogContentID);
                listContent.remove(selListPosition);

                //更新数据库表BlogContent里关联BlogInfoID的OrderBy；
                for (int i = 0; i < listContent.size(); i++) {
                    mapNoteDBHelper.updateOrderByFromBlogContentID(listContent.get(i).getBlogContentID(), i);
                }
                listAdapter(listContent);
                break;

            //操作：上移一行
            case 5:
                downWriteNoteHelper = listContent.get(selListPosition);
                upWriteNotHelper = listContent.get(selListPosition - 1);
                listContent.add(selListPosition - 1, downWriteNoteHelper);
                listContent.add(selListPosition, upWriteNotHelper);
                listContent.remove(selListPosition + 1);
                listContent.remove(selListPosition + 1);

                //更新数据库表BlogContent里关联BlogInfoID的OrderBy；
                for (int i = 0; i < listContent.size(); i++) {
                    mapNoteDBHelper.updateOrderByFromBlogContentID(listContent.get(i).getBlogContentID(), i);
                }
                listAdapter(listContent);
                break;

            //操作：下移一行
            case 6:
                int mListPosition = selListPosition + 1;
                downWriteNoteHelper = listContent.get(mListPosition);
                upWriteNotHelper = listContent.get(mListPosition - 1);
                listContent.add(mListPosition - 1, downWriteNoteHelper);
                listContent.add(mListPosition, upWriteNotHelper);
                listContent.remove(mListPosition + 1);
                listContent.remove(mListPosition + 1);

                //更新数据库表BlogContent里关联BlogInfoID的OrderBy；
                for (int i = 0; i < listContent.size(); i++) {
                    mapNoteDBHelper.updateOrderByFromBlogContentID(listContent.get(i).getBlogContentID(), i);
                }
                listAdapter(listContent);
                break;

            case 7:
                mapNoteDBHelper.updateIsUpMergeFromBlogContentID(listContent.get(selListPosition).getBlogContentID(), 1);
                listContent.get(selListPosition).setIsUpMerge(1);
                listAdapter(listContent);
                break;

            case 8:
                mapNoteDBHelper.updateIsUpMergeFromBlogContentID(listContent.get(selListPosition).getBlogContentID(), 0);
                listContent.get(selListPosition).setIsUpMerge(0);
                listAdapter(listContent);
                break;
            default:
                break;
        }
        return super.onContextItemSelected(menuItem);
    }

    public void send() {
        String contString = etWriteNote.getText().toString();
        String writeDate = ToolsClass.getNowDate();

        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(this);
        int mapNoteID = 0;
        int lineID = mapNoteDBHelper.getLineIDFromBlogID(selBlogInfoID);
        int orderBy = mapNoteDBHelper.getOrderByInBlogContent(selBlogInfoID);
        int isUpMerge = 0;
        mapNoteDBHelper.insertBlogContent(selBlogInfoID, lineID, mapNoteID, ToolsClass.NOTETYPE_TEXT, contString, "", orderBy, writeDate, writeDate, isUpMerge);
        int blogContentID = mapNoteDBHelper.getLastID("DM_BlogContent");
//        mapNoteDBHelper.insertMapNote(selLineID, ToolsClass.NOTETYPE_TEXT, contString, writeDate);
//        int MapNoteID = mapNoteDBHelper.getLastMapNoteID();

        WriteNoteHelper writeNoteHelper = new WriteNoteHelper();
        writeNoteHelper.setBlogContentID(blogContentID);
        writeNoteHelper.setTypeContent(ToolsClass.NOTETYPE_TEXT);
        writeNoteHelper.setContentText(contString);
        writeNoteHelper.setOrderBy(orderBy);

        listContent.add(writeNoteHelper);
        listAdapter(listContent);
        etWriteNote.setText("");
    }

    private void listAdapter(List<WriteNoteHelper> list) {
        lvWriteNote.setAdapter(null);
        lvWriteNote.setAdapter(new WriteNoteAdapter(this, list));
    }

    private List<WriteNoteHelper> getIniListViewShow() {
        List<WriteNoteHelper> list = new ArrayList<WriteNoteHelper>();
        WriteNoteHelper writeNoteHelper = new WriteNoteHelper();

        if (selBlogInfoID == 0) {
            selBlogInfoID = writeNoteHelper.createBlogInfo(WriteNoteActivity.this, selLineID);
            list = writeNoteHelper.createBlogContentList(WriteNoteActivity.this, selLineID, selBlogInfoID);
        } else {
            selLineID = new MapNoteDBHelper(this).getLineIDFromBlogID(selBlogInfoID);
            list = writeNoteHelper.getBlogContentListFormBlogInfoID(this, selBlogInfoID);
        }

        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(this);
        selBlogTitle = mapNoteDBHelper.getBlogTitle(selBlogInfoID);
        getActionBar().setTitle(selBlogTitle);
        return list;
    }

    private List<UploadQueue> getUploadQueueList(int blogInfoID) {
        UploadQueue uploadQueue = null;
        List<UploadQueue> list = new ArrayList<UploadQueue>();

        MapNoteDBHelper mapNoteDBHelper = new MapNoteDBHelper(this);
        String sql = "select ID, Content from DM_BlogContent where BlogInfoID=" + blogInfoID + " and TypeID in (2,4) order by OrderBy";
        Cursor cursor = mapNoteDBHelper.getReadableDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            uploadQueue = new UploadQueue();
            uploadQueue.blogContentId = cursor.getInt(0);
            uploadQueue.picPath = cursor.getString(1);
            list.add(uploadQueue);
        }
        return list;
    }

    private int getUploadingListId(List<UploadQueue> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).fileServerId == 0) {
                return i;
            }
        }
        return 10000;
    }

    private class UploadQueue {
        int blogContentId;
        String picPath;
        String zipFile;
        int fileServerId = 0;
    }
}