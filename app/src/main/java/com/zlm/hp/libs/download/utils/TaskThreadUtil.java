package com.zlm.hp.libs.download.utils;

import android.content.Context;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.libs.download.DownloadTask;
import com.zlm.hp.libs.download.interfaces.IDownloadTaskEvent;
import com.zlm.hp.libs.download.interfaces.IDownloadTaskThreadEven;
import com.zlm.hp.libs.download.thread.TaskThread;
import com.zlm.hp.libs.utils.LoggerUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * 任务线程处理
 *
 * @author zhangliangming
 */
public class TaskThreadUtil {
    /**
     * 任务线程
     */
    private List<TaskThread> taskThreads = new ArrayList<TaskThread>();
    /**
     * 任务
     */
    private DownloadTask task;
    /**
     * 文件大小
     */
    private int fileLength = 0;
    /**
     * 任务事件
     */
    private IDownloadTaskEvent taskEvent;

    /**
     * 任务线程错误信息
     */
    private String taskThreadErrorMsg = "";

    /**
     * 是否是第一个线程任务先执行
     */
    private boolean isTheOneTaskThreadFristStart;
    private HPApplication mHPApplication;

    /**
     * 是否已完成
     */
    private boolean isFinish = false;
    /**
     * 更新下载进程
     */
    private Thread mUpdateDownloadThread = new Thread() {
        @Override
        public void run() {
            while (!isFinish) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //更新任务线程
                if (taskEvent != null) {
                    int taskDownloadedSize = getTaskDownloadedSize();
                    if (taskDownloadedSize != 0)
                        taskEvent.taskDownloading(task, taskDownloadedSize);
                }
            }
        }
    };
    /**
     *
     */
    private LoggerUtil logger;

    public TaskThreadUtil(HPApplication hpApplication, DownloadTask task, IDownloadTaskEvent taskEvent,
                          boolean isTheOneTaskThreadFristStart) {
        this.mHPApplication = hpApplication;
        this.task = task;
        this.taskEvent = taskEvent;
        this.isTheOneTaskThreadFristStart = isTheOneTaskThreadFristStart;
        logger = LoggerUtil.getZhangLogger(hpApplication.getApplicationContext());
    }

    /**
     * 任务线程事件
     */
    private IDownloadTaskThreadEven taskThreadEven = new IDownloadTaskThreadEven() {

        @Override
        public int getTaskThreadDownloadedSize(DownloadTask task, int threadId) {
            if (taskEvent != null) {
                return taskEvent.getTaskThreadDownloadedSize(task, threadId);
            }
            return 0;
        }

        @Override
        public void taskThreadDownloading(DownloadTask task, int threadId,
                                          int downloadedSize) {
            if (taskEvent != null) {
                taskEvent.taskThreadDownloading(task, threadId,
                        downloadedSize);
            }
        }

        @Override
        public void taskThreadPause(DownloadTask task, int threadId,
                                    int downloadedSize) {

            if (taskEvent != null)
                taskEvent.taskThreadPause(task, threadId, downloadedSize);

        }

        @Override
        public void taskThreadFinish(DownloadTask task, int threadId,
                                     int downloadedSize) {
            if (taskEvent != null) {
                taskEvent.taskThreadFinish(task, threadId, downloadedSize);
            }
            int taskDownloadedSize = getTaskDownloadedSize();
            if (taskDownloadedSize >= fileLength && !isFinish) {
                isFinish = true;
                if (taskEvent != null) {
                    taskEvent.taskFinish(task, taskDownloadedSize);
                }

                if (task.getTaskPath() != null) {
                    // 临时文件复制到真正的路径
                    copyFile(task.getTaskTempPath(), task.getTaskPath());
                }
            }
            // 开始其它线程任务
            if (isTheOneTaskThreadFristStart && threadId == 1) {

                for (int i = 0; i < taskThreads.size(); i++) {
                    TaskThread taskThread = taskThreads.get(i);
                    if (taskThread.getThreadId() != 1) {
                        taskThread.start();
                    }
                }

            }

        }

        @Override
        public void taskThreadError(DownloadTask task, int threadId, String msg) {

            if (taskEvent != null) {
                taskEvent.taskThreadError(task, threadId, msg);
            }
            if (taskThreadErrorMsg.equals("")) {
                taskThreadErrorMsg = "错误信息如下：";
                for (int i = 0; i < taskThreads.size(); i++) {
                    TaskThread taskThread = taskThreads.get(i);
                    taskThreadErrorMsg += taskThread.getTaskThreadErrorMsg();
                }

                if (taskEvent != null) {
                    taskEvent.taskError(task, taskThreadErrorMsg);
                }

                taskThreadErrorMsg = "";
            }

        }

    };

    /**
     * 获取任务的总下载进度
     *
     * @return
     * @author zhangliangming
     * @date 2017年7月8日
     */
    public synchronized int getTaskDownloadedSize() {
        int downloadedSize = 0;

        // logger.e("taskThreads:" + taskThreads.size());
        for (int i = 0; i < taskThreads.size(); i++) {
            TaskThread taskThread = taskThreads.get(i);

            // logger.e("taskThreadsgetDownloadedSize:" + taskThread.getDownloadedSize());

            downloadedSize += taskThread.getDownloadedSize();
        }
        return downloadedSize;
    }

    /**
     * 开始任务
     *
     * @author zhangliangming
     * @date 2017年7月8日
     */
    public void startTask(Context context) {
        // 1.获取文件的长度
        // 2.对文件进行多线程下载
        try {
            // 1获取文件的长度
            fileLength = (int) task.getTaskFileSize();
            if (fileLength == 0)
                fileLength = getFileLength(task.getTaskUrl());
            if (fileLength <= 0) {
                // 获取文件的长度失败

                if (taskEvent != null) {
                    taskEvent.taskError(task, "获取文件的长度失败");
                }

                return;
            }
            //
            File destFile = new File(task.getTaskTempPath());
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }
            //目标文件不存在
            if (!destFile.exists()) {

                // 临时文件
                RandomAccessFile tempFile = new RandomAccessFile(
                        task.getTaskTempPath(), "rw");
                tempFile.setLength(fileLength);
                tempFile.close();
            }


            // 2对文件进行多线程下载
            int threadNum = task.getThreadNum();
            int avg = fileLength / threadNum;
            for (int i = 0; i < threadNum; i++) {
                int threadId = (i + 1);
                int startPos = i * avg;
                int endPos = 0;
                if (i == (threadNum - 1)) {
                    endPos = fileLength;
                } else {
                    endPos = startPos + avg;
                }

                //
                TaskThread taskThread = new TaskThread(mHPApplication, context, threadId, startPos,
                        endPos, task, taskThreadEven);
                taskThreads.add(taskThread);


                // logger.e("添加线程任务，任务个数：" + taskThreads.size());

                if (isTheOneTaskThreadFristStart) {
                    if (threadId == 1) {
                        taskThread.start();
                    }
                } else {
                    taskThread.start();
                }
            }
            mUpdateDownloadThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            // 下载出错
            if (taskEvent != null) {
                taskEvent.taskError(task, e.getMessage());
            }
        }
    }

    /**
     * 暂停
     *
     * @author zhangliangming
     * @date 2017年7月9日
     */
    public void pauseTaskThread() {
        for (int i = 0; i < taskThreads.size(); i++) {
            TaskThread taskThread = taskThreads.get(i);
            taskThread.setCanDownload(false);
        }
        if (taskEvent != null) {
            taskEvent.taskPause(task, getTaskDownloadedSize());
        }
    }

    /**
     * 取消任务
     *
     * @author zhangliangming
     * @date 2017年7月9日
     */
    public void cancelTaskThread() {
        for (int i = 0; i < taskThreads.size(); i++) {
            TaskThread taskThread = taskThreads.get(i);
            taskThread.setCanDownload(false);
        }
        if (taskEvent != null) {
            taskEvent.taskCancel(task);
        }

    }

    /**
     * 获取下载文件的长度
     *
     * @param downloadUrl
     * @return
     * @throws Exception
     * @author zhangliangming
     * @date 2017年7月8日
     */
    private int getFileLength(String downloadUrl) {
        int length = 0;
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            seURLConnectiontHeader(conn);
            length = conn.getContentLength();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return length;
    }

    /**
     * 设置请求头
     *
     * @param conn
     * @author zhangliangming
     * @date 2017年7月8日
     */
    private void seURLConnectiontHeader(URLConnection conn) {
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

    /**
     * 复制文件
     *
     * @param oldPath 旧文件路径
     * @param newPath 新文件路径
     * @author zhangliangming
     * @date 2017年7月8日
     */
    private void copyFile(String oldPath, String newPath) {
        File oldfile = null;
        try {
            int bytesum = 0;
            int byteread = 0;
            oldfile = new File(oldPath);
            if (oldfile.exists()) { // 文件存在时
                InputStream inStream = new FileInputStream(oldPath); // 读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (oldfile != null) {
                System.gc();
                oldfile.delete();
            }

        }
    }

}
