package com.zlm.hp.lyrics.formats.krc;

import android.util.Base64;

import com.zlm.hp.lyrics.LyricsFileReader;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.model.LyricsTag;
import com.zlm.hp.lyrics.model.TranslateLrcLineInfo;
import com.zlm.hp.lyrics.model.TranslateLyricsInfo;
import com.zlm.hp.lyrics.model.TransliterationLyricsInfo;
import com.zlm.hp.lyrics.utils.StringCompressUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: krcs歌词读取器
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/12/25 16:29
 * @Throws:
 */

public class KrcLyricsFileReader extends LyricsFileReader {

    /**
     * 歌曲名 字符串
     */
    private final static String LEGAL_SONGNAME_PREFIX = "[ti:";
    /**
     * 歌手名 字符串
     */
    private final static String LEGAL_SINGERNAME_PREFIX = "[ar:";
    /**
     * 时间补偿值 字符串
     */
    private final static String LEGAL_OFFSET_PREFIX = "[offset:";
    /**
     * 歌词上传者
     */
    private final static String LEGAL_BY_PREFIX = "[by:";
    private final static String LEGAL_HASH_PREFIX = "[hash:";
    private final static String LEGAL_AL_PREFIX = "[al:";
    private final static String LEGAL_SIGN_PREFIX = "[sign:";
    private final static String LEGAL_QQ_PREFIX = "[qq:";
    private final static String LEGAL_TOTAL_PREFIX = "[total:";
    private final static String LEGAL_LANGUAGE_PREFIX = "[language:";
    /**
     * 解码参数
     */
    private static final char[] key = {'@', 'G', 'a', 'w', '^', '2', 't', 'G',
            'Q', '6', '1', '-', 'Î', 'Ò', 'n', 'i'};

    public KrcLyricsFileReader() {
    }

    @Override
    public LyricsInfo readInputStream(InputStream in) throws Exception {
        LyricsInfo lyricsIfno = new LyricsInfo();
        lyricsIfno.setLyricsFileExt(getSupportFileExt());
        if (in != null) {
            byte[] zip_byte = new byte[in.available()];
            byte[] top = new byte[4];
            in.read(top);
            in.read(zip_byte);
            int j = zip_byte.length;
            for (int k = 0; k < j; k++) {
                int l = k % 16;
                int tmp67_65 = k;
                byte[] tmp67_64 = zip_byte;
                tmp67_64[tmp67_65] = (byte) (tmp67_64[tmp67_65] ^ key[l]);
            }
            String lyricsTextStr = StringCompressUtils.decompress(zip_byte,
                    getDefaultCharset());
            String[] lyricsTexts = lyricsTextStr.split("\n");
            TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
            Map<String, Object> lyricsTags = new HashMap<String, Object>();
            int index = 0;

            for (int i = 0; i < lyricsTexts.length; i++) {
                String lineInfo = lyricsTexts[i];
                try {
                    // 行读取，并解析每行歌词的内容
                    LyricsLineInfo lyricsLineInfo = parserLineInfos(lyricsTags,
                            lineInfo, lyricsIfno);
                    if (lyricsLineInfo != null) {
                        lyricsLineInfos.put(index, lyricsLineInfo);
                        index++;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            in.close();

            // 设置歌词的标签类
            lyricsIfno.setLyricsTags(lyricsTags);
            //
            lyricsIfno.setLyricsLineInfoTreeMap(lyricsLineInfos);
        }
        return lyricsIfno;
    }

    /**
     * 解析歌词
     *
     * @param lyricsTags
     * @param lineInfo
     * @param lyricsIfno
     * @return
     */
    private LyricsLineInfo parserLineInfos(Map<String, Object> lyricsTags,
                                           String lineInfo, LyricsInfo lyricsIfno) {
        LyricsLineInfo lyricsLineInfo = null;
        if (lineInfo.startsWith(LEGAL_SONGNAME_PREFIX)) {
            int startIndex = LEGAL_SONGNAME_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            //
            lyricsTags.put(LyricsTag.TAG_TITLE,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_SINGERNAME_PREFIX)) {
            int startIndex = LEGAL_SINGERNAME_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            lyricsTags.put(LyricsTag.TAG_ARTIST,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_OFFSET_PREFIX)) {
            int startIndex = LEGAL_OFFSET_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            lyricsTags.put(LyricsTag.TAG_OFFSET,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_BY_PREFIX)
                || lineInfo.startsWith(LEGAL_HASH_PREFIX)
                || lineInfo.startsWith(LEGAL_SIGN_PREFIX)
                || lineInfo.startsWith(LEGAL_QQ_PREFIX)
                || lineInfo.startsWith(LEGAL_TOTAL_PREFIX)
                || lineInfo.startsWith(LEGAL_AL_PREFIX)) {

            int startIndex = lineInfo.indexOf("[") + 1;
            int endIndex = lineInfo.lastIndexOf("]");
            String temp[] = lineInfo.substring(startIndex, endIndex).split(":");
            lyricsTags.put(temp[0], temp.length == 1 ? "" : temp[1]);

        } else if (lineInfo.startsWith(LEGAL_LANGUAGE_PREFIX)) {
            int startIndex = lineInfo.indexOf("[") + 1;
            int endIndex = lineInfo.lastIndexOf("]");
            String temp[] = lineInfo.substring(startIndex, endIndex).split(":");
            // 解析翻译歌词
            // 获取json base64字符串
            String translateJsonBase64String = temp.length == 1 ? "" : temp[1];
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
        } else {
            // 匹配歌词行
            Pattern pattern = Pattern.compile("\\[\\d+,\\d+\\]");
            Matcher matcher = pattern.matcher(lineInfo);
            if (matcher.find()) {
                lyricsLineInfo = new LyricsLineInfo();
                // [此行开始时刻距0时刻的毫秒数,此行持续的毫秒数]<0,此字持续的毫秒数,0>歌<此字开始的时刻距此行开始时刻的毫秒数,此字持续的毫秒数,0>词<此字开始的时刻距此行开始时刻的毫秒数,此字持续的毫秒数,0>正<此字开始的时刻距此行开始时刻的毫秒数,此字持续的毫秒数,0>文
                // 获取行的出现时间和结束时间
                int mStartIndex = matcher.start();
                int mEndIndex = matcher.end();
                String lineTime[] = lineInfo.substring(mStartIndex + 1,
                        mEndIndex - 1).split(",");
                //

                int startTime = Integer.parseInt(lineTime[0]);
                int endTime = startTime + Integer.parseInt(lineTime[1]);
                lyricsLineInfo.setEndTime(endTime);
                lyricsLineInfo.setStartTime(startTime);
                // 获取歌词信息
                String lineContent = lineInfo.substring(mEndIndex,
                        lineInfo.length());

                // 歌词匹配的正则表达式
                String regex = "\\<\\d+,\\d+,\\d+\\>";
                Pattern lyricsWordsPattern = Pattern.compile(regex);
                Matcher lyricsWordsMatcher = lyricsWordsPattern
                        .matcher(lineContent);

                // 歌词分隔
                String lineLyricsTemp[] = lineContent.split(regex);
                String[] lyricsWords = getLyricsWords(lineLyricsTemp);
                lyricsLineInfo.setLyricsWords(lyricsWords);

                // 获取每个歌词的时间
                int wordsDisInterval[] = new int[lyricsWords.length];
                int index = 0;
                while (lyricsWordsMatcher.find()) {
                    //
                    String wordsDisIntervalStr = lyricsWordsMatcher.group();
                    String wordsDisIntervalStrTemp = wordsDisIntervalStr
                            .substring(1, wordsDisIntervalStr.length() - 1);
                    String wordsDisIntervalTemp[] = wordsDisIntervalStrTemp
                            .split(",");
                    wordsDisInterval[index++] = Integer
                            .parseInt(wordsDisIntervalTemp[1]);
                }
                lyricsLineInfo.setWordsDisInterval(wordsDisInterval);

                // 获取当行歌词
                String lineLyrics = lyricsWordsMatcher.replaceAll("");
                lyricsLineInfo.setLineLyrics(lineLyrics);
            }

        }
        return lyricsLineInfo;
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
                int type = dataObj.getInt("type");
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
     * 分隔每个歌词
     *
     * @param lineLyricsTemp
     * @return
     */
    private String[] getLyricsWords(String[] lineLyricsTemp) {
        String temp[] = null;
        if (lineLyricsTemp.length < 2) {
            return new String[lineLyricsTemp.length];
        }
        //
        temp = new String[lineLyricsTemp.length - 1];
        for (int i = 1; i < lineLyricsTemp.length; i++) {
            temp[i - 1] = lineLyricsTemp[i];
        }
        return temp;
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("krc");
    }

    @Override
    public String getSupportFileExt() {
        return "krc";
    }

}
