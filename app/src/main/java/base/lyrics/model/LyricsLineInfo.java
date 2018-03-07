package base.lyrics.model;

import java.util.List;

/**
 * 歌词实体类
 *
 * @author zhangliangming
 */
public class LyricsLineInfo {

    /**
     * 歌词开始时间
     */
    private int mStartTime;
    /**
     * 歌词结束时间
     */
    private int mEndTime;
    /**
     * 该行歌词
     */
    private String mLineLyrics;

    /**
     * 歌词数组，用来分隔每个歌词
     */
    public String[] mLyricsWords;
    /**
     * 数组，用来存放每个歌词的时间
     */
    private int[] mWordsDisInterval;

    /**
     * 分割歌词行歌词
     */
    private List<LyricsLineInfo> mSplitDynamicLrcLineInfos;

    public List<LyricsLineInfo> getSplitLyricsLineInfos() {
        return mSplitDynamicLrcLineInfos;
    }

    public void setSplitLyricsLineInfos(
            List<LyricsLineInfo> splitDynamicLrcLineInfos) {
        this.mSplitDynamicLrcLineInfos = splitDynamicLrcLineInfos;
    }

    public String[] getLyricsWords() {
        return mLyricsWords;
    }

    public void setLyricsWords(String[] lyricsWords) {
        for (int i = 0; i < lyricsWords.length; i++) {
            lyricsWords[i] = lyricsWords[i].replaceAll("\r|\n", "");
        }

        this.mLyricsWords = lyricsWords;
    }

    public int[] getWordsDisInterval() {
        return mWordsDisInterval;
    }

    public void setWordsDisInterval(int[] wordsDisInterval) {
        this.mWordsDisInterval = wordsDisInterval;
    }

    public int getStartTime() {
        return mStartTime;
    }

    public void setStartTime(int mStartTime) {
        this.mStartTime = mStartTime;
    }

    public int getEndTime() {
        return mEndTime;
    }

    public void setEndTime(int mEndTime) {
        this.mEndTime = mEndTime;
    }

    public String getLineLyrics() {
        return mLineLyrics;
    }

    public void setLineLyrics(String mLineLyrics) {
        this.mLineLyrics = mLineLyrics.replaceAll("\r|\n", "");
    }

    /**
     * 复制
     *
     * @param dist 要复制的实体类
     * @param orig 原始实体类
     */
    public void copy(LyricsLineInfo dist, LyricsLineInfo orig) {
        if (orig.getWordsDisInterval() != null) {
            dist.setWordsDisInterval(orig.getWordsDisInterval());
        }
        dist.setStartTime(orig.getStartTime());
        dist.setEndTime(orig.getEndTime());

        if (orig.getLyricsWords() != null) {
            dist.setLyricsWords(orig.getLyricsWords());
        }

        dist.setLineLyrics(orig.getLineLyrics());

    }
}
