package base.lyrics.formats.hrc;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import base.lyrics.formats.LyricsFileReader;
import base.lyrics.model.LyricsInfo;
import base.lyrics.model.LyricsLineInfo;
import base.lyrics.model.LyricsTag;
import base.lyrics.model.TranslateLrcLineInfo;
import base.lyrics.utils.StringCompressUtils;
import base.lyrics.utils.StringUtils;

/**
 * @Description: hrc歌词解析，乐乐音乐的自定义歌词
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/12/25 16:40
 * @Throws:
 */
public class HrcLyricsFileReader extends LyricsFileReader {
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

    public HrcLyricsFileReader() {
    }

    @Override
    public LyricsInfo readInputStream(InputStream in) throws Exception {
        LyricsInfo lyricsIfno = new LyricsInfo();
        lyricsIfno.setLyricsFileExt(getSupportFileExt());
        if (in != null) {
            // 获取歌词文件里面的所有内容，并对文本内容进行解压
            String lyricsTextStr = StringCompressUtils.decompress(in,
                    getDefaultCharset());
            String[] lyricsTexts = lyricsTextStr.split("\n");
            // 这里面key为该行歌词的开始时间，方便后面排序
            SortedMap<Integer, LyricsLineInfo> lyricsLineInfosTemp = new TreeMap<Integer, LyricsLineInfo>();
            Map<String, Object> lyricsTags = new HashMap<String, Object>();
            for (int i = 0; i < lyricsTexts.length; i++) {

                // 解析歌词
                parserLineInfos(lyricsIfno, lyricsLineInfosTemp,
                        lyricsTags, lyricsTexts[i]);

            }
            in.close();
            in = null;
            // 重新封装
            TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
            int index = 0;
            Iterator<Integer> it = lyricsLineInfosTemp.keySet().iterator();
            while (it.hasNext()) {
                lyricsLineInfos
                        .put(index++, lyricsLineInfosTemp.get(it.next()));
            }
            it = null;
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
                                 Map<String, Object> lyricsTags, String lineInfo) throws Exception {
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
            int leftIndex = lineInfo.indexOf('\'');
            int rightIndex = lineInfo.lastIndexOf('\'');

            // 解析翻译歌词
            // 获取json base64字符串
            String translateJsonBase64String = lineInfo.substring(leftIndex + 1,
                    rightIndex);
            if (!translateJsonBase64String.equals("")) {
                String translateJsonString = new String(
                        Base64.decode(translateJsonBase64String, Base64.NO_WRAP));
                parserOtherLrc(lyricsIfno, translateJsonString);
            }

        } else if (lineInfo.startsWith(LEGAL_LYRICS_LINE_PREFIX)) {
            int leftIndex = lineInfo.indexOf('\'');
            int rightIndex = lineInfo.lastIndexOf('\'');

            String[] lineComments = lineInfo.substring(leftIndex + 1, rightIndex)
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
            int timeRight = timeText.lastIndexOf('>');
            timeText = timeText.substring(timeLeft + 1, timeRight);
            String[] timeTexts = timeText.split("><");

            // 每个歌词的时间标签
            String wordsDisIntervalText = lineComments[2];
            int wordsDisIntervalLeft = wordsDisIntervalText.indexOf('<');
            int wordsDisIntervalRight = wordsDisIntervalText.lastIndexOf('>');
            wordsDisIntervalText = wordsDisIntervalText.substring(wordsDisIntervalLeft + 1, wordsDisIntervalRight);
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

        JSONObject resultObj = new JSONObject(translateJsonString);
        JSONArray contentArrayObj = resultObj.getJSONArray("content");
        for (int i = 0; i < contentArrayObj.length(); i++) {
            JSONObject dataObj = contentArrayObj.getJSONObject(i);
            JSONArray lyricContentArrayObj = dataObj
                    .getJSONArray("lyricContent");
            int type = dataObj.getInt("lyricType");
            if (type == 1) {
                // 解析翻译歌词
                if (lyricsIfno.getTranslateLrcLineInfos() == null || lyricsIfno.getTranslateLrcLineInfos().size() == 0)
                    parserTranslateLrc(lyricsIfno, lyricContentArrayObj);

            } else if (type == 0) {
                // 解析音译歌词
                if (lyricsIfno.getTransliterationLrcLineInfos() == null || lyricsIfno.getTransliterationLrcLineInfos().size() == 0)
                    parserTransliterationLrc(lyricsIfno,
                            lyricContentArrayObj);
            }
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
        List<LyricsLineInfo> transliterationLrcLineInfos = new ArrayList<LyricsLineInfo>();
        // 获取歌词内容
        for (int j = 0; j < lyricContentArrayObj.length(); j++) {
            JSONArray lrcDataArrayObj = lyricContentArrayObj.getJSONArray(j);
            // 音译行歌词
            LyricsLineInfo transliterationLrcLineInfo = new LyricsLineInfo();
            String[] lyricsWords = new String[lrcDataArrayObj.length()];
            StringBuilder lineLyrics = new StringBuilder();
            for (int k = 0; k < lrcDataArrayObj.length(); k++) {
                if (k == lrcDataArrayObj.length() - 1) {
                    lyricsWords[k] = lrcDataArrayObj.getString(k).trim();
                } else {
                    lyricsWords[k] = lrcDataArrayObj.getString(k).trim() + " ";
                }
                lineLyrics.append(lyricsWords[k]);
            }
            transliterationLrcLineInfo.setLineLyrics(lineLyrics.toString());
            transliterationLrcLineInfo.setLyricsWords(lyricsWords);

            transliterationLrcLineInfos.add(transliterationLrcLineInfo);
        }
        // 添加音译歌词
        if (transliterationLrcLineInfos.size() > 0) {
            lyricsIfno.setTransliterationLrcLineInfos(transliterationLrcLineInfos);
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
            lyricsIfno.setTranslateLrcLineInfos(translateLrcLineInfos);
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
            String[] wordsDisIntervalTexts) throws Exception {
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
                int wordsDisInterval[] = getWordsDisIntervalString(wordsDisIntervalTexts[i]);

                //验证
                if (lyricsWords.length != wordsDisInterval.length) {
                    throw new Exception("字标签个数与字时间标签个数不相符");
                }

                lyricsLineInfo.setWordsDisInterval(wordsDisInterval);
                //
                lyricsLineInfos.put(startTime, lyricsLineInfo);
            }
        } else {
            throw new Exception("开始与结束的标签个数与行字分配时间的标签个数不相等");
        }
    }

    /**
     * 获取每个歌词的时间
     *
     * @param wordsDisIntervalString
     * @return
     */
    private int[] getWordsDisIntervalString(String wordsDisIntervalString) throws Exception {
        String[] wordsDisIntervalStr = wordsDisIntervalString.split(",");
        int wordsDisInterval[] = new int[wordsDisIntervalStr.length];
        for (int i = 0; i < wordsDisIntervalStr.length; i++) {
            String wordDisIntervalStr = wordsDisIntervalStr[i];
            if (StringUtils.isNumeric(wordDisIntervalStr))
                wordsDisInterval[i] = Integer.parseInt(wordDisIntervalStr);
            else throw new Exception("字时间标签不能含有非数字字符串");
        }
        return wordsDisInterval;
    }

    /**
     * 获取当前行歌词，去掉中括号
     *
     * @param lineLyricsStr
     * @return
     */
    private String getLineLyrics(String lineLyricsStr) throws Exception {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < lineLyricsStr.length(); i++) {
            char c = lineLyricsStr.charAt(i);
            switch (c) {
                case '<':
                    break;
                case '>':
                    break;
                default:
                    temp.append(c);
                    break;
            }
        }
        return temp.toString();
    }

    /**
     * 分隔每个歌词
     *
     * @param lineLyricsStr
     * @return
     */
    private String[] getLyricsWords(String lineLyricsStr) throws Exception {
        int startIndex = lineLyricsStr.indexOf("<");
        int endIndex = lineLyricsStr.lastIndexOf('>');
        lineLyricsStr = lineLyricsStr.substring(startIndex + 1, endIndex);
        String lineLyrics[] = lineLyricsStr.split("><");
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
