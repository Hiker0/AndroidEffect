package com.allen.androideffect;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListViewItem extends LinearLayout {
	
	TextView mTextView;
	public ListViewItem(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	public ListViewItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	public ListViewItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mTextView = (TextView) this.findViewById(R.id.title);
	}
	
	public void setText(String string){
		mTextView.setText(string);
	}

}
