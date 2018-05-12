package com.zlm.hp.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.adapter.MainPopPlayListAdapter;
import com.zlm.hp.adapter.TabFragmentAdapter;
import com.zlm.hp.db.DownloadThreadDB;
import com.zlm.hp.fragment.DownloadMusicFragment;
import com.zlm.hp.fragment.LikeMusicFragment;
import com.zlm.hp.fragment.LocalMusicFragment;
import com.zlm.hp.fragment.RankSongFragment;
import com.zlm.hp.fragment.RecentMusicFragment;
import com.zlm.hp.fragment.SearchFragment;
import com.zlm.hp.fragment.TabMyFragment;
import com.zlm.hp.fragment.TabRecommendFragment;
import com.zlm.hp.libs.utils.ColorUtil;
import com.zlm.hp.libs.utils.ToastUtil;
import com.zlm.hp.libs.widget.CircleImageView;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.utils.ColorUtils;
import com.zlm.hp.lyrics.utils.TimeUtils;
import com.zlm.hp.lyrics.widget.AbstractLrcView;
import com.zlm.hp.lyrics.widget.FloatLyricsView;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.manager.OnLineAudioManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.model.DownloadMessage;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.receiver.LockLrcReceiver;
import com.zlm.hp.receiver.MobliePhoneReceiver;
import com.zlm.hp.receiver.OnLineAudioReceiver;
import com.zlm.hp.receiver.PhoneReceiver;
import com.zlm.hp.receiver.SystemReceiver;
import com.zlm.hp.service.AudioPlayerService;
import com.zlm.hp.service.FloatService;
import com.zlm.hp.utils.ImageUtil;
import com.zlm.hp.utils.ToastShowUtil;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.IconfontIndicatorTextView;
import com.zlm.hp.widget.IconfontTextView;
import com.zlm.hp.widget.LinearLayoutRecyclerView;
import com.zlm.hp.widget.SlidingMenuLayout;
import com.zlm.hp.widget.SwipeOutLayout;
import com.zlm.libs.widget.MusicSeekBar;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 主界面
 * @Author: zhangliangming
 * @Date: 2017/7/15 23:39
 * @Version:
 */
public class MainActivity extends BaseActivity {
    /**
     * 主界面跳转到歌词界面的code
     */
    private final int MAINTOLRCRESULTCODE = 0;
    /**
     * 歌词界面跳转到主界面的code
     */
    private final int LRCTOMAINRESULTCODE = 1;

    /**
     * 保存退出时间
     */
    private long mExitTime;

    //////////////////////////标题栏/////////////////////////////////////////////////

    /**
     * 图标按钮
     */
    private IconfontImageButtonTextView mIconButton;


    private IconfontImageButtonTextView mSearchButton;

    /**
     * tab菜单图标按钮
     */
    private IconfontIndicatorTextView[] mTabImageButton;

    ////////////////////////////////中间内容//////////////////////////////////////////
    /**
     * 选中索引
     */
    private int mSelectedIndex = 0;

    /**
     * 中间视图
     */
    private ViewPager mViewPager;
    /**
     * 中间视图布局
     */
    private SlidingMenuLayout slidingMenuLayout;

    /////////////////////////////////////////////////////////////////////////////////

    private LinearLayout mPlayerBarParentLinearLayout;

    /**
     * 底部播放器的布局
     */
    private SwipeOutLayout mSwipeOutLayout;
    /**
     * 歌手头像
     */
    private CircleImageView mSingerImg;
    /**
     * bar打开标记
     */
    private ImageView mBarOpenFlagView;
    /**
     * bar关闭标记
     */
    private ImageView mBarCloseFlagView;
    /**
     * 歌曲名称tv
     */
    private TextView mSongNameTextView;
    /**
     * 歌手tv
     */
    private TextView mSingerNameTextView;
    /**
     * 播放按钮
     */
    private ImageView mPlayImageView;
    /**
     * 暂停按钮
     */
    private ImageView mPauseImageView;
    /**
     * 下一首按钮
     */
    private ImageView mNextImageView;
    /**
     * 歌曲进度
     */
    private MusicSeekBar mMusicSeekBar;
    /**
     * 双行歌词
     */
    private FloatLyricsView mFloatLyricsView;


    /////////////////////////////////////////////////////////////////////////////////

    /**
     * main frame监听
     */
    private SlidingMenuLayout.FragmentListener mFragmentListener;

    /**
     * 音频广播
     */
    private AudioBroadcastReceiver mAudioBroadcastReceiver;
    /**
     * 在线音乐广播
     */
    private OnLineAudioReceiver mOnLineAudioReceiver;
    private OnLineAudioReceiver.OnlineAudioReceiverListener mOnlineAudioReceiverListener = new OnLineAudioReceiver.OnlineAudioReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doNetMusicReceive(context, intent);
        }
    };
    /**
     * 耳机广播
     */
    private PhoneReceiver mPhoneReceiver;
    /**
     * 监听电话
     */
    private MobliePhoneReceiver mMobliePhoneReceiver;
    /**
     * 系统广播
     */
    private SystemReceiver mSystemReceiver;
    private SystemReceiver.SystemReceiverListener mSystemReceiverListener = new SystemReceiver.SystemReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doSystemReceive(context, intent);
        }
    };
    /**
     * Fragment广播
     */
    private FragmentReceiver mFragmentReceiver;
    private FragmentReceiver.FragmentReceiverListener mFragmentReceiverListener = new FragmentReceiver.FragmentReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doFragmentReceive(context, intent);
        }
    };
    /**
     * 锁屏广播
     */
    private LockLrcReceiver mLockLrcReceiver;
    private LockLrcReceiver.LockLrcReceiverListener mLockLrcReceiverListener = new LockLrcReceiver.LockLrcReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doLockLrcReceiver(context, intent);
        }
    };
    /**
     *
     */
    private Handler mCheckServiceHandler = new Handler();
    /**
     * 检测时间
     */
    private int mCheckServiceTime = 200;
    /**
     * 当前播放歌曲的索引
     */
    private String mCurPlayIndexHash = "";
    /**
     * 检测服务线程
     */
    private Runnable mCheckServiceRunnable = new Runnable() {
        @Override
        public void run() {

            //如果歌曲正在播放，实时更新页面数据，防止回收后启动时，页面还是旧数据的问题
            if (mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYING || mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYNET) {
                if (mHPApplication.getCurAudioMessage() != null && mHPApplication.getCurAudioInfo() != null) {
                    if (!mCurPlayIndexHash.equals(mHPApplication.getCurAudioInfo().getHash())) {

                        Intent initIntent = new Intent(AudioBroadcastReceiver.ACTION_INITMUSIC);
                        initIntent.putExtra(AudioMessage.KEY, mHPApplication.getCurAudioMessage());
                        initIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        sendBroadcast(initIntent);

                    }
                }
            }

            //
            if (!isServiceRunning(AudioPlayerService.class.getName())) {
                logger.e("监听音频服务初始回收");
                if (!mHPApplication.isAppClose()) {

                    //服务被强迫回收
                    Intent playerServiceIntent = new Intent(getApplicationContext(), AudioPlayerService.class);
                    mHPApplication.startService(playerServiceIntent);


                    mHPApplication.setPlayServiceForceDestroy(true);
                    logger.e("重新启动音频播放服务广播");
                }
            }

            //
            if (isBackgroundRunning(getApplicationContext())) {
                logger.e("正在后台运行");
                if (!isServiceRunning(FloatService.class.getName())) {

                    if (mHPApplication.isShowDesktop()) {
                        //启动悬浮窗口服务
                        Intent floatServiceIntent = new Intent(getApplicationContext(), FloatService.class);
                        mHPApplication.startService(floatServiceIntent);
                    }
                } else {
                    //悬浮窗口服务已经启动
                    if (!mHPApplication.isShowDesktop()) {
                        //不显示桌面歌词
                        //关闭悬浮窗口服务
                        Intent floatServiceIntent = new Intent(getApplicationContext(), FloatService.class);
                        mHPApplication.stopService(floatServiceIntent);
                    }
                }
            } else {
                logger.e("正在前台运行");
                if (isServiceRunning(FloatService.class.getName())) {
                    //关闭悬浮窗口服务
                    Intent floatServiceIntent = new Intent(getApplicationContext(), FloatService.class);
                    mHPApplication.stopService(floatServiceIntent);

                }
            }


            mCheckServiceHandler.postDelayed(mCheckServiceRunnable, mCheckServiceTime);
        }
    };
    /**
     * 弹出窗口是否显示
     */
    private boolean isPopViewShow = false;

    ///////////////////////////pop///////////////////////////////////////
    /**
     * 弹出窗口全屏界面
     */
    private RelativeLayout mListPopLinearLayout;
    /**
     * 弹出视图
     */
    private RelativeLayout mPopMenuRelativeLayout;
    /**
     * 当前播放列表
     */
    private LinearLayoutRecyclerView mCurRecyclerView;
    /**
     *
     */
    private MainPopPlayListAdapter mPopPlayListAdapter;
    /**
     * 广播监听
     */
    private AudioBroadcastReceiver.AudioReceiverListener mAudioReceiverListener = new AudioBroadcastReceiver.AudioReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doAudioReceive(context, intent);
        }
    };
    /**
     * 当前播放列表歌曲总数
     */
    private TextView mCurPLSizeTv;

    //播放模式
    private IconfontTextView modeAllTv;
    private IconfontTextView modeRandomTv;
    private IconfontTextView modeSingleTv;

    @Override
    protected void initViews(Bundle savedInstanceState) {

        //初始化标题栏视图
        initTitleViews();

        //初始化中间视图
        initPageViews();

        //初始化底部播放器视图
        initPlayerViews();

        //初始化服务
        initService();

    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        if (!isRestoreInstance) {
            AudioPlayerManager.getAudioPlayerManager(getApplicationContext(), mHPApplication).initSongInfoData();
        }
    }

    /**
     * 处理Fragment广播
     *
     * @param context
     * @param intent
     */
    private void doFragmentReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(FragmentReceiver.ACTION_OPENRANKSONGFRAGMENT)) {
            //打开单个排行的歌曲列表
            mFragmentListener.openFragment(new RankSongFragment());
        } else if (action.equals(FragmentReceiver.ACTION_OPENLOCALMUSICFRAGMENT)) {
            //打开本地音乐
            mFragmentListener.openFragment(new LocalMusicFragment());
        } else if (action.equals(FragmentReceiver.ACTION_OPENLIKEMUSICFRAGMENT)) {
            //打开喜欢
            mFragmentListener.openFragment(new LikeMusicFragment());
        } else if (action.equals(FragmentReceiver.ACTION_OPENDOWNLOADMUSICFRAGMENT)) {
            //打开下载
            mFragmentListener.openFragment(new DownloadMusicFragment());
        } else if (action.equals(FragmentReceiver.ACTION_OPENRECENTMUSICFRAGMENT)) {
            //打开最近
            mFragmentListener.openFragment(new RecentMusicFragment());
        } else if (action.equals(FragmentReceiver.ACTION_CLOSEDFRAGMENT)) {
            //关闭
            mFragmentListener.closeFragment();
        }
    }


    /**
     * 处理系统广播
     *
     * @param context
     * @param intent
     */
    private void doSystemReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(SystemReceiver.ACTION_TOASTMESSAGE)) {
            //提示信息
            String message = intent.getStringExtra(ToastUtil.MESSAGEKEY);
            ToastShowUtil.showTextToast(getApplicationContext(), message);
        } else if (action.equals(SystemReceiver.ACTION_OPENWIREMESSAGE)) {
            //打开线控
            mPhoneReceiver.registerReceiver(getApplicationContext());
        } else if (action.equals(SystemReceiver.ACTION_CLOSEWIREMESSAGE)) {
            //关闭线控
            mPhoneReceiver.unregisterReceiver(getApplicationContext());
        } else if (action.equals("android.media.AUDIO_BECOMING_NOISY") || action.equals("android.provider.Telephony.SMS_RECEIVED")) {
// 耳机拔出  或者收到短信
            /**
             * 从硬件层面来看，直接监听耳机拔出事件不难，耳机的拔出和插入，会引起手机电平的变化，然后触发什么什么中断，
             *
             * 最终在stack overflow找到答案，监听Android的系统广播AudioManager.
             * ACTION_AUDIO_BECOMING_NOISY，
             * 但是这个广播只是针对有线耳机，或者无线耳机的手机断开连接的事件，监听不到有线耳机和蓝牙耳机的接入
             * ，但对于我的需求来说足够了，监听这个广播就没有延迟了，UI可以立即响应
             */
            int playStatus = mHPApplication.getPlayStatus();
            if (playStatus == AudioPlayerManager.PLAYING) {

                Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_PAUSEMUSIC);
                resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(resumeIntent);

            }

        }
    }

    /**
     * 处理网络歌曲广播
     *
     * @param context
     * @param intent
     */
    private void doNetMusicReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(OnLineAudioReceiver.ACTION_ONLINEMUSICDOWNLOADING)) {
            DownloadMessage downloadMessage = (DownloadMessage) intent.getSerializableExtra(DownloadMessage.KEY);
            if (mHPApplication.getPlayIndexHashID().equals(downloadMessage.getTaskId())) {
                int downloadedSize = DownloadThreadDB.getDownloadThreadDB(getApplicationContext()).getDownloadedSize(downloadMessage.getTaskId(), OnLineAudioManager.threadNum);
                double pre = downloadedSize * 1.0 / mHPApplication.getCurAudioInfo().getFileSize();
                int downloadProgress = (int) (mMusicSeekBar.getMax() * pre);
                mMusicSeekBar.setSecondaryProgress(downloadProgress);
            }
        } else if (action.equals(OnLineAudioReceiver.ACTION_ONLINEMUSICERROR)) {
            DownloadMessage downloadMessage = (DownloadMessage) intent.getSerializableExtra(DownloadMessage.KEY);
            if (mHPApplication.getPlayIndexHashID().equals(downloadMessage.getTaskId())) {
                ToastUtil.showTextToast(getApplicationContext(), downloadMessage.getErrorMsg());
            }
        }

    }

    /**
     * 处理音频广播事件
     *
     * @param context
     * @param intent
     */
    private void doAudioReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(AudioBroadcastReceiver.ACTION_NULLMUSIC)) {
            //空数据
            mSongNameTextView.setText(R.string.def_songName);
            mSingerNameTextView.setText(R.string.def_artist);
            mPauseImageView.setVisibility(View.INVISIBLE);
            mPlayImageView.setVisibility(View.VISIBLE);

            //
            mMusicSeekBar.setEnabled(false);
            mMusicSeekBar.setProgress(0);
            mMusicSeekBar.setSecondaryProgress(0);
            mMusicSeekBar.setMax(0);
            //隐藏
            mSingerImg.setTag(null);

            //
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.singer_def);
            mSingerImg.setImageDrawable(new BitmapDrawable(bitmap));

            //
            mFloatLyricsView.initLrcData();

            //重置弹出窗口播放列表
            if (isPopViewShow) {
                if (mPopPlayListAdapter != null) {
                    mPopPlayListAdapter.reshViewHolder(null);
                }
            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_INITMUSIC)) {


            //初始化
            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            AudioInfo audioInfo = mHPApplication.getCurAudioInfo();

            mCurPlayIndexHash = audioInfo.getHash();

            mSongNameTextView.setText(audioInfo.getSongName());
            mSingerNameTextView.setText(audioInfo.getSingerName());
            mPauseImageView.setVisibility(View.INVISIBLE);
            mPlayImageView.setVisibility(View.VISIBLE);

            //
            mMusicSeekBar.setEnabled(true);
            mMusicSeekBar.setMax((int) audioInfo.getDuration());
            mMusicSeekBar.setProgress((int) audioMessage.getPlayProgress());
            mMusicSeekBar.setSecondaryProgress(0);
            //加载歌手图片
            ImageUtil.loadSingerImage(mHPApplication, getApplicationContext(), mSingerImg, audioInfo.getSingerName());

            //加载歌词
            String keyWords = "";
            if (audioInfo.getSingerName().equals("未知")) {
                keyWords = audioInfo.getSongName();
            } else {
                keyWords = audioInfo.getSingerName() + " - " + audioInfo.getSongName();
            }
            LyricsManager.getLyricsManager(mHPApplication, getApplicationContext()).loadLyricsUtil(keyWords, keyWords, audioInfo.getDuration() + "", audioInfo.getHash());

            //
            mFloatLyricsView.initLrcData();
            //加载中
            mFloatLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);

            //设置弹出窗口播放列表
            if (isPopViewShow) {
                if (mPopPlayListAdapter != null) {
                    mPopPlayListAdapter.reshViewHolder(audioInfo);
                }
            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYMUSIC)) {
            //播放

            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);

            mPauseImageView.setVisibility(View.VISIBLE);
            mPlayImageView.setVisibility(View.INVISIBLE);

            //
            mMusicSeekBar.setProgress((int) audioMessage.getPlayProgress());

            if (audioMessage != null) {
                mMusicSeekBar.setProgress((int) audioMessage.getPlayProgress());
                AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                if (audioInfo != null) {
                    //更新歌词


                    if (mFloatLyricsView.getLyricsReader() != null && mFloatLyricsView.getLyricsReader().getHash().equals(audioInfo.getHash()) && mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mFloatLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY) {
                        mFloatLyricsView.play((int) audioMessage.getPlayProgress());
                    }
                }

            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PAUSEMUSIC)) {

            if (mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                mFloatLyricsView.pause();
            }

            //暂停完成
            mPauseImageView.setVisibility(View.INVISIBLE);
            mPlayImageView.setVisibility(View.VISIBLE);


        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_RESUMEMUSIC)) {
            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
            if (audioMessage != null) {
                if (mFloatLyricsView != null && mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mFloatLyricsView.play((int) audioMessage.getPlayProgress());
                }
            }

            //唤醒完成
            mPauseImageView.setVisibility(View.VISIBLE);
            mPlayImageView.setVisibility(View.INVISIBLE);


        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_SEEKTOMUSIC)) {
            //唤醒完成
            mPauseImageView.setVisibility(View.VISIBLE);
            mPlayImageView.setVisibility(View.INVISIBLE);

            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
            if (audioMessage != null) {
                if (mFloatLyricsView != null && mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mFloatLyricsView.play((int) audioMessage.getPlayProgress());
                }
            }
        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYINGMUSIC)) {

            //播放中
            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
            if (audioMessage != null) {
                mMusicSeekBar.setProgress((int) audioMessage.getPlayProgress());

            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_LRCLOADED)) {
            if (mHPApplication.getCurAudioMessage() != null && mHPApplication.getCurAudioInfo() != null) {
                //歌词加载完成
                AudioMessage curAudioMessage = mHPApplication.getCurAudioMessage();
                AudioMessage audioMessage = (AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
                String hash = audioMessage.getHash();
                if (hash.equals(mHPApplication.getCurAudioInfo().getHash())) {
                    //
                    LyricsReader lyricsReader = LyricsManager.getLyricsManager(mHPApplication, getApplicationContext()).getLyricsUtil(hash);
                    if (lyricsReader != null) {
                        if (lyricsReader.getHash() != null && lyricsReader.getHash().equals(hash) && mFloatLyricsView.getLyricsReader() != null) {
                            //已加载歌词，不用重新加载
                        } else {
                            lyricsReader.setHash(hash);
                            mFloatLyricsView.setLyricsReader(lyricsReader);
                            if (mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYING && mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mFloatLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY)
                                mFloatLyricsView.play((int) curAudioMessage.getPlayProgress());
                        }
                    }
                }
            }
        } else if (action.equals(AudioBroadcastReceiver.ACTION_LRCSEEKTO)) {
            if (mHPApplication.getCurAudioMessage() != null) {
                mMusicSeekBar.setProgress((int) mHPApplication.getCurAudioMessage().getPlayProgress());
                if (mHPApplication.getCurAudioInfo() != null) {
                    if (mFloatLyricsView.getLyricsReader() != null && mFloatLyricsView.getLyricsReader().getHash().equals(mHPApplication.getCurAudioInfo().getHash()) && mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                        mFloatLyricsView.seekto((int) mHPApplication.getCurAudioMessage().getPlayProgress());
                    }
                }
            }

        }
    }

    /**
     * 处理锁屏广播事件
     *
     * @param context
     * @param intent
     */
    private void doLockLrcReceiver(Context context, Intent intent) {

        if (intent.getAction().equals(LockLrcReceiver.ACTION_LOCKLRC_SCREEN_OFF)) {

            //显示锁屏歌词和正在播放
            if (mHPApplication.isShowLockScreen() && mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYING) {
                Intent lockIntent = new Intent(MainActivity.this,
                        LockActivity.class);
                lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(lockIntent);
                //去掉动画
                overridePendingTransition(0, 0);
            }

        }
    }

    /**
     * 初始化服务
     */
    private void initService() {
        Intent playerServiceIntent = new Intent(this, AudioPlayerService.class);
        mHPApplication.startService(playerServiceIntent);


        //注册接收音频播放广播
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(getApplicationContext(), mHPApplication);
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(getApplicationContext());

        //在线音乐广播
        mOnLineAudioReceiver = new OnLineAudioReceiver(getApplicationContext(), mHPApplication);
        mOnLineAudioReceiver.setOnlineAudioReceiverListener(mOnlineAudioReceiverListener);
        mOnLineAudioReceiver.registerReceiver(getApplicationContext());

        //系统广播
        mSystemReceiver = new SystemReceiver(getApplicationContext(), mHPApplication);
        mSystemReceiver.setSystemReceiverListener(mSystemReceiverListener);
        mSystemReceiver.registerReceiver(getApplicationContext());

        //耳机广播
        mPhoneReceiver = new PhoneReceiver();
        if (mHPApplication.isWire()) {
            mPhoneReceiver.registerReceiver(getApplicationContext());
        }

        //电话监听
        mMobliePhoneReceiver = new MobliePhoneReceiver(getApplicationContext(), mHPApplication);
        mMobliePhoneReceiver.registerReceiver(getApplicationContext());

        //mFragment广播
        mFragmentReceiver = new FragmentReceiver(getApplicationContext(), mHPApplication);
        mFragmentReceiver.setFragmentReceiverListener(mFragmentReceiverListener);
        mFragmentReceiver.registerReceiver(getApplicationContext());

        //锁屏广播
        mLockLrcReceiver = new LockLrcReceiver(getApplicationContext());
        mLockLrcReceiver.setLockLrcReceiverListener(mLockLrcReceiverListener);
        mLockLrcReceiver.registerReceiver(getApplicationContext());

        //
        mCheckServiceHandler.postDelayed(mCheckServiceRunnable, mCheckServiceTime);
    }


    /**
     * 初始化标题栏视图
     */
    private void initTitleViews() {
        //图标
        mIconButton = findViewById(R.id.iconImageButton);
        mIconButton.setConvert(true);
        mIconButton.setPressed(false);
        mIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                //去掉动画
                overridePendingTransition(0, 0);
            }
        });

        //初始化tab菜单

        mTabImageButton = new IconfontIndicatorTextView[2];
        int index = 0;
        //我的tab
        mTabImageButton[index] = findViewById(R.id.myImageButton);
        mTabImageButton[index].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean selected = mTabImageButton[0].isSelected();
                if (!selected) {
                    mViewPager.setCurrentItem(0, true);
                }
            }
        });
        mTabImageButton[index++].setSelected(true);

        //排行
        mTabImageButton[index] = findViewById(R.id.recommendImageButton);
        mTabImageButton[index].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean selected = mTabImageButton[1].isSelected();
                if (!selected) {
                    mViewPager.setCurrentItem(1, true);
                }
            }
        });
        mTabImageButton[index++].setSelected(false);


        //搜索
        mSearchButton = findViewById(R.id.searchImageButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFragmentListener.openFragment(new SearchFragment());


            }
        });
        mSearchButton.setConvert(true);
        mSearchButton.setPressed(false);

    }

    /**
     * 初始化中间视图
     */
    private void initPageViews() {

        //
        slidingMenuLayout = findViewById(R.id.slidingMenuLayout);
        slidingMenuLayout.initView((LinearLayout) findViewById((R.id.main_container)));
        slidingMenuLayout.addStatusBarView((ViewGroup) findViewById(R.id.main_container));
        mFragmentListener = new SlidingMenuLayout.FragmentListener() {
            @Override
            public void openFragment(Fragment fragment) {
                slidingMenuLayout.showMenuView(getSupportFragmentManager(), fragment);
            }

            @Override
            public void closeFragment() {
                slidingMenuLayout.hideMenuView(getSupportFragmentManager());
            }
        };
        //
        mViewPager = findViewById(R.id.viewpage);

        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new TabMyFragment());
        fragments.add(new TabRecommendFragment());

        //
        TabFragmentAdapter adapter = new TabFragmentAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position != mSelectedIndex) {
                    mTabImageButton[mSelectedIndex].setSelected(false);
                    mTabImageButton[position].setSelected(true);
                    mSelectedIndex = position;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    /**
     * 初始化播放列表
     */
    private void initListPopView() {

        ViewStub stub = findViewById(R.id.viewstub_main_pop);
        stub.inflate();

        mCurPLSizeTv = findViewById(R.id.list_size);
        mCurRecyclerView = findViewById(R.id.curplaylist_recyclerView);
        //初始化内容视图
        mCurRecyclerView.setLinearLayoutManager(new LinearLayoutManager(getApplicationContext()));

        //
        mListPopLinearLayout = findViewById(R.id.list_pop);
        mListPopLinearLayout.setVisibility(View.INVISIBLE);
        mListPopLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidePopView();
            }
        });
        //
        mPopMenuRelativeLayout = findViewById(R.id.pop_parent);

        //播放模式
        modeAllTv = findViewById(R.id.modeAll);
        modeRandomTv = findViewById(R.id.modeRandom);
        modeSingleTv = findViewById(R.id.modeSingle);
        //
        modeAllTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(1, modeAllTv, modeRandomTv, modeSingleTv, true);
            }
        });

        modeRandomTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(3, modeAllTv, modeRandomTv, modeSingleTv, true);
            }
        });

        modeSingleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(0, modeAllTv, modeRandomTv, modeSingleTv, true);
            }
        });
        initPlayModeView(mHPApplication.getPlayModel(), modeAllTv, modeRandomTv, modeSingleTv, false);

        //删除播放列表按钮
//        IconfontTextView deleteTv = findViewById(R.id.delete);
//        deleteTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
    }

    /**
     * 初始化播放列表播放模式
     *
     * @param playMode
     * @param modeAllImg
     * @param modeRandomImg
     * @param modeSingleImg
     */
    private void initPlayModeView(int playMode, IconfontTextView modeAllImg, IconfontTextView modeRandomImg, IconfontTextView modeSingleImg, boolean isTipShow) {
        if (playMode == 0) {
            if (isTipShow)
                ToastUtil.showTextToast(MainActivity.this, "顺序播放");
            modeAllImg.setVisibility(View.VISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else if (playMode == 1) {
            if (isTipShow)
                ToastUtil.showTextToast(MainActivity.this, "随机播放");
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.VISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else {
            if (isTipShow)
                ToastUtil.showTextToast(MainActivity.this, "单曲播放");
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.VISIBLE);
        }
        //
        mHPApplication.setPlayModel(playMode);
    }


    /**
     * 隐藏popview
     */
    private void hidePopView() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, mPopMenuRelativeLayout.getHeight());
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mListPopLinearLayout.setBackgroundColor(ColorUtil.parserColor(Color.BLACK, 0));
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                isPopViewShow = false;
                mListPopLinearLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mPopMenuRelativeLayout.clearAnimation();
        mPopMenuRelativeLayout.startAnimation(translateAnimation);
    }

    /**
     * 显示popview
     */
    private void showPopView() {

        if (mListPopLinearLayout == null) {
            //初始化播放列表
            initListPopView();
        }

        initPlayModeView(mHPApplication.getPlayModel(), modeAllTv, modeRandomTv, modeSingleTv, false);
        //加载当前播放列表数据
        List<AudioInfo> curAudioInfos = mHPApplication.getCurAudioInfos();
        if (curAudioInfos == null) {
            curAudioInfos = new ArrayList<AudioInfo>();
        }
        mCurPLSizeTv.setText(curAudioInfos.size() + "");
        mPopPlayListAdapter = new MainPopPlayListAdapter(mHPApplication, getApplicationContext(), curAudioInfos);
        mCurRecyclerView.setAdapter(mPopPlayListAdapter);
        //滚动到当前播放位置
        int position = mPopPlayListAdapter.getPlayIndexPosition(mHPApplication.getCurAudioInfo());
        if (position >= 0)
            mCurRecyclerView.moveToPosition(position);

        /**
         * 如果该界面还没初始化，则监听
         */
        if (mPopMenuRelativeLayout.getHeight() == 0) {
            mPopMenuRelativeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mPopMenuRelativeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    showPopViewHandler();
                }
            });

        } else {
            showPopViewHandler();
        }
    }

    /**
     * 动画处理
     */
    private void showPopViewHandler() {
        //
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mPopMenuRelativeLayout.getHeight(), 0);
        translateAnimation.setDuration(350);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mListPopLinearLayout.setBackgroundColor(ColorUtil.parserColor(Color.BLACK, 120));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //
        mListPopLinearLayout.setVisibility(View.VISIBLE);
        mListPopLinearLayout.setBackgroundColor(ColorUtil.parserColor(Color.BLACK, 0));
        //
        mPopMenuRelativeLayout.clearAnimation();
        mPopMenuRelativeLayout.startAnimation(translateAnimation);
        isPopViewShow = true;
    }


    /**
     * 初始化底部播放器视图
     */
    private void initPlayerViews() {

        //
        mPlayerBarParentLinearLayout = findViewById(R.id.playerBarParent);

        mSwipeOutLayout = findViewById(R.id.playerBar);
        mSwipeOutLayout.setBackgroundColor(ColorUtil.parserColor("#ffffff", 245));
        ViewGroup barContentView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.layout_main_player_content, null);

        ViewGroup barMenuView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.layout_main_player_menu, null);
        //
        mFloatLyricsView = barMenuView.findViewById(R.id.floatLyricsView);
        //默认颜色
        int[] paintColors = new int[]{
                ColorUtils.parserColor("#00348a"),
                ColorUtils.parserColor("#0080c0"),
                ColorUtils.parserColor("#03cafc")
        };
        mFloatLyricsView.setPaintColor(paintColors);

        //高亮颜色
        int[] paintHLColors = new int[]{
                ColorUtils.parserColor("#82f7fd"),
                ColorUtils.parserColor("#ffffff"),
                ColorUtils.parserColor("#03e9fc")
        };
        mFloatLyricsView.setPaintHLColor(paintHLColors);
        //设置字体文件
        Typeface typeFace = Typeface.createFromAsset(getAssets(),
                "fonts/weiruanyahei14M.ttf");
        mFloatLyricsView.setTypeFace(typeFace, false);

        //歌手头像
        mSingerImg = barContentView.findViewById(R.id.play_bar_artist);
        mSingerImg.setTag(null);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.singer_def);
        mSingerImg.setImageDrawable(new BitmapDrawable(bitmap));


        mBarCloseFlagView = barContentView.findViewById(R.id.bar_dragflagClosed);
        mBarOpenFlagView = barContentView.findViewById(R.id.bar_dragflagOpen);
        //
        if (mHPApplication.isBarMenuShow()) {
            mSwipeOutLayout.initViewAndShowMenuView(barContentView, barMenuView, mSingerImg);
        } else {
            mSwipeOutLayout.initViewAndShowContentView(barContentView, barMenuView, mSingerImg);
        }

//        playerBarLinearLayout.setDragViewOnClickListener(new PlayerBarLinearLayout.DragViewOnClickListener() {
//            @Override
//            public void onClick() {
//
//                if(playerBarLinearLayout.isMenuViewShow()){
//
//                    //隐藏菜单
//                    playerBarLinearLayout.hideMenuView();
//
//                }else{
//                    logger.e("点击了专辑图片");
//                }
//            }
//        });
        mSwipeOutLayout.setPlayerBarListener(new SwipeOutLayout.PlayerBarListener() {
            @Override
            public void onClose() {
//                if (mBarCloseFlagView.getVisibility() != View.VISIBLE) {
//                    mBarCloseFlagView.setVisibility(View.VISIBLE);
//                }

                if (mBarOpenFlagView.getVisibility() != View.INVISIBLE) {
                    mBarOpenFlagView.setVisibility(View.INVISIBLE);
                }

                //
                mHPApplication.setBarMenuShow(false);
            }


            @Override
            public void onOpen() {
//                if (mBarCloseFlagView.getVisibility() != View.INVISIBLE) {
//                    mBarCloseFlagView.setVisibility(View.INVISIBLE);
//                }

                if (mBarOpenFlagView.getVisibility() != View.VISIBLE) {
                    mBarOpenFlagView.setVisibility(View.VISIBLE);
                }

                //
                mHPApplication.setBarMenuShow(true);
            }
        });
        mSwipeOutLayout.setPlayerBarOnClickListener(new SwipeOutLayout.PlayerBarOnClickListener() {
            @Override
            public void onClick() {

                if (isPopViewShow) {
                    hidePopView();
                    return;
                }
                if (mSwipeOutLayout.isMenuViewShow() && mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    if (mFloatLyricsView.getExtraLrcType() != AbstractLrcView.EXTRALRCTYPE_NOLRC) {

                        if (mFloatLyricsView.getExtraLrcType() == AbstractLrcView.EXTRALRCTYPE_BOTH) {
                            //有两种歌词
                            if (mFloatLyricsView.getExtraLrcStatus() == AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC) {
                                mFloatLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                            } else if (mFloatLyricsView.getExtraLrcStatus() == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
                                mFloatLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                            } else if (mFloatLyricsView.getExtraLrcStatus() == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
                                mFloatLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);
                            }
                        } else if (mFloatLyricsView.getExtraLrcType() == AbstractLrcView.EXTRALRCTYPE_TRANSLITERATIONLRC) {
                            if (mFloatLyricsView.getExtraLrcStatus() == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
                                mFloatLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                            } else {
                                mFloatLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                            }
                        } else {
                            if (mFloatLyricsView.getExtraLrcStatus() == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
                                mFloatLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                            } else {
                                mFloatLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);
                            }
                        }

                        return;
                    }
                }
                //设置底部点击后，下沉动画
                TranslateAnimation transAnim = new TranslateAnimation(0, 0, 0, mPlayerBarParentLinearLayout.getHeight());
                transAnim.setDuration(150);
                transAnim.setFillAfter(true);
                mPlayerBarParentLinearLayout.startAnimation(transAnim);


                //
                Intent intent = new Intent(MainActivity.this, LrcActivity.class);
                startActivityForResult(intent, MAINTOLRCRESULTCODE);
                //去掉动画
                overridePendingTransition(0, 0);
            }
        });

        //
        mSongNameTextView = findViewById(R.id.songName);
        mSingerNameTextView = findViewById(R.id.singerName);
        //播放
        mPlayImageView = findViewById(R.id.bar_play);
        mPlayImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int playStatus = mHPApplication.getPlayStatus();
                if (playStatus == AudioPlayerManager.PAUSE) {

                    AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                    if (audioInfo != null) {

                        AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
                        Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_RESUMEMUSIC);
                        resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                        resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        sendBroadcast(resumeIntent);

                    }

                } else {
                    if (mHPApplication.getCurAudioMessage() != null) {
                        AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
                        AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                        if (audioInfo != null) {
                            audioMessage.setAudioInfo(audioInfo);
                            Intent playIntent = new Intent(AudioBroadcastReceiver.ACTION_PLAYMUSIC);
                            playIntent.putExtra(AudioMessage.KEY, audioMessage);
                            playIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            sendBroadcast(playIntent);
                        }
                    }
                }
            }
        });
        //暂停
        mPauseImageView = findViewById(R.id.bar_pause);
        mPauseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int playStatus = mHPApplication.getPlayStatus();
                if (playStatus == AudioPlayerManager.PLAYING) {

                    Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_PAUSEMUSIC);
                    resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(resumeIntent);

                }
            }
        });
        //下一首
        mNextImageView = findViewById(R.id.bar_next);
        mNextImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                Intent nextIntent = new Intent(AudioBroadcastReceiver.ACTION_NEXTMUSIC);
                nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(nextIntent);
            }
        });

        mMusicSeekBar = findViewById(R.id.seekBar);
        mMusicSeekBar.setOnMusicListener(new MusicSeekBar.OnMusicListener() {
            @Override
            public String getTimeText() {
                if (mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    if (mFloatLyricsView.getExtraLrcStatus() == AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC)
                        //不显示额外歌词
                        return TimeUtils.parseMMSSString(Math.max(0, mFloatLyricsView.getSplitLineLrcStartTime(mMusicSeekBar.getProgress())));
                    else
                        return TimeUtils.parseMMSSString(Math.max(0, mFloatLyricsView.getLineLrcStartTime(mMusicSeekBar.getProgress())));
                }
                return TimeUtils.parseMMSSString(mMusicSeekBar.getProgress());
            }

            @Override
            public String getLrcText() {
                if (mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    if (mFloatLyricsView.getExtraLrcStatus() == AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC)
                        //不显示额外歌词
                        return mFloatLyricsView.getSplitLineLrc(mMusicSeekBar.getProgress());
                    else
                        return mFloatLyricsView.getLineLrc(mMusicSeekBar.getProgress());
                }
                return null;
            }

            @Override
            public void onProgressChanged(MusicSeekBar musicSeekBar) {

            }

            @Override
            public void onTrackingTouchStart(MusicSeekBar musicSeekBar) {

            }

            @Override
            public void onTrackingTouchFinish(MusicSeekBar musicSeekBar) {
                int seekToTime = mMusicSeekBar.getProgress();
                mMusicSeekBar.setTrackingTouchSleepTime(1000);
                if (mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {

                    if (mFloatLyricsView.getExtraLrcStatus() == AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC)
                        //不显示额外歌词
                        seekToTime = mFloatLyricsView.getSplitLineLrcStartTime(mMusicSeekBar.getProgress());
                    else
                        seekToTime = mFloatLyricsView.getLineLrcStartTime(mMusicSeekBar.getProgress());

                    mMusicSeekBar.setTrackingTouchSleepTime(0);
                }


                int playStatus = mHPApplication.getPlayStatus();
                if (playStatus == AudioPlayerManager.PLAYING) {
                    //正在播放
                    if (mHPApplication.getCurAudioMessage() != null) {
                        AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
                        // AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                        //if (audioInfo != null) {
                        //  audioMessage.setAudioInfo(audioInfo);
                        if (audioMessage != null) {
                            audioMessage.setPlayProgress(seekToTime);
                            Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_SEEKTOMUSIC);
                            resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                            resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            sendBroadcast(resumeIntent);
                        }
                    }
                } else {

                    if (mHPApplication.getCurAudioMessage() != null)
                        mHPApplication.getCurAudioMessage().setPlayProgress(seekToTime);

                    //歌词快进
                    Intent lrcSeektoIntent = new Intent(AudioBroadcastReceiver.ACTION_LRCSEEKTO);
                    lrcSeektoIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(lrcSeektoIntent);


                }
            }
        });

        //
        ImageView listMenuImg = findViewById(R.id.list_menu);
        listMenuImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPopViewShow) {
                    hidePopView();
                    return;
                }

                showPopView();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAINTOLRCRESULTCODE) {
            if (resultCode == LRCTOMAINRESULTCODE) {


                //设置底部点击后，下沉动画
                TranslateAnimation transAnim = new TranslateAnimation(0, 0, mPlayerBarParentLinearLayout.getHeight(), 0);
                transAnim.setDuration(150);
                transAnim.setFillAfter(true);
                mPlayerBarParentLinearLayout.startAnimation(transAnim);

            }
        }
    }

    @Override
    protected boolean isAddStatusBar() {
        return false;
    }

    @Override
    protected int setContentViewId() {

        return R.layout.activity_main;
    }

    @Override
    public int setStatusBarParentView() {
        return 0;
    }


    @Override
    public void onBackPressed() {
        if (isPopViewShow) {
            hidePopView();
            return;
        }
        if (slidingMenuLayout.isMenuViewShow()) {
            slidingMenuLayout.hideMenuView(getSupportFragmentManager());
            return;
        }

        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            // Toast.makeText(getApplicationContext(), getString(R.string.back_tip), Toast.LENGTH_SHORT).show();
            ToastUtil.showTextToast(getApplicationContext(), getString(R.string.back_tip));
            mExitTime = System.currentTimeMillis();
        } else {
            // 跳转到桌面
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }

    }

    @Override
    protected void onDestroy() {

        //
        mCheckServiceHandler.removeCallbacks(mCheckServiceRunnable);
        Intent playerServiceIntent = new Intent(this, AudioPlayerService.class);
        mHPApplication.stopService(playerServiceIntent);


        //注销广播
        mAudioBroadcastReceiver.unregisterReceiver(getApplicationContext());
        //在线歌曲
        mOnLineAudioReceiver.unregisterReceiver(getApplicationContext());
        //系统广播
        mSystemReceiver.unregisterReceiver(getApplicationContext());

        if (mHPApplication.isWire())
            //耳机广播
            mPhoneReceiver.unregisterReceiver(getApplicationContext());

        //电话
        mMobliePhoneReceiver.unregisterReceiver(getApplicationContext());

        //Fragment广播
        mFragmentReceiver.unregisterReceiver(getApplicationContext());

        //锁屏广播
        mLockLrcReceiver.unregisterReceiver(getApplicationContext());

        super.onDestroy();
    }

    /**
     * 判断服务是否正在运行
     *
     * @param serviceName
     * @return
     */
    private boolean isServiceRunning(String serviceName) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否在后台运行
     *
     * @param context
     * @return
     */
    private boolean isBackgroundRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!isServiceRunning(AudioPlayerService.class.getName())) {

            if (!mHPApplication.isAppClose()) {

                //服务被强迫回收
                Intent playerServiceIntent = new Intent(this, AudioPlayerService.class);
                mHPApplication.startService(playerServiceIntent);


                mHPApplication.setPlayServiceForceDestroy(true);

                logger.e("resume时，重新启动音频播放服务广播");
            }
        }
    }

}
