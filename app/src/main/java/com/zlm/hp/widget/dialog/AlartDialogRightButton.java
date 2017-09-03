package com.zlm.hp.widget.dialog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class AlartDialogRightButton extends RelativeLayout {
	private boolean isLoadColor = false;
	private boolean isPressed = false;

	public AlartDialogRightButton(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public AlartDialogRightButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlartDialogRightButton(Context context) {
		super(context);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {

		if (!isLoadColor) {
			int strokeWidth = 1; // 3dp 边框宽度
			float[] roundRadius = { 0, 0, 0, 0, 15, 15, 0, 0 }; // 圆角半径
			int strokeColor = parserColor("#eeeeee,50");
			int fillColor = Color.WHITE;
			if (isPressed) {
				fillColor = parserColor("#eeeeee");
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

	/**
	 * 解析颜色字符串
	 * 
	 * @param value
	 *            颜色字符串 #edf8fc,255
	 * @return
	 */
	private int parserColor(String value) {
		String regularExpression = ",";
		if (value.contains(regularExpression)) {
			String[] temp = value.split(regularExpression);

			int color = Color.parseColor(temp[0]);
			int alpha = Integer.valueOf(temp[1]);
			int red = (color & 0xff0000) >> 16;
			int green = (color & 0x00ff00) >> 8;
			int blue = (color & 0x0000ff);

			return Color.argb(alpha, red, green, blue);
		}
		return Color.parseColor(value);
	}
}
