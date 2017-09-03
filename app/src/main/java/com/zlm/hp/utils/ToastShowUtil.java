package com.zlm.hp.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;


/**
 * 弹窗口提示
 */
public class ToastShowUtil {
    private static Toast toast;

    public static void showTextToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }

        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    public static void showCenterTextToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
