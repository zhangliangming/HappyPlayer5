package com.zlm.hp.mp3.lyrics.formats.hrcx;

import android.util.Base64;

import com.zlm.hp.mp3.lyrics.LyricsFileWriter;
import com.zlm.hp.mp3.lyrics.model.LyricsInfo;
import com.zlm.hp.mp3.lyrics.model.LyricsLineInfo;
import com.zlm.hp.mp3.lyrics.model.LyricsTag;
import com.zlm.hp.mp3.lyrics.model.TranslateLrcLineInfo;
import com.zlm.hp.mp3.lyrics.utils.CharUtils;
import com.zlm.hp.mp3.lyrics.utils.StringCompressUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/**
 * @Description: hrcx歌词
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/12/25 17:35
 * @Throws:
 */

public class HrcxLyricsFileWriter extends LyricsFileWriter {

    /**
     * 歌曲名 字符串
     */
    private final static String LEGAL_TITLE_PREFIX = "[ti:";
    /**
     * 歌手名 字符串
     */
    private final static String LEGAL_ARTIST_PREFIX = "[ar:";
    /**
     * 时间补偿值 字符串
     */
    private final static String LEGAL_OFFSET_PREFIX = "[offset:";
    /**
     * 歌曲长度
     */
    private final static String LEGAL_TOTAL_PREFIX = "[total:";
    /**
     * 上传者
     */
    private final static String LEGAL_BY_PREFIX = "[by:";
    /**
     * Tag标签
     */
    private final static String LEGAL_TAG_PREFIX = "haplayer.tag[";

    /**
     * 歌词 字符串
     */
    public final static String LEGAL_LYRICS_LINE_PREFIX = "haplayer.lrc";
    /**
     * 额外歌词
     */
    private final static String LEGAL_EXTRA_LYRICS_PREFIX = "haplayer.extra.lrc";

    public HrcxLyricsFileWriter() {
    }

    @Override
    public boolean writer(LyricsInfo lyricsIfno, String lyricsFilePath) throws Exception {
        // 对字符串运行压缩
        byte[] lyricsContent = StringCompressUtils.compress(
                getLyricsContent(lyricsIfno), getDefaultCharset());
        return saveLyricsFile(lyricsContent, lyricsFilePath);
    }

    @Override
    public String getLyricsContent(LyricsInfo lyricsIfno) throws Exception {
        String lyricsCom = "";
        // 先保存所有的标签数据
        Map<String, Object> tags = lyricsIfno.getLyricsTags();
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            Object val = entry.getValue();
            if (entry.getKey().equals(LyricsTag.TAG_TITLE)) {
                lyricsCom += LEGAL_TITLE_PREFIX;
            } else if (entry.getKey().equals(LyricsTag.TAG_ARTIST)) {
                lyricsCom += LEGAL_ARTIST_PREFIX;
            } else if (entry.getKey().equals(LyricsTag.TAG_OFFSET)) {
                lyricsCom += LEGAL_OFFSET_PREFIX;
            } else if (entry.getKey().equals(LyricsTag.TAG_BY)) {
                lyricsCom += LEGAL_BY_PREFIX;
            } else if (entry.getKey().equals(LyricsTag.TAG_TOTAL)) {
                lyricsCom += LEGAL_TOTAL_PREFIX;
            } else {
                lyricsCom += LEGAL_TAG_PREFIX + entry.getKey() + ":";
            }
            lyricsCom += val + "];\n";
        }

        // 获取额外歌词行（翻译歌词和音译歌词）
        JSONObject extraLyricsObj = new JSONObject();
        JSONArray contentArray = new JSONArray();
        // 判断是否有翻译歌词
        if (lyricsIfno.getTranslateLyricsInfo() != null) {
            List<TranslateLrcLineInfo> translateLrcLineInfos = lyricsIfno
                    .getTranslateLyricsInfo().getTranslateLrcLineInfos();
            if (translateLrcLineInfos != null
                    && translateLrcLineInfos.size() > 0) {


                JSONObject lyricsObj = new JSONObject();
                JSONArray lyricContentArray = new JSONArray();
                lyricsObj.put("lyricType", 1);
                for (int i = 0; i < translateLrcLineInfos.size(); i++) {
                    JSONArray lyricArray = new JSONArray();
                    TranslateLrcLineInfo translateLrcLineInfo = translateLrcLineInfos
                            .get(i);
                    lyricArray.put(translateLrcLineInfo.getLineLyrics());
                    lyricContentArray.put(lyricArray);
                }
                if (lyricContentArray.length() > 0) {
                    lyricsObj.put("lyricContent", lyricContentArray);
                    contentArray.put(lyricsObj);
                }
            }
        }

        // 判断是否有音译歌词
        if (lyricsIfno.getTransliterationLyricsInfo() != null) {
            List<LyricsLineInfo> lyricsLineInfos = lyricsIfno
                    .getTransliterationLyricsInfo()
                    .getTransliterationLrcLineInfos();
            if (lyricsLineInfos != null && lyricsLineInfos.size() > 0) {

                JSONObject lyricsObj = new JSONObject();
                JSONArray lyricContentArray = new JSONArray();
                lyricsObj.put("lyricType", 0);
                for (int i = 0; i < lyricsLineInfos.size(); i++) {

                    LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);
                    String[] lyricsWords = lyricsLineInfo.getLyricsWords();
                    JSONArray lyricArray = new JSONArray();
                    for (int j = 0; j < lyricsWords.length; j++) {
                        lyricArray.put(lyricsWords[j].trim());
                    }
                    lyricContentArray.put(lyricArray);
                }
                if (lyricContentArray.length() > 0) {
                    lyricsObj.put("lyricContent", lyricContentArray);
                    contentArray.put(lyricsObj);
                }

            }
        }
        //

        extraLyricsObj.put("content", contentArray);

        // 添加翻译和音译歌词
        lyricsCom += LEGAL_EXTRA_LYRICS_PREFIX
                + "('"
                + Base64.encodeToString(extraLyricsObj.toString()
                .getBytes(), Base64.NO_WRAP) + "');\n";


        // 每行歌词内容
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsIfno
                .getLyricsLineInfoTreeMap();
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
                timeText += "<" + lyricsLineInfo.getStartTime() + ",";
                timeText += lyricsLineInfo.getEndTime() + ">";

                // 获取每个歌词的时间
                String wordsDisIntervalTextTemp = "";
                int wordsDisInterval[] = lyricsLineInfo.getWordsDisInterval();
                for (int j = 0; j < wordsDisInterval.length; j++) {
                    if (j == 0)
                        wordsDisIntervalTextTemp += wordsDisInterval[j] + "";
                    else
                        wordsDisIntervalTextTemp += "," + wordsDisInterval[j]
                                + "";
                }
                // 获取每个歌词时间的文本
                wordsDisIntervalText += "<" + wordsDisIntervalTextTemp + ">";
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
     * @param lrcComTxt 歌词文本
     * @return
     */
    private String getLineLyrics(String lrcComTxt) {
        String newLrc = "";
        Stack<String> lrcStack = new Stack<String>();
        String temp = "";
        for (int i = 0; i < lrcComTxt.length(); i++) {
            char c = lrcComTxt.charAt(i);
            if (CharUtils.isChinese(c) || CharUtils.isChinese(c) || CharUtils.isHangulSyllables(c)
                    || CharUtils.isHiragana(c)) {

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
    public boolean isFileSupported(String ext) {
        return false;
    }

    @Override
    public String getSupportFileExt() {
        return null;
    }
}
