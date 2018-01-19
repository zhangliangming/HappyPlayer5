package base.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 配置文件处理类
 * Created by zhangliangming on 2017/8/6.
 */
public class PreferencesUtil {
    private static final String PREFERENCE_NAME = "PreferencesUtil";
    private static SharedPreferences preferences;

    /**
     * 保存数据到SharedPreferences配置文件
     *
     * @param context
     * @param key     关键字
     * @param value    要保存的数据
     */
    public static void putValue(Context context, String key, Object value) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
        }
        SharedPreferences.Editor editor = preferences.edit();
        if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putFloat(key, (Long) value);
        }

        // 提交修改
        editor.commit();
    }

    /**
     * 从SharedPreferences配置文件中获取数据
     *
     * @param context
     * @param key     关键字
     * @param value 默认获取的数据
     * @return
     */
    public static Object getValue(Context context, String key, Object value) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
        }

        if (value instanceof Boolean) {
            return preferences.getBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            return preferences.getInt(key, (Integer) value);
        } else if (value instanceof String) {
            return preferences.getString(key, (String) value);
        } else if (value instanceof Float) {
            return preferences.getFloat(key, (Float) value);
        } else if (value instanceof Long) {
            return preferences.getLong(key, (Long) value);
        }

        return null;

    }

    public static void putBooleanVaule(Context context, String key, boolean value) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);

        // 提交修改
        editor.commit();
    }

    public static boolean getBooleanValue(Context context, String key, boolean value) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
        }
        return preferences.getBoolean(key, value);
    }

    public static void putIntVaule(Context context, String key, int value) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);

        // 提交修改
        editor.commit();
    }

    public static int getIntValue(Context context, String key, int value) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
        }
        return preferences.getInt(key, value);
    }

    public static void putLongVaule(Context context, String key, long value) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);

        // 提交修改
        editor.commit();
    }

    public static long getLongValue(Context context, String key, long value) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
        }
        return preferences.getLong(key, value);
    }

    public static void putFloatVaule(Context context, String key, float value) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(key, value);

        // 提交修改
        editor.commit();
    }

    public static float getLongValue(Context context, String key, float value) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
        }
        return preferences.getFloat(key, value);
    }

    public static void putStringVaule(Context context, String key, String value) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);

        // 提交修改
        editor.commit();
    }

    public static String getStringValue(Context context, String key, String value) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
        }
        return preferences.getString(key, value);
    }
}
