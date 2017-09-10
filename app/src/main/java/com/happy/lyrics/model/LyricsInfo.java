package com.happy.lyrics.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 歌词数据
 *
 * @author zhangliangming
 */
public class LyricsInfo {
    /**
     * 歌词格式
     */
    private String lyricsFileExt;
    /**
     * 所有的歌词行数据
     */
    private TreeMap<Integer, LyricsLineInfo> lyricsLineInfos;
    /**
     * 翻译歌词集合
     */
    private List<TranslateLyricsInfo> translateLyricsInfos;
    /**
     * 歌词标签
     */
    private Map<String, Object> lyricsTags;

    public Map<String, Object> getLyricsTags() {
        return lyricsTags;
    }

    public void setLyricsTags(Map<String, Object> lyricsTags) {
        this.lyricsTags = lyricsTags;
    }

    public TreeMap<Integer, LyricsLineInfo> getLyricsLineInfos() {
        return lyricsLineInfos;
    }

    public List<TranslateLyricsInfo> getTranslateLyricsInfos() {
        return translateLyricsInfos;
    }

    public void setTranslateLyricsInfos(List<TranslateLyricsInfo> translateLyricsInfos) {
        this.translateLyricsInfos = translateLyricsInfos;
    }

    public void setLyricsLineInfos(
            TreeMap<Integer, LyricsLineInfo> lyricsLineInfos) {
        this.lyricsLineInfos = lyricsLineInfos;
    }

    public String getLyricsFileExt() {
        return lyricsFileExt;
    }

    public void setLyricsFileExt(String lyricsFileExt) {
        this.lyricsFileExt = lyricsFileExt;
    }

    public void setTitle(String title) {

        if (lyricsTags != null) {
            lyricsTags = new HashMap<String, Object>();
        }
        lyricsTags.put(LyricsTag.TAG_TITLE, title);

    }

    public String getTitle() {

        String title = "";
        if (lyricsTags != null && !lyricsTags.isEmpty()
                && lyricsTags.containsKey(LyricsTag.TAG_TITLE)) {
            title = (String) lyricsTags.get(LyricsTag.TAG_TITLE);
        }
        return title;

    }

    public void setArtist(String artist) {
        if (lyricsTags != null) {
            lyricsTags = new HashMap<String, Object>();
        }
        lyricsTags.put(LyricsTag.TAG_ARTIST, artist);
    }

    public String getArtist() {

        String artist = "";
        if (lyricsTags != null && !lyricsTags.isEmpty()
                && lyricsTags.containsKey(LyricsTag.TAG_ARTIST)) {
            artist = (String) lyricsTags.get(LyricsTag.TAG_ARTIST);
        }
        return artist;

    }

    public void setOffset(long offset) {
        if (lyricsTags != null) {
            lyricsTags = new HashMap<String, Object>();
        }
        lyricsTags.put(LyricsTag.TAG_OFFSET, offset);
    }

    public long getOffset() {

        long offset = 0;
        if (lyricsTags != null && !lyricsTags.isEmpty()
                && lyricsTags.containsKey(LyricsTag.TAG_OFFSET)) {
            offset = Long.parseLong((String) lyricsTags
                    .get(LyricsTag.TAG_OFFSET));
        }
        return offset;

    }

    public void setBy(String by) {
        if (lyricsTags != null) {
            lyricsTags = new HashMap<String, Object>();
        }
        lyricsTags.put(LyricsTag.TAG_BY, by);
    }

    public String getBy() {

        String by = "";
        if (lyricsTags != null && !lyricsTags.isEmpty()
                && lyricsTags.containsKey(LyricsTag.TAG_BY)) {
            by = (String) lyricsTags.get(LyricsTag.TAG_BY);
        }
        return by;

    }

    public void setTotal(long total) {
        if (lyricsTags != null) {
            lyricsTags = new HashMap<String, Object>();
        }
        lyricsTags.put(LyricsTag.TAG_TOTAL, total);
    }

    public long getTotal() {

        long total = 0;
        if (lyricsTags != null && !lyricsTags.isEmpty()
                && lyricsTags.containsKey(LyricsTag.TAG_TOTAL)) {
            total = Long
                    .parseLong((String) lyricsTags.get(LyricsTag.TAG_TOTAL));
        }
        return total;

    }

}
