package com.zlm.hp.mp3.lyrics.model;

import java.util.List;

/**
 * 翻译歌词
 * Created by zhangliangming on 2017/9/10.
 */

public class TranslateLyricsInfo {
    /**
     * 翻译行歌词
     */
    private List<TranslateLrcLineInfo> translateLrcLineInfos;

    public List<TranslateLrcLineInfo> getTranslateLrcLineInfos() {
        return translateLrcLineInfos;
    }

    public void setTranslateLrcLineInfos(List<TranslateLrcLineInfo> translateLrcLineInfos) {
        this.translateLrcLineInfos = translateLrcLineInfos;
    }
}
