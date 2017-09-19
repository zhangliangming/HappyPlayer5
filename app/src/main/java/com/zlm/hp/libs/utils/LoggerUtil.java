package com.zlm.hp.libs.utils;

import android.content.Context;

import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.utils.ResourceFileUtil;

import org.apache.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class LoggerUtil {
    /**
     * 是否输出log
     */
    private final static boolean logFlag = true;

    /**
     * 应用名标签
     */
    public final static String tag = "[" + ResourceConstants.APPNAME + "]";
    private String userName = null;
    private Context mContext = null;
    /**
     * 保存用户对应的Logger
     */
    private static Hashtable<String, LoggerUtil> sLoggerTable = new Hashtable<String, LoggerUtil>();

    private static int SDCARD_LOG_FILE_SAVE_DAYS = 3;// sd卡中日志文件的最多保存天数
    private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");// 日志文件格式
    private static Logger logger;
    private static LogConfigurator logConfigurator;

    public LoggerUtil(Context context, String userName) {
        this.mContext = context;
        this.userName = userName;
        if (logger == null) {
            initLog();
            logger = Logger.getLogger(tag);
            logger.info("\n--------应用启动--------\n");
        }
    }

    /**
     * 初始化log的相关配置
     */
    private void initLog() {
        logConfigurator = new LogConfigurator();

        String time = logfile.format(new Date());
        String fileName = time + ".log";
        String path = ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_LOGCAT, fileName);
        logConfigurator.setFileName(path);
        // 输出内容的格式
        logConfigurator.setFilePattern("%d{yyyy-MM-dd HH:mm:ss} <%m>%n");
        // 文件大小500k
        logConfigurator.setMaxFileSize(1024 * 500);
        // 备份
        logConfigurator.setMaxBackupSize(1);
        // 表示所有消息都会被立即输出，设为false则不输出，默认值是true。
        logConfigurator.setImmediateFlush(true);
        logConfigurator.configure();

        cleanOldLogFile();

    }

    /**
     * 清除过期的log文件
     */
    private void cleanOldLogFile() {
        File logFileParent = new File(ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_LOGCAT, null));
        if (logFileParent.exists()) {
            String needDelTime = logfile.format(getDateBefore());
            File[] files = logFileParent.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        String fileName = files[i].getName();
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));
                        if (needDelTime.compareTo(fileName) > 0) {
                            files[i].delete();
                        }
                    }
                }
            }
        }
    }

    /**
     * 得到现在时间前的几天日期，用来得到需要删除的日志文件名
     */
    private Date getDateBefore() {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE)
                - SDCARD_LOG_FILE_SAVE_DAYS);
        return now.getTime();
    }

    /**
     * @param userName 用戶名
     * @return
     */
    public static LoggerUtil getLogger(Context context, String userName) {
        LoggerUtil userLogger = sLoggerTable.get(userName);
        if (userLogger == null) {
            userLogger = new LoggerUtil(context, userName);
            sLoggerTable.put(userName, userLogger);
        }
        return userLogger;

    }

    /**
     * 创建zhangliangming用法
     *
     * @return
     */
    public static LoggerUtil getZhangLogger(Context context) {
        String name = "zhangliangming";
        LoggerUtil userLogger = sLoggerTable.get(name);
        if (userLogger == null) {
            userLogger = new LoggerUtil(context, name);
            sLoggerTable.put(name, userLogger);
        }
        return userLogger;
    }

    /**
     * 获取方法名
     *
     * @return
     */
    private String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }
            return "@" + userName + "@ " + "[ "
                    + Thread.currentThread().getName() + ": "
                    + st.getClassName() + ":" + st.getLineNumber() + " "
                    + st.getMethodName() + " ]";
        }
        return null;
    }

    public void i(String str) {
        String name = getFunctionName();
        if (logFlag) {

            if (name != null) {
                // Log.i(tag, name + " - " + str);
                logger.info(name + " - " + str);
            } else {
                // Log.i(tag, str);
                logger.info(str.toString());
            }
        }
    }

    public void d(String str) {
        String name = getFunctionName();
        if (logFlag) {
            if (name != null) {
                // Log.d(tag, name + " - " + str);
                logger.debug(name + " - " + str);
            } else {
                //  Log.d(tag, str);
                logger.debug(str.toString());
            }
        }
    }

    public void f(String str) {
        String name = getFunctionName();
        if (logFlag) {

            if (name != null) {
                //  Log.wtf(tag, name + " - " + str);
                logger.fatal(name + " - " + str);
            } else {
                //  Log.wtf(tag, str);
                logger.fatal(str.toString());
            }
        }
    }

    public void w(String str) {
        String name = getFunctionName();
        if (logFlag) {

            if (name != null) {
                // Log.w(tag, name + " - " + str);
                logger.warn(name + " - " + str);
            } else {
                //  Log.w(tag, str);
                logger.warn(str.toString());
            }
        }
    }

    public void e(String str) {
        String name = getFunctionName();
        if (logFlag) {

            if (name != null) {
                //  Log.e(tag, name + " - " + str);
                logger.error(name + " - " + str);
            } else {
                //  Log.e(tag, str);
                logger.error(str.toString());
            }
        }
    }

}
