package com.allen.androideffect.custom;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.Scroller;

import com.allen.androideffect.interpolator.LeCubicBezierInterpolator;

/**
 * Created by Administrator on 2016/9/18.
 */
public class WidgetViewGroup extends LinearLayout {
    final static String TAG = "allen";
    private static final int INVALID_POINTER = -1;
    private static final int DURATION_MILS = 500;
    private int mScrollX ;
    int mScrollRange = 0;
    int mActivePointerId = 0;
    private int mLastMotionX;
    boolean mIsBeingDragged = false;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private LeHorizontalScroller[] mChildScrollers;
    private Interpolator mInterpolator;
    private ValueAnimator mValueAnimators;
    private boolean  mLeftDirect = true;
    private float durationScale = 1.0f * 825 / 945;
    private boolean mFling = false;

    public WidgetViewGroup(Context context) {
        this(context, null);
    }

    public WidgetViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WidgetViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mScrollX = 0;
        Path path = new Path();
        path.moveTo(0.0f, 0.0f);
        path.cubicTo(1 / 3f, 0, 0, 1, 1, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mInterpolator = new PathInterpolator(path);
            //mInterpolator = new LinearInterpolator();
        } else {
            mInterpolator = new DecelerateInterpolator();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int count = getChildCount();
        mChildScrollers = new LeHorizontalScroller[count];
        for (int i = 0; i < count; i++) {
            LeHorizontalScroller scroller = new LeHorizontalScroller(getContext(), mInterpolator);
            mChildScrollers[i] = scroller;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = getChildCount();
        int maxWidth = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            maxWidth += lp.leftMargin + lp.rightMargin + child.getMeasuredWidth();
        }
        mScrollRange = maxWidth - getMeasuredWidth();
        Log.d(TAG,"mScrollRange = "+mScrollRange );
        if (mScrollRange < 0) {
            mScrollRange = 0;
        }
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private boolean inChild(int x, int y) {
        if (getChildCount() > 0) {
            final float scrollX = mScrollX;
            final View child = getChildAt(0);
            return !(y < child.getTop()
                    || y >= child.getBottom()
                    || x < child.getLeft() - scrollX
                    || x >= child.getRight() - scrollX);
        }
        return false;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = (int) ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {

                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + activePointerId
                            + " in onInterceptTouchEvent");
                    break;
                }

                final int x = (int) ev.getX(pointerIndex);
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                if (xDiff > mTouchSlop) {
                    mIsBeingDragged = true;
                    mLastMotionX = x;
                    initVelocityTrackerIfNotExists();
                    mVelocityTracker.addMovement(ev);
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                final int x = (int) ev.getX();
                if (!inChild((int) x, (int) ev.getY())) {
                    mIsBeingDragged = false;
                    recycleVelocityTracker();
                    break;
                }
                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionX = x;
                mActivePointerId = ev.getPointerId(0);

                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);

                /*
                * If being flinged and user touches the screen, initiate drag;
                * otherwise don't.  mScroller.isFinished should be false when
                * being flinged.
                */
//                mIsBeingDragged = !mScroller.isFinished();
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                /* Release the drag */
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
//                if (mScroller.springBack(mScrollX, mScrollY, 0, getScrollRange(), 0, 0)) {
//                    postInvalidateOnAnimation();
//                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                mLastMotionX = (int) ev.getX(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                mLastMotionX = (int) ev.getX(ev.findPointerIndex(mActivePointerId));
                break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(ev);
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if (getChildCount() == 0) {
                    return false;
                }
//                if ((mIsBeingDragged = !mScroller.isFinished())) {
//                    final ViewParent parent = getParent();
//                    if (parent != null) {
//                        parent.requestDisallowInterceptTouchEvent(true);
//                    }
//                }
//                if (!mScroller.isFinished()) {
//                    mScroller.abortAnimation();
//                }

                // Remember where the motion event started
                mLastMotionX = (int) ev.getX();
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
                    break;
                }
                final int x = (int) ev.getX(activePointerIndex);
                int deltaX = x - mLastMotionX;
                if (!mIsBeingDragged && Math.abs(deltaX) > mTouchSlop) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    mIsBeingDragged = true;
                    if (deltaX > 0) {
                        deltaX -= mTouchSlop;
                    } else {
                        deltaX += mTouchSlop;
                    }
                }
                if (mIsBeingDragged) {
                    mLastMotionX = (int) ev.getX();
                    if(!scrollBy(deltaX)) {
                        mVelocityTracker.clear();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getXVelocity(mActivePointerId);

                    if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                        fling(initialVelocity);
                    } else {
//                            if (mScroller.springBack(mScrollX, 0, 0,
//                                    mScrollRange, 0, 0)) {
//                                postInvalidateOnAnimation();
//                            }
                    }
                    mActivePointerId = INVALID_POINTER;
                    mIsBeingDragged = false;
                    recycleVelocityTracker();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged && getChildCount() > 0) {
//                    if (mScroller.springBack(mScrollX, mScrollY, 0, getScrollRange(), 0, 0)) {
//                        postInvalidateOnAnimation();
//                    }
                    mActivePointerId = INVALID_POINTER;
                    mIsBeingDragged = false;
                    recycleVelocityTracker();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }
        return true;
    }


    private boolean scrollBy(int dx) {
        Log.i(TAG, "dx=" + dx+", mScrollX="+mScrollX);
        int count = getChildCount();
        int scrollX;
        scrollX = dx + mScrollX;

        if (scrollX > 0) {
            scrollX = 0;
        } else if ( Math.abs(scrollX) > mScrollRange) {
            scrollX = -mScrollRange;
        }
        float scrollBy = scrollX - mScrollX;
        if (scrollBy == 0) {
            return false;
        }
        mScrollX = scrollX;
        for (int i = 0; i < count; i++) {
            getChildAt(i).setTranslationX(mScrollX);
        }
//        int delay = 0;
//        if(Math.abs(scrollBy) < 6){
//            delay = (int)Math.abs(scrollBy*scrollBy);
//        }
//        for (int i = 0; i < count; i++) {
//            ChildScroller scroller = mChildScrollers[i];
//            if(scrollBy < 0) {
//                scroller.start((int) getChildAt(i).getTranslationX(), (int) mScrollX, i * delay);
//            }else{
//                scroller.start((int) getChildAt(i).getTranslationX(), (int) mScrollX, (count-i-1) * delay);
//            }
//        }
        postInvalidateOnAnimation();
        return true;
    }

    public void fling(int velocityX) {
        int count = getChildCount();
        mFling = true;
        for (int i = 0; i < count; i++) {
            LeHorizontalScroller scroller = mChildScrollers[i];
            if(velocityX < 0) {
                scroller.fling(mScrollX, velocityX, -mScrollRange,
                        0,i*33);
            }else{
                scroller.fling(mScrollX, velocityX, -mScrollRange,
                        0,(count-i-1)*33);
            }
        }
        postInvalidateOnAnimation();
    }

    @Override
    public void computeScroll() {
        int count = getChildCount();
        boolean finished = true;
        for (int i = 0; i < count; i++) {
            LeHorizontalScroller scroller = mChildScrollers[i];
            if (scroller.computeScrollOffset()) {
                scroller.getCurrVelocity();
                int x = scroller.getCurrX();
                finished = false;
                getChildAt(i).setTranslationX(x);
            }
        }
        if(!finished) {
            postInvalidateOnAnimation();
        }else{
            mFling = false;
        }

    }
    class ChildScroller{
        boolean mStarted = false;
        LeHorizontalScroller mScroller ;
        int mStartX;
        int mEndX;
        int mId;

        ChildScroller(Context context, Interpolator interpolator,int id){
            mScroller= new LeHorizontalScroller(context, interpolator);
            mId = id;
        };
        void start(int startx,int endx, int delay){
            mStartX = startx;
            mEndX = endx;
            int dx = mEndX-mStartX;

            if(mScroller.isScrollingInDirection(dx) ){
                mScroller.setFinalX(mEndX);
            }else{
                mScroller.startScroll(mStartX,dx, DURATION_MILS, 20);
            }

//            int duration =(int) (1.0f * Math.abs(dx) * 825/945);
//            if(mScroller.isScrollingInDirection(dx) ){
//                int extend = mScroller.timePassed() + duration - mScroller.getDuration();
//                        mScroller.setFinalX(mEndX);
//                if (extend > 0) {
//                        mScroller.extendDuration(extend);
//                }
//            }else{
//                mScroller.startScroll(mStartX,dx, duration, delay);
//            }

            mStarted = true;
        }

        boolean isScrollingInDirection(float xvel ){
            return mScroller.isScrollingInDirection(xvel);
        }

        boolean computeScrollOffset(){
            return mScroller.computeScrollOffset();
        }

        int getCurX(){
            int cur = mScroller.getCurrX();
                Log.d(TAG, "id= " + mId +",cur =" + cur);
            return cur;
        }
    }
}
