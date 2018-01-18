package com.zlm.hp.constants;

import android.content.Context;

import com.zlm.hp.manager.AudioPlayerManager;

import base.utils.PreferencesUtil;

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
     * 是否桌面歌词key
     */
    public static String isDesktop_KEY = "isDesktop_KEY";

    /**
     * 是否锁屏歌词key
     */
    public static String isLockScreen_KEY = "isLockScreen_KEY";

    /**
     * 是否锁屏歌词key
     */
    public static String isShowLockScreen_KEY = "isShowLockScreen_KEY";

    /**
     * 是否是多行歌词
     */
    public static String isManyLineLrc_KEY = "isManyLineLrc_KEY";
    public static String musicId_KEY = "music_id";
    public static String playMode_KEY = "play_mode";
    public static String splashUrl_KEY = "splash_url";
    public static String nightMode_KEY = "night_mode";

    /**
     * 歌词字体大小
     */
    private static int lrcFontSize = 50;
    /**
     * 最小字体大小
     */
    private static int minLrcFontSize = 50;

    /**
     * 最大字体大小
     */
    private static int maxLrcFontSize = 70;
    /**
     * 歌词颜色索引
     */
    private static int lrcColorIndex = 0;

    /**
     * 歌词颜色集合
     */
    private static String[] lrcColorStr = {"#fada83", "#fe8db6", "#feb88e",
            "#adfe8e", "#8dc7ff", "#e69bff"};
    ///////////////////////////////如下是临时变量key//////////////////////////////////////////
    /**
     * 播放状态key
     */
    public static String playStatus_KEY = "playStatus_KEY";
    /**
     * SwipeBackLayout阴影是否可用
     */
    public static String shadowEnable_KEY = "shadowEnable_KEY";

    public static boolean isFrist(Context context) {
        return PreferencesUtil.getBooleanValue(context, isFrist_KEY, true);
    }

    public static void setFrist(Context context, boolean frist) {
        PreferencesUtil.putBooleanVaule(context, PreferencesConstants.isSayHello_KEY, false);
    }

    public static boolean isSayHello(Context context) {
        return PreferencesUtil.getBooleanValue(context, isFrist_KEY, true);
    }

    public static void setSayHello(Context context, boolean isSayHello) {
        PreferencesUtil.putBooleanVaule(context, PreferencesConstants.isSayHello_KEY, isSayHello);
    }

    public static boolean isWifi(Context context) {
        return PreferencesUtil.getBooleanValue(context, PreferencesConstants.isWifi_KEY, true);
    }

    public static void setWifi(Context context, boolean wifi) {
        PreferencesUtil.putBooleanVaule(context, PreferencesConstants.isWifi_KEY, wifi);
    }

    public static boolean isWire(Context context) {
        return PreferencesUtil.getBooleanValue(context, PreferencesConstants.isWire_KEY, false);
    }

    public static void setWire(Context context, boolean wire) {
        PreferencesUtil.putBooleanVaule(context, PreferencesConstants.isWire_KEY, wire);
    }

    public static boolean isDesktop(Context context) {
        return PreferencesUtil.getBooleanValue(context, PreferencesConstants.isDesktop_KEY, false);
    }

    public static void setDesktop(Context context, boolean desktop) {
        PreferencesUtil.putBooleanVaule(context, PreferencesConstants.isDesktop_KEY, desktop);
    }

    public static boolean isLockScreen(Context context) {
        return PreferencesUtil.getBooleanValue(context, PreferencesConstants.isLockScreen_KEY, false);
    }

    public static void setLockScreen(Context context, boolean lockScreen) {
        PreferencesUtil.putBooleanVaule(context, PreferencesConstants.isLockScreen_KEY, lockScreen);
    }

    public static boolean isShowLockScreen(Context context) {
        return PreferencesUtil.getBooleanValue(context, PreferencesConstants.isShowLockScreen_KEY, true);
    }

    public static void setShowLockScreen(Context context, boolean lockScreen) {
        PreferencesUtil.putBooleanVaule(context, PreferencesConstants.isShowLockScreen_KEY, lockScreen);
    }

    public static String getPlayIndexHashID(Context context) {
        return PreferencesUtil.getStringValue(context, PreferencesConstants.playIndexHashID_KEY, "");
    }

    public static void setPlayIndexHashID(Context context, String playIndexHashID) {
        PreferencesUtil.putStringVaule(context, PreferencesConstants.playIndexHashID_KEY, playIndexHashID);
    }

    public static boolean isBarMenuShow(Context context) {
        return PreferencesUtil.getBooleanValue(context, PreferencesConstants.isBarMenuShow_KEY, false);
    }

    public static void setBarMenuShow(Context context, boolean barMenuShow) {
        PreferencesUtil.putBooleanVaule(context, PreferencesConstants.isBarMenuShow_KEY, barMenuShow);
    }

    public static int getPlayModel(Context context) {
        // 0是 顺序播放 1是随机播放 2是循环播放 3是单曲播放
        return PreferencesUtil.getIntValue(context, PreferencesConstants.playModel_KEY, 0);
    }

    /**
     * // 0是 顺序播放 1是随机播放 2是循环播放 3是单曲播放
     *
     * @param context
     * @param playModel
     */
    public static void setPlayModel(Context context, int playModel) {
        PreferencesUtil.putIntVaule(context, PreferencesConstants.playModel_KEY, playModel);
    }

    public static int getPlayStatus(Context context) {
        return PreferencesUtil.getIntValue(context, PreferencesConstants.playStatus_KEY, AudioPlayerManager.STOP);
    }

    public static void setPlayStatus(Context context, int playStatus) {
        PreferencesUtil.putIntVaule(context, PreferencesConstants.playStatus_KEY, playStatus);
    }



    public static int getLrcColorIndex(Context context) {
        return PreferencesUtil.getIntValue(context, PreferencesConstants.lrcColorIndex_KEY, 0);
    }

    public static void setLrcColorIndex(Context context, int lrcColorIndex) {
        PreferencesUtil.putIntVaule(context, PreferencesConstants.lrcColorIndex_KEY, lrcColorIndex);
    }

    public static int getLrcFontSize(Context context) {
        return PreferencesUtil.getIntValue(context, PreferencesConstants.lrcFontSize_KEY, lrcFontSize);
    }

    public static void setLrcFontSize(Context context, int lrcFontSize) {
        PreferencesUtil.putIntVaule(context, PreferencesConstants.lrcFontSize_KEY, lrcFontSize);
    }

    public static boolean isManyLineLrc(Context context) {
        return PreferencesUtil.getBooleanValue(context, PreferencesConstants.isManyLineLrc_KEY, true);
    }

    public static void setManyLineLrc(Context context, boolean isManyLineLrc) {
        PreferencesUtil.putBooleanVaule(context, PreferencesConstants.isManyLineLrc_KEY, true);
    }

    public static int getMaxLrcFontSize() {
        return maxLrcFontSize;
    }

    public static int getMinLrcFontSize() {
        return minLrcFontSize;
    }

    public static int getCurLrcFontSize() {
        return lrcFontSize;
    }

    public static String[] getLrcColorStr() {
        return lrcColorStr;
    }
}
