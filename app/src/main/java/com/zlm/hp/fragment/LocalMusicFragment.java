package com.zlm.hp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.zlm.hp.adapter.LocalMusicAdapter;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.Category;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.ui.ScanActivity;
import com.zlm.hp.utils.AsyncTaskUtil;
import com.zlm.hp.widget.IconfontImageButtonTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地音乐
 * Created by zhangliangming on 2017/7/23.
 */
public class LocalMusicFragment extends BaseFragment {

    //
    private LocalMusicAdapter mAdapter;
    private ArrayList<Category> mDatas;

    /**
     * 列表视图
     */
    private RecyclerView mRecyclerView;


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


    public LocalMusicFragment() {

    }

    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_local_music;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {

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
        //扫描
        IconfontImageButtonTextView scanImg = mainView.findViewById(R.id.scan_img);
        scanImg.setConvert(true);
        scanImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //
                Intent intent = new Intent(mActivity, ScanActivity.class);
                startActivity(intent);
                //去掉动画
                mActivity.overridePendingTransition(0, 0);

            }
        });
        //
        //
        mRecyclerView = mainView.findViewById(R.id.local_recyclerView);
        //初始化内容视图
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity.getApplicationContext()));

        //
        mDatas = new ArrayList<Category>();
        mAdapter = new LocalMusicAdapter(mHPApplication, mActivity.getApplicationContext(), mDatas);
        mRecyclerView.setAdapter(mAdapter);

        LocalMusicAdapter.CallBack callBack = new LocalMusicAdapter.CallBack() {
            @Override
            public void delete() {
                mDatas.clear();
                loadDataUtil();
            }
        };
        mAdapter.setCallBack(callBack);


        showLoadingView();


        //注册监听
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(mActivity.getApplicationContext(), mHPApplication);
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(mActivity.getApplicationContext());
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        if (isRestoreInstance) {
            mDatas.clear();
        }
        loadDataUtil();
    }


    private void loadDataUtil() {


        new AsyncTaskUtil() {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mDatas.size() > 0) {
                    mAdapter.notifyDataSetChanged();
                }
                showContentView();
            }

            @Override
            protected Void doInBackground(String... strings) {

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<String> categorys = AudioInfoDB.getAudioInfoDB(mActivity.getApplicationContext()).getAllLocalCategory();
                for (int i = 0; i < categorys.size(); i++) {
                    Category category = new Category();
                    String categoryName = categorys.get(i);
                    category.setCategoryName(categoryName);
                    List<Object> audioInfos = AudioInfoDB.getAudioInfoDB(mActivity.getApplicationContext()).getLocalAudio(categoryName);
                    category.setCategoryItem(audioInfos);

                    mDatas.add(category);
                }
                return super.doInBackground(strings);
            }
        }.execute("");
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
            AudioInfo audioInfo = mHPApplication.getCurAudioInfo();//audioMessage.getAudioInfo();
            mAdapter.reshViewHolder(audioInfo);

        } else if (action.equals(AudioBroadcastReceiver.ACTION_LOCALUPDATE)) {
            mDatas.clear();
            loadDataUtil();
        }
    }


    @Override
    public void onDestroy() {
        mAudioBroadcastReceiver.unregisterReceiver(mActivity.getApplicationContext());
        super.onDestroy();
    }

    @Override
    protected int setTitleViewId() {
        return R.layout.layout_localmusic_title;
    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }
}
