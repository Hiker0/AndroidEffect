package com.allen.androideffect.custom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Scroller;

import com.allen.androideffect.R;

public class HorizontalListView extends AdapterView<ListAdapter> {

    /**
     * Adds a view during layout. This is useful if in your onLayout() method,
     * you need to add more views (as does the list view for example).
     * <p>
     * If index is negative, it means put it at the end of the list.（-1代表尾插）
     *
     * @param child the view to add to the group
     * @param index the index at which the child must be added
     * @param params the layout parameters to associate with the child
     * @param preventRequestLayout if true, calling this method will not trigger a
     * layout request on child
     * @return true if the child was added, false otherwise
     */
    private static final int INSERT_AT_END_OF_LIST = -1;
    private static final int INSERT_AT_START_OF_LIST = 0;

    /**
     * The velocity to use for overscroll absorption
     */
    private static final float FLING_DEFAULT_ABSORB_VELOCITY = 30f;

    /**
     * The friction amount to use for the fling tracker
     */
    private static final float FLING_FRICTION = 0.009f;

    /**
     * 下面两个常量用作状态恢复  ，一个恢复mCurrentScrollX, 一个缓存父类状态
     */
    private static final String BUNDLE_ID_CURRENT_SCROLL_X = "BUNDLE_ID_CURRENT_SCROLL_X";
    private static final String BUNDLE_ID_PARENT_STATE = "BUNDLE_ID_PARENT_STATE";

    /**
     * 用于计算fling时滑动dx的Scroller，会根据你的滑动，为你计算出下一个滚动位置
     */
    protected Scroller mFlingTracker = new Scroller(getContext());

    /**
     * 检测fling等操作的回调
     */
    private final GestureListener mGestureListener = new GestureListener();

    /**
     * 检测fling等操作
     */
    private GestureDetector mGestureDetector;

    /**
     * 记录最左边可见的那个View从什么位置开始展示
     * 取值范围0到-view.getWidth()
     * 其实就是我们layout的时候需要用到的那个offset
     **/
    private int mLeftVisibleViewDisplayOffset;

    /**
     * 适配器
     */
    protected ListAdapter mAdapter;

    /**
     * 回收，缓存View的队列，因为HorizontalListView对ItemViewType做了支持，如果你还不了解的话，自己查一查ListView
     * 怎么使用Adapter的ItemViewType在一个ListView里展示不同的View，这个缓存为每种类型的View使用了独立的缓存，
     * 如果还不明白先画个问号，等到看到存取缓存的时候就明白了
     */
    private List<Queue<View>> mRemovedViewsCache = new ArrayList<Queue<View>>();

    /**
     * 标记数据集改变，你对notifyDataSetChanged一定不陌生
     */
    private boolean mDataSetChanged = false;

    /**
     * Temporary rectangle to be used for measurements
     */
    private Rect mRect = new Rect();

    /**
     * Tracks the currently touched view, used to delegate touches to the view being touched
     */
    private View mViewBeingTouched = null;

    /**
     * The width of the divider that will be used between list items
     */
    private int mDividerWidth = 0;

    /**
     * The drawable that will be used as the list divider
     */
    private Drawable mDividerDrawable = null;

    /**
     * 这个值以像素为单位，是最重要的一个值，记录当前滑动的距离里
     * 取值范围是0-最大滑动距离，也就是最后一个Item完全显示出来的时候，永远是正数
     */
    protected int mCurrentScrollX;

    /**
     * 滑动的过程是平滑的，这需要保存下一个滚动的位置，一般地， mCurrentScrollX和mNextScrollX相差很小，
     * 这样重绘UI的时候才会觉得平滑，mNextScrollX可以通过Scroller计算，亦可以通过GestureListener 计算得到，
     * 不滑动的时候mNextScrollX等于mCurrentScrollX
     */
    protected int mNextScrollX;

    /**
     * Used to hold the scroll position to restore to post rotate
     */
    private Integer mRestoreScrollX = null;

    /**
     * 记录最大的滚动距离，也就是最后一个item完全显示出来的时候的滚动距离。
     * 这个值没有办法初始化，只能在滑动的过程中动态计算，且一旦计算完成就不需要再计算，除非布局发生改变
     */
    private int mMaxScrollX = Integer.MAX_VALUE;

    /**
     * 最左边的View在adapter中的索引
     */
    private int mLeftVisibleViewAdapterIndex;

    /**
     * 最右边的View在adapter中的索引
     */
    private int mRightVisibleViewAdapterIndex;

    /**
     * This tracks the currently selected accessibility item
     */
    private int mCurrentlySelectedAdapterIndex;

    /**
     * 这个可以不用看，不影响我们解释主要逻辑，在这里解释一下，就是滑到adapter index快要到底的时候回调这个方法，
     * 有一个阀值，根据这个阀值来判断是否滑到底了。
     * Callback interface to notify listener that the user has scrolled this view to the point that it is low on data.
     */
    private RunningOutOfDataListener mRunningOutOfDataListener = null;

    /**
     * 没有数据的阀值
     */
    private int mRunningOutOfDataThreshold = 0;

    /**
     * Tracks if we have told the listener that we are running low on data. We only want to tell them once.
     */
    private boolean mHasNotifiedRunningLowOnData = false;

    /**
     * 监听滚动状态变化，有3个状态，IDLE ， TOUCH_SCROLL，FLING
     */
    private OnScrollStateChangedListener mOnScrollStateChangedListener = null;

    /**
     * 当前滚动状态，默认IDLE
     */
    private OnScrollStateChangedListener.ScrollState mCurrentScrollState = OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE;

    /**
     * 滑动过程中左边的亮边
     */
    private EdgeEffectCompat mEdgeGlowLeft;

    /**
     * 滑动过程中右边的亮边
     */
    private EdgeEffectCompat mEdgeGlowRight;

    /**
     * HorizontalListView的高度测量参数 MeasureSpec，我们需要用这个参数来对child施加约束，
     * 从而测量child的高度
     */
    private int mHeightMeasureSpec;

    /**
     * Used to track if a view touch should be blocked because it stopped a fling
     */
    private boolean mBlockTouchAction = false;

    /**
     * 如果HorizontalListView放在一个scrollView等滚动的View中，用来禁用parent处理事件，从而解决滑动冲突
     */
    private boolean mIsParentVerticiallyScrollableViewDisallowingInterceptTouchEvent = false;

    /**
     * The listener that receives notifications when this view is clicked.
     */
    private OnClickListener mOnClickListener;

    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEdgeGlowLeft = new EdgeEffectCompat(context);//左边的滑动亮边
        mEdgeGlowRight = new EdgeEffectCompat(context);//右边的滑动亮边
        mGestureDetector = new GestureDetector(context, mGestureListener);//手势检测
        bindGestureDetector();
        initView();
        retrieveXmlConfiguration(context, attrs);
        setWillNotDraw(false);
        // If the OS version is high enough then set the friction on the fling tracker */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            HoneycombPlus.setFriction(mFlingTracker, FLING_FRICTION);
        }
    }

    /**
     * 为当前的HorizontalListView设置onTouchListener,并把onTouch事件交给我们的手势检测对象mGestureDetector来处理
     */
    private void bindGestureDetector() {
        // Generic touch listener that can be applied to any view that needs to process gestures
        final View.OnTouchListener gestureListenerHandler = new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                // Delegate the touch event to our gesture detector
                return mGestureDetector.onTouchEvent(event);
            }
        };

        setOnTouchListener(gestureListenerHandler);
    }

    /**
     * When this HorizontalListView is embedded within a vertical scrolling view it is important to disable the parent view from interacting with
     * any touch events while the user is scrolling within this HorizontalListView. This will start at this view and go up the view tree looking
     * for a vertical scrolling view. If one is found it will enable or disable parent touch interception.
     *
     * @param disallowIntercept If true the parent will be prevented from intercepting child touch events
     */
    private void requestParentListViewToNotInterceptTouchEvents(Boolean disallowIntercept) {
        // Prevent calling this more than once needlessly
        if (mIsParentVerticiallyScrollableViewDisallowingInterceptTouchEvent != disallowIntercept) {
            View view = this;

            while (view.getParent() instanceof View) {
                // 如果parent是 ListView ， ScrollView ，那么就禁用掉他们的拦截事件能力，从而避免滑动冲突
                if (view.getParent() instanceof ListView || view.getParent() instanceof ScrollView) {
                    view.getParent().requestDisallowInterceptTouchEvent(disallowIntercept);
                    mIsParentVerticiallyScrollableViewDisallowingInterceptTouchEvent = disallowIntercept;
                    return;
                }

                view = (View) view.getParent();
            }
        }
    }

    /**
     * 获取divider 和divider宽度
     */
    private void retrieveXmlConfiguration(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HorizontalListView);

            // Get the provided drawable from the XML
            final Drawable d = a.getDrawable(R.styleable.HorizontalListView_android_divider);
            if (d != null) {
                // If a drawable is provided to use as the divider then use its intrinsic width for the divider width
                setDivider(d);
            }

            // If a width is explicitly specified then use that width
            final int dividerWidth = a.getDimensionPixelSize(R.styleable.HorizontalListView_dividerWidth, 0);
            if (dividerWidth != 0) {
                setDividerWidth(dividerWidth);
            }

            a.recycle();
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {//状态保存，只保存两个状态

        Bundle bundle = new Bundle();

        // Add the parent state to the bundle
        bundle.putParcelable(BUNDLE_ID_PARENT_STATE, super.onSaveInstanceState());

        // Add our state to the bundle
        bundle.putInt(BUNDLE_ID_CURRENT_SCROLL_X, mCurrentScrollX);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {//状态恢复
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            // Restore our state from the bundle
            mRestoreScrollX = Integer.valueOf((bundle.getInt(BUNDLE_ID_CURRENT_SCROLL_X)));

            // Restore out parent's state from the bundle
            super.onRestoreInstanceState(bundle.getParcelable(BUNDLE_ID_PARENT_STATE));
        }
    }

    /**
     * 设置分隔线，同时设置分隔线宽度
     *
     * @param divider The drawable to use.
     */
    public void setDivider(Drawable divider) {
        mDividerDrawable = divider;

        if (divider != null) {
            setDividerWidth(divider.getIntrinsicWidth());
        } else {
            setDividerWidth(0);
        }
    }

    /**
     * 设置分隔线宽度
     */
    public void setDividerWidth(int width) {
        mDividerWidth = width;

        // 强制重布局，重绘
        requestLayout();
        invalidate();
    }

    /**
     * 初始化默认值
     */
    private void initView() {
        mLeftVisibleViewAdapterIndex = -1;
        mRightVisibleViewAdapterIndex = -1;
        mLeftVisibleViewDisplayOffset = 0;
        mCurrentScrollX = 0;
        mNextScrollX = 0;
        mMaxScrollX = Integer.MAX_VALUE;
        setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE);
    }

    /**
     * 重置默认值
     */
    private void reset() {
        initView();
        removeAllViewsInLayout();
        requestLayout();
    }

    /**
     * ListView也使用这个，可以到源码里看一下，继承的层次略深
     */
    private DataSetObserver mAdapterDataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            mDataSetChanged = true;

            // Clear so we can notify again as we run out of data
            mHasNotifiedRunningLowOnData = false;

            unpressTouchedChild();

            // 数据集改变，我们就重绘自己
            invalidate();
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            // Clear so we can notify again as we run out of data
            mHasNotifiedRunningLowOnData = false;

            unpressTouchedChild();
            reset();

            // Invalidate and request layout to force this view to completely redraw itself
            invalidate();
            requestLayout();
        }
    };

    @Override
    public void setSelection(int position) {
        mCurrentlySelectedAdapterIndex = position;
        requestLayout();
        invalidate();
    }

    @Override
    public View getSelectedView() {
        return getChild(mCurrentlySelectedAdapterIndex);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mAdapterDataObserver);
        }

        if (adapter != null) {
            // Clear so we can notify again as we run out of data
            mHasNotifiedRunningLowOnData = false;
            //这样，当我们调用notifyDataSetChanged的时候，onChanged()，onInvalidated()才会被回调
            mAdapter = adapter;
            mAdapter.registerDataSetObserver(mAdapterDataObserver);
        }

        initializeRecycledViewCache(mAdapter.getViewTypeCount());//比较重要
        reset();//requestLayout
    }

    @Override
    public ListAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Will create and initialize a cache for the given number of different types of views.
     *
     * @param viewTypeCount - The total number of different views supported
     */
    private void initializeRecycledViewCache(int viewTypeCount) {
        // 看到了吧，我们为每一个View的类型初始化一个Cache
        mRemovedViewsCache.clear();
        for (int i = 0; i < viewTypeCount; i++) {
            mRemovedViewsCache.add(new LinkedList<View>());
        }
    }

    /**
     * Returns a recycled view from the cache that can be reused, or null if one is not available.
     *
     * @param adapterIndex
     * @return
     */
    private View getRecycledView(int adapterIndex) {
        //从缓存中拿出一个View，但是，我们需要首先根据adapterIndex来获取view的类型。
        //这个adapterIndex就相当于getView的position参数
        int itemViewType = mAdapter.getItemViewType(adapterIndex);

        if (isItemViewTypeValid(itemViewType)) {
            return mRemovedViewsCache.get(itemViewType).poll();//pull one from Q head.
        }

        return null;
    }

    /**
     * Adds the provided view to a recycled views cache.
     *
     * @param adapterIndex
     * @param view
     */
    private void recycleView(int adapterIndex, View view) {
        // There is one Queue of views for each different type of view.
        // Just add the view to the pile of other views of the same type.
        // The order they are added and removed does not matter.
        // 划出屏幕的View都会被回收，参照getRecycledView
        int itemViewType = mAdapter.getItemViewType(adapterIndex);//getItemViewType是adapter的方法，一般开发者很少使用
        if (isItemViewTypeValid(itemViewType)) {
            mRemovedViewsCache.get(itemViewType).offer(view);//push one to Q tail.
        }
    }

    private boolean isItemViewTypeValid(int itemViewType) {
        return itemViewType < mRemovedViewsCache.size();
    }

    /**
     * Adds a child to this viewgroup and measures it so it renders the correct size
     * 第二个参数0，-1两个选项，将一个view放置到HorizontalListView里，但是View只被测量了，没有被layout 和draw
     */
    private void addAndMeasureChild(final View child, int viewPos) {
        LayoutParams params = getLayoutParams(child);
        //Me: perform addView(View) like a ViewGroup. If viewPos is negative then put view at the end.
        addViewInLayout(child, viewPos, params, true);
        measureChild(child);
    }

    /**
     * Measure the provided child.
     *
     * @param child The child.
     */
    private void measureChild(View child) {
        ViewGroup.LayoutParams childLayoutParams = getLayoutParams(child);
        //根据HorizontalListView自身的约束mHeightMeasureSpec，来计算child的childWidthSpec
        //这个地方比较难理解，涉及到View在ViewGroup的Measure过程
        int childHeightSpec = ViewGroup.getChildMeasureSpec(
                mHeightMeasureSpec, getPaddingTop() + getPaddingBottom(), childLayoutParams.height);

        int childWidthSpec;
        if (childLayoutParams.width > 0) {
            childWidthSpec = MeasureSpec.makeMeasureSpec(childLayoutParams.width, MeasureSpec.EXACTLY);
        } else {
            //如果宽度是wrap content，就测量最大宽度
            childWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        //我们并没有使用容器提供的measureChild方法，而是重载，这样，我们就能测量出真实的宽高，而不局限于容器的大小
        child.measure(childWidthSpec, childHeightSpec);
    }

    /**
     * Gets a child's layout parameters, defaults if not available.
     */
    private ViewGroup.LayoutParams getLayoutParams(View child) {
        ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
        if (layoutParams == null) {
            // Since this is a horizontal list view default to matching the parents height, and wrapping the width
            //看，宽度我们用wrap content, 高度我们用 match parent,即使用容器的高度
            layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        return layoutParams;
    }

    /**
     * 核心逻辑在这里。。。。。
     * 为了让滑动生效，我们需要每滑动一小段距离，或是fling一小段距离，就需要调用一下requestLayout来
     * 触发本方法执行，直到滑动结束或是fling完毕，
     * 你可以跳过mEdgeGlowRight的操作看核心逻辑，这个只是个锦上添花，滑动亮边而已
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mAdapter == null) {
            return;
        }

        // Force the OS to redraw this view
        invalidate();//Me: I think this call is useless.

        // If the data changed then reset everything and render from scratch at the same offset as last time
        if (mDataSetChanged) {
            int oldCurrentX = mCurrentScrollX;
            initView();
            removeAllViewsInLayout();
            mNextScrollX = oldCurrentX;// mCurrentX is 0 now.
            mDataSetChanged = false;
        }

        // If restoring from a rotation
        if (mRestoreScrollX != null) {
            mNextScrollX = mRestoreScrollX;
            mRestoreScrollX = null;
        }

        // If in a fling
        //这个方法会帮你计算下一个要滑动的位置，Scroller如何使用，自己去查查
        if (mFlingTracker.computeScrollOffset()) {
            // Compute the next position
            mNextScrollX = mFlingTracker.getCurrX();
        }

        // Prevent scrolling past 0 so you can't scroll past the end of the list to the left
        if (mNextScrollX <= 0) {//做一个简单的非法值校验
            mNextScrollX = 0;

            // Show an edge effect absorbing the current velocity
            if (mEdgeGlowLeft.isFinished()) {
                mEdgeGlowLeft.onAbsorb((int) determineFlingAbsorbVelocity());
            }

            //滑到最左边就强制停止fling
            mFlingTracker.forceFinished(true);
            setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE);
        } else if (mNextScrollX >= mMaxScrollX) {//做一个简单的非法值校验
            // Clip the maximum scroll position at mMaxX so you can't scroll past the end of the list to the right
            mNextScrollX = mMaxScrollX;

            // Show an edge effect absorbing the current velocity
            if (mEdgeGlowRight.isFinished()) {
                mEdgeGlowRight.onAbsorb((int) determineFlingAbsorbVelocity());
            }
            //滑到最右边就强制停止fling
            mFlingTracker.forceFinished(true);
            setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE);
        }

        // Calculate our delta from the last time the view was drawn
        // fling(int startX, int startY, int velocityX, int velocityY,int minX, int maxX, int minY, int maxY)
        //计算滑动的变化量，向左滑动 dx < 0  向右滑动 dx > 0
        int dx = mCurrentScrollX - mNextScrollX;

        // Since the view has now been drawn, update our current position
        mCurrentScrollX = mNextScrollX;

        removeNonVisibleChildren(dx);
        fillList(dx);
        positionChildren(dx);


        // If we have scrolled enough to lay out all views, then determine the maximum scroll position now
        //滑动的过程当中，尝试计算mMaxScrollX
        if (determineMaxX()) {
            // Redo the layout pass since we now know the maximum scroll position
            //递归调用onLayout， 因为requestLayout()会触发onLayout
            ViewCompat.postOnAnimation(this, mDelayedLayout);
            return;
        }

        // If the fling has finished
        if (mFlingTracker.isFinished()) {
            // If the fling just ended
            if (mCurrentScrollState == OnScrollStateChangedListener.ScrollState.SCROLL_STATE_FLING) {
                setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE);
            }
        } else {
            // Still in a fling so schedule the next frame
            //递归调用onLayout， 因为requestLayout()会触发onLayout
            ViewCompat.postOnAnimation(this, mDelayedLayout);
        }
    }

    @Override
    protected float getLeftFadingEdgeStrength() {
        int horizontalFadingEdgeLength = getHorizontalFadingEdgeLength();

        // If completely at the edge then disable the fading edge
        if (mCurrentScrollX == 0) {
            return 0;
        } else if (mCurrentScrollX < horizontalFadingEdgeLength) {
            // We are very close to the edge, so enable the fading edge proportional to the distance from the edge, and the width of the edge effect
            return (float) mCurrentScrollX / horizontalFadingEdgeLength;
        } else {
            // The current x position is more then the width of the fading edge so enable it fully.
            return 1;
        }
    }

    @Override
    protected float getRightFadingEdgeStrength() {
        int horizontalFadingEdgeLength = getHorizontalFadingEdgeLength();

        // If completely at the edge then disable the fading edge
        if (mCurrentScrollX == mMaxScrollX) {
            return 0;
        } else if ((mMaxScrollX - mCurrentScrollX) < horizontalFadingEdgeLength) {
            // We are very close to the edge, so enable the fading edge proportional to the distance from the ednge, and the width of the edge effect
            return (float) (mMaxScrollX - mCurrentScrollX) / horizontalFadingEdgeLength;
        } else {
            // The distance from the maximum x position is more then the width of the fading edge so enable it fully.
            return 1;
        }
    }

    /**
     * Determines the current fling absorb velocity
     */
    private float determineFlingAbsorbVelocity() {
        // If the OS version is high enough get the real velocity */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return IceCreamSandwichPlus.getCurrVelocity(mFlingTracker);
        } else {
            // Unable to get the velocity so just return a default.
            // In actuality this is never used since EdgeEffectCompat does not draw anything unless the device is ICS+.
            // Less then ICS EdgeEffectCompat essentially performs a NOP.
            return FLING_DEFAULT_ABSORB_VELOCITY;
        }
    }

    /**
     * Use to schedule a request layout via a runnable
     */
    private Runnable mDelayedLayout = new Runnable() {
        @Override
        public void run() {
            requestLayout();
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Cache off the measure spec
        mHeightMeasureSpec = heightMeasureSpec;
    }

    ;

    /**
     * Determine the Max X position. This is the farthest that the user can scroll the screen. Until the last adapter item has been
     * laid out it is impossible to calculate; once that has occurred this will perform the calculation, and if necessary force a
     * redraw and relayout of this view.
     *
     * @return true if the maxx position was just determined
     */
    private boolean determineMaxX() {
        // If the last view has been laid out, then we can determine the maximum x position
        //只有最后一个，也是最右边的item滑入屏幕的时候才计算mMaxScrollX
        if (isLastItemInAdapter(mRightVisibleViewAdapterIndex)) {
            View rightView = getRightmostChild();

            if (rightView != null) {
                int oldMaxX = mMaxScrollX;

                // Determine the maximum x position
                //注意，不包括getRenderWidth()
                mMaxScrollX = mCurrentScrollX + (rightView.getRight() - getPaddingLeft()) - getRenderWidth();

                // Handle the case where the views do not fill at least 1 screen
                if (mMaxScrollX < 0) {
                    mMaxScrollX = 0;
                }

                if (mMaxScrollX != oldMaxX) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Adds children views to the left and right of the current views until the screen is full
     */
    private void fillList(final int dx) {
        // Get the rightmost child and determine its right edge
        int edge = 0;
        View child = getRightmostChild();
        if (child != null) {
            edge = child.getRight();
        }

        // Add new children views to the right, until past the edge of the screen
        fillListRight(edge, dx);//如果最右边空了，就填充，直到最右边填满屏幕为止

        // Get the leftmost child and determine its left edge
        edge = 0;
        child = getLeftmostChild();
        if (child != null) {
            edge = child.getLeft();
        }

        // Add new children views to the left, until past the edge of the screen
        //如果最左边空了，就填充，直到最左边填满屏幕为止
        fillListLeft(edge, dx);
    }

    private void removeNonVisibleChildren(final int dx) {
        View child = getLeftmostChild();
        //移除左边的滑出屏幕的view，直到最左边的view还可见，就停止
        // Loop removing the leftmost child, until that child is on the screen
        while (child != null && child.getRight() + dx <= 0) {
            // The child is being completely removed so remove its width from the display offset and its divider if it has one.
            // To remove add the size of the child and its divider (if it has one) to the offset.
            // You need to add since its being removed from the left side, i.e. shifting the offset to the right.

            // After this, mDisplayOffset will be a small negative and next to zero value.
            mLeftVisibleViewDisplayOffset += isLastItemInAdapter(mLeftVisibleViewAdapterIndex) ? child.getMeasuredWidth() : mDividerWidth + child.getMeasuredWidth();
            Log.e("zzz", "move left mLeftVisibleViewDisplayOffset " + mLeftVisibleViewDisplayOffset);
            // Add the removed view to the cache
            recycleView(mLeftVisibleViewAdapterIndex, child);

            // Actually remove the view
            removeViewInLayout(child);

            // Keep track of the adapter index of the left most child
            mLeftVisibleViewAdapterIndex++;

            // Get the new leftmost child
            child = getLeftmostChild();
        }

        child = getRightmostChild();
        //移除右边的滑出屏幕的view，直到最右边的view还可见，就停止
        // Loop removing the rightmost child, until that child is on the screen
        while (child != null && child.getLeft() + dx >= getWidth()) {
            recycleView(mRightVisibleViewAdapterIndex, child);
            removeViewInLayout(child);
            mRightVisibleViewAdapterIndex--;
            child = getRightmostChild();
        }
    }

    private void fillListRight(int rightEdge, final int dx) {
        // Loop adding views to the right until the screen is filled
        while (rightEdge + dx + mDividerWidth < getWidth() && mRightVisibleViewAdapterIndex + 1 < mAdapter.getCount()) {
            mRightVisibleViewAdapterIndex++;

            // If mLeftViewAdapterIndex < 0 then this is the first time a view is being added, and left == right
            if (mLeftVisibleViewAdapterIndex < 0) {
                mLeftVisibleViewAdapterIndex = mRightVisibleViewAdapterIndex;
            }

            // Get the view from the adapter, utilizing a cached view if one is available
            //我们写的adapter的getView，第一个参数是position,第二个参数是convertView，第三个参数是ViewGroup
            View child = mAdapter.getView(mRightVisibleViewAdapterIndex, getRecycledView(mRightVisibleViewAdapterIndex), this);
            addAndMeasureChild(child, INSERT_AT_END_OF_LIST);

            // If first view, then no divider to the left of it, otherwise add the space for the divider width
            rightEdge += (mRightVisibleViewAdapterIndex == 0 ? 0 : mDividerWidth) + child.getMeasuredWidth();

            // Check if we are running low on data so we can tell listeners to go get more
            determineIfLowOnData();
        }
    }

    private void fillListLeft(int leftEdge, final int dx) {
        // Loop adding views to the left until the screen is filled
        while (leftEdge + dx - mDividerWidth > 0 && mLeftVisibleViewAdapterIndex >= 1) {
            mLeftVisibleViewAdapterIndex--;
            View child = mAdapter.getView(mLeftVisibleViewAdapterIndex, getRecycledView(mLeftVisibleViewAdapterIndex), this);
            addAndMeasureChild(child, INSERT_AT_START_OF_LIST);

            // If first view, then no divider to the left of it
            leftEdge -= mLeftVisibleViewAdapterIndex == 0 ? child.getMeasuredWidth() : mDividerWidth + child.getMeasuredWidth();

            // If on a clean edge then just remove the child, otherwise remove the divider as well
            mLeftVisibleViewDisplayOffset -= leftEdge + dx <= 0 ? child.getMeasuredWidth() : mDividerWidth + child.getMeasuredWidth();
            Log.e("zzz", "move right mLeftVisibleViewDisplayOffset " + mLeftVisibleViewDisplayOffset);
        }
    }

    /**
     * Loops through each child and positions them onto the screen
     */
    private void positionChildren(final int dx) {
        int childCount = getChildCount();
        //这个方法就是item的摆放逻辑了，从做摆放到右，用到mLeftVisibleViewDisplayOffset这个值
        //这里需要强调一下,layout的过程，可用的宽度只有屏幕的大小，这个坐标也是相对于屏幕的
        //而mCurrentScrollX则是逻辑上的
        //其实这个逻辑就是，将item整体移动dx具体，可能向左，可能向右，取决于dx的正负
        if (childCount > 0) {
            mLeftVisibleViewDisplayOffset += dx;
            int leftOffset = mLeftVisibleViewDisplayOffset;

            // Loop each child view
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                int left = leftOffset + getPaddingLeft();//不要在paddingLeft的位置渲染哦
                int top = getPaddingTop();
                int right = left + child.getMeasuredWidth();
                int bottom = top + child.getMeasuredHeight();

                // Layout the child
                child.layout(left, top, right, bottom);

                // Increment our offset by added child's size and divider width
                leftOffset += child.getMeasuredWidth() + mDividerWidth;
            }
        }
    }

    /**
     * Gets the current child that is leftmost on the screen.
     */
    private View getLeftmostChild() {
        return getChildAt(0);
    }

    /**
     * Gets the current child that is rightmost on the screen.
     */
    private View getRightmostChild() {
        return getChildAt(getChildCount() - 1);
    }

    /**
     * Finds a child view that is contained within this view, given the adapter index.
     *
     * @return View The child view, or or null if not found.
     */
    private View getChild(int adapterIndex) {
        if (adapterIndex >= mLeftVisibleViewAdapterIndex && adapterIndex <= mRightVisibleViewAdapterIndex) {
            return getChildAt(adapterIndex - mLeftVisibleViewAdapterIndex);
        }

        return null;
    }

    /**
     * Returns the index of the child that contains the coordinates given.
     * This is useful to determine which child has been touched.
     * This can be used for a call to {@link #getChildAt(int)}
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return The index of the child that contains the coordinates. If no child is found then returns -1
     */
    private int getChildIndex(final int x, final int y) {
        int childCount = getChildCount();

        for (int index = 0; index < childCount; index++) {
            getChildAt(index).getHitRect(mRect);
            if (mRect.contains(x, y)) {
                return index;
            }
        }

        return -1;
    }

    /**
     * Simple convenience method for determining if this index is the last index in the adapter
     */
    private boolean isLastItemInAdapter(int index) {
        return index == mAdapter.getCount() - 1;
    }

    /**
     * Gets the height in px this view will be rendered. (padding removed)
     */
    private int getRenderHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    /**
     * Gets the width in px this view will be rendered. (padding removed)
     */
    private int getRenderWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    /**
     * Scroll to the provided offset
     */
    public void scrollTo(int x) {
        mFlingTracker.startScroll(mNextScrollX, 0, x - mNextScrollX, 0);
        setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_FLING);
        requestLayout();
    }

    @Override
    public int getFirstVisiblePosition() {
        return mLeftVisibleViewAdapterIndex;
    }

    @Override
    public int getLastVisiblePosition() {
        return mRightVisibleViewAdapterIndex;
    }

    /**
     * Draws the overscroll edge glow effect on the left and right sides of the horizontal list
     */
    private void drawEdgeGlow(Canvas canvas) {
        if (mEdgeGlowLeft != null && !mEdgeGlowLeft.isFinished() && isEdgeGlowEnabled()) {
            // The Edge glow is meant to come from the top of the screen, so rotate it to draw on the left side.
            final int restoreCount = canvas.save();
            final int height = getHeight();

            canvas.rotate(-90, 0, 0);
            canvas.translate(-height + getPaddingBottom(), 0);

            mEdgeGlowLeft.setSize(getRenderHeight(), getRenderWidth());
            if (mEdgeGlowLeft.draw(canvas)) {
                invalidate();
            }

            canvas.restoreToCount(restoreCount);
        } else if (mEdgeGlowRight != null && !mEdgeGlowRight.isFinished() && isEdgeGlowEnabled()) {
            // The Edge glow is meant to come from the top of the screen, so rotate it to draw on the right side.
            final int restoreCount = canvas.save();
            final int width = getWidth();

            canvas.rotate(90, 0, 0);
            canvas.translate(getPaddingTop(), -width);
            mEdgeGlowRight.setSize(getRenderHeight(), getRenderWidth());
            if (mEdgeGlowRight.draw(canvas)) {
                invalidate();
            }

            canvas.restoreToCount(restoreCount);
        }
    }

    /**
     * Draws the dividers that go in between the horizontal list view items
     */
    private void drawDividers(Canvas canvas) {
        final int count = getChildCount();

        // Only modify the left and right in the loop, we set the top and bottom here since they are always the same
        final Rect bounds = mRect;
        mRect.top = getPaddingTop();
        mRect.bottom = mRect.top + getRenderHeight();

        // Draw the list dividers
        for (int i = 0; i < count; i++) {
            // Don't draw a divider to the right of the last item in the adapter
            if (!(i == count - 1 && isLastItemInAdapter(mRightVisibleViewAdapterIndex))) {
                View child = getChildAt(i);

                bounds.left = child.getRight();
                bounds.right = child.getRight() + mDividerWidth;

                // Clip at the left edge of the screen
                if (bounds.left < getPaddingLeft()) {
                    bounds.left = getPaddingLeft();
                }

                // Clip at the right edge of the screen
                if (bounds.right > getWidth() - getPaddingRight()) {
                    bounds.right = getWidth() - getPaddingRight();
                }

                // Draw a divider to the right of the child
                drawDivider(canvas, bounds);

                // If the first view, determine if a divider should be shown to the left of it.
                // A divider should be shown if the left side of this view does not fill to the left edge of the screen.
                if (i == 0 && child.getLeft() > getPaddingLeft()) {
                    bounds.left = getPaddingLeft();
                    bounds.right = child.getLeft();
                    drawDivider(canvas, bounds);
                }
            }
        }
    }

    /**
     * Draws a divider in the given bounds.
     *
     * @param canvas The canvas to draw to.
     * @param bounds The bounds of the divider.
     */
    private void drawDivider(Canvas canvas, Rect bounds) {
        if (mDividerDrawable != null) {
            mDividerDrawable.setBounds(bounds);
            mDividerDrawable.draw(canvas);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDividers(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawEdgeGlow(canvas);
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
        // Don't dispatch setPressed to our children. We call setPressed on ourselves to
        // get the selector in the right state, but we don't want to press each child.
    }

    /**
     * 检测到fling事件，启动scroller，并且触发onLayout，就会触发整个fling操作
     */
    protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //            fling(startX,startY,velocityX,velocityY,minX, int maxX, int minY, int maxY)
        mFlingTracker.fling(mNextScrollX, 0, (int) -velocityX, 0, 0, mMaxScrollX, 0, 0);
        setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_FLING);
        requestLayout();
        return true;
    }

    protected boolean onDown(MotionEvent e) {
        // If the user just caught a fling, then disable all touch actions until they release their finger
        mBlockTouchAction = !mFlingTracker.isFinished();

        // Allow a finger down event to catch a fling
        //滑动过程中，只要触摸就停止滑动
        mFlingTracker.forceFinished(true);
        setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE);

        unpressTouchedChild();

        if (!mBlockTouchAction) {
            // Find the child that was pressed
            final int index = getChildIndex((int) e.getX(), (int) e.getY());
            if (index >= 0) {
                // Save off view being touched so it can later be released
                mViewBeingTouched = getChildAt(index);

                if (mViewBeingTouched != null) {
                    // Set the view as pressed
                    mViewBeingTouched.setPressed(true);
                    refreshDrawableState();
                }
            }
        }

        return true;
    }

    /**
     * If a view is currently pressed then unpress it
     */
    private void unpressTouchedChild() {
        if (mViewBeingTouched != null) {
            // Set the view as not pressed
            mViewBeingTouched.setPressed(false);
            refreshDrawableState();

            // Null out the view so we don't leak it
            mViewBeingTouched = null;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return HorizontalListView.this.onDown(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return HorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Lock the user into interacting just with this view
            requestParentListViewToNotInterceptTouchEvents(true);

            setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_TOUCH_SCROLL);
            unpressTouchedChild();
            //触摸滚动的时候，计算mNextScrollX，然后触发onLayout进行重绘，触摸滑动停止，重新布局停止
            mNextScrollX += (int) distanceX;// key operation.
            updateOverscrollAnimation(Math.round(distanceX));
            requestLayout();

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            unpressTouchedChild();
            OnItemClickListener onItemClickListener = getOnItemClickListener();

            final int index = getChildIndex((int) e.getX(), (int) e.getY());

            // If the tap is inside one of the child views, and we are not blocking touches
            if (index >= 0 && !mBlockTouchAction) {
                View child = getChildAt(index);
                int adapterIndex = mLeftVisibleViewAdapterIndex + index;

                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(HorizontalListView.this, child, adapterIndex, mAdapter.getItemId(adapterIndex));
                    return true;
                }
            }

            if (mOnClickListener != null && !mBlockTouchAction) {
                mOnClickListener.onClick(HorizontalListView.this);
            }

            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            unpressTouchedChild();

            final int index = getChildIndex((int) e.getX(), (int) e.getY());
            if (index >= 0 && !mBlockTouchAction) {
                View child = getChildAt(index);
                OnItemLongClickListener onItemLongClickListener = getOnItemLongClickListener();
                if (onItemLongClickListener != null) {
                    int adapterIndex = mLeftVisibleViewAdapterIndex + index;
                    boolean handled = onItemLongClickListener.onItemLongClick(HorizontalListView.this, child, adapterIndex, mAdapter
                            .getItemId(adapterIndex));

                    if (handled) {
                        // BZZZTT!!1!
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    }
                }
            }
        }
    }

    ;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Detect when the user lifts their finger off the screen after a touch
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // If not flinging then we are idle now. The user just finished a finger scroll.
            if (mFlingTracker == null || mFlingTracker.isFinished()) {
                setCurrentScrollState(OnScrollStateChangedListener.ScrollState.SCROLL_STATE_IDLE);
            }

            // Allow the user to interact with parent views
            requestParentListViewToNotInterceptTouchEvents(false);

            releaseEdgeGlow();
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            unpressTouchedChild();
            releaseEdgeGlow();

            // Allow the user to interact with parent views
            requestParentListViewToNotInterceptTouchEvents(false);
        }

        return super.onTouchEvent(event);
    }

    /**
     * Release the EdgeGlow so it animates
     */
    private void releaseEdgeGlow() {
        if (mEdgeGlowLeft != null) {
            mEdgeGlowLeft.onRelease();
        }

        if (mEdgeGlowRight != null) {
            mEdgeGlowRight.onRelease();
        }
    }

    /**
     * Sets a listener to be called when the HorizontalListView has been scrolled to a point where it is
     * running low on data. An example use case is wanting to auto download more data when the user
     * has scrolled to the point where only 10 items are left to be rendered off the right of the
     * screen. To get called back at that point just register with this function with a
     * numberOfItemsLeftConsideredLow value of 10. <br>
     * <br>
     * This will only be called once to notify that the HorizontalListView is running low on data.
     * Calling notifyDataSetChanged on the adapter will allow this to be called again once low on data.
     *
     * @param listener                       The listener to be notified when the number of array adapters items left to
     *                                       be shown is running low.
     * @param numberOfItemsLeftConsideredLow The number of array adapter items that have not yet
     *                                       been displayed that is considered too low.
     */
    public void setRunningOutOfDataListener(RunningOutOfDataListener listener, int numberOfItemsLeftConsideredLow) {
        mRunningOutOfDataListener = listener;
        mRunningOutOfDataThreshold = numberOfItemsLeftConsideredLow;
    }

    /**
     * This listener is used to allow notification when the HorizontalListView is running low on data to display.
     */
    public static interface RunningOutOfDataListener {
        /**
         * Called when the HorizontalListView is running out of data and has reached at least the provided threshold.
         */
        void onRunningOutOfData();
    }

    /**
     * Determines if we are low on data and if so will call to notify the listener, if there is one,
     * that we are running low on data.
     */
    private void determineIfLowOnData() {
        // Check if the threshold has been reached and a listener is registered
        if (mRunningOutOfDataListener != null && mAdapter != null &&
                mAdapter.getCount() - (mRightVisibleViewAdapterIndex + 1) < mRunningOutOfDataThreshold) {

            // Prevent notification more than once
            if (!mHasNotifiedRunningLowOnData) {
                mHasNotifiedRunningLowOnData = true;
                mRunningOutOfDataListener.onRunningOutOfData();
            }
        }
    }

    /**
     * Register a callback to be invoked when the HorizontalListView has been clicked.
     *
     * @param listener The callback that will be invoked.
     */
    @Override
    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when the view scroll state has changed.
     */
    public interface OnScrollStateChangedListener {
        public enum ScrollState {
            /**
             * The view is not scrolling. Note navigating the list using the trackball counts as being
             * in the idle state since these transitions are not animated.
             */
            SCROLL_STATE_IDLE,

            /**
             * The user is scrolling using touch, and their finger is still on the screen
             */
            SCROLL_STATE_TOUCH_SCROLL,

            /**
             * The user had previously been scrolling using touch and had performed a fling. The
             * animation is now coasting to a stop
             */
            SCROLL_STATE_FLING
        }

        /**
         * Callback method to be invoked when the scroll state changes.
         *
         * @param scrollState The current scroll state.
         */
        public void onScrollStateChanged(ScrollState scrollState);
    }

    /**
     * Sets a listener to be invoked when the scroll state has changed.
     *
     * @param listener The listener to be invoked.
     */
    public void setOnScrollStateChangedListener(OnScrollStateChangedListener listener) {
        mOnScrollStateChangedListener = listener;
    }

    /**
     * Call to set the new scroll state.
     * If it has changed and a listener is registered then it will be notified.
     */
    private void setCurrentScrollState(OnScrollStateChangedListener.ScrollState newScrollState) {
        // If the state actually changed then notify listener if there is one
        if (mCurrentScrollState != newScrollState && mOnScrollStateChangedListener != null) {
            mOnScrollStateChangedListener.onScrollStateChanged(newScrollState);
        }

        mCurrentScrollState = newScrollState;
    }

    /**
     * Updates the over scroll animation based on the scrolled offset.
     *
     * @param scrolledOffset The scroll offset
     */
    private void updateOverscrollAnimation(final int scrolledOffset) {
        if (mEdgeGlowLeft == null || mEdgeGlowRight == null) return;

        // Calculate where the next scroll position would be
        int nextScrollPosition = mCurrentScrollX + scrolledOffset;

        // If not currently in a fling (Don't want to allow fling offset updates to cause over scroll animation)
        if (mFlingTracker == null || mFlingTracker.isFinished()) {
            // If currently scrolled off the left side of the list and the adapter is not empty
            if (nextScrollPosition < 0) {

                // Calculate the amount we have scrolled since last frame
                int overscroll = Math.abs(scrolledOffset);

                // Tell the edge glow to redraw itself at the new offset
                mEdgeGlowLeft.onPull((float) overscroll / getRenderWidth());

                // Cancel animating right glow
                if (!mEdgeGlowRight.isFinished()) {
                    mEdgeGlowRight.onRelease();
                }
            } else if (nextScrollPosition > mMaxScrollX) {
                // Scrolled off the right of the list

                // Calculate the amount we have scrolled since last frame
                int overscroll = Math.abs(scrolledOffset);

                // Tell the edge glow to redraw itself at the new offset
                mEdgeGlowRight.onPull((float) overscroll / getRenderWidth());

                // Cancel animating left glow
                if (!mEdgeGlowLeft.isFinished()) {
                    mEdgeGlowLeft.onRelease();
                }
            }
        }
    }

    /**
     * Checks if the edge glow should be used enabled.
     * The glow is not enabled unless there are more views than can fit on the screen at one time.
     */
    private boolean isEdgeGlowEnabled() {
        if (mAdapter == null || mAdapter.isEmpty()) return false;

        // If the maxx is more then zero then the user can scroll, so the edge effects should be shown
        return mMaxScrollX > 0;
    }

    @TargetApi(11)
    /** Wrapper class to protect access to API version 11 and above features */
    private static final class HoneycombPlus {
        static {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                throw new RuntimeException("Should not get to HoneycombPlus class unless sdk is >= 11!");
            }
        }

        /**
         * Sets the friction for the provided scroller
         */
        public static void setFriction(Scroller scroller, float friction) {
            if (scroller != null) {
                scroller.setFriction(friction);
            }
        }
    }

    @TargetApi(14)
    /** Wrapper class to protect access to API version 14 and above features */
    private static final class IceCreamSandwichPlus {
        static {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                throw new RuntimeException("Should not get to IceCreamSandwichPlus class unless sdk is >= 14!");
            }
        }

        /**
         * Gets the velocity for the provided scroller
         */
        public static float getCurrVelocity(Scroller scroller) {
            return scroller.getCurrVelocity();
        }
    }
}
