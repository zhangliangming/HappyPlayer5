package com.zlm.hp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.PageTransformer.ZoomOutPageTransformer;
import com.zlm.hp.adapter.TabFragmentAdapter;
import com.zlm.hp.fragment.LrcFragment;
import com.zlm.hp.libs.utils.ToastUtil;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.net.api.DownloadLyricsUtil;
import com.zlm.hp.net.api.SearchLyricsUtil;
import com.zlm.hp.net.entity.DownloadLyricsResult;
import com.zlm.hp.net.entity.SearchLyricsResult;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.utils.AsyncTaskUtil;
import com.zlm.hp.widget.IconfontTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 歌词搜索窗口
 */
public class SearchLrcActivity extends BaseActivity {
    /**
     * 歌曲名称
     */
    private EditText mSongNameEditText;
    private IconfontTextView mSongNameCleanImg;

    /**
     * 歌手名称
     */
    private EditText mSingerNameEditText;
    private IconfontTextView mSingerNameCleanImg;
    private TextView mSearchBtn;
    //
    private String mDuration = "";
    private String mHash = "";
    private AudioInfo mCurAudioInfo;
    //
    private final int LOADDATA = 0;
    private final int INITDATA = 1;
    private final int SHOWLOADINGVIEW = 2;
    private final int SHOWCONTENTVIEW = 3;

    /**
     * http请求
     */
    private AsyncTaskUtil mAsyncTaskUtil;
    private List<DownloadLyricsResult> mDatas;
    private ArrayList<Fragment> mLrcViews;
    private ViewPager mViewPager;

    //
    private TextView mSumTv;
    private TextView mCurIndexTv;

    //
    /**
     * 加载中布局
     */
    private RelativeLayout mLoadingContainer;
    /**
     * 加载图标
     */
    private IconfontTextView mLoadImgView;

    /**
     * 旋转动画
     */
    private Animation rotateAnimation;

    /**
     * 内容布局
     */
    private RelativeLayout mContentContainer;


    /**
     *
     */
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
            }
        }
    };
    /**
     * 音频广播
     */
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

    private TabFragmentAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected int setContentViewId() {
        return R.layout.activity_search_lrc;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        TextView titleView = findViewById(R.id.title);
        titleView.setText("选择歌词");


        //关闭
        IconfontTextView backTextView = findViewById(R.id.closebtn);
        backTextView.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {

                //关闭输入法
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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


                    }
                }.start();
            }
        });
        //歌曲
        mSongNameEditText = findViewById(R.id.songNameEt);
        mSongNameCleanImg = findViewById(R.id.songclean_img);
        mSongNameCleanImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSongNameEditText.setText("");
                mSongNameEditText.requestFocus();

            }
        });
        mSongNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    //关闭输入法
                    InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


                    doSearch();
                }
                return false;
            }
        });
        mSongNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchKey = mSongNameEditText.getText().toString();
                if (searchKey == null || searchKey.equals("")) {
                    if (mSongNameCleanImg.getVisibility() != View.INVISIBLE) {
                        mSongNameCleanImg.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mSongNameCleanImg.getVisibility() != View.VISIBLE) {
                        mSongNameCleanImg.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        //歌手
        mSingerNameEditText = findViewById(R.id.singerNameEt);
        mSingerNameCleanImg = findViewById(R.id.singclean_img);
        mSingerNameCleanImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSingerNameEditText.setText("");
                mSingerNameEditText.requestFocus();

            }
        });
        mSingerNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    doSearch();
                }
                return false;
            }
        });
        mSingerNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchKey = mSingerNameEditText.getText().toString();
                if (searchKey == null || searchKey.equals("")) {
                    if (mSingerNameCleanImg.getVisibility() != View.INVISIBLE) {
                        mSingerNameCleanImg.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mSingerNameCleanImg.getVisibility() != View.VISIBLE) {
                        mSingerNameCleanImg.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        //搜索按钮
        mSearchBtn = findViewById(R.id.searchbtn);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSearch();
            }
        });

        //
        mViewPager = findViewById(R.id.viewpage);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                mCurIndexTv.setText((position + 1) + "");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //
        mSumTv = findViewById(R.id.sum);
        mSumTv.setText("0");
        mCurIndexTv = findViewById(R.id.cur_index);
        mCurIndexTv.setText("0");
        //
        mLoadingContainer = findViewById(R.id.loading);
        mLoadImgView = findViewById(R.id.load_img);
        rotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.anim_rotate);
        rotateAnimation.setInterpolator(new LinearInterpolator());// 匀速
        mLoadImgView.startAnimation(rotateAnimation);
        //
        mContentContainer = findViewById(R.id.content);

        //注册接收音频播放广播
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(getApplicationContext(), mHPApplication);
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(getApplicationContext());


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
        mLoadImgView.clearAnimation();
        mLoadImgView.startAnimation(rotateAnimation);

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
        mLoadImgView.clearAnimation();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mCurAudioInfo = mHPApplication.getCurAudioInfo();
        if (mCurAudioInfo != null) {
            mSongNameEditText.setText(mCurAudioInfo.getSongName());
            mSingerNameEditText.setText(mCurAudioInfo.getSingerName());
            mDuration = mCurAudioInfo.getDuration() + "";
            mHash = mCurAudioInfo.getHash();

            loadDataUtil(500);
        }

    }


    @Override
    protected void loadData(boolean isRestoreInstance) {
        mHandler.sendEmptyMessage(INITDATA);
    }

    /**
     * 搜索歌词
     */
    private void doSearch() {

        //关闭输入法
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


        String songName = mSongNameEditText.getText().toString();
        String singerName = mSingerNameEditText.getText().toString();
        if (songName.equals("") && singerName.equals("")) {
            ToastUtil.showTextToast(getApplicationContext(), "请输入关键字");
            return;
        }

        loadDataUtil(500);
    }

    /**
     * @param sleepTime
     */
    private void loadDataUtil(int sleepTime) {
        mSumTv.setText("0");
        mCurIndexTv.setText("0");
        showLoadingView();
        if (mDatas == null) {
            mLrcViews = new ArrayList<Fragment>();
            mDatas = new ArrayList<DownloadLyricsResult>();
        } else {

            if (mLrcViews.size() > 0) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                for (int i = 0; i < mLrcViews.size(); i++) {
                    transaction.remove(mLrcViews.get(i));
                }
                transaction.commit();
            }

            mLrcViews.clear();
            mDatas.clear();
        }

        //
        if (mAsyncTaskUtil != null && !mAsyncTaskUtil.isCancelled()) {
            mAsyncTaskUtil.cancel(true);
        }
        mAsyncTaskUtil = new AsyncTaskUtil();
        mAsyncTaskUtil.setSleepTime(sleepTime);
        mAsyncTaskUtil.setAsyncTaskListener(new AsyncTaskUtil.AsyncTaskListener() {
            @Override
            public void doInBackground() {

                String songName = mSongNameEditText.getText().toString();
                String singerName = mSingerNameEditText.getText().toString();
                //加载歌词
                String keyWords = "";
                if (singerName.equals("未知")) {
                    keyWords = songName;
                } else {
                    keyWords = singerName + " - " + songName;
                }

                //获取歌曲列表
                List<SearchLyricsResult> results = SearchLyricsUtil.searchLyrics(mHPApplication, getApplicationContext(), keyWords, mDuration, "");
                if (results != null && results.size() > 0)
                    for (int i = 0; i < results.size(); i++) {
                        SearchLyricsResult searchLyricsResult = results.get(i);
                        if (searchLyricsResult != null) {
                            DownloadLyricsResult downloadLyricsResult = DownloadLyricsUtil.downloadLyrics(mHPApplication, getApplicationContext(), searchLyricsResult.getId(), searchLyricsResult.getAccesskey(), "krc");
                            if (downloadLyricsResult != null) {
                                mDatas.add(downloadLyricsResult);
                            }
                        }
                    }
            }

            @Override
            public void onPostExecute() {
                //

                for (int i = 0; i < mDatas.size(); i++) {
                    DownloadLyricsResult downloadLyricsResult = mDatas.get(i);
                    LrcFragment lrcFragment = new LrcFragment(downloadLyricsResult, mCurAudioInfo);
                    mLrcViews.add(lrcFragment);
                }
                //
                if (mLrcViews.size() == 0) {
                    ToastUtil.showTextToast(getApplicationContext(), "无歌词");

                } else {
                    mCurIndexTv.setText("1");
                }
                //
                mSumTv.setText(mLrcViews.size() + "");
                adapter = new TabFragmentAdapter(getSupportFragmentManager(), mLrcViews);
                mViewPager.setAdapter(adapter);


                showContentView();
            }
        });
        mAsyncTaskUtil.execute("");

    }

    /**
     * 处理音频广播事件
     *
     * @param context
     * @param intent
     */

    private void doAudioReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(AudioBroadcastReceiver.ACTION_LRCUSE)) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        //
        if (mAsyncTaskUtil != null && !mAsyncTaskUtil.isCancelled()) {
            mAsyncTaskUtil.cancel(true);
        }
        //注销广播
        mAudioBroadcastReceiver.unregisterReceiver(getApplicationContext());

        super.onDestroy();
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.out_to_bottom);
    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }

    @Override
    public int setStatusBarParentView() {
        return 0;
    }

    @Override
    public void setStatusColor(int statusColor) {
        super.setStatusColor(statusColor);
    }
}
