package com.allen.androideffect.listview.circlelist;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.allen.androideffect.R;

public class CircleListShow extends ListActivity {
	ArrayList<String> data;
	ListAdapter adapter;
	CircleAdapter2 adapter2;
	ListView listView,listView2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.circlelist_show);
		
		initData();
		
		listView = this.getListView();
		listView2 = (ListView) this.findViewById(R.id.list2);
		
		
		adapter= new CircleAdapter(this, data);
		listView.setAdapter(adapter);
		listView.setSelection(adapter.getCount()/2/data.size()*data.size()); 
		
		adapter2 =  new CircleAdapter2(this, data);
		listView2.setAdapter(adapter2);
		listView2.setOnScrollListener(mScrollListener);
		
	}
	
	AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener(){

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			Log.d("CircleListShow","onScroll:firstVisibleItem="+ firstVisibleItem +
					" visibleItemCount="+visibleItemCount+
					" totalItemCount="+totalItemCount);
			
			boolean loadMore = /* maybe add a padding */
					firstVisibleItem + visibleItemCount >= totalItemCount;

		        if(loadMore) {
		        	adapter2.count += visibleItemCount; // or any other amount
		        	adapter2.notifyDataSetChanged();
		        }
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			Log.d("CircleListShow","onScrollStateChanged scrollState="+scrollState);

		}
		
	};
	
	private void initData(){
		
		data = new ArrayList<String>();
		for(int i=0; i < 20; i ++){
			data.add("Circle item "+ i);
		}
		
	}
	
	

}
