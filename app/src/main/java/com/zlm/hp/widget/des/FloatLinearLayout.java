package com.zlm.hp.widget.des;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

/**
 * @Description: 悬浮布局
 * @author: zhangliangming
 * @date: 2018-05-12 14:33
 **/
public class FloatLinearLayout extends LinearLayout {
    /**
     * 判断view是点击还是移动的距离
     */
    private int mTouchSlop;

    /**
     * 拦截的X轴和Y最后的位置
     */
    private float mLastInterceptX = 0, mLastInterceptY = 0;

    private FloatEventCallBack mFloatEventCallBack;

    private boolean isTouchMove = false;


    public FloatLinearLayout(Context context) {
        super(context);
        init(context);
    }

    public FloatLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        boolean intercepted = false;
        float curX = event.getRawX();
        float curY = event.getRawY();

        int actionId = event.getAction();
        switch (actionId) {

            case MotionEvent.ACTION_DOWN:
                mLastInterceptX = curX;
                mLastInterceptY = curY;
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (mLastInterceptX - curX);
                int deltaY = (int) (mLastInterceptY - curY);

                if ((Math.abs(deltaX) < mTouchSlop
                        && Math.abs(deltaY) > mTouchSlop)) {

                    //上下移动

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

        float curX = event.getRawX();
        float curY = event.getRawY();

        int actionId = event.getAction();
        switch (actionId) {

            case MotionEvent.ACTION_DOWN:
                mLastInterceptX = curX;
                mLastInterceptY = curY;
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (mLastInterceptX - curX);
                int deltaY = (int) (mLastInterceptY - curY);
                if (isTouchMove || (Math.abs(deltaX) < mTouchSlop
                        && Math.abs(deltaY) > mTouchSlop)) {

                    if (!isTouchMove) {
                        if (mFloatEventCallBack != null) {
                            mFloatEventCallBack.moveStart();
                        }
                    }
                    //上下移动

                    isTouchMove = true;

                    if (mFloatEventCallBack != null) {
                        mFloatEventCallBack.move(deltaY);
                    }

                }

                break;
            case MotionEvent.ACTION_UP:
                int x = (int) (mLastInterceptX - curX);
                int y = (int) (mLastInterceptY - curY);
                if ((Math.abs(x) < mTouchSlop
                        && Math.abs(y) < mTouchSlop)) {

                    //点击

                    if (mFloatEventCallBack != null) {
                        mFloatEventCallBack.click();
                    }
                }
                if (isTouchMove) {
                    if (mFloatEventCallBack != null) {
                        mFloatEventCallBack.move(y);
                    }
                }

            default:
                if (mFloatEventCallBack != null) {
                    mFloatEventCallBack.moveEnd();
                }
                isTouchMove = false;

                break;
        }

        return true;
    }

    public void setFloatEventCallBack(FloatEventCallBack mFloatEventCallBack) {
        this.mFloatEventCallBack = mFloatEventCallBack;
    }

    public interface FloatEventCallBack {
        void moveStart();

        void move(int dy);

        void moveEnd();

        void click();
    }
}
