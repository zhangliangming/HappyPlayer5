package com.zlm.hp.model;

/**
 * 歌手写真
 * Created by zhangliangming on 2017/9/2.
 */

public class SongSingerInfo {
    private String hash;
    private String imgUrl;
    private String createTime;
    /**
     * 歌手名称
     */
    private String singerName;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getSingerName() {
        return singerName;
    }

    public void setSingerName(String singerName) {
        this.singerName = singerName;
    }
}
