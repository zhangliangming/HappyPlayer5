package com.zlm.hp.model;

import java.io.Serializable;

/**
 * 下载任务
 * Created by zhangliangming on 2017/9/8.
 */

public class DownloadInfo implements Serializable {
    public static final String KEY = "com.zlm.hp.dli.key";
    /**
     * 歌曲id
     */
    private String dHash;

    /**
     * 下载大小
     */
    private long downloadedSize;
    /**
     * 歌曲信息
     */
    private AudioInfo audioInfo;

    public DownloadInfo() {

    }

    public String getDHash() {
        return dHash;
    }

    public void setDHash(String dHash) {
        this.dHash = dHash;
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    public AudioInfo getAudioInfo() {
        return audioInfo;
    }

    public void setAudioInfo(AudioInfo audioInfo) {
        this.audioInfo = audioInfo;
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "dHash='" + dHash + '\'' +
                ", downloadedSize=" + downloadedSize +
                ", audioInfo=" + audioInfo +
                '}';
    }
}
