package com.happy.lyrics.utils;

import com.happy.lyrics.model.LyricsInfo;

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

}
