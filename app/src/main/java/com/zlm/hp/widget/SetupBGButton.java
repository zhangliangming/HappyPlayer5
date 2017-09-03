package com.zlm.hp.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @Description: 设置开关按钮
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/21 21:51
 * @Throws:
*/
public class SetupBGButton extends RelativeLayout {

	private boolean isLoadColor = false;
	private boolean isSelect = false;
	private boolean isPressed = false;

	public SetupBGButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public SetupBGButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SetupBGButton(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {

	}

	@Override
	protected void dispatchDraw(Canvas canvas) {

		if (!isLoadColor) {
			int strokeWidth = 2; // 3dp 边框宽度
			int strokeColor = parserColor("#555555,60");// 边框颜色
			int fillColor = Color.TRANSPARENT;
			if (isPressed || isSelect) {
				strokeColor = parserColor("#31afff,130");
				fillColor = parserColor("#31afff,40");
				invalidateChild(parserColor("#31afff"));
			} else {
				invalidateChild(parserColor("#555555"));
			}
			GradientDrawable gd = new GradientDrawable();// 创建drawable
			gd.setColor(fillColor);
			gd.setStroke(strokeWidth, strokeColor);
			gd.setShape(GradientDrawable.OVAL);
			setBackgroundDrawable(gd);

			isLoadColor = true;
		}
		super.dispatchDraw(canvas);
	}

	@Override
	public void setPressed(boolean pressed) {
		isLoadColor = false;
		isPressed = pressed;
		invalidate();
		super.setPressed(pressed);
	}

	public void setSelect(boolean select) {
		isLoadColor = false;
		isSelect = select;
		invalidate();
	}

	public boolean isSelect() {
		return isSelect;
	}

	private void invalidateChild(int textColor) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View v = getChildAt(i);
			if (v instanceof TextView) {
				TextView temp = (TextView) v;
				temp.setTextColor(textColor);
			}
		}
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
