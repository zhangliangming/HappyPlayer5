package com.zlm.hp.model;

import java.io.Serializable;

/**
 * 广播信息类
 * Created by zhangliangming on 2017/8/6.
 */
public class AudioMessage implements Serializable {
    public static final String KEY = "com.zlm.hp.am.key";
    /**
     * 错误信息
     */
    private String errorMsg;
    /**
     * 播放进度
     */
    private long playProgress;
    /**
     * 音频信息
     */
    private AudioInfo audioInfo;
    /**
     *
     */
    private String hash;

    public AudioMessage() {

    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public long getPlayProgress() {
        return playProgress;
    }

    public  void setPlayProgress(long playProgress) {
        this.playProgress = playProgress;
    }

    public AudioInfo getAudioInfo() {
        return audioInfo;
    }

    public void setAudioInfo(AudioInfo audioInfo) {
        this.audioInfo = audioInfo;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "AudioMessage{" +
                "errorMsg='" + errorMsg + '\'' +
                ", playProgress=" + playProgress +
                ", audioInfo=" + audioInfo +
                ", hash='" + hash + '\'' +
                '}';
    }
}
