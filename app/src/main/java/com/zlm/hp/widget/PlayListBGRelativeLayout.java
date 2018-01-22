package com.zlm.hp.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.zlm.hp.libs.utils.ColorUtil;

/**
 * 播放列表背景
 */
public class PlayListBGRelativeLayout extends RelativeLayout {

    public PlayListBGRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public PlayListBGRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PlayListBGRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        int strokeWidth = 1; // 3dp 边框宽度
        float[] roundRadius = {15, 15, 15, 15, 0, 0, 0, 0}; // 圆角半径
        int strokeColor = Color.TRANSPARENT;// 边框颜色
        int fillColor = ColorUtil.parserColor("#263c56", 255);

        GradientDrawable gd = new GradientDrawable();// 创建drawable
        gd.setColor(fillColor);
        gd.setCornerRadii(roundRadius);
        gd.setStroke(strokeWidth, strokeColor);
        setBackgroundDrawable(gd);

    }
}
