package com.zlm.hp.libs.utils;

import android.content.Context;
import android.content.Intent;

import com.zlm.hp.receiver.SystemReceiver;


/**
 * 弹窗口提示
 */
public class ToastUtil {

    public static final String MESSAGEKEY = "com.zlm.hp.toast.message";

    public static void showTextToast(Context context, String msg) {

        Intent intent = new Intent(SystemReceiver.ACTION_TOASTMESSAGE);
        intent.putExtra(MESSAGEKEY, msg);
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(intent);

    }
}
