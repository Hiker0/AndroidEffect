package com.allen.androideffect.listview.swipelist;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.allen.androideffect.ListViewAdapter;
import com.allen.androideffect.R;

public class SwipeListActivity extends ListActivity  {

	SwipeListView mListView;
	ListViewAdapter adapter;
	ArrayList<String> mList;
	
	private void initList(){
		mList = new ArrayList<String>();
		for(int i=0;i < 20; i++){
			mList.add("item" + i);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.swipe_list_activity);
		
		initList();
		mListView = (SwipeListView) this.getListView();
		adapter =  new ListViewAdapter(this, mList);
		mListView.setSwipeAdapter(new SwipeListAdapter(this,adapter) );
		
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}


	
	class SwipeListAdapter extends AbstractSwipeListAdapter{

		public SwipeListAdapter(Context context, ListAdapter adapter) {
			super(context, adapter);
			// TODO Auto-generated constructor stub
		}
		@Override
		public int getDragLayoutResource(int position, boolean isLeft) {
			// TODO Auto-generated method stub
				if(isLeft){
					return R.layout.swipelist_left_menu;
				}else{
					return R.layout.swipelist_right_menu;
				}
		}
		@Override
		public void onToggle(int position, boolean isLeft) {
			// TODO Auto-generated method stub
			Toast.makeText(SwipeListActivity.this, adapter.getItem(position)+(isLeft?" left":" right"), 100).show();
		}
		@Override
		public void onSwipe(int position, boolean isLeft) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSwipeCancel(int position) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onItemClickListener(View view, int position) {
			// TODO Auto-generated method stub
			Toast.makeText(SwipeListActivity.this, adapter.getItem(position), 100).show();
		}
	
	}

}
