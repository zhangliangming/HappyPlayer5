package base.utils;

import android.content.Context;
import android.util.Log;

import com.zlm.hp.constants.ResourceConstants;

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

    public LoggerUtil(Context context, String userName) {
        this.mContext = context;
        this.userName = userName;
    }

    /**
     * 创建zhangliangming用法
     *
     * @return
     */
    public static LoggerUtil getZhangLogger(Context context) {
        String name = "zhangliangming";
        LoggerUtil userLogger = new LoggerUtil(context, name);
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
                 Log.i(tag, name + " - " + str);
//                logger.info(name + " - " + str);
            } else {
                 Log.i(tag, str);
//                logger.info(str.toString());
            }
        }
    }

    public void d(String str) {
        String name = getFunctionName();
        if (logFlag) {
            if (name != null) {
                 Log.d(tag, name + " - " + str);
//                logger.debug(name + " - " + str);
            } else {
                  Log.d(tag, str);
//                logger.debug(str.toString());
            }
        }
    }

    public void f(String str) {
        String name = getFunctionName();
        if (logFlag) {

            if (name != null) {
                  Log.wtf(tag, name + " - " + str);
//                logger.fatal(name + " - " + str);
            } else {
                //  Log.wtf(tag, str);
//                logger.fatal(str.toString());
            }
        }
    }

    public void w(String str) {
        String name = getFunctionName();
        if (logFlag) {

            if (name != null) {
                 Log.w(tag, name + " - " + str);
//                logger.warn(name + " - " + str);
            } else {
                  Log.w(tag, str);
//                logger.warn(str.toString());
            }
        }
    }

    public void e(String str) {
        String name = getFunctionName();
        if (logFlag) {

            if (name != null) {
                  Log.e(tag, name + " - " + str);
//                logger.error(name + " - " + str);
            } else {
                  Log.e(tag, str);
//                logger.error(str.toString());
            }
        }
    }

}
