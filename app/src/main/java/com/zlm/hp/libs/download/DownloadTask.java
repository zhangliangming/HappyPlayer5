package com.zlm.hp.libs.download;

import com.zlm.hp.libs.download.utils.TaskThreadUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * 下载任务
 *
 * @author zhangliangming
 */
public class DownloadTask implements Serializable {

    /**
     * 任务id
     */
    private String taskId;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 任务文件后缀名
     */
    private String taskExt;
    /**
     * 任务保存路径
     */
    private String taskPath;
    /**
     * 任务hash
     */
    private String taskHash;
    /**
     * 任务临时保存路径
     */
    private String taskTempPath;
    /**
     * 任务下载路径
     */
    private String taskUrl;
    /**
     * 添加时间
     */
    private Date createTime;
    /**
     * 任务状态
     */
    private int status;
    /**
     * 线程总数
     */
    private int threadNum;
    /**
     * 任务文件大小
     */
    private long taskFileSize;
    /**
     * 任务线程
     */
    private TaskThreadUtil taskThreadUtil;

    public DownloadTask() {

    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskExt() {
        return taskExt;
    }

    public void setTaskExt(String taskExt) {
        this.taskExt = taskExt;
    }

    public String getTaskPath() {
        return taskPath;
    }

    public void setTaskPath(String taskPath) {
        this.taskPath = taskPath;
    }

    public String getTaskHash() {
        return taskHash;
    }

    public void setTaskHash(String taskHash) {
        this.taskHash = taskHash;
    }

    public String getTaskTempPath() {
        return taskTempPath;
    }

    public void setTaskTempPath(String taskTempPath) {
        this.taskTempPath = taskTempPath;
    }

    public String getTaskUrl() {
        return taskUrl;
    }

    public void setTaskUrl(String taskUrl) {
        this.taskUrl = taskUrl;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public long getTaskFileSize() {
        return taskFileSize;
    }

    public void setTaskFileSize(long taskFileSize) {
        this.taskFileSize = taskFileSize;
    }

    public TaskThreadUtil getTaskThreadUtil() {
        return taskThreadUtil;
    }

    public void setTaskThreadUtil(TaskThreadUtil taskThreadUtil) {
        this.taskThreadUtil = taskThreadUtil;
    }

    @Override
    public String toString() {
        return "DownloadTask{" +
                "taskId='" + taskId + '\'' +
                ", taskName='" + taskName + '\'' +
                ", taskExt='" + taskExt + '\'' +
                ", taskPath='" + taskPath + '\'' +
                ", taskHash='" + taskHash + '\'' +
                ", taskTempPath='" + taskTempPath + '\'' +
                ", taskUrl='" + taskUrl + '\'' +
                ", createTime=" + createTime +
                ", status=" + status +
                ", threadNum=" + threadNum +
                ", taskFileSize=" + taskFileSize +
                ", taskThreadUtil=" + taskThreadUtil +
                '}';
    }
}
