package com.allen.androideffect;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

public class ListViewAdapter extends BaseAdapter implements ListAdapter{

	ArrayList<String> mList = null;
	Context mContext;
	
	public ListViewAdapter(Context context, ArrayList<String> list){
		mList = list;
		mContext = context;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(mContext);
		
		ListViewItem view;
		if(convertView != null){
			view = (ListViewItem) convertView;
		}else{
			view = (ListViewItem) inflater.inflate(R.layout.list_view_item, null);
		}
		view.setText(mList.get(position));
		return view;
	}

}
