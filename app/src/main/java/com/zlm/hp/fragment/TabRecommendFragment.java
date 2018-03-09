package com.zlm.hp.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zlm.hp.adapter.RecommendAdapter;
import com.zlm.hp.libs.utils.ToastUtil;
import com.zlm.hp.net.api.RankListHttpUtil;
import com.zlm.hp.net.entity.RankListResult;
import com.zlm.hp.net.model.HttpResult;
import com.zlm.hp.ui.R;
import com.zlm.hp.utils.AsyncTaskHttpUtil;

import java.util.ArrayList;
import java.util.Map;

/**
 * @Description: tab推荐界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/16 20:42
 * @Throws:
 */
public class TabRecommendFragment extends BaseFragment {

    /**
     * 列表视图
     */
    private RecyclerView mRecyclerView;

    /**
     * 是否已加载数据
     */
    private boolean isLoadData = false;
    //
    private RecommendAdapter mAdapter;
    private ArrayList<RankListResult> mDatas;
    /**
     * http请求
     */
    private AsyncTaskHttpUtil mAsyncTaskHttpUtil;

    public TabRecommendFragment() {

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_recommend;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {

        //
        mRecyclerView = mainView.findViewById(R.id.recyclerView);
        //初始化内容视图
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity.getApplicationContext()));

        //
        mDatas = new ArrayList<RankListResult>();
        mAdapter = new RecommendAdapter(mHPApplication, mActivity.getApplicationContext(), mDatas);
        mRecyclerView.setAdapter(mAdapter);
        //
        showLoadingView();

        setRefreshListener(new RefreshListener() {
            @Override
            public void refresh() {
                showLoadingView();

                loadDataUtil(300);
            }
        });
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        if (isLoadData) {
            if (isRestoreInstance) {
                mDatas.clear();
            }
            loadDataUtil(300);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        if (isVisibleToUser && !isLoadData) {
            isLoadData = true;
            loadDataUtil(0);
        }
    }

    /**
     * 加载数据
     */
    private void loadDataUtil(final int sleepTime) {
        mAsyncTaskHttpUtil = new AsyncTaskHttpUtil();
        mAsyncTaskHttpUtil.setSleepTime(sleepTime);
        mAsyncTaskHttpUtil.setAsyncTaskListener(new AsyncTaskHttpUtil.AsyncTaskListener() {
            @Override
            public HttpResult doInBackground() {
                return RankListHttpUtil.rankList(mHPApplication, mActivity.getApplicationContext());
            }

            @Override
            public void onPostExecute(HttpResult httpResult) {
                if (httpResult.getStatus() == HttpResult.STATUS_NONET) {
                    showNoNetView();
                } else if (httpResult.getStatus() == HttpResult.STATUS_SUCCESS) {

                    //
                    Map<String, Object> returnResult = (Map<String, Object>) httpResult.getResult();

                    ArrayList<RankListResult> datas = (ArrayList<RankListResult>) returnResult.get("rows");
                    if (datas.size() == 0) {
                        mAdapter.setState(RecommendAdapter.NODATA);
                    } else {
                        for (int i = datas.size() - 1; i >= 0; i--) {
                            mDatas.add(0, datas.get(i));
                        }
                        mAdapter.setState(RecommendAdapter.NOMOREDATA);
                    }

                    mAdapter.notifyDataSetChanged();

                    showContentView();

                } else {
                    showContentView();
                    ToastUtil.showTextToast(mActivity.getApplicationContext(), httpResult.getErrorMsg());
                }
            }
        });
        mAsyncTaskHttpUtil.execute("");
    }

    @Override
    public void onDestroy() {
        if (mAsyncTaskHttpUtil != null)
            mAsyncTaskHttpUtil.cancel(true);
        super.onDestroy();
    }


    @Override
    protected int setTitleViewId() {
        return 0;
    }

    @Override
    protected boolean isAddStatusBar() {
        return false;
    }
}