package com.zlm.hp.net.entity;

/**
 * 下载歌词返回结果
 * 
 * @author zhangliangming
 * 
 */
public class DownloadLyricsResult {
	/**
	 * 歌词编码
	 */
	private String charset;
	/**
	 * 歌词内容(base64)
	 */
	private String content;
	/**
	 * 歌词格式
	 */
	private String fmt;

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFmt() {
		return fmt;
	}

	public void setFmt(String fmt) {
		this.fmt = fmt;
	}

}
