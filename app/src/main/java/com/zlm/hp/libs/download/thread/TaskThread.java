package com.zlm.hp.libs.download.thread;

import android.content.Context;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.libs.download.DownloadTask;
import com.zlm.hp.libs.download.interfaces.IDownloadTaskThreadEven;
import com.zlm.hp.libs.utils.LoggerUtil;
import com.zlm.hp.libs.utils.NetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


/**
 * 任务线程
 *
 * @author zhangliangming
 */
public class TaskThread extends Thread {
    /**
     * 连接超时时间
     */
    private final int CONNECTTIME = 10 * 1000;
    /**
     * 读取数据超时时间
     */
    private final int READTIME = 10 * 1000;
    /**
     *
     */
    private final int BUFF_LENGTH = 1024 * 8;
    /**
     * 任务
     */
    private DownloadTask task;
    /**
     * 线程id
     */
    private int threadId = -1;
    /**
     * 旧的开始位置
     */
    private int startPos = 0;
    /**
     * 新的开始位置
     */
    private int newStartPos = 0;
    /**
     * 结束位置
     */
    private int endPos = 0;
    /**
     * 下载大小
     */
    private int downloadedSize = 0;
    /**
     * 是否已完成
     */
    private boolean isFinish = false;
    /**
     * 是否能继续下载
     */
    private boolean canDownload = true;
    /**
     *
     */
    private RandomAccessFile itemFile;
    /**
     * 线程任务事件
     */
    private IDownloadTaskThreadEven taskThreadEven;

    /**
     * 线程任务的错误信息
     */
    private String taskThreadErrorMsg = "";

    private Context context;
    private HPApplication mHPApplication;
    /**
     *
     */
    private LoggerUtil logger;
    /**
     * 更新下载进程
     */
    private Thread mUpdateDownloadThread = new Thread() {
        @Override
        public void run() {
            while (!isFinish && canDownload) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //更新任务线程
                if (taskThreadEven != null) {
                    int taskThreadDownloadedSize = getDownloadedSize();
                    if (taskThreadDownloadedSize != 0)
                        taskThreadEven.taskThreadDownloading(task,threadId, taskThreadDownloadedSize);
                }
            }
        }
    };


    public TaskThread(HPApplication hpApplication, Context context, int threadId, int startPos, int endPos,
                      DownloadTask task, IDownloadTaskThreadEven taskThreadEven) {
        this.mHPApplication = hpApplication;
        this.context = context;
        this.threadId = threadId;
        this.startPos = startPos;
        this.task = task;
        this.endPos = endPos;
        this.taskThreadEven = taskThreadEven;
        //
        logger = LoggerUtil.getZhangLogger(context);
        downloadedSize = taskThreadEven.getTaskThreadDownloadedSize(task, threadId);

        //if (threadId == 1)
        //logger.e("name=" + task.getTaskName() + " threadId=" + threadId + " 任务已下载：" + downloadedSize);

    }

    /**
     * 设置请求头
     *
     * @param conn
     * @author zhangliangming
     * @date 2017年7月8日
     */
    private static void seURLConnectiontHeader(URLConnection conn) {
        conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.3) Gecko/2008092510 Ubuntu/8.04 (hardy) Firefox/3.0.3");
        conn.setRequestProperty("Accept-Language", "en-us,en;q=0.7,zh-cn;q=0.3");
        conn.setRequestProperty("Accept-Encoding", "utf-8");
        conn.setRequestProperty("Accept-Charset",
                "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        conn.setRequestProperty("Keep-Alive", "300");
        conn.setRequestProperty("connnection", "keep-alive");
        conn.setRequestProperty("If-Modified-Since",
                "Fri, 02 Jan 2009 17:00:05 GMT");
        conn.setRequestProperty("If-None-Match", "\"1261d8-4290-df64d224\"");
        conn.setRequestProperty("Cache-conntrol", "max-age=0");
    }

    @Override
    public void run() {

        // 设置新的开始位置
        newStartPos = startPos + downloadedSize;

        mUpdateDownloadThread.start();
        if (endPos > newStartPos && canDownload) {

            try {

                URL url = new URL(task.getTaskUrl());
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                // 设置连接超时时间
                conn.setConnectTimeout(CONNECTTIME);
                // 设置读取数据超时时间
                conn.setReadTimeout(READTIME);
                seURLConnectiontHeader(conn);
                conn.setRequestProperty("Range", "bytes=" + newStartPos + "-"
                        + endPos);
                // 获取文件输入流，读取文件内容
                InputStream is = conn.getInputStream();

                //
                itemFile = new RandomAccessFile(task.getTaskTempPath(), "rw");
                itemFile.seek(newStartPos);

                byte[] buff = new byte[BUFF_LENGTH];
                int length = -1;
                while ((length = is.read(buff)) > 0
                        && (startPos + downloadedSize) < endPos) {

                    if (!NetUtil.isNetworkAvailable(context)) {


                        if (taskThreadEven != null) {
                            //无网络
                            taskThreadErrorMsg = "无网络";
                            taskThreadEven.taskThreadError(task,threadId,
                                    taskThreadErrorMsg);
                        }

                        return;
                    }

                    if (mHPApplication.isWifi() && !NetUtil.isWifi(context)) {
                        // 不是wifi
                        if (taskThreadEven != null) {
                            taskThreadErrorMsg = "非wifi网络";
                            taskThreadEven.taskThreadError(task,threadId,
                                    taskThreadErrorMsg);
                        }
                        return;
                    }


                    itemFile.write(buff, 0, length);
                    downloadedSize += length;

//                    if (taskThreadEven != null)
//                        taskThreadEven.taskThreadDownloading(task.getTaskId(), task.getThreadNum(),
//                                threadId, getDownloadedSize());

                }

                is.close();

                isFinish = true;
                if (taskThreadEven != null)
                    taskThreadEven.taskThreadFinish(task,threadId,
                            getDownloadedSize());

            } catch (Exception e) {
                e.printStackTrace();

                if (taskThreadEven != null) {
                    taskThreadErrorMsg = e.getMessage();
                    taskThreadEven.taskThreadError(task,threadId,
                            taskThreadErrorMsg);
                }

            } finally {
                if (itemFile != null)
                    try {
                        itemFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }

        } else {
            downloadedSize = endPos - startPos;
            //  logger.e("name=" + task.getTaskName() + " threadId=" + threadId + " 下载了：" + downloadedSize);
            isFinish = true;
            if (taskThreadEven != null)
                taskThreadEven.taskThreadFinish(task,threadId,
                        getDownloadedSize());
        }
    }

    /***
     *
     * 获取总下载进度
     *
     * @return
     * @author zhangliangming
     * @date 2017年7月8日
     */
    public synchronized int getDownloadedSize() {
        //  logger.e("name=" + task.getTaskName() + " threadId=" + threadId + " 当前下载大小：" + downloadedSize);
        int tempDownloadedSize = Math.min(downloadedSize,
                (endPos - startPos));

        //  logger.e("tempDownloadedSize=" + tempDownloadedSize);

        return tempDownloadedSize;
    }

    /**
     * 获取错误信息
     *
     * @return
     * @author zhangliangming
     * @date 2017年7月9日
     */
    public String getTaskThreadErrorMsg() {
        return taskThreadErrorMsg + "\n";
    }

    ////

    public int getThreadId() {
        return threadId;
    }

    public void setCanDownload(boolean canDownload) {
        this.canDownload = canDownload;
    }
}
