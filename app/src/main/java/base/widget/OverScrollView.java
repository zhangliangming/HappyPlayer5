package base.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.ScrollView;

import base.utils.LoggerUtil;

/**
 * @Description: 可越界面滚动布局。越界时，只要是移动scrollview的子view的位置。首先用Rect记录Scrollview里的唯一一个子view的位置，然后判断是否可以越界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/26 19:19
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
     * 触摸最后一次的坐标
     */
    private float mLastX;
    private float mLastY;
    /**
     * 日志
     */
    private LoggerUtil logger;
    private Context mContext;
    //ScrollView的子View
    private View mContentView;
    /**
     * 是否正在移动
     */
    private boolean isMoved = false;
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
        try {
            if (mContentView == null) {
                return super.onInterceptTouchEvent(event);
            }
            int actionId = event.getAction();
            switch (actionId) {
                case MotionEvent.ACTION_DOWN:
                    mLastX = event.getX();
                    mLastY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float curX = event.getX();
                    float curY = event.getY();
                    int deltaX = (int) (mLastX - curX);
                    int deltaY = (int) (mLastY - curY);

                    //事件为上下移动，并且可以下拉和上拉
                    if (Math.abs(deltaX) < mTouchSlop && Math.abs(deltaY) > mTouchSlop && canPullUpOrDown()) {

                        isMoved = true;
                        return true;

                    }

                    //mLastY = event.getY();
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:

                    mLastX = 0;
                    mLastY = 0;


                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mContentView == null) {
            return super.onTouchEvent(event);
        }

        try {

            int actionId = event.getAction();
            switch (actionId) {
                case MotionEvent.ACTION_DOWN:
                    mLastX = event.getX();
                    mLastY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:

                    float curX = event.getX();
                    float curY = event.getY();
                    int deltaY = (int) ((mLastY - curY));
                    int deltaX = (int) (mLastX - curX);
                    //事件为上下移动，并且可以下拉和上拉
                    if (isMoved || (Math.abs(deltaX) < mTouchSlop && Math.abs(deltaY) > mTouchSlop && canPullUpOrDown())) {

                        isMoved = true;
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
                    mLastX = 0;
                    mLastY = 0;
                    if (!isMoved) break;

                    isMoved = false;

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
