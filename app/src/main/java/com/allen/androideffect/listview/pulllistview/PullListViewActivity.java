package com.allen.androideffect.listview.pulllistview;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.allen.androideffect.ListViewAdapter;
import com.allen.androideffect.R;
import com.allen.androideffect.listview.circlelist.CircleAdapter;
import com.allen.androideffect.listview.circlelist.CircleAdapter2;

import java.util.ArrayList;

public class PullListViewActivity extends ListActivity {
	ArrayList<String> data;
	ListAdapter adapter;
	ListView listView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_pulllistview);
		
		initData();
		
		listView = this.getListView();
		adapter= new ListViewAdapter(this, data);
		listView.setAdapter(adapter);
	}
	

	
	private void initData(){
		
		data = new ArrayList<String>();
		for(int i=0; i < 20; i ++){
			data.add("Circle item "+ i);
		}
		
	}
	
	

}
