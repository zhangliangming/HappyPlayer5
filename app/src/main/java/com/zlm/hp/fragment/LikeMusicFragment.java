package com.zlm.hp.fragment;

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

import com.zlm.hp.adapter.RecentOrLikeMusicAdapter;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.utils.AsyncTaskUtil;

import java.util.ArrayList;
import java.util.List;

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


    public LikeMusicFragment() {

    }


    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_like;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {
        TextView titleView = mainView.findViewById(R.id.title);
        titleView.setText("喜欢");

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
        mAdapter = new RecentOrLikeMusicAdapter(mHPApplication, mActivity.getApplicationContext(), mDatas,false);
        mRecyclerView.setAdapter(mAdapter);

        RecentOrLikeMusicAdapter.LikeCallBack likeCallBack = new RecentOrLikeMusicAdapter.LikeCallBack() {
            @Override
            public void delete() {
                loadDataUtil(0);
            }
        };
        mAdapter.setLikeCallBack(likeCallBack);


        showLoadingView();

        //
        //注册监听
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(mActivity.getApplicationContext(), mHPApplication);
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(mActivity.getApplicationContext());

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
            mAdapter.reshViewHolder(null,false);
        } else if (action.equals(AudioBroadcastReceiver.ACTION_LIKEADD)) {
            //添加喜欢歌曲
            AudioInfo audioInfo = (AudioInfo) intent.getSerializableExtra(AudioInfo.KEY);
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
            AudioInfo audioInfo = mHPApplication.getCurAudioInfo();//audioMessage.getAudioInfo();
            mAdapter.reshViewHolder(audioInfo,false);
        }
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        if (isRestoreInstance) {

            mDatas.clear();
        }
        loadDataUtil(300);

    }

    /**
     * 加载数据
     */
    private void loadDataUtil(int sleepTime) {
        mDatas.clear();
        new AsyncTaskUtil() {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mDatas.size() > 0) {
                    mAdapter.setState(RecentOrLikeMusicAdapter.NOMOREDATA);
                } else {
                    mAdapter.setState(RecentOrLikeMusicAdapter.NODATA);
                }
                mAdapter.notifyDataSetChanged();
                showContentView();
            }

            @Override
            protected Void doInBackground(String... strings) {
                List<AudioInfo> data = AudioInfoDB.getAudioInfoDB(mActivity.getApplicationContext()).getAllLikeAudio();
                for (int i = 0; i < data.size(); i++) {
                    mDatas.add(data.get(i));
                }
                return super.doInBackground(strings);
            }
        }.execute("");
    }

    @Override
    public void onDestroy() {
        mAudioBroadcastReceiver.unregisterReceiver(mActivity.getApplicationContext());
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
