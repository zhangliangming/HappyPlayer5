package com.zlm.hp.lyrics.utils;

/**
 * 
 * @author zhangliangming
 * 
 */
public class CharUtils {
	/**
	 * 判断字符是不是中文，中文字符标点都可以判断
	 * 
	 * @param c
	 *            字符
	 * @return
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是日语平假名
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isHiragana(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.HIRAGANA) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是韩语
	 * 
	 * @return
	 */
	public static boolean isHangulSyllables(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.HANGUL_SYLLABLES) {
			return true;
		}
		return false;
	}

	/**
	 * 判断该歌词是不是字母
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isWord(char c) {
		if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')) {
			return true;
		}
		return false;
	}
}
