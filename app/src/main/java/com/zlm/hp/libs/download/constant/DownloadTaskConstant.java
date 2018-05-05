package com.zlm.hp.libs.download.constant;

/**
 * 下载任务常量
 * 
 * @author zhangliangming
 * 
 */
public enum DownloadTaskConstant {
	/**
	 * 初始化
	 */
	INT(0),
	/**
	 * 等待下载
	 */
	WAIT(1),
	/**
	 * 下载中
	 */
	DOWNLOADING(2),
	/**
	 * 下载暂停
	 */
	PAUSE(3),
	/**
	 * 取消
	 */
	CANCEL(4),
	/**
	 * 错误
	 */
	ERROR(5);

	private int value;

	DownloadTaskConstant(int value) {

		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static int getValue(String name) {
		for (DownloadTaskConstant downloadTaskConstant : DownloadTaskConstant
				.values()) {
			if (downloadTaskConstant.name().equals(name)) {
				return downloadTaskConstant.getValue();
			}
		}
		return DownloadTaskConstant.INT.getValue();
	}

}
