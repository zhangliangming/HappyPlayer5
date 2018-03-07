package base.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 旋转view，先确定旋转view的旋转中心为Q（width/2，height*1.5），然后根据第一次触摸时，该触摸点在屏幕上面的AXY(ax,ay)坐标，根据A点和Q点，计算出该次的夹角A。滑动时，同理计算滑动时的BXY(bx,by)坐标，计算出B点
 * 和Q点的夹角B，通过B - A，可以得出view的旋转度数。计算夹角时，可以使用tan来计算，如果计算出来的夹角为负数时，需要+180来得到真正的夹角。注意，需要activity开启硬件加速,动画才流畅。
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2018/02/12
 * @Throws:
 */

public class RotateLayout extends LinearLayout {

    /**
     * 全部
     */
    public static final int ALL = -1;

    /**
     * 左到右
     */
    public static final int LEFT_TO_RIGHT = 0;

    /**
     * 右到左
     */
    public static final int RIGHT_TO_LEFT = 1;

    /**
     * 状态打开
     */
    private final int OPEN = 0;
    /**
     * 状态关闭
     */
    private final int CLOSE = 1;
    /**
     * 状态移动
     */
    private final int MOVE = 2;
    /**
     * 界面状态
     */
    private int mDragStatus = CLOSE;

    /**
     * 拖动类型
     */
    private int mDragType = ALL;

    /**
     * 判断view是点击还是移动的距离
     */
    private int mTouchSlop;

    /**
     * 拦截的X轴和Y最后的位置
     */
    private float mLastInterceptX = 0, mLastInterceptY = 0;

    /**
     * 记录手势速度
     */
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;

    /**
     * 动画时间
     */
    private int mDuration = 300;
    /**
     * xy轴移动动画
     */
    private ValueAnimator mValueAnimator;
    /**
     * 旋转界面View
     */
    private LinearLayout mRotateLayout;

    /**
     * 关闭窗口的判断值
     */
    private float mCloseFlagDegree = 40;
    /**
     * 窗口的最大旋转度数
     */
    private float mClosedDegree = 85;
    /**
     * 用于判断旋转的最小角度，避免开启硬件加速后带来的界面布局闪烁问题。暂时这样修复
     */
    private float mMinFlagDegree = 0.2f;

    /**
     * 当前角度
     */
    private float mCurDegree = mClosedDegree;
    /**
     * 旋转角度的旋转中心
     */
    private float mPivotX = 0, mPivotY = 0;

    /**
     *
     */
    private LayoutInflater mLayoutInflater;
    /**
     * LinearLayout布局
     */
    public static final int CONTENTVIEWTYPE_LINEARLAYOUT = 0;
    /**
     * RelativeLayout布局
     */
    public static final int CONTENTVIEWTYPE_RELATIVELAYOUT = 1;

    /**
     * 不拦截水平视图
     */
    private List<View> mIgnoreHorizontalViews;

    /**
     * 不处理视图，该集合中的view所在的区域，将不做任何操作
     */
    private List<View> mIgnoreViews;

    private RotateLayoutListener mRotateLayoutListener;

    public RotateLayout(Context context) {
        super(context);
        init(context);
    }

    public RotateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        //获取旋转中心的坐标值
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int mScreensWidth = display.getWidth();
        int mScreensHeigh = display.getHeight();
        mPivotX = mScreensWidth * 0.5f;
        mPivotY = 1.5f * mScreensHeigh;
        //
        setOrientation(LinearLayout.VERTICAL);
        setBackgroundColor(Color.TRANSPARENT);

        //
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        // 初始化手势速度监听
        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        // 加载完成后回调
        getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);

                        open();

                    }
                });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0 && getChildCount() < 2) {
            mRotateLayout = (LinearLayout) getChildAt(0);
        } else {
            mRotateLayout = new LinearLayout(getContext());
            mRotateLayout.setOrientation(LinearLayout.VERTICAL);
            mRotateLayout.setBackgroundColor(Color.WHITE);
            removeAllViews();
            addView(mRotateLayout, LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mRotateLayout != null) {
            invalidateLayout(mCurDegree);
        }
    }

    /**
     * 刷新界面的角度
     *
     * @param degree
     */
    private void invalidateLayout(float degree) {
        mCurDegree = degree;
        mRotateLayout.setPivotX(mPivotX);
        mRotateLayout.setPivotY(mPivotY);
        if (Math.abs(degree) < mMinFlagDegree) {
            mRotateLayout.setRotation(0);
        } else {
            mRotateLayout.setRotation(degree);
        }
        invalidate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mRotateLayout == null)
            super.onInterceptTouchEvent(event);

        boolean intercepted = false;
        float curX = event.getX();
        float curY = event.getY();

        int actionId = event.getAction();
        switch (actionId) {

            case MotionEvent.ACTION_DOWN:
                mLastInterceptX = curX;
                mLastInterceptY = curY;
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (mLastInterceptX - curX);
                int deltaY = (int) (mLastInterceptY - curY);

                //左右移动
                if (Math.abs(deltaX) > mTouchSlop
                        && Math.abs(deltaY) < mTouchSlop
                        && !isInIgnoreHorizontalView(event) && !isInIgnoreView(event)) {

                    intercepted = true;
                }
                break;

            default:
                break;
        }

        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mRotateLayout == null)
            super.onTouchEvent(event);

        obtainVelocityTracker(event);

        float curX = event.getX();
        float curY = event.getY();

        int actionId = event.getAction();
        switch (actionId) {

            case MotionEvent.ACTION_DOWN:
                mLastInterceptX = curX;
                mLastInterceptY = curY;
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (mLastInterceptX - curX);
                int deltaY = (int) (mLastInterceptY - curY);

                if (mDragStatus == MOVE || (Math.abs(deltaX) > mTouchSlop
                        && Math.abs(deltaY) < mTouchSlop
                        && !isInIgnoreHorizontalView(event) && !isInIgnoreView(event))) {
                    // 左右移动事件
                    if (mDragStatus == MOVE || (mDragType == ALL || (deltaX < 0 && mDragType == LEFT_TO_RIGHT) || (deltaX > 0 && mDragType == RIGHT_TO_LEFT))) {

                        //根据当前的tan值计算上一次的角度
                        float lastW = mPivotX - mLastInterceptX;
                        float lastH = mPivotY - mLastInterceptY;
                        float lastDegree = (float) Math.toDegrees(Math.atan(lastH / lastW));
                        if (lastDegree < 0) {
                            lastDegree += 180;
                        }

                        //根据当前的tan值计算当前的角度
                        float curW = mPivotX - curX;
                        float curH = mPivotY - curY;
                        float curDegree = (float) Math.toDegrees(Math.atan(curH / curW));
                        if (curDegree < 0) {
                            curDegree += 180;
                        }

                        float degree = curDegree - lastDegree;
                        invalidateLayout(degree);

                        setDragStatus(MOVE);
                    }
                }
                break;

            default:

                // 处理up，cancel事件
                // 处理手势速度监听事件
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int xVelocity = (int) velocityTracker.getXVelocity();

                int oldDragStatus = mDragStatus;
                setDragStatus(OPEN);

                if (((Math.abs(xVelocity) > mMinimumVelocity && xVelocity > 0) || (mCurDegree > mCloseFlagDegree)) && (mDragType == LEFT_TO_RIGHT || mDragType == ALL) && oldDragStatus == MOVE) {
                    // 从左往右滑动
                    setDragStatus(CLOSE);

                    //执行旋转动画
                    rotateValueAnimator(mCurDegree, mClosedDegree);

                } else if (((Math.abs(xVelocity) > mMinimumVelocity && xVelocity < 0) || (mCurDegree < -mCloseFlagDegree)) && (mDragType == RIGHT_TO_LEFT || mDragType == ALL) && oldDragStatus == MOVE) {
                    // 从右往左滑动
                    setDragStatus(CLOSE);

                    //执行旋转动画
                    rotateValueAnimator(mCurDegree, -mClosedDegree);

                } else {

                    //执行还原动画
                    rotateValueAnimator(mCurDegree, 0);
                }

                //
                releaseVelocityTracker();
                break;
        }

        return true;
    }

    /**
     * 打开界面
     */
    public void open() {
        if (mDragStatus == OPEN) {
            return;
        }
        setDragStatus(OPEN);
        if (mRotateLayout != null) {
            // 执行打开动画
            rotateValueAnimator(mCurDegree, 0);
        }
    }

    /**
     * 关闭界面
     */
    public void closeView() {
        if (mDragStatus == CLOSE) {
            return;
        }
        setDragStatus(CLOSE);
        if (mRotateLayout != null) {
            // 执行关闭动画
            if (mDragType == LEFT_TO_RIGHT
                    || mDragType == ALL) {

                // 执行打开动画
                rotateValueAnimator(mCurDegree, mClosedDegree);

            } else {

                //执行旋转动画
                rotateValueAnimator(mCurDegree, -mClosedDegree);

            }
        }
    }

    /**
     * 旋转动画
     *
     * @param fromDegree
     * @param toDegree
     */
    private void rotateValueAnimator(float fromDegree, float toDegree) {
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
            mValueAnimator = null;
        }
        mValueAnimator = ValueAnimator.ofFloat(fromDegree, toDegree);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Number number = (Number) animation.getAnimatedValue();
                invalidateLayout(number.floatValue());
            }
        });
        mValueAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                if (mRotateLayoutListener != null && mDragStatus == CLOSE) {
                    mRotateLayoutListener.close();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setDuration(mDuration);
        mValueAnimator.start();
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


    /**
     * 设置拖动的状态
     *
     * @param dragStatus
     */
    private void setDragStatus(int dragStatus) {
        this.mDragStatus = dragStatus;
    }

    /**
     * 是否在水平不处理视图中
     *
     * @param event
     * @return
     */
    private boolean isInIgnoreHorizontalView(MotionEvent event) {
        return isInView(mIgnoreHorizontalViews, event);
    }

    /**
     * 是否在不处理视图中
     *
     * @param event
     * @return
     */
    private boolean isInIgnoreView(MotionEvent event) {
        return isInView(mIgnoreViews, event);
    }

    /**
     * 是否在view里面
     *
     * @param views
     * @param event
     * @return
     */
    private boolean isInView(List<View> views, MotionEvent event) {
        if (views == null || views.size() == 0)
            return false;
        for (int i = 0; i < views.size(); i++) {
            View view = views.get(i);
            Rect rect = new Rect(view.getLeft(), view.getTop(),
                    view.getRight(), view.getBottom());
            if (rect.contains((int) event.getX(), (int) event.getY())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加不拦截水平view
     *
     * @param ignoreView
     */
    public void addIgnoreHorizontalView(View ignoreView) {
        if (mIgnoreHorizontalViews == null) {
            mIgnoreHorizontalViews = new ArrayList<View>();
        }
        if (!mIgnoreHorizontalViews.contains(ignoreView)) {
            mIgnoreHorizontalViews.add(ignoreView);
        }
    }

    /**
     * 添加不处理view
     *
     * @param ignoreView
     */
    public void addIgnoreView(View ignoreView) {
        if (mIgnoreViews == null) {
            mIgnoreViews = new ArrayList<View>();
        }
        if (!mIgnoreViews.contains(ignoreView)) {
            mIgnoreViews.add(ignoreView);
        }
    }

    /**
     * 添加内容view
     *
     * @param resourceId 布局文件id(默认是：CONTENTVIEWTYPE_LINEARLAYOUT)
     */
    public void setContentView(int resourceId) {
        View contentView = mLayoutInflater.inflate(resourceId, null);
        setContentView(contentView, CONTENTVIEWTYPE_LINEARLAYOUT);
    }

    /**
     * 添加内容view
     *
     * @param resourceId
     * @param contentViewType 内容view类型(CONTENTVIEWTYPE_LINEARLAYOUT /
     *                        CONTENTVIEWTYPE_RELATIVELAYOUT)
     */
    public void setContentView(int resourceId, int contentViewType) {
        View contentView = mLayoutInflater.inflate(resourceId, null);
        setContentView(contentView, contentViewType);
    }

    /**
     * 添加内容view
     *
     * @param contentView     内容view
     * @param contentViewType 内容view类型(CONTENTVIEWTYPE_LINEARLAYOUT /
     *                        CONTENTVIEWTYPE_RELATIVELAYOUT)
     */
    public void setContentView(View contentView, int contentViewType) {
        if (contentViewType == CONTENTVIEWTYPE_LINEARLAYOUT) {
            LayoutParams layoutParams = new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            mRotateLayout.addView(contentView, layoutParams);
        } else if (contentViewType == CONTENTVIEWTYPE_RELATIVELAYOUT) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            mRotateLayout.addView(contentView, layoutParams);
        }
    }

    public void setDragType(int dragType) {
        this.mDragType = dragType;
    }

    public LinearLayout getRotateLayout() {
        return mRotateLayout;
    }

    public void setRotateLayoutListener(RotateLayoutListener rotateLayoutListener) {
        this.mRotateLayoutListener = rotateLayoutListener;
    }

    public interface RotateLayoutListener {
        void close();
    }
}