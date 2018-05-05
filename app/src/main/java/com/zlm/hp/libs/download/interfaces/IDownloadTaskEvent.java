package com.zlm.hp.libs.download.interfaces;

import com.zlm.hp.libs.download.DownloadTask;

/**
 * 下载事件接口
 * 
 * @author zhangliangming
 * 
 */
public interface IDownloadTaskEvent extends IDownloadTaskThreadEven {

	/**
	 * 
	 * 任务等待
	 * 
	 * @param task
	 *            任务
	 * @throws Exception
	 * @author zhangliangming
	 * @date 2017年7月8日
	 */
    void taskWaiting(DownloadTask task);

	/**
	 * 
	 * 任务下载中
	 * 
	 * @param task
	 *            任务
	 * @param downloadedSize
	 *            下载大小
	 * @throws Exception
	 * @author zhangliangming
	 * @date 2017年7月8日
	 */
    void taskDownloading(DownloadTask task, int downloadedSize);

	/**
	 * 
	 * 任务暂停
	 * 
	 * @param task
	 *            任务
	 * @param downloadedSize
	 *            下载进度
	 * @throws Exception
	 * @author zhangliangming
	 * @date 2017年7月8日
	 */
    void taskPause(DownloadTask task, int downloadedSize);
	
	/**
	 * 
	 * 取消任务
	 * @param task
	 * @author zhangliangming
	 * @date 2017年7月9日
	 */
    void taskCancel(DownloadTask task);

	/**
	 * 
	 * 任务完成
	 * 
	 * @param task
	 *            任务
	 * @param downloadedSize
	 *            下载进度
	 * @throws Exception
	 * @author zhangliangming
	 * @date 2017年7月8日
	 */
    void taskFinish(DownloadTask task, int downloadedSize);

	/**
	 * 
	 * 任务错误
	 * 
	 * @param task
	 *            任务
	 * @param msg
	 *            错误信息
	 * @throws Exception
	 * @author zhangliangming
	 * @date 2017年7月8日
	 */
    void taskError(DownloadTask task, String msg);

}
