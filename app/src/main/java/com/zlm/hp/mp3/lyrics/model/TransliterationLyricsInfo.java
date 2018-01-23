package com.zlm.hp.mp3.lyrics.model;

import java.util.List;

/**
 * 音译歌词
 * Created by zhangliangming on 2017/9/11.
 */

public class TransliterationLyricsInfo {

    /**
     * 音译歌词行
     */
    private List<LyricsLineInfo> transliterationLrcLineInfos;

    public List<LyricsLineInfo> getTransliterationLrcLineInfos() {
        return transliterationLrcLineInfos;
    }

    public void setTransliterationLrcLineInfos(List<LyricsLineInfo> transliterationLrcLineInfos) {
        this.transliterationLrcLineInfos = transliterationLrcLineInfos;
    }
}
