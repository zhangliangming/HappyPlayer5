package com.happy.lyrics.model;

import java.util.List;

/**
 * 动感歌词实体类
 *
 * @author zhangliangming
 */
public class LyricsLineInfo extends LrcLyricsLineInfo {

    /**
     * 歌词数组，用来分隔每个歌词
     */
    public String[] lyricsWords;
    /**
     * 数组，用来存放每个歌词的时间
     */
    public int[] wordsDisInterval;

    /**
     * 分割歌词行歌词
     */
    private List<LyricsLineInfo> splitLyricsLineInfos;

    public List<LyricsLineInfo> getSplitLyricsLineInfos() {
        return splitLyricsLineInfos;
    }

    public void setSplitLyricsLineInfos(List<LyricsLineInfo> splitLyricsLineInfos) {
        this.splitLyricsLineInfos = splitLyricsLineInfos;
    }

    public String[] getLyricsWords() {
        return lyricsWords;
    }

    public void setLyricsWords(String[] lyricsWords) {
        this.lyricsWords = lyricsWords;
    }

    public int[] getWordsDisInterval() {
        return wordsDisInterval;
    }

    public void setWordsDisInterval(int[] wordsDisInterval) {
        this.wordsDisInterval = wordsDisInterval;
    }
}
