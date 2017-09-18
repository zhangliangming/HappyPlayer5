package com.happy.lyrics;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.happy.lyrics.model.LyricsInfo;

/**
 * 歌词文件读取器
 * 
 * @author zhangliangming
 * 
 */
public abstract class LyricsFileReader {
	/**
	 * 默认编码
	 */
	protected Charset defaultCharset = Charset.forName("utf-8");

	/**
	 * 读取歌词文件
	 * 
	 * @param file
	 * @return
	 */
	public abstract LyricsInfo readFile(File file) throws Exception;
	
	/**
	 * 读取歌词文本内容
	 * 
	 * @param base64FileContentString
	 *            base64位文件内容
	 * @param saveLrcFile
	 *            要保存的歌词文件
	 * @return
	 * @throws Exception
	 */
	public abstract LyricsInfo readLrcText(String base64FileContentString,
			File saveLrcFile) throws Exception;
	/**
	 * 读取歌词文本内容
	 * @param base64ByteArray base64内容数组
	 * @param saveLrcFile
	 * @return
	 * @throws Exception
	 */
	public abstract LyricsInfo readLrcText(byte[] base64ByteArray,
			File saveLrcFile) throws Exception;
	
	/**
	 * 读取歌词文件
	 * 
	 * @param in
	 * @return
	 */
	public abstract LyricsInfo readInputStream(InputStream in) throws Exception;

	/**
	 * 支持文件格式
	 * 
	 * @param ext
	 *            文件后缀名
	 * @return
	 */
	public abstract boolean isFileSupported(String ext);

	/**
	 * 获取支持的文件后缀名
	 * 
	 * @return
	 */
	public abstract String getSupportFileExt();

	public void setDefaultCharset(Charset charset) {
		defaultCharset = charset;
	}

	public Charset getDefaultCharset() {
		return defaultCharset;
	}
}
