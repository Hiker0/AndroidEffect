package com.allen.androideffect.listview.swipelist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.allen.androideffect.Utils;

public class SwipeListView extends ListView {
	
	public static final int SWIPE_NONE = 0;
	public static final int SWIPE_X = 1;	
	public static final int SWIPE_Y = 2;
	
	public static final int MAX_Y = 7;
	public static final int MAX_X = 5;
	
	

	private AbstractSwipeListAdapter mAdapter;
	float downX,downY;
	int touchPosition;
	int swipe_state = SWIPE_NONE;
	SwipeListItemMenu touchItem;
	OnItemClickListener mListener;
	Context mContext;
	public SwipeListView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	
	public SwipeListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	
	public SwipeListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public void setSwipeAdapter(AbstractSwipeListAdapter adapter) {
		// TODO Auto-generated method stub
		mAdapter = adapter;
		setAdapter(mAdapter);
	}
	@Override
	public void setOnItemClickListener(OnItemClickListener listener) {
		// TODO Auto-generated method stub
		mListener = listener;

	}


	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		float x= ev.getX();
		float y = ev.getY();
		float dx ,dy;
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			
			downX = x;
			downY = y;

			int position = this.pointToPosition((int)downX, (int)downY);
			if(position != touchPosition){
				touchPosition=position;
				if(touchItem!=null && touchItem.isSwiping()){
					touchItem.onRelease(0);
				}
				touchItem = (SwipeListItemMenu) this.getChildAt(touchPosition-getFirstVisiblePosition());
			}
		
		case MotionEvent.ACTION_MOVE:
			 dx = x-downX;
			 dy = y-downY;
			switch(swipe_state){
			
			case SWIPE_NONE:
				if(Math.abs(dy) > Utils.convertDpToPixel(mContext,MAX_Y)){
					swipe_state=SWIPE_Y;
				}else if(Math.abs(dx) > Utils.convertDpToPixel(mContext,MAX_X)){
					swipe_state = SWIPE_X;
					if(touchItem != null){
						touchItem.onSwipe((int) dx);
						return true;
					}	
					
				}
				break;
			case SWIPE_X:
				if(touchItem != null){
					touchItem.onSwipe( (int) dx);
					return true;
				}
				break;
			case SWIPE_Y:
				break;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			dx = x-downX;
			if(swipe_state == SWIPE_X){
				
				if(touchItem != null){
					touchItem.onRelease((int) dx);
				}
			}else if(swipe_state == SWIPE_NONE){
				if(mAdapter != null) {
					mAdapter.onItemClickListener(touchItem, touchPosition);
				}
			}
			
			swipe_state = SWIPE_NONE;
			touchPosition = -1;
			
		}
		
		return super.onTouchEvent(ev);
	}
}
