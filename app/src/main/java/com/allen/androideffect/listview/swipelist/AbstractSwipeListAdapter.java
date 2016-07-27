package com.allen.androideffect.listview.swipelist;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

import com.allen.androideffect.R;

public abstract class AbstractSwipeListAdapter implements WrapperListAdapter, SwipeListItemMenu.OnToggleListener {
	
	Context mContext ;
	ListAdapter mAdapter ;
	LayoutInflater inflater; 
	
	public AbstractSwipeListAdapter(Context context,ListAdapter adapter){
		mContext = context;
		mAdapter = adapter;
		inflater= LayoutInflater.from(mContext);
	}

	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return mAdapter.areAllItemsEnabled();
	}

	@Override
	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		return mAdapter.isEmpty();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		mAdapter.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		mAdapter.unregisterDataSetObserver(observer);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mAdapter.getCount();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mAdapter.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return mAdapter.getItemId(position);
	}                      

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return mAdapter.hasStableIds();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		SwipeListItemMenu container= null;
		View view ;

		if(convertView == null){
			
			container=(SwipeListItemMenu) inflater.inflate(R.layout.swipelist_view_menu, null);
			view=mAdapter.getView(position, convertView, parent);
				
		}else{
			container = (SwipeListItemMenu) convertView;
			view=container.findViewById(R.id.content);
			mAdapter.getView(position, view, parent); 
		}
		
		View left = inflater.inflate(getDragLayoutResource(position,true), null);
		View right = inflater.inflate(getDragLayoutResource(position,false), null);
		container.addLeftMenu(left);
		container.addRightMenu(right);
		container.addContent(view);
		container.setId(position);
		container.setOnToggleListener(this);

		return container;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return mAdapter.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return mAdapter.getViewTypeCount();
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return mAdapter.isEmpty();
	}

	@Override
	public ListAdapter getWrappedAdapter() {
		// TODO Auto-generated method stub
		return this;
	}


	public abstract int getDragLayoutResource(int position,boolean isLeft);
	public abstract void onItemClickListener(final View view,final int position);

}
