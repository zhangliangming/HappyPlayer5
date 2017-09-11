package com.happy.lyrics.model;

import java.util.List;

/**
 * 音译歌词行
 * Created by zhangliangming on 2017/9/11.
 */

public class TransliterationLrcLineInfo extends TranslateLrcLineInfo {
    /**
     * 歌词数组，用来分隔每个歌词
     */
    public String[] lyricsWords;

    /**
     * 分割音译歌词行
     */
    private List<TransliterationLrcLineInfo> splitTransliterationLrcLineInfos;

    public List<TransliterationLrcLineInfo> getSplitTransliterationLrcLineInfos() {
        return splitTransliterationLrcLineInfos;
    }

    public void setSplitTransliterationLrcLineInfos(List<TransliterationLrcLineInfo> splitTransliterationLrcLineInfos) {
        this.splitTransliterationLrcLineInfos = splitTransliterationLrcLineInfos;
    }

    public String[] getLyricsWords() {
        return lyricsWords;
    }

    public void setLyricsWords(String[] lyricsWords) {
        this.lyricsWords = lyricsWords;
    }
}
