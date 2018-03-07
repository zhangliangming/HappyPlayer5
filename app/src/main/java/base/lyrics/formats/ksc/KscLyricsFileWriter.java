package base.lyrics.formats.ksc;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;

import base.lyrics.formats.LyricsFileWriter;
import base.lyrics.model.LyricsInfo;
import base.lyrics.model.LyricsLineInfo;
import base.lyrics.model.LyricsTag;
import base.lyrics.utils.TimeUtils;

/**
 * @Description: ksc歌词保存器
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/12/25 16:45
 * @Throws:
 */
public class KscLyricsFileWriter extends LyricsFileWriter {

    /**
     * 歌曲名 字符串
     */
    private final static String LEGAL_SONGNAME_PREFIX = "karaoke.songname";
    /**
     * 歌手名 字符串
     */
    private final static String LEGAL_SINGERNAME_PREFIX = "karaoke.singer";
    /**
     * 时间补偿值 字符串
     */
    private final static String LEGAL_OFFSET_PREFIX = "karaoke.offset";
    /**
     * 歌词 字符串
     */
    public final static String LEGAL_LYRICS_LINE_PREFIX = "karaoke.add";

    /**
     * 歌词Tag
     */
    public final static String LEGAL_TAG_PREFIX = "karaoke.tag";

    public KscLyricsFileWriter() {
        // 设置编码
        setDefaultCharset(Charset.forName("GB2312"));
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("ksc");
    }

    @Override
    public String getSupportFileExt() {
        return "ksc";
    }

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
            } else {
                lyricsCom.append(LEGAL_TAG_PREFIX);
                val = entry.getKey() + ":" + val;
            }
            lyricsCom.append(" := '" + val + "';\n");
        }
        // 每行歌词内容
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsIfno
                .getLyricsLineInfoTreeMap();
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);

            lyricsCom.append(LEGAL_LYRICS_LINE_PREFIX + "('"
                    + TimeUtils.parseMMSSFFFString(lyricsLineInfo.getStartTime())
                    + "',");// 添加开始时间
            lyricsCom.append("'"
                    + TimeUtils.parseMMSSFFFString(lyricsLineInfo.getEndTime()) + "',");// 添加结束时间

            // 获取歌词文本行
            String lyricsText = getLineLyrics(lyricsLineInfo.getLyricsWords());
            lyricsCom.append("'" + lyricsText + "',");// 解析文本歌词

            // 添加每个歌词的时间
            StringBuilder wordsDisIntervalText = new StringBuilder();
            int wordsDisInterval[] = lyricsLineInfo.getWordsDisInterval();
            for (int j = 0; j < wordsDisInterval.length; j++) {
                if (j == 0)
                    wordsDisIntervalText.append(wordsDisInterval[j] + "");
                else
                    wordsDisIntervalText.append("," + wordsDisInterval[j] + "");
            }
            lyricsCom.append("'" + wordsDisIntervalText.toString() + "');\n");
        }
        return lyricsCom.toString();
    }

    /**
     * 获取当行歌词(每个字添加[]是因为存在部分krc歌词转换成ksc歌词时，一个字时间标签对应几个歌词，这样子在ksc在解析时，会导致字时间标签与字标签的个数不对应，出错的问题，这是ksc歌词格式存在的问题)
     *
     * @param lyricsWords
     * @return
     */
    private String getLineLyrics(String[] lyricsWords) throws Exception {
        StringBuilder lrcText = new StringBuilder();
        for (int i = 0; i < lyricsWords.length; i++) {
            lrcText.append("[" + lyricsWords[i] + "]");
        }
        return lrcText.toString();
    }
}
