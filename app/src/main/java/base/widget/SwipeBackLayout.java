package base.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;

import base.utils.LoggerUtil;


/**
 * @Description: 右滑动关闭界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2018/1/27
 * @Throws:
 */
public class SwipeBackLayout extends LinearLayout {
    //
    private Context mContext;
    /**
     * 日志
     */
    private LoggerUtil logger;

    /**
     * 内容view
     */
    private View mContentView;
    /**
     * 屏幕宽度
     */
    private int mScreensWidth;

    /**
     * 判断view是点击还是移动的距离
     */
    private int mTouchSlop;

    //拦截的x轴位置
    private float mLastXIntercept = 0;
    private float mLastYIntercept = 0;
    private ViewDragHelper mViewDragHelper;

    /**
     * 记录手势速度
     */
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;

    /**
     * 阴影画笔
     */
    private Paint mFadePaint;

    /**
     * 是否绘画阴影
     */
    private boolean isPaintFade = true;
    /**
     * 是否允许拖动
     */
    private boolean isAllowDrag = true;

    /**
     * 记录当前内容view的x轴位置，方便设置contentView的位置
     */
    private int mContentViewCurX = 0;
    //
    private SwipeBackLayoutListener mSwipeBackLayoutListener;

    public SwipeBackLayout(Context context) {
        super(context);
        init(context);
    }

    public SwipeBackLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeBackLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SwipeBackLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        logger = LoggerUtil.getZhangLogger(context);


        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mScreensWidth = display.getWidth();

        mContentViewCurX = mScreensWidth;
        //
        mFadePaint = new Paint();
        mFadePaint.setAntiAlias(true);
        mFadePaint.setColor(Color.argb(255, 0, 0, 0));

        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());

        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        //加载完成后回调
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //执行打开页面动画
                mViewDragHelper.smoothSlideViewTo(mContentView, 0, 0);
                ViewCompat.postInvalidateOnAnimation(SwipeBackLayout.this);

            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            mContentView = getChildAt(0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mContentView == null) return;
        mContentView.layout(mContentViewCurX, 0, mContentViewCurX + mContentView.getWidth(), mContentView.getHeight());
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isAllowDrag) return super.onInterceptTouchEvent(event);
        boolean intercepted = false;
        try {

            float curX = event.getX();
            float curY = event.getY();

            int actionId = event.getAction();
            switch (actionId) {

                case MotionEvent.ACTION_DOWN:

                    mLastXIntercept = curX;
                    mLastYIntercept = curY;

                    obtainVelocityTracker(event);
                    mViewDragHelper.processTouchEvent(event);
                    break;

                case MotionEvent.ACTION_MOVE:

                    int[] location = new int[2];
                    mContentView.getLocationOnScreen(location);
                    int mDragViewLeftX = location[0];
                    int mDragViewRightX = mDragViewLeftX + mContentView.getWidth();

                    //按下焦点在手动view里面
                    if ((mDragViewLeftX <= event.getRawX() && event.getRawX() <= mDragViewRightX)) {

                        int deltaX = (int) (mLastXIntercept - curX);
                        int deltaY = (int) (mLastYIntercept - curY);
                        //左右移动事件
                        if (Math.abs(deltaX) > mTouchSlop && Math.abs(deltaY) < mTouchSlop) {
                            intercepted = true;
                        }
                    }
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            logger.e(e.getMessage());
        }

        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isAllowDrag) return super.onTouchEvent(event);
        try {
            obtainVelocityTracker(event);
            mViewDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
        return true;
    }

    /**
     * @param event
     */
    private void obtainVelocityTracker(MotionEvent event) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(event);

    }

    /**
     * 释放
     */
    private void releaseVelocityTracker() {

        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;

        }

    }

    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return mContentView == child;
        }

        //处理水平和垂直滑动，返回top和left，如果为0，不能滑动，dy、dx表示相对上一次的增量
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0; //不允许垂直滑动
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (left < 0) {
                //不允许左越界
                left = 0;
            } else if (left > getWidth()) {
                left = getWidth();
            }
            return left;
        }

        /**
         * 当child位置改变时执行
         *
         * @param changedView 位置改变的子View
         * @param left        child最新的left位置
         * @param top         child最新的top位置
         * @param dx          相较于上一次水平移动的距离
         * @param dy          相较于上一次垂直移动的距离
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (changedView == mContentView) {
                drawMask();
                mContentViewCurX = left;
                //因为view的位置发生了改变，需要重新布局，如果不进行此操作，存在刷新时，view的位置被还原的问题.之前老是因为view中动态添加数据后，导致还原view位置的问题
                requestLayout();
            }
        }

        //拖动结束后调用,类似于ACTION_UP事件

        /**
         * 手指抬起的时候执行该方法
         *
         * @param releasedChild 当前抬起的View
         * @param xvel          x方向移动的速度：正值：向右移动  负值：向左移动
         * @param yvel          y方向移动的速度：正值：向下移动  负值：向上移动
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (releasedChild == mContentView) {

                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

                int xVelocity = (int) velocityTracker.getXVelocity();


                if (Math.abs(xVelocity) > mMinimumVelocity && xvel > 0) {

                    mViewDragHelper.smoothSlideViewTo(releasedChild, getWidth(), 0);
                    ViewCompat.postInvalidateOnAnimation(SwipeBackLayout.this);

                } else {

                    if (releasedChild.getLeft() < getWidth() / 2) {
                        //在左半边
                        mViewDragHelper.smoothSlideViewTo(releasedChild, 0, 0);
                        ViewCompat.postInvalidateOnAnimation(SwipeBackLayout.this);
                    } else {
                        //在右半边
                        mViewDragHelper.smoothSlideViewTo(releasedChild, getWidth(), 0);
                        ViewCompat.postInvalidateOnAnimation(SwipeBackLayout.this);
                    }

                }
                releaseVelocityTracker();
                mViewDragHelper.cancel();

            }
        }

        //如果ViewGroup的子控件会消耗点击事件，例如按钮，在触摸屏幕的时候就会先走onInterceptTouchEvent方法，判断是否可以捕获，而在判断的过程中会去判断另外两个回调的方法：getViewHorizontalDragRange和getViewVerticalDragRange，只有这两个方法返回大于0的值才能正常的捕获。

        @Override
        public int getViewHorizontalDragRange(View child) {
            return child.getMeasuredWidth();//只要返回大于0的值就行
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return child.getMeasuredHeight();//只要返回大于0的值就行
        }
    }

    /**
     * 绘画阴影
     */
    private void drawMask() {

        if (isPaintFade) {
            float percent = mContentView.getLeft() * 1.0f / getWidth();
            int alpha = 200 - (int) (200 * percent);
            mFadePaint.setColor(Color.argb(Math.max(alpha, 0), 0, 0, 0));

            invalidate();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (isPaintFade && mContentView.getLeft() > 0) {
            canvas.drawRect(0, 0, mContentView.getLeft(), getHeight(), mFadePaint);
        }
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);


            invalidate();
        } else {

            if (mContentView.getLeft() >= getWidth()) {
                if (mSwipeBackLayoutListener != null) {
                    mSwipeBackLayoutListener.finishView();
                }
            }

        }

    }

    public void setShadowEnable(boolean paintFade) {
        isPaintFade = paintFade;
    }

    /**
     * 关闭
     */
    public void finish() {
        mViewDragHelper.smoothSlideViewTo(mContentView, getWidth(), 0);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /////////////////////////////////////////////////////////////////////
    public void setAllowDrag(boolean allowDrag) {
        isAllowDrag = allowDrag;
    }


    public void setSwipeBackLayoutListener(SwipeBackLayoutListener mSwipeBackLayoutListener) {
        this.mSwipeBackLayoutListener = mSwipeBackLayoutListener;
    }

    public interface SwipeBackLayoutListener {
        void finishView();
    }
}
