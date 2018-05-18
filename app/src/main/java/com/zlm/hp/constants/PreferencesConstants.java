package com.zlm.hp.constants;

import android.graphics.Color;

import com.zlm.hp.lyrics.utils.ColorUtils;

/**
 * 配置文件
 * Created by zhangliangming on 2017/8/6.
 */
public class PreferencesConstants {

    /**
     * 应用是否是第一次启动key
     */
    public static String isFrist_KEY = "isFrist_version2_KEY";

    /**
     * 应用是否在wifi下联网key
     */
    public static String isWifi_KEY = "isWifi_KEY";

    /**
     * 播放歌曲id key
     */
    public static String playIndexHashID_KEY = "playIndexHashID_KEY";

    /**
     * 是否是本地歌曲列表
     */
    public static String isLocalPlayList_KEY = "isLocalPlayList_KEY";

    /**
     * 歌曲播放模式key
     */
    public static String playModel_KEY = "playModel_KEY";


    public static String isBarMenuShow_KEY = "isBarMenuShow_KEY";


    /**
     * 歌词字体大小key
     */
    public static String lrcFontSize_KEY = "lrcFontSize_KEY";

    /**
     * 歌词颜色索引key
     */
    public static String lrcColorIndex_KEY = "lrcColorIndex_KEY";
    /**
     * 是否开启问候音key
     */
    public static String isSayHello_KEY = "isSayHello_KEY";

    /**
     * 是否线控key
     */
    public static String isWire_KEY = "isWire_KEY";


    /**
     * 是否显示桌面歌词
     */

    public static String isShowDesktop_KEY = "isShowDesktop_KEY";

    /**
     * 是否显示锁屏歌词key
     */

    public static String isShowLockScreen_KEY = "isShowLockScreen_KEY";

    /**
     * 是否是多行歌词
     */
    public static String isManyLineLrc_KEY = "isManyLineLrc_KEY";
    ///////////////////////////////如下是临时变量key//////////////////////////////////////////
    /**
     * 播放状态key
     */
    public static String playStatus_KEY = "playStatus_KEY";

    //////////////////////////////////////桌面歌词/////////////////////////////////////////
    /**
     * 桌面歌词是否可以移动
     */
    public static String desktopLyricsIsMove_KEY = "desktopLyricsIsMove_KEY";
    /***
     * 桌面歌词颜色索引
     */
    public static String desktopLrcColorIndex_KEY = "desktopLrcColorIndex_KEY";

    /**
     * 桌面歌词字体大小key
     */
    public static String desktopLrcFontSize_KEY = "desktopLrcFontSize_KEY";

    /**
     * 歌词窗口y坐标
     */
    public static String desktopLrcY_KEY = "desktopLrcY_KEY";

    /**
     * 桌面歌词未读颜色
     */
    public static int desktopLrcNotReadColors[][] = {{
            ColorUtils.parserColor("#00348a"),
            ColorUtils.parserColor("#0080c0"),
            ColorUtils.parserColor("#03cafc")
    }, {
            ColorUtils.parserColor("#ffffff"),
            ColorUtils.parserColor("#ffffff"),
            ColorUtils.parserColor("#ffffff")
    }, {
            ColorUtils.parserColor("#ffffff"),
            ColorUtils.parserColor("#ffffff"),
            ColorUtils.parserColor("#ffffff")
    }, {
            ColorUtils.parserColor("#ffac00"),
            ColorUtils.parserColor("#ff0000"),
            ColorUtils.parserColor("#aa0000")
    }, {
            ColorUtils.parserColor("#93ff26"),
            ColorUtils.parserColor("#46b000"),
            ColorUtils.parserColor("#005500")
    }};

    /**
     * 桌面歌词已读颜色
     */
    public static int desktopLrcReadedColors[][] = {{
            ColorUtils.parserColor("#82f7fd"),
            ColorUtils.parserColor("#ffffff"),
            ColorUtils.parserColor("#03e9fc")
    }, {
            ColorUtils.parserColor("#ffff00"),
            ColorUtils.parserColor("#ffff00"),
            ColorUtils.parserColor("#ffff00")
    }, {
            ColorUtils.parserColor("#e17db3"),
            ColorUtils.parserColor("#e17db3"),
            ColorUtils.parserColor("#e17db3")
    }, {
            ColorUtils.parserColor("#ffffa4"),
            ColorUtils.parserColor("#ffff00"),
            ColorUtils.parserColor("#ff641a")
    }, {
            ColorUtils.parserColor("#ffffff"),
            ColorUtils.parserColor("#9aff11"),
            ColorUtils.parserColor("#ffff00")
    }};
}
