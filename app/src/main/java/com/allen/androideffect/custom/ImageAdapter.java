package com.allen.androideffect.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.allen.androideffect.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/9/14.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    ArrayList<Integer> mImageList;
    private LayoutInflater mInflater;

    ImageAdapter(Context context, ArrayList<Integer> list){
        mImageList = list;
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return mImageList.size();
    }

    @Override
    public Object getItem(int position) {
        return mImageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int image = mImageList.get(position);

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.image_item1, null);
            ImageView iv = (ImageView) convertView.findViewById(R.id.id_image);
            iv.setImageResource(image);

        }else{
            ImageView iv = (ImageView) convertView.findViewById(R.id.id_image);
            iv.setImageResource(image);
        }

        return convertView;
    }
}
