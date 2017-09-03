package com.zlm.hp.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.zlm.hp.libs.utils.ColorUtil;

/**
 * @Description: 自定义分隔线
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/16 22:18
 * @Throws:
 */
public class DividerView extends View {

    public DividerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public DividerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DividerView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {

    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        int startColor = ColorUtil.parserColor("#000000", 10);
        int endColor = ColorUtil.parserColor("#000000", 20);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Rect rect = new Rect();
        rect.left = 0;
        rect.top = 0;
        rect.right = getWidth();
        rect.bottom = getHeight();

        LinearGradient gradient = new LinearGradient(0, 0, 0, getHeight(), startColor, endColor, Shader.TileMode.MIRROR);
        paint.setShader(gradient);

        canvas.drawRect(rect, paint);

    }
}
