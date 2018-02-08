package com.zlm.hp.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.R;
import com.zlm.hp.adapter.RecentOrLikeMusicAdapter;
import com.zlm.hp.application.HPApplication;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.FragmentReceiver;

import java.util.ArrayList;
import java.util.List;

import base.utils.ThreadUtil;

/**
 * 喜欢音乐
 * Created by zhangliangming on 2017/7/23.
 */
public class LikeMusicFragment extends BaseFragment {
    private ArrayList<AudioInfo> mDatas;

    /**
     * 列表视图
     */
    private RecyclerView mRecyclerView;
    //
    private RecentOrLikeMusicAdapter mAdapter;

    private static final int LOADDATA = 0;


    /**
     *
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOADDATA:
                    loadDataUtil(0);
                    break;
            }
        }
    };
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
    private Runnable runnable;


    public LikeMusicFragment() {

    }


    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_like;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {
        TextView titleView = mainView.findViewById(R.id.title);
        titleView.setText(R.string.like);

        //返回
        RelativeLayout backImg = mainView.findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                Intent closeIntent = new Intent(FragmentReceiver.ACTION_CLOSEDFRAGMENT);
                closeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(closeIntent);

            }
        });
        //
        mRecyclerView = mainView.findViewById(R.id.like_recyclerView);
        //初始化内容视图
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity.getApplicationContext()));

        //
        mDatas = new ArrayList<AudioInfo>();
        mAdapter = new RecentOrLikeMusicAdapter(mActivity, mDatas,false);
        mRecyclerView.setAdapter(mAdapter);

        showLoadingView();

        //
        //注册监听
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(mActivity.getApplicationContext());
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(mActivity.getApplicationContext());

    }

    /**
     * 处理音频监听事件
     *
     * @param context
     * @param intent
     */
    private void doAudioReceive(Context context, final Intent intent) {
        final String action = intent.getAction();

        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (action.equals(AudioBroadcastReceiver.ACTION_NULLMUSIC)) {
                    mAdapter.reshViewHolder(null,false);
                } else if (action.equals(AudioBroadcastReceiver.ACTION_LIKEADD)) {
                    //添加喜欢歌曲
                    AudioInfo audioInfo = (AudioInfo) intent.getSerializableExtra(AudioInfo.KEY);
                    if (audioInfo.getType() == AudioInfo.LOCAL) {
                        audioInfo.setLike(AudioInfo.LIKE_LOCAL);
                    } else if (audioInfo.getType() == AudioInfo.NET) {
                        audioInfo.setLike(AudioInfo.LIKE_NET);
                    } else if (audioInfo.getType() == AudioInfo.THIIRDNET) {
                        audioInfo.setLike(AudioInfo.LIKE_THIIRDNET);
                    }
                    mAdapter.reshViewHolder(audioInfo,true);

                } else if (action.equals(AudioBroadcastReceiver.ACTION_LIKEDELETE)) {
                    AudioInfo audioInfo = (AudioInfo) intent.getSerializableExtra(AudioInfo.KEY);
                    if (audioInfo != null) {
                        //删除喜欢歌曲
                        for (int i = 0; i < mDatas.size(); i++) {
                            AudioInfo temp = mDatas.get(i);
                            if (temp.getHash().equals(audioInfo.getHash())) {
                                mDatas.remove(i);
                                mAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }

                } else if (action.equals(AudioBroadcastReceiver.ACTION_NULLMUSIC)) {
                    mAdapter.reshViewHolder(null,false);
                } else if (action.equals(AudioBroadcastReceiver.ACTION_INITMUSIC)) {
                    //初始化
                    // AudioMessage audioMessage = (AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
                    AudioInfo audioInfo = HPApplication.getInstance().getCurAudioInfo();//audioMessage.getAudioInfo();
                    mAdapter.reshViewHolder(audioInfo,false);
                }
            }
        });
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        if (isRestoreInstance) {

            mDatas.clear();
        }
        mHandler.sendEmptyMessageDelayed(LOADDATA, 300);

    }

    /**
     * 加载数据
     */
    private void loadDataUtil(int sleepTime) {
        mDatas.clear();
        runnable = new Runnable() {
            @Override
            public void run() {
                List<AudioInfo> data = AudioInfoDB.getAudioInfoDB(mActivity.getApplicationContext()).getAllLikeAudio();
                for (int i = 0; i < data.size(); i++) {
                    mDatas.add(data.get(i));
                }

                if (mDatas.size() > 0) {
                    mAdapter.setState(RecentOrLikeMusicAdapter.NOMOREDATA);
                } else {
                    mAdapter.setState(RecentOrLikeMusicAdapter.NODATA);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() {
                        mAdapter.notifyDataSetChanged();
                        showContentView();
                    }
                });//切换至主线程更新ui

            }
        };
        ThreadUtil.runInThread(runnable);
    }

    @Override
    public void onDestroy() {
        mAudioBroadcastReceiver.unregisterReceiver(mActivity.getApplicationContext());
        if(runnable != null) {
            ThreadUtil.cancelThread(runnable);
        }
        super.onDestroy();
    }

    @Override
    protected int setTitleViewId() {
        return R.layout.layout_title;
    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }
}
