package base.lyrics;

import android.util.Base64;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import base.lyrics.formats.LyricsFileReader;
import base.lyrics.model.LyricsInfo;
import base.lyrics.model.LyricsLineInfo;
import base.lyrics.model.LyricsTag;
import base.lyrics.utils.LyricsIOUtils;
import base.lyrics.utils.LyricsUtils;

/**
 * 歌词读管理器
 * Created by zhangliangming on 2018-02-25.
 */

public class LyricsReader {

    /**
     * 时间补偿值,其单位是毫秒，正值表示整体提前，负值相反。这是用于总体调整显示快慢的。
     */
    private int mDefOffset = 0;
    /**
     * 增量
     */
    private int mOffset = 0;

    /**
     * 歌词类型
     */
    private int mLyricsType = LyricsInfo.DYNAMIC;

    /**
     * 歌词文件路径
     */
    private String mLrcFilePath;

    /**
     * 文件hash
     */
    private String mHash;

    /**
     * 原始歌词列表
     */
    private TreeMap<Integer, LyricsLineInfo> mLrcLineInfos;
    /**
     * 原始翻译行歌词列表
     */
    private List<LyricsLineInfo> mTranslateLrcLineInfos;
    /**
     * 原始音译歌词行
     */
    private List<LyricsLineInfo> mTransliterationLrcLineInfos;

    private LyricsInfo mLyricsInfo;

    public LyricsReader() {

    }


    /**
     * 加载歌词数据
     *
     * @param lyricsFile
     */
    public void loadLrc(File lyricsFile) {
        this.mLrcFilePath = lyricsFile.getPath();
        LyricsFileReader lyricsFileReader = LyricsIOUtils.getLyricsFileReader(lyricsFile);
        try {
            LyricsInfo lyricsInfo = lyricsFileReader.readFile(lyricsFile);
            parser(lyricsInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param base64FileContentString 歌词base64文件
     * @param saveLrcFile             要保存的的lrc文件
     * @param fileName                含后缀名的文件名称
     */
    public void loadLrc(String base64FileContentString, File saveLrcFile, String fileName) {
        loadLrc(Base64.decode(base64FileContentString, Base64.NO_WRAP), saveLrcFile, fileName);
    }

    /**
     * @param base64ByteArray 歌词base64数组
     * @param saveLrcFile
     * @param fileName
     */
    public void loadLrc(byte[] base64ByteArray, File saveLrcFile, String fileName) {
        if (saveLrcFile != null)
            mLrcFilePath = saveLrcFile.getPath();
        LyricsFileReader lyricsFileReader = LyricsIOUtils.getLyricsFileReader(fileName);
        try {
            LyricsInfo lyricsInfo = lyricsFileReader.readLrcText(base64ByteArray, saveLrcFile);
            parser(lyricsInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 解析
     *
     * @param lyricsInfo
     */
    private void parser(LyricsInfo lyricsInfo) {
        mLyricsInfo = lyricsInfo;
        mLyricsType = lyricsInfo.getLyricsType();
        Map<String, Object> tags = lyricsInfo.getLyricsTags();
        if (tags.containsKey(LyricsTag.TAG_OFFSET)) {
            mDefOffset = 0;
            try {
                mDefOffset = Integer.parseInt((String) tags.get(LyricsTag.TAG_OFFSET));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mDefOffset = 0;
        }
        //默认歌词行
        mLrcLineInfos = lyricsInfo.getLyricsLineInfoTreeMap();
        //翻译歌词集合
        if (lyricsInfo.getTranslateLrcLineInfos() != null)
            mTranslateLrcLineInfos = LyricsUtils.getTranslateLrc(mLyricsType, mLrcLineInfos, lyricsInfo.getTranslateLrcLineInfos());
        //音译歌词集合
        if (lyricsInfo.getTransliterationLrcLineInfos() != null)
            mTransliterationLrcLineInfos = LyricsUtils.getTransliterationLrc(mLyricsType, mLrcLineInfos, lyricsInfo.getTransliterationLrcLineInfos());

    }


    ////////////////////////////////////////////////////////////////////////////////


    public int getLyricsType() {
        return mLyricsType;
    }

    public TreeMap<Integer, LyricsLineInfo> getLrcLineInfos() {
        return mLrcLineInfos;
    }

    public List<LyricsLineInfo> getTranslateLrcLineInfos() {
        return mTranslateLrcLineInfos;
    }

    public List<LyricsLineInfo> getTransliterationLrcLineInfos() {
        return mTransliterationLrcLineInfos;
    }

    public String getHash() {
        return mHash;
    }

    public void setHash(String mHash) {
        this.mHash = mHash;
    }

    public String getLrcFilePath() {
        return mLrcFilePath;
    }

    public void setLrcFilePath(String mLrcFilePath) {
        this.mLrcFilePath = mLrcFilePath;
    }

    public int getOffset() {
        return mOffset;
    }

    public void setOffset(int offset) {
        this.mOffset = offset;
    }

    public LyricsInfo getLyricsInfo() {
        return mLyricsInfo;
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * 播放的时间补偿值
     *
     * @return
     */
    public int getPlayOffset() {
        return mDefOffset + mOffset;
    }
}
