package com.zlm.hp.libs.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 配置文件处理类
 * Created by zhangliangming on 2017/8/6.
 */
public class PreferencesUtil {
    private static final String PREFERENCE_NAME = "com.zlm.hp.sp";
    private static SharedPreferences preferences;

    /**
     * 保存数据到SharedPreferences配置文件
     *
     * @param context
     * @param key     关键字
     * @param data    要保存的数据
     */
    public static void saveValue(Context context, String key, Object data) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
        }
        SharedPreferences.Editor editor = preferences.edit();
        if (data instanceof Boolean) {
            editor.putBoolean(key, (Boolean) data);
        } else if (data instanceof Integer) {
            editor.putInt(key, (Integer) data);
        } else if (data instanceof String) {
            editor.putString(key, (String) data);
        } else if (data instanceof Float) {
            editor.putFloat(key, (Float) data);
        } else if (data instanceof Long) {
            editor.putFloat(key, (Long) data);
        }

        // 提交修改
        editor.commit();
    }

    /**
     * 从SharedPreferences配置文件中获取数据
     *
     * @param context
     * @param key     关键字
     * @param defData 默认获取的数据
     * @return
     */
    public static Object getValue(Context context, String key, Object defData) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
        }

        if (defData instanceof Boolean) {
            return preferences.getBoolean(key, (Boolean) defData);
        } else if (defData instanceof Integer) {
            return preferences.getInt(key, (Integer) defData);
        } else if (defData instanceof String) {
            return preferences.getString(key, (String) defData);
        } else if (defData instanceof Float) {
            return preferences.getFloat(key, (Float) defData);
        } else if (defData instanceof Long) {
            return preferences.getLong(key, (Long) defData);
        }

        return null;

    }
}
