package com.duomap.maplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.duomap.maplayer.myclass.ExitApplication;
import com.duomap.maplayer.myclass.MapNoteDBHelper;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * desc: 我的旅记
 */
public class BlogListShowActivity extends Activity {
    ListView lvBlogList;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            final List<Map<String, Object>> list = new MapNoteDBHelper(BlogListShowActivity.this).getBlogInfoList();
            SimpleAdapter simpleAdapter = new SimpleAdapter(BlogListShowActivity.this, list, R.layout.listview_item_map_line,
                    new String[]{"BlogInfoID", "BlogTitle", "WriteDate"},
                    new int[]{R.id.tvLineID, R.id.tvLineTitle, R.id.tvLineTime});
            lvBlogList.setAdapter(simpleAdapter);

            lvBlogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                    int blogInfoID = Integer.parseInt((String) list.get(position).get("BlogInfoID"));
                    Intent intent = new Intent();
                    intent.putExtra("BlogInfoID", String.valueOf(blogInfoID));
                    intent.putExtra("LineID", "0");
                    intent.setClass(BlogListShowActivity.this, WriteNoteActivity.class);
                    BlogListShowActivity.this.startActivity(intent);
                }
            });

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_list_show);
        ExitApplication.getInstance().addActivity(this);

        iniView();
        mHandler.post(mRunnable);
    }

    private void iniView() {
        lvBlogList = (ListView) findViewById(R.id.lvBlogList);
    }
}
