package com.zlm.hp.widget.dialog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class AlartDialogCenter extends RelativeLayout {
	private boolean isLoadColor = false;

	public AlartDialogCenter(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AlartDialogCenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlartDialogCenter(Context context) {
		super(context);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {

		if (!isLoadColor) {
			int strokeWidth = 1; // 3dp 边框宽度
			float[] roundRadius = { 0, 0, 0, 0, 0, 0, 0, 0 }; // 圆角半径
			int strokeColor = Color.TRANSPARENT;
			int fillColor = Color.WHITE;

			GradientDrawable gd = new GradientDrawable();// 创建drawable
			gd.setColor(fillColor);
			gd.setCornerRadii(roundRadius);
			gd.setStroke(strokeWidth, strokeColor);
			setBackgroundDrawable(gd);

			isLoadColor = true;
		}
		super.dispatchDraw(canvas);
	}
}
