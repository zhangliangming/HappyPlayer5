package com.happy.lyrics.utils;

import java.util.TreeMap;

import com.happy.lyrics.model.LyricsLineInfo;

/**
 * 歌词数据处理
 * 
 * @author zhangliangming
 * 
 */
public class LyricsUtils {

	/**
	 * 根据当前的播放时间，获取当前的播放行和（歌词变大，导致一行歌词变为多行）子行的索引
	 * 
	 * @param currTime
	 *            当前播放时间
	 * @param lyricsLineInfos
	 *            歌词列表
	 * @param viewWidth
	 *            歌词视图宽度
	 * @param textWidth
	 *            歌词文本的宽度
	 */
	public static int[] getLineIndexAndItemLineIndex(int currTime,
			TreeMap<Integer, LyricsLineInfo> lyricsLineInfos, int viewWidth,
			int textWidth) {
		int[] result = new int[2];
		int lrcIndex = getLyricsLineIndex(lyricsLineInfos, currTime);
		int itemLrcIndex = -1;

		//
		if (lrcIndex != -1) {
			LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(lrcIndex);
			TreeMap<Integer, LyricsLineInfo> itemLyricsLineInfos = getItemLyricsLineInfo(
					lyricsLineInfo, viewWidth, textWidth);
			itemLrcIndex = getLyricsLineIndex(itemLyricsLineInfos, currTime);
		}

		result[0] = lrcIndex;
		result[1] = itemLrcIndex;
		return result;

	}

	/**
	 * 获取歌词行索引
	 * 
	 * @param lyricsLineInfos
	 * @param currTime
	 * @return
	 */
	private static int getLyricsLineIndex(
			TreeMap<Integer, LyricsLineInfo> lyricsLineInfos, int currTime) {
		if (currTime >= lyricsLineInfos.get(lyricsLineInfos.size() - 1)
				.getEndTime()) {
			return lyricsLineInfos.size() - 1;
		} else {
			for (int i = 0; i < lyricsLineInfos.size(); i++) {

				if (currTime >= lyricsLineInfos.get(i).getStartTime()
						&& currTime <= lyricsLineInfos.get(i).getEndTime()) {
					return i;
				}
				if (currTime > lyricsLineInfos.get(i).getEndTime()
						&& i + 1 < lyricsLineInfos.size()
						&& currTime < lyricsLineInfos.get(i + 1).getStartTime()) {
					return i;
				}
			}
			return -1;
		}
	}

	/**
	 * 根据行歌词和该视图的宽度，获取换行后的歌词列表
	 * 
	 * @param lyricsLineInfo
	 * @param viewWidth
	 * @param textWidth
	 * @return
	 */
	public static TreeMap<Integer, LyricsLineInfo> getItemLyricsLineInfo(
			LyricsLineInfo lyricsLineInfo, int viewWidth, int textWidth) {

		// 换行歌词列表
		TreeMap<Integer, LyricsLineInfo> itemLyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
		String lineLyrics = lyricsLineInfo.getLineLyrics();
		// 获取整行歌词的长度
		int lyricsTextWidth = lineLyrics.length() * textWidth;
		// 行歌词数组
		String[] lyricsWords = lyricsLineInfo.getLyricsWords();
		// 每行歌词的字时间
		int[] wordsDisInterval = lyricsLineInfo.getWordsDisInterval();
		// 判断该行歌词是否超出了视图
		int maxLineWidth = viewWidth / 3 * 2;

		int index = 0;
		if (lyricsTextWidth > maxLineWidth) {

			// 最大的歌词行数
			int maxLrcLineNum = lyricsTextWidth % maxLineWidth == 0 ? lyricsTextWidth
					/ maxLineWidth
					: (lyricsTextWidth / maxLineWidth + 1);
			// 最大的行歌词长度
			int maxLrcLineWidth = lyricsTextWidth / maxLrcLineNum;

			// 每行的最后一个字的索引
			int lastIndex = lyricsWords.length - 1;

			int lyricsWordsWidth = 0;
			for (int i = lyricsWords.length - 1; i >= 0; i--) {
				// 类加每个字的长度
				lyricsWordsWidth += textWidth;// lyricsWords[i];
				if (lyricsWordsWidth > maxLrcLineWidth) {

					//
					LyricsLineInfo itemLyricsLineInfo = getItemLyricsLineInfo(
							wordsDisInterval, lyricsWords, i, lastIndex);
					//
					lastIndex = i;
					i++;// 超出的视图宽度，后退一个索引
					lyricsWordsWidth = 0;// 清空之前累计的歌词宽度
					//
					if (itemLyricsLineInfo != null)
						itemLyricsLineInfos.put(index++, itemLyricsLineInfo);

				} else if (i == 0) {
					//
					LyricsLineInfo itemLyricsLineInfo = getItemLyricsLineInfo(
							wordsDisInterval, lyricsWords, 0, lastIndex);
					if (itemLyricsLineInfo != null)
						itemLyricsLineInfos.put(index++, itemLyricsLineInfo);
				}
			}

		} else {
			itemLyricsLineInfos.put(index++, lyricsLineInfo);
		}
		return itemLyricsLineInfos;
	}

	/**
	 * 获取换行歌词数据
	 * 
	 * @param wordsDisInterval
	 *            行歌词时间数组
	 * @param lyricsWords
	 *            行歌词歌词数组
	 * @param startIndex
	 *            开始索引
	 * @param endIndex
	 *            结束索引
	 * @return
	 */
	private static LyricsLineInfo getItemLyricsLineInfo(int[] wordsDisInterval,
			String[] lyricsWords, int startIndex, int endIndex) {

		if (endIndex < 0 || startIndex < 0 || startIndex >= lyricsWords.length
				|| endIndex >= lyricsWords.length || startIndex > endIndex)
			return null;

		//
		LyricsLineInfo newLyricsLineInfo = new LyricsLineInfo();
		int[] newWordsDisInterval = new int[(endIndex - startIndex + 1)];
		String[] newLyricsWords = new String[(endIndex - startIndex + 1)];

		// 获取歌词时间
		int startTime = 0;
		int itemEndTimeSum = 0;
		for (int i = 0, index = 0; i <= endIndex; i++) {
			if (i < startIndex) {
				startTime += wordsDisInterval[i];
			} else {
				newWordsDisInterval[index++] = wordsDisInterval[i];
				itemEndTimeSum += wordsDisInterval[i];
			}
		}
		newLyricsLineInfo.setStartTime(startTime);
		newLyricsLineInfo.setEndTime((startTime + itemEndTimeSum));
		newLyricsLineInfo.setWordsDisInterval(newWordsDisInterval);

		// 获取歌词
		String newLineLyrics = "";
		for (int i = startIndex, index = 0; i <= endIndex; i++) {
			newLyricsWords[index++] = lyricsWords[i];
			newLineLyrics += lyricsWords[i];
		}
		newLyricsLineInfo.setLyricsWords(newLyricsWords);
		newLyricsLineInfo.setLineLyrics(newLineLyrics);

		return newLyricsLineInfo;
	}
}
