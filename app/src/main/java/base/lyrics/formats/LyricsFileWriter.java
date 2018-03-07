package base.lyrics.formats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import base.lyrics.model.LyricsInfo;

/**
 * @Description: 歌词文件生成器
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/12/25 16:42
 * @Throws:
 */
public abstract class LyricsFileWriter {
    /**
     * 默认编码
     */
    private Charset defaultCharset = Charset.forName("utf-8");

    /**
     * 保存歌词文件
     *
     * @param lyricsIfno     歌词数据
     * @param lyricsFilePath 歌词文件路径
     */
    public abstract boolean writer(LyricsInfo lyricsIfno, String lyricsFilePath)
            throws Exception;


    /**
     * 保存歌词文件到本地
     *
     * @param lyricsContent  歌词文件内容文本
     * @param lyricsFilePath 歌词文件路径
     * @return
     * @throws Exception
     */
    public boolean saveLyricsFile(String lyricsContent, String lyricsFilePath) throws Exception {

        File lyricsFile = new File(lyricsFilePath);
        if (lyricsFile != null) {
            //
            if (lyricsFile.getParentFile().isDirectory() && !lyricsFile.getParentFile().exists()) {
                lyricsFile.getParentFile().mkdirs();
            }
            OutputStreamWriter outstream = new OutputStreamWriter(
                    new FileOutputStream(lyricsFilePath),
                    getDefaultCharset());
            PrintWriter writer = new PrintWriter(outstream);
            writer.write(lyricsContent);
            writer.close();

            outstream = null;
            writer = null;

            return true;
        }

        return false;
    }

    /**
     * 保存歌词文件到本地
     *
     * @param lyricsContent  歌词文件内容文本
     * @param lyricsFilePath 歌词文件路径
     * @return
     * @throws Exception
     */
    public boolean saveLyricsFile(byte[] lyricsContent, String lyricsFilePath) throws Exception {

        File lyricsFile = new File(lyricsFilePath);
        if (lyricsFile != null) {
            //
            if (lyricsFile.getParentFile().isDirectory() && !lyricsFile.getParentFile().exists()) {
                lyricsFile.getParentFile().mkdirs();
            }
            FileOutputStream os = new FileOutputStream(lyricsFile);
            os.write(lyricsContent);
            os.close();

            os = null;

            return true;
        }
        return false;
    }

    /**
     * 获取歌词文件内容
     *
     * @param lyricsIfno 歌词内容类
     * @return
     * @throws Exception
     */
    public abstract String getLyricsContent(LyricsInfo lyricsIfno) throws Exception;

    /**
     * 支持文件格式
     *
     * @param ext 文件后缀名
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
