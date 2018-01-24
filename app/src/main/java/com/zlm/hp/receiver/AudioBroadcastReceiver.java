package com.zlm.hp.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.model.AudioMessage;

import java.util.Date;

import base.utils.LoggerUtil;

/**
 * 音频广播
 * Created by zhangliangming on 2017/8/6.
 */
public class AudioBroadcastReceiver {

    private static final String base_action = "com.zlm.hp";

    private LoggerUtil logger;
    /**
     * 是否注册成功
     */
    private boolean isRegisterSuccess = false;
    private Context mContext;
    /**
     * 注册成功广播
     */
    private String ACTION_MUSICSUCCESS = base_action + ".music.success_" + new Date().getTime();

    //重新启动广播
    //public static final String ACTION_MUSICRESTART = base_action + ".music.start";
    //空音乐
    public static final String ACTION_NULLMUSIC = base_action + ".null.music";
    //添加音乐
    public static final String ACTION_ADDMUSIC = base_action + ".add.music";
    //初始化音乐
    public static final String ACTION_INITMUSIC = base_action + ".init.music";
    //点击播放音乐
    public static final String ACTION_PLAYMUSIC = base_action + ".play.music";
    //继续播放
    public static final String ACTION_RESUMEMUSIC = base_action + ".resume.music";
    //点击暂停播放
    public static final String ACTION_PAUSEMUSIC = base_action + ".pause.music";
    //点击音乐快进
    public static final String ACTION_SEEKTOMUSIC = base_action + ".seekto.music";
    //点击上一首
    public static final String ACTION_PREMUSIC = base_action + ".pre.music";
    //点击下一首
    public static final String ACTION_NEXTMUSIC = base_action + ".next.music";
    //点击关闭通知栏
    public static final String ACTION_CANCELNOTIFICATION = base_action + ".cancel.notification";

    //播放器开始播放
    public static final String ACTION_SERVICE_PLAYMUSIC = base_action + ".service.play.music";
    //播放器暂停
    public static final String ACTION_SERVICE_PAUSEMUSIC = base_action + ".service.pause.music";
    //播放器唤醒
    public static final String ACTION_SERVICE_RESUMEMUSIC = base_action + ".service.resume.music";
    //播放器播放中
    public static final String ACTION_SERVICE_PLAYINGMUSIC = base_action + ".service.playing.music";
    //播放错误
    public static final String ACTION_SERVICE_PLAYERRORMUSIC = base_action + ".service.playerror.music";
    //更新定时停止播放时间
    public static final String ACTION_SERVICE_UPDATESTOPPLAYTIME = base_action + ".service.update.stopplaytime";

    //歌词搜索中广播
    public static final String ACTION_LRCSEARCHING = base_action + ".lrc.searching";
    //歌词下载中
    public static final String ACTION_LRCDOWNLOADING = base_action + ".lrc.downloading";
    //歌词加载完成广播
    public static final String ACTION_LRCLOADED = base_action + ".lrc.loaded";

    //
    public static final String ACTION_SINGERPICLOADED = base_action + ".singerpic.loaded";

    /**
     * 暂停播放时，如果进度条快进时，歌曲也快进
     */
    public static final String ACTION_LRCSEEKTO = base_action + ".lrc.seekto";
    //使用歌词
    public static final String ACTION_LRCUSE = base_action + ".lrc.use";
    //
    //本地歌曲更新
    public static final String ACTION_LOCALUPDATE = base_action + ".local.update.music";
    //最近歌曲更新
    public static final String ACTION_RECENTUPDATE = base_action + ".recent.update.music";
    //下载歌曲更新
    public static final String ACTION_DOWNLOADUPDATE = base_action + ".download.update.music";
    //喜欢歌曲更新
    public static final String ACTION_LIKEUPDATE = base_action + ".like.update.music";
    //添加喜欢歌曲
    public static final String ACTION_LIKEADD = base_action + ".like.add.music";
    public static final String ACTION_LIKEDELETE = base_action + ".like.delete.music";

    /**
     * 重新加载歌手写真
     */
    public static String ACTION_RELOADSINGERIMG = base_action + ".reload.singerimg";
    public static String ACTION_SINGERIMGLOADED = base_action + ".singerimg.loaded";

    private BroadcastReceiver mAudioBroadcastReceiver;
    private IntentFilter mAudioIntentFilter;
    private AudioReceiverListener mAudioReceiverListener;

    public AudioBroadcastReceiver(Context context) {
        this.mContext = context;
        logger = LoggerUtil.getZhangLogger(context);
        mAudioIntentFilter = new IntentFilter();
        //
        mAudioIntentFilter.addAction(ACTION_MUSICSUCCESS);
        mAudioIntentFilter.addAction(ACTION_NULLMUSIC);
        // mAudioIntentFilter.addAction(ACTION_MUSICRESTART);
        mAudioIntentFilter.addAction(ACTION_ADDMUSIC);
        mAudioIntentFilter.addAction(ACTION_INITMUSIC);
        mAudioIntentFilter.addAction(ACTION_PLAYMUSIC);
        mAudioIntentFilter.addAction(ACTION_RESUMEMUSIC);
        mAudioIntentFilter.addAction(ACTION_PAUSEMUSIC);
        mAudioIntentFilter.addAction(ACTION_SEEKTOMUSIC);
        mAudioIntentFilter.addAction(ACTION_PREMUSIC);
        mAudioIntentFilter.addAction(ACTION_NEXTMUSIC);
        mAudioIntentFilter.addAction(ACTION_CANCELNOTIFICATION);
        mAudioIntentFilter.addAction(ACTION_SERVICE_UPDATESTOPPLAYTIME);

        mAudioIntentFilter.addAction(ACTION_SERVICE_PLAYMUSIC);
        mAudioIntentFilter.addAction(ACTION_SERVICE_PAUSEMUSIC);
        mAudioIntentFilter.addAction(ACTION_SERVICE_RESUMEMUSIC);
        mAudioIntentFilter.addAction(ACTION_SERVICE_PLAYINGMUSIC);
        mAudioIntentFilter.addAction(ACTION_SERVICE_PLAYERRORMUSIC);

        //
        mAudioIntentFilter.addAction(ACTION_LRCSEARCHING);
        mAudioIntentFilter.addAction(ACTION_LRCDOWNLOADING);
        mAudioIntentFilter.addAction(ACTION_LRCLOADED);
        mAudioIntentFilter.addAction(ACTION_LRCSEEKTO);
        mAudioIntentFilter.addAction(ACTION_LRCUSE);
        //
        mAudioIntentFilter.addAction(ACTION_SINGERPICLOADED);

        //
        mAudioIntentFilter.addAction(ACTION_LOCALUPDATE);
        mAudioIntentFilter.addAction(ACTION_RECENTUPDATE);
        mAudioIntentFilter.addAction(ACTION_DOWNLOADUPDATE);
        mAudioIntentFilter.addAction(ACTION_LIKEUPDATE);
        mAudioIntentFilter.addAction(ACTION_LIKEADD);
        mAudioIntentFilter.addAction(ACTION_LIKEDELETE);
        //
        mAudioIntentFilter.addAction(ACTION_RELOADSINGERIMG);
        mAudioIntentFilter.addAction(ACTION_SINGERIMGLOADED);
    }

    /**
     *
     */
    @SuppressLint("HandlerLeak")
    private Handler mAudioHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (mAudioReceiverListener != null) {
                Intent intent = (Intent) msg.obj;
                if (intent.getAction().equals(ACTION_MUSICSUCCESS)) {
                    isRegisterSuccess = true;

                    //
                    //服务被强迫回收
                    if (HPApplication.getInstance().isPlayServiceForceDestroy()) {
                        HPApplication.getInstance().setPlayServiceForceDestroy(false);
                        int playStatus = HPApplication.getInstance().getPlayStatus();
                        if (playStatus == AudioPlayerManager.PLAYING) {

                            //
                            logger.e("发送重启后重新播放音频广播");
                            AudioMessage audioMessage = HPApplication.getInstance().getCurAudioMessage();
                            if (audioMessage != null) {
                                Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_PLAYMUSIC);
                                resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                                resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                mContext.sendBroadcast(resumeIntent);
                            }
                        } else {
                            //服务回收了，修改当前的播放状态
                            HPApplication.getInstance().setPlayStatus(AudioPlayerManager.STOP);
                        }
                    }

                } else {
                    mAudioReceiverListener.onReceive(mContext, intent);
                }

            }
        }
    };

    /**
     * 注册广播
     *
     * @param context
     */
    public void registerReceiver(Context context) {
        if (mAudioBroadcastReceiver == null) {
            //
            mAudioBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Message msg = new Message();
                    msg.obj = intent;
                    mAudioHandler.sendMessage(msg);


                }
            };

            mContext.registerReceiver(mAudioBroadcastReceiver, mAudioIntentFilter);
            //发送注册成功广播
            Intent successIntent = new Intent(ACTION_MUSICSUCCESS);
            successIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            mContext.sendBroadcast(successIntent);

        }
    }

    /**
     * 取消注册广播
     */
    public void unregisterReceiver(Context context) {
        if (mAudioBroadcastReceiver != null && isRegisterSuccess) {

            mContext.unregisterReceiver(mAudioBroadcastReceiver);

        }

    }

    public void setAudioReceiverListener(AudioReceiverListener audioReceiverListener) {
        this.mAudioReceiverListener = audioReceiverListener;
    }

    ///////////////////////////////////
    public interface AudioReceiverListener {
        void onReceive(Context context, Intent intent);
    }
}
