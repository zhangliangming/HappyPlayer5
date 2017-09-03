package com.zlm.hp.net.entity;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 排行列表数据
 *
 * @author zhangliangming
 */
public class RankListResult implements Serializable {

    public static final String KEY = "rank";
    /**
     * 排行标题
     */
    private String rankName;
    private String rankId;
    private String rankType;
    private String banner7Url;
    private String imgUrl;
    private String bannerUrl;
    /**
     *
     */
    private String[] songNames;

    public RankListResult() {

    }

    public String getRankName() {
        return rankName;
    }

    public void setRankName(String rankName) {
        this.rankName = rankName;
    }

    public String getRankId() {
        return rankId;
    }

    public void setRankId(String rankId) {
        this.rankId = rankId;
    }

    public String getRankType() {
        return rankType;
    }

    public void setRankType(String rankType) {
        this.rankType = rankType;
    }

    public String getBanner7Url() {
        return banner7Url;
    }

    public void setBanner7Url(String banner7Url) {
        this.banner7Url = banner7Url;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String[] getSongNames() {
        return songNames;
    }

    public void setSongNames(String[] songNames) {
        this.songNames = songNames;
    }

    @Override
    public String toString() {
        return "RankListResult{" +
                "rankName='" + rankName + '\'' +
                ", rankId='" + rankId + '\'' +
                ", rankType='" + rankType + '\'' +
                ", banner7Url='" + banner7Url + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", bannerUrl='" + bannerUrl + '\'' +
                ", songNames=" + Arrays.toString(songNames) +
                '}';
    }
}
