package com.allen.androideffect.listview.circlelist;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.allen.androideffect.R;

public class CircleAdapter2 extends BaseAdapter {
	private  ArrayList<String> mdata ;
	private Context mContext;
	private LayoutInflater mInflater;
	public  int count;
	
	public CircleAdapter2(Context context, ArrayList<String> list){
		mContext = context;
		mdata = list;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		count = mdata.size();
	}


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return count;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mdata.get(position%mdata.size());
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position ;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		String text = mdata.get(position% mdata.size() );

		if(convertView == null){
			convertView = mInflater.inflate(R.layout.circle_item, null);
			TextView tv = (TextView) convertView.findViewById(R.id.text);
			if(tv != null){
				tv.setText(text);
			}
			  
		}else{
			TextView tv = (TextView) convertView.findViewById(R.id.text);
			if(tv != null){
				tv.setText(text);
			}
		}
		
		Log.d("CircleAdapter", "getView,position="+position+" ,id="+convertView.hashCode());
		return convertView;
		
	}

}
