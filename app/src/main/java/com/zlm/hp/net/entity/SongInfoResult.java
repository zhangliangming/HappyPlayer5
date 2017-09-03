package com.zlm.hp.net.entity;

/**
 * 歌曲信息结果
 * 
 * @author zhangliangming
 * 
 */
public class SongInfoResult {

	/**
	 * 文件大小
	 */
	private String fileSize;
	/**
	 * 歌手
	 */
	private String singerName;
	/**
	 * 歌曲名称
	 */
	private String songName;
	/**
	 * 歌曲格式
	 */
	private String extName;
	/**
	 * 歌曲长度
	 */
	private String duration;
	/**
	 * 歌曲hash
	 */
	private String hash;
	/**
	 * 歌曲下载路径
	 */
	private String url;
	/**
	 * 歌曲专辑图片
	 */
	private String imgUrl;

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public String getSingerName() {
		return singerName;
	}

	public void setSingerName(String singerName) {
		this.singerName = singerName;
	}

	public String getSongName() {
		return songName;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}

	public String getExtName() {
		return extName;
	}

	public void setExtName(String extName) {
		this.extName = extName;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
}
