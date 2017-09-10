package com.zlm.hp.utils;

import android.content.Context;
import android.graphics.Paint;
import android.util.Base64;

import com.happy.lyrics.LyricsFileReader;
import com.happy.lyrics.model.LyricsInfo;
import com.happy.lyrics.model.LyricsLineInfo;
import com.happy.lyrics.model.LyricsTag;
import com.happy.lyrics.utils.LyricsIOUtils;
import com.zlm.hp.constants.ResourceConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 歌词加载器
 * Created by zhangliangming on 2017/8/13.
 */

public class LyricsUtil {
    /**
     * 时间补偿值,其单位是毫秒，正值表示整体提前，负值相反。这是用于总体调整显示快慢的。
     */
    private int mDefOffset = 0;
    /**
     * 增量
     */
    private int mOffset = 0;

    /**
     * 默认的歌词集合
     */
    private TreeMap<Integer, LyricsLineInfo> mDefLyricsLineTreeMap = null;

    private LyricsInfo mLyricsIfno = null;

    /**
     * 歌词文件路径
     */
    private String mLrcFilePath;

    private String mHash;

    /**
     * 加载歌词数据
     *
     * @param lyricsFile
     */
    public void loadLrc(File lyricsFile) {
        mLrcFilePath = lyricsFile.getPath();
        LyricsFileReader lyricsFileReader = LyricsIOUtils.getLyricsFileReader(lyricsFile);
        try {
            mLyricsIfno = lyricsFileReader.readFile(lyricsFile);
            Map<String, Object> tags = mLyricsIfno.getLyricsTags();
            if (tags.containsKey(LyricsTag.TAG_OFFSET)) {
                mDefOffset = Integer.parseInt((String) tags.get(LyricsTag.TAG_OFFSET));
            } else {
                mDefOffset = 0;
            }
            mDefLyricsLineTreeMap = mLyricsIfno.getLyricsLineInfos();
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
            mLyricsIfno = lyricsFileReader.readLrcText(base64ByteArray, saveLrcFile);
            Map<String, Object> tags = mLyricsIfno.getLyricsTags();
            if (tags.containsKey(LyricsTag.TAG_OFFSET)) {
                mDefOffset = Integer.parseInt((String) tags.get(LyricsTag.TAG_OFFSET));
            } else {
                mDefOffset = 0;
            }
            mDefLyricsLineTreeMap = mLyricsIfno.getLyricsLineInfos();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过播放的进度，获取所唱歌词行数
     *
     * @param curPlayingTime
     * @return
     */
    public int getLineNumber(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int curPlayingTime) {
        for (int i = 0; i < lyricsLineTreeMap.size(); i++) {
            if (curPlayingTime >= lyricsLineTreeMap.get(i).getStartTime()
                    && curPlayingTime <= lyricsLineTreeMap.get(i).getEndTime()) {
                return i;
            }
            if (curPlayingTime > lyricsLineTreeMap.get(i).getEndTime()
                    && i + 1 < lyricsLineTreeMap.size()
                    && curPlayingTime < lyricsLineTreeMap.get(i + 1).getStartTime()) {
                return i;
            }
        }
        if (curPlayingTime >= lyricsLineTreeMap.get(lyricsLineTreeMap.size() - 1)
                .getEndTime()) {
            return lyricsLineTreeMap.size() - 1;
        }
        return 0;
    }

    /**
     * 获取当前时间正在唱的歌词的第几个字
     *
     * @param lyricsLineNum  行数
     * @param curPlayingTime
     * @return
     */
    public int getDisWordsIndex(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int lyricsLineNum, int curPlayingTime) {
        if (lyricsLineNum == -1)
            return -1;
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
        int elapseTime = lyrLine.getStartTime();
        for (int i = 0; i < lyrLine.getLyricsWords().length; i++) {
            elapseTime += lyrLine.wordsDisInterval[i];
            if (curPlayingTime < elapseTime) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取当前歌词的第几个歌词的播放时间
     *
     * @param lyricsLineNum  行数
     * @param curPlayingTime
     * @return
     */
    public int getDisWordsIndexLenTime(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int lyricsLineNum, int curPlayingTime) {
        if (lyricsLineNum == -1)
            return 0;
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
        int elapseTime = lyrLine.getStartTime();
        for (int i = 0; i < lyrLine.getLyricsWords().length; i++) {
            elapseTime += lyrLine.wordsDisInterval[i];
            if (curPlayingTime < elapseTime) {
                return lyrLine.wordsDisInterval[i] - (elapseTime - curPlayingTime);
            }
        }
        return 0;
    }
    //--------分割歌词-------//

    /**
     * 获取分割后的歌词列表
     *
     * @param lyricsLineInfo 原歌词集合
     * @param textMaxWidth
     * @param paint
     * @return
     */
    public List<LyricsLineInfo> getSplitLyrics(LyricsLineInfo lyricsLineInfo, int textMaxWidth, Paint paint) {
        final List<LyricsLineInfo> lyricsLineInfos = new ArrayList<LyricsLineInfo>();

        splitLyrics(lyricsLineInfo, paint, textMaxWidth, new ForeachListener() {
            @Override
            public void foreach(LyricsLineInfo lyricsLineInfo) {
                lyricsLineInfos.add(0, lyricsLineInfo);
            }
        });
        return lyricsLineInfos;
    }

    /**
     * 根据歌词字索引，获取分割后的歌词索引行
     *
     * @param lyricsLineInfos     分割后的歌词列表
     * @param lyricsDisWordsIndex 当前的歌词字索引
     * @return
     */
    public int getSplitLyricsLineNum(List<LyricsLineInfo> lyricsLineInfos, int lyricsDisWordsIndex) {
        int index = 0;
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            LyricsLineInfo temp = lyricsLineInfos.get(i);
            index += temp.getLyricsWords().length;
            if (index > lyricsDisWordsIndex) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 根据歌词字索引和分割后的当前行索引，获取分割后的歌词字索引
     *
     * @param lyricsLineInfos
     * @param lyricsLineNum
     * @param lyricsDisWordsIndex @return
     */
    public int getSplitLyricsWordIndex(List<LyricsLineInfo> lyricsLineInfos, int lyricsLineNum, int lyricsDisWordsIndex) {

        int ldwSum = 0;
        for (int i = 0; i < lyricsLineNum; i++) {
            ldwSum += lyricsLineInfos.get(i).getLyricsWords().length - 1;
        }
        String[] lyricsWords = lyricsLineInfos.get(lyricsLineNum).getLyricsWords();
        for (int j = 0; j < lyricsWords.length; j++) {

            if (ldwSum == lyricsDisWordsIndex) {
                return j;
            }

            ldwSum++;
        }

        return -1;
    }


    //

    //--------重构歌词-------//

    /**
     * 获取重构后的歌词
     *
     * @param textMaxWidth 歌词最大长度
     * @param paint
     * @return
     */
    private TreeMap<Integer, LyricsLineInfo> getReconstructLyrics(int textMaxWidth, Paint paint) {
        // 这里面key为该行歌词的开始时间，方便后面排序
        SortedMap<Integer, LyricsLineInfo> lyricsLineInfosTemp = new TreeMap<Integer, LyricsLineInfo>();
        for (int i = 0; i < mDefLyricsLineTreeMap.size(); i++) {

            LyricsLineInfo lyricsLineInfo = mDefLyricsLineTreeMap.get(i);
            reconstructLyrics(lyricsLineInfo, lyricsLineInfosTemp,
                    paint, textMaxWidth);
        }
        TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap = new TreeMap<Integer, LyricsLineInfo>();

        int index = 0;
        Iterator<Integer> it = lyricsLineInfosTemp.keySet().iterator();
        while (it.hasNext()) {
            lyricsLineTreeMap.put(index++, lyricsLineInfosTemp.get(it.next()));
        }

        return lyricsLineTreeMap;
    }

    /**
     * 重构歌词
     *
     * @param lyricsLineInfo
     * @param lyricsLineInfosTemp
     * @param paint
     * @param textMaxWidth
     */
    private void reconstructLyrics(LyricsLineInfo lyricsLineInfo,
                                   final SortedMap<Integer, LyricsLineInfo> lyricsLineInfosTemp,
                                   Paint paint, int textMaxWidth) {

        splitLyrics(lyricsLineInfo, paint, textMaxWidth, new ForeachListener() {
            @Override
            public void foreach(LyricsLineInfo lyricsLineInfo) {
                lyricsLineInfosTemp.put(lyricsLineInfo.getStartTime(),
                        lyricsLineInfo);
            }
        });

    }

    /**
     * 分割歌词
     *
     * @param lyricsLineInfo
     * @param paint
     * @param textMaxWidth
     * @param foreachListener
     */
    private void splitLyrics(LyricsLineInfo lyricsLineInfo, Paint paint, int textMaxWidth, ForeachListener foreachListener) {
        String lineLyrics = lyricsLineInfo.getLineLyrics();
        // 行歌词数组
        String[] lyricsWords = lyricsLineInfo.getLyricsWords();
        // 每行的歌词长度
        int lineWidth = (int) paint.measureText(lineLyrics);
        int maxLineWidth = textMaxWidth;
        if (lineWidth > maxLineWidth) {
            // 最大的歌词行数
            int maxLrcLineNum = lineWidth % maxLineWidth == 0 ? lineWidth
                    / maxLineWidth : (lineWidth / maxLineWidth + 1);
            // 最大的行歌词长度
            int maxLrcLineWidth = lineWidth / maxLrcLineNum;
            // 大于视图的宽度
            int lastIndex = lyricsWords.length - 1;
            int lyricsWordsWidth = 0;
            for (int i = lyricsWords.length - 1; i >= 0; i--) {

                // 当前的歌词宽度
                lyricsWordsWidth += (int) paint.measureText(lyricsWords[i]);
                // 上一个字的宽度
                int preLyricsWordWidth = 0;
                if ((i - 1) > 0) {
                    preLyricsWordWidth = (int) paint.measureText(lyricsWords[(i - 1)]);
                }
                if (lyricsWordsWidth + preLyricsWordWidth > maxLrcLineWidth) {

                    LyricsLineInfo newLyricsLineInfo = getNewLyricsLineInfo(
                            lyricsLineInfo, i, lastIndex);

                    if (newLyricsLineInfo != null && foreachListener != null) {
                        foreachListener.foreach(newLyricsLineInfo);
                    }

                    //
                    lastIndex = i - 1;
                    lyricsWordsWidth = 0;
                } else if (i == 0) {
                    LyricsLineInfo newLyricsLineInfo = getNewLyricsLineInfo(
                            lyricsLineInfo, 0, lastIndex);

                    if (newLyricsLineInfo != null && foreachListener != null) {
                        foreachListener.foreach(newLyricsLineInfo);
                    }
                }
            }

        } else {
            if (foreachListener != null) {
                foreachListener.foreach(lyricsLineInfo);
            }
        }
    }

    /**
     * 根据新歌词的索引和旧歌词数据，构造新的歌词数据
     *
     * @param lyricsLineInfo 旧的行歌词数据
     * @param startIndex     开始歌词索引
     * @param lastIndex      结束歌词索引
     * @return
     */
    private LyricsLineInfo getNewLyricsLineInfo(
            LyricsLineInfo lyricsLineInfo, int startIndex, int lastIndex) {

        if (lastIndex < 0)
            return null;
        LyricsLineInfo newLyricsLineInfo = new LyricsLineInfo();
        // 行开始时间
        int lineStartTime = lyricsLineInfo.getStartTime();
        int startTime = lineStartTime;
        int endTime = 0;
        String lineLyrics = "";
        List<String> lyricsWordsList = new ArrayList<String>();
        List<Integer> wordsDisIntervalList = new ArrayList<Integer>();
        String[] lyricsWords = lyricsLineInfo.getLyricsWords();
        int[] wordsDisInterval = lyricsLineInfo.getWordsDisInterval();
        for (int i = 0; i <= lastIndex; i++) {
            if (i < startIndex) {
                startTime += wordsDisInterval[i];
            } else {
                lineLyrics += lyricsWords[i];
                wordsDisIntervalList.add(wordsDisInterval[i]);
                lyricsWordsList.add(lyricsWords[i]);
                endTime += wordsDisInterval[i];
            }
        }
        endTime += startTime;
        //
        String[] newLyricsWords = lyricsWordsList
                .toArray(new String[lyricsWordsList.size()]);
        int newWordsDisInterval[] = getWordsDisIntervalList(wordsDisIntervalList);
        newLyricsLineInfo.setEndTime(endTime);
        newLyricsLineInfo.setStartTime(startTime);
        newLyricsLineInfo.setLineLyrics(lineLyrics);
        newLyricsLineInfo.setLyricsWords(newLyricsWords);
        newLyricsLineInfo.setWordsDisInterval(newWordsDisInterval);

        return newLyricsLineInfo;
    }

    /**
     * 获取每个歌词的时间
     *
     * @param wordsDisIntervalList
     * @return
     */
    private int[] getWordsDisIntervalList(
            List<Integer> wordsDisIntervalList) {
        int wordsDisInterval[] = new int[wordsDisIntervalList.size()];
        for (int i = 0; i < wordsDisIntervalList.size(); i++) {
            wordsDisInterval[i] = wordsDisIntervalList.get(i);
        }
        return wordsDisInterval;
    }
    //--------------//


    public LyricsInfo getLyricsIfno() {
        return mLyricsIfno;
    }

    public String getLrcFilePath() {
        return mLrcFilePath;
    }

    public int getOffset() {
        return mOffset;
    }

    public void setOffset(int offset) {
        this.mOffset = offset;
    }

    public void setLrcFilePath(String mLrcFilePath) {
        this.mLrcFilePath = mLrcFilePath;
    }

    /**
     * 播放的时间补偿值
     *
     * @return
     */
    public int getPlayOffset() {
        return mDefOffset + mOffset;
    }


    /**
     * 通过音频文件名获取歌词文件
     *
     * @return
     */
    public static File getLrcFile(Context context, String fileName) {
        List<String> lrcExts = LyricsIOUtils.getSupportLyricsExts();
        for (int i = 0; i < lrcExts.size(); i++) {
            String lrcFilePath = ResourceFileUtil.getFilePath(context, ResourceConstants.PATH_LYRICS) + File.separator
                    + fileName + "." + lrcExts.get(i);
            File lrcFile = new File(lrcFilePath);
            if (lrcFile.exists()) {
                return lrcFile;
            }
        }
        return null;
    }

    /**
     * 获取当前时间对应的行歌词
     *
     * @param progress
     * @return
     */
    public String getLineLrc(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int progress) {
        int index = getLineNumber(lyricsLineTreeMap, progress);
        if (lyricsLineTreeMap == null || index >= lyricsLineTreeMap.size())
            return null;
        LyricsLineInfo lyricsLineInfo = lyricsLineTreeMap.get(index);
        if (lyricsLineInfo == null)
            return null;
        return lyricsLineInfo.getLineLyrics();
    }


    /**
     * 获取重构后的歌词集合
     *
     * @param textMaxWidth
     * @param paint
     * @return
     */
    public TreeMap<Integer, LyricsLineInfo> getReconstructLyricsLineTreeMap(int textMaxWidth, Paint paint) {
        return getReconstructLyrics(textMaxWidth, paint);
    }

    public void setDefLyricsLineTreeMap(TreeMap<Integer, LyricsLineInfo> defLyricsLineTreeMap) {
        this.mDefLyricsLineTreeMap = defLyricsLineTreeMap;
    }

    public TreeMap<Integer, LyricsLineInfo> getDefLyricsLineTreeMap() {
        return mDefLyricsLineTreeMap;
    }

    public String getHash() {
        return mHash;
    }

    public void setHash(String mHash) {
        this.mHash = mHash;
    }

    /**
     *
     */
    public interface ForeachListener {
        /**
         * 遍历
         *
         * @param lyricsLineInfo
         */
        public void foreach(LyricsLineInfo lyricsLineInfo);

    }

}
