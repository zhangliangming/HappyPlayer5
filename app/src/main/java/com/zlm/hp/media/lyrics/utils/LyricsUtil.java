package com.zlm.hp.media.lyrics.utils;

import android.content.Context;
import android.graphics.Paint;
import android.util.Base64;

import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.media.lyrics.LyricsFileReader;
import com.zlm.hp.media.lyrics.model.LyricsInfo;
import com.zlm.hp.media.lyrics.model.LyricsLineInfo;
import com.zlm.hp.media.lyrics.model.LyricsTag;
import com.zlm.hp.media.lyrics.model.TranslateLrcLineInfo;
import com.zlm.hp.utils.ResourceFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 歌词处理类
 * Created by zhangliangming on 2017/9/11.
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

    private LyricsInfo mLyricsIfno;

    /**
     * 歌词文件路径
     */
    private String mLrcFilePath;

    private String mHash;

    /**
     * 默认的歌词集合
     */
    private TreeMap<Integer, LyricsLineInfo> mDefLyricsLineTreeMap;

    /**
     * 翻译行歌词
     */
    private List<TranslateLrcLineInfo> mTranslateLrcLineInfos;
    /**
     * 音译歌词
     */
    private List<LyricsLineInfo> mTransliterationLrcLineInfos;

    /**
     * 没有的额外的歌词
     */
    public static final int NOEXTRA_LRC = 0;
    /**
     * 翻译歌词
     */
    public static final int TRANSLATE_LRC = 1;
    /**
     * 音译歌词
     */
    public static final int TRANSLITERATION_LRC = 2;

    /**
     * 翻译和音译歌词
     */
    public static final int TRANSLATE_AND_TRANSLITERATION_LRC = 3;

    /**
     * 额外的歌词类型
     */
    public int mExtraLrcType = NOEXTRA_LRC;

    /**
     * 通过音频文件名获取歌词文件
     *
     * @return
     */
    public static File getLrcFile(Context context, String fileName) {
        List<String> lrcExts = LyricsIOUtils.getSupportLyricsExts();
        for (int i = 0; i < lrcExts.size(); i++) {
            String lrcFilePath = ResourceFileUtil.getFilePath(context, ResourceConstants.PATH_LYRICS, fileName + "." + lrcExts.get(i));
            File lrcFile = new File(lrcFilePath);
            if (lrcFile.exists()) {
                return lrcFile;
            }
        }
        return null;
    }

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
            mDefLyricsLineTreeMap = mLyricsIfno.getLyricsLineInfoTreeMap();
            //翻译歌词集合
            if (mLyricsIfno.getTranslateLyricsInfo() != null)
                mTranslateLrcLineInfos = mLyricsIfno.getTranslateLyricsInfo().getTranslateLrcLineInfos();
            //音译歌词集合
            if (mLyricsIfno.getTransliterationLyricsInfo() != null)
                mTransliterationLrcLineInfos = getTransliterationLrc(mDefLyricsLineTreeMap, mLyricsIfno.getTransliterationLyricsInfo().getTransliterationLrcLineInfos());

            initExtraLrcType();
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
            mDefLyricsLineTreeMap = mLyricsIfno.getLyricsLineInfoTreeMap();
            //翻译歌词集合
            if (mLyricsIfno.getTranslateLyricsInfo() != null)
                mTranslateLrcLineInfos = mLyricsIfno.getTranslateLyricsInfo().getTranslateLrcLineInfos();
            //音译歌词集合
            if (mLyricsIfno.getTransliterationLyricsInfo() != null)
                mTransliterationLrcLineInfos = getTransliterationLrc(mDefLyricsLineTreeMap, mLyricsIfno.getTransliterationLyricsInfo().getTransliterationLrcLineInfos());

            initExtraLrcType();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从默认歌词集合中，替换音译歌词，获取音译歌词集合
     *
     * @param mDefLyricsLineTreeMap
     * @param transliterationLrcLineInfos
     * @return
     */
    private List<LyricsLineInfo> getTransliterationLrc(TreeMap<Integer, LyricsLineInfo> mDefLyricsLineTreeMap, List<LyricsLineInfo> transliterationLrcLineInfos) {
     if(mDefLyricsLineTreeMap == null) return null;
        List<LyricsLineInfo> newLyricsLineInfos = new ArrayList<LyricsLineInfo>();
        for (int i = 0; i < mDefLyricsLineTreeMap.size(); i++) {
            LyricsLineInfo origLyricsLineInfo = transliterationLrcLineInfos.get(i);
            LyricsLineInfo defLyricsLineInfo = mDefLyricsLineTreeMap.get(i);

            //构造新的音译行歌词
            LyricsLineInfo newLyricsLineInfo = new LyricsLineInfo();
            newLyricsLineInfo.copy(newLyricsLineInfo, defLyricsLineInfo);
            //
            String[] defLyricsWords = defLyricsLineInfo.getLyricsWords();
            String[] origLyricsWords = origLyricsLineInfo.getLyricsWords();
            String[] newLyricsWords = new String[defLyricsWords.length];
            String newLineLyrics = "";
            for (int j = 0; j < defLyricsWords.length; j++) {
                if (defLyricsWords[j].lastIndexOf(" ") != -1) {
                    newLyricsWords[j] = origLyricsWords[j].trim() + " ";
                } else {
                    String origLyricsWordsString = origLyricsWords[j].trim();
                    boolean isWord = origLyricsWordsString.matches("[a-zA-Z]+");
                    if (isWord) {
                        newLyricsWords[j] = origLyricsWords[j].trim() + " ";
                    } else {
                        newLyricsWords[j] = origLyricsWords[j].trim();
                    }
                }
                newLineLyrics += newLyricsWords[j];
            }

            newLyricsLineInfo.setLyricsWords(newLyricsWords);
            newLyricsLineInfo.setLineLyrics(newLineLyrics);

            newLyricsLineInfos.add(newLyricsLineInfo);
        }

        return newLyricsLineInfos;
    }

    /**
     * 初始化额外歌词类型
     */
    private void initExtraLrcType() {

        //判断音译和翻译歌词
        if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0 && mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
            //有翻译歌词和音译歌词
            mExtraLrcType = TRANSLATE_AND_TRANSLITERATION_LRC;
        } else if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {
            //有翻译歌词
            mExtraLrcType = TRANSLATE_LRC;
        } else if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
            //音译歌词
            mExtraLrcType = TRANSLITERATION_LRC;
        } else {
            //无翻译歌词和音译歌词
            mExtraLrcType = NOEXTRA_LRC;
        }
    }

    /**
     * 通过播放的进度，获取所唱歌词行数
     *
     * @param oldPlayingTime
     * @return
     */
    public int getLineNumber(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int oldPlayingTime) {

        //添加歌词增量
        int curPlayingTime = oldPlayingTime + getPlayOffset();

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
     * 获取分割后的歌词行索引
     *
     * @param lyricsLineTreeMap
     * @param origLineNumber    原行号
     * @param oldPlayingTime
     * @return
     */
    public int getSplitLyricsLineNum(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int origLineNumber, int oldPlayingTime) {
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(origLineNumber);
        List<LyricsLineInfo> lyricsLineInfos = lyrLine.getSplitLyricsLineInfos();
        return getSplitLyricsLineNum(lyricsLineInfos, oldPlayingTime);
    }

    /**
     * 获取分割音译歌词索引
     *
     * @param lyricsLineInfos
     * @param origLineNumber
     * @param oldPlayingTime
     * @return
     */
    public int getSplitTransliterationLyricsLineNum(List<LyricsLineInfo> lyricsLineInfos, int origLineNumber, int oldPlayingTime) {
        LyricsLineInfo lyrLine = lyricsLineInfos.get(origLineNumber);
        List<LyricsLineInfo> newLineInfos = lyrLine.getSplitLyricsLineInfos();
        return getSplitLyricsLineNum(newLineInfos, oldPlayingTime);
    }

    /**
     * 获取分割后的行索引
     *
     * @param lyricsLineInfos
     * @param oldPlayingTime
     * @return
     */
    private int getSplitLyricsLineNum(List<LyricsLineInfo> lyricsLineInfos, int oldPlayingTime) {
        //添加歌词增量
        int curPlayingTime = oldPlayingTime + getPlayOffset();
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            if (curPlayingTime >= lyricsLineInfos.get(i).getStartTime()
                    && curPlayingTime <= lyricsLineInfos.get(i).getEndTime()) {
                return i;
            }
            if (curPlayingTime > lyricsLineInfos.get(i).getEndTime()
                    && i + 1 < lyricsLineInfos.size()
                    && curPlayingTime < lyricsLineInfos.get(i + 1).getStartTime()) {
                return i;
            }
        }
        if (curPlayingTime >= lyricsLineInfos.get(lyricsLineInfos.size() - 1)
                .getEndTime()) {
            return lyricsLineInfos.size() - 1;
        }
        return 0;
    }

    /**
     * 获取当前时间正在唱的歌词的第几个字
     *
     * @param lyricsLineNum  行数
     * @param oldPlayingTime
     * @return
     */
    public int getDisWordsIndex(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int lyricsLineNum, int oldPlayingTime) {
        if (lyricsLineNum == -1)
            return -1;

        //添加歌词增量
        int curPlayingTime = oldPlayingTime + getPlayOffset();
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
        int elapseTime = lyrLine.getStartTime();
        for (int i = 0; i < lyrLine.getLyricsWords().length; i++) {

            elapseTime += lyrLine.getWordsDisInterval()[i];
            if (curPlayingTime < elapseTime) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取分割歌词后的歌词字索引
     *
     * @param lyricsLineTreeMap
     * @param lyricsLineNum
     * @param oldPlayingTime
     * @return
     */
    public int getSplitLyricsWordIndex(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int lyricsLineNum, int oldPlayingTime) {
        if (lyricsLineNum == -1)
            return -1;

        //添加歌词增量
        int curPlayingTime = oldPlayingTime + getPlayOffset();
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
        int elapseTime = lyrLine.getStartTime();
        List<LyricsLineInfo> lyricsLineInfos = lyrLine.getSplitLyricsLineInfos();
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            LyricsLineInfo temp = lyricsLineInfos.get(i);
            for (int j = 0; j < temp.getLyricsWords().length; j++) {
                elapseTime += temp.getWordsDisInterval()[j];
                if (curPlayingTime < elapseTime) {
                    return j;
                }
            }
        }
        return -1;
    }

    /**
     * 获取分割音译歌词字索引
     *
     * @param lyricsLineInfos
     * @param lyricsLineNum
     * @param oldPlayingTime
     * @return
     */
    public int getSplitTransliterationLyricsWordIndex(List<LyricsLineInfo> lyricsLineInfos, int lyricsLineNum, int oldPlayingTime) {
        if (lyricsLineNum == -1)
            return -1;

        //添加歌词增量
        int curPlayingTime = oldPlayingTime + getPlayOffset();
        LyricsLineInfo lyrLine = lyricsLineInfos.get(lyricsLineNum);
        int elapseTime = lyrLine.getStartTime();
        List<LyricsLineInfo> newLyricsLineInfos = lyrLine.getSplitLyricsLineInfos();
        for (int i = 0; i < newLyricsLineInfos.size(); i++) {
            LyricsLineInfo temp = newLyricsLineInfos.get(i);
            for (int j = 0; j < temp.getLyricsWords().length; j++) {
                elapseTime += temp.getWordsDisInterval()[j];
                if (curPlayingTime < elapseTime) {
                    return j;
                }
            }
        }
        return -1;
    }

    /**
     * 获取当前歌词的第几个歌词的播放时间
     *
     * @param lyricsLineNum  行数
     * @param oldPlayingTime
     * @return
     */
    public int getDisWordsIndexLenTime(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int lyricsLineNum, int oldPlayingTime) {
        if (lyricsLineNum == -1)
            return 0;
        //添加歌词增量
        int curPlayingTime = oldPlayingTime + getPlayOffset();
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
        int elapseTime = lyrLine.getStartTime();
        for (int i = 0; i < lyrLine.getLyricsWords().length; i++) {
            elapseTime += lyrLine.getWordsDisInterval()[i];
            if (curPlayingTime < elapseTime) {
                return lyrLine.getWordsDisInterval()[i] - (elapseTime - curPlayingTime);
            }
        }
        return 0;
    }

    //--------分割歌词-------//

    /**
     * 播放的时间补偿值
     *
     * @return
     */
    public int getPlayOffset() {
        return mDefOffset + mOffset;
    }

    /**
     * 获取分割歌词
     *
     * @param textMaxWidth 歌词行最大宽度
     * @param paint
     * @return
     */
    public synchronized TreeMap<Integer, LyricsLineInfo> getSplitLyrics(int textMaxWidth, Paint paint) {
        if (mDefLyricsLineTreeMap == null) return null;
        // long startTime = System.currentTimeMillis();
        TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap = new TreeMap<Integer, LyricsLineInfo>();
        for (int i = 0; i < mDefLyricsLineTreeMap.size(); i++) {
            LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
            //复制
            lyricsLineInfo.copy(lyricsLineInfo, mDefLyricsLineTreeMap.get(i));
            //分割歌词
            splitLyrics(lyricsLineInfo, paint, textMaxWidth);

            lyricsLineTreeMap.put(i, lyricsLineInfo);
        }
        // long endTime = System.currentTimeMillis();
        //long time  = endTime - startTime;
        return lyricsLineTreeMap;
    }

    //--------分割歌词结束-------//

    //--------分割音译歌词开始-------//

    /**
     * 获取分割音译歌词集合
     *
     * @param textMaxWidth
     * @param paint
     * @return
     */
    public synchronized List<LyricsLineInfo> getSplitTransliterationLyrics(int textMaxWidth, Paint paint) {
        List<LyricsLineInfo> transliterationLrcLineInfos = new ArrayList<LyricsLineInfo>();

        if (mTransliterationLrcLineInfos != null) {
            for (int i = 0; i < mTransliterationLrcLineInfos.size(); i++) {

                LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
                lyricsLineInfo.copy(lyricsLineInfo, mTransliterationLrcLineInfos.get(i));

                //分隔音译歌词
                splitLyrics(lyricsLineInfo, paint, textMaxWidth);
                transliterationLrcLineInfos.add(lyricsLineInfo);
            }
        }

        return transliterationLrcLineInfos;
    }

    //--------分割音译歌词结束-------//


    //--------分割翻译歌词开始-------//

    /**
     * 分割歌词
     *
     * @param lyricsLineInfo
     * @param paint
     * @param textMaxWidth
     */
    private void splitLyrics(LyricsLineInfo lyricsLineInfo, Paint paint, int textMaxWidth) {

        final List<LyricsLineInfo> lyricsLineInfos = new ArrayList<LyricsLineInfo>();
        splitLineLyrics(lyricsLineInfo, paint, textMaxWidth, new ForeachListener() {
            @Override
            public void foreach(LyricsLineInfo mLyricsLineInfo) {
                lyricsLineInfos.add(mLyricsLineInfo);
            }
        });

        lyricsLineInfo.setSplitLyricsLineInfos(lyricsLineInfos);

    }


    //--------分割歌词结束-------//

    //--------重构歌词-------//

    /**
     * 获取分割翻译行歌词
     *
     * @param textMaxWidth
     * @param paint
     * @return
     */
    public synchronized List<TranslateLrcLineInfo> getSplitTranslateLyrics(int textMaxWidth, Paint paint) {
        List<TranslateLrcLineInfo> translateLrcLineInfos = new ArrayList<TranslateLrcLineInfo>();
        if (mTranslateLrcLineInfos != null) {
            for (int i = 0; i < mTranslateLrcLineInfos.size(); i++) {
                TranslateLrcLineInfo translateLrcLineInfo = new TranslateLrcLineInfo();
                //复制
                translateLrcLineInfo.copy(translateLrcLineInfo, mTranslateLrcLineInfos.get(i));
                splitTranslateLineLrc(translateLrcLineInfo, paint, textMaxWidth);

                translateLrcLineInfos.add(translateLrcLineInfo);
            }
        }
        return translateLrcLineInfos;
    }

    /**
     * 获取分割后的翻译歌词
     *
     * @param translateLrcLineInfo
     * @param paint
     * @param textMaxWidth
     */
    private void splitTranslateLineLrc(TranslateLrcLineInfo translateLrcLineInfo, Paint paint, int textMaxWidth) {
        final List<TranslateLrcLineInfo> splitTranslateLrcLineInfos = new ArrayList<TranslateLrcLineInfo>();

        getSplitTranslateLineLrc(translateLrcLineInfo, paint, textMaxWidth, new TranslateForeachListener() {
            @Override
            public void foreach(TranslateLrcLineInfo mTranslateLrcLineInfo) {

                splitTranslateLrcLineInfos.add(mTranslateLrcLineInfo);
            }
        });

        translateLrcLineInfo.setSplitTranslateLrcLineInfos(splitTranslateLrcLineInfos);
    }

    /**
     * 获取翻译分割行歌词
     *
     * @param translateLrcLineInfo
     * @param paint
     * @param textMaxWidth
     * @param translateForeachListener
     */
    private void getSplitTranslateLineLrc(TranslateLrcLineInfo translateLrcLineInfo, Paint paint, int textMaxWidth, TranslateForeachListener translateForeachListener) {
        String lineLyrics = translateLrcLineInfo.getLineLyrics();
        // 每行的歌词长度
        int lineWidth = (int) paint.measureText(lineLyrics);
        int maxLineWidth = textMaxWidth;
        if (lineWidth > maxLineWidth) {

            int lyricsWordsWidth = 0;
            //开始索引和结束索引
            int startIndex = 0;
            for (int i = 0; i < lineLyrics.length(); i++) {
                // 当前的歌词宽度
                lyricsWordsWidth += (int) paint.measureText(lineLyrics.charAt(i) + "");
                //下一个字的宽度
                int nextLyricsWordWidth = 0;
                if ((i + 1) < lineLyrics.length()) {
                    nextLyricsWordWidth = (int) paint.measureText(lineLyrics.charAt(i) + "");
                }
                if (lyricsWordsWidth + nextLyricsWordWidth > maxLineWidth) {

                    TranslateLrcLineInfo newTranslateLrcLineInfo1 = getNewTranslateLrcLineInfo(lineLyrics, startIndex, i);

                    if (newTranslateLrcLineInfo1 != null && translateForeachListener != null) {
                        translateForeachListener.foreach(newTranslateLrcLineInfo1);
                    }

                    //
                    lyricsWordsWidth = 0;
                    startIndex = i + 1;
                    if (startIndex == lineLyrics.length()) {
                        startIndex = lineLyrics.length() - 1;
                    }
                } else if (i == lineLyrics.length() - 1) {
                    TranslateLrcLineInfo newTranslateLrcLineInfo1 = getNewTranslateLrcLineInfo(lineLyrics, startIndex, lineLyrics.length() - 1);

                    if (newTranslateLrcLineInfo1 != null && translateForeachListener != null) {
                        translateForeachListener.foreach(newTranslateLrcLineInfo1);
                    }
                }
            }

        } else {
            if (translateForeachListener != null) {
                translateForeachListener.foreach(translateLrcLineInfo);
            }
        }
    }

    /**
     * 获取新的分割翻译歌词
     *
     * @param lineLyrics
     * @param startIndex
     * @param lastIndex
     * @return
     */
    private TranslateLrcLineInfo getNewTranslateLrcLineInfo(String lineLyrics, int startIndex, int lastIndex) {

        if (lastIndex < 0)
            return null;

        TranslateLrcLineInfo translateLrcLineInfo = new TranslateLrcLineInfo();
        translateLrcLineInfo.setLineLyrics(lineLyrics.substring(startIndex, lastIndex + 1));

        return translateLrcLineInfo;
    }

    /**
     * 获取重构后的歌词
     *
     * @param textMaxWidth 歌词最大长度
     * @param paint
     * @return
     */
    public synchronized TreeMap<Integer, LyricsLineInfo> getReconstructLyrics(int textMaxWidth, Paint paint) {
        if (mDefLyricsLineTreeMap == null) return null;
        // 这里面key为该行歌词的开始时间，方便后面排序
        SortedMap<Integer, LyricsLineInfo> lyricsLineInfosTemp = new TreeMap<Integer, LyricsLineInfo>();
        for (int i = 0; i < mDefLyricsLineTreeMap.size(); i++) {

            LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
            //复制
            lyricsLineInfo.copy(lyricsLineInfo, mDefLyricsLineTreeMap.get(i));
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

        splitLineLyrics(lyricsLineInfo, paint, textMaxWidth, new ForeachListener() {
            @Override
            public void foreach(LyricsLineInfo mLyricsLineInfo) {
                lyricsLineInfosTemp.put(mLyricsLineInfo.getStartTime(),
                        mLyricsLineInfo);
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
    private void splitLineLyrics(LyricsLineInfo lyricsLineInfo, Paint paint, int textMaxWidth, ForeachListener foreachListener) {
        String lineLyrics = lyricsLineInfo.getLineLyrics().trim();
        // 行歌词数组
        String[] lyricsWords = lyricsLineInfo.getLyricsWords();
        // 每行的歌词长度
        int lineWidth = (int) paint.measureText(lineLyrics);
        int maxLineWidth = textMaxWidth;
        if (lineWidth > maxLineWidth) {

            int lyricsWordsWidth = 0;
            //开始索引和结束索引
            int startIndex = 0;
            for (int i = 0; i < lyricsWords.length; i++) {
                // 当前的歌词宽度
                lyricsWordsWidth += (int) paint.measureText(lyricsWords[i]);
                //下一个字的宽度
                int nextLyricsWordWidth = 0;
                if ((i + 1) < lyricsWords.length) {
                    nextLyricsWordWidth = (int) paint.measureText(lyricsWords[(i + 1)]);
                }
                if (lyricsWordsWidth + nextLyricsWordWidth > maxLineWidth) {

                    LyricsLineInfo newLyricsLineInfo = getNewLyricsLineInfo(
                            lyricsLineInfo, startIndex, i);

                    if (newLyricsLineInfo != null && foreachListener != null) {
                        foreachListener.foreach(newLyricsLineInfo);
                    }

                    //
                    lyricsWordsWidth = 0;
                    startIndex = i + 1;
                    if (startIndex == lyricsWords.length) {
                        startIndex = lyricsWords.length - 1;
                    }
                } else if (i == lyricsWords.length - 1) {
                    LyricsLineInfo newLyricsLineInfo = getNewLyricsLineInfo(
                            lyricsLineInfo, startIndex, lyricsWords.length - 1);

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
    //--------重构歌词结束------//

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

    public String getHash() {
        return mHash;
    }

    public void setHash(String mHash) {
        this.mHash = mHash;
    }

    public LyricsInfo getLyricsIfno() {
        return mLyricsIfno;
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

    ////////////////////////////////////////////////////////////////////////////////

    public void setOffset(int offset) {
        this.mOffset = offset;
    }

    public int getExtraLrcType() {
        return mExtraLrcType;
    }

    /**
     * 获取当前时间对应的行歌词
     *
     * @param oldPlayingTime
     * @return
     */
    public String getLineLrc(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int oldPlayingTime) {
        //添加歌词增量
        int curPlayingTime = oldPlayingTime + getPlayOffset();
        int index = getLineNumber(lyricsLineTreeMap, curPlayingTime);
        if (lyricsLineTreeMap == null || index >= lyricsLineTreeMap.size())
            return null;
        LyricsLineInfo lyricsLineInfo = lyricsLineTreeMap.get(index);
        if (lyricsLineInfo == null)
            return null;
        return lyricsLineInfo.getLineLyrics();
    }

    /**
     * 默认歌词遍历
     */
    public interface ForeachListener {
        /**
         * 遍历
         *
         * @param lyricsLineInfo
         */
        public void foreach(LyricsLineInfo lyricsLineInfo);

    }

    /**
     * 翻译歌词遍历
     */
    public interface TranslateForeachListener {
        public void foreach(TranslateLrcLineInfo translateLrcLineInfo);
    }

}
