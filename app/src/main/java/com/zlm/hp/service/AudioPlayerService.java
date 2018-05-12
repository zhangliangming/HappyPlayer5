package com.zlm.hp.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.RemoteViews;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.DownloadThreadDB;
import com.zlm.hp.libs.utils.LoggerUtil;
import com.zlm.hp.libs.utils.ToastUtil;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.OnLineAudioManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.NotificationReceiver;
import com.zlm.hp.ui.MainActivity;
import com.zlm.hp.ui.R;
import com.zlm.hp.utils.ImageUtil;
import com.zlm.hp.utils.ResourceFileUtil;

import java.io.File;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * @Description:播放服务
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/8/6 12:08
 * @Throws:
 */
public class AudioPlayerService extends Service {
    /**
     *
     */
    private LoggerUtil logger;

    /**
     * 播放器
     */
    private IjkMediaPlayer mMediaPlayer;

    /**
     * 播放线程
     */
    private Thread mPlayerThread = null;

    private HPApplication mHPApplication;

    /**
     * 音频广播
     */
    private AudioBroadcastReceiver mAudioBroadcastReceiver;

    /**
     * 广播监听
     */
    private AudioBroadcastReceiver.AudioReceiverListener mAudioReceiverListener = new AudioBroadcastReceiver.AudioReceiverListener() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            new AsyncTask<String, Integer, String>() {
                @Override
                protected String doInBackground(String... strings) {
                    doAudioReceive(context, intent);
                    return null;
                }
            }.execute("");
        }
    };
    /**
     * 是否正在快进
     */
    private boolean isSeekTo = false;

    ///////////////////////////////通知栏//////////////////////////////
    private int mNotificationPlayBarId = 19900420;
    /**
     * 状态栏播放器视图
     */
    private RemoteViews mNotifyPlayBarRemoteViews;
    /**
     *
     */
    private Notification mPlayBarNotification;

    /**
     *
     */
    private NotificationReceiver mNotificationReceiver;
    /**
     *
     */
    private NotificationReceiver.NotificationReceiverListener mNotificationReceiverListener = new NotificationReceiver.NotificationReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Message msg = new Message();
            msg.obj = intent;
            msg.what = 1;
            mNotificationHandler.sendMessage(msg);


        }
    };

    /**
     *
     */
    private Handler mNotificationHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Intent intent = (Intent) msg.obj;
            if (msg.what == 1) {

                doNotificationReceive(getApplicationContext(), intent);
            } else {
                doNotification(getApplicationContext(), intent);
            }

        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        mHPApplication = HPApplication.getInstance();
        logger = LoggerUtil.getZhangLogger(getApplicationContext());

        //注册接收音频播放广播
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(getApplicationContext(), mHPApplication);
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(getApplicationContext());

        //注册通知栏广播
        mNotificationReceiver = new NotificationReceiver(getApplicationContext(), mHPApplication);
        mNotificationReceiver.setNotificationReceiverListener(mNotificationReceiverListener);
        mNotificationReceiver.registerReceiver(getApplicationContext());


        logger.i("音频播放服务启动");

        //初始化通知栏
        initNotificationView();
    }

    /**
     * 初始化通知栏
     */
    private void initNotificationView() {

        String tickerText = "乐乐音乐";

        //判断系统版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // 通知渠道的id
            String CHANNEL_ID = "hp_channel";
            String CHANNEL_NAME = "hp";

            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {

                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
                mChannel.enableLights(true);
                notificationManager.createNotificationChannel(mChannel);
            }

            // Create a notification and set the notification channel.
            mPlayBarNotification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(tickerText)
                    .setContentText("开心每一天")
                    .setSmallIcon(R.mipmap.notifi_icon)
                    .setChannelId(CHANNEL_ID)
                    .build();
        } else {

            //android5.0修改通知栏图标
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                mPlayBarNotification = new Notification.Builder(getApplicationContext())
                        .setContentTitle(tickerText)
                        .setContentText("开心每一天")
                        .setSmallIcon(R.mipmap.notifi_icon)
                        .build();

            } else {
                // Create a notification and set the notification channel.
                mPlayBarNotification = new Notification.Builder(getApplicationContext())
                        .setContentTitle(tickerText)
                        .setContentText("开心每一天")
                        .setSmallIcon(R.mipmap.singer_def)
                        .build();
            }
        }


        // FLAG_AUTO_CANCEL 该通知能被状态栏的清除按钮给清除掉
        // FLAG_NO_CLEAR 该通知不能被状态栏的清除按钮给清除掉
        // FLAG_ONGOING_EVENT 通知放置在正在运行
        // FLAG_INSISTENT 是否一直进行，比如音乐一直播放，知道用户响应
        mPlayBarNotification.flags |= Notification.FLAG_ONGOING_EVENT;
        // mNotification.flags |= Notification.FLAG_NO_CLEAR;

        // DEFAULT_ALL 使用所有默认值，比如声音，震动，闪屏等等
        // DEFAULT_LIGHTS 使用默认闪光提示
        // DEFAULT_SOUND 使用默认提示声音
        // DEFAULT_VIBRATE 使用默认手机震动，需加上<uses-permission
        // android:name="android.permission.VIBRATE" />权限
        // mNotification.defaults = Notification.DEFAULT_SOUND;

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayBarNotification.contentIntent = pendingIntent;
        mNotifyPlayBarRemoteViews = new RemoteViews(getPackageName(),
                R.layout.layout_notify_playbar);

        AudioInfo curAudioInfo = mHPApplication.getCurAudioInfo();
        if (curAudioInfo != null) {
            Intent initIntent = new Intent(AudioBroadcastReceiver.ACTION_INITMUSIC);
            doNotification(getApplicationContext(), initIntent);
        } else {
            Intent nullIntent = new Intent(AudioBroadcastReceiver.ACTION_NULLMUSIC);
            doNotification(getApplicationContext(), nullIntent);
        }
    }

    /**
     * 处理通知栏广播
     *
     * @param context
     * @param intent
     */
    private void doNotificationReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NotificationReceiver.NOTIFIATION_APP_PLAYMUSIC)) {

            int playStatus = mHPApplication.getPlayStatus();
            if (playStatus == AudioPlayerManager.PAUSE) {

                AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                if (audioInfo != null) {

                    AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
                    Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_RESUMEMUSIC);
                    resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                    resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(resumeIntent);

                }

            } else {
                if (mHPApplication.getCurAudioMessage() != null) {
                    AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
                    AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                    if (audioInfo != null) {
                        audioMessage.setAudioInfo(audioInfo);
                        Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_PLAYMUSIC);
                        resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                        resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        sendBroadcast(resumeIntent);
                    }
                }
            }

        } else if (intent.getAction().equals(
                NotificationReceiver.NOTIFIATION_APP_PAUSEMUSIC)) {

            int playStatus = mHPApplication.getPlayStatus();
            if (playStatus == AudioPlayerManager.PLAYING) {

                Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_PAUSEMUSIC);
                resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(resumeIntent);

            }

        } else if (intent.getAction().equals(
                NotificationReceiver.NOTIFIATION_APP_NEXTMUSIC)) {

            //
            Intent nextIntent = new Intent(AudioBroadcastReceiver.ACTION_NEXTMUSIC);
            nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(nextIntent);

        } else if (intent.getAction().equals(
                NotificationReceiver.NOTIFIATION_APP_PREMUSIC)) {

            //
            Intent nextIntent = new Intent(AudioBroadcastReceiver.ACTION_PREMUSIC);
            nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(nextIntent);

        } else if (intent.getAction().equals(
                NotificationReceiver.NOTIFIATION_DESLRC_SHOW) || intent.getAction().equals(
                NotificationReceiver.NOTIFIATION_DESLRC_HIDE)) {
            if (intent.getAction().equals(
                    NotificationReceiver.NOTIFIATION_DESLRC_HIDE)) {
                mHPApplication.setShowDesktop(false);
                mHPApplication.setDesktopLyricsIsMove(false);
            } else {
                mHPApplication.setShowDesktop(true);
                mHPApplication.setDesktopLyricsIsMove(true);
            }

            //
            Intent showOrHideIntent = new Intent(NotificationReceiver.NOTIFIATION_DESLRC_SHOWORHIDE);
            showOrHideIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(showOrHideIntent);

            doNotification(context, intent);

        } else if (intent.getAction().equals(
                NotificationReceiver.NOTIFIATION_DESLRC_UNLOCK) || intent.getAction().equals(
                NotificationReceiver.NOTIFIATION_DESLRC_LOCK)) {
            if (intent.getAction().equals(
                    NotificationReceiver.NOTIFIATION_DESLRC_UNLOCK)) {
                mHPApplication.setDesktopLyricsIsMove(true);
            } else {
                mHPApplication.setDesktopLyricsIsMove(false);
            }

            //
            Intent lockOrUnlockIntent = new Intent(AudioBroadcastReceiver.ACTION_DESLRC_LOCKORUNLOCK);
            lockOrUnlockIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(lockOrUnlockIntent);

            doNotification(context, intent);
        }
    }


    /**
     * 处理通知栏视图
     *
     * @param context
     * @param intent
     */
    private void doNotification(Context context, Intent intent) {

        Intent buttonplayIntent = new Intent(
                NotificationReceiver.NOTIFIATION_APP_PLAYMUSIC);
        PendingIntent pendplayButtonIntent = PendingIntent.getBroadcast(
                AudioPlayerService.this, 0, buttonplayIntent, 0);

        mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.play,
                pendplayButtonIntent);

        Intent buttonpauseIntent = new Intent(
                NotificationReceiver.NOTIFIATION_APP_PAUSEMUSIC);
        PendingIntent pendpauseButtonIntent = PendingIntent.getBroadcast(
                AudioPlayerService.this, 0, buttonpauseIntent, 0);

        Intent buttonnextIntent = new Intent(
                NotificationReceiver.NOTIFIATION_APP_NEXTMUSIC);
        PendingIntent pendnextButtonIntent = PendingIntent.getBroadcast(
                AudioPlayerService.this, 0, buttonnextIntent, 0);

        mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.next,
                pendnextButtonIntent);

        Intent buttonprewtIntent = new Intent(
                NotificationReceiver.NOTIFIATION_APP_PREMUSIC);
        PendingIntent pendprewButtonIntent = PendingIntent.getBroadcast(
                AudioPlayerService.this, 0, buttonprewtIntent, 0);

        mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.prew,
                pendprewButtonIntent);

        // 设置歌词显示状态和解锁歌词

        Intent buttonDesLrcUnlockIntent = new Intent(
                NotificationReceiver.NOTIFIATION_DESLRC_UNLOCK);
        PendingIntent pendDesLrcUnlockIntent = PendingIntent.getBroadcast(
                AudioPlayerService.this, 0, buttonDesLrcUnlockIntent, 0);

        mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.deslrcUnlock,
                pendDesLrcUnlockIntent);

        Intent buttonDesLrcHideIntent = new Intent(
                NotificationReceiver.NOTIFIATION_DESLRC_HIDE);
        PendingIntent pendDesLrcHideIntent = PendingIntent.getBroadcast(
                AudioPlayerService.this, 0, buttonDesLrcHideIntent, 0);

        mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.showdeslrc,
                pendDesLrcHideIntent);

        Intent buttonDesLrcShowIntent = new Intent(
                NotificationReceiver.NOTIFIATION_DESLRC_SHOW);
        PendingIntent pendDesLrcShowIntent = PendingIntent.getBroadcast(
                AudioPlayerService.this, 0, buttonDesLrcShowIntent, 0);

        mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.hidedeslrc,
                pendDesLrcShowIntent);

        if (mHPApplication.isShowDesktop()) {
            if (mHPApplication.isDesktopLyricsIsMove()) {
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.showdeslrc,
                        View.VISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.hidedeslrc,
                        View.INVISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(
                        R.id.deslrcUnlock, View.INVISIBLE);
            } else {
                mNotifyPlayBarRemoteViews.setViewVisibility(
                        R.id.deslrcUnlock, View.VISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.hidedeslrc,
                        View.INVISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.showdeslrc,
                        View.INVISIBLE);
            }
        } else {
            mNotifyPlayBarRemoteViews.setViewVisibility(R.id.hidedeslrc,
                    View.VISIBLE);
            mNotifyPlayBarRemoteViews.setViewVisibility(R.id.showdeslrc,
                    View.INVISIBLE);
            mNotifyPlayBarRemoteViews.setViewVisibility(R.id.deslrcUnlock,
                    View.INVISIBLE);
        }

        String action = intent.getAction();
        if (action.equals(AudioBroadcastReceiver.ACTION_NULLMUSIC)) {
            mNotifyPlayBarRemoteViews.setImageViewResource(R.id.singPic,
                    R.mipmap.singer_def);// 显示专辑封面图片

            mNotifyPlayBarRemoteViews.setTextViewText(R.id.songName,
                    getString(R.string.def_text));
            mNotifyPlayBarRemoteViews.setViewVisibility(R.id.play,
                    View.VISIBLE);
            mNotifyPlayBarRemoteViews.setViewVisibility(R.id.pause,
                    View.INVISIBLE);

        } else {

            AudioInfo curAudioInfo = mHPApplication.getCurAudioInfo();

            if (action.equals(AudioBroadcastReceiver.ACTION_INITMUSIC)) {

                String titleName = curAudioInfo.getSingerName() + " - " + curAudioInfo.getSongName();
                mNotifyPlayBarRemoteViews.setTextViewText(R.id.titleName,
                        titleName);

                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.play,
                        View.VISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.pause,
                        View.INVISIBLE);

                mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.play,
                        pendplayButtonIntent);


            } else if (action.equals(AudioBroadcastReceiver.ACTION_SINGERPICLOADED)) {
                Bitmap bm = ImageUtil.getNotifiIcon(mHPApplication, getApplicationContext(), curAudioInfo.getSingerName());
                if (bm != null) {
                    mNotifyPlayBarRemoteViews.setImageViewBitmap(
                            R.id.singPic, bm);// 显示专辑封面图片
                } else {
                    mNotifyPlayBarRemoteViews.setImageViewResource(
                            R.id.singPic, R.mipmap.singer_def);// 显示专辑封面图片
                }

            } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYMUSIC) || action.equals(AudioBroadcastReceiver.ACTION_SERVICE_RESUMEMUSIC) || action.equals(AudioBroadcastReceiver.ACTION_SERVICE_SEEKTOMUSIC)) {
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.play,
                        View.INVISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.pause,
                        View.VISIBLE);

                mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.pause,
                        pendpauseButtonIntent);
            } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PAUSEMUSIC)) {
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.play,
                        View.VISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.pause,
                        View.INVISIBLE);

                mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.play,
                        pendplayButtonIntent);

            }
        }

        mPlayBarNotification.contentView = mNotifyPlayBarRemoteViews;

        startForeground(mNotificationPlayBarId, mPlayBarNotification);

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDestroy() {
        mAudioBroadcastReceiver.unregisterReceiver(getApplicationContext());
        mNotificationReceiver.unregisterReceiver(getApplicationContext());

        //关闭通知栏
        stopForeground(true);

        releasePlayer();
        logger.i("音频播放服务销毁");
        super.onDestroy();
    }

    /**
     * 广播处理
     *
     * @param context
     * @param intent
     */
    private void doAudioReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(AudioBroadcastReceiver.ACTION_NULLMUSIC)) {
            releasePlayer();
            resetPlayData();

        } else if (action.equals(AudioBroadcastReceiver.ACTION_PLAYMUSIC)) {
            //播放歌曲
            playMusic((AudioMessage) intent.getSerializableExtra(AudioMessage.KEY));

        } else if (action.equals(AudioBroadcastReceiver.ACTION_PAUSEMUSIC)) {
            //暂停歌曲
            pauseMusic();
        } else if (action.equals(AudioBroadcastReceiver.ACTION_RESUMEMUSIC)) {
            //唤醒歌曲
            resumeMusic((AudioMessage) intent.getSerializableExtra(AudioMessage.KEY));
        } else if (action.equals(AudioBroadcastReceiver.ACTION_SEEKTOMUSIC)) {
            //歌曲快进
            seekToMusic((AudioMessage) intent.getSerializableExtra(AudioMessage.KEY));
        } else if (action.equals(AudioBroadcastReceiver.ACTION_NEXTMUSIC)) {
            //下一首
            nextMusic();
        } else if (action.equals(AudioBroadcastReceiver.ACTION_PREMUSIC)) {
            //上一首
            preMusic();
        }

        if (action.equals(AudioBroadcastReceiver.ACTION_NULLMUSIC)
                || action.equals(AudioBroadcastReceiver.ACTION_INITMUSIC)
                || action.equals(AudioBroadcastReceiver.ACTION_SINGERPICLOADED)
                || action.equals(AudioBroadcastReceiver.ACTION_SERVICE_SEEKTOMUSIC)
                || action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYMUSIC)
                || action.equals(AudioBroadcastReceiver.ACTION_SERVICE_RESUMEMUSIC)
                || action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PAUSEMUSIC)) {

            //处理通知栏数据
            Message msg = new Message();
            msg.obj = intent;
            msg.what = 0;
            mNotificationHandler.sendMessage(msg);
        }

    }

    /**
     * 上一首
     */
    private void preMusic() {

        logger.e("准备播放上一首");
        int playModel = mHPApplication.getPlayModel();
        AudioInfo audioInfo = AudioPlayerManager.getAudioPlayerManager(getApplicationContext(), mHPApplication).preMusic(playModel);
        if (audioInfo == null) {
            releasePlayer();
            resetPlayData();

            //发送空数据广播
            Intent nullIntent = new Intent(AudioBroadcastReceiver.ACTION_NULLMUSIC);
            nullIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(nullIntent);

            return;
        }

        logger.e("上一首歌曲为：" + audioInfo.getSongName());
        //
        AudioMessage audioMessage = new AudioMessage();
        audioMessage.setAudioInfo(audioInfo);
        playMusic(audioMessage);
    }

    /**
     * 下一首
     */
    private void nextMusic() {
        logger.e("准备播放下一首");
        int playModel = mHPApplication.getPlayModel();
        AudioInfo audioInfo = AudioPlayerManager.getAudioPlayerManager(getApplicationContext(), mHPApplication).nextMusic(playModel);
        if (audioInfo == null) {
            releasePlayer();
            resetPlayData();

            //发送空数据广播
            Intent nullIntent = new Intent(AudioBroadcastReceiver.ACTION_NULLMUSIC);
            nullIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(nullIntent);

            return;
        }
        logger.e("下一首歌曲为：" + audioInfo.getSongName());
        //
        AudioMessage audioMessage = new AudioMessage();
        audioMessage.setAudioInfo(audioInfo);
        playMusic(audioMessage);
    }


    /**
     * 快进
     *
     * @param audioMessage
     */
    private void seekToMusic(AudioMessage audioMessage) {

        if (mMediaPlayer != null) {
            isSeekTo = true;
            mMediaPlayer.seekTo(audioMessage.getPlayProgress());
        }

    }

    /**
     * 唤醒播放
     */
    private void resumeMusic(AudioMessage audioMessage) {

        //如果是网络歌曲，先进行下载，再进行播放
        if (mHPApplication.getCurAudioInfo() != null && mHPApplication.getCurAudioInfo().getType() == AudioInfo.NET) {
            //如果进度为0，表示上一次下载直接错误。
            int downloadedSize = DownloadThreadDB.getDownloadThreadDB(getApplicationContext()).getDownloadedSize(mHPApplication.getPlayIndexHashID(), OnLineAudioManager.threadNum);
            if (downloadedSize == 0) {
                //发送init的广播
                Intent initIntent = new Intent(AudioBroadcastReceiver.ACTION_INITMUSIC);
                //initIntent.putExtra(AudioMessage.KEY, audioMessage);
                initIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(initIntent);
            }
            doNetMusic();
        } else {
            if (mMediaPlayer != null) {
                isSeekTo = true;
                mMediaPlayer.seekTo(audioMessage.getPlayProgress());
            }
            mHPApplication.setPlayStatus(AudioPlayerManager.PLAYING);
        }

        Intent nextIntent = new Intent(AudioBroadcastReceiver.ACTION_SERVICE_RESUMEMUSIC);
        nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(nextIntent);
    }

    /**
     * 暂停播放
     */
    private void pauseMusic() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
        mHPApplication.setPlayStatus(AudioPlayerManager.PAUSE);
        Intent pauseIntent = new Intent(AudioBroadcastReceiver.ACTION_SERVICE_PAUSEMUSIC);
        pauseIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(pauseIntent);
    }

    /**
     * 播放歌曲
     *
     * @param audioMessage
     */
    private void playMusic(AudioMessage audioMessage) {
        releasePlayer();
        // resetPlayData();

        AudioInfo audioInfo = audioMessage.getAudioInfo();

        if (mHPApplication.getCurAudioInfo() != null) {
            if (!mHPApplication.getCurAudioInfo().getHash().equals(audioInfo.getHash())) {


                //设置当前播放数据
                mHPApplication.setCurAudioMessage(audioMessage);
                //设置当前正在播放的歌曲数据
                mHPApplication.setCurAudioInfo(audioInfo);
                //设置当前的播放索引
                mHPApplication.setPlayIndexHashID(audioInfo.getHash());

                //发送init的广播
                Intent initIntent = new Intent(AudioBroadcastReceiver.ACTION_INITMUSIC);
                //initIntent.putExtra(AudioMessage.KEY, audioMessage);
                initIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(initIntent);
            }

        } else {

            //设置当前播放数据
            mHPApplication.setCurAudioMessage(audioMessage);
            //设置当前正在播放的歌曲数据
            mHPApplication.setCurAudioInfo(audioInfo);
            //设置当前的播放索引
            mHPApplication.setPlayIndexHashID(audioInfo.getHash());

            //发送init的广播
            Intent initIntent = new Intent(AudioBroadcastReceiver.ACTION_INITMUSIC);
            //initIntent.putExtra(AudioMessage.KEY, audioMessage);
            initIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(initIntent);
        }


        if (audioInfo.getType() == AudioInfo.LOCAL) {
            //播放本地歌曲
            playLocalMusic(audioMessage);
        } else {
            String fileName = audioInfo.getSingerName() + " - " + audioInfo.getSongName();
            String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_AUDIO, fileName + "." + audioInfo.getFileExt());
            //设置文件路径
            audioInfo.setFilePath(filePath);
            File audioFile = new File(filePath);
            if (audioFile.exists()) {
                //播放本地歌曲
                playLocalMusic(audioMessage);
            } else {
                //播放网络歌曲
                doNetMusic();
            }
        }
    }

    /**
     * 下载线程
     */
    private Handler mDownloadHandler = new Handler();

    /**
     *
     */
    private Runnable mDownloadCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYNET) {

                int downloadedSize = DownloadThreadDB.getDownloadThreadDB(getApplicationContext()).getDownloadedSize(mHPApplication.getPlayIndexHashID(), OnLineAudioManager.threadNum);
                logger.e("在线播放任务名称：" + mHPApplication.getCurAudioInfo().getSongName() + "  缓存播放时，监听已下载大小：" + downloadedSize);

                mDownloadHandler.removeCallbacks(mDownloadCheckRunnable);
                if (downloadedSize > 1024 * 200) {

                    if (mHPApplication.getPlayStatus() != AudioPlayerManager.PAUSE) {
                        playNetMusic();
                    }

                } else {
                    mDownloadHandler.postDelayed(mDownloadCheckRunnable, 1000);
                }
            }
        }
    };

    /**
     * 播放网络歌曲
     */
    private void playNetMusic() {
        if (mHPApplication.getCurAudioMessage() != null && mHPApplication.getCurAudioInfo() != null) {
            String filePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_CACHE_AUDIO, mHPApplication.getCurAudioInfo().getHash() + ".temp");
            try {
                mMediaPlayer = new IjkMediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(filePath);
                mMediaPlayer.prepareAsync();
                //
                mMediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(IMediaPlayer mp) {
                        mMediaPlayer.start();

                        AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
                        //设置当前播放的状态
                        mHPApplication.setPlayStatus(AudioPlayerManager.PLAYING);
                        audioMessage.setPlayProgress(mMediaPlayer.getCurrentPosition());
                        Intent seekToIntent = new Intent(AudioBroadcastReceiver.ACTION_SERVICE_SEEKTOMUSIC);
                        seekToIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        sendBroadcast(seekToIntent);

                        if (mHPApplication.isLrcSeekTo()) {

                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mHPApplication.setLrcSeekTo(false);
                        }
                        isSeekTo = false;

                    }
                });
                mMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(IMediaPlayer mp) {

                        if (mMediaPlayer.getCurrentPosition() < (mHPApplication.getCurAudioInfo().getDuration() - 2 * 1000)) {
                            playNetMusic();
                        } else {
                            //播放完成，执行下一首操作
                            nextMusic();
                        }

                    }
                });
                mMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(IMediaPlayer mp, int what, int extra) {

                        //发送播放错误广播
                        Intent errorIntent = new Intent(AudioBroadcastReceiver.ACTION_SERVICE_PLAYERRORMUSIC);
                        errorIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        sendBroadcast(errorIntent);

                        ToastUtil.showTextToast(getApplicationContext(), "播放歌曲出错，1秒后播放下一首");


                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                    //播放下一首
                                    nextMusic();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();

                        return false;
                    }
                });
                mMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(IMediaPlayer mp) {

                        if (mHPApplication.getCurAudioMessage() != null) {
                            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();

                            if (audioMessage.getPlayProgress() > 0) {
                                isSeekTo = true;
                                mMediaPlayer.seekTo(audioMessage.getPlayProgress());
                            } else {
                                mMediaPlayer.start();


                                //设置当前播放的状态
                                mHPApplication.setPlayStatus(AudioPlayerManager.PLAYING);
                                audioMessage.setPlayProgress(mMediaPlayer.getCurrentPosition());
                                //发送play的广播
                                Intent playIntent = new Intent(AudioBroadcastReceiver.ACTION_SERVICE_PLAYMUSIC);
                                playIntent.putExtra(AudioMessage.KEY, audioMessage);
                                playIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                sendBroadcast(playIntent);

                            }


                        }
                    }
                });

                if (mPlayerThread == null) {
                    mPlayerThread = new Thread(new PlayerRunable());
                    mPlayerThread.start();
                }

            } catch (Exception e) {
                e.printStackTrace();
                logger.e(e.getMessage());

                //发送播放错误广播
                Intent errorIntent = new Intent(AudioBroadcastReceiver.ACTION_SERVICE_PLAYERRORMUSIC);
                mHPApplication.getCurAudioMessage().setErrorMsg(e.getMessage());
                errorIntent.putExtra(AudioMessage.KEY, mHPApplication.getCurAudioMessage());
                errorIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(errorIntent);

                ToastUtil.showTextToast(getApplicationContext(), "播放歌曲出错，1秒后播放下一首");
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            //播放下一首
                            nextMusic();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }


        }
    }

    /**
     * 播放网络歌曲
     */
    private void doNetMusic() {
        AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
        mDownloadHandler.removeCallbacks(mDownloadCheckRunnable);
        //设置当前的播放状态
        mHPApplication.setPlayStatus(AudioPlayerManager.PLAYNET);

        //下载
        if (!OnLineAudioManager.getOnLineAudioManager(mHPApplication, getApplicationContext()).taskIsExists(audioInfo.getHash())) {
            OnLineAudioManager.getOnLineAudioManager(mHPApplication, getApplicationContext()).addTask(audioInfo);
            mDownloadHandler.postAtTime(mDownloadCheckRunnable, 1000);
            logger.e("准备播放在线歌曲：" + audioInfo.getSongName());
        }

    }

    /**
     * 播放本地歌曲
     *
     * @param audioMessage
     */
    private void playLocalMusic(AudioMessage audioMessage) {

        try {
            mMediaPlayer = new IjkMediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(audioMessage.getAudioInfo().getFilePath());
            mMediaPlayer.prepareAsync();
            //
            mMediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(IMediaPlayer mp) {
                    mMediaPlayer.start();

                    AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
                    //设置当前播放的状态
                    mHPApplication.setPlayStatus(AudioPlayerManager.PLAYING);
                    audioMessage.setPlayProgress(mMediaPlayer.getCurrentPosition());
                    Intent seekToIntent = new Intent(AudioBroadcastReceiver.ACTION_SERVICE_SEEKTOMUSIC);
                    seekToIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(seekToIntent);

                    if (mHPApplication.isLrcSeekTo()) {

                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mHPApplication.setLrcSeekTo(false);
                    }
                    isSeekTo = false;

                }
            });
            mMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {

                    //播放完成，执行下一首操作
                    nextMusic();

                }
            });
            mMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, int what, int extra) {

                    //发送播放错误广播
                    Intent errorIntent = new Intent(AudioBroadcastReceiver.ACTION_SERVICE_PLAYERRORMUSIC);
                    errorIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(errorIntent);

                    ToastUtil.showTextToast(getApplicationContext(), "播放歌曲出错，1秒后播放下一首");


                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                //播放下一首
                                nextMusic();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                    return false;
                }
            });
            mMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {

                    if (mHPApplication.getCurAudioMessage() != null) {
                        AudioMessage audioMessage = mHPApplication.getCurAudioMessage();

                        if (audioMessage.getPlayProgress() > 0) {
                            isSeekTo = true;
                            mMediaPlayer.seekTo(audioMessage.getPlayProgress());
                        } else {
                            mMediaPlayer.start();

                            //设置当前播放的状态
                            mHPApplication.setPlayStatus(AudioPlayerManager.PLAYING);
                            audioMessage.setPlayProgress(mMediaPlayer.getCurrentPosition());
                            //发送play的广播
                            Intent playIntent = new Intent(AudioBroadcastReceiver.ACTION_SERVICE_PLAYMUSIC);
                            playIntent.putExtra(AudioMessage.KEY, audioMessage);
                            playIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            sendBroadcast(playIntent);
                        }


                    }
                }
            });

            if (mPlayerThread == null) {
                mPlayerThread = new Thread(new PlayerRunable());
                mPlayerThread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());

            //发送播放错误广播
            Intent errorIntent = new Intent(AudioBroadcastReceiver.ACTION_SERVICE_PLAYERRORMUSIC);
            audioMessage.setErrorMsg(e.getMessage());
            errorIntent.putExtra(AudioMessage.KEY, audioMessage);
            errorIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(errorIntent);

            ToastUtil.showTextToast(getApplicationContext(), "播放歌曲出错，1秒后播放下一首");
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        //播放下一首
                        nextMusic();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    /**
     * 播放线程
     */

    private class PlayerRunable implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {

                    if (!isSeekTo && mMediaPlayer != null && mMediaPlayer.isPlaying()) {

                        if (mHPApplication.getCurAudioMessage() != null) {
                            mHPApplication.getCurAudioMessage().setPlayProgress(mMediaPlayer.getCurrentPosition());


                            //发送正在播放中的广播
                            Intent playingIntent = new Intent(AudioBroadcastReceiver.ACTION_SERVICE_PLAYINGMUSIC);
                            //playingIntent.putExtra(AudioMessage.KEY, mHPApplication.getCurAudioMessage());
                            playingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            sendBroadcast(playingIntent);

                        }
                    }

                    Thread.sleep(1000);//
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 释放播放器
     */
    private void releasePlayer() {
        mHPApplication.setPlayStatus(AudioPlayerManager.STOP);
        if (mPlayerThread != null) {
            mPlayerThread = null;
        }
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            //mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        System.gc();
    }

    /**
     * 重置播放数据
     */
    private void resetPlayData() {
        mHPApplication.setCurAudioMessage(null);
        //设置当前播放的状态
        mHPApplication.setCurAudioInfo(null);
        mHPApplication.setPlayIndexHashID("");
    }
}
