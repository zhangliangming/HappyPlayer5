package com.zlm.hp.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.R;

import base.utils.ColorUtil;
import base.utils.LoggerUtil;

/**
 * Created by zhangliangming on 2017/7/23.
 */
public abstract class BaseFragment extends StatedFragment {

    public Activity mActivity;
    public Context mContext;

    /**
     * 内容布局
     */
    private LinearLayout mContentContainer;

    //////////////////////////////////////////////////////////////////////

    /**
     * 加载中布局
     */
    private LinearLayout mLoadingContainer;

    /**
     * 旋转动画
     */
    private Animation rotateAnimation;

    //////////////////////////////////////////////////////////////////////
    /**
     * 无网络
     */
    private LinearLayout mNetContainer;

    /**
     *
     */
    private RelativeLayout mNetBGLayout;
    /**
     * 没有网络时的文字描述
     */
    private TextView mTvNonetMsg;


    /////////////////////////////////////////////////////////////////////

    public LoggerUtil logger;
    private ViewGroup mainView;
    private final int SHOWLOADINGVIEW = 0;
    private final int SHOWCONTENTVIEW = 1;
    private final int SHOWNONETView = 2;

    @SuppressLint("HandlerLeak")
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
                case SHOWNONETView:
                    @StringRes int resId = (int) msg.obj;
                    if(mTvNonetMsg != null) {
                        mTvNonetMsg.setText(resId);
                    }
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
        mContentContainer = mainView.findViewById(R.id.content_container);
        View contentView = inflater.inflate(setContentViewId(), null, false);
        //
        mLoadingContainer = mainView.findViewById(R.id.loading_container);
        View loadingView = inflater.inflate(R.layout.layout_fragment_loading, null, false);

        //
        mNetContainer = mainView.findViewById(R.id.net_container);
        View noNetView = inflater.inflate(R.layout.layout_fragment_nonet, null, false);
        mTvNonetMsg = noNetView.findViewById(R.id.nonet_msg);

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
        mNetContainer.addView(noNetView, vlp);
        mLoadingContainer.addView(loadingView, vlp);
        mContentContainer.addView(contentView, vlp);

        logger = LoggerUtil.getZhangLogger(mActivity.getApplicationContext());
        //初始化界面
        initView();
        initViews(savedInstanceState, mainView);
        loadData(false);
    }


    /**
     * Save Fragment's State here
     */
    @Override
    protected void onSaveState(Bundle outState) {
        super.onSaveState(outState);
        // For example:
        //outState.putString("text", tvSample.getText().toString());
    }

    /**
     * Restore Fragment's State here
     */
    @Override
    protected void onRestoreState(Bundle savedInstanceState) {
        super.onRestoreState(savedInstanceState);
        // For example:
        //tvSample.setText(savedInstanceState.getString("text"));
        loadData(true);
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
        this.mContext = context;
        this.mActivity = (Activity) context;
    }

    /**
     * 初始界面
     */
    private void initView() {
        //
        mNetBGLayout = mNetContainer.findViewById(R.id.net_layout);
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
        mShowViewHandler.sendEmptyMessage(SHOWLOADINGVIEW);
    }

    /**
     * 显示加载窗口
     */
    private void showLoadingViewHandler() {

        mNetContainer.setVisibility(View.GONE);
        mContentContainer.setVisibility(View.GONE);
        mLoadingContainer.setVisibility(View.VISIBLE);

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
        mLoadingContainer.setVisibility(View.GONE);
        mNetContainer.setVisibility(View.GONE);
    }

    /**
     * 显示无网络界面
     */
    public void showNoNetView(@StringRes int resId) {
        Message obtain = Message.obtain();
        obtain.what = SHOWNONETView;
        obtain.obj = resId;
        mShowViewHandler.sendMessage(obtain);
    }

    /**
     * 显示无网络界面
     */
    private void showNoNetViewHandler() {
        mContentContainer.setVisibility(View.GONE);
        mLoadingContainer.setVisibility(View.GONE);
        mNetContainer.setVisibility(View.VISIBLE);
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

        return ColorUtil.parserColor(ContextCompat.getColor(mActivity.getApplicationContext(), R.color.colorPrimary));

    }

    ///////////////////////////

    public interface RefreshListener {
        void refresh();
    }

    public void setRefreshListener(RefreshListener mRefreshListener) {
        this.mRefreshListener = mRefreshListener;
    }
}
