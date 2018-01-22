package com.zlm.hp.libs.crash;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.widget.Toast;

import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.libs.utils.LoggerUtil;
import com.zlm.hp.manager.ActivityManage;
import com.zlm.hp.utils.ResourceFileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录错误报告.
 * <p>
 * 需要在Application中注册，为了要在程序启动器就监控整个程序。
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private LoggerUtil logger;
    private SimpleDateFormat crashfile = new SimpleDateFormat("yyyy-MM-dd");// 日志文件格式
    private int SDCARD_LOG_FILE_SAVE_DAYS = 3;// sd卡中日志文件的最多保存天数
    // 系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    // 程序的Context对象
    private Context mContext;
    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();

    /**
     * 保证只有一个CrashHandler实例
     */
    public CrashHandler() {
    }

    /**
     * 初始化
     */
    public void init(Context context) {
        mContext = context;
        logger = LoggerUtil.getZhangLogger(mContext);
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        cleanOldLogFile();
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.e(e.toString());
            }
            //退出程序
            ActivityManage.getInstance().exit();
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        // 收集设备参数信息
        collectDeviceInfo(mContext);
        // 使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "程序出现异常,即将退出！", Toast.LENGTH_SHORT)
                        .show();

                Looper.loop();
            }
        }.start();
        // 保存日志文件
        saveCatchInfoFile(ex);
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    private void collectDeviceInfo(Context ctx) {
        // 获取当前程序的版本号. 版本的id
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
        // 获取手机的硬件信息
        // 通过反射获取系统的硬件信息
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                e.printStackTrace();
                logger.e(e.getMessage());
            }
        }
    }

    /**
     * 保存日志文件
     *
     * @param ex
     */
    private void saveCatchInfoFile(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            // 用于格式化日期,作为日志文件名的一部分
            String time = crashfile.format(new Date());
            String fileName = time + ".log";
            String path = ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_CRASH, null);
            FileOutputStream fos = new FileOutputStream(path + fileName);
            fos.write(sb.toString().getBytes());
            fos.close();
            logger.e(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.toString());
        }
    }

    /**
     * 清除过期的日志文件
     */
    private void cleanOldLogFile() {
        File logFileParent = new File(ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_CRASH, null));
        if (logFileParent.exists()) {
            String needDelTime = crashfile.format(getDateBefore());
            File[] files = logFileParent.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        String fileName = files[i].getName();
                        fileName = fileName.substring(0,
                                fileName.lastIndexOf("."));
                        if (needDelTime.compareTo(fileName) > 0) {
                            files[i].delete();
                        }
                    }
                }
            }
        }
    }

    /**
     * @return
     */
    private Date getDateBefore() {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE)
                - SDCARD_LOG_FILE_SAVE_DAYS);
        return now.getTime();
    }
}