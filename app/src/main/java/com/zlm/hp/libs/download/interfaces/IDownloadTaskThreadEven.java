package com.zlm.hp.libs.download.interfaces;

import com.zlm.hp.libs.download.DownloadTask;

/**
 * 下载任务线程事件
 *
 * @author zhangliangming
 */
public interface IDownloadTaskThreadEven {

    /**
     * 获取已下载的进度
     *
     * @param task
     * @param threadId
     * @return
     */
    int getTaskThreadDownloadedSize(DownloadTask task, int threadId);

    /**
     * 任务线程下载中
     *
     * @param task           任务
     * @param threadId       线程任务id
     * @param downloadedSize 线程下载进度
     * @throws Exception
     * @author zhangliangming
     * @date 2017年7月8日
     */
    void taskThreadDownloading(DownloadTask task, int threadId,
                               int downloadedSize);

    /**
     * 任务线程暂停
     *
     * @param task           任务
     * @param threadId       线程任务id
     * @param downloadedSize 线程下载进度
     * @throws Exception
     * @author zhangliangming
     * @date 2017年7月8日
     */
    void taskThreadPause(DownloadTask task, int threadId, int downloadedSize);

    /***
     *
     * 任务线程完成
     *
     * @param task
     *            任务
     * @param threadId
     *            线程任务id
     * @param downloadedSize
     *            线程下载进度
     * @throws Exception
     * @author zhangliangming
     * @date 2017年7月8日
     */
    void taskThreadFinish(DownloadTask task, int threadId, int downloadedSize);

    /***
     *
     * 任务线程错误
     *
     * @param task
     *            任务
     * @param msg
     *            错误信息
     * @throws Exception
     * @author zhangliangming
     * @date 2017年7月8日
     */
    void taskThreadError(DownloadTask task, int threadId, String msg);

}
