package com.zlm.hp.lyrics.formats.hrcs;

import android.util.Base64;


import com.zlm.hp.lyrics.LyricsFileReader;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.model.LyricsTag;
import com.zlm.hp.lyrics.model.TranslateLrcLineInfo;
import com.zlm.hp.lyrics.model.TranslateLyricsInfo;
import com.zlm.hp.lyrics.model.TransliterationLyricsInfo;
import com.zlm.hp.lyrics.utils.StringCompressUtils;
import com.zlm.hp.lyrics.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @Description: hrcs歌词解析，乐乐音乐第三版的自定义歌词
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/12/25 16:40
 * @Throws:
 */
public class HrcsLyricsFileReader extends LyricsFileReader {
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

    public HrcsLyricsFileReader() {
    }

    @Override
    public LyricsInfo readInputStream(InputStream in) throws Exception {
        LyricsInfo lyricsIfno = new LyricsInfo();
        lyricsIfno.setLyricsFileExt(getSupportFileExt());
        if (in != null) {
            // 获取歌词文件里面的所有内容，并对文本内容进行解压
            String lyricsTextStr = StringCompressUtils.decompress(in,
                    getDefaultCharset());
            // System.out.println(lyricsTextStr);
            String[] lyricsTexts = lyricsTextStr.split("\n");
            // 这里面key为该行歌词的开始时间，方便后面排序
            SortedMap<Integer, LyricsLineInfo> lyricsLineInfosTemp = new TreeMap<Integer, LyricsLineInfo>();
            Map<String, Object> lyricsTags = new HashMap<String, Object>();
            for (int i = 0; i < lyricsTexts.length; i++) {
                try {
                    // 解析歌词
                    parserLineInfos(lyricsIfno, lyricsLineInfosTemp,
                            lyricsTags, lyricsTexts[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            in.close();
            // 重新封装
            TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
            int index = 0;
            Iterator<Integer> it = lyricsLineInfosTemp.keySet().iterator();
            while (it.hasNext()) {
                lyricsLineInfos
                        .put(index++, lyricsLineInfosTemp.get(it.next()));
            }
            // 设置歌词的标签类
            lyricsIfno.setLyricsTags(lyricsTags);
            //
            lyricsIfno.setLyricsLineInfoTreeMap(lyricsLineInfos);
        }
        return lyricsIfno;
    }

    /**
     * 解析每行的歌词
     *
     * @param lyricsLineInfos
     * @param lyricsTags
     * @param lineInfo
     */
    private void parserLineInfos(LyricsInfo lyricsIfno,
                                 SortedMap<Integer, LyricsLineInfo> lyricsLineInfos,
                                 Map<String, Object> lyricsTags, String lineInfo) {
        if (lineInfo.startsWith(LEGAL_TITLE_PREFIX)) {

            int start = LEGAL_TITLE_PREFIX.length();
            int end = lineInfo.lastIndexOf("]");
            String tagValue = lineInfo.substring(start, end);
            lyricsTags.put(LyricsTag.TAG_TITLE, tagValue);

        } else if (lineInfo.startsWith(LEGAL_ARTIST_PREFIX)) {

            int start = LEGAL_ARTIST_PREFIX.length();
            int end = lineInfo.lastIndexOf("]");
            String tagValue = lineInfo.substring(start, end);

            lyricsTags.put(LyricsTag.TAG_ARTIST, tagValue);

        } else if (lineInfo.startsWith(LEGAL_OFFSET_PREFIX)) {

            int start = LEGAL_OFFSET_PREFIX.length();
            int end = lineInfo.lastIndexOf("]");
            String tagValue = lineInfo.substring(start, end);
            lyricsTags.put(LyricsTag.TAG_OFFSET, tagValue);

        } else if (lineInfo.startsWith(LEGAL_BY_PREFIX)) {

            int start = LEGAL_BY_PREFIX.length();
            int end = lineInfo.lastIndexOf("]");
            String tagValue = lineInfo.substring(start, end);
            lyricsTags.put(LyricsTag.TAG_BY, tagValue);

        } else if (lineInfo.startsWith(LEGAL_TOTAL_PREFIX)) {

            int start = LEGAL_TOTAL_PREFIX.length();
            int end = lineInfo.lastIndexOf("]");
            String tagValue = lineInfo.substring(start, end);
            lyricsTags.put(LyricsTag.TAG_TOTAL, tagValue);

        } else if (lineInfo.startsWith(LEGAL_TAG_PREFIX)) {

            int start = LEGAL_TAG_PREFIX.length();
            int end = lineInfo.lastIndexOf("]");
            String tagValue = lineInfo.substring(start, end);
            String temp[] = tagValue.split(":");
            lyricsTags.put(temp[0], temp.length == 1 ? "" : temp[1]);

        } else if (lineInfo.startsWith(LEGAL_EXTRA_LYRICS_PREFIX)) {
            int left = LEGAL_EXTRA_LYRICS_PREFIX.length() + 1;
            int right = lineInfo.length();

            // 解析翻译歌词
            // 获取json base64字符串
            String translateJsonBase64String = lineInfo.substring(left + 1,
                    right - 3);
            if (!translateJsonBase64String.equals("")) {
                try {
                    //
                    String translateJsonString = new String(
                            Base64.decode(translateJsonBase64String, Base64.NO_WRAP));
                    parserOtherLrc(lyricsIfno, translateJsonString);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        } else if (lineInfo.startsWith(LEGAL_LYRICS_LINE_PREFIX)) {
            int left = LEGAL_LYRICS_LINE_PREFIX.length() + 1;
            int right = lineInfo.length();
            String[] lineComments = lineInfo.substring(left + 1, right - 3)
                    .split("'\\s*,\\s*'", -1);
            // 歌词
            String lineLyricsStr = lineComments[1];

            // 歌词分隔
            String[] lyricsWords = getLyricsWords(lineLyricsStr);

            // 获取当行歌词
            String lineLyrics = getLineLyrics(lineLyricsStr);

            // 时间标签
            String timeText = lineComments[0];
            int timeLeft = timeText.indexOf('<');
            int timeRight = timeText.length();
            timeText = timeText.substring(timeLeft + 1, timeRight - 1);
            String[] timeTexts = timeText.split("><");

            // 每个歌词的时间标签
            String wordsDisIntervalText = lineComments[2];
            String[] wordsDisIntervalTexts = wordsDisIntervalText.split("><");

            parserLineInfos(lyricsLineInfos, lyricsWords, lineLyrics,
                    timeTexts, wordsDisIntervalTexts);
        }

    }

    /**
     * 解析翻译和音译歌词
     *
     * @param lyricsIfno
     * @param translateJsonString
     */
    private void parserOtherLrc(LyricsInfo lyricsIfno,
                                String translateJsonString) throws Exception {

        try {

            JSONObject resultObj = new JSONObject(translateJsonString);
            JSONArray contentArrayObj = resultObj.getJSONArray("content");
            for (int i = 0; i < contentArrayObj.length(); i++) {
                JSONObject dataObj = contentArrayObj.getJSONObject(i);
                JSONArray lyricContentArrayObj = dataObj
                        .getJSONArray("lyricContent");
                int type = dataObj.getInt("lyricType");
                if (type == 1) {
                    // 解析翻译歌词
                    if (lyricsIfno.getTranslateLyricsInfo() == null)
                        parserTranslateLrc(lyricsIfno, lyricContentArrayObj);

                } else if (type == 0) {
                    // 解析音译歌词
                    if (lyricsIfno.getTransliterationLyricsInfo() == null)
                        parserTransliterationLrc(lyricsIfno,
                                lyricContentArrayObj);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析音译歌词
     *
     * @param lyricsIfno
     * @param lyricContentArrayObj
     */
    private void parserTransliterationLrc(LyricsInfo lyricsIfno,
                                          JSONArray lyricContentArrayObj) throws Exception {

        // 音译歌词集合
        TransliterationLyricsInfo transliterationLyricsInfo = new TransliterationLyricsInfo();
        List<LyricsLineInfo> transliterationLrcLineInfos = new ArrayList<LyricsLineInfo>();
        // 获取歌词内容
        for (int j = 0; j < lyricContentArrayObj.length(); j++) {
            JSONArray lrcDataArrayObj = lyricContentArrayObj.getJSONArray(j);
            // 音译行歌词
            LyricsLineInfo transliterationLrcLineInfo = new LyricsLineInfo();
            String[] lyricsWords = new String[lrcDataArrayObj.length()];
            String lineLyrics = "";
            for (int k = 0; k < lrcDataArrayObj.length(); k++) {
                if (k == lrcDataArrayObj.length() - 1) {
                    lyricsWords[k] = lrcDataArrayObj.getString(k).trim();
                    lineLyrics += lrcDataArrayObj.getString(k).trim();
                } else {
                    lyricsWords[k] = lrcDataArrayObj.getString(k).trim() + " ";
                    lineLyrics += lrcDataArrayObj.getString(k).trim() + " ";
                }
            }
            transliterationLrcLineInfo.setLineLyrics(lineLyrics);
            transliterationLrcLineInfo.setLyricsWords(lyricsWords);

            transliterationLrcLineInfos.add(transliterationLrcLineInfo);
        }
        // 添加音译歌词
        if (transliterationLrcLineInfos.size() > 0) {
            transliterationLyricsInfo
                    .setTransliterationLrcLineInfos(transliterationLrcLineInfos);
            lyricsIfno.setTransliterationLyricsInfo(transliterationLyricsInfo);
        }
    }

    /**
     * 解析翻译歌词
     *
     * @param lyricsIfno
     * @param lyricContentArrayObj
     */
    private void parserTranslateLrc(LyricsInfo lyricsIfno,
                                    JSONArray lyricContentArrayObj) throws Exception {

        // 翻译歌词集合
        TranslateLyricsInfo translateLyricsInfo = new TranslateLyricsInfo();
        List<TranslateLrcLineInfo> translateLrcLineInfos = new ArrayList<TranslateLrcLineInfo>();

        // 获取歌词内容
        for (int j = 0; j < lyricContentArrayObj.length(); j++) {
            JSONArray lrcDataArrayObj = lyricContentArrayObj.getJSONArray(j);
            String lrcComtext = lrcDataArrayObj.getString(0);

            // 翻译行歌词
            TranslateLrcLineInfo translateLrcLineInfo = new TranslateLrcLineInfo();
            translateLrcLineInfo.setLineLyrics(lrcComtext);

            translateLrcLineInfos.add(translateLrcLineInfo);
        }
        // 添加翻译歌词
        if (translateLrcLineInfos.size() > 0) {
            translateLyricsInfo.setTranslateLrcLineInfos(translateLrcLineInfos);
            lyricsIfno.setTranslateLyricsInfo(translateLyricsInfo);
        }
    }

    /**
     * 解析每行歌词的数据
     *
     * @param lyricsLineInfos
     * @param lyricsWords           歌词
     * @param lineLyrics            该行歌词
     * @param timeTexts             时间文本
     * @param wordsDisIntervalTexts
     */
    private void parserLineInfos(
            SortedMap<Integer, LyricsLineInfo> lyricsLineInfos,
            String[] lyricsWords, String lineLyrics, String[] timeTexts,
            String[] wordsDisIntervalTexts) {
        if (timeTexts.length == wordsDisIntervalTexts.length) {
            for (int i = 0; i < wordsDisIntervalTexts.length; i++) {

                LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();

                // 每一行的开始时间和结束时间
                String timeTextStr = timeTexts[i];
                String[] timeTextCom = timeTextStr.split(",");

                String startTimeStr = timeTextCom[0];
                int startTime = Integer.parseInt(startTimeStr);

                String endTimeStr = timeTextCom[1];
                int endTime = Integer.parseInt(endTimeStr);

                lyricsLineInfo.setEndTime(endTime);
                lyricsLineInfo.setStartTime(startTime);

                //
                lyricsLineInfo.setLineLyrics(lineLyrics);
                lyricsLineInfo.setLyricsWords(lyricsWords);

                // 每一行歌词的每个时间
                String wordsDisIntervalStr = wordsDisIntervalTexts[i];
                List<String> wordsDisIntervalList = getWordsDisIntervalList(wordsDisIntervalStr);
                int wordsDisInterval[] = getWordsDisIntervalList(wordsDisIntervalList);
                lyricsLineInfo.setWordsDisInterval(wordsDisInterval);
                //
                lyricsLineInfos.put(startTime, lyricsLineInfo);
            }
        }
    }

    /**
     * 获取每个歌词的时间
     *
     * @param wordsDisIntervalString
     * @return
     */
    private List<String> getWordsDisIntervalList(String wordsDisIntervalString) {
        List<String> wordsDisIntervalList = new ArrayList<String>();
        String temp = "";
        for (int i = 0; i < wordsDisIntervalString.length(); i++) {
            char c = wordsDisIntervalString.charAt(i);
            switch (c) {
                case ',':
                    wordsDisIntervalList.add(temp);
                    temp = "";
                    break;
                default:
                    // 判断是否是数字
                    if (Character.isDigit(c)) {
                        temp += String.valueOf(wordsDisIntervalString.charAt(i));
                    }
                    break;
            }
        }
        if (!temp.equals("")) {
            wordsDisIntervalList.add(temp);
        }
        return wordsDisIntervalList;
    }

    /**
     * 获取每个歌词的时间
     *
     * @param wordsDisIntervalList
     * @return
     */
    private int[] getWordsDisIntervalList(List<String> wordsDisIntervalList) {
        int wordsDisInterval[] = new int[wordsDisIntervalList.size()];
        for (int i = 0; i < wordsDisIntervalList.size(); i++) {
            String wordDisIntervalStr = wordsDisIntervalList.get(i);
            if (StringUtils.isNumeric(wordDisIntervalStr)) {
                wordsDisInterval[i] = Integer.parseInt(wordDisIntervalStr);
            }
        }
        return wordsDisInterval;
    }

    /**
     * 获取当前行歌词，去掉中括号
     *
     * @param lineLyricsStr
     * @return
     */
    private String getLineLyrics(String lineLyricsStr) {
        String temp = "";
        for (int i = 0; i < lineLyricsStr.length(); i++) {
            switch (lineLyricsStr.charAt(i)) {
                case '<':
                    break;
                case '>':
                    break;
                default:
                    temp += String.valueOf(lineLyricsStr.charAt(i));
                    break;
            }
        }
        return temp;
    }

    /**
     * 分隔每个歌词
     *
     * @param lineLyricsStr
     * @return
     */
    private String[] getLyricsWords(String lineLyricsStr) {
        int startIndex = lineLyricsStr.indexOf("<");
        String lineLyrics[] = lineLyricsStr.substring(startIndex + 1,
                lineLyricsStr.length() - 1).split("><");
        return lineLyrics;
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("hrcs");
    }

    @Override
    public String getSupportFileExt() {
        return "hrcs";
    }
}
