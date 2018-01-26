package com.zlm.hp.application;

import android.app.Activity;
import android.app.Application;

import com.tencent.bugly.Bugly;
import com.zlm.hp.constants.Constant;
import com.zlm.hp.constants.PreferencesConstants;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.media.net.entity.RankListResult;
import com.zlm.hp.utils.ResourceFileUtil;
import com.zlm.hp.utils.SerializableObjUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import base.utils.PreferencesUtil;
import base.utils.ThreadUtil;

/**
 * Created by zhangliangming on 2017/7/15.
 */
public class HPApplication extends Application {
    private boolean playServiceForceDestroy = false;//播放服务是否被强迫回收
    private boolean appClose = false;//应用关闭
    private boolean isFrist = true;//应用是否是第一次启动
    private boolean isSayHello = false;//是否开启问候音
    private boolean isWifi = true;//应用是否在wifi下联网
    private boolean isDesktop = false;//应用是否在桌面显示歌词
    private boolean isShowLockScreen = true;//应用是否显示锁屏，isLockScreen为true时才生效
    private boolean isLockScreen = false;//应用是否在锁屏显示歌词
    private String playIndexHashID = "";// 播放歌曲id
    private boolean isBarMenuShow = false;//底部按钮是否打开
    private int playModel = 0; //歌曲播放模式   0 顺序   1随机   2循环 3单曲
    private int playStatus;//播放歌曲状态
    private List<AudioInfo> curAudioInfos;//当前播放列表
    private AudioInfo curAudioInfo;//设置当前正在播放的歌曲
    private AudioMessage curAudioMessage;//当前歌曲
    private RankListResult rankListResult;//排行数据
    private boolean isLrcSeekTo = false;//是否是歌词快进
    private int lrcFontSize = 50;//歌词字体大小
    private int minLrcFontSize = 50;//最小字体大小
    private int maxLrcFontSize = 70;//最大字体大小
    private int lrcColorIndex = 0;//歌词颜色索引
    private String[] lrcColorStr = {"#fada83", "#fe8db6", "#feb88e", "#adfe8e", "#8dc7ff", "#e69bff"};//歌词颜色集合
    private boolean isWire = true;//是否线控
    private boolean isManyLineLrc = true;//是否是多行歌词
    private static HPApplication instance;
      List<Activity> list=new ArrayList<>();


    public static HPApplication getInstance() {
        return instance;
    }

    @Override  public void onCreate() {
        super.onCreate();
        instance = this;
        registerActivityLifecycleCallbacks(new LifeCallbackActivity());
        Bugly.init(getApplicationContext(), Constant.BUGLY_APPID, false);

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
        PreferencesUtil.putValue(getApplicationContext(), PreferencesConstants.isFrist_KEY, isFrist);
    }

    public boolean isSayHello() {
        return isSayHello;
    }

    public void setSayHello(boolean sayHello) {
        isSayHello = sayHello;
        PreferencesUtil.putValue(getApplicationContext(), PreferencesConstants.isSayHello_KEY, isSayHello);
    }

    public boolean isWifi() {
        return PreferencesUtil.getBooleanValue(getApplicationContext(), PreferencesConstants.isWifi_KEY, isWifi);
    }

    public void setWifi(boolean wifi) {
        PreferencesUtil.putValue(getApplicationContext(), PreferencesConstants.isWifi_KEY, wifi);
    }

    public boolean isDesktop() {
        return PreferencesUtil.getBooleanValue(getApplicationContext(), PreferencesConstants.isDesktop_KEY, isDesktop);
    }

    public void setDesktop(boolean desktop) {
        PreferencesUtil.putValue(getApplicationContext(), PreferencesConstants.isDesktop_KEY, desktop);
    }

    public boolean isLockScreen() {
        return PreferencesUtil.getBooleanValue(getApplicationContext(), PreferencesConstants.isLockScreen_KEY, isLockScreen);
    }

    public void setLockScreen(boolean lockScreen) {
        PreferencesUtil.putValue(getApplicationContext(), PreferencesConstants.isLockScreen_KEY, lockScreen);
    }

    public boolean isShowLockScreen() {
        return PreferencesUtil.getBooleanValue(getApplicationContext(), PreferencesConstants.isShowLockScreen_KEY, isShowLockScreen);
    }

    public void setShowLockScreen(boolean lockScreen) {
        PreferencesUtil.putValue(getApplicationContext(), PreferencesConstants.isShowLockScreen_KEY, lockScreen);
    }

    public String getPlayIndexHashID() {
        return PreferencesUtil.getStringValue(getApplicationContext(), PreferencesConstants.playIndexHashID_KEY, playIndexHashID);
    }

    public void setPlayIndexHashID(String playIndexHashID) {
        PreferencesUtil.putValue(getApplicationContext(), PreferencesConstants.playIndexHashID_KEY, playIndexHashID);
    }

    public boolean isBarMenuShow() {
        return PreferencesUtil.getBooleanValue(getApplicationContext(), PreferencesConstants.isBarMenuShow_KEY, isBarMenuShow);
    }

    public void setBarMenuShow(boolean barMenuShow) {

        PreferencesUtil.putValue(getApplicationContext(), PreferencesConstants.isBarMenuShow_KEY, barMenuShow);
    }

    public int getPlayModel() {
        return PreferencesUtil.getIntValue(getApplicationContext(), PreferencesConstants.playModel_KEY, playModel);
    }

    public void setPlayModel(int playModel) {
        PreferencesUtil.putValue(getApplicationContext(), PreferencesConstants.playModel_KEY, playModel);
    }

    public int getPlayStatus() {
        return PreferencesUtil.getIntValue(getApplicationContext(), PreferencesConstants.playStatus_KEY, AudioPlayerManager.STOP);
    }

    public void setPlayStatus(int playStatus) {
        PreferencesUtil.putValue(getApplicationContext(), PreferencesConstants.playStatus_KEY, playStatus);
    }

    public List<AudioInfo> getCurAudioInfos() {
        if (curAudioInfos == null) {
            String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "curAudioInfos.ser");
            curAudioInfos = (List<AudioInfo>) SerializableObjUtil.readObj(filePath);
        }

        return curAudioInfos;
    }

    public void setCurAudioInfos(final List<AudioInfo> curAudioInfos) {
        this.curAudioInfos = curAudioInfos;
        ThreadUtil.runInThread(new Runnable() {
            @Override public void run() {

                String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "curAudioInfos.ser");
                if (curAudioInfos != null) {
                    SerializableObjUtil.saveObj(filePath, curAudioInfos);
                } else {
                    File file = new File(filePath);
                    if (file.exists()) {  file.delete();  }
                }

            }  });
    }

    public AudioInfo getCurAudioInfo() {
        if (curAudioInfo == null) {
            String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "curAudioInfo.ser");
            curAudioInfo = (AudioInfo) SerializableObjUtil.readObj(filePath);
        }
        return curAudioInfo;
    }

    public void setCurAudioInfo(final AudioInfo curAudioInfo) {
        this.curAudioInfo = curAudioInfo;
        ThreadUtil.runInThread(new Runnable() {
            @Override public void run() {

                String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "curAudioInfo.ser");
                if (curAudioInfo != null) {
                    SerializableObjUtil.saveObj(filePath, curAudioInfo);
                } else {
                    File file = new File(filePath);
                    if (file.exists()) { file.delete(); }
                }
            }  });

    }

    public AudioMessage getCurAudioMessage() {
        if (curAudioMessage == null) {
            String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "curAudioMessage.ser");
            curAudioMessage = (AudioMessage) SerializableObjUtil.readObj(filePath);
        }
        return curAudioMessage;
    }

    public void setCurAudioMessage(final AudioMessage curAudioMessage) {
        this.curAudioMessage = curAudioMessage;
        ThreadUtil.runInThread(new Runnable() {
            @Override public void run() {

                String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "curAudioMessage.ser");
                if (curAudioMessage != null) {
                    SerializableObjUtil.saveObj(filePath, curAudioMessage);
                } else {
                    File file = new File(filePath);
                    if (file.exists()) {  file.delete(); }
                }
            } });

    }

    public RankListResult getRankListResult() {
        if (rankListResult == null) {
            String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "rankListResult.ser");
            rankListResult = (RankListResult) SerializableObjUtil.readObj(filePath);
        }
        return rankListResult;
    }

    public void setRankListResult(final RankListResult rankListResult) {
        this.rankListResult = rankListResult;
        ThreadUtil.runInThread(new Runnable() {
            @Override public void run() {

                String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_SERIALIZABLE, "rankListResult.ser");
                if (rankListResult != null) {
                    SerializableObjUtil.saveObj(filePath, rankListResult);
                } else {
                    File file = new File(filePath);
                    if (file.exists()) { file.delete();  }
                }
            }  });
    }

    public boolean isLrcSeekTo() {
        return isLrcSeekTo;
    }

    public void setLrcSeekTo(boolean lrcSeekTo) {
        isLrcSeekTo = lrcSeekTo;
    }

    public int getLrcFontSize() {
        return PreferencesUtil.getIntValue(getApplicationContext(), PreferencesConstants.lrcFontSize_KEY, lrcFontSize);
    }

    public void setLrcFontSize(int lrcFontSize) {

        PreferencesUtil.putValue(getApplicationContext(), PreferencesConstants.lrcFontSize_KEY, lrcFontSize);
    }

    public int getLrcColorIndex() {
        return PreferencesUtil.getIntValue(getApplicationContext(), PreferencesConstants.lrcColorIndex_KEY, lrcColorIndex);

    }

    public void setLrcColorIndex(int lrcColorIndex) {
        PreferencesUtil.putValue(getApplicationContext(), PreferencesConstants.lrcColorIndex_KEY, lrcColorIndex);
    }

    public boolean isWire() {
        return isWire;
    }

    public void setWire(boolean wire) {
        isWire = wire;
        PreferencesUtil.putValue(getApplicationContext(), PreferencesConstants.isWire_KEY, isWire);
    }

    public boolean isManyLineLrc() {
        return isManyLineLrc;
    }

    public void setManyLineLrc(boolean manyLineLrc) {
        isManyLineLrc = manyLineLrc;
        PreferencesUtil.putValue(getApplicationContext(), PreferencesConstants.isManyLineLrc_KEY, isManyLineLrc);
    }

    public String[] getLrcColorStr() {
        return lrcColorStr;
    }

    public int getMinLrcFontSize() {
        return minLrcFontSize;
    }

    public int getMaxLrcFontSize() {
        return maxLrcFontSize;
    }

    public void exit(){
        setAppClose(true);
        setPlayStatus(AudioPlayerManager.STOP);
        for (Activity activity : list) {
                  activity.finish();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

}
