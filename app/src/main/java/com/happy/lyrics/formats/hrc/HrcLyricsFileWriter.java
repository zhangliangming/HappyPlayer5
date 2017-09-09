package com.happy.lyrics.formats.hrc;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import com.happy.lyrics.LyricsFileWriter;
import com.happy.lyrics.model.LyricsInfo;
import com.happy.lyrics.model.LyricsLineInfo;
import com.happy.lyrics.model.LyricsTag;
import com.happy.lyrics.utils.CharUtils;
import com.happy.lyrics.utils.StringCompressUtils;
import com.happy.lyrics.utils.TimeUtils;

/**
 * hrc歌词保存器
 * 
 * @author zhangliangming
 * 
 */
public class HrcLyricsFileWriter extends LyricsFileWriter {
	/**
	 * 歌曲名 字符串
	 */
	private final static String LEGAL_SONGNAME_PREFIX = "haplayer.songName";
	/**
	 * 歌手名 字符串
	 */
	private final static String LEGAL_SINGERNAME_PREFIX = "haplayer.singer";
	/**
	 * 时间补偿值 字符串
	 */
	private final static String LEGAL_OFFSET_PREFIX = "haplayer.offset";
	/**
	 * 歌词Tag
	 */
	public final static String LEGAL_TAG_PREFIX = "haplayer.tag";
	/**
	 * 歌词 字符串
	 */
	public final static String LEGAL_LYRICS_LINE_PREFIX = "haplayer.lrc";

	public HrcLyricsFileWriter() {
		// 设置编码
		setDefaultCharset(Charset.forName("GB2312"));
	}

	private String parseLyricsInfo(LyricsInfo lyricsIfno) throws Exception {
		String lyricsCom = "";
		// 先保存所有的标签数据
		Map<String, Object> tags = lyricsIfno.getLyricsTags();
		for (Map.Entry<String, Object> entry : tags.entrySet()) {
			Object val = entry.getValue();
			if (entry.getKey().equals(LyricsTag.TAG_TITLE)) {
				lyricsCom += LEGAL_SONGNAME_PREFIX;
			} else if (entry.getKey().equals(LyricsTag.TAG_ARTIST)) {
				lyricsCom += LEGAL_SINGERNAME_PREFIX;
			} else if (entry.getKey().equals(LyricsTag.TAG_OFFSET)) {
				lyricsCom += LEGAL_OFFSET_PREFIX;
			} else {
				lyricsCom += LEGAL_TAG_PREFIX;
				val = entry.getKey() + ":" + val;
			}
			lyricsCom += " := '" + val + "';\n";
		}
		// 每行歌词内容
		TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsIfno
				.getLyricsLineInfos();
		// 将每行歌词，放到有序的map，判断已重复的歌词
		LinkedHashMap<String, List<Integer>> lyricsLineInfoMapResult = new LinkedHashMap<String, List<Integer>>();
		for (int i = 0; i < lyricsLineInfos.size(); i++) {
			LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);
			String lyricsText = lyricsLineInfo.getLineLyrics();
			List<Integer> indexs = null;
			// 如果已存在该行歌词，则往里面添加歌词行索引
			if (lyricsLineInfoMapResult.containsKey(lyricsText)) {
				indexs = lyricsLineInfoMapResult.get(lyricsText);
			} else {
				indexs = new ArrayList<Integer>();
			}
			indexs.add(i);
			lyricsLineInfoMapResult.put(lyricsText, indexs);
		}
		// 遍历
		for (Map.Entry<String, List<Integer>> entry : lyricsLineInfoMapResult
				.entrySet()) {
			lyricsCom += LEGAL_LYRICS_LINE_PREFIX + "('";
			List<Integer> indexs = entry.getValue();
			// 当前行歌词文本
			String lyricsText = getLineLyrics(entry.getKey());
			String timeText = "";// 时间标签内容
			String wordsDisIntervalText = "";// 每个歌词时间
			for (int i = 0; i < indexs.size(); i++) {
				int key = indexs.get(i);
				LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(key);
				// 获取开始时间和结束时间
				timeText += "<"
						+ TimeUtils.parseString(lyricsLineInfo.getStartTime())
						+ ",";
				timeText += TimeUtils.parseString(lyricsLineInfo.getEndTime())
						+ ">";
				// 获取每个歌词的时间
				String wordsDisIntervalTextTemp = "";
				int wordsDisInterval[] = lyricsLineInfo.getWordsDisInterval();
				for (int j = 0; j < wordsDisInterval.length; j++) {
					if (j == 0)
						wordsDisIntervalTextTemp += wordsDisInterval[j] + "";
					else
						wordsDisIntervalTextTemp += ":" + wordsDisInterval[j]
								+ "";
				}
				// 获取每个歌词时间的文本
				if (wordsDisIntervalText.equals("")) {
					wordsDisIntervalText += wordsDisIntervalTextTemp;
				} else {
					wordsDisIntervalText += "," + wordsDisIntervalTextTemp;
				}
			}
			lyricsCom += timeText + "'";
			lyricsCom += ",'" + lyricsText + "'";
			lyricsCom += ",'" + wordsDisIntervalText + "');\n";
		}
		return lyricsCom;
	}

	/**
	 * 获取当行歌词
	 * 
	 * @param lrcComTxt
	 *            歌词文本
	 * @return
	 */
	private String getLineLyrics(String lrcComTxt) {
		String newLrc = "";
		Stack<String> lrcStack = new Stack<String>();
		String temp = "";
		for (int i = 0; i < lrcComTxt.length(); i++) {
			char c = lrcComTxt.charAt(i);
			if (CharUtils.isChinese(c)) {

				if (!temp.equals("")) {
					lrcStack.push(temp);
					temp = "";
				}

				lrcStack.push(String.valueOf(c));
			} else if (Character.isSpaceChar(c)) {
				if (!temp.equals("")) {
					lrcStack.push(temp);
					temp = "";
				}
				String tw = lrcStack.pop();
				if (tw != null) {
					lrcStack.push("[" + tw + " " + "]");
				}
			} else {
				temp += String.valueOf(c);
			}
		}
		//
		if (!temp.equals("")) {
			lrcStack.push("[" + temp + "]");
			temp = "";
		}

		String[] lyricsWords = new String[lrcStack.size()];
		Iterator<String> it = lrcStack.iterator();
		int i = 0;
		while (it.hasNext()) {
			String com = it.next();
			String tempCom = "";
			for (int j = 0; j < com.length(); j++) {
				char reg = com.charAt(j);
				if (reg == '[')
					continue;
				if (reg == ']')
					continue;
				tempCom += reg;
			}
			lyricsWords[i++] = tempCom;
			newLrc += com;
		}
		return newLrc;
	}

	@Override
	public boolean writer(LyricsInfo lyricsIfno, String lyricsFilePath)
			throws Exception {
		try {
			File lyricsFile = new File(lyricsFilePath);
			if (lyricsFile != null) {
				//
				if (!lyricsFile.getParentFile().exists()) {
					lyricsFile.getParentFile().mkdirs();
				}
				// 对字符串运行压缩
				byte[] content = StringCompressUtils.compress(
						parseLyricsInfo(lyricsIfno), getDefaultCharset());
				// 生成歌词文件
				FileOutputStream os = new FileOutputStream(lyricsFile);
				os.write(content);
				os.close();

			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isFileSupported(String ext) {
		return ext.equalsIgnoreCase("hrc");
	}

	@Override
	public String getSupportFileExt() {
		return "hrc";
	}

}
