package base.lyrics.model;

/**
 * 翻译行歌词
 * Created by zhangliangming on 2017/9/11.
 */

public class TranslateLrcLineInfo {

    /**
     * 该行歌词
     */
    private String mLineLyrics;

    public String getLineLyrics() {
        return mLineLyrics;
    }

    public void setLineLyrics(String lineLyrics) {
        this.mLineLyrics = lineLyrics.replaceAll("\r|\n","");
    }
}
