package com.zlm.hp.application;

import android.support.multidex.MultiDexApplication;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.zlm.hp.constants.PreferencesConstants;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.libs.utils.LoggerUtil;
import com.zlm.hp.libs.utils.PreferencesUtil;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.net.entity.RankListResult;
import com.zlm.hp.utils.ResourceFileUtil;
import com.zlm.hp.utils.SerializableObjUtil;
import com.zlm.libs.register.RegisterHelper;

import java.io.File;
import java.util.List;

/**
 * Created by zhangliangming on 2017/7/15.
 */
public class HPApplication extends MultiDexApplication {

    /**
     * 用来后续监控可能发生泄漏的对象
     */
    private static RefWatcher sRefWatcher;

    /**
     * 播放服务是否被强迫回收
     */
    private boolean playServiceForceDestroy = false;
    /**
     * 应用关闭
     */
    private boolean appClose = false;
    /**
     * 应用是否是第一次启动
     */
    private boolean isFrist = true;

    /**
     * 是否开启问候音
     */
    private boolean isSayHello = false;

    /**
     * 应用是否在wifi下联网
     */
    private boolean isWifi = true;

    /**
     * 播放歌曲id
     */
    private String playIndexHashID = "";

    /**
     * 底部按钮是否打开
     */
    private boolean isBarMenuShow = false;

    /**
     * 歌曲播放模式
     */
    private int playModel = 0; // 0是 顺序播放 1是随机播放 2是循环播放 3是单曲播放

    /**
     * 播放歌曲状态
     */
    private int playStatus;

    /**
     * 当前播放列表
     */
    private List<AudioInfo> curAudioInfos;
    /**
     * 设置当前正在播放的歌曲
     */
    private AudioInfo curAudioInfo;

    /**
     * 当前歌曲
     */
    private AudioMessage curAudioMessage;

    /**
     * 排行数据
     */
    private RankListResult rankListResult;
    /**
     *
     */
    private LoggerUtil logger;

    /**
     * 是否是歌词快进
     */
    private boolean isLrcSeekTo = false;

    /**
     * 歌词字体大小
     */
    private int lrcFontSize = 30;
    /**
     * 最小字体大小
     */
    private int minLrcFontSize = 30;

    /**
     * 最大字体大小
     */
    private int maxLrcFontSize = 50;
    /**
     * 歌词颜色索引
     */
    private int lrcColorIndex = 0;

    /**
     * 桌面歌词颜色索引
     */
    private int desktopLrcColorIndex = 0;
    /**
     * 桌面歌词大小
     */
    private int desktopLrcFontSize = 0;
    /**
     * 桌面歌词的位置
     */
    private int desktopLrcY = 0;

    /**
     * 歌词颜色集合
     */
    private String[] lrcColorStr = {"#fada83", "#fe8db6", "#feb88e",
            "#adfe8e", "#8dc7ff", "#e69bff"};


    /**
     * 是否线控
     */
    private boolean isWire = true;

    /**
     * 是否显示锁屏
     */
    private boolean isShowLockScreen = false;

    /**
     * 是否显示桌面歌词
     */
    private boolean isShowDesktop = false;

    /**
     * 是否是多行歌词
     */
    private boolean isManyLineLrc = true;

    /**
     * 桌面歌词是否可以移动
     */
    private boolean desktopLyricsIsMove = true;


    private static HPApplication instance;

    public static HPApplication getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        RegisterHelper.verify();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        sRefWatcher = RefWatcher.DISABLED;
    }

    /**
     * 用来后续监控可能发生泄漏的对象
     *
     * @return
     */
    public static RefWatcher getRefWatcher() {
        return sRefWatcher;
    }


    public boolean isPlayServiceForceDestroy() {
        return playServiceForceDestroy;
    }

    public void setPlayServiceForceDestroy(boolean playServiceForceDestroy) {
        this.playServiceForceDestroy = playServiceForceDestroy;
    }

    public boolean isAppClose() {
        return appClose;
    }

    public void setAppClose(boolean appClose) {
        this.appClose = appClose;
    }

    public boolean isFrist() {
        return isFrist;
    }

    public void setFrist(boolean frist) {
        isFrist = frist;
        //
        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.isFrist_KEY, isFrist);
    }

    public boolean isSayHello() {
        return isSayHello;
    }

    public void setSayHello(boolean sayHello) {
        isSayHello = sayHello;
        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.isSayHello_KEY, isSayHello);
    }

    public boolean isWifi() {
        return (boolean) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.isWifi_KEY, isWifi);
    }

    public void setWifi(boolean wifi) {
        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.isWifi_KEY, wifi);
    }

    public String getPlayIndexHashID() {
        return (String) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.playIndexHashID_KEY, playIndexHashID);
    }

    public void setPlayIndexHashID(String playIndexHashID) {
        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.playIndexHashID_KEY, playIndexHashID);
    }

    public boolean isBarMenuShow() {
        return (boolean) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.isBarMenuShow_KEY, isBarMenuShow);
    }

    public void setBarMenuShow(boolean barMenuShow) {

        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.isBarMenuShow_KEY, barMenuShow);
    }

    public int getPlayModel() {
        return (int) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.playModel_KEY, playModel);
    }

    public void setPlayModel(int playModel) {
        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.playModel_KEY, playModel);
    }

    public int getPlayStatus() {
        return (int) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.playStatus_KEY, AudioPlayerManager.STOP);
    }

    public void setPlayStatus(int playStatus) {
        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.playStatus_KEY, playStatus);
    }

    public List<AudioInfo> getCurAudioInfos() {
        if (curAudioInfos == null) {
            if (logger == null) {
                logger = LoggerUtil.getZhangLogger(getApplicationContext());
            }
            logger.e("curAudioInfos为空，从本地获取");
            String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "curAudioInfos.ser");
            curAudioInfos = (List<AudioInfo>) SerializableObjUtil.readObj(filePath);
        }

        return curAudioInfos;
    }

    public void setCurAudioInfos(final List<AudioInfo> curAudioInfos) {
        this.curAudioInfos = curAudioInfos;
        new Thread() {
            @Override
            public void run() {
                String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "curAudioInfos.ser");
                if (curAudioInfos != null) {
                    SerializableObjUtil.saveObj(filePath, curAudioInfos);
                } else {
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }.start();
    }

    public AudioInfo getCurAudioInfo() {
        if (curAudioInfo == null) {
            if (logger == null) {
                logger = LoggerUtil.getZhangLogger(getApplicationContext());
            }
            logger.e("curAudioInfo为空，从本地获取");
            String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "curAudioInfo.ser");
            curAudioInfo = (AudioInfo) SerializableObjUtil.readObj(filePath);
        }
        return curAudioInfo;
    }

    public void setCurAudioInfo(final AudioInfo curAudioInfo) {
        this.curAudioInfo = curAudioInfo;
        new Thread() {
            @Override
            public void run() {
                String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "curAudioInfo.ser");
                if (curAudioInfo != null) {
                    SerializableObjUtil.saveObj(filePath, curAudioInfo);
                } else {
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }.start();


    }

    public AudioMessage getCurAudioMessage() {
        if (curAudioMessage == null) {
            if (logger == null) {
                logger = LoggerUtil.getZhangLogger(getApplicationContext());
            }
            logger.e("curAudioMessage为空，从本地获取");
            String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "curAudioMessage.ser");
            curAudioMessage = (AudioMessage) SerializableObjUtil.readObj(filePath);
        }
        return curAudioMessage;
    }

    public void setCurAudioMessage(final AudioMessage curAudioMessage) {
        this.curAudioMessage = curAudioMessage;
        new Thread() {
            @Override
            public void run() {
                String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "curAudioMessage.ser");
                if (curAudioMessage != null) {
                    SerializableObjUtil.saveObj(filePath, curAudioMessage);
                } else {
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }.start();
    }

    public RankListResult getRankListResult() {
        if (rankListResult == null) {
            if (logger == null) {
                logger = LoggerUtil.getZhangLogger(getApplicationContext());
            }
            logger.e("rankListResult为空，从本地获取");
            String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "rankListResult.ser");
            rankListResult = (RankListResult) SerializableObjUtil.readObj(filePath);
        }
        return rankListResult;
    }

    public void setRankListResult(final RankListResult rankListResult) {
        this.rankListResult = rankListResult;
        new Thread() {
            @Override
            public void run() {
                String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "rankListResult.ser");
                if (rankListResult != null) {
                    SerializableObjUtil.saveObj(filePath, rankListResult);
                } else {
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }.start();
    }

    public boolean isLrcSeekTo() {
        return isLrcSeekTo;
    }

    public void setLrcSeekTo(boolean lrcSeekTo) {
        isLrcSeekTo = lrcSeekTo;
    }

    public int getLrcFontSize() {
        return (int) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.lrcFontSize_KEY, lrcFontSize);
    }

    public void setLrcFontSize(int lrcFontSize) {

        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.lrcFontSize_KEY, lrcFontSize);
    }

    public int getLrcColorIndex() {
        return (int) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.lrcColorIndex_KEY, lrcColorIndex);

    }

    public void setLrcColorIndex(int lrcColorIndex) {
        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.lrcColorIndex_KEY, lrcColorIndex);
    }

    public int getDesktopLrcColorIndex() {
        return (int) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.desktopLrcColorIndex_KEY, desktopLrcColorIndex);
    }

    public void setDesktopLrcColorIndex(int desktopLrcColorIndex) {
        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.desktopLrcColorIndex_KEY, desktopLrcColorIndex);

    }

    public int getDesktopLrcFontSize() {
        return (int) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.desktopLrcFontSize_KEY, desktopLrcFontSize);
    }

    public void setDesktopLrcFontSize(int desktopLrcFontSize) {
        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.desktopLrcFontSize_KEY, desktopLrcFontSize);

    }

    public int getDesktopLrcY() {
        return (int) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.desktopLrcY_KEY, desktopLrcY);

    }

    public void setDesktopLrcY(int desktopLrcY) {
        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.desktopLrcY_KEY, desktopLrcY);

    }

    public boolean isWire() {
        return isWire;
    }

    public void setWire(boolean wire) {
        isWire = wire;
        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.isWire_KEY, isWire);
    }

    public boolean isShowDesktop() {
        return isShowDesktop;
    }

    public void setShowDesktop(boolean showDesktop) {
        isShowDesktop = showDesktop;

        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.isShowDesktop_KEY, isShowDesktop);
    }

    public boolean isShowLockScreen() {
        return isShowLockScreen;
    }

    public void setShowLockScreen(boolean showLockScreen) {
        isShowLockScreen = showLockScreen;
        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.isShowLockScreen_KEY, isShowLockScreen);
    }

    public boolean isManyLineLrc() {
        return isManyLineLrc;
    }

    public void setManyLineLrc(boolean manyLineLrc) {
        isManyLineLrc = manyLineLrc;
        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.isManyLineLrc_KEY, isManyLineLrc);
    }

    ///////////////////////


    public String[] getLrcColorStr() {
        return lrcColorStr;
    }

    public int getMinLrcFontSize() {
        return minLrcFontSize;
    }

    public int getMaxLrcFontSize() {
        return maxLrcFontSize;
    }

    public boolean isDesktopLyricsIsMove() {
        return desktopLyricsIsMove;
    }

    public void setDesktopLyricsIsMove(boolean desktopLyricsIsMove) {
        this.desktopLyricsIsMove = desktopLyricsIsMove;

        PreferencesUtil.saveValue(getApplicationContext(), PreferencesConstants.desktopLyricsIsMove_KEY, desktopLyricsIsMove);

    }
}
