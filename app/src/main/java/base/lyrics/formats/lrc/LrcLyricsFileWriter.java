package base.lyrics.formats.lrc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import base.lyrics.formats.LyricsFileWriter;
import base.lyrics.model.LyricsInfo;
import base.lyrics.model.LyricsLineInfo;
import base.lyrics.model.LyricsTag;
import base.lyrics.utils.TimeUtils;

/**
 * lrc歌词生成器
 * Created by zhangliangming on 2018-02-24.
 */

public class LrcLyricsFileWriter extends LyricsFileWriter {

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
     * 歌曲长度
     */
    private final static String LEGAL_TOTAL_PREFIX = "[total:";

    @Override
    public boolean writer(LyricsInfo lyricsIfno, String lyricsFilePath) throws Exception {
        String lyricsContent = getLyricsContent(lyricsIfno);
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
                lyricsCom.append(LEGAL_SONGNAME_PREFIX);
            } else if (entry.getKey().equals(LyricsTag.TAG_ARTIST)) {
                lyricsCom.append(LEGAL_SINGERNAME_PREFIX);
            } else if (entry.getKey().equals(LyricsTag.TAG_OFFSET)) {
                lyricsCom.append(LEGAL_OFFSET_PREFIX);
            } else if (entry.getKey().equals(LyricsTag.TAG_TOTAL)) {
                lyricsCom.append(LEGAL_TOTAL_PREFIX);
            } else {
                val = "[" + entry.getKey() + ":" + val;
            }
            lyricsCom.append(val + "]\n");
        }

        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsIfno
                .getLyricsLineInfoTreeMap();
        // 将每行歌词，放到有序的map，判断已重复的歌词
        LinkedHashMap<String, List<Integer>> lyricsLineInfoMapResult = new LinkedHashMap<String, List<Integer>>();
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);
            String saveLineLyrics = lyricsLineInfo.getLineLyrics();
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
            List<Integer> indexs = entry.getValue();
            // 当前行歌词文本
            String saveLineLyrics = entry.getKey();
            StringBuilder timeText = new StringBuilder();// 时间标签内容

            for (int i = 0; i < indexs.size(); i++) {
                int key = indexs.get(i);
                LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(key);
                // 获取开始时间
                timeText.append("[" + TimeUtils.parseMMSSFFString(lyricsLineInfo.getStartTime()) + "]");
            }
            lyricsCom.append(timeText.toString() + "");
            lyricsCom.append("" + saveLineLyrics + "\n");
        }
        return lyricsCom.toString();
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("lrc");
    }

    @Override
    public String getSupportFileExt() {
        return "lrc";
    }
}
