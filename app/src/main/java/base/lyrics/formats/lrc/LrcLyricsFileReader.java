package base.lyrics.formats.lrc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.lyrics.formats.LyricsFileReader;
import base.lyrics.model.LyricsInfo;
import base.lyrics.model.LyricsLineInfo;
import base.lyrics.model.LyricsTag;
import base.lyrics.utils.TimeUtils;

/**
 * lrc歌词解析器
 * Created by zhangliangming on 2018-02-24.
 */

public class LrcLyricsFileReader extends LyricsFileReader {

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

    /**
     * 专辑
     */
    private final static String LEGAL_AL_PREFIX = "[al:";

    private final static String LEGAL_TOTAL_PREFIX = "[total:";

    @Override
    public LyricsInfo readInputStream(InputStream in) throws Exception {
        LyricsInfo lyricsIfno = new LyricsInfo();
        lyricsIfno.setLyricsFileExt(getSupportFileExt());
        lyricsIfno.setLyricsType(LyricsInfo.LRC);

        if (in != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in,
                    getDefaultCharset()));

            // 这里面key为该行歌词的开始时间，方便后面排序
            SortedMap<Integer, LyricsLineInfo> lyricsLineInfosTemp = new TreeMap<Integer, LyricsLineInfo>();
            Map<String, Object> lyricsTags = new HashMap<String, Object>();
            String lineInfo = "";
            while ((lineInfo = br.readLine()) != null) {

                // 解析歌词
                parserLineInfos(lyricsIfno, lyricsLineInfosTemp,
                        lyricsTags, lineInfo);

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
     * 解析行歌词
     *
     * @param lyricsIfno          歌词实体
     * @param lyricsLineInfosTemp 排序集合
     * @param lyricsTags          歌曲标签
     * @param lineInfo            行歌词内容
     * @throws Exception
     */
    private void parserLineInfos(LyricsInfo lyricsIfno, SortedMap<Integer, LyricsLineInfo> lyricsLineInfosTemp, Map<String, Object> lyricsTags, String lineInfo) throws Exception {
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
                || lineInfo.startsWith(LEGAL_TOTAL_PREFIX)
                || lineInfo.startsWith(LEGAL_AL_PREFIX)) {

            int startIndex = lineInfo.indexOf("[") + 1;
            int endIndex = lineInfo.lastIndexOf("]");
            String temp[] = lineInfo.substring(startIndex, endIndex).split(":");
            lyricsTags.put(temp[0], temp.length == 1 ? "" : temp[1]);

        } else {
            //时间标签
            String timeRegex = "\\[\\d+:\\d+.\\d+\\]";
            String timeRegexs = "(" + timeRegex + ")+";
            // 如果含有时间标签，则是歌词行
            Pattern pattern = Pattern.compile(timeRegexs);
            Matcher matcher = pattern.matcher(lineInfo);
            if (matcher.find()) {
                Pattern timePattern = Pattern.compile(timeRegex);
                Matcher timeMatcher = timePattern
                        .matcher(matcher.group());
                //遍历时间标签
                while (timeMatcher.find()) {
                    lyricsLineInfo = new LyricsLineInfo();
                    //获取开始时间
                    String startTimeString = timeMatcher.group().trim();
                    int startTime = TimeUtils.parseInteger(startTimeString.substring(startTimeString.indexOf('[') + 1, startTimeString.lastIndexOf(']')));
                    lyricsLineInfo.setStartTime(startTime);
                    //获取歌词内容
                    int timeEndIndex = matcher.end();
                    String lineLyrics = lineInfo.substring(timeEndIndex,
                            lineInfo.length()).trim();
                    lyricsLineInfo.setLineLyrics(lineLyrics);
                    lyricsLineInfosTemp.put(startTime, lyricsLineInfo);
                }
            }
        }
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
