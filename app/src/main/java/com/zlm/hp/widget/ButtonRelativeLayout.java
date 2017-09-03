package com.zlm.hp.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.zlm.hp.libs.utils.ColorUtil;


/**
 * 按钮点击后，背景颜色
 */
public class ButtonRelativeLayout extends RelativeLayout {

    private boolean isPressed = false;
    private boolean isLoadColor = false;

    private int defFillColor = ColorUtil.parserColor(Color.rgb(45, 162, 249), 255);
    private int pressedFillColor = ColorUtil.parserColor(Color.rgb(70, 178, 250), 255);

    public ButtonRelativeLayout(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ButtonRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ButtonRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!isLoadColor) {

            int strokeWidth = 1; // 3dp 边框宽度
            float[] roundRadius = {15, 15, 15, 15, 15, 15, 15, 15}; // 圆角半径
            int strokeColor = Color.TRANSPARENT;
            int fillColor = Color.TRANSPARENT;
            if (isPressed) {
                fillColor = pressedFillColor;

            } else {
                fillColor = defFillColor;
            }

            GradientDrawable gd = new GradientDrawable();// 创建drawable
            gd.setColor(fillColor);
            gd.setCornerRadii(roundRadius);
            gd.setStroke(strokeWidth, strokeColor);
            setBackgroundDrawable(gd);

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

    public void setDefFillColor(int defFillColor) {
        this.defFillColor = defFillColor;
        isLoadColor = false;
        invalidate();
    }

    public void setPressedFillColor(int pressedFillColor) {
        this.pressedFillColor = pressedFillColor;
        isLoadColor = false;
        invalidate();
    }
}
