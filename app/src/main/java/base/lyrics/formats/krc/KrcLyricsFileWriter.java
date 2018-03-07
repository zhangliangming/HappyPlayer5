package base.lyrics.formats.krc;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
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
 * @Description: krc歌词生成器, 不是生成官方的歌词文件
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/12/25 17:00
 * @Throws:
 */

public class KrcLyricsFileWriter extends LyricsFileWriter {
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
     * 额外歌词字符串
     */
    private final static String LEGAL_LANGUAGE_PREFIX = "[language:";

    private final static String LEGAL_TOTAL_PREFIX = "[total:";

    /**
     * 解码参数
     */
    private static final char[] key = {'@', 'G', 'a', 'w', '^', '2', 't', 'G',
            'Q', '6', '1', '-', 'Î', 'Ò', 'n', 'i'};

    public KrcLyricsFileWriter() {
    }

    @Override
    public boolean writer(LyricsInfo lyricsIfno, String lyricsFilePath) throws Exception {
        File lyricsFile = new File(lyricsFilePath);
        if (lyricsFile != null) {
            //
            if (lyricsFile.getParentFile().isDirectory() && !lyricsFile.getParentFile().exists()) {
                lyricsFile.getParentFile().mkdirs();
            }

            //
            // 对字符串运行压缩
            byte[] content = StringCompressUtils.compress(
                    getLyricsContent(lyricsIfno), getDefaultCharset());

            int j = content.length;
            for (int k = 0; k < j; k++) {
                int l = k % 16;
                int tmp67_65 = k;
                byte[] tmp67_64 = content;
                tmp67_64[tmp67_65] = (byte) (tmp67_64[tmp67_65] ^ key[l]);
            }
            String topText = "krc1";
            byte[] top = new byte[4];
            for (int i = 0; i < topText.length(); i++) {
                top[i] = (byte) topText.charAt(i);
            }

            // 生成歌词文件
            FileOutputStream os = new FileOutputStream(lyricsFile);
            os.write(top);
            os.write(content);
            os.close();
            os = null;

            return true;
        }
        return false;
    }

    @Override
    public String getLyricsContent(LyricsInfo lyricsIfno) throws Exception {
        StringBuilder lyricsCom = new StringBuilder();
        // 先保存所有的标签数据
        Map<String, Object> tags = lyricsIfno.getLyricsTags();
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            Object val = entry.getValue();
            if (entry.getKey().equals(LyricsTag.TAG_TITLE)) {
                lyricsCom.append(LEGAL_SONGNAME_PREFIX);
            } else if (entry.getKey().equals(LyricsTag.TAG_ARTIST)) {
                lyricsCom.append(LEGAL_SINGERNAME_PREFIX);
            } else if (entry.getKey().equals(LyricsTag.TAG_OFFSET)) {
                lyricsCom.append(LEGAL_OFFSET_PREFIX);
            }  else if (entry.getKey().equals(LyricsTag.TAG_TOTAL)) {
                lyricsCom.append(LEGAL_TOTAL_PREFIX);
            }else {
                val = "[" + entry.getKey() + ":" + val;
            }
            lyricsCom.append(val + "]\n");
        }

        JSONObject extraLyricsObj = new JSONObject();
        JSONArray contentArray = new JSONArray();
        // 判断是否有翻译歌词
        if (lyricsIfno.getTranslateLrcLineInfos() != null && lyricsIfno.getTranslateLrcLineInfos().size() != 0) {
            List<TranslateLrcLineInfo> translateLrcLineInfos = lyricsIfno.getTranslateLrcLineInfos();
            if (translateLrcLineInfos != null
                    && translateLrcLineInfos.size() > 0) {
                JSONObject lyricsObj = new JSONObject();
                JSONArray lyricContentArray = new JSONArray();
                lyricsObj.put("language", 0);
                lyricsObj.put("type", 1);
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
                lyricsObj.put("language", 0);
                lyricsObj.put("type", 0);
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
        lyricsCom.append(LEGAL_LANGUAGE_PREFIX
                + Base64.encodeToString(extraLyricsObj.toString()
                .getBytes(), Base64.NO_WRAP) + "]\n");

        // [1679,1550]<0,399,0>作<399,200,0>词<599,250,0>：<849,301,0>李<1150,400,0>健
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsIfno
                .getLyricsLineInfoTreeMap();
        // 每行歌词内容
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);
            //
            int startTime = lyricsLineInfo.getStartTime();
            int endTime = lyricsLineInfo.getEndTime();
            lyricsCom.append("[" + startTime + "," + (endTime - startTime) + "]");
            //
            String[] lyricsWords = lyricsLineInfo.getLyricsWords();
            int wordsDisInterval[] = lyricsLineInfo.getWordsDisInterval();
            int lastTime = 0;
            for (int j = 0; j < wordsDisInterval.length; j++) {
                if (j == 0) {
                    lyricsCom.append("<0," + wordsDisInterval[j] + ",0>"
                            + lyricsWords[j]);
                } else {
                    lyricsCom.append("<" + lastTime + "," + wordsDisInterval[j]
                            + ",0>" + lyricsWords[j]);
                }
                lastTime = wordsDisInterval[j];
            }
            lyricsCom.append("\n");
        }

        return lyricsCom.toString();
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
