package com.zlm.hp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.libs.utils.LoggerUtil;

import java.util.Date;

/**
 * 在线音频广播
 * Created by zhangliangming on 2017/8/12.
 */

public class OnLineAudioReceiver {
    /**
     *
     */
    private LoggerUtil logger;
    /**
     * 是否注册成功
     */
    private boolean isRegisterSuccess = false;
    private Context mContext;
    private HPApplication mHPApplication;

    /**
     * 注册成功广播
     */
    private String ACTION_ONLINEMUSICSUCCESS = "com.zlm.hp.online.music.success_" + new Date().getTime();
    public static String ACTION_ONLINEMUSICDOWNLOADING = "com.zlm.hp.online.music.downloading";
    public static String ACTION_ONLINEMUSICERROR = "com.zlm.hp.online.music.error";
    private BroadcastReceiver mOnlineAudioBroadcastReceiver;
    private IntentFilter mOnlineAudioIntentFilter;
    private OnlineAudioReceiverListener mOnlineAudioReceiverListener;

    public OnLineAudioReceiver(Context context, HPApplication hPApplication) {
        this.mHPApplication = hPApplication;
        this.mContext = context;
        logger = LoggerUtil.getZhangLogger(context);

        //
        mOnlineAudioIntentFilter = new IntentFilter();
        mOnlineAudioIntentFilter.addAction(ACTION_ONLINEMUSICSUCCESS);
        mOnlineAudioIntentFilter.addAction(ACTION_ONLINEMUSICDOWNLOADING);
        mOnlineAudioIntentFilter.addAction(ACTION_ONLINEMUSICERROR);
    }

    /**
     *
     */
    private Handler mOnlineAudioHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (mOnlineAudioReceiverListener != null) {
                Intent intent = (Intent) msg.obj;

                if (intent.getAction().equals(ACTION_ONLINEMUSICSUCCESS)) {
                    isRegisterSuccess = true;
                } else {
                    mOnlineAudioReceiverListener.onReceive(mContext, intent);
                }
            }
        }
    };

    /**
     * 注册广播
     *
     * @param context
     */
    public void registerReceiver(Context context) {
        if (mOnlineAudioBroadcastReceiver == null) {
            //
            mOnlineAudioBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Message msg = new Message();
                    msg.obj = intent;
                    mOnlineAudioHandler.sendMessage(msg);


                }
            };

            mContext.registerReceiver(mOnlineAudioBroadcastReceiver, mOnlineAudioIntentFilter);
            //发送注册成功广播
            Intent successIntent = new Intent(ACTION_ONLINEMUSICSUCCESS);
            successIntent .setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            mContext.sendBroadcast(successIntent);

        }
    }

    /**
     * 取消注册广播
     */
    public void unregisterReceiver(Context context) {
        if (mOnlineAudioBroadcastReceiver != null && isRegisterSuccess) {

            mContext.unregisterReceiver(mOnlineAudioBroadcastReceiver);

        }

    }


    public void setOnlineAudioReceiverListener(OnlineAudioReceiverListener onlineAudioReceiverListener) {
        this.mOnlineAudioReceiverListener = onlineAudioReceiverListener;
    }

    ///////////////////////////////////
    public interface OnlineAudioReceiverListener {
        void onReceive(Context context, Intent intent);
    }

}
