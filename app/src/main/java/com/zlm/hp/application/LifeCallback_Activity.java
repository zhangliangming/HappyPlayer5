package com.zlm.hp.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import base.utils.LoggerUtil;

/**
 * 717219917@qq.com      2018/1/23  10:16
 */
public class LifeCallback_Activity implements Application.ActivityLifecycleCallbacks{
    @Override  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        LoggerUtil.getZhangLogger(activity).e("生命周期回调activity名："+activity.getClass().getSimpleName());
        HPApplication.getInstance().list.add(activity);
    }
    @Override public void onActivityStarted(Activity activity) {  }
    @Override public void onActivityResumed(Activity activity) {  }
    @Override  public void onActivityPaused(Activity activity) {  }
    @Override public void onActivityStopped(Activity activity) { }
    @Override  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {  }
    @Override public void onActivityDestroyed(Activity activity) {

    }

}
