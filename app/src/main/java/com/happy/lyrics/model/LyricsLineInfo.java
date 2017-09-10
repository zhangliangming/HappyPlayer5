package com.happy.lyrics.model;

/**
 * 歌词行数据
 *
 * @author zhangliangming
 */
public class LyricsLineInfo {
    /**
     * 是否是卡拉ok歌词
     */
    private boolean isKaraokeLrc = true;
    /**
     * 歌词开始时间
     */
    private int startTime = 0;
    /**
     * 歌词结束时间
     */
    private int endTime = 0;
    /**
     * 该行歌词
     */
    private String lineLyrics = null;
    /**
     * 歌词数组，用来分隔每个歌词
     */
    public String[] lyricsWords = null;
    /**
     * 数组，用来存放每个歌词的时间
     */
    public int[] wordsDisInterval = null;

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public String getLineLyrics() {
        return lineLyrics;
    }

    public void setLineLyrics(String lineLyrics) {
        this.lineLyrics = lineLyrics;
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

    public boolean isKaraokeLrc() {
        return isKaraokeLrc;
    }

    public void setKaraokeLrc(boolean karaokeLrc) {
        isKaraokeLrc = karaokeLrc;
    }
}
