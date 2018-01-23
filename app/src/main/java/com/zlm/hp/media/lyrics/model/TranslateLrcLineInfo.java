package com.zlm.hp.media.lyrics.model;

import java.util.List;

/**
 * 翻译行歌词
 * Created by zhangliangming on 2017/9/11.
 */

public class TranslateLrcLineInfo {

    /**
     * 该行歌词
     */
    private String lineLyrics;

    /**
     * 分割翻译行歌词
     */
    private List<TranslateLrcLineInfo> splitTranslateLrcLineInfos;

    public List<TranslateLrcLineInfo> getSplitTranslateLrcLineInfos() {
        return splitTranslateLrcLineInfos;
    }

    public void setSplitTranslateLrcLineInfos(List<TranslateLrcLineInfo> splitTranslateLrcLineInfos) {
        this.splitTranslateLrcLineInfos = splitTranslateLrcLineInfos;
    }

    public String getLineLyrics() {
        return lineLyrics;
    }

    public void setLineLyrics(String lineLyrics) {
        this.lineLyrics = lineLyrics.replaceAll("\r|\n","");
    }

    public void copy(TranslateLrcLineInfo dist, TranslateLrcLineInfo orig) {
        dist.setLineLyrics(orig.getLineLyrics());
    }
}
