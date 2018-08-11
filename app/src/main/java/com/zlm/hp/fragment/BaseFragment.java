package com.zlm.hp.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.libs.utils.ColorUtil;
import com.zlm.hp.libs.utils.LoggerUtil;
import com.zlm.hp.ui.R;
import com.zlm.hp.widget.IconfontTextView;

/**
 * Created by zhangliangming on 2017/7/23.
 */
public abstract class BaseFragment extends Fragment {
    /**
     *
     */
    public HPApplication mHPApplication;
    public Activity mActivity;

    /**
     * 内容布局
     */
    private ViewStub mContentContainer;

    //////////////////////////////////////////////////////////////////////

    /**
     * 加载中布局
     */
    private ViewStub mLoadingContainer;
    /**
     * 加载图标
     */
    private IconfontTextView mLoadImgView;

    /**
     * 旋转动画
     */
    private Animation rotateAnimation;

    //////////////////////////////////////////////////////////////////////
    /**
     * 无网络
     */
    private ViewStub mNetContainer;

    /**
     *
     */
    private RelativeLayout mNetBGLayout;


    /////////////////////////////////////////////////////////////////////

    public LoggerUtil logger;
    private ViewGroup mainView;
    private final int SHOWLOADINGVIEW = 0;
    private final int SHOWCONTENTVIEW = 1;
    private final int SHOWNONETVIEW = 2;

    private Handler mShowViewHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWCONTENTVIEW:
                    showContentViewHandler();
                    break;
                case SHOWLOADINGVIEW:
                    showLoadingViewHandler();
                    break;
                case SHOWNONETVIEW:
                    showNoNetViewHandler();
                    break;
            }
        }
    };

    private RefreshListener mRefreshListener;

    public BaseFragment() {
        if (getArguments() == null && !isVisible()) {
            setArguments(new Bundle());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        LayoutInflater inflater = mActivity.getLayoutInflater();
        mainView = (ViewGroup) inflater.inflate(R.layout.layout_fragment_base, null, false);
        ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isAddStatusBar()) {
                View statusBarView = new View(mActivity.getApplicationContext());

                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(mActivity.getApplicationContext()));
                statusBarView.setBackgroundColor(getStatusColor());
                mainView.addView(statusBarView, 0, lp);
            }
        }
        //

        if (setTitleViewId() != 0) {
            View titleView = inflater.inflate(setTitleViewId(), null, false);
            int titleHeight = (int) mActivity.getResources().getDimension(R.dimen.title_height);
            ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, titleHeight);

            mainView.addView(titleView, 1, tlp);
        }
        //
        mContentContainer = mainView.findViewById(R.id.viewstub_content_container);
        mContentContainer.setLayoutResource(setContentViewId());
        mContentContainer.inflate();
        //
        mHPApplication = (HPApplication) mActivity.getApplication();
        logger = LoggerUtil.getZhangLogger(mActivity.getApplicationContext());

        //
        initViews(savedInstanceState, mainView);
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {

                loadData(false);

                return null;
            }
        }.execute("");
    }

    /**
     * 加载数据
     */
    protected abstract void loadData(boolean isRestoreInstance);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) mainView.getParent();
        if (viewGroup != null) {
            viewGroup.removeAllViewsInLayout();
        }
        return mainView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (Activity) context;
    }

    /**
     * 初始化加载界面
     */
    private void initLoadingView() {
        mLoadingContainer = mainView.findViewById(R.id.viewstub_loading_container);
        mLoadingContainer.inflate();
        mLoadImgView = mainView.findViewById(R.id.load_img);
        rotateAnimation = AnimationUtils.loadAnimation(getContext(),
                R.anim.anim_rotate);
        rotateAnimation.setInterpolator(new LinearInterpolator());// 匀速
        mLoadImgView.startAnimation(rotateAnimation);

    }

    /**
     * 初始化没网络界面
     */
    private void initNoNetView() {
        //
        mNetContainer = mainView.findViewById(R.id.viewstub_net_container);
        mNetContainer.inflate();
        mNetBGLayout = mainView.findViewById(R.id.net_layout);
        mNetBGLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRefreshListener != null) {
                    mRefreshListener.refresh();
                }
            }
        });
    }

    /**
     * 显示加载窗口
     */
    public void showLoadingView() {
        if (mLoadingContainer == null) {
            initLoadingView();
        }
        mShowViewHandler.sendEmptyMessage(SHOWLOADINGVIEW);
    }

    /**
     * 显示加载窗口
     */
    private void showLoadingViewHandler() {
        if (mNetContainer != null)
            mNetContainer.setVisibility(View.GONE);
        mContentContainer.setVisibility(View.GONE);
        if (mLoadingContainer != null) {
            mLoadingContainer.setVisibility(View.VISIBLE);
            mLoadImgView.clearAnimation();
            mLoadImgView.startAnimation(rotateAnimation);
        }
    }

    /**
     * 显示主界面
     */
    public void showContentView() {
        mShowViewHandler.sendEmptyMessage(SHOWCONTENTVIEW);
    }

    /**
     * 显示主界面
     */
    private void showContentViewHandler() {
        mContentContainer.setVisibility(View.VISIBLE);
        if (mLoadingContainer != null) {
            mLoadingContainer.setVisibility(View.GONE);
            mLoadImgView.clearAnimation();
        }
        if (mNetContainer != null)
            mNetContainer.setVisibility(View.GONE);
    }

    /**
     * 显示无网络界面
     */
    public void showNoNetView() {
        if (mNetContainer == null) {
            initNoNetView();
        }
        mShowViewHandler.sendEmptyMessage(SHOWNONETVIEW);
    }

    /**
     * 显示无网络界面
     */
    private void showNoNetViewHandler() {
        mContentContainer.setVisibility(View.GONE);
        if (mLoadingContainer != null) {
            mLoadingContainer.setVisibility(View.GONE);
            mLoadImgView.clearAnimation();
        }
        if (mNetContainer != null)
            mNetContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        HPApplication.getRefWatcher().watch(this);
        super.onDestroy();
    }

    /**
     * 设置内容视图
     *
     * @return
     */
    protected abstract int setContentViewId();

    /**
     * 标题视图
     *
     * @return
     */
    protected abstract int setTitleViewId();

    /**
     * 初始化view视图
     *
     * @param savedInstanceState
     */
    protected abstract void initViews(Bundle savedInstanceState, View mainView);

    protected abstract boolean isAddStatusBar();

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

        return ColorUtil.parserColor(ContextCompat.getColor(mActivity.getApplicationContext(), R.color.defColor));

    }

    ///////////////////////////

    public interface RefreshListener {
        void refresh();
    }

    public void setRefreshListener(RefreshListener mRefreshListener) {
        this.mRefreshListener = mRefreshListener;
    }
}
