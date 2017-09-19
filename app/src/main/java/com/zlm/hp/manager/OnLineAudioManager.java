package com.zlm.hp.manager;

import android.content.Context;
import android.content.Intent;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.DownloadThreadDB;
import com.zlm.hp.libs.download.DownloadTask;
import com.zlm.hp.libs.download.constant.DownloadTaskConstant;
import com.zlm.hp.libs.download.interfaces.IDownloadTaskEvent;
import com.zlm.hp.libs.download.manager.DownloadTaskManage;
import com.zlm.hp.libs.utils.LoggerUtil;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.DownloadMessage;
import com.zlm.hp.model.DownloadThreadInfo;
import com.zlm.hp.net.api.SongInfoHttpUtil;
import com.zlm.hp.net.entity.SongInfoResult;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.OnLineAudioReceiver;
import com.zlm.hp.utils.AsyncTaskUtil;
import com.zlm.hp.utils.ResourceFileUtil;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * 在线音频管理
 * Created by zhangliangming on 2017/8/10.
 */

public class OnLineAudioManager {
    /**
     *
     */
    private Context mContext;
    /**
     * 当前任务id
     */
    private String mCurTaskId = "-1";

    /**
     * 下载管理器
     */
    private static DownloadTaskManage mDownloadTaskManage;
    private static OnLineAudioManager _OnLineAudioManager;
    /**
     * 下载事件监听
     */
    private IDownloadTaskEvent mIDownloadTaskEvent;
    private HPApplication mHPApplication;

    /**
     *
     */
    private LoggerUtil logger;

    /**
     * 线程个数
     */
    public static final int threadNum = 1;

    public OnLineAudioManager(HPApplication hpApplication, Context context) {
        logger = LoggerUtil.getZhangLogger(context);
        this.mHPApplication = hpApplication;
        this.mContext = context;

        mIDownloadTaskEvent = new IDownloadTaskEvent() {
            @Override
            public int getTaskThreadDownloadedSize(DownloadTask task, int threadId) {

                if (DownloadThreadDB.getDownloadThreadDB(mContext).isExists(task.getTaskId(), threadNum, threadId)) {
                    //任务存在
                    DownloadThreadInfo downloadThreadInfo = DownloadThreadDB.getDownloadThreadDB(mContext).getDownloadThreadInfo(task.getTaskId(), threadNum, threadId);
                    if (downloadThreadInfo != null) {
                        logger.e("在线播放任务名称：" + task.getTaskName() + " 子任务线程id: " + threadId + " 已下载大小：" + downloadThreadInfo.getDownloadedSize());
                        return downloadThreadInfo.getDownloadedSize();
                    }
                }
                return 0;
            }

            @Override
            public void taskWaiting(DownloadTask task) {

            }

            @Override
            public void taskDownloading(DownloadTask task, int downloadedSize) {


                DownloadMessage downloadMessage = new DownloadMessage();
                downloadMessage.setTaskHash(task.getTaskId());
                downloadMessage.setTaskId(task.getTaskId());

                Intent downloadingIntent = new Intent(OnLineAudioReceiver.ACTION_ONLINEMUSICDOWNLOADING);
                downloadingIntent.putExtra(DownloadMessage.KEY, downloadMessage);
                downloadingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(downloadingIntent);

            }

            @Override
            public void taskPause(DownloadTask task, int downloadedSize) {
            }

            @Override
            public void taskCancel(DownloadTask task) {

            }

            @Override
            public void taskFinish(DownloadTask task, int downloadedSize) {
                if (mCurTaskId.equals(task.getTaskId())) {
                    //任务完成后，重置任务id
                    mCurTaskId = "-1";
                }
                logger.e("在线播放任务名称：" + task.getTaskName() + " 任务完成，文件大小为：" + downloadedSize);
            }

            @Override
            public void taskError(DownloadTask task, String msg) {

                logger.e("在线播放任务名称：" + task.getTaskName() + " 任务下载失败，错误信息为：" + msg);

                DownloadMessage downloadMessage = new DownloadMessage();
                downloadMessage.setTaskHash(task.getTaskId());
                downloadMessage.setTaskId(task.getTaskId());
                downloadMessage.setErrorMsg("在线播放错误");

                //发送在线播放错误广播
                Intent errorIntent = new Intent(OnLineAudioReceiver.ACTION_ONLINEMUSICERROR);
                errorIntent.putExtra(DownloadMessage.KEY, downloadMessage);
                errorIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(errorIntent);

                logger.e("在线播放任务名称：" + task.getTaskName() + " 在线缓存播放错误");

                //如果当前歌曲播放时出现错误，则直接跳转下一首
                if (mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYNET || (mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYING && mHPApplication.getPlayIndexHashID().equals(task.getTaskId()))) {
                    //
                    Intent pauseIntent = new Intent(AudioBroadcastReceiver.ACTION_PAUSEMUSIC);
                    pauseIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mContext.sendBroadcast(pauseIntent);
                }
            }

            @Override
            public void taskThreadDownloading(DownloadTask task, int threadId, final int downloadedSize) {

                if (DownloadThreadDB.getDownloadThreadDB(mContext).isExists(task.getTaskId(), threadNum, threadId)) {
                    //任务存在
                    DownloadThreadDB.getDownloadThreadDB(mContext).update(task.getTaskId(), threadNum, threadId, downloadedSize);
                } else {
                    //任务不存在
                    DownloadThreadInfo downloadThreadInfo = new DownloadThreadInfo();
                    downloadThreadInfo.setDownloadedSize(downloadedSize);
                    downloadThreadInfo.setThreadId(threadId);
                    downloadThreadInfo.setTaskId(task.getTaskId());
                    downloadThreadInfo.setThreadNum(threadNum);
                    DownloadThreadDB.getDownloadThreadDB(mContext).add(downloadThreadInfo);
                }

            }

            @Override
            public void taskThreadPause(DownloadTask task, int threadId, int downloadedSize) {

            }

            @Override
            public void taskThreadFinish(DownloadTask task, int threadId, int downloadedSize) {

                // logger.e("taskId =" + taskId + " threadId：" + threadId + "  任务进度：" + downloadedSize);
                if (DownloadThreadDB.getDownloadThreadDB(mContext).isExists(task.getTaskId(), threadNum, threadId)) {
                    //任务存在
                    DownloadThreadDB.getDownloadThreadDB(mContext).update(task.getTaskId(), threadNum, threadId, downloadedSize);
                }
                // logger.e("taskId =" + taskId + " threadId=" + threadId + " 单个任务完成，文件大小为：" + downloadedSize);
            }

            @Override
            public void taskThreadError(DownloadTask task, int threadId, String msg) {
                logger.e("在线播放任务名称：" + task.getTaskName() + " 子任务id为：" + threadId + "  任务下载失败，错误信息为：" + msg);
            }
        };

        mDownloadTaskManage = new DownloadTaskManage(mHPApplication, context, true, mIDownloadTaskEvent);
    }

    public static OnLineAudioManager getOnLineAudioManager(HPApplication hpApplication, Context context) {
        if (_OnLineAudioManager == null) {
            _OnLineAudioManager = new OnLineAudioManager(hpApplication, context);
        }
        return _OnLineAudioManager;
    }

    /**
     * 添加任务
     *
     * @param audioInfo
     */
    public synchronized void addTask(final AudioInfo audioInfo) {
        //暂停旧的任务
        if (!mCurTaskId.equals("-1")) {
            pauseTask(mCurTaskId);
        }
        // String fileName = audioInfo.getSingerName() + " - " + audioInfo.getSongName();
        //重新获取歌曲下载路径
        new AsyncTaskUtil() {
            @Override
            protected Void doInBackground(String... strings) {

                //获取歌曲下载路径

                SongInfoResult songInfoResult = SongInfoHttpUtil.songInfo(mContext, audioInfo.getHash());
                if (songInfoResult != null) {
                    audioInfo.setDownloadUrl(songInfoResult.getUrl());
                }
                DownloadTask task = new DownloadTask();
                task.setCreateTime(new Date());
                task.setStatus(DownloadTaskConstant.INT.getValue());
                task.setTaskExt(audioInfo.getFileExt());
                task.setTaskId(audioInfo.getHash());
                task.setTaskHash(audioInfo.getHash());
                task.setTaskFileSize(audioInfo.getFileSize());
                task.setTaskName(audioInfo.getSongName());
                //  task.setTaskPath(ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_AUDIO) + File.separator + fileName + "." + audioInfo.getFileExt());
                task.setTaskTempPath(ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_CACHE_AUDIO, audioInfo.getHash() + ".temp"));
                task.setTaskUrl(audioInfo.getDownloadUrl());
                task.setThreadNum(threadNum);
                //
                File temlpFile = new File(task.getTaskTempPath());
                //缓存文件不存在
                if (!temlpFile.exists()) {
                    //删除缓存任务
                    DownloadThreadDB.getDownloadThreadDB(mContext).delete(task.getTaskId(), task.getThreadNum());
                }


                logger.e("添加在线缓存任务：" + task.getTaskName() + " 任务id为：" + task.getTaskHash());


                try {
                    mDownloadTaskManage.addMultiThreadSingleTask(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                mCurTaskId = task.getTaskId();
                return super.doInBackground(strings);
            }
        }.execute("");
    }

    /**
     * 暂停任务
     *
     * @param taskId
     */
    private synchronized void pauseTask(String taskId) {
        mDownloadTaskManage.pauseTask(taskId);
    }

    /**
     * 判断任务是否存在
     *
     * @param taskId
     * @return
     */
    public boolean taskIsExists(String taskId) {
        List<DownloadTask> tasks = mDownloadTaskManage.getTasks();
        for (int i = 0; i < tasks.size(); i++) {

            if (tasks.get(i).getTaskId().equals(taskId)) {
                return true;
            }
        }
        return false;
    }
}
