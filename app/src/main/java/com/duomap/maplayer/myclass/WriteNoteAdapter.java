package com.duomap.maplayer.myclass;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.duomap.maplayer.R;
import com.duomap.maplayer.Util.LogUtils;

import java.util.List;

/**
 * Created by Administrator on 2017-12-24.
 */

public class WriteNoteAdapter extends BaseAdapter {
    private Context context;
    private List<WriteNoteHelper> list;
    private LayoutInflater mInflater;

    public WriteNoteAdapter(Context context, List<WriteNoteHelper> list) {
        this.context = context;
        this.list = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        WriteNoteHelper writeNoteHelper = list.get(position);
        return writeNoteHelper.getTypeContent();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        WriteNoteHelper writeNoteHelper = null;
        writeNoteHelper = list.get(i);

        int mBlogContentID = writeNoteHelper.getBlogContentID();
        int mNoteType = writeNoteHelper.getTypeContent();
        String mNoteContent = writeNoteHelper.getContentText();
        String mThumbnail = writeNoteHelper.getThumbnail();
        int mOrderBy = writeNoteHelper.getOrderBy();
        int mIsUpMerge = writeNoteHelper.getIsUpMerge();

        ViewHolder viewHolder = null;
        if(view == null) {
            viewHolder = new ViewHolder();
            view = mInflater.inflate(R.layout.listview_item_writenote_text, null);
            viewHolder.tvWNBlogContentID = (TextView) view.findViewById(R.id.tvWNBlogContentID);
            viewHolder.tvWNOrderByID = (TextView) view.findViewById(R.id.tvWNOrderByID);
            viewHolder.tvNoteText = (TextView) view.findViewById(R.id.tvWNNoteText);
            viewHolder.tvNoteType = (TextView) view.findViewById(R.id.tvWNNoteType);
            viewHolder.ivNoteImage = (ImageView) view.findViewById(R.id.ivWNNoteImage);
            viewHolder.tvWNIsUpMerge = (TextView) view.findViewById(R.id.tvWNIsUpMerge);
            viewHolder.tvWNIsUpMergePic = (TextView) view.findViewById(R.id.tvWNIsUpMergePic);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

//        viewHolder.ivNoteImage.setTag(mNoteType);
        viewHolder.tvWNBlogContentID.setText(String.valueOf(mBlogContentID));
        viewHolder.tvWNOrderByID.setText(String.valueOf(mOrderBy));
        viewHolder.tvNoteType.setText(String.valueOf(mNoteType));
        viewHolder.tvNoteText.setText(mNoteContent);
        viewHolder.tvWNIsUpMerge.setText(String.valueOf(mIsUpMerge));
        if(mNoteType==1) {
            viewHolder.ivNoteImage.setVisibility(View.GONE);
            viewHolder.tvNoteText.setVisibility(View.VISIBLE);
        }else if(mNoteType==2||mNoteType==4){
            ToolsClass toolsClass = new ToolsClass();
            String showThumb="";
            if(TextUtils.isEmpty(mThumbnail)){
                showThumb = mNoteContent;
            }else{
                showThumb = mThumbnail;
            }
            LogUtils.e(mThumbnail);
            Bitmap bitmapPic = toolsClass.getLoacalBitmap(mThumbnail, false);
            viewHolder.ivNoteImage.setImageBitmap(bitmapPic);

            viewHolder.ivNoteImage.setVisibility(View.VISIBLE);
            viewHolder.tvNoteText.setVisibility(View.GONE);
        }

        if(mIsUpMerge==0){
            viewHolder.tvWNIsUpMergePic.setVisibility(View.GONE);
        }else if(mIsUpMerge==1){
            viewHolder.tvWNIsUpMergePic.setVisibility(View.VISIBLE);
        }
        return view;
    }

    class ViewHolder{
        public TextView tvWNBlogContentID;
        public TextView tvWNOrderByID;
        public TextView tvNoteText;
        public TextView tvNoteType;
        public ImageView ivNoteImage;
        public TextView tvWNIsUpMerge;
        public TextView tvWNIsUpMergePic;
    }
}
