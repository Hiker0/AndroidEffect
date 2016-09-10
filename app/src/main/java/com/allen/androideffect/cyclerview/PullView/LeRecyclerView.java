package com.allen.androideffect.cyclerview.PullView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

/**
 * Created by zhouxufeng on 16-8-12.
 */
public class LeRecyclerView extends RecyclerView{

    private static final int MAX_Y_OVERSCROLL_DISTANCE = 100;

    private Context mContext;
    private int mMaxYOverscrollDistance;
    private double scale=0.8;
    private int totalDy = 0;
    int offX = 0;
    LinearLayoutManager mLayoutManager;

    public LeRecyclerView(Context context) {
        super(context);
    }

    public LeRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOverScrollMode(OVER_SCROLL_IF_CONTENT_SCROLLS);
        final DisplayMetrics metrics = context.getResources()
                .getDisplayMetrics();
        final float density = metrics.density;
        mMaxYOverscrollDistance = (int)(density * MAX_Y_OVERSCROLL_DISTANCE);
        setNestedScrollingEnabled(true);
    }

    public void setflingScale(double scale){
        this.scale = scale;
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        velocityX *= scale;
        return super.fling(velocityX, velocityY);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        offX += dx;
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if(layout instanceof LinearLayoutManager) {
            mLayoutManager = (LinearLayoutManager) layout;
        }
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {

        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }


    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
//        switch (state) {
//            case RecyclerView.SCROLL_STATE_IDLE:
//                int first = mLayoutManager.findFirstVisibleItemPosition();
//                int scrollX = offX % weight;
//                int scroll = 0;
//                if (scrollX > weight / 2) {
//                    scroll = weight - scrollX;
//                } else {
//                    scroll = -scrollX;
//                }
//                smoothScrollBy(scroll, 0);
//                break;
//            default:
//                break;
//        }
    }

}
