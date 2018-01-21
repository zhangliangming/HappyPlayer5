package com.zlm.hp.widget.lock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 
 * 锁屏按钮
 * 
 */
public class LockButtonRelativeLayout extends RelativeLayout {

	private Paint paint;
	private boolean isTouch = false;

	public LockButtonRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public LockButtonRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LockButtonRelativeLayout(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		paint = new Paint();
		paint.setDither(true);
		paint.setAntiAlias(true);

	}

	public void setPressed(boolean pressed) {
		isTouch = pressed;
		invalidate();
		super.setPressed(pressed);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (isTouch) {
			paint.setColor(Color.rgb(37, 158, 247));
			paint.setStyle(Paint.Style.FILL);
			paint.setStrokeWidth(3);
			int cx = getWidth() / 2;
			int cy = getHeight() / 2;
			canvas.drawCircle(cx, cy, getWidth() / 3, paint);
		} else {
			paint.setColor(Color.WHITE);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(3);
			int cx = getWidth() / 2;
			int cy = getHeight() / 2;
			canvas.drawCircle(cx, cy, getWidth() / 3, paint);
		}
		super.dispatchDraw(canvas);
	}
}
