package com.zlm.hp.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.zlm.hp.libs.utils.ColorUtil;
import com.zlm.hp.libs.utils.LoggerUtil;
import com.zlm.hp.ui.R;

/**
 * @Description: SlidingMenu布局。因为该界面的view是一层一层的，所以这里使用FrameLayout布局
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/28 0:16
 * @Throws:
 */
public class SlidingMenuLayout extends FrameLayout {

    /**
     * 菜单布局
     */
    private FrameLayout mMenuFrameLayout;

    /**
     * 主界面布局
     */
    private LinearLayout mainLinearLayoutContainer;

    /**
     * 屏幕宽度
     */
    private int mScreensWidth;

    /**
     * 判断view是点击还是移动的距离
     */
    private int mTouchSlop;

    /**
     * 日志
     */
    private LoggerUtil logger;

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


    //
    private Context mContext;

    /**
     * 当前fragment
     */
    private Fragment mCurrentFragment;
    private FragmentManager mFragmentManager;
    /////////////////////////////////////////////

    /**
     * 阴影画笔
     */
    private Paint mFadePaint;

    /**
     * 是否绘画阴影
     */
    private boolean isPaintFade = true;
    /**
     * 记录menuX轴的位置，用于设置menuview的位置
     */
    private int mMenuCurLeftX = 0;

    public SlidingMenuLayout(@NonNull Context context) {
        super(context);
    }

    public SlidingMenuLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlidingMenuLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SlidingMenuLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        logger = LoggerUtil.getZhangLogger(context);
        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        //
        mFadePaint = new Paint();
        mFadePaint.setAntiAlias(true);
        mFadePaint.setColor(Color.argb(255, 0, 0, 0));

    }

    /**
     * 初始化菜单布局
     */
    public void initView(LinearLayout mainLinearLayoutContainer) {
        this.mainLinearLayoutContainer = mainLinearLayoutContainer;

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mScreensWidth = display.getWidth();

        //
        mMenuFrameLayout = new FrameLayout(mContext);
        mMenuFrameLayout.setBackgroundColor(Color.BLACK);
        mMenuFrameLayout.setId(R.id.menu_container);

        //
        LayoutParams menuLayout = new LayoutParams(mScreensWidth, LayoutParams.MATCH_PARENT);
        menuLayout.leftMargin = mScreensWidth;
        addView(mMenuFrameLayout, menuLayout);

        //
        mMenuCurLeftX = menuLayout.leftMargin;
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        //置顶部
        this.bringChildToFront(mMenuFrameLayout);

        //设置防止事件穿透明
        mMenuFrameLayout.setClickable(true);
        this.mainLinearLayoutContainer.setClickable(true);

    }

    /**
     * 显示菜单view
     *
     * @param fragmentManager
     * @param fragment
     */
    public void showMenuView(FragmentManager fragmentManager, Fragment fragment) {

        mFragmentManager = fragmentManager;
        mCurrentFragment = fragment;
        mFragmentManager.beginTransaction().add(mMenuFrameLayout.getId(), mCurrentFragment).commit();

        //

        if (mViewDragHelper.smoothSlideViewTo(mMenuFrameLayout, 0, 0))
            ViewCompat.postInvalidateOnAnimation(this);

    }

    /**
     * 隐藏菜单界面
     */
    public void hideMenuView(FragmentManager supportFragmentManager) {

        if (mViewDragHelper.smoothSlideViewTo(mMenuFrameLayout, getWidth(), 0))
            ViewCompat.postInvalidateOnAnimation(this);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mMenuFrameLayout != null) {
            mMenuFrameLayout.layout(mMenuCurLeftX, 0, mMenuCurLeftX + mMenuFrameLayout.getWidth(), mMenuFrameLayout.getHeight());
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
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
                    mMenuFrameLayout.getLocationOnScreen(location);
                    int mDragViewLeftX = location[0];
                    int mDragViewRightX = mDragViewLeftX + mMenuFrameLayout.getWidth();

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

    /**
     * @param event
     */
    private void obtainVelocityTracker(MotionEvent event) {

        if (mVelocityTracker == null) {

            mVelocityTracker = VelocityTracker.obtain();

        }

        mVelocityTracker.addMovement(event);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            obtainVelocityTracker(event);
            mViewDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
        return true;
    }

    @Override
    public void computeScroll() {

        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        } else {

            if (mMenuCurLeftX >= getWidth()) {

                if (mCurrentFragment != null) {

                    if (mFragmentManager != null) {

                        //logger.e("回收currentFragment");
                        FragmentTransaction transaction = mFragmentManager.beginTransaction();
                        transaction.remove(mCurrentFragment);
                        transaction.commit();
                    }

                    mCurrentFragment = null;
                }


            }

        }

    }

    public boolean isMenuViewShow() {
        return mMenuFrameLayout.getLeft() < getWidth() / 2;
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

    /**
     * 添加状态栏视图
     *
     * @param statusBarParentView
     */
    public void addStatusBarView(ViewGroup statusBarParentView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View statusBarView = new View(mContext.getApplicationContext());
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(mContext.getApplicationContext()));
            statusBarView.setBackgroundColor(getStatusColor());
            statusBarParentView.addView(statusBarView, 0, lp);

        }
    }

    /**
     * @Description: 获取状态栏高度
     * @Param: context
     * @Return:
     * @Author: zhangliangming
     * @Date: 2017/7/15 19:30
     * @Throws:
     */
    private int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取状态栏颜色
     *
     * @return
     */
    private int getStatusColor() {

        return ColorUtil.parserColor(ContextCompat.getColor(mContext.getApplicationContext(), R.color.defColor));

    }

    /**
     * 绘画阴影
     */
    private void drawFade() {
        if (isPaintFade) {

            float percent = mMenuFrameLayout.getLeft() * 1.0f / getWidth();
            int alpha = 200 - (int) (200 * percent);
            mFadePaint.setColor(Color.argb(Math.max(alpha, 0), 0, 0, 0));

        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (isPaintFade && mMenuFrameLayout.getLeft() < getWidth())
            canvas.drawRect(0, 0, mMenuFrameLayout.getLeft(), getHeight(), mFadePaint);
    }

    /////////////////////////////////

    public interface FragmentListener {
        void openFragment(Fragment fragment);

        void closeFragment();
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {

        //此方法是自动生成的，何时开始检测触摸事件
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            //如果当前触摸的child是mMainView时开始检测
            return mMenuFrameLayout == child;
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
            if (changedView == mMenuFrameLayout) {

                //1.计算view移动的百分比0~1
                float percent = left * 1f / getWidth();

                //缩放
                mainLinearLayoutContainer.setScaleX( 0.9f + 0.1f * percent);
                mainLinearLayoutContainer.setScaleY(0.9f + 0.1f * percent);

                //
                mMenuCurLeftX = left;
                drawFade();
                //因为view的位置发生了改变，需要重新布局，如果不进行此操作，存在刷新时，view的位置被还原的问题.之前老是因为view中动态添加数据后，导致还原view位置的问题
                invalidate();
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
            if (releasedChild == mMenuFrameLayout) {

                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

                int xVelocity = (int) velocityTracker.getXVelocity();


                if (Math.abs(xVelocity) > mMinimumVelocity && xvel > 0) {

                    if (mViewDragHelper.smoothSlideViewTo(releasedChild, getWidth(), 0))
                        ViewCompat.postInvalidateOnAnimation(SlidingMenuLayout.this);


                } else {
                    if (releasedChild.getLeft() < getWidth() / 2) {

                        //在左半边
                        if (mViewDragHelper.smoothSlideViewTo(releasedChild, 0, 0))
                            ViewCompat.postInvalidateOnAnimation(SlidingMenuLayout.this);
                    } else {
                        //在右半边

                        if (mViewDragHelper.smoothSlideViewTo(releasedChild, getWidth(), 0))
                            ViewCompat.postInvalidateOnAnimation(SlidingMenuLayout.this);
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

    ///////////////////////////

    public void setPaintFade(boolean paintFade) {
        isPaintFade = paintFade;
    }
}
