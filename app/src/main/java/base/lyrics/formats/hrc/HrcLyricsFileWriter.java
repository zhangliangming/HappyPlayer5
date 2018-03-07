package base.lyrics.formats.hrc;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import base.lyrics.formats.LyricsFileWriter;
import base.lyrics.model.LyricsInfo;
import base.lyrics.model.LyricsLineInfo;
import base.lyrics.model.LyricsTag;
import base.lyrics.model.TranslateLrcLineInfo;
import base.lyrics.utils.StringCompressUtils;

/**
 * @Description: hrc歌词
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/12/25 17:32
 * @Throws:
 */

public class HrcLyricsFileWriter extends LyricsFileWriter {

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

    public HrcLyricsFileWriter() {
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
        StringBuilder lyricsCom = new StringBuilder();
        // 先保存所有的标签数据
        Map<String, Object> tags = lyricsIfno.getLyricsTags();
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            Object val = entry.getValue();
            if (entry.getKey().equals(LyricsTag.TAG_TITLE)) {
                lyricsCom.append(LEGAL_TITLE_PREFIX);
            } else if (entry.getKey().equals(LyricsTag.TAG_ARTIST)) {
                lyricsCom.append(LEGAL_ARTIST_PREFIX);
            } else if (entry.getKey().equals(LyricsTag.TAG_OFFSET)) {
                lyricsCom.append(LEGAL_OFFSET_PREFIX);
            } else if (entry.getKey().equals(LyricsTag.TAG_BY)) {
                lyricsCom.append(LEGAL_BY_PREFIX);
            } else if (entry.getKey().equals(LyricsTag.TAG_TOTAL)) {
                lyricsCom.append(LEGAL_TOTAL_PREFIX);
            } else {
                lyricsCom.append(LEGAL_TAG_PREFIX + entry.getKey() + ":");
            }
            lyricsCom.append(val + "];\n");
        }

        // 获取额外歌词行（翻译歌词和音译歌词）
        JSONObject extraLyricsObj = new JSONObject();
        JSONArray contentArray = new JSONArray();
        // 判断是否有翻译歌词
        if (lyricsIfno.getTranslateLrcLineInfos() != null && lyricsIfno.getTranslateLrcLineInfos().size() != 0) {
            List<TranslateLrcLineInfo> translateLrcLineInfos = lyricsIfno.getTranslateLrcLineInfos();
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
        if (lyricsIfno.getTransliterationLrcLineInfos() != null && lyricsIfno.getTransliterationLrcLineInfos().size() != 0) {
            List<LyricsLineInfo> lyricsLineInfos = lyricsIfno.getTransliterationLrcLineInfos();
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
        lyricsCom.append(LEGAL_EXTRA_LYRICS_PREFIX
                + "('"
                + Base64.encodeToString(extraLyricsObj.toString()
                .getBytes(), Base64.NO_WRAP) + "');\n");

        // 每行歌词内容
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsIfno
                .getLyricsLineInfoTreeMap();
        // 将每行歌词，放到有序的map，判断已重复的歌词
        LinkedHashMap<String, List<Integer>> lyricsLineInfoMapResult = new LinkedHashMap<String, List<Integer>>();
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);
            String saveLineLyrics = getSaveLineLyrics(lyricsLineInfo
                    .getLyricsWords());
            List<Integer> indexs = null;
            // 如果已存在该行歌词，则往里面添加歌词行索引
            if (lyricsLineInfoMapResult.containsKey(saveLineLyrics)) {
                indexs = lyricsLineInfoMapResult.get(saveLineLyrics);
            } else {
                indexs = new ArrayList<Integer>();
            }
            indexs.add(i);
            lyricsLineInfoMapResult.put(saveLineLyrics, indexs);
        }
        // 遍历
        for (Map.Entry<String, List<Integer>> entry : lyricsLineInfoMapResult
                .entrySet()) {
            lyricsCom.append(LEGAL_LYRICS_LINE_PREFIX + "('");
            List<Integer> indexs = entry.getValue();
            // 当前行歌词文本
            String saveLineLyrics = entry.getKey();
            StringBuilder timeText = new StringBuilder();// 时间标签内容
            StringBuilder wordsDisIntervalText = new StringBuilder();// 每个歌词时间

            for (int i = 0; i < indexs.size(); i++) {
                int key = indexs.get(i);
                LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(key);
                // 获取开始时间和结束时间
                timeText.append("<" + lyricsLineInfo.getStartTime() + ",");
                timeText.append(lyricsLineInfo.getEndTime() + ">");

                // 获取每个歌词的时间
                StringBuilder wordsDisIntervalTextTemp = new StringBuilder();
                int wordsDisInterval[] = lyricsLineInfo.getWordsDisInterval();
                for (int j = 0; j < wordsDisInterval.length; j++) {
                    if (j == 0)
                        wordsDisIntervalTextTemp.append(wordsDisInterval[j] + "");
                    else
                        wordsDisIntervalTextTemp.append("," + wordsDisInterval[j]
                                + "");
                }
                // 获取每个歌词时间的文本
                wordsDisIntervalText.append("<" + wordsDisIntervalTextTemp.toString() + ">");
            }
            lyricsCom.append(timeText.toString() + "'");
            lyricsCom.append(",'" + saveLineLyrics + "'");
            lyricsCom.append(",'" + wordsDisIntervalText.toString() + "');\n");
        }
        return lyricsCom.toString();
    }

    /**
     * 获取要保存的行歌词内容
     *
     * @param lyricsWords
     * @return
     */
    private String getSaveLineLyrics(String[] lyricsWords) {
        StringBuilder saveLineLyrics = new StringBuilder();
        for (int i = 0; i < lyricsWords.length; i++) {
            saveLineLyrics.append("<" + lyricsWords[i] + ">");
        }
        return saveLineLyrics.toString();
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
