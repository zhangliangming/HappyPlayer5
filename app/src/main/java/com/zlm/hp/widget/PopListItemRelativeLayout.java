package com.zlm.hp.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.zlm.hp.libs.utils.ColorUtil;

/**
 * 弹出窗口
 * listview item
 */
public class PopListItemRelativeLayout extends RelativeLayout {

    private int defColor;
    private int pressColor;

    private boolean isPressed = false;
    private boolean isLoadColor = false;


    public PopListItemRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public PopListItemRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PopListItemRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        defColor = ColorUtil.parserColor("#ffffff", 0);
        pressColor = ColorUtil.parserColor("#ffffff", 50);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!isLoadColor) {

            if (isPressed) {
                setBackgroundColor(pressColor);
            } else {

                setBackgroundColor(defColor);

            }
            isLoadColor = true;
        }
        super.dispatchDraw(canvas);
    }

    public void setPressed(boolean pressed) {
        isLoadColor = false;
        isPressed = pressed;
        invalidate();
        super.setPressed(pressed);
    }

}
