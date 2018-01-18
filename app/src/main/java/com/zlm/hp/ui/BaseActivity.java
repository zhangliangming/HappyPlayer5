package com.zlm.hp.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.zlm.hp.R;
import com.zlm.hp.application.HPApplication;

import java.lang.reflect.Field;

import base.utils.ColorUtil;
import base.utils.LoggerUtil;

/**
 * @Description: 所有activity都要继承该类并实现里面的方法
 * @Author: zhangliangming
 * @Date: 2017/7/15 15:44
 * @Version:
 */
public abstract class BaseActivity extends AppCompatActivity {

    public LoggerUtil logger;
    public Handler mHandler = new Handler(Looper.getMainLooper());
    public BaseActivity mActivity;
    public Context mContext;

    /**
     *
     */
    public HPApplication mHPApplication;

    /**
     * 默认颜色
     */
    private int mStatusColor = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        ViewGroup layout = (ViewGroup) LayoutInflater.from(this).inflate(setContentViewId(), null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //透明状态栏
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //设置状态栏的颜色
            window.setStatusBarColor(Color.TRANSPARENT);
            //设置底部的颜色
            // window.setNavigationBarColor(Color.TRANSPARENT);

            //修复android7.0半透明问题
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    Field field = getWindow().getDecorView().getClass().getDeclaredField("mSemiTransparentStatusBarColor");  //获取特定的成员变量
                    field.setAccessible(true);   //设置对此属性的可访问性
                    field.setInt(getWindow().getDecorView(), Color.TRANSPARENT);  //修改属性值

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //
            if (isAddStatusBar()) {
                View statusBarView = new View(getApplicationContext());
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(getApplicationContext()));
                statusBarView.setBackgroundColor(getStatusColor());


                if (setStatusBarParentView() == 0) {
                    layout.addView(statusBarView, 0, lp);
                } else {
                    ViewGroup newlayout = layout.findViewById(setStatusBarParentView());
                    newlayout.addView(statusBarView, 0, lp);
                }
            }

        }

        logger = LoggerUtil.getZhangLogger(getApplicationContext());
        mHPApplication = (HPApplication) getApplication();
        preLoad();
        setContentView(layout);
        mActivity = this;
        mContext = this;
        initViews(savedInstanceState);
        loadData(false);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        loadData(true);
    }

    protected void preLoad() {

    }

    /**
     * 设置内容视图
     *
     * @return
     */
    protected abstract int setContentViewId();

    /**
     * 初始化view视图
     *
     * @param savedInstanceState
     */
    protected abstract void initViews(Bundle savedInstanceState);

    /**
     * 加载数据
     */
    protected abstract void loadData(boolean isRestoreInstance);

    protected abstract boolean isAddStatusBar();

    /**
     * 设置添加状态栏的视图
     *
     * @return
     */
    public abstract int setStatusBarParentView();

    /**
     * @Description: 获取状态栏高度
     * @Param: context
     * @Return:
     * @Author: zhangliangming
     * @Date: 2017/7/15 19:30
     * @Throws:
     */
    private int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取状态栏颜色
     *
     * @return
     */
    private int getStatusColor() {

        if (mStatusColor == -1) {
            return ColorUtil.parserColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }
        return mStatusColor;
    }

    ///////////////////////////////////////////////


    public void setStatusColor(int statusColor) {
        this.mStatusColor = statusColor;
    }


}
