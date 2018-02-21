package com.zlm.hp.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.ScrollView;

import com.zlm.hp.libs.utils.LoggerUtil;

/**
 * @Description: 可越界面滚动布局。越界时，只要是移动scrollview的子view的位置。首先用Rect记录Scrollview里的唯一一个子view的位置，然后判断是否可以越界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2018/1/27
 * @Throws:
 */
public class OverScrollView extends ScrollView {

    //根据比例缩小
    private static final float MOVE_FACTOR = 0.5f;
    /**
     * 判断view是点击还是移动的距离
     */
    private int mTouchSlop;

    /**
     * 日志
     */
    private LoggerUtil logger;
    private Context mContext;
    //ScrollView的子View
    private View mContentView;

    //拦截的x轴位置
    private float mLastXIntercept = 0;
    private float mLastYIntercept = 0;

    //是否越界滑动
    private boolean isOverScroll = false;

    //用于记录正常的布局位置
    private Rect originalRect = new Rect();


    //动画
    private ValueAnimator mAnimator;
    private boolean mAnimatorIsCancel = false;

    private Interpolator sInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    public OverScrollView(Context context) {
        super(context);
        init(context);
    }

    public OverScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OverScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public OverScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        logger = LoggerUtil.getZhangLogger(context);
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
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
                    break;

                case MotionEvent.ACTION_MOVE:

                    int deltaX = (int) (mLastXIntercept - curX);
                    int deltaY = (int) (mLastYIntercept - curY);

                    if (Math.abs(deltaX) < mTouchSlop && Math.abs(deltaY) > mTouchSlop && canPullUpOrDown()) {
                        intercepted = true;
                    }

                    break;
                default:
                    break;
            }

            mLastXIntercept = curX;
            mLastYIntercept = curY;

        } catch (Exception e) {
            logger.e(e.getMessage());
        }

        return intercepted;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mContentView == null) {
            return super.onTouchEvent(event);
        }

        try {

            float curX = event.getX();
            float curY = event.getY();

            int actionId = event.getAction();
            switch (actionId) {
                case MotionEvent.ACTION_DOWN:

                    mLastXIntercept = curX;
                    mLastYIntercept = curY;

                    break;
                case MotionEvent.ACTION_MOVE:

                    int deltaX = (int) (mLastXIntercept - curX);
                    int deltaY = (int) (mLastYIntercept - curY);
                    //事件为上下移动，并且可以下拉和上拉
                    if (isOverScroll || (Math.abs(deltaX) < mTouchSlop && Math.abs(deltaY) > mTouchSlop && canPullUpOrDown())) {

                        isOverScroll = true;
                        //计算偏移量
                        int offset = (int) (deltaY * MOVE_FACTOR);
                        //随着手指的移动而移动布局
                        mContentView.layout(originalRect.left, originalRect.top - offset,
                                originalRect.right, originalRect.bottom - offset);

                    }

                    // mLastY = event.getY();
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:

                    if (!isOverScroll) break;

                    isOverScroll = false;

                    resetView();

                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            logger.e(e.getMessage());
        }
        return true;
    }

    /**
     * 还原view
     */
    private void resetView() {
        if (mAnimator != null) {

            if (mAnimator.isRunning()) {
                mAnimatorIsCancel = true;
                mAnimator.removeAllListeners();
                mAnimator.cancel();
            }
            mAnimator = null;
        }

        //使用开源动画库nineoldandroids来兼容api11之前的版本
        mAnimator = ValueAnimator.ofInt(mContentView.getTop(), originalRect.top);

        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                if (!mAnimatorIsCancel) {

                    Number number = (Number) valueAnimator.getAnimatedValue();

                    //计算偏移量
                    int offset = number.intValue() - mContentView.getTop();

                    mContentView.layout(originalRect.left, mContentView.getTop() + offset,
                            originalRect.right, mContentView.getBottom() + offset);
                }

            }
        });
        mAnimator.setDuration(250);
        mAnimator.setInterpolator(sInterpolator);
        mAnimator.start();
        mAnimatorIsCancel = false;
    }

    /**
     * 判断当前是否可以下拉或者上拉
     *
     * @return
     */
    private boolean canPullUpOrDown() {
        if (this.getScrollY() == 0 || mContentView.getHeight() < this.getHeight() + this.getScrollY()) {
            //滚动到顶部
            return true;
        } else if (mContentView.getHeight() <= this.getHeight() + this.getScrollY()) {
            //滚动底部
            return true;
        }
        return false;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            mContentView = getChildAt(0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (mContentView == null) return;

        //ScrollView中的唯一子控件的位置信息, 这个位置信息在整个控件的生命周期中保持不变
        originalRect.set(mContentView.getLeft(), mContentView.getTop(), mContentView
                .getRight(), mContentView.getBottom());
    }
}
