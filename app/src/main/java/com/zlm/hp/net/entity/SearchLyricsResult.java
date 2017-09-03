package com.zlm.hp.net.entity;

/**
 * 搜索歌词结果集合
 * 
 * @author zhangliangming
 * 
 */
public class SearchLyricsResult {

	private String id;
	private String accesskey;
	private String duration;

	/**
	 * 歌手
	 */
	private String singerName;
	/**
	 * 歌曲名称
	 */
	private String songName;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAccesskey() {
		return accesskey;
	}

	public void setAccesskey(String accesskey) {
		this.accesskey = accesskey;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
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

}
