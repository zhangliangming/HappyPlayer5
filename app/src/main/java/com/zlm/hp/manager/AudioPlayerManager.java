package com.zlm.hp.manager;

import android.content.Context;
import android.content.Intent;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.libs.utils.LoggerUtil;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.receiver.AudioBroadcastReceiver;

import java.util.List;
import java.util.Random;

/**
 * 音频播放处理类
 * Created by zhangliangming on 2017/8/6.
 */
public class AudioPlayerManager {
    /**
     *
     */
    private Context mContext;
    private HPApplication mHPApplication;
    /**
     * 正在播放
     */
    public static final int PLAYING = 0;
    /**
     * 暂停
     */
    public static final int PAUSE = 1;
    /**
     * 停止
     */
    public static final int STOP = 2;
    /**
     * 播放在线音乐
     */
    public static final int PLAYNET = 3;

    /**
     *
     */
    private LoggerUtil logger;

    private static AudioPlayerManager _AudioPlayerManager;

    public AudioPlayerManager(Context context, HPApplication hPApplication) {
        this.mHPApplication = hPApplication;
        //
        logger = LoggerUtil.getZhangLogger(context);
        this.mContext = context;
    }

    /**
     * @param context
     * @return
     */
    public static AudioPlayerManager getAudioPlayerManager(Context context, HPApplication hPApplication) {

        if (_AudioPlayerManager == null) {
            _AudioPlayerManager = new AudioPlayerManager(context, hPApplication);
        }
        return _AudioPlayerManager;
    }

    /***
     * 初始化播放歌曲数据
     */
    public void initSongInfoData() {
        //从本地文件中获取上次的播放歌曲列表
        List<AudioInfo> curAudioInfos = mHPApplication.getCurAudioInfos();
        if (curAudioInfos != null && curAudioInfos.size() > 0) {
            String playInfoHashID = mHPApplication.getPlayIndexHashID();
            //
            if (playInfoHashID == null || playInfoHashID.equals("")) {

                resetData();

                //发送空数据广播
                Intent nullIntent = new Intent(AudioBroadcastReceiver.ACTION_NULLMUSIC);
                nullIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(nullIntent);

                return;
            }
            boolean flag = false;
            for (int i = 0; i < curAudioInfos.size(); i++) {
                AudioInfo temp = curAudioInfos.get(i);
                if (temp.getHash().equals(playInfoHashID)) {
                    flag = true;


                    //发送init的广播
                    AudioMessage curAudioMessage = new AudioMessage();
                    curAudioMessage.setAudioInfo(temp);


                    mHPApplication.setCurAudioMessage(curAudioMessage);
                    mHPApplication.setCurAudioInfo(temp);

                    Intent initIntent = new Intent(AudioBroadcastReceiver.ACTION_INITMUSIC);
                    initIntent.putExtra(AudioMessage.KEY, curAudioMessage);
                    initIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mContext.sendBroadcast(initIntent);


                }
            }
            if (!flag) {
                resetData();
            }
        } else {
            resetData();
            //发送空数据广播
            Intent nullIntent = new Intent(AudioBroadcastReceiver.ACTION_NULLMUSIC);
            nullIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            mContext.sendBroadcast(nullIntent);
        }
    }

    /**
     *
     */
    private void resetData() {
        //清空之前的播放数据
        mHPApplication.setPlayStatus(STOP);
        mHPApplication.setPlayIndexHashID("-1");
        mHPApplication.setCurAudioInfos(null);
        mHPApplication.setCurAudioInfo(null);
        mHPApplication.setCurAudioMessage(null);
    }

    /**
     * 上一首
     *
     * @param playModel
     */
    public AudioInfo preMusic(int playModel) {
        if (mHPApplication.getCurAudioInfo() == null || mHPApplication.getCurAudioMessage() == null || mHPApplication.getCurAudioInfos() == null) {
            return null;
        }
        //获取播放索引
        int playIndex = getCurPlayIndex();

        if (playIndex == -1) {
            return null;
        }

        switch (playModel) {
            case 0:
                // 顺序播放
                playIndex--;
                if (playIndex < 0) {
                    return null;
                }

                if (mHPApplication.getCurAudioInfos().size() > 0) {
                    return mHPApplication.getCurAudioInfos().get(playIndex);
                }

                break;
            case 1:
                // 随机播放

                playIndex = new Random().nextInt(mHPApplication.getCurAudioInfos().size());
                if (mHPApplication.getCurAudioInfos().size() > 0) {
                    return mHPApplication.getCurAudioInfos().get(playIndex);
                }
                break;
            case 2:
                // 循环播放
                playIndex--;
                if (playIndex < 0) {
                    playIndex = 0;
                }
                if (playIndex >= mHPApplication.getCurAudioInfos().size()) {
                    playIndex = 0;
                }

                if (mHPApplication.getCurAudioInfos().size() != 0) {
                    return mHPApplication.getCurAudioInfos().get(playIndex);
                }

                break;
            case 3:
                // 单曲播放
                return mHPApplication.getCurAudioInfos().get(playIndex);

        }
        return null;
    }


    /**
     * 下一首
     *
     * @param playModel 播放模式
     * @return
     */
    public AudioInfo nextMusic(int playModel) {
        if (mHPApplication.getCurAudioInfo() == null || mHPApplication.getCurAudioMessage() == null || mHPApplication.getCurAudioInfos() == null) {
            return null;
        }
        //获取播放索引
        int playIndex = getCurPlayIndex();

        if (playIndex == -1) {
            return null;
        }

        switch (playModel) {
            case 0:
                // 顺序播放
                playIndex++;
                if (playIndex >= mHPApplication.getCurAudioInfos().size()) {
                    return null;
                }
                if (mHPApplication.getCurAudioInfos().size() > 0) {
                    return mHPApplication.getCurAudioInfos().get(playIndex);
                }

                break;
            case 1:
                // 随机播放

                playIndex = new Random().nextInt(mHPApplication.getCurAudioInfos().size());
                if (mHPApplication.getCurAudioInfos().size() > 0) {
                    return mHPApplication.getCurAudioInfos().get(playIndex);
                }
                break;
            case 2:
                // 循环播放

                playIndex++;
                if (playIndex >= mHPApplication.getCurAudioInfos().size()) {
                    playIndex = 0;
                }

                if (mHPApplication.getCurAudioInfos().size() > 0) {
                    return mHPApplication.getCurAudioInfos().get(playIndex);
                }

                break;
            case 3:
                // 单曲播放
                return mHPApplication.getCurAudioInfos().get(playIndex);

        }
        return null;
    }

    /**
     * 获取当前的播放索引
     *
     * @return
     */
    private int getCurPlayIndex() {

        int index = -1;
        for (int i = 0; i < mHPApplication.getCurAudioInfos().size(); i++) {
            AudioInfo audioInfo = mHPApplication.getCurAudioInfos().get(i);
            if (audioInfo.getHash().equals(mHPApplication.getPlayIndexHashID())) {
                return i;
            }
        }
        return index;
    }
}
