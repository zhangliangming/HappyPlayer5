package com.zlm.hp.libs.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间处理
 * Created by zhangliangming on 2017/8/4.
 */
public class DateUtil {

    /**
     * 日期转字符串 yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String parseDateToString(Date date) {
        if (date == null) {
            return null;
        }
        try {
            DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateformat.format(date);
        } catch (Exception e) {
            return null;
        }
    }

}
