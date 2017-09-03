package com.zlm.hp.utils;

import android.os.AsyncTask;

import com.zlm.hp.net.model.HttpResult;

/**
 * Created by zhangliangming on 2017/8/1.
 */
public class AsyncTaskHttpUtil extends AsyncTask<String, Integer, HttpResult> {

    private AsyncTaskListener mAsyncTaskListener;

    private int sleepTime = 0;

    @Override
    protected void onPostExecute(HttpResult httpResult) {
        if (mAsyncTaskListener != null) {
            mAsyncTaskListener.onPostExecute(httpResult);
        }
    }

    @Override
    protected HttpResult doInBackground(String... strings) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mAsyncTaskListener != null) {
            return mAsyncTaskListener.doInBackground();
        }
        return null;


    }

    public interface AsyncTaskListener {
        HttpResult doInBackground();

        void onPostExecute(HttpResult httpResult);
    }

    public void setAsyncTaskListener(AsyncTaskListener mAsyncTaskListener) {
        this.mAsyncTaskListener = mAsyncTaskListener;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }
}
