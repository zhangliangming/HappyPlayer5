package com.zlm.hp.ui.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.soundcloud.android.crop.Crop;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.zlm.hp.R;
import com.zlm.hp.adapter.MainPopPlayListAdapter;
import com.zlm.hp.adapter.TabFragmentAdapter;
import com.zlm.hp.application.HPApplication;
import com.zlm.hp.db.DownloadThreadDB;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.manager.OnLineAudioManager;
import com.zlm.hp.media.lyrics.utils.LyricsUtil;
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
import com.zlm.hp.ui.fragment.DownloadMusicFragment;
import com.zlm.hp.ui.fragment.LikeMusicFragment;
import com.zlm.hp.ui.fragment.LocalMusicFragment;
import com.zlm.hp.ui.fragment.RankSongFragment;
import com.zlm.hp.ui.fragment.RecentMusicFragment;
import com.zlm.hp.ui.fragment.SearchFragment;
import com.zlm.hp.ui.fragment.TabMyFragment;
import com.zlm.hp.ui.fragment.TabRecommendFragment;
import com.zlm.hp.ui.widget.dialog.AlartTwoButtonDialog;
import com.zlm.hp.utils.ImageUtil;
import com.zlm.hp.utils.MediaUtil;
import com.zlm.hp.utils.NaviMenuHelper;
import com.zlm.hp.utils.QuitTimer;
import com.zlm.hp.utils.ToastShowUtil;

import java.util.ArrayList;
import java.util.List;

import base.utils.ColorUtil;
import base.utils.ThreadUtil;
import base.utils.ToastUtil;
import base.widget.CircleImageView;
import base.widget.IconfontImageButtonTextView;
import base.widget.IconfontIndicatorTextView;
import base.widget.IconfontTextView;
import base.widget.LinearLayoutRecyclerView;
import base.widget.LrcSeekBar;
import base.widget.SlidingMenuLayout;
import base.widget.SwipeoutLayout;
import base.widget.lrc.FloatLyricsView;

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
     * 从相册中选择，换肤
     */
    public static final int PHOTO_REQUEST_GALLERY = 2;

    /**
     * 保存退出时间
     */
    private long mExitTime;

    //侧边栏
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MenuItem timerItem;

    //////////////////////////标题栏/////////////////////////////////////////////////

    /**
     * 图标按钮
     */
    private ImageView mIconButton;


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
    private SwipeoutLayout mSwipeoutLayout;
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
    private LrcSeekBar mLrcSeekBar;
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
     * 广播监听
     */
    private AudioBroadcastReceiver.AudioReceiverListener mAudioReceiverListener = new AudioBroadcastReceiver.AudioReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doAudioReceive(context, intent);
        }
    };

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

    private LockLrcReceiver mLockLrcReceiver;
    private LockLrcReceiver.LockLrcReceiverListener mLockLrcReceiverListener = new LockLrcReceiver.LockLrcReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doLockLrcReceive(context, intent);
        }
    };


    /**
     *
     */
    private Handler mCheckServiceHandler = new Handler();
    /**
     * 检测时间
     */
    private int mCheckServiceTime = 500;
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
            if (HPApplication.getInstance().getPlayStatus() == AudioPlayerManager.PLAYING ||
                    HPApplication.getInstance().getPlayStatus() == AudioPlayerManager.PLAYNET) {
                if (HPApplication.getInstance().getCurAudioMessage() != null &&
                        HPApplication.getInstance().getCurAudioInfo() != null) {
                    if (!mCurPlayIndexHash.equals(HPApplication.getInstance().getCurAudioInfo().getHash())) {

                        Intent initIntent = new Intent(AudioBroadcastReceiver.ACTION_INITMUSIC);
                        initIntent.putExtra(AudioMessage.KEY, HPApplication.getInstance().getCurAudioMessage());
                        initIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        sendBroadcast(initIntent);

                    }
                }
            }

            if (!isServiceRunning(AudioPlayerService.class.getName())) {
                logger.e("监听音频服务初始回收");
                if (!HPApplication.getInstance().isAppClose()) {

                    //服务被强迫回收
                    Intent playerServiceIntent = new Intent(getApplicationContext(), AudioPlayerService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        HPApplication.getInstance().startForegroundService(playerServiceIntent);
                    } else {
                        HPApplication.getInstance().startService(playerServiceIntent);
                    }

                    HPApplication.getInstance().setPlayServiceForceDestroy(true);
//                    Intent restartIntent = new Intent(AudioBroadcastReceiver.ACTION_MUSICRESTART);
//                    restartIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//                    sendBroadcast(restartIntent);
                    logger.e("发送重新启动音频播放服务广播");
                }
            }
            mCheckServiceHandler.postDelayed(mCheckServiceRunnable, mCheckServiceTime);
        }
    };

    ///////////////////////////pop///////////////////////////////////////

    /**
     * 弹出窗口是否显示
     */
    private boolean isPopViewShow = false;
    /**
     * 弹出窗口全屏界面
     */
    private LinearLayout mListPopLinearLayout;
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
     * 当前播放列表歌曲总数
     */
    private TextView mCurPLSizeTv;

    //播放模式
    private IconfontTextView modeAllTv;
    private IconfontTextView modeRandomTv;
    private IconfontTextView modeSingleTv;

    //删除
    private IconfontTextView mDeleteTv;

    @Override
    protected int setContentViewId() {

        return R.layout.activity_main;
    }

    @Override
    protected boolean isAddStatusBar() {
        return false;
    }

    @Override
    public int setStatusBarParentView() {
        return 0;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        //初始化标题栏视图
        initTitleViews();

        //初始化侧边栏
        initNavigation();

        //初始化中间视图
        initPageViews();

        //初始化底部播放器视图
        initPlayerViews();

        //初始化播放列表
        initListPopView();

        //初始化服务
        initService();

    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        if (!isRestoreInstance) {
            ThreadUtil.runInThread(new Runnable() {
                @Override
                public void run() {
                    AudioPlayerManager.getAudioPlayerManager(mContext).initSongInfoData();
                }
            });
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AndPermission.with(mActivity)
                    .requestCode(110)
                    .permission(Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_SMS)
                    .callback(new PermissionListener() {
                        @Override
                        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {

                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            AlartTwoButtonDialog mExitAlartDialog = new AlartTwoButtonDialog(mActivity, new AlartTwoButtonDialog.TwoButtonDialogListener() {
                                @Override
                                public void onLeftButtonClick() {

                                }

                                @Override
                                public void onRightButtonClick() {

                                }
                            });
                            mExitAlartDialog.showDialog(mContext.getString(R.string.need_access_read_sms),
                                    mContext.getString(R.string.cancel),
                                    mContext.getString(R.string.sure));
                        }
                    }).start();
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
            int playStatus = HPApplication.getInstance().getPlayStatus();
            if (playStatus == AudioPlayerManager.PLAYING) {

                Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_PAUSEMUSIC);
                resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(resumeIntent);

            }
        } else if(action.equals(SystemReceiver.ACTION_OPENLRCMESSAGE)) {
            //打开锁屏歌词
            mLockLrcReceiver.registerReceiver(getApplicationContext());
        } else if(action.equals(SystemReceiver.ACTION_CLOSELRCMESSAGE)) {
            //关闭锁屏歌词
            mLockLrcReceiver.unregisterReceiver(getApplicationContext());
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
            if (HPApplication.getInstance().getPlayIndexHashID().equals(downloadMessage.getTaskId())) {
                int downloadedSize = DownloadThreadDB.getDownloadThreadDB(getApplicationContext()).getDownloadedSize(downloadMessage.getTaskId(), OnLineAudioManager.threadNum);
                double pre = downloadedSize * 1.0 / HPApplication.getInstance().getCurAudioInfo().getFileSize();
                int downloadProgress = (int) (mLrcSeekBar.getMax() * pre);
                mLrcSeekBar.setSecondaryProgress(downloadProgress);
            }
        } else if (action.equals(OnLineAudioReceiver.ACTION_ONLINEMUSICERROR)) {
            DownloadMessage downloadMessage = (DownloadMessage) intent.getSerializableExtra(DownloadMessage.KEY);
            if (HPApplication.getInstance().getPlayIndexHashID().equals(downloadMessage.getTaskId())) {
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
            mLrcSeekBar.setEnabled(false);
            mLrcSeekBar.setProgress(0);
            mLrcSeekBar.setSecondaryProgress(0);
            mLrcSeekBar.setMax(0);
            //隐藏
            mSingerImg.setTag(null);

            //
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.singer_def);
            mSingerImg.setImageDrawable(new BitmapDrawable(bitmap));

            //
            mFloatLyricsView.setLyricsUtil(null, 0);

            //重置弹出窗口播放列表
            if (isPopViewShow) {
                if (mPopPlayListAdapter != null) {
                    mPopPlayListAdapter.reshViewHolder(null);
                }
            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_INITMUSIC)) {


            //初始化
            AudioMessage audioMessage = HPApplication.getInstance().getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            AudioInfo audioInfo = HPApplication.getInstance().getCurAudioInfo();

            mCurPlayIndexHash = audioInfo.getHash();

            mSongNameTextView.setText(audioInfo.getSongName());
            mSingerNameTextView.setText(audioInfo.getSingerName());
            mPauseImageView.setVisibility(View.INVISIBLE);
            mPlayImageView.setVisibility(View.VISIBLE);

            //
            mLrcSeekBar.setEnabled(true);
            mLrcSeekBar.setMax((int) audioInfo.getDuration());
            mLrcSeekBar.setProgress((int) audioMessage.getPlayProgress());
            mLrcSeekBar.setSecondaryProgress(0);
            //加载歌手图片
            ImageUtil.loadSingerImage(mContext, mSingerImg, audioInfo.getSingerName());

            //加载歌词
            String keyWords = "";
            if (audioInfo.getSingerName().equals("未知")) {
                keyWords = audioInfo.getSongName();
            } else {
                keyWords = audioInfo.getSingerName() + " - " + audioInfo.getSongName();
            }
            LyricsManager.getLyricsManager(mContext).loadLyricsUtil(keyWords, keyWords, audioInfo.getDuration() + "", audioInfo.getHash());

            //
            mFloatLyricsView.setLyricsUtil(null, 0);

            //设置弹出窗口播放列表
            if (isPopViewShow) {
                if (mPopPlayListAdapter != null) {
                    mPopPlayListAdapter.reshViewHolder(audioInfo);
                }
            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYMUSIC)) {
            //播放

            AudioMessage audioMessage = HPApplication.getInstance().getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);

            mPauseImageView.setVisibility(View.VISIBLE);
            mPlayImageView.setVisibility(View.INVISIBLE);

            //
            mLrcSeekBar.setProgress((int) audioMessage.getPlayProgress());

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PAUSEMUSIC)) {
            //暂停完成
            mPauseImageView.setVisibility(View.INVISIBLE);
            mPlayImageView.setVisibility(View.VISIBLE);

        } else if (action.equals(AudioBroadcastReceiver.ACTION_CANCELNOTIFICATION)) {
            //暂停完成
            mPauseImageView.setVisibility(View.INVISIBLE);
            mPlayImageView.setVisibility(View.VISIBLE);

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_RESUMEMUSIC)) {
            //唤醒完成
            mPauseImageView.setVisibility(View.VISIBLE);
            mPlayImageView.setVisibility(View.INVISIBLE);

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYINGMUSIC)) {
            //播放中
            AudioMessage audioMessage = HPApplication.getInstance().getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            if (audioMessage != null) {
                mLrcSeekBar.setProgress((int) audioMessage.getPlayProgress());
                AudioInfo audioInfo = HPApplication.getInstance().getCurAudioInfo();
                if (audioInfo != null) {
                    //更新歌词
                    if (mFloatLyricsView.getLyricsUtil() != null && mFloatLyricsView.getLyricsUtil().getHash().equals(audioInfo.getHash())) {
                        mFloatLyricsView.updateView((int) audioMessage.getPlayProgress());
                    }
                }

            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_LOCALUPDATE)) {
            //
            //更新当前的播放列表
//            List<AudioInfo> data = AudioInfoDB.getAudioInfoDB(getApplicationContext()).getAllLocalAudio();
//            mHPApplication.setCurAudioInfos(data);

        }
//        else if (action.equals(AudioBroadcastReceiver.ACTION_MUSICRESTART)) {
        //重新启动播放服务
//            Intent playerServiceIntent = new Intent(this, AudioPlayerService.class);
//            mHPApplication.startService(playerServiceIntent);
//            logger.e("接收广播并且重新启动音频播放服务");

//        }
        else if (action.equals(AudioBroadcastReceiver.ACTION_LRCLOADED)) {
            if (HPApplication.getInstance().getCurAudioMessage() != null &&
                    HPApplication.getInstance().getCurAudioInfo() != null) {
                //歌词加载完成
                AudioMessage curAudioMessage = HPApplication.getInstance().getCurAudioMessage();
                AudioMessage audioMessage = (AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
                String hash = audioMessage.getHash();
                if (hash.equals(HPApplication.getInstance().getCurAudioInfo().getHash())) {
                    //
                    LyricsUtil lyricsUtil = LyricsManager.getLyricsManager(mContext).getLyricsUtil(hash);
                    if (lyricsUtil != null) {
                        if (lyricsUtil.getHash() != null && lyricsUtil.getHash().equals(hash) && mFloatLyricsView.getLyricsUtil() != null) {
                            //已加载歌词，不用重新加载
                        } else {
                            lyricsUtil.setHash(hash);
                            mFloatLyricsView.setLyricsUtil(lyricsUtil, mFloatLyricsView.getWidth() / 4 * 3);
                            mFloatLyricsView.updateView((int) curAudioMessage.getPlayProgress());
                        }
                    }
                }
            }
        } else if (action.equals(AudioBroadcastReceiver.ACTION_LRCSEEKTO)) {
            if (HPApplication.getInstance().getCurAudioMessage() != null) {
                mLrcSeekBar.setProgress((int) HPApplication.getInstance().getCurAudioMessage().getPlayProgress());
                if (HPApplication.getInstance().getCurAudioInfo() != null) {
                    if (mFloatLyricsView.getLyricsUtil() != null &&
                            mFloatLyricsView.getLyricsUtil().getHash().
                                    equals(HPApplication.getInstance().getCurAudioInfo().getHash())) {
                        mFloatLyricsView.updateView((int) HPApplication.getInstance().getCurAudioMessage().getPlayProgress());
                    }
                }
            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_UPDATESTOPPLAYTIME)) {
            //更新定时停止播放时间
            if (timerItem == null) {
                timerItem = navigationView.getMenu().findItem(R.id.action_timer);
            }
            long remain = intent.getLongExtra(AudioBroadcastReceiver.ACTION_SERVICE_UPDATESTOPPLAYTIME, 0L);
            String title = getString(R.string.menu_timer);
            timerItem.setTitle(remain == 0 ? title : QuitTimer.formatTime(title + "(mm:ss)", remain));
        }
    }

    /**
     * 处理锁屏歌词广播事件
     *
     * @param context
     * @param intent
     */
    private void doLockLrcReceive(Context context, Intent intent) {
        String action = intent.getAction();
//        if (action.equals(LockLrcReceiver.ACTION_OPENLRCMESSAGE)) {
//            //打开锁屏歌词
//            mLockLrcReceiver.registerReceiver(getApplicationContext());
//        } else if (action.equals(LockLrcReceiver.ACTION_CLOSELRCMESSAGE)) {
//            //关闭锁屏歌词
//            mLockLrcReceiver.unregisterReceiver(getApplicationContext());
//        }
        boolean showLockScreen = HPApplication.getInstance().isShowLockScreen();
        if (showLockScreen && action.equals(Intent.ACTION_SCREEN_OFF) && HPApplication.getInstance().getPlayStatus() == AudioPlayerManager.PLAYING) {
            Intent lockscreen = new Intent(this, LockScreenActivity.class);
            //在Service中启动一个Activity, 需要加上Intent.FLAG_ACTIVITY_NEW_TASK
            lockscreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(lockscreen);
        }
    }

    /**
     * 初始化服务
     */
    private void initService() {
        Intent playerServiceIntent = new Intent(this, AudioPlayerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            HPApplication.getInstance().startForegroundService(playerServiceIntent);
        } else {
            HPApplication.getInstance().startService(playerServiceIntent);
        }

        //注册接收音频播放广播
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(getApplicationContext());
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(getApplicationContext());

        //在线音乐广播
        mOnLineAudioReceiver = new OnLineAudioReceiver(getApplicationContext());
        mOnLineAudioReceiver.setOnlineAudioReceiverListener(mOnlineAudioReceiverListener);
        mOnLineAudioReceiver.registerReceiver(getApplicationContext());

        //系统广播
        mSystemReceiver = new SystemReceiver(getApplicationContext());
        mSystemReceiver.setSystemReceiverListener(mSystemReceiverListener);
        mSystemReceiver.registerReceiver(getApplicationContext());

        //耳机广播
        mPhoneReceiver = new PhoneReceiver();
        if (HPApplication.getInstance().isWire()) {
            mPhoneReceiver.registerReceiver(getApplicationContext());
        }

        //电话监听
        mMobliePhoneReceiver = new MobliePhoneReceiver(getApplicationContext());
        mMobliePhoneReceiver.registerReceiver(getApplicationContext());

        //mFragment广播
        mFragmentReceiver = new FragmentReceiver(getApplicationContext());
        mFragmentReceiver.setFragmentReceiverListener(mFragmentReceiverListener);
        mFragmentReceiver.registerReceiver(getApplicationContext());

        //注册锁屏歌词广播
        mLockLrcReceiver = new LockLrcReceiver(getApplicationContext());
        mLockLrcReceiver.setLockLrcReceiverListener(mLockLrcReceiverListener);
        if (HPApplication.getInstance().isLockScreen()) {
            mLockLrcReceiver.registerReceiver(getApplicationContext());
        }

        //
        mCheckServiceHandler.postDelayed(mCheckServiceRunnable, mCheckServiceTime);
    }

    private void initNavigation() {
        //侧边栏
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // add navigation header
        View vNavigationHeader = LayoutInflater.from(this).inflate(R.layout.navigation_header, navigationView, false);
        navigationView.addHeaderView(vNavigationHeader);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
                drawerLayout.closeDrawers();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        item.setChecked(false);
                    }
                }, 500);
                return NaviMenuHelper.onNavigationItemSelected(item, mActivity);
            }
        });
    }

    /**
     * 初始化标题栏视图
     */
    private void initTitleViews() {
        //图标
        mIconButton = findViewById(R.id.iv_menu);
        mIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
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
                //不允许拖动
//                slidingMenuLayout.setAllowDrag(false);
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
        initPlayModeView(HPApplication.getInstance().getPlayModel(), modeAllTv, modeRandomTv, modeSingleTv, false);

        //删除播放列表按钮
        mDeleteTv = findViewById(R.id.delete);
        mDeleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
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
                ToastUtil.showTextToast(MainActivity.this, mContext.getString(R.string.order_play));
            modeAllImg.setVisibility(View.VISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else if (playMode == 1) {
            if (isTipShow)
                ToastUtil.showTextToast(MainActivity.this, mContext.getString(R.string.random_play));
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.VISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else {
            if (isTipShow)
                ToastUtil.showTextToast(MainActivity.this, mContext.getString(R.string.single_play));
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.VISIBLE);
        }
        //
        HPApplication.getInstance().setPlayModel(playMode);
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

        initPlayModeView(HPApplication.getInstance().getPlayModel(), modeAllTv, modeRandomTv, modeSingleTv, false);
        //加载当前播放列表数据
        List<AudioInfo> curAudioInfos = HPApplication.getInstance().getCurAudioInfos();
        if (curAudioInfos == null) {
            curAudioInfos = new ArrayList<AudioInfo>();
        }
        mCurPLSizeTv.setText(curAudioInfos.size() + "");
        mPopPlayListAdapter = new MainPopPlayListAdapter(mContext, curAudioInfos);
        mCurRecyclerView.setAdapter(mPopPlayListAdapter);


        //
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mPopMenuRelativeLayout.getHeight(), 0);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mListPopLinearLayout.setBackgroundColor(ColorUtil.parserColor(Color.BLACK, 0));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mListPopLinearLayout.setBackgroundColor(ColorUtil.parserColor(Color.BLACK, 120));

                //滚动到当前播放位置
                int position = mPopPlayListAdapter.getPlayIndexPosition(HPApplication.getInstance().getCurAudioInfo());
                if (position >= 0)
                    mCurRecyclerView.move(position,
                            LinearLayoutRecyclerView.smoothScroll);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mListPopLinearLayout.setVisibility(View.VISIBLE);
        mPopMenuRelativeLayout.clearAnimation();
        mPopMenuRelativeLayout.setAnimation(translateAnimation);
        translateAnimation.start();
        isPopViewShow = true;


    }


    /**
     * 初始化底部播放器视图
     */
    private void initPlayerViews() {

        //
        mPlayerBarParentLinearLayout = findViewById(R.id.playerBarParent);

        mSwipeoutLayout = findViewById(R.id.playerBar);
        mSwipeoutLayout.setBackgroundColor(ColorUtil.parserColor("#ffffff", 235));
        ViewGroup barContentView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.layout_main_player_content, null);
        barContentView.setBackgroundColor(Color.TRANSPARENT);

        ViewGroup barMenuView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.layout_main_player_menu, null);
        barMenuView.setBackgroundColor(Color.TRANSPARENT);
        //
        mFloatLyricsView = barMenuView.findViewById(R.id.floatLyricsView);

        //歌手头像
        mSingerImg = barContentView.findViewById(R.id.play_bar_artist);
        mSingerImg.setTag(null);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.singer_def);
        mSingerImg.setImageDrawable(new BitmapDrawable(bitmap));


        mBarCloseFlagView = barContentView.findViewById(R.id.bar_dragflagClosed);
        mBarOpenFlagView = barContentView.findViewById(R.id.bar_dragflagOpen);
        //
        if (HPApplication.getInstance().isBarMenuShow()) {
            mSwipeoutLayout.initViewAndShowMenuView(barContentView, barMenuView, mSingerImg);
        } else {
            mSwipeoutLayout.initViewAndShowContentView(barContentView, barMenuView, mSingerImg);
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
        mSwipeoutLayout.setPlayerBarListener(new SwipeoutLayout.PlayerBarListener() {
            @Override
            public void onClose() {
//                if (mBarCloseFlagView.getVisibility() != View.VISIBLE) {
//                    mBarCloseFlagView.setVisibility(View.VISIBLE);
//                }

                if (mBarOpenFlagView.getVisibility() != View.INVISIBLE) {
                    mBarOpenFlagView.setVisibility(View.INVISIBLE);
                }

                //
                HPApplication.getInstance().setBarMenuShow(false);
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
                HPApplication.getInstance().setBarMenuShow(true);
            }
        });
        mSwipeoutLayout.setPlayerBarOnClickListener(new SwipeoutLayout.PlayerBarOnClickListener() {
            @Override
            public void onClick() {

                if (isPopViewShow) {
                    hidePopView();
                    return;
                }

                //设置底部点击后，下沉动画
                TranslateAnimation transAnim = new TranslateAnimation(0, 0, 0, mPlayerBarParentLinearLayout.getHeight());
                transAnim.setDuration(500);
                transAnim.setFillAfter(true);
                mPlayerBarParentLinearLayout.setAnimation(transAnim);
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
                int playStatus = HPApplication.getInstance().getPlayStatus();
                if (playStatus == AudioPlayerManager.PAUSE) {

                    AudioInfo audioInfo = HPApplication.getInstance().getCurAudioInfo();
                    if (audioInfo != null) {

                        AudioMessage audioMessage = HPApplication.getInstance().getCurAudioMessage();
                        Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_RESUMEMUSIC);
                        resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                        resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        sendBroadcast(resumeIntent);

                    }

                } else {
                    if (HPApplication.getInstance().getCurAudioMessage() != null) {
                        AudioMessage audioMessage = HPApplication.getInstance().getCurAudioMessage();
                        AudioInfo audioInfo = HPApplication.getInstance().getCurAudioInfo();
                        if (audioInfo != null) {
                            audioMessage.setAudioInfo(audioInfo);
                            Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_PLAYMUSIC);
                            resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                            resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            sendBroadcast(resumeIntent);
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
                int playStatus = HPApplication.getInstance().getPlayStatus();
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

        mLrcSeekBar = findViewById(R.id.seekBar);
        mLrcSeekBar.setOnChangeListener(new LrcSeekBar.OnChangeListener() {

            @Override
            public void onProgressChanged() {

            }

            @Override
            public String getTimeText() {
                return MediaUtil.parseTimeToString(mLrcSeekBar.getProgress());
            }

            @Override
            public String getLrcText() {

                //获取行歌词
                if (mFloatLyricsView.getLyricsUtil() != null &&
                        mFloatLyricsView.getLyricsUtil().getHash().
                                equals(HPApplication.getInstance().getCurAudioMessage().getAudioInfo().getHash())) {
                    return mFloatLyricsView.getLyricsUtil().getLineLrc(mFloatLyricsView.getLyricsLineTreeMap(), mLrcSeekBar.getProgress());
                }

                return null;
            }

            @Override
            public void dragFinish() {
                //
                int playStatus = HPApplication.getInstance().getPlayStatus();
                if (playStatus == AudioPlayerManager.PLAYING) {
                    //正在播放
                    if (HPApplication.getInstance().getCurAudioMessage() != null) {
                        AudioMessage audioMessage = HPApplication.getInstance().getCurAudioMessage();
                        // AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                        //if (audioInfo != null) {
                        //  audioMessage.setAudioInfo(audioInfo);
                        if (audioMessage != null) {
                            audioMessage.setPlayProgress(mLrcSeekBar.getProgress());
                            Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_SEEKTOMUSIC);
                            resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                            resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            sendBroadcast(resumeIntent);
                        }
                    }
                } else {

                    if (HPApplication.getInstance().getCurAudioMessage() != null)
                        HPApplication.getInstance().getCurAudioMessage().setPlayProgress(mLrcSeekBar.getProgress());

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
                mPlayerBarParentLinearLayout.setAnimation(transAnim);
                mPlayerBarParentLinearLayout.startAnimation(transAnim);

            }
        }else if(requestCode == PHOTO_REQUEST_GALLERY) {
            Uri output = Crop.getOutput(data);
            if(!TextUtils.isEmpty(output.getPath())){
                Bitmap bitmap = BitmapFactory.decodeFile(output.getPath());

            }
        }
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
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
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
        stopService(playerServiceIntent);

        //注销广播
        mAudioBroadcastReceiver.unregisterReceiver(getApplicationContext());
        //在线歌曲
        mOnLineAudioReceiver.unregisterReceiver(getApplicationContext());
        //系统广播
        mSystemReceiver.unregisterReceiver(getApplicationContext());

        if (HPApplication.getInstance().isWire())
            //耳机广播
            mPhoneReceiver.unregisterReceiver(getApplicationContext());

        //电话
        mMobliePhoneReceiver.unregisterReceiver(getApplicationContext());

        //Fragment广播
        mFragmentReceiver.unregisterReceiver(getApplicationContext());

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

    @Override
    protected void onResume() {
        super.onResume();
        if (!isServiceRunning(AudioPlayerService.class.getName())) {

            if (!HPApplication.getInstance().isAppClose()) {

                //服务被强迫回收
                Intent playerServiceIntent = new Intent(this, AudioPlayerService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    HPApplication.getInstance().startForegroundService(playerServiceIntent);
                } else {
                    HPApplication.getInstance().startService(playerServiceIntent);
                }
                HPApplication.getInstance().setPlayServiceForceDestroy(true);

                logger.e("resume时，重新启动音频播放服务广播");
            }
        }
    }

}
