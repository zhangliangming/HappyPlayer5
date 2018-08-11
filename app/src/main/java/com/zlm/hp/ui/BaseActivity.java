package com.zlm.hp.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.libs.utils.ColorUtil;
import com.zlm.hp.libs.utils.LoggerUtil;
import com.zlm.hp.manager.ActivityManage;
import com.zlm.hp.permissions.StoragePermissionUtil;

import java.lang.reflect.Field;

/**
 * @Description: 所有activity都要继承该类并实现里面的方法
 * @Author: zhangliangming
 * @Date: 2017/7/15 15:44
 * @Version:
 */
public abstract class BaseActivity extends AppCompatActivity {

    public LoggerUtil logger;

    /**
     *
     */
    public HPApplication mHPApplication;

    /**
     * 默认颜色
     */
    private int mStatusColor = -1;

    public StoragePermissionUtil mStoragePermissionUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        getSupportActionBar().hide();
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
        }
        preLoad();
        setContentView(layout);
        //
        contentViewFinish(layout);
        mHPApplication = (HPApplication) getApplication();
        mStoragePermissionUtil = new StoragePermissionUtil(mHPApplication, this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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

        initViews(savedInstanceState);

        //1.先判断文件权限是否已经分配，如果没有分配，则直接退出应用
        //2.如果已经分配，则执行下面操作
        if (!mStoragePermissionUtil.verifyStoragePermissions(this)) {
            return;
        }

        logger = LoggerUtil.getZhangLogger(getApplicationContext());
        ActivityManage.getInstance().addActivity(this);

        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {

                loadData(false);

                return null;
            }
        }.execute("");
    }

    protected void preLoad() {

    }

    protected void contentViewFinish(View contentView) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        loadData(true);
    }

    //用户处理权限反馈，在这里判断用户是否授予相应的权限
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mStoragePermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults, null);
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
            return ColorUtil.parserColor(ContextCompat.getColor(getApplicationContext(), R.color.defColor));
        }
        return mStatusColor;
    }

    ///////////////////////////////////////////////


    public void setStatusColor(int statusColor) {
        this.mStatusColor = statusColor;
    }


    @Override
    public void finish() {
        ActivityManage.getInstance().removeActivity(this);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        HPApplication.getRefWatcher().watch(this);
        super.onDestroy();
    }
}
