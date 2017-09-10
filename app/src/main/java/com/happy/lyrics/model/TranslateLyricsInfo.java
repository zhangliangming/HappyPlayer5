package com.happy.lyrics.model;

import java.util.TreeMap;

/**
 * 翻译歌词
 * Created by zhangliangming on 2017/9/10.
 */

public class TranslateLyricsInfo {
    private String language;
    private String type;

    /**
     * 翻译歌词集合
     */
    private TreeMap<Integer, LyricsLineInfo> lyricsLineInfos;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TreeMap<Integer, LyricsLineInfo> getLyricsLineInfos() {
        return lyricsLineInfos;
    }

    public void setLyricsLineInfos(TreeMap<Integer, LyricsLineInfo> lyricsLineInfos) {
        this.lyricsLineInfos = lyricsLineInfos;
    }
}
