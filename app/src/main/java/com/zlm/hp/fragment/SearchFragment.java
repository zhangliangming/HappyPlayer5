package com.zlm.hp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.zlm.hp.adapter.RankSongAdapter;
import com.zlm.hp.adapter.SearchResultAdapter;
import com.zlm.hp.libs.utils.ToastUtil;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.net.api.SearchResultHttpUtil;
import com.zlm.hp.net.model.HttpResult;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.utils.AsyncTaskHttpUtil;
import com.zlm.hp.widget.IconfontTextView;
import com.zlm.hp.widget.SearchEditText;

import java.util.ArrayList;
import java.util.Map;

/**
 * 搜索
 * Created by zhangliangming on 2017/7/23.
 */
public class SearchFragment extends BaseFragment {

    /**
     * 搜索框
     */
    private SearchEditText mSearchEditText;


    /**
     * 列表视图
     */
    private RecyclerView mRecyclerView;
    private ArrayList<AudioInfo> mDatas;
    private SearchResultAdapter mAdapter;

    private final int LOADDATA = 0;
    private final int INIT = 1;
    /**
     *
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOADDATA:
                    loadDataUtil(300, true);
                    break;
                case INIT:
                    mSearchEditText.requestFocus();
                    //打开软键盘
                    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    break;
            }
        }
    };

    /**
     * 页码
     */
    private int mPage = 1;
    /**
     * 每页显示条数
     */
    private int mPageSize = 30;

    /**
     * http请求
     */
    private AsyncTaskHttpUtil mAsyncTaskHttpUtil;
    /**
     * 清空
     */
    private IconfontTextView mCleanIconfontTextView;

    private AudioBroadcastReceiver mAudioBroadcastReceiver;

    /**
     * 广播监听
     */
    private AudioBroadcastReceiver.AudioReceiverListener mAudioReceiverListener = new AudioBroadcastReceiver.AudioReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doAudioReceive(context, intent);
        }
    };

    public SearchFragment() {

    }

    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_search;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {
        //
        mCleanIconfontTextView = mainView.findViewById(R.id.clean_img);
        mCleanIconfontTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSearchEditText.setText("");
                mSearchEditText.requestFocus();

                //清空data的数据
                if (mDatas != null && mDatas.size() > 0) {
                    mAdapter.setState(RankSongAdapter.NODATA);
                    mDatas.clear();
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        //
        mCleanIconfontTextView.setVisibility(View.VISIBLE);
        //
        mSearchEditText = mainView.findViewById(R.id.search);
        mSearchEditText.setText("初恋情人");
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //
                    String searchKey = mSearchEditText.getText().toString();
                    if (searchKey == null || searchKey.equals("")) {
                        ToastUtil.showTextToast(mActivity.getApplicationContext(), "请输入关键字");
                        return true;
                    }

                    //关闭输入法
                    InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(mActivity.getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    doSearch();
                    //
                    return false;
                }
                return false;
            }
        });
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchKey = mSearchEditText.getText().toString();
                if (searchKey == null || searchKey.equals("")) {
                    if (mCleanIconfontTextView.getVisibility() != View.INVISIBLE) {
                        mCleanIconfontTextView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mCleanIconfontTextView.getVisibility() != View.VISIBLE) {
                        mCleanIconfontTextView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        //取消
        TextView backTextView = mainView.findViewById(R.id.right_flag);
        backTextView.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {

                //关闭输入法
                InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(mActivity.getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //
                        Intent closeIntent = new Intent(FragmentReceiver.ACTION_CLOSEDFRAGMENT);
                        closeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        mActivity.sendBroadcast(closeIntent);

                    }
                }.start();
            }
        });

        //
        mRecyclerView = mainView.findViewById(R.id.search_recyclerView);
        //初始化内容视图
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity.getApplicationContext()));
        //
        mDatas = new ArrayList<AudioInfo>();
        mAdapter = new SearchResultAdapter(mHPApplication, mActivity.getApplicationContext(), mDatas);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setSearchResultListener(new SearchResultAdapter.SearchResultListener()

        {
            @Override
            public void loadMoreData() {
                loadMoreDataHandler();
            }
        });

        setRefreshListener(new RefreshListener() {
            @Override
            public void refresh() {
                showLoadingView();

                loadDataUtil(300, true);
            }
        });

        //


        showContentView();


        //注册监听
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(mActivity.getApplicationContext(), mHPApplication);
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(mActivity.getApplicationContext());
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        if (isRestoreInstance) {
            mPage = 1;
            mAdapter.setState(RankSongAdapter.NODATA);
            mDatas.clear();
            mAdapter.notifyDataSetChanged();

        }
        mAdapter.setPlayIndexHash("-1");
        mAdapter.setPlayIndexPosition(-1);
        mHandler.sendEmptyMessage(LOADDATA);

    }

    /**
     * 处理音频监听事件
     *
     * @param context
     * @param intent
     */
    private void doAudioReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(AudioBroadcastReceiver.ACTION_NULLMUSIC)) {
            mAdapter.reshViewHolder(null);
        } else if (action.equals(AudioBroadcastReceiver.ACTION_INITMUSIC)) {
            //初始化
            //AudioMessage audioMessage = (AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            AudioInfo audioInfo = mHPApplication.getCurAudioInfo(); //audioMessage.getAudioInfo();
            mAdapter.reshViewHolder(audioInfo);
        }

    }

    /**
     * 搜索
     */
    private void doSearch() {
        mPage = 1;

        if (mDatas != null && mDatas.size() > 0) {
            mAdapter.setState(RankSongAdapter.NODATA);
            mDatas.clear();
            mAdapter.notifyDataSetChanged();
        }
        mAdapter.setPlayIndexHash("-1");
        mAdapter.setPlayIndexPosition(-1);

        showLoadingView();
        loadDataUtil(300, true);
    }

    /**
     * 加载更多数据
     */
    private void loadMoreDataHandler() {
        loadDataUtil(0, false);
    }

    /**
     * 加载数据
     */
    private void loadDataUtil(int sleepTime, final boolean showView) {
        //
        if (mAsyncTaskHttpUtil != null && !mAsyncTaskHttpUtil.isCancelled()) {
            mAsyncTaskHttpUtil.cancel(true);
        }
        //
        //
        mAdapter.setState(RankSongAdapter.LOADING);
        mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
        //
        mAsyncTaskHttpUtil = new AsyncTaskHttpUtil();
        mAsyncTaskHttpUtil.setSleepTime(sleepTime);
        mAsyncTaskHttpUtil.setAsyncTaskListener(new AsyncTaskHttpUtil.AsyncTaskListener() {
            @Override
            public HttpResult doInBackground() {

                return SearchResultHttpUtil.search(mHPApplication, mActivity.getApplicationContext(), mSearchEditText.getText().toString(), mPage + "", mPageSize + "");

            }

            @Override
            public void onPostExecute(HttpResult httpResult) {
                if (httpResult.getStatus() == HttpResult.STATUS_NONET || httpResult.getStatus() == HttpResult.STATUS_NOWIFI) {
                    if (showView)
                        showNoNetView();
                    ToastUtil.showTextToast(mActivity.getApplicationContext(), httpResult.getErrorMsg());
                } else if (httpResult.getStatus() == HttpResult.STATUS_SUCCESS) {

                    //
                    Map<String, Object> returnResult = (Map<String, Object>) httpResult.getResult();

                    ArrayList<AudioInfo> datas = (ArrayList<AudioInfo>) returnResult.get("rows");

                    if (mPage == 1 && datas.size() == 0) {
                        mAdapter.setState(RankSongAdapter.NODATA);
                    } else {
                        //没有数据
                        if (datas.size() < mPageSize) {
                            mAdapter.setState(RankSongAdapter.NOMOREDATA);
                        } else {
                            mAdapter.setState(RankSongAdapter.HASMOREDATA);
                            mPage++;
                        }
                        for (int i = 0; i < datas.size(); i++) {
                            mDatas.add(datas.get(i));
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    if (showView)
                        showContentView();

                } else {
                    if (showView)
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
        mAudioBroadcastReceiver.unregisterReceiver(mActivity.getApplicationContext());
        super.onDestroy();
    }


    @Override
    protected int setTitleViewId() {
        return R.layout.layout_search_title;
    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }
}
