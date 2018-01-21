package com.zlm.hp.widget.lock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 
 * 锁屏按钮
 * 
 */
public class LockPalyOrPauseButtonRelativeLayout extends RelativeLayout {
	private Paint progressPaint;
	private Paint paint;
	private boolean isTouch = false;

	private int maxProgress = 0;
	private int playingProgress = 0;

	public LockPalyOrPauseButtonRelativeLayout(Context context,
			AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public LockPalyOrPauseButtonRelativeLayout(Context context,
			AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LockPalyOrPauseButtonRelativeLayout(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		paint = new Paint();
		paint.setDither(true);
		paint.setAntiAlias(true);

		progressPaint = new Paint();
		progressPaint.setDither(true);
		progressPaint.setAntiAlias(true);
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
			int r = getWidth() / 3;
			canvas.drawCircle(cx, cy, r, paint);

			progressPaint.setColor(Color.rgb(37, 158, 247));
			progressPaint.setStyle(Paint.Style.STROKE);
			progressPaint.setStrokeWidth(5);

			if (maxProgress != 0) {

				RectF oval = new RectF();
				oval.left = (getWidth() / 2 - r);
				oval.top = (getHeight() / 2 - r);
				oval.right = getWidth() / 2 + r;
				oval.bottom = getHeight() / 2 + r;
				canvas.drawArc(oval, -90,
						((float) playingProgress / maxProgress) * 360, false,
						progressPaint);
			}

		}
		super.dispatchDraw(canvas);
	}

	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
	}

	public void setPlayingProgress(int playingProgress) {
		this.playingProgress = playingProgress;
	}

}
