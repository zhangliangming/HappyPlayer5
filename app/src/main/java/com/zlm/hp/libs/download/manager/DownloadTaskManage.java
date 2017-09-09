package com.zlm.hp.libs.download.manager;

import android.content.Context;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.libs.download.DownloadTask;
import com.zlm.hp.libs.download.constant.DownloadTaskConstant;
import com.zlm.hp.libs.download.interfaces.IDownloadTaskEvent;
import com.zlm.hp.libs.download.utils.TaskThreadUtil;
import com.zlm.hp.libs.utils.DateUtil;
import com.zlm.hp.libs.utils.LoggerUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 下载任务管理器
 *
 * @author zhangliangming
 */
public class DownloadTaskManage {

    private Context context;

    /**
     * 任务队列
     */
    private List<DownloadTask> tasks = new ArrayList<DownloadTask>();

    /**
     * 正在等待
     */
    private boolean isWaiting = false;
    /**
     * 是否是第一个线程任务先开始
     */
    private boolean isTheOneTaskThreadFristStart = false;
    private LoggerUtil logger;
    /**
     * 主任务执行线程
     */
    public Runnable mainRunnable = new Runnable() {

        @Override
        public void run() {

            while (true) {
                synchronized (this) {
                    try {
                        isWaiting = true;
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // 执行任务
                startTaskThread();

            }
        }
    };

    /**
     * 主任务线程
     */
    public Thread mainThread = new Thread(mainRunnable);

    private IDownloadTaskEvent interfaceEvent;

    /**
     * 任务事件
     */
    private IDownloadTaskEvent taskEvent = new IDownloadTaskEvent() {

        @Override
        public int getTaskThreadDownloadedSize(DownloadTask task, int threadId) {
            if (interfaceEvent != null) {
                return interfaceEvent.getTaskThreadDownloadedSize(task, threadId);
            }
            return 0;
        }

        @Override
        public void taskThreadDownloading(DownloadTask task, int threadId,
                                          int downloadedSize) {

            if (interfaceEvent != null) {
                interfaceEvent.taskThreadDownloading(task, threadId,
                        downloadedSize);
            }

        }

        @Override
        public void taskThreadPause(DownloadTask task, int threadId,
                                    int downloadedSize) {

            if (interfaceEvent != null) {
                interfaceEvent
                        .taskThreadPause(task, threadId, downloadedSize);
            }

        }

        @Override
        public void taskThreadFinish(DownloadTask task, int threadId,
                                     int downloadedSize) {

            if (interfaceEvent != null) {
                interfaceEvent.taskThreadFinish(task, threadId,
                        downloadedSize);
            }

        }

        @Override
        public void taskThreadError(DownloadTask task, int threadId, String msg) {

            if (interfaceEvent != null) {
                interfaceEvent.taskThreadError(task, threadId, msg);
            }

        }

        @Override
        public void taskWaiting(DownloadTask task) {

            if (interfaceEvent != null) {
                interfaceEvent.taskWaiting(task);
            }

        }

        @Override
        public void taskDownloading(DownloadTask task, int downloadedSize) {

            if (interfaceEvent != null) {
                interfaceEvent.taskDownloading(task, downloadedSize);
            }

        }

        @Override
        public void taskPause(DownloadTask task, int downloadedSize) {

            if (interfaceEvent != null) {
                interfaceEvent.taskPause(task, downloadedSize);
            }

            for (int i = 0; i < tasks.size(); i++) {
                DownloadTask taskTemp = tasks.get(i);
                if (taskTemp.getTaskId().equals(task.getTaskId())) {
                    tasks.remove(i);

                    break;
                }
            }
            if (isWaiting) {
                synchronized (mainRunnable) {
                    mainRunnable.notify();
                }
            } else {
                startTaskThread();
            }

        }

        @Override
        public void taskFinish(DownloadTask task, int downloadedSize) {

            if (interfaceEvent != null) {
                interfaceEvent.taskFinish(task, downloadedSize);
            }


            for (int i = 0; i < tasks.size(); i++) {
                DownloadTask taskTemp = tasks.get(i);
                if (taskTemp.getTaskId().equals(task.getTaskId())) {
                    tasks.remove(i);
                    break;
                }
            }
            if (isWaiting) {
                synchronized (mainRunnable) {
                    mainRunnable.notify();
                }

            } else {
                startTaskThread();
            }

        }

        @Override
        public void taskError(DownloadTask task, String msg) {

            if (interfaceEvent != null) {
                interfaceEvent.taskError(task, msg);
            }


            for (int i = 0; i < tasks.size(); i++) {
                DownloadTask taskTemp = tasks.get(i);
                if (taskTemp.getTaskId().equals(task.getTaskId())) {
                    tasks.remove(i);
                    break;
                }
            }
            if (isWaiting) {
                synchronized (mainRunnable) {
                    mainRunnable.notify();
                }
            } else {
                startTaskThread();
            }

        }

        @Override
        public void taskCancel(DownloadTask task) {
            if (interfaceEvent != null) {
                interfaceEvent.taskCancel(task);
            }


            for (int i = 0; i < tasks.size(); i++) {
                DownloadTask taskTemp = tasks.get(i);
                if (taskTemp.getTaskId().equals(task.getTaskId())) {
                    tasks.remove(i);
                    break;
                }
            }
            if (isWaiting) {
                synchronized (mainRunnable) {
                    mainRunnable.notify();
                }
            } else {
                startTaskThread();
            }

        }
    };
    private HPApplication mHPApplication;


    public DownloadTaskManage(HPApplication hpApplication, Context context, boolean isTheOneTaskThreadFristStart, IDownloadTaskEvent interfaceEvent) {
        mainThread.start();
        this.mHPApplication = hpApplication;
        this.context = context;
        this.interfaceEvent = interfaceEvent;
        this.isTheOneTaskThreadFristStart = isTheOneTaskThreadFristStart;
        logger = LoggerUtil.getZhangLogger(context);
    }

    /**
     * 添加多线程多任务下载
     *
     * @param task
     * @throws Exception
     */
    public synchronized void addMultiThreadMultiTask(final DownloadTask task) throws Exception {
        new Thread() {
            @Override
            public void run() {
                TaskThreadUtil taskThreadUtil = new TaskThreadUtil(mHPApplication, task, taskEvent,
                        isTheOneTaskThreadFristStart);
                taskThreadUtil.startTask(context);
                task.setTaskThreadUtil(taskThreadUtil);
            }
        }.start();

        if (taskEvent != null) {

        }
    }

    /**
     * 添加多线程单个任务
     *
     * @param task
     * @throws Exception
     * @author zhangliangming
     * @date 2017年7月8日
     */
    public synchronized void addMultiThreadSingleTask(DownloadTask task) throws Exception {

        TaskThreadUtil taskThreadUtil = new TaskThreadUtil(mHPApplication, task, taskEvent,
                isTheOneTaskThreadFristStart);
        task.setTaskThreadUtil(taskThreadUtil);

        //
        if (tasks.size() == 0 || tasks.size() > 0) {
            task.setStatus(DownloadTaskConstant.WAIT.getValue());
            if (taskEvent != null) {
                taskEvent.taskWaiting(task);
            }
        }
        //
        tasks.add(task);
        // 任务队列不为0
        // if (tasks.size() > 0) {
        DownloadTask temp = tasks.get(0);
        // logger.e("isWaiting = " + isWaiting + " 任务当前状态是否是初始化：" + (temp.getStatus() == DownloadTaskConstant.INT.getValue()));

        // 如果等于初始化
        if (temp.getStatus() == DownloadTaskConstant.WAIT.getValue()) {
            if (isWaiting) {
                synchronized (mainRunnable) {
                    mainRunnable.notify();
                }
            } else {
                startTaskThread();
            }


        }
//        }
    }

    /**
     * 根据添加时间顺序下载
     *
     * @param task
     * @throws Exception
     */
    public synchronized void addMultiThreadSingleTaskOrderByTime(DownloadTask task) throws Exception {
        TaskThreadUtil taskThreadUtil = new TaskThreadUtil(mHPApplication, task, taskEvent,
                isTheOneTaskThreadFristStart);
        task.setTaskThreadUtil(taskThreadUtil);

        int i = 0;
        for (; i < tasks.size(); i++) {
            DownloadTask temp = tasks.get(i);
            if (DateUtil.parseDateToString(temp.getCreateTime()).compareTo(DateUtil.parseDateToString(task.getCreateTime())) > 0) {
                break;
            }
        }

        if (tasks.size() == 0 || tasks.size() > 0) {
            task.setStatus(DownloadTaskConstant.WAIT.getValue());
            if (taskEvent != null) {
                taskEvent.taskWaiting(task);
            }
        }
        tasks.add(i, task);
        //


        // 任务队列不为0
//        if (tasks.size() > 0) {
        DownloadTask temp = tasks.get(0);
        // logger.e("isWaiting = " + isWaiting + " 任务当前状态是否是初始化：" + (temp.getStatus() == DownloadTaskConstant.INT.getValue()));
        // 如果等于初始化
        if (temp.getStatus() == DownloadTaskConstant.WAIT.getValue()) {
            if (isWaiting) {
                synchronized (mainRunnable) {
                    mainRunnable.notify();
                }
            } else {
                startTaskThread();
            }

        }
//        }
    }

    /**
     * 添加多线程单个任务，根据任务id来排序任务，使用前提是任务id必须有序
     *
     * @param task
     * @throws Exception
     * @author zhangliangming
     * @date 2017年7月9日
     */
    public synchronized void addMultiThreadSingleTaskOrderByTId(DownloadTask task) throws Exception {
        TaskThreadUtil taskThreadUtil = new TaskThreadUtil(mHPApplication, task, taskEvent,
                isTheOneTaskThreadFristStart);
        task.setTaskThreadUtil(taskThreadUtil);

        int i = 0;
        for (; i < tasks.size(); i++) {
            DownloadTask temp = tasks.get(i);
            if (temp.getTaskId().compareTo(task.getTaskId()) > 0) {
                break;
            }
        }
        tasks.add(i, task);
        // 任务队列不为0
//        if (tasks.size() > 0) {
        DownloadTask temp = tasks.get(0);
        logger.e("isWaiting = " + isWaiting + " 任务当前状态是否是初始化：" + (temp.getStatus() == DownloadTaskConstant.INT.getValue()));
        // 如果等于初始化
        if (temp.getStatus() == DownloadTaskConstant.INT.getValue()) {
            if (isWaiting) {
                synchronized (mainRunnable) {
                    mainRunnable.notify();
                }
            } else {
                startTaskThread();
            }

        }
//        }
    }

    /**
     * 执行任务线程
     *
     * @author zhangliangming
     * @date 2017年7月8日
     */
    protected synchronized void startTaskThread() {
        if (tasks.size() > 0) {
            DownloadTask task = tasks.get(0);
            if (task.getStatus() == DownloadTaskConstant.WAIT.getValue()) {
                task.setStatus(DownloadTaskConstant.DOWNLOADING.getValue());
                TaskThreadUtil taskThreadUtil = task.getTaskThreadUtil();
                taskThreadUtil.startTask(context);
                logger.e(task.getTaskName() + " 任务正在下载，任务id是：" + task.getTaskId());
            }

        }
    }

    /**
     * 暂停任务
     *
     * @param taskId
     * @author zhangliangming
     * @date 2017年7月9日
     */
    public synchronized void pauseTask(String taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            DownloadTask task = tasks.get(i);
            if (task.getTaskId().equals(taskId)) {
                if (i == 0) {

                    TaskThreadUtil taskThreadUtil = task.getTaskThreadUtil();
                    taskThreadUtil.pauseTaskThread();

                } else {
                    if (taskEvent != null) {
                        taskEvent.taskPause(task, 0);
                    }
                }
                break;
            }
        }
    }

    /**
     * 取消任务
     *
     * @param taskId
     * @author zhangliangming
     * @date 2017年7月9日
     */
    public synchronized void cancelTask(String taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            DownloadTask task = tasks.get(i);
            if (task.getTaskId().equals(taskId)) {
                if (i == 0) {

                    TaskThreadUtil taskThreadUtil = task.getTaskThreadUtil();
                    taskThreadUtil.cancelTaskThread();

                } else {

                    if (taskEvent != null) {
                        taskEvent.taskCancel(task);
                    }
                }
                break;
            }
        }
    }

    public List<DownloadTask> getTasks() {
        return tasks;
    }
}
