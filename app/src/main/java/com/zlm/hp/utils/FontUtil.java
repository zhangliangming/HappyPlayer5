package com.zlm.hp.utils;

import android.content.Context;
import android.graphics.Typeface;

/**
 * 加载字体
 * 
 * @author zhangliangming
 * 
 */
public class FontUtil {
	/**
	 * 字体
	 */
	private static Typeface typeFace;

	private static FontUtil _FontUtil;

	public FontUtil(Context context) {
		typeFace = Typeface.createFromAsset(context.getAssets(),
				"fonts/iconfont.ttf");
	}

	public static FontUtil getInstance(Context context) {
		if (_FontUtil == null) {
			_FontUtil = new FontUtil(context);
		}
		return _FontUtil;
	}

	public Typeface getTypeFace() {
		return typeFace;
	}

}
