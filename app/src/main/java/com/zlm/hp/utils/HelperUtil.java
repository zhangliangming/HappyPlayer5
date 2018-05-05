package com.zlm.hp.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * 加载对话框帮助类
 */
public class HelperUtil {
    ProgressDialog progressDialog = null;
    private Activity mActivity;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isLoading()) {
                String text = (String) msg.obj;
                progressDialog.setMessage(text);
            }
        }
    };


    public HelperUtil(Activity act) {
        super();
        this.mActivity = act;
    }

    /**
     * 显示加载对话
     *
     * @param str 对话框上的提示信
     */
    public void showLoading(String str) {
        if (progressDialog == null) {
            // 先判断是否为null，可避免重复创建
            progressDialog = new ProgressDialog(mActivity);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(str);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    /**
     * 刷新提示信息
     *
     * @param text
     */
    public void refreshLoadingText(String text) {
        if (isLoading()) {
            Message msg = new Message();
            msg.obj = text;
            mHandler.sendMessage(msg);
        }
    }

    /**
     * 加载对话框是否存
     *
     * @return 存在就返回true，不存在则返回发false
     */
    public boolean isLoading() {
        return progressDialog != null && progressDialog.isShowing();
    }

    /**
     * 关闭加载对话
     */
    public void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}
