package com.zlm.hp.model;

import com.zlm.hp.libs.download.DownloadTask;

import java.io.Serializable;

/**
 * 下载任务
 * Created by zhangliangming on 2017/8/13.
 */

public class DownloadMessage implements Serializable {
    public static final String KEY = "com.zlm.hp.dm.key";
    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 任务hash
     */
    private String taskHash;

    public DownloadMessage() {

    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskHash() {
        return taskHash;
    }

    public void setTaskHash(String taskHash) {
        this.taskHash = taskHash;
    }

    @Override
    public String toString() {
        return "DownloadMessage{" +
                "errorMsg='" + errorMsg + '\'' +
                ", taskId='" + taskId + '\'' +
                ", taskHash='" + taskHash + '\'' +
                '}';
    }
}
