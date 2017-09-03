package com.zlm.hp.model;

import java.io.Serializable;

/**
 * 任务线程
 *
 * @author zhangliangming
 */
public class DownloadThreadInfo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String taskId;
    private int threadNum;
    private int threadId;
    private int downloadedSize;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(int downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    @Override
    public String toString() {
        return "DownloadThreadInfo{" +
                "taskId='" + taskId + '\'' +
                ", threadNum=" + threadNum +
                ", threadId=" + threadId +
                ", downloadedSize=" + downloadedSize +
                '}';
    }
}
