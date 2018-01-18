package com.zlm.hp.application;

import android.app.Application;

import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.net.entity.RankListResult;
import com.zlm.hp.utils.ResourceFileUtil;
import com.zlm.hp.utils.SerializableObjUtil;

import java.io.File;
import java.util.List;

import base.utils.LoggerUtil;

/**
 * Created by zhangliangming on 2017/7/15.
 */
public class HPApplication extends Application {
    /**
     * 播放服务是否被强迫回收
     */
    private boolean playServiceForceDestroy = false;
    /**
     * 应用关闭
     */
    private boolean appClose = false;

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
    ;
    /**
     *
     */
    private LoggerUtil logger;

    /**
     * 是否是歌词快进
     */
    private boolean isLrcSeekTo = false;

    private static HPApplication instance;

    public static HPApplication getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //
        //注册捕捉全局异常
//        CrashHandler crashHandler = new CrashHandler();
//        crashHandler.init(getApplicationContext());

        logger = LoggerUtil.getZhangLogger(getApplicationContext());
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

    public List<AudioInfo> getCurAudioInfos() {
        if (curAudioInfos == null) {
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
}
