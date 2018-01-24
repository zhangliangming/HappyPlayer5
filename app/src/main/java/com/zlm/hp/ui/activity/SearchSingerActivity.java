package com.zlm.hp.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.R;
import com.zlm.hp.adapter.SearchSingerAdapter;
import com.zlm.hp.application.HPApplication;
import com.zlm.hp.db.SongSingerDB;
import com.zlm.hp.media.net.api.SearchArtistPicUtil;
import com.zlm.hp.media.net.entity.SearchArtistPicResult;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.SongSingerInfo;
import com.zlm.hp.receiver.AudioBroadcastReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import base.utils.ThreadUtil;
import base.utils.ToastUtil;
import base.widget.ButtonRelativeLayout;
import base.widget.IconfontTextView;
import base.widget.SearchEditText;
import base.widget.SwipeBackLayout;

/**
 * 搜索歌手写真
 */
public class SearchSingerActivity extends BaseActivity {
    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;

    /**
     * 搜索框
     */
    private SearchEditText mSearchEditText;

    /**
     * 清空
     */
    private IconfontTextView mCleanIconfontTextView;
    private final int LOADDATA = 0;
    private final int INITDATA = 1;
    private final int SHOWLOADINGVIEW = 2;
    private final int SHOWCONTENTVIEW = 3;
    private final int INITSELECTED = 4;

    //
    /**
     * 加载中布局
     */
    private RelativeLayout mLoadingContainer;
    /**
     * 内容布局
     */
    private RelativeLayout mContentContainer;
    /**
     *
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOADDATA:
                    loadDataUtil(500);
                    break;
                case INITDATA:
                    initData();
                    break;
                case SHOWCONTENTVIEW:
                    showContentViewHandler();
                    break;
                case SHOWLOADINGVIEW:
                    showLoadingViewHandler();
                    break;
                case INITSELECTED:

                    int sumSize = mDatas.size();
                    int curSize = mSelectDatas.size();
                    mIndextipTv.setText(curSize + "/" + sumSize);
                    if (sumSize == curSize) {
                        mAllSelectTv.setVisibility(View.INVISIBLE);
                        mCancelTv.setVisibility(View.VISIBLE);
                    } else {
                        mAllSelectTv.setVisibility(View.VISIBLE);
                        mCancelTv.setVisibility(View.INVISIBLE);
                    }

                    break;
            }
        }
    };

    private RecyclerView mRecyclerView;
    private SearchSingerAdapter mAdapter;
    private List<SearchArtistPicResult> mDatas;
    private AudioInfo mAudioInfo;

    /**
     * 屏幕宽度
     */
    private int mScreensWidth;
    private int mScreensHeight;

    //
    private SearchSingerListener mSearchSingerListener;
    private Map<String, String> mSelectDatas;
    /**
     * 确定按钮
     */
    private ButtonRelativeLayout mSureBtn;
    /**
     * 索引
     */
    private TextView mIndextipTv;
    /**
     * 取消全选按钮
     */
    private TextView mCancelTv;

    /**
     * 全选按钮
     */
    private TextView mAllSelectTv;

    private String mCurSingerName;
    private Runnable runnable;

    @Override
    protected int setContentViewId() {
        return R.layout.activity_search_singer;
    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }

    @Override
    public int setStatusBarParentView() {
        return R.id.singer_layout;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        //
        mSwipeBackLayout = findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setmSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {
            @Override
            public void finishView() {

                //关闭输入法
                InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        finish();
                        overridePendingTransition(0, 0);
                    }
                }.start();
            }
        });

        //返回按钮
        RelativeLayout backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.finish();
            }
        });

        //
        mCleanIconfontTextView = findViewById(R.id.clean_img);
        mCleanIconfontTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSearchEditText.setText("");
                mSearchEditText.requestFocus();


            }
        });
        //
        mCleanIconfontTextView.setVisibility(View.VISIBLE);
        //
        mSearchEditText = findViewById(R.id.search);
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //
                    String searchKey = mSearchEditText.getText().toString();
                    if (searchKey == null || searchKey.equals("")) {
                        ToastUtil.showTextToast(getApplicationContext(), mContext.getString(R.string.please_enter_singer_name));
                        return true;
                    }

                    //关闭输入法
                    InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

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

        //搜索
        TextView searchTextView = findViewById(R.id.right_flag);
        searchTextView.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                doSearch();
            }
        });
        //
        //
        mLoadingContainer = findViewById(R.id.loading);
        //
        mContentContainer = findViewById(R.id.content);

        //
        mRecyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplication(), 2);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // 设置布局管理器
        mRecyclerView.setLayoutManager(gridLayoutManager);

        //
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mScreensWidth = display.getWidth();
        mScreensHeight = display.getHeight();
        //
        mSearchSingerListener = new SearchSingerListener() {

            @Override
            public void onClick(String imgUrl) {
                if (mSelectDatas.containsKey(imgUrl.hashCode() + "")) {
                    mSelectDatas.remove(imgUrl.hashCode() + "");
                } else {
                    mSelectDatas.put(imgUrl.hashCode() + "", imgUrl);
                }
            }
        };

        //确定按钮
        mSureBtn = findViewById(R.id.surebtn);
        mSureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //获取数据库数据
                Map<String, String> oldData = SongSingerDB.getSongSingerDB(getApplicationContext()).getAllImgUrlBySingerName(mCurSingerName);
                for (Map.Entry<String, String> entry : oldData.entrySet()) {
                    if (!mSelectDatas.containsKey(entry.getKey())) {
                        //删除数据
                        SongSingerDB.getSongSingerDB(getApplicationContext()).deleteFromSI(mCurSingerName, entry.getValue());
                    }
                }

                //保存勾选的图片
                for (Map.Entry<String, String> entry : mSelectDatas.entrySet()) {
                    if (oldData.containsKey(entry.getKey())) {
                        continue;
                    }
                    SongSingerInfo songSingerInfo = new SongSingerInfo();
                    songSingerInfo.setHash(mAudioInfo.getHash());
                    songSingerInfo.setImgUrl(entry.getValue());
                    songSingerInfo.setSingerName(mCurSingerName);

                    SongSingerDB.getSongSingerDB(getApplicationContext()).add(songSingerInfo);
                }

                mHandler.sendEmptyMessage(INITSELECTED);

                //发送重新选择写真图片广播
                Intent reloadIntent = new Intent(AudioBroadcastReceiver.ACTION_RELOADSINGERIMG);
                reloadIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                reloadIntent.putExtra("hash", mAudioInfo.getHash());
                reloadIntent.putExtra("singerName", mAudioInfo.getSingerName());
                sendBroadcast(reloadIntent);

                mSwipeBackLayout.finish();
            }
        });
        //索引
        mIndextipTv = findViewById(R.id.indextip);
        //全选按钮
        mAllSelectTv = findViewById(R.id.allselect);
        mAllSelectTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < mDatas.size(); i++) {
                    SearchArtistPicResult temp = mDatas.get(i);
                    if (!mSelectDatas.containsKey(temp.getImgUrl().hashCode() + "")) {
                        mSelectDatas.put(temp.getImgUrl().hashCode() + "", temp.getImgUrl());
                    }
                }
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                mHandler.sendEmptyMessage(INITSELECTED);
            }
        });
        //取消全选
        mCancelTv = findViewById(R.id.cancel);
        mCancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectDatas.clear();
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                mHandler.sendEmptyMessage(INITSELECTED);
            }
        });
    }

    /**
     * 显示加载窗口
     */
    public void showLoadingView() {
        mHandler.sendEmptyMessage(SHOWLOADINGVIEW);
    }

    /**
     * 显示加载窗口
     */
    private void showLoadingViewHandler() {
        mContentContainer.setVisibility(View.GONE);
        mLoadingContainer.setVisibility(View.VISIBLE);
    }

    /**
     * 显示主界面
     */
    public void showContentView() {
        mHandler.sendEmptyMessage(SHOWCONTENTVIEW);
    }

    /**
     * 显示主界面
     */
    private void showContentViewHandler() {
        mContentContainer.setVisibility(View.VISIBLE);
        mLoadingContainer.setVisibility(View.GONE);
    }


    /**
     * 初始化数据
     */
    private void initData() {
        AudioInfo curAudioInfo = HPApplication.getInstance().getCurAudioInfo();
        if (curAudioInfo != null) {
            mCurSingerName = getIntent().getStringExtra("singerName");
            this.mAudioInfo = curAudioInfo;
            mSearchEditText.setText(mCurSingerName);
            loadDataUtil(500);
        }
    }


    /**
     * 搜索
     */
    private void doSearch() {

        String singerName = mSearchEditText.getText().toString();
        if (singerName.equals("")) {
            ToastUtil.showTextToast(getApplicationContext(), mContext.getString(R.string.please_enter_singer_name));
            return;
        }

        loadDataUtil(500);
    }

    /**
     * @param sleepTime
     */
    private void loadDataUtil(int sleepTime) {
        showLoadingView();

        runnable = new Runnable() {
            @Override
            public void run() {
                String singerName = mSearchEditText.getText().toString();
                mSelectDatas = SongSingerDB.getSongSingerDB(getApplicationContext()).getAllImgUrlBySingerName(mCurSingerName);
                mDatas = SearchArtistPicUtil.searchArtistPic(mContext, singerName, mScreensWidth + "", mScreensHeight + "", "app");

                if (mSelectDatas == null) {
                    mSelectDatas = new HashMap<>();
                }

                if (mDatas == null) {
                    mDatas = new ArrayList<SearchArtistPicResult>();
                }
                //
                mAdapter = new SearchSingerAdapter(mContext, mDatas, mAudioInfo, mSelectDatas, mSearchSingerListener);
                mRecyclerView.setAdapter(mAdapter);
                mHandler.sendEmptyMessage(INITSELECTED);

                showContentView();
            }
        };
        ThreadUtil.runInThread(runnable);
    }

    @Override
    protected void onDestroy() {
        if(runnable != null) {
            ThreadUtil.cancelThread(runnable);
        }
        super.onDestroy();
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        mHandler.sendEmptyMessage(INITDATA);
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.finish();
    }


    //////////////////////////////////////////////////////////
    public interface SearchSingerListener {
        void onClick(String imgUrl);
    }

}
