package com.allen.androideffect.listview.layoutanimation;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.allen.androideffect.R;

public class ListLayoutAnimation extends Activity implements OnClickListener {
	private Button button, button2, button3, button4, button5;
	private ListView mListView;
	private Animation animation;
	private LayoutAnimationController controller;
	private String[] arry = { "item1", "item2", "item3", "item4", "item5", "item6" };
	private ArrayAdapter<String> adapter;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listlayout_animation);
		initView();
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, arry);
		mListView.setAdapter(adapter);

	}

	private void initView() {
		// TODO Auto-generated method stub
		mListView = (ListView) findViewById(R.id.list);
		button = (Button) findViewById(R.id.btn_translate);
		button.setOnClickListener(this);
		button2 = (Button) findViewById(R.id.btn_alpha);
		button2.setOnClickListener(this);
		button3 = (Button) findViewById(R.id.btn_rotate);
		button3.setOnClickListener(this);
		button4 = (Button) findViewById(R.id.btn_scale);
		button4.setOnClickListener(this);
		button5 = (Button) findViewById(R.id.btn_rotate3d);
		button5.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.btn_translate:
			animation = new TranslateAnimation(-50f, 0f, 0f, 0f);
			animation.setDuration(500);
			controller = new LayoutAnimationController(animation, 1f);
			controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
			mListView.setLayoutAnimation(controller);
			adapter.notifyDataSetInvalidated();
			break;
		case R.id.btn_alpha:
			animation = new AlphaAnimation(0f, 1f);
			animation.setDuration(500);
			controller = new LayoutAnimationController(animation, 1f);
			controller.setOrder(LayoutAnimationController.ORDER_REVERSE);
			mListView.setLayoutAnimation(controller);
			adapter.notifyDataSetInvalidated();
			break;
		case R.id.btn_rotate:
			animation = new RotateAnimation(0f, 360f);
			animation.setDuration(500);
			controller = new LayoutAnimationController(animation, 1f);
			controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
			mListView.setLayoutAnimation(controller);
			adapter.notifyDataSetInvalidated();
			break;
		case R.id.btn_scale:
			animation = new ScaleAnimation(0.1f, 1.0f, 0.1f, 1.0f);
			animation.setDuration(500);
			controller = new LayoutAnimationController(animation, 1f);
			controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
			mListView.setLayoutAnimation(controller);
			adapter.notifyDataSetInvalidated();
			break;
		case R.id.btn_rotate3d:
			animation = new Rotate3dAnimation(0, 360, 200, 200, 0, true);
			animation.setDuration(1000);
			controller = new LayoutAnimationController(animation, 0.1f);
			controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
			mListView.setLayoutAnimation(controller);
			adapter.notifyDataSetInvalidated();
			break;
		default:
			break;
		}

	}
}
