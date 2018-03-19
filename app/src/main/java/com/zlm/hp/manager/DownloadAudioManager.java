package com.zlm.hp.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.yanzhenjie.nohttp.Headers;
import com.zlm.hp.R;
import com.zlm.hp.application.HPApplication;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.db.DownloadInfoDB;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.DownloadInfo;
import com.zlm.hp.model.DownloadMessage;
import com.zlm.hp.net.api.SongInfoHttpUtil;
import com.zlm.hp.net.entity.SongInfoResult;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.DownloadAudioReceiver;
import com.zlm.hp.utils.ResourceFileUtil;

import java.io.File;
import java.util.Date;
import java.util.Map;

import base.download.DownloadManager;
import base.download.DownloadTask;
import base.download.constant.DownloadTaskConstant;
import base.download.interfaces.IDownloadListener;
import base.utils.DateUtil;
import base.utils.LoggerUtil;
import base.utils.ThreadUtil;
import base.utils.ToastUtil;

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
    private static DownloadManager mDownloadManager;

    private static DownloadAudioManager _DownloadAudioManager;

    /**
     *
     */
    private LoggerUtil logger;

    /**
     * 线程个数
     */
    public static final int threadNum = 5;

    public DownloadAudioManager(Context context) {
        logger = LoggerUtil.getZhangLogger(context);
        this.mContext = context;

        IDownloadListener iDownloadListener = new IDownloadListener() {
            @Override
            public void onBefore(int what) {
                DownloadTask task = mDownloadManager.getDownloadTasks().get(what);
                DownloadMessage downloadMessage = new DownloadMessage();
                downloadMessage.setTaskHash(task.getTaskId());
                downloadMessage.setTaskId(task.getTaskId());
                Intent waitingIntent = new Intent(DownloadAudioReceiver.ACTION_DOWMLOADMUSICWAIT);
                waitingIntent.putExtra(DownloadMessage.KEY, downloadMessage);
                waitingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(waitingIntent);
            }

            @Override
            public void onDownloadError(int what, Exception exception, String message) {
                DownloadTask task = mDownloadManager.getDownloadTasks().get(what);
                ToastUtil.showTextToast(mContext, "歌曲：" + task.getTaskName() + "，下载出错");

                logger.e("下载任务名称：" + task.getTaskName() + " 任务下载失败，错误信息为：" + message);

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
            }

            @Override
            public void onStart(int what, boolean isResume, long rangeSize, Headers responseHeaders, long allCount) {
                DownloadTask task = mDownloadManager.getDownloadTasks().get(what);
                //更新
                DownloadInfoDB.getAudioInfoDB(mContext).update(task.getTaskHash(), rangeSize, AudioInfo.DOWNLOADING);

                DownloadMessage downloadMessage = new DownloadMessage();
                downloadMessage.setTaskHash(task.getTaskId());
                downloadMessage.setTaskId(task.getTaskId());
                downloadMessage.setTaskFileSize(allCount);
                downloadMessage.setTaskCurFileSize(rangeSize);

                Intent downloadingIntent = new Intent(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNLOADING);
                downloadingIntent.putExtra(DownloadMessage.KEY, downloadMessage);
                downloadingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(downloadingIntent);
            }

            @Override
            public void onProgress(int what, int progress, long fileCount, long speed) {
                DownloadTask task = mDownloadManager.getDownloadTasks().get(what);
                //更新
                long downloadCount = progress * fileCount / 100;
                DownloadInfoDB.getAudioInfoDB(mContext).update(task.getTaskHash(), downloadCount, AudioInfo.DOWNLOADING);

                DownloadMessage downloadMessage = new DownloadMessage();
                downloadMessage.setTaskHash(task.getTaskId());
                downloadMessage.setTaskId(task.getTaskId());
                downloadMessage.setTaskFileSize(fileCount);
                downloadMessage.setTaskCurFileSize(downloadCount);

                Intent downloadingIntent = new Intent(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNLOADING);
                downloadingIntent.putExtra(DownloadMessage.KEY, downloadMessage);
                downloadingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(downloadingIntent);
            }

            @Override
            public void onFinish(int what, String filePath) {
                DownloadTask task = mDownloadManager.getDownloadTasks().get(what);
                //更新
                DownloadInfoDB.getAudioInfoDB(mContext).update(task.getTaskHash(), task.getTaskFileSize(), AudioInfo.FINISH);

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

                //扫描系统媒体数据库库
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//如果是4.4及以上版本
                    Intent mediaScanIntent = new Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(new File(task.getTaskPath())); //out is your output file
                    mediaScanIntent.setData(contentUri);
                    mContext.sendBroadcast(mediaScanIntent);
                } else {
                    mContext.sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_MOUNTED,
                            Uri.parse("file://"
                                    + Environment.getExternalStorageDirectory())));
                }
            }

            @Override
            public void onCancel(int what) {
                DownloadTask task = mDownloadManager.getDownloadTasks().get(what);
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
            }
        };
        mDownloadManager = new DownloadManager(context, iDownloadListener);
    }

    public static DownloadAudioManager getDownloadAudioManager(Context context) {
        if (_DownloadAudioManager == null) {
            synchronized (DownloadAudioManager.class) {
                if (_DownloadAudioManager == null) {
                    _DownloadAudioManager = new DownloadAudioManager(context);
                }
            }
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
        if (audioInfo.getType() == AudioInfo.LOCAL
                || audioInfo.getType() == AudioInfo.DOWNLOAD
                || AudioInfoDB.getAudioInfoDB(mContext).isNetAudioExists(audioInfo.getHash())) {

            ToastUtil.showTextToast(mContext, mContext.getString(R.string.local_song_no_download));

            return;

        } else if (DownloadInfoDB.getAudioInfoDB(mContext).isExists(audioInfo.getHash())) {
            //下载任务已经存在
            if (taskIsExists(audioInfo.getHash())) {
                ToastUtil.showTextToast(mContext, mContext.getString(R.string.song_has_added));

                return;
            } else {
                int downloadedSize = DownloadInfoDB.getAudioInfoDB(mContext)
                        .getDownloadedSize(HPApplication.getInstance().getPlayIndexHashID());
                if (downloadedSize >= audioInfo.getFileSize()) {
                    ToastUtil.showTextToast(mContext, mContext.getString(R.string.song_has_downloaded));

                    return;
                }
            }

        } else {
            DownloadInfoDB.getAudioInfoDB(mContext).add(downloadInfo);


            ToastUtil.showTextToast(mContext, mContext.getString(R.string.has_add_to_download));


            //添加下载任务
            Intent updateIntent = new Intent(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNADD);
            updateIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            mContext.sendBroadcast(updateIntent);
        }
        //重新获取歌曲下载路径
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                //获取歌曲最新的下载路径
                if (audioInfo.getType() == AudioInfo.NET) {//不是第三方播放
                    SongInfoResult songInfoResult = SongInfoHttpUtil.songInfo(mContext, audioInfo.getHash());
                    if (songInfoResult != null) {
                        audioInfo.setDownloadUrl(songInfoResult.getUrl());
                    }
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

                mDownloadManager.startDownload((int) task.getCreateTime().getTime(), task);

                //更新下载歌曲个数
                Intent updateIntent = new Intent(AudioBroadcastReceiver.ACTION_DOWNLOADUPDATE);
                updateIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(updateIntent);
            }
        });

    }

    /**
     * 添加缓存任务
     * @param audioInfo
     */
    public synchronized void addCacheTask(final AudioInfo audioInfo) {
        // String fileName = audioInfo.getSingerName() + " - " + audioInfo.getSongName();
        //重新获取歌曲下载路径
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                //获取歌曲下载路径
                if (audioInfo.getType() == AudioInfo.NET) {//不是第三方播放
                    SongInfoResult songInfoResult = SongInfoHttpUtil.songInfo(mContext, audioInfo.getHash());
                    if (songInfoResult != null) {
                        audioInfo.setDownloadUrl(songInfoResult.getUrl());
                    }
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
//                    DownloadThreadDB.getDownloadThreadDB(mContext).delete(task.getTaskId(), task.getThreadNum());
                }

                logger.e("添加在线缓存任务：" + task.getTaskName() + " 任务id为：" + task.getTaskHash());

                mDownloadManager.startDownload((int) task.getCreateTime().getTime(), task);
            }
        });
    }

    /**
     * 获取任务的下载状态
     *
     * @param taskId
     * @return
     */
    public int taskIsDLStatus(String taskId) {
        for (Map.Entry<Integer, DownloadTask>  taskEntry: mDownloadManager.getDownloadTasks().entrySet()) {
            Integer what = taskEntry.getKey();
            DownloadTask task = taskEntry.getValue();
            if(task.getTaskId().equals(taskId)) {
                return task.getStatus();
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
        for (Map.Entry<Integer, DownloadTask>  taskEntry: mDownloadManager.getDownloadTasks().entrySet()) {
            Integer what = taskEntry.getKey();
            DownloadTask task = taskEntry.getValue();
            if(task.getTaskId().equals(taskId)) {
                mDownloadManager.stopDownload(what);
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
        for (Map.Entry<Integer, DownloadTask>  taskEntry: mDownloadManager.getDownloadTasks().entrySet()) {
            Integer what = taskEntry.getKey();
            DownloadTask task = taskEntry.getValue();
            if(task.getTaskId().equals(taskId)) {
                mDownloadManager.stopDownload(what);
            }
        }
    }

    /**
     * 取消任务
     *
     * @param taskId
     */
    public synchronized void cancelTask(String taskId) {
        for (Map.Entry<Integer, DownloadTask>  taskEntry: mDownloadManager.getDownloadTasks().entrySet()) {
            Integer what = taskEntry.getKey();
            DownloadTask task = taskEntry.getValue();
            if(task.getTaskId().equals(taskId)) {
                mDownloadManager.stopDownload(what);
            }
        }
    }
}
