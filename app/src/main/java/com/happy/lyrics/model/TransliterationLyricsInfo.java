package com.happy.lyrics.model;

import java.util.List;

/**
 * 音译歌词
 * Created by zhangliangming on 2017/9/11.
 */

public class TransliterationLyricsInfo {

    /**
     * 音译歌词行
     */
    private List<TransliterationLrcLineInfo> transliterationLrcLineInfos;

    public List<TransliterationLrcLineInfo> getTransliterationLrcLineInfos() {
        return transliterationLrcLineInfos;
    }

    public void setTransliterationLrcLineInfos(List<TransliterationLrcLineInfo> transliterationLrcLineInfos) {
        this.transliterationLrcLineInfos = transliterationLrcLineInfos;
    }
}
