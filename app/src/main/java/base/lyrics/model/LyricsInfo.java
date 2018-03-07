package base.lyrics.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 歌词数据
 *
 * @author zhangliangming
 */
public class LyricsInfo {
    /**
     * lrc类型
     */
    public static final int LRC = 0;
    /**
     * 动感歌词类型
     */
    public static final int DYNAMIC = 1;
    /**
     * 默认歌词类型是：动感歌词类型
     */
    private int mLyricsType = DYNAMIC;

    /**
     * 歌词格式
     */
    private String mLyricsFileExt;
    /**
     * 所有的歌词行数据
     */
    private TreeMap<Integer, LyricsLineInfo> mLyricsLineInfoTreeMap;
    /**
     * 翻译行歌词
     */
    private List<TranslateLrcLineInfo> mTranslateLrcLineInfos;
    /**
     * 音译歌词行
     */
    private List<LyricsLineInfo> mTransliterationLrcLineInfos;
    /**
     * 歌词标签
     */
    private Map<String, Object> mLyricsTags;

    public Map<String, Object> getLyricsTags() {
        return mLyricsTags;
    }

    public void setLyricsTags(Map<String, Object> lyricsTags) {
        this.mLyricsTags = lyricsTags;
    }

    public List<TranslateLrcLineInfo> getTranslateLrcLineInfos() {
        return mTranslateLrcLineInfos;
    }

    public void setTranslateLrcLineInfos(List<TranslateLrcLineInfo> translateLrcLineInfos) {
        this.mTranslateLrcLineInfos = translateLrcLineInfos;
    }

    public List<LyricsLineInfo> getTransliterationLrcLineInfos() {
        return mTransliterationLrcLineInfos;
    }

    public void setTransliterationLrcLineInfos(List<LyricsLineInfo> transliterationLrcLineInfos) {
        this.mTransliterationLrcLineInfos = transliterationLrcLineInfos;
    }

    public String getLyricsFileExt() {
        return mLyricsFileExt;
    }

    public void setLyricsFileExt(String lyricsFileExt) {
        this.mLyricsFileExt = lyricsFileExt;
    }

    public TreeMap<Integer, LyricsLineInfo> getLyricsLineInfoTreeMap() {
        return mLyricsLineInfoTreeMap;
    }

    public void setLyricsLineInfoTreeMap(
            TreeMap<Integer, LyricsLineInfo> lyricsLineInfoTreeMap) {
        this.mLyricsLineInfoTreeMap = lyricsLineInfoTreeMap;
    }

    public void setTitle(String title) {

        if (mLyricsTags != null) {
            mLyricsTags = new HashMap<String, Object>();
        }
        mLyricsTags.put(LyricsTag.TAG_TITLE, title);

    }

    public String getTitle() {

        String title = "";
        if (mLyricsTags != null && !mLyricsTags.isEmpty()
                && mLyricsTags.containsKey(LyricsTag.TAG_TITLE)) {
            title = (String) mLyricsTags.get(LyricsTag.TAG_TITLE);
        }
        return title;

    }

    public void setArtist(String artist) {
        if (mLyricsTags != null) {
            mLyricsTags = new HashMap<String, Object>();
        }
        mLyricsTags.put(LyricsTag.TAG_ARTIST, artist);
    }

    public String getArtist() {

        String artist = "";
        if (mLyricsTags != null && !mLyricsTags.isEmpty()
                && mLyricsTags.containsKey(LyricsTag.TAG_ARTIST)) {
            artist = (String) mLyricsTags.get(LyricsTag.TAG_ARTIST);
        }
        return artist;

    }

    public void setOffset(long offset) {
        if (mLyricsTags != null) {
            mLyricsTags = new HashMap<String, Object>();
        }
        mLyricsTags.put(LyricsTag.TAG_OFFSET, offset);
    }

    public long getOffset() {

        long offset = 0;
        if (mLyricsTags != null && !mLyricsTags.isEmpty()
                && mLyricsTags.containsKey(LyricsTag.TAG_OFFSET)) {
            try {
                offset = Long.parseLong((String) mLyricsTags
                        .get(LyricsTag.TAG_OFFSET));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return offset;

    }

    public void setBy(String by) {
        if (mLyricsTags != null) {
            mLyricsTags = new HashMap<String, Object>();
        }
        mLyricsTags.put(LyricsTag.TAG_BY, by);
    }

    public String getBy() {

        String by = "";
        if (mLyricsTags != null && !mLyricsTags.isEmpty()
                && mLyricsTags.containsKey(LyricsTag.TAG_BY)) {
            by = (String) mLyricsTags.get(LyricsTag.TAG_BY);
        }
        return by;

    }

    public void setTotal(long total) {
        if (mLyricsTags != null) {
            mLyricsTags = new HashMap<String, Object>();
        }
        mLyricsTags.put(LyricsTag.TAG_TOTAL, total);
    }

    public long getTotal() {

        long total = 0;
        if (mLyricsTags != null && !mLyricsTags.isEmpty()
                && mLyricsTags.containsKey(LyricsTag.TAG_TOTAL)) {
            try {
                total = Long.parseLong((String) mLyricsTags
                        .get(LyricsTag.TAG_TOTAL));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return total;

    }

    public void setLyricsType(int mLyricsType) {
        this.mLyricsType = mLyricsType;
    }

    public int getLyricsType() {
        return mLyricsType;
    }
}
