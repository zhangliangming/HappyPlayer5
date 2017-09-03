package com.zlm.hp.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.zlm.hp.libs.utils.ColorUtil;

/**
 * Created by zhangliangming on 2017/8/2.
 */
public class SearchEditText extends AppCompatEditText {


    public SearchEditText(Context context) {
        super(context);
        init(context);
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        int strokeWidth = 1; // 3dp 边框宽度
        float[] roundRadius = {15, 15, 15, 15, 15, 15, 15, 15}; // 圆角半径
        int strokeColor = Color.TRANSPARENT;
        int fillColor = ColorUtil.parserColor(Color.BLACK, 50);

        GradientDrawable gd = new GradientDrawable();// 创建drawable
        gd.setColor(fillColor);
        gd.setCornerRadii(roundRadius);
        gd.setStroke(strokeWidth, strokeColor);
        setBackgroundDrawable(gd);

    }
}
