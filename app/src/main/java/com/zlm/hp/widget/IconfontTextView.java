package com.zlm.hp.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.zlm.hp.utils.FontUtil;


/**
 * @Description: 字体图标文本
 * @Author: zhangliangming
 * @Date: 2017/7/16 15:55
 * @Version:
 */
public class IconfontTextView extends AppCompatTextView {
    public IconfontTextView(Context context) {
        super(context);
        init(context);
    }

    public IconfontTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IconfontTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 设置字体图片
        Typeface iconfont = FontUtil.getInstance(context).getTypeFace();
        setTypeface(iconfont);
    }


}
