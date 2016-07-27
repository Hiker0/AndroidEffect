package com.allen.androideffect.listview.swipelist;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.allen.androideffect.R;

public class SwipeListItemMenu extends FrameLayout {

	ImageView mLeftBackground = null;
	ImageView mRightBackground = null;
	View leftView, rightView, contentView;
	FrameLayout menuContainer;
	LinearLayout contentContainer;
	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
			LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

	public final int SWIPE_STATE_NONE = 0;
	public final int SWIPE_STATE_LEFT = 1;
	public final int SWIPE_STATE_RIGHT = 2;
	public final int SWIPE_LEFT_ANIMATION = 3;
	public final int SWIPE_RIGHT_ANIMATION = 4;
	public final int SWIPE_LEFT_ANIMATION_CANCEL = 5;
	public final int SWIPE_RIGHT_ANIMATION_CANCEL = 6;
	private final int SWIPE_ANIMATION_TIME = 500;
	private int swipe_state = SWIPE_STATE_NONE;
	private ValueAnimator animation;
	private int mId = -1;

	public SwipeListItemMenu(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public SwipeListItemMenu(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public SwipeListItemMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mLeftBackground = (ImageView) this.findViewById(R.id.menu_bg_left);
		mRightBackground = (ImageView) this.findViewById(R.id.menu_bg_right);
		menuContainer = (FrameLayout) this.findViewById(R.id.menu_container);
		contentContainer = (LinearLayout) this
				.findViewById(R.id.content_container);
	}

	public void setId(int id) {
		mId = id;
	}

	public boolean isSwiping() {
		return swipe_state != SWIPE_STATE_NONE;
	}

	public void addContent(View view) {
		view.setId(R.id.content);
		if (contentView != null) {
			contentContainer.removeView(contentView);
			contentView = view;
			contentContainer.addView(contentView, lp);
		} else {
			contentView = view;
			contentContainer.addView(contentView, lp);
		}
	}

	public void addLeftMenu(View view) {
		if (leftView != null) {
			menuContainer.removeView(leftView);
			leftView = view;
			menuContainer.addView(leftView, lp);
		} else {
			leftView = view;
			menuContainer.addView(leftView, lp);
		}
	}

	public void addRightMenu(View view) {
		if (rightView != null) {
			menuContainer.removeView(rightView);
			rightView = view;
			menuContainer.addView(rightView, lp);
		} else {
			rightView = view;
			menuContainer.addView(rightView, lp);
		}
	}

	public void removeAll(View view) {
		leftView = null;
		rightView = null;
		menuContainer.removeAllViews();
		contentView = null;
		contentContainer.removeAllViews();
	}

	public void onSwipeLeft() {
		swipe_state = SWIPE_STATE_LEFT;
		leftView.setVisibility(View.VISIBLE);
		mLeftBackground.setVisibility(View.VISIBLE);
		rightView.setVisibility(View.INVISIBLE);
		mRightBackground.setVisibility(View.INVISIBLE);
	}

	public void onSwipeRight() {
		swipe_state = SWIPE_STATE_RIGHT;
		rightView.setVisibility(View.VISIBLE);
		mRightBackground.setVisibility(View.VISIBLE);
		leftView.setVisibility(View.INVISIBLE);
		mLeftBackground.setVisibility(View.INVISIBLE);
	}


	
	public void onSwipe(int dx) {
		if (swipe_state >= SWIPE_LEFT_ANIMATION
				&& swipe_state <= SWIPE_RIGHT_ANIMATION_CANCEL) {
			return;
		}
		dx = (int) (1.5f * dx);
		if (dx < 0) {
			if (swipe_state != SWIPE_STATE_LEFT) {
				onSwipeLeft();
				mOnToggleListener.onSwipe(mId, true);
			}
			mLeftBackground.layout(dx, mLeftBackground.getTop(),
					mLeftBackground.getWidth() + dx,
					mLeftBackground.getBottom());
		} else {
			if (swipe_state != SWIPE_STATE_RIGHT) {
				onSwipeRight();
				mOnToggleListener.onSwipe(mId, false);
			}
			mRightBackground.layout(
					this.getRight() + dx - mRightBackground.getWidth(),
					mRightBackground.getTop(), this.getRight() + dx,
					mRightBackground.getBottom());
		}
	}

	private void finishSwipe() {
		swipe_state = SWIPE_STATE_NONE;
		leftView.setVisibility(View.INVISIBLE);
		mLeftBackground.setVisibility(View.INVISIBLE);
		rightView.setVisibility(View.INVISIBLE);
		mRightBackground.setVisibility(View.INVISIBLE);

		mLeftBackground.layout(0, mLeftBackground.getTop(),
				mLeftBackground.getWidth(), mLeftBackground.getBottom());
		mRightBackground.layout(this.getRight() - mRightBackground.getWidth(),
				mRightBackground.getTop(), this.getRight(),
				mRightBackground.getBottom());
	}

	public void onRelease(int dx) {
		if (swipe_state >= SWIPE_LEFT_ANIMATION
				&& swipe_state <= SWIPE_RIGHT_ANIMATION_CANCEL) {
			return;
		}

		if (animation != null && animation.isRunning()) {
			animation.cancel();
		}
		animation = null;

		dx = (int) (1.5f * dx);
		if (swipe_state == SWIPE_STATE_LEFT) {
			int tarX = 0;
			if (dx +  getWidth()  < 0) {
				tarX = getWidth() - mLeftBackground.getWidth();
				swipe_state = SWIPE_LEFT_ANIMATION;
			} else {
				swipe_state = SWIPE_LEFT_ANIMATION_CANCEL;
				tarX = 0;
			}

			animation = ValueAnimator.ofInt(dx, tarX);
			animation.addUpdateListener(leftBgUpdate);
		} else if (swipe_state == SWIPE_STATE_RIGHT){
			int tarX = 0;
			if (dx - 3 * getWidth() / 5 > 0) {
				tarX = mRightBackground.getWidth() - getWidth();
				swipe_state = SWIPE_RIGHT_ANIMATION;
			} else {
				swipe_state = SWIPE_RIGHT_ANIMATION_CANCEL;
				tarX = 0;
			}
			animation = ValueAnimator.ofInt(dx, tarX);
			animation.addUpdateListener(rightBgUpdate);

		}else {
			finishSwipe();
			//mOnToggleListener.onSwipeCancel(mId);
		}

		if (animation != null) {
			animation.setDuration(SWIPE_ANIMATION_TIME);
			animation.addListener(animationListener);
			animation.setInterpolator(value);
			animation.start();
		}

	}

	TimeInterpolator value = new TimeInterpolator() {

		@Override
		public float getInterpolation(float input) {
			// TODO Auto-generated method stub
			return 1 - (1 - input) * (1 - input);
		}

	};

	AnimatorListener animationListener = new AnimatorListener() {

		@Override
		public void onAnimationStart(Animator animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationEnd(Animator animation) {
			// TODO Auto-generated method stub
			if(swipe_state == SWIPE_LEFT_ANIMATION ||
					swipe_state == SWIPE_RIGHT_ANIMATION){
				mOnToggleListener.onToggle(mId,
					swipe_state == SWIPE_LEFT_ANIMATION ? true : false);
			}else{
				mOnToggleListener.onSwipeCancel(mId);
			}
			
			finishSwipe();
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
			finishSwipe();
			mOnToggleListener.onSwipeCancel(mId);
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			// TODO Auto-generated method stub

		}

	};
	ValueAnimator.AnimatorUpdateListener leftBgUpdate = new ValueAnimator.AnimatorUpdateListener() {

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			// TODO Auto-generated method stub
			int value = (Integer) animation.getAnimatedValue();
			mLeftBackground.layout(value, mLeftBackground.getTop(),
					mLeftBackground.getWidth() + value,
					mLeftBackground.getBottom());
		}
	};

	ValueAnimator.AnimatorUpdateListener rightBgUpdate = new ValueAnimator.AnimatorUpdateListener() {

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			// TODO Auto-generated method stub
			int value = (Integer) animation.getAnimatedValue();
			mRightBackground.layout(
					getRight() + value - mRightBackground.getWidth(),
					mRightBackground.getTop(), getRight() + value,
					mRightBackground.getBottom());
		}
	};

	private OnToggleListener mOnToggleListener;

	public void setOnToggleListener(OnToggleListener mListener) {
		mOnToggleListener = mListener;
	};

	public interface OnToggleListener {
		public abstract void onSwipe(int position, boolean isLeft);

		public abstract void onSwipeCancel(int position);

		public abstract void onToggle(int position, boolean isLeft);

	};
}
