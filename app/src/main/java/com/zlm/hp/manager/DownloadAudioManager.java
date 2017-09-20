package com.zlm.hp.manager;

import android.content.Context;
import android.content.Intent;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.db.DownloadInfoDB;
import com.zlm.hp.db.DownloadThreadDB;
import com.zlm.hp.libs.download.DownloadTask;
import com.zlm.hp.libs.download.constant.DownloadTaskConstant;
import com.zlm.hp.libs.download.interfaces.IDownloadTaskEvent;
import com.zlm.hp.libs.download.manager.DownloadTaskManage;
import com.zlm.hp.libs.utils.DateUtil;
import com.zlm.hp.libs.utils.LoggerUtil;
import com.zlm.hp.libs.utils.ToastUtil;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.DownloadInfo;
import com.zlm.hp.model.DownloadMessage;
import com.zlm.hp.model.DownloadThreadInfo;
import com.zlm.hp.net.api.SongInfoHttpUtil;
import com.zlm.hp.net.entity.SongInfoResult;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.DownloadAudioReceiver;
import com.zlm.hp.utils.AsyncTaskUtil;
import com.zlm.hp.utils.ResourceFileUtil;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * 下载管理
 * Created by zhangliangming on 2017/9/9.
 */

public class DownloadAudioManager {
    /**
     *
     */
    private Context mContext;

    /**
     * 下载管理器
     */
    private static DownloadTaskManage mDownloadTaskManage;

    private static DownloadAudioManager _DownloadAudioManager;

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
    public static final int threadNum = 5;

    public DownloadAudioManager(HPApplication hpApplication, Context context) {
        logger = LoggerUtil.getZhangLogger(context);
        this.mHPApplication = hpApplication;
        this.mContext = context;

        //
        mIDownloadTaskEvent = new IDownloadTaskEvent() {
            @Override
            public void taskWaiting(DownloadTask task) {

                DownloadMessage downloadMessage = new DownloadMessage();
                downloadMessage.setTaskHash(task.getTaskId());
                downloadMessage.setTaskId(task.getTaskId());

                Intent waitingIntent = new Intent(DownloadAudioReceiver.ACTION_DOWMLOADMUSICWAIT);
                waitingIntent.putExtra(DownloadMessage.KEY, downloadMessage);
                waitingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(waitingIntent);
            }

            @Override
            public void taskDownloading(DownloadTask task, int downloadedSize) {

                if (task.getTaskFileSize() <= downloadedSize) {
                    return;
                }

                //更新
                DownloadInfoDB.getAudioInfoDB(mContext).update(task.getTaskHash(), downloadedSize, AudioInfo.DOWNLOADING);

                DownloadMessage downloadMessage = new DownloadMessage();
                downloadMessage.setTaskHash(task.getTaskId());
                downloadMessage.setTaskId(task.getTaskId());

                Intent downloadingIntent = new Intent(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNLOADING);
                downloadingIntent.putExtra(DownloadMessage.KEY, downloadMessage);
                downloadingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(downloadingIntent);


                logger.e("下载任务名称：" + task.getTaskName() + " 任务下载中，进度为：" + downloadedSize);
            }

            @Override
            public void taskPause(DownloadTask task, int downloadedSize) {

                if (task.getTaskFileSize() <= downloadedSize) {
                    return;
                }

                //更新
                DownloadInfoDB.getAudioInfoDB(mContext).update(task.getTaskHash(), downloadedSize, AudioInfo.DOWNLOADING);

                DownloadMessage downloadMessage = new DownloadMessage();
                downloadMessage.setTaskHash(task.getTaskId());
                downloadMessage.setTaskId(task.getTaskId());

                Intent pauseIntent = new Intent(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNPAUSE);
                pauseIntent.putExtra(DownloadMessage.KEY, downloadMessage);
                pauseIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(pauseIntent);

                logger.e("下载任务名称：" + task.getTaskName() + " 任务暂停");
            }

            @Override
            public void taskCancel(DownloadTask task) {

                //删除任务
                DownloadInfoDB.getAudioInfoDB(mContext).delete(task.getTaskHash());

                DownloadMessage downloadMessage = new DownloadMessage();
                downloadMessage.setTaskHash(task.getTaskId());
                downloadMessage.setTaskId(task.getTaskId());

                //发送取消广播
                Intent cancelIntent = new Intent(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNCANCEL);
                cancelIntent.putExtra(DownloadMessage.KEY, downloadMessage);
                cancelIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(cancelIntent);

                //发送更新下载歌曲总数广播
                Intent updateIntent = new Intent(AudioBroadcastReceiver.ACTION_DOWNLOADUPDATE);
                updateIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(updateIntent);


                logger.e("下载任务名称：" + task.getTaskName() + " 任务取消");
            }

            @Override
            public void taskFinish(DownloadTask task, int downloadedSize) {


                //更新
                DownloadInfoDB.getAudioInfoDB(mContext).update(task.getTaskHash(), downloadedSize, AudioInfo.FINISH);

                DownloadMessage downloadMessage = new DownloadMessage();
                downloadMessage.setTaskHash(task.getTaskId());
                downloadMessage.setTaskId(task.getTaskId());

                //发送下载完成广播
                Intent finishIntent = new Intent(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNFINISH);
                finishIntent.putExtra(DownloadMessage.KEY, downloadMessage);
                finishIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(finishIntent);


                //发送更新本地歌曲广播
                Intent updateIntent = new Intent(AudioBroadcastReceiver.ACTION_LOCALUPDATE);
                updateIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(updateIntent);

                logger.e("下载任务名称：" + task.getTaskName() + " 任务完成，文件大小为：" + downloadedSize);
            }

            @Override
            public void taskError(DownloadTask task, String msg) {

                ToastUtil.showTextToast(mContext, "歌曲：" + task.getTaskName() + "，下载出错");

                logger.e("下载任务名称：" + task.getTaskName() + " 任务下载失败，错误信息为：" + msg);

                DownloadMessage downloadMessage = new DownloadMessage();
                downloadMessage.setTaskHash(task.getTaskId());
                downloadMessage.setTaskId(task.getTaskId());
                downloadMessage.setErrorMsg("下载错误");

                //发送在线播放错误广播
                Intent errorIntent = new Intent(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNERROR);
                errorIntent.putExtra(DownloadMessage.KEY, downloadMessage);
                errorIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(errorIntent);
            }

            @Override
            public int getTaskThreadDownloadedSize(DownloadTask task, int threadId) {
                if (DownloadThreadDB.getDownloadThreadDB(mContext).isExists(task.getTaskId(), threadNum, threadId)) {
                    //任务存在
                    DownloadThreadInfo downloadThreadInfo = DownloadThreadDB.getDownloadThreadDB(mContext).getDownloadThreadInfo(task.getTaskId(), threadNum, threadId);
                    if (downloadThreadInfo != null) {
                        logger.e("在线下载任务名称：" + task.getTaskName() + " 子任务线程名称: " + threadId + " 已下载大小：" + downloadThreadInfo.getDownloadedSize());
                        return downloadThreadInfo.getDownloadedSize();
                    }
                }
                return 0;
            }

            @Override
            public void taskThreadDownloading(DownloadTask task, int threadId, int downloadedSize) {
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
                if (DownloadThreadDB.getDownloadThreadDB(mContext).isExists(task.getTaskId(), threadNum, threadId)) {
                    //任务存在
                    DownloadThreadDB.getDownloadThreadDB(mContext).update(task.getTaskId(), threadNum, threadId, downloadedSize);
                }
            }

            @Override
            public void taskThreadError(DownloadTask task, int threadId, String msg) {

                logger.e("下载任务名称：" + task.getTaskName() + " 子任务id为：" + threadId + "  任务下载失败，错误信息为：" + msg);


            }
        };
        mDownloadTaskManage = new DownloadTaskManage(mHPApplication, context, false, mIDownloadTaskEvent);
    }

    public static DownloadAudioManager getDownloadAudioManager(HPApplication hpApplication, Context context) {
        if (_DownloadAudioManager == null) {
            _DownloadAudioManager = new DownloadAudioManager(hpApplication, context);
            _DownloadAudioManager = new DownloadAudioManager(hpApplication, context);
        }
        return _DownloadAudioManager;
    }


    /**
     * 添加任务
     *
     * @param audioInfo
     */
    public synchronized void addTask(final AudioInfo audioInfo) {

        final DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setDHash(audioInfo.getHash());
        downloadInfo.setAudioInfo(audioInfo);
        downloadInfo.setDownloadedSize(0);
        //添加歌曲路径
        String fileName = audioInfo.getSingerName() + " - " + audioInfo.getSongName();
        final String filePath = ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_AUDIO, (fileName + "." + audioInfo.getFileExt()));
        audioInfo.setFilePath(filePath);
        audioInfo.setCreateTime(DateUtil.parseDateToString(new Date()));
        downloadInfo.setAudioInfo(audioInfo);
        if (audioInfo.getType() == AudioInfo.LOCAL || AudioInfoDB.getAudioInfoDB(mContext).isNetAudioExists(audioInfo.getHash())) {

            ToastUtil.showTextToast(mContext, "本地歌曲，不用下载!");

            return;

        } else if (DownloadInfoDB.getAudioInfoDB(mContext).isExists(audioInfo.getHash())) {
            //下载任务已经存在
            if (taskIsExists(audioInfo.getHash())) {
                ToastUtil.showTextToast(mContext, "歌曲已添加!");

                return;
            } else {
                int downloadedSize = DownloadThreadDB.getDownloadThreadDB(mContext).getDownloadedSize(downloadInfo.getDHash(), DownloadAudioManager.threadNum);
                if (downloadedSize >= audioInfo.getFileSize()) {
                    ToastUtil.showTextToast(mContext, "歌曲已下载!");

                    return;
                }
            }

        } else {
            DownloadInfoDB.getAudioInfoDB(mContext).add(downloadInfo);


            ToastUtil.showTextToast(mContext, "已添加到下载");


            //添加下载任务
            Intent updateIntent = new Intent(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNADD);
            updateIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            mContext.sendBroadcast(updateIntent);
        }
        //重新获取歌曲下载路径
        new AsyncTaskUtil() {
            @Override
            protected Void doInBackground(String... strings) {


                //获取歌曲最新的下载路径

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
                task.setTaskPath(filePath);
                task.setTaskTempPath(ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_AUDIO_TEMP, audioInfo.getHash() + ".temp"));
                task.setTaskUrl(audioInfo.getDownloadUrl());
                task.setThreadNum(threadNum);
                //

                logger.e("添加下载任务：" + task.getTaskName() + " 任务名称为：" + task.getTaskName());

                try {
                    mDownloadTaskManage.addMultiThreadSingleTaskOrderByTime(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //更新下载歌曲个数
                Intent updateIntent = new Intent(AudioBroadcastReceiver.ACTION_DOWNLOADUPDATE);
                updateIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(updateIntent);

                return super.doInBackground(strings);
            }
        }.execute("");


    }

    /**
     * 获取任务的下载状态
     *
     * @param taskId
     * @return
     */
    public int taskIsDLStatus(String taskId) {
        List<DownloadTask> tasks = mDownloadTaskManage.getTasks();
        for (int i = 0; i < tasks.size(); i++) {

            if (tasks.get(i).getTaskId().equals(taskId)) {
                return tasks.get(i).getStatus();
            }
        }
        return DownloadTaskConstant.INT.getValue();
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

    /**
     * 暂停任务
     *
     * @param taskId
     */
    public synchronized void pauseTask(String taskId) {
        mDownloadTaskManage.pauseTask(taskId);
    }


    /**
     * 取消任务
     *
     * @param taskId
     */
    public synchronized void cancelTask(String taskId) {
        mDownloadTaskManage.cancelTask(taskId);
    }
}
