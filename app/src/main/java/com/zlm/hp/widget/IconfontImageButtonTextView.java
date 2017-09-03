package com.zlm.hp.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.zlm.hp.libs.utils.ColorUtil;
import com.zlm.hp.utils.FontUtil;


/**
 * @Description: 字体图标按钮文本
 * @Author: zhangliangming
 * @Date: 2017/7/16 15:55
 * @Version:
 */
public class IconfontImageButtonTextView extends AppCompatTextView {

    private float oldTextSize = -1f;
    /**
     * 是否倒置
     */
    private boolean convert = false;

    public IconfontImageButtonTextView(Context context) {
        super(context);
        init(context);
    }

    public IconfontImageButtonTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IconfontImageButtonTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 设置字体图片
        Typeface iconfont = FontUtil.getInstance(context).getTypeFace();
        setTypeface(iconfont);
        setClickable(true);
    }


    public void setConvert(boolean convert) {
        this.convert = convert;
        setPressed(false);
    }

    @Override
    public void setPressed(boolean pressed) {
        {

            int color = getCurrentTextColor();
            TextPaint paint = getPaint();

            //
            if (oldTextSize == -1) {
                oldTextSize = getTextSize();
            }

            boolean isPressed = pressed;
            //如果倒置为true
            if (convert) {
                isPressed = !pressed;
            }

            if (isPressed) {
                int pressedColor = ColorUtil.parserColor(color, 240);
                setTextColor(pressedColor);
                paint.setFakeBoldText(true);
                paint.setTextSize(oldTextSize + 5);
            } else {
                int defColor = ColorUtil.parserColor(color, 150);
                setTextColor(defColor);
                paint.setFakeBoldText(false);
                paint.setTextSize(oldTextSize);
            }

        }

        super.setPressed(pressed);
    }


}
