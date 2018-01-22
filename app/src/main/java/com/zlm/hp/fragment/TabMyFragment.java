package com.zlm.hp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.dialog.AlartOneButtonDialog;
import com.zlm.hp.dialog.AlartTwoButtonDialog;
import com.zlm.hp.manager.ActivityManage;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.receiver.SystemReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.utils.AppOpsUtils;
import com.zlm.hp.utils.AsyncTaskUtil;
import com.zlm.hp.utils.IntentUtils;
import com.zlm.hp.widget.SetupBGButton;

/**
 * @Description: tab我的界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/16 20:42
 * @Throws:
 */
public class TabMyFragment extends BaseFragment {

    /**
     * 本地音乐
     */
    private LinearLayout mLocalMusic;

    /**
     * 本地音乐个数
     */
    private TextView mLocalCountTv;
    /**
     * 本地音乐个数
     */
    private int mLocalCount = 0;

    //////////////////////////////////////////////

    /**
     * 喜欢音乐
     */
    private LinearLayout mLikeMusic;
    /**
     * 喜欢音乐个数
     */
    private TextView mLikeCountTv;
    /**
     * 喜欢音乐个数
     */
    private int mLikeCount = 0;

    /**
     * wifi设置按钮
     */
    private SetupBGButton mWifiSetupBGButton;
    /**
     * 问候语按钮
     */
    private SetupBGButton mSayHelloSetupBGButton;

    /**
     * 线控
     */
    private SetupBGButton mWireSetupBGButton;
    /**
     * 锁屏按钮
     */
    private SetupBGButton mLocklrcSetupBGButton;

    /**
     * 桌面按钮
     */
    private SetupBGButton mFloatWSetupBGButton;

    /**
     * 退出设置按钮
     */
    private SetupBGButton mExitSetupBGButton;

    /**
     * 退出提示窗口
     */
    private AlartTwoButtonDialog mExitAlartDialog;
    /***
     * 桌面歌词弹出窗口
     */
    private AlartOneButtonDialog mFloatWPAlartDialog;

    /**
     * 更新本地音乐
     */
    private final int UPDATELOCALCOUNT = 0;
    /**
     * 更新最近音乐
     */
    private final int UPDATERECENTCOUNT = 1;

    /**
     * 更新喜欢音乐
     */
    private final int UPDATELIKECOUNT = 2;
    /**
     * 更新下载音乐
     */
    private final int UPDATEDOWNLOADCOUNT = 3;
    /////////////////////////////////////////////////////////

    /**
     * 最近音乐
     */
    private LinearLayout mRecentMusic;

    /**
     * 最近音乐个数
     */
    private TextView mRecentCountTv;
    /**
     * 最近音乐个数
     */
    private int mRecentCount = 0;

    ////////////////////////////////////////////////////////

    /**
     * 下载音乐
     */
    private LinearLayout mDownloadMusic;
    /**
     * 下载音乐个数
     */
    private TextView mDownloadCountTv;
    /**
     * 下载音乐个数
     */
    private int mDownloadCount = 0;

    ////////////////////////////////
    /**
     *
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATELOCALCOUNT:
                    mLocalCountTv.setText(mLocalCount + "");
                    break;
                case UPDATERECENTCOUNT:
                    mRecentCountTv.setText(mRecentCount + "");
                    break;
                case UPDATELIKECOUNT:
                    mLikeCountTv.setText(mLikeCount + "");
                    break;
                case UPDATEDOWNLOADCOUNT:
                    mDownloadCountTv.setText(mDownloadCount + "");
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


    public TabMyFragment() {

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_my;
    }

    @Override
    protected int setTitleViewId() {
        return 0;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {

        //本地音乐
        mLocalMusic = mainView.findViewById(R.id.tab_local_music);
        mLocalMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //打开
                Intent openIntent = new Intent(FragmentReceiver.ACTION_OPENLOCALMUSICFRAGMENT);
                openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(openIntent);
            }
        });
        mLocalCountTv = mainView.findViewById(R.id.local_music_count);


        //喜欢的音乐
        mLikeMusic = mainView.findViewById(R.id.tab_like_music);
        mLikeMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //打开
                Intent openIntent = new Intent(FragmentReceiver.ACTION_OPENLIKEMUSICFRAGMENT);
                openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(openIntent);

            }
        });
        mLikeCountTv = mainView.findViewById(R.id.like_music_count);

        //下载音乐
        mDownloadMusic = mainView.findViewById(R.id.tab_download_music);
        mDownloadMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开
                Intent openIntent = new Intent(FragmentReceiver.ACTION_OPENDOWNLOADMUSICFRAGMENT);
                openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(openIntent);
            }
        });
        mDownloadCountTv = mainView.findViewById(R.id.download_music_count);

        //最近音乐
        mRecentMusic = mainView.findViewById(R.id.tab_centent_music);
        mRecentMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开
                Intent openIntent = new Intent(FragmentReceiver.ACTION_OPENRECENTMUSICFRAGMENT);
                openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(openIntent);

            }
        });
        mRecentCountTv = mainView.findViewById(R.id.recent_music_count);


        //wifi设置按钮
        mWifiSetupBGButton = mainView.findViewById(R.id.wifibg);
        if (mHPApplication.isWifi()) {
            mWifiSetupBGButton.setSelect(true);
        }
        mWifiSetupBGButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                boolean selected = mWifiSetupBGButton.isSelect();
                mHPApplication.setWifi(!selected);
                mWifiSetupBGButton.setSelect(mHPApplication.isWifi());
            }
        });


        //锁屏按钮
        mLocklrcSetupBGButton = mainView.findViewById(R.id.locklrcbg);
        if (mHPApplication.isShowLockScreen()) {
            mLocklrcSetupBGButton.setSelect(true);
        }
        mLocklrcSetupBGButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean selected = mLocklrcSetupBGButton.isSelect();
                mHPApplication.setShowLockScreen(!selected);
                mLocklrcSetupBGButton.setSelect(mHPApplication.isShowLockScreen());

            }
        });

        mFloatWPAlartDialog = new AlartOneButtonDialog(getActivity(), new AlartOneButtonDialog.ButtonDialogListener() {
            @Override
            public void ButtonClick() {
                IntentUtils.gotoPermissionSetting(getActivity());
            }
        });

        mFloatWSetupBGButton = mainView.findViewById(R.id.floatwbg);
        if (mHPApplication.isShowDesktop()) {
            mFloatWSetupBGButton.setSelect(true);
        }
        mFloatWSetupBGButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean selected = mFloatWSetupBGButton.isSelect();
                if (!selected) {
                    if (!AppOpsUtils.allowFloatWindow(getActivity())) {
                        mFloatWPAlartDialog.showDialog("1.进入设置>更多应用>乐乐音乐>权限管理>显示悬浮窗\n2.开启允许", "立即设置");
                        return;
                    }
                }
                mHPApplication.setShowDesktop(!selected);
                mFloatWSetupBGButton.setSelect(mHPApplication.isShowDesktop());

            }
        });

        //问候语按钮
        mSayHelloSetupBGButton = mainView.findViewById(R.id.sayhello);
        if (mHPApplication.isSayHello()) {
            mSayHelloSetupBGButton.setSelect(true);
        }
        mSayHelloSetupBGButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean selected = mSayHelloSetupBGButton.isSelect();
                mHPApplication.setSayHello(!selected);
                mSayHelloSetupBGButton.setSelect(mHPApplication.isSayHello());
            }
        });

        //线控
        mWireSetupBGButton = mainView.findViewById(R.id.wire);
        if (mHPApplication.isWire()) {
            mWireSetupBGButton.setSelect(true);
        }
        mWireSetupBGButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean selected = mWireSetupBGButton.isSelect();
                mHPApplication.setWire(!selected);
                mWireSetupBGButton.setSelect(mHPApplication.isWire());
                if (mHPApplication.isWire()) {
                    Intent openIntent = new Intent(SystemReceiver.ACTION_OPENWIREMESSAGE);
                    openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mActivity.sendBroadcast(openIntent);
                } else {
                    Intent closeIntent = new Intent(SystemReceiver.ACTION_CLOSEWIREMESSAGE);
                    closeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mActivity.sendBroadcast(closeIntent);
                }
            }
        });


        //关闭设置按钮
        mExitSetupBGButton = mainView.findViewById(R.id.exitbg);
        mExitSetupBGButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExitAlartDialog.showDialog("是否退出应用？", "取消", "确定");
            }
        });
        //
        mExitAlartDialog = new AlartTwoButtonDialog(mActivity, new AlartTwoButtonDialog.TwoButtonDialogListener() {
            @Override
            public void oneButtonClick() {

            }

            @Override
            public void twoButtonClick() {

                mHPApplication.setAppClose(true);

                ActivityManage.getInstance().exit();

            }
        });

        showContentView();


        //注册监听
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(mActivity.getApplicationContext(), mHPApplication);
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(mActivity.getApplicationContext());
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        loadLocalCount();
        loadRecentCount();
        loadLikeCount();
        loadDownloadCount();
    }

    /**
     * 加载喜欢歌曲列表
     */
    private void loadDownloadCount() {
        new AsyncTaskUtil() {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mHandler.sendEmptyMessage(UPDATEDOWNLOADCOUNT);
            }

            @Override
            protected Void doInBackground(String... strings) {

                mDownloadCount = AudioInfoDB.getAudioInfoDB(mActivity.getApplicationContext()).getDonwloadAudioCount();

                return super.doInBackground(strings);
            }
        }.execute("");
    }

    /**
     * 加载喜欢歌曲列表
     */
    private void loadLikeCount() {
        new AsyncTaskUtil() {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mHandler.sendEmptyMessage(UPDATELIKECOUNT);
            }

            @Override
            protected Void doInBackground(String... strings) {

                mLikeCount = AudioInfoDB.getAudioInfoDB(mActivity.getApplicationContext()).getLikeAudioCount();

                return super.doInBackground(strings);
            }
        }.execute("");
    }

    /**
     * 加载本地音乐个数
     */
    private void loadLocalCount() {
        new AsyncTaskUtil() {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mHandler.sendEmptyMessage(UPDATELOCALCOUNT);
            }

            @Override
            protected Void doInBackground(String... strings) {

                mLocalCount = AudioInfoDB.getAudioInfoDB(mActivity.getApplicationContext()).getLocalAudioCount();

                return super.doInBackground(strings);
            }
        }.execute("");
    }

    /**
     * 获取最近音乐个数
     */
    private void loadRecentCount() {
        new AsyncTaskUtil() {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mHandler.sendEmptyMessage(UPDATERECENTCOUNT);
            }

            @Override
            protected Void doInBackground(String... strings) {

                mRecentCount = AudioInfoDB.getAudioInfoDB(mActivity.getApplicationContext()).getRecentAudioCount();

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
        if (action.equals(AudioBroadcastReceiver.ACTION_LOCALUPDATE)) {
            loadLocalCount();
        } else if (action.equals(AudioBroadcastReceiver.ACTION_RECENTUPDATE)) {
            loadRecentCount();
        } else if (action.equals(AudioBroadcastReceiver.ACTION_LIKEUPDATE)) {
            loadLikeCount();
        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYMUSIC)) {
            //将正在播放的歌曲加入最近播放列表中
            AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
            if (audioInfo != null) {
                if (AudioInfoDB.getAudioInfoDB(mActivity.getApplication()).isRecentOrLikeExists(audioInfo.getHash(), audioInfo.getType(), true)) {
                    AudioInfoDB.getAudioInfoDB(mActivity.getApplication()).updateRecentAudio(audioInfo.getHash(), audioInfo.getType(), true);
                } else {
                    AudioInfoDB.getAudioInfoDB(mActivity.getApplication()).addRecentOrLikeAudio(audioInfo, true);
                }
                loadRecentCount();
            }
        } else if (action.equals(AudioBroadcastReceiver.ACTION_DOWNLOADUPDATE)) {
            loadDownloadCount();
        } else if (action.equals(AudioBroadcastReceiver.ACTION_LIKEADD)) {
            //添加喜欢
            AudioInfo audioInfo = (AudioInfo) intent.getSerializableExtra(AudioInfo.KEY);
            AudioInfoDB.getAudioInfoDB(mActivity.getApplication()).addRecentOrLikeAudio(audioInfo, false);
            loadLikeCount();
        } else if (action.equals(AudioBroadcastReceiver.ACTION_LIKEDELETE)) {
            //删除喜欢
            AudioInfo audioInfo = (AudioInfo) intent.getSerializableExtra(AudioInfo.KEY);
            AudioInfoDB.getAudioInfoDB(mActivity.getApplication()).deleteRecentOrLikeAudio(audioInfo.getHash(), audioInfo.getType(), false);
            loadLikeCount();
        }
    }

    @Override
    public void onDestroy() {
        mAudioBroadcastReceiver.unregisterReceiver(mActivity.getApplicationContext());
        super.onDestroy();
    }

    @Override
    protected boolean isAddStatusBar() {
        return false;
    }

}  