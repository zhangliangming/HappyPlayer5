package com.zlm.hp.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 *    author : Senh Linsh
 *    github : https://github.com/SenhLinsh
 *    date   : 2017/11/13
 *    desc   : 工具类: 字符串相关
 *             API  : 判空, 编码 等
 * </pre>
 */
public class StringUtils {

    private StringUtils() {
    }

    /**
     * 判断字符串是否为空
     *
     * @param string 指定字符串
     * @return null 或 空字符串返回 true, 否则返回 false
     */
    public static boolean isEmpty(CharSequence string) {
        return (string == null || string.length() == 0);
    }

    /**
     * 判断字符串是否不为空
     *
     * @param string 指定字符串
     * @return 不为 null 且 长度大于 0 返回 true, 否则返回 false
     */
    public static boolean notEmpty(CharSequence string) {
        return string != null && string.length() > 0;
    }

    /**
     * 判断所有的字符串是否都为空
     *
     * @param strings 指定字符串数组获或可变参数
     * @return 所有字符串都为空返回 true, 否则放回 false
     */
    public static boolean isAllEmpty(CharSequence... strings) {
        if (strings == null) return true;
        for (CharSequence charSequence : strings) {
            if (!isEmpty(charSequence)) return false;
        }
        return true;
    }

    /**
     * 判断所有的字符串是否都不为空
     *
     * @param strings 指定字符串数组获或可变参数
     * @return 所有字符串都不为空返回 true, 否则放回 false
     */
    public static boolean isAllNotEmpty(CharSequence... strings) {
        if (strings == null) return false;
        for (CharSequence charSequence : strings) {
            if (isEmpty(charSequence)) return false;
        }
        return true;
    }

    /**
     * 判断所有的字符串是否不都为空
     *
     * @param strings 指定字符串数组获或可变参数
     * @return 所有字符串不都为空返回 true, 否则返回 false
     */
    public static boolean isNotAllEmpty(CharSequence... strings) {
        return !isAllEmpty(strings);
    }

    /**
     * 判断字符串是否为空或空格
     *
     * @param string 指定字符串
     * @return null 或空字符串或空格字符串返回true, 否则返回 false
     */
    public static boolean isTrimEmpty(String string) {
        return (string == null || string.trim().length() == 0);
    }

    /**
     * 判断字符串是否为空或空白
     *
     * @param string 指定字符串
     * @return null 或空白字符串返回true, 否则返回 false
     */
    public static boolean isBlank(String string) {
        if (string == null) return true;
        for (int i = 0, len = string.length(); i < len; ++i) {
            if (!Character.isWhitespace(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断两个字符串是否相同
     *
     * @param a 作为对比的字符串
     * @param b 作为对比的字符串
     * @return 是否相同
     */
    public static boolean isEquals(String a, String b) {
        return a == b || (a != null && a.equals(b));
    }

    /**
     * 判断两个字符串是否不同
     *
     * @param a 作为对比的字符串
     * @param b 作为对比的字符串
     * @return 是否不同
     */
    public static boolean notEquals(String a, String b) {
        return !isEquals(a, b);
    }

    /**
     * null 转 空字符串
     *
     * @param obj 对象
     * @return 将 null 对象返回空字符串(""), 其他对象调用 toString() 返回的字符串
     */
    public static String nullStrToEmpty(Object obj) {
        return (obj == null ? "" : (obj instanceof String ? (String) obj : obj.toString()));
    }

    /**
     * 将字符串进行 UTF-8 编码
     *
     * @param string 指定字符串
     * @return 编码后的字符串
     */
    public static String utf8Encode(String string) {
        if (!isEmpty(string) && string.getBytes().length != string.length()) {
            try {
                return URLEncoder.encode(string, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UnsupportedEncodingException occurred. ", e);
            }
        }
        return string;
    }

    /**
     * 将字符串进行 UTF-8 编码
     *
     * @param string        指定字符串
     * @param defaultReturn 编码失败返回的字符串
     * @return 编码后的字符串
     */
    public static String utf8Encode(String string, String defaultReturn) {
        if (!isEmpty(string) && string.getBytes().length != string.length()) {
            try {
                return URLEncoder.encode(string, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return defaultReturn;
            }
        }
        return string;
    }

    /**
     * 判断字符串中是否存在中文汉字
     *
     * @param string 指定字符串
     * @return 是否存在
     */
    public static boolean hasChineseChar(String string) {
        boolean temp = false;
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(string);
        if (m.find()) {
            temp = true;
        }
        return temp;
    }


    /**
     * 格式化字符串, 用参数进行替换, 例子: format("I am {arg1}, {arg2}", arg1, arg2);
     *
     * @param format 需要格式化的字符串
     * @param args   格式化参数
     * @return 格式化后的字符串
     */
    public static String format(String format, Object... args) {
        for (Object arg : args) {
            format = format.replaceFirst("\\{[^\\}]+\\}", arg.toString());
        }
        return format;
    }
}
