package com.zlm.hp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.happy.lyrics.model.LyricsInfo;
import com.happy.lyrics.model.LyricsTag;
import com.happy.lyrics.utils.LyricsIOUtils;
import com.zlm.hp.adapter.LrcPopPlayListAdapter;
import com.zlm.hp.adapter.LrcPopSingerListAdapter;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.db.DownloadThreadDB;
import com.zlm.hp.db.SongSingerDB;
import com.zlm.hp.libs.utils.ColorUtil;
import com.zlm.hp.libs.utils.ToastUtil;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.model.DownloadMessage;
import com.zlm.hp.model.SongSingerInfo;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.OnLineAudioReceiver;
import com.zlm.hp.utils.ImageUtil;
import com.zlm.hp.utils.LyricsUtil;
import com.zlm.hp.utils.MediaUtil;
import com.zlm.hp.widget.ButtonRelativeLayout;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.IconfontTextView;
import com.zlm.hp.widget.LinearLayoutRecyclerView;
import com.zlm.hp.widget.LrcSeekBar;
import com.zlm.hp.widget.PlayListBGRelativeLayout;
import com.zlm.hp.widget.RotateLinearLayout;
import com.zlm.hp.widget.SingerImageView;
import com.zlm.hp.widget.lrc.ManyLineLyricsView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 歌词界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/24 21:10
 * @Throws:
 */
public class LrcActivity extends BaseActivity {
    /**
     * 歌词界面跳转到主界面的code
     */
    private final int LRCTOMAINRESULTCODE = 1;
    /**
     * 旋转布局界面
     */
    private RotateLinearLayout mRotateLinearLayout;
    private LinearLayout mLrcPlaybarLinearLayout;

    /**
     * 歌曲名称tv
     */
    private TextView mSongNameTextView;
    /**
     * 歌手tv
     */
    private TextView mSingerNameTextView;
    ////////////////////////////底部

    private LrcSeekBar mLrcSeekBar;
    /**
     * 播放
     */
    private RelativeLayout mPlayBtn;
    /**
     * 暂停
     */
    private RelativeLayout mPauseBtn;
    /**
     * 下一首
     */
    private RelativeLayout mNextBtn;

    /**
     * 上一首
     */
    private RelativeLayout mPreBtn;
    /**
     * 播放进度
     */
    private TextView mSongProgressTv;

    /**
     * 歌曲总长度
     */
    private TextView mSongDurationTv;

    private ManyLineLyricsView mManyLineLyricsView;
    /**
     * 歌手写真
     */
    private SingerImageView mSingerImageView;

    //播放模式
    private ImageView modeAllImg;
    private ImageView modeRandomImg;
    private ImageView modeSingleImg;

    /**
     * 屏幕宽度
     */
    private int mScreensWidth;


    /////////////////////////////菜单///////////////////////////
    private IconfontImageButtonTextView mMoreMenuImgBtn;
    private IconfontImageButtonTextView mDownloadImgBtn;
    private IconfontImageButtonTextView mLikeImgBtn;
    private IconfontImageButtonTextView mUnLikeImgBtn;

    //、、、、、、、、、、、、、、、、、、更多弹出窗口、、、、、、、、、、、、、、、、、、、、、、、、、、
    private boolean isPopViewShow = false;
    private LinearLayout mPopLinearLayout;
    private LinearLayout mMenuLayout;

    //、、、、、、、、、、、、、、、、、、、当前播放列表窗口、、、、、、、、、、、、、、、、、、、、、、、、
    private boolean isPLPopViewShow = false;
    private LinearLayout mPlPopLinearLayout;
    private PlayListBGRelativeLayout mPlPLayout;

    //、、、、、、、、、、、、、、、、、、、、、、歌手列表、、、、、、、、、、、、、、、、、、、、、、、、
    private boolean isSPLPopViewShow = false;
    private LinearLayout mSPlPopLinearLayout;
    private PlayListBGRelativeLayout mSPlPLayout;
    private LinearLayoutRecyclerView mSingerNameRecyclerView;
    private LrcPopSingerListAdapter mLrcPopSingerListAdapter;
    private LrcActivityListener mLrcActivityListener = new LrcActivityListener() {
        @Override
        public void closeSingerPopListVeiw() {
            hideSPLPopView();
        }
    };

    //、、、、、、、、、、、、、、、、、、、当前歌曲信息窗口、、、、、、、、、、、、、、、、、、、、、、、、
    private boolean isSIPopViewShow = false;
    private LinearLayout mSIPopLinearLayout;
    private PlayListBGRelativeLayout mSIPLayout;
    private TextView mPopSingerNameTv;
    private TextView mPopFileExtTv;
    private TextView mPopTimeTv;
    private TextView mPopFileSizeTv;

    //、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、
    //播放模式
    private IconfontTextView modeAllTv;
    private IconfontTextView modeRandomTv;
    private IconfontTextView modeSingleTv;

    /**
     * 当前播放列表
     */
    private LinearLayoutRecyclerView mCurRecyclerView;

    /**
     *
     */
    private LrcPopPlayListAdapter mPopPlayListAdapter;
    /**
     * 当前播放列表歌曲总数
     */
    private TextView mCurPLSizeTv;

    /////////////////////////////////////////////////////////////////////////////////

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
                int downloadedSize = DownloadThreadDB.getDownloadThreadDB(getApplicationContext()).getDownloadedSize(downloadMessage.getTaskId());
                double pre = downloadedSize * 1.0 / mHPApplication.getCurAudioInfo().getFileSize();
                int downloadProgress = (int) (mLrcSeekBar.getMax() * pre);
                mLrcSeekBar.setSecondaryProgress(downloadProgress);
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
            mPauseBtn.setVisibility(View.INVISIBLE);
            mPlayBtn.setVisibility(View.VISIBLE);

            mSongProgressTv.setText("00:00");
            mSongDurationTv.setText("00:00");

            //
            mLrcSeekBar.setEnabled(false);
            mLrcSeekBar.setProgress(0);
            mLrcSeekBar.setSecondaryProgress(0);
            mLrcSeekBar.setMax(0);

            //
            mManyLineLyricsView.setLyricsUtil(null, 0);
            //歌手写真
            mSingerImageView.setVisibility(View.INVISIBLE);
            mSingerImageView.setSongSingerInfos(mHPApplication, getApplicationContext(), null);

            //重置弹出窗口播放列表
            if (isPLPopViewShow) {
                if (mPopPlayListAdapter != null) {
                    mPopPlayListAdapter.reshViewHolder(null);
                }
            }

            //设置喜欢
            mUnLikeImgBtn.setVisibility(View.VISIBLE);
            mLikeImgBtn.setVisibility(View.GONE);

        } else if (action.equals(AudioBroadcastReceiver.ACTION_INITMUSIC)) {
            //初始化
            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            AudioInfo audioInfo = mHPApplication.getCurAudioInfo();

            mSongNameTextView.setText(audioInfo.getSongName());
            mSingerNameTextView.setText(audioInfo.getSingerName());

            if (mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYING) {
                mPauseBtn.setVisibility(View.VISIBLE);
                mPlayBtn.setVisibility(View.GONE);
            } else {
                mPauseBtn.setVisibility(View.GONE);
                mPlayBtn.setVisibility(View.VISIBLE);
            }


            //
            mSongProgressTv.setText(MediaUtil.parseTimeToString((int) audioMessage.getPlayProgress()));
            mSongDurationTv.setText(MediaUtil.parseTimeToString((int) audioInfo.getDuration()));
            //
            mLrcSeekBar.setEnabled(true);
            mLrcSeekBar.setMax((int) audioInfo.getDuration());
            mLrcSeekBar.setProgress((int) audioMessage.getPlayProgress());
            mLrcSeekBar.setSecondaryProgress(0);

            //加载歌词
            String keyWords = "";
            if (audioInfo.getSingerName().equals("未知")) {
                keyWords = audioInfo.getSongName();
            } else {
                keyWords = audioInfo.getSingerName() + " - " + audioInfo.getSongName();
            }
            LyricsManager.getLyricsManager(mHPApplication, getApplicationContext()).loadLyricsUtil(keyWords, keyWords, audioInfo.getDuration() + "", audioInfo.getHash());

            //
            mManyLineLyricsView.setLyricsUtil(null, 0);

            //设置弹出窗口播放列表
            if (isPLPopViewShow) {
                if (mPopPlayListAdapter != null) {
                    mPopPlayListAdapter.reshViewHolder(audioInfo);
                }
            }

            mSingerImageView.setVisibility(View.INVISIBLE);
            mSingerImageView.setSongSingerInfos(mHPApplication, getApplicationContext(), null);
            //加载歌手写真
            ImageUtil.loadSingerImg(mHPApplication, getApplicationContext(), audioInfo.getHash(), audioInfo.getSingerName());


            //设置喜欢
            boolean isLike = AudioInfoDB.getAudioInfoDB(getApplicationContext()).isRecentOrLikeExists(audioInfo.getHash(), audioInfo.getType(), false);
            if (isLike) {
                mUnLikeImgBtn.setVisibility(View.GONE);
                mLikeImgBtn.setVisibility(View.VISIBLE);
            } else {
                mUnLikeImgBtn.setVisibility(View.VISIBLE);
                mLikeImgBtn.setVisibility(View.GONE);
            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYMUSIC)) {
            //播放

            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);

            mPauseBtn.setVisibility(View.VISIBLE);
            mPlayBtn.setVisibility(View.INVISIBLE);

            //
            mSongProgressTv.setText(MediaUtil.parseTimeToString((int) audioMessage.getPlayProgress()));
            //
            mLrcSeekBar.setProgress((int) audioMessage.getPlayProgress());

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PAUSEMUSIC)) {
            //暂停完成
            mPauseBtn.setVisibility(View.INVISIBLE);
            mPlayBtn.setVisibility(View.VISIBLE);

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_RESUMEMUSIC)) {
            //唤醒完成
            mPauseBtn.setVisibility(View.VISIBLE);
            mPlayBtn.setVisibility(View.INVISIBLE);

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYINGMUSIC)) {
            //播放中
            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            if (audioMessage != null) {
                mSongProgressTv.setText(MediaUtil.parseTimeToString((int) audioMessage.getPlayProgress()));
                mLrcSeekBar.setProgress((int) audioMessage.getPlayProgress());
                AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                if (audioInfo != null) {
                    //更新歌词
                    if (mManyLineLyricsView.getLyricsUtil() != null && mManyLineLyricsView.getLyricsUtil().getHash().equals(audioInfo.getHash())) {
                        mManyLineLyricsView.updateView((int) audioMessage.getPlayProgress());
                    }
                }

            }

        }
//        else if (action.equals(AudioBroadcastReceiver.ACTION_MUSICRESTART)) {
        //重新启动播放服务
//            Intent playerServiceIntent = new Intent(this, AudioPlayerService.class);
//            mHPApplication.startService(playerServiceIntent);
//            logger.e("接收广播并且重新启动音频播放服务");

//        }
        else if (action.equals(AudioBroadcastReceiver.ACTION_LRCLOADED)) {
            //歌词加载完成
            AudioMessage curAudioMessage = mHPApplication.getCurAudioMessage();
            AudioMessage audioMessage = (AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            String hash = audioMessage.getHash();
            if (hash.equals(mHPApplication.getCurAudioInfo().getHash())) {
                //
                LyricsUtil lyricsUtil = LyricsManager.getLyricsManager(mHPApplication, getApplicationContext()).getLyricsUtil(hash);
                if (lyricsUtil != null) {
                    lyricsUtil.setHash(hash);
                    mManyLineLyricsView.setLyricsUtil(lyricsUtil, mScreensWidth / 4 * 3);
                    mManyLineLyricsView.updateView((int) curAudioMessage.getPlayProgress());
                }
            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_LRCSEEKTO)) {
            //歌词快进
            if (mHPApplication.getCurAudioMessage() != null) {
                mSongProgressTv.setText(MediaUtil.parseTimeToString((int) mHPApplication.getCurAudioMessage().getPlayProgress()));
                mLrcSeekBar.setProgress((int) mHPApplication.getCurAudioMessage().getPlayProgress());
                if (mHPApplication.getCurAudioInfo() != null) {
                    if (mManyLineLyricsView.getLyricsUtil() != null && mManyLineLyricsView.getLyricsUtil().getHash().equals(mHPApplication.getCurAudioInfo().getHash())) {
                        mManyLineLyricsView.updateView((int) mHPApplication.getCurAudioMessage().getPlayProgress());
                    }
                }
            }
        } else if (action.equals(AudioBroadcastReceiver.ACTION_RELOADSINGERIMG)) {
            //重新加载歌手写真
            if (mHPApplication.getCurAudioInfo() != null) {
                String hash = intent.getStringExtra("hash");
                if (mHPApplication.getCurAudioInfo().getHash().equals(hash)) {
                    String singerName = intent.getStringExtra("singerName");
                    mSingerImageView.setVisibility(View.INVISIBLE);
                    mSingerImageView.setSongSingerInfos(mHPApplication, getApplicationContext(), null);
                    //加载歌手写真
                    ImageUtil.loadSingerImg(mHPApplication, getApplicationContext(), hash, singerName);

                }
            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SINGERIMGLOADED)) {
            //歌手写真加载完成
            if (mHPApplication.getCurAudioInfo() != null) {
                String hash = intent.getStringExtra("hash");
                if (mHPApplication.getCurAudioInfo().getHash().equals(hash)) {
                    mSingerImageView.setVisibility(View.VISIBLE);

                    String singerName = intent.getStringExtra("singerName");
                    String[] singerNameArray = null;
                    if (singerName.contains("、")) {

                        String regex = "\\s*、\\s*";
                        singerNameArray = singerName.split(regex);


                    } else {
                        singerNameArray = new String[1];
                        singerNameArray[0] = singerName;
                    }


                    //设置数据
                    List<SongSingerInfo> list = SongSingerDB.getSongSingerDB(context).getAllSingerImg(singerNameArray,false);
                    mSingerImageView.setSongSingerInfos(mHPApplication, getApplicationContext(), list);
                }
            }
        }
    }

    @Override
    protected int setContentViewId() {
        return R.layout.activity_lrc;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mRotateLinearLayout = findViewById(R.id.rotateLayout);
        //mRotateLinearLayout.setBackgroundView(findViewById(R.id.bg_layout));
        mRotateLinearLayout.setmRotateListener(new RotateLinearLayout.RotateListener() {
            @Override
            public void close() {

                LrcActivity.this.setResult(LRCTOMAINRESULTCODE);
                finish();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onClick() {
                logger.e("点击歌词事件");
            }
        });
        mRotateLinearLayout.resetView();
        //
        mLrcPlaybarLinearLayout = findViewById(R.id.lrc_playbar);
        mRotateLinearLayout.setIgnoreView(mLrcPlaybarLinearLayout);

        //返回按钮
        RelativeLayout backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRotateLinearLayout.finish();
            }
        });
        //
        mSongNameTextView = findViewById(R.id.songName);
        mSingerNameTextView = findViewById(R.id.singerName);
        //

        mManyLineLyricsView = findViewById(R.id.manyLineLyricsView);
        mManyLineLyricsView.setOnLrcClickListener(new ManyLineLyricsView.OnLrcClickListener() {
            @Override
            public void onLrcPlayClicked(int progress, boolean isLrcSeekTo) {
                seekToMusic(progress, isLrcSeekTo);
            }
        });
        //
        mRotateLinearLayout.setVerticalScrollView(mManyLineLyricsView);
        //设置字体大小和歌词颜色
        mManyLineLyricsView.setLrcFontSize(mHPApplication.getLrcFontSize());
        int lrcColor = ColorUtil.parserColor(mHPApplication.getLrcColorStr()[mHPApplication.getLrcColorIndex()]);
        mManyLineLyricsView.setLrcColor(lrcColor);
        //歌手写真
        mSingerImageView = findViewById(R.id.singerimg);
        mSingerImageView.setVisibility(View.INVISIBLE);

        //初始化底部播放器视图
        initPlayerViews();


        //初始化服务
        initService();

        //菜单
        mMoreMenuImgBtn = findViewById(R.id.more_menu);
        mMoreMenuImgBtn.setConvert(true);
        mMoreMenuImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopView();
            }
        });

        mDownloadImgBtn = findViewById(R.id.download_menu);
        mDownloadImgBtn.setConvert(true);
        mDownloadImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //喜欢按钮
        mLikeImgBtn = findViewById(R.id.liked_menu);
        mLikeImgBtn.setConvert(true);
        mLikeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHPApplication.getCurAudioInfo() != null) {
                    ToastUtil.showTextToast(getApplicationContext(), "取消成功");

                    mUnLikeImgBtn.setVisibility(View.VISIBLE);
                    mLikeImgBtn.setVisibility(View.GONE);

                    //删除喜欢歌曲
                    Intent delIntent = new Intent(AudioBroadcastReceiver.ACTION_LIKEDELETE);
                    delIntent.putExtra(AudioInfo.KEY, mHPApplication.getCurAudioInfo());
                    delIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(delIntent);
                }

            }
        });
        //不喜欢
        mUnLikeImgBtn = findViewById(R.id.unlike_menu);
        mUnLikeImgBtn.setConvert(true);
        mUnLikeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHPApplication.getCurAudioInfo() != null) {
                    ToastUtil.showTextToast(getApplicationContext(), "已添加收藏");

                    mUnLikeImgBtn.setVisibility(View.GONE);
                    mLikeImgBtn.setVisibility(View.VISIBLE);

                    //添加喜欢歌曲
                    Intent addIntent = new Intent(AudioBroadcastReceiver.ACTION_LIKEADD);
                    addIntent.putExtra(AudioInfo.KEY, mHPApplication.getCurAudioInfo());
                    addIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(addIntent);
                }
            }
        });
        mUnLikeImgBtn.setVisibility(View.VISIBLE);
        mLikeImgBtn.setVisibility(View.GONE);

        initPopView();
        initPLPopView();
        initSPLPopView();
        initSIPopView();

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mScreensWidth = display.getWidth();
    }


    /**
     * 隐藏歌曲信息
     */
    private void hideSIPopView() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, mSIPLayout.getHeight());
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                isSIPopViewShow = false;
                mSIPopLinearLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mSIPLayout.clearAnimation();
        mSIPLayout.setAnimation(translateAnimation);
        translateAnimation.start();
    }

    /**
     * 显示歌曲信息
     */
    private void showSPIPopView(AudioInfo audioInfo) {
        //设置歌曲信息
        mPopSingerNameTv.setText(audioInfo.getSingerName());
        mPopFileExtTv.setText(audioInfo.getFileExt());
        mPopTimeTv.setText(audioInfo.getDurationText());
        mPopFileSizeTv.setText(audioInfo.getFileSizeText());
        //
        mSIPopLinearLayout.setVisibility(View.VISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mSIPLayout.getHeight(), 0);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        mSIPLayout.clearAnimation();
        mSIPLayout.setAnimation(translateAnimation);
        translateAnimation.start();
        isSIPopViewShow = true;
    }

    /**
     * 初始化歌曲信息窗口
     */
    private void initSIPopView() {

        mSIPopLinearLayout = findViewById(R.id.songinfoPopLayout);
        mSIPopLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSIPopView();
            }
        });
        mSIPopLinearLayout.setVisibility(View.INVISIBLE);
        mSIPLayout = findViewById(R.id.pop_songinfo_parent);
        //
        LinearLayout splcalcel = findViewById(R.id.songcalcel);
        splcalcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSIPopView();
            }
        });
        //
        mPopSingerNameTv = findViewById(R.id.pop_singerName);
        mPopFileExtTv = findViewById(R.id.pop_fileext);
        mPopTimeTv = findViewById(R.id.pop_time);
        mPopFileSizeTv = findViewById(R.id.pop_filesize);
    }

    /**
     * 隐藏歌手列表窗口
     */
    private void hideSPLPopView() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, mSPlPLayout.getHeight());
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                isSPLPopViewShow = false;
                mSPlPopLinearLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mSPlPLayout.clearAnimation();
        mSPlPLayout.setAnimation(translateAnimation);
        translateAnimation.start();
    }

    /**
     * 显示歌手列表弹出窗口
     */
    private void showSPLPopView(String[] singerNameArray) {

        //
        mLrcPopSingerListAdapter = new LrcPopSingerListAdapter(mHPApplication, getApplicationContext(), singerNameArray, mLrcActivityListener);
        mSingerNameRecyclerView.setAdapter(mLrcPopSingerListAdapter);

        mSPlPopLinearLayout.setVisibility(View.VISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mSPlPLayout.getHeight(), 0);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        mSPlPLayout.clearAnimation();
        mSPlPLayout.setAnimation(translateAnimation);
        translateAnimation.start();
        isSPLPopViewShow = true;
    }

    /**
     * 初始化歌手列表窗口
     */
    private void initSPLPopView() {

        mSPlPopLinearLayout = findViewById(R.id.singerListPopLayout);
        mSPlPopLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSPLPopView();
            }
        });
        mSPlPopLinearLayout.setVisibility(View.INVISIBLE);
        mSPlPLayout = findViewById(R.id.pop_singerlist_parent);
        //
        LinearLayout splcalcel = findViewById(R.id.splcalcel);
        splcalcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSPLPopView();
            }
        });

        //
        mSingerNameRecyclerView = findViewById(R.id.singerlist_recyclerView);
        mSingerNameRecyclerView.setLinearLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    /**
     * 初始化播放列表弹出窗口
     */
    private void initPLPopView() {

        mCurPLSizeTv = findViewById(R.id.list_size);
        mCurRecyclerView = findViewById(R.id.curplaylist_recyclerView);
        //初始化内容视图
        mCurRecyclerView.setLinearLayoutManager(new LinearLayoutManager(getApplicationContext()));


        mPlPopLinearLayout = findViewById(R.id.lrcListPopLayout);
        mPlPopLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidePlPopView();
            }
        });
        mPlPopLinearLayout.setVisibility(View.INVISIBLE);
        mPlPLayout = findViewById(R.id.pop_list_parent);

        //播放模式
        modeAllTv = findViewById(R.id.modeAllTv);
        modeRandomTv = findViewById(R.id.modeRandomTv);
        modeSingleTv = findViewById(R.id.modeSingleTv);
        //
        modeAllTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPLPlayModeView(1, modeAllTv, modeRandomTv, modeSingleTv, true);
                initPlayModeView(1, modeAllImg, modeRandomImg, modeSingleImg, false);
            }
        });

        modeRandomTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPLPlayModeView(3, modeAllTv, modeRandomTv, modeSingleTv, true);
                initPlayModeView(3, modeAllImg, modeRandomImg, modeSingleImg, false);
            }
        });

        modeSingleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPLPlayModeView(0, modeAllTv, modeRandomTv, modeSingleTv, true);
                initPlayModeView(0, modeAllImg, modeRandomImg, modeSingleImg, false);
            }
        });
        initPLPlayModeView(mHPApplication.getPlayModel(), modeAllTv, modeRandomTv, modeSingleTv, false);
    }

    /**
     * 初始化播放列表播放模式
     *
     * @param playMode
     * @param modeAllImg
     * @param modeRandomImg
     * @param modeSingleImg
     */
    private void initPLPlayModeView(int playMode, IconfontTextView modeAllImg, IconfontTextView modeRandomImg, IconfontTextView modeSingleImg, boolean isTipShow) {
        if (playMode == 0) {
            if (isTipShow)
                ToastUtil.showTextToast(LrcActivity.this, "顺序播放");
            modeAllImg.setVisibility(View.VISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else if (playMode == 1) {
            if (isTipShow)
                ToastUtil.showTextToast(LrcActivity.this, "随机播放");
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.VISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else {
            if (isTipShow)
                ToastUtil.showTextToast(LrcActivity.this, "单曲播放");
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 显示播放列表弹出窗口
     */
    private void showPlPopView() {
        initPLPlayModeView(mHPApplication.getPlayModel(), modeAllTv, modeRandomTv, modeSingleTv, false);

        //加载当前播放列表数据
        List<AudioInfo> curAudioInfos = mHPApplication.getCurAudioInfos();
        if (curAudioInfos == null) {
            curAudioInfos = new ArrayList<AudioInfo>();
        }
        mCurPLSizeTv.setText(curAudioInfos.size() + "");
        mPopPlayListAdapter = new LrcPopPlayListAdapter(mHPApplication, getApplicationContext(), curAudioInfos);
        mCurRecyclerView.setAdapter(mPopPlayListAdapter);

             /*
                参数解释：
                    第一个参数：X轴水平缩放起始位置的大小（fromX）。1代表正常大小
                    第二个参数：X轴水平缩放完了之后（toX）的大小，0代表完全消失了
                    第三个参数：Y轴垂直缩放起始时的大小（fromY）
                    第四个参数：Y轴垂直缩放结束后的大小（toY）
                    第五个参数：pivotXType为动画在X轴相对于物件位置类型
                    第六个参数：pivotXValue为动画相对于物件的X坐标的开始位置
                    第七个参数：pivotXType为动画在Y轴相对于物件位置类型
                    第八个参数：pivotYValue为动画相对于物件的Y坐标的开始位置

                   （第五个参数，第六个参数），（第七个参数,第八个参数）是用来指定缩放的中心点
                    0.5f代表从中心缩放
             */
        ScaleAnimation translateAnimation = new ScaleAnimation(0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 1f);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                //滚动到当前播放位置
                int position = mPopPlayListAdapter.getPlayIndexPosition(mHPApplication.getCurAudioInfo());
                if (position >= 0)
                    mCurRecyclerView.move(position,
                            LinearLayoutRecyclerView.smoothScroll);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mPlPopLinearLayout.setVisibility(View.VISIBLE);
        mPlPLayout.clearAnimation();
        mPlPLayout.setAnimation(translateAnimation);
        translateAnimation.start();
        isPLPopViewShow = true;


    }

    /**
     * 隐藏播放列表弹出窗口
     */
    private void hidePlPopView() {
  /*
                参数解释：
                    第一个参数：X轴水平缩放起始位置的大小（fromX）。1代表正常大小
                    第二个参数：X轴水平缩放完了之后（toX）的大小，0代表完全消失了
                    第三个参数：Y轴垂直缩放起始时的大小（fromY）
                    第四个参数：Y轴垂直缩放结束后的大小（toY）
                    第五个参数：pivotXType为动画在X轴相对于物件位置类型
                    第六个参数：pivotXValue为动画相对于物件的X坐标的开始位置
                    第七个参数：pivotXType为动画在Y轴相对于物件位置类型
                    第八个参数：pivotYValue为动画相对于物件的Y坐标的开始位置

                   （第五个参数，第六个参数），（第七个参数,第八个参数）是用来指定缩放的中心点
                    0.5f代表从中心缩放
             */
        ScaleAnimation translateAnimation = new ScaleAnimation(1f, 0f, 1f, 0f,
                Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 1f);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                isPLPopViewShow = false;
                mPlPopLinearLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        mPlPLayout.clearAnimation();
        mPlPLayout.setAnimation(translateAnimation);
        translateAnimation.start();

    }

    /**
     * 隐藏popview
     */
    private void hidePopView() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, mMenuLayout.getHeight());
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                isPopViewShow = false;
                mPopLinearLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mMenuLayout.clearAnimation();
        mMenuLayout.setAnimation(translateAnimation);
        translateAnimation.start();
    }

    /**
     * 显示popview
     */
    private void showPopView() {
        mPopLinearLayout.setVisibility(View.VISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mMenuLayout.getHeight(), 0);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        mMenuLayout.clearAnimation();
        mMenuLayout.setAnimation(translateAnimation);
        translateAnimation.start();
        isPopViewShow = true;
    }

    /**
     * 初始化pop
     */
    private void initPopView() {


        mPopLinearLayout = findViewById(R.id.lrcPopLayout);
        mPopLinearLayout.setVisibility(View.INVISIBLE);
        mPopLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidePopView();
            }
        });
        //
        mMenuLayout = findViewById(R.id.menuLayout);
        //
        LinearLayout cancelLinearLayout = findViewById(R.id.calcel);
        cancelLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidePopView();
            }
        });


        //搜索歌手写真
        ImageView searchSingerImg = findViewById(R.id.search_singer_pic);
        searchSingerImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHPApplication.getCurAudioInfo() == null) {

                    ToastUtil.showTextToast(getApplicationContext(), "请选择歌曲");

                } else {
                    hidePopView();
                    //歌手名称
                    String singerName = mHPApplication.getCurAudioInfo().getSingerName();
                    if (singerName.contains("、")) {

                        String regex = "\\s*、\\s*";
                        String[] singerNameArray = singerName.split(regex);
                        showSPLPopView(singerNameArray);

                    } else {
                        Intent intent = new Intent(LrcActivity.this, SearchSingerActivity.class);
                        intent.putExtra("singerName", singerName);
                        startActivity(intent);
                        //
                        overridePendingTransition(0, 0);
                    }
                }
            }
        });

        //搜索歌词
        ImageView searchLrcImg = findViewById(R.id.search_lrc);
        searchLrcImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHPApplication.getCurAudioInfo() == null) {

                    ToastUtil.showTextToast(getApplicationContext(), "请选择歌曲");

                } else {
                    hidePopView();
                    //
                    Intent intent = new Intent(LrcActivity.this, SearchLrcActivity.class);
                    startActivity(intent);
                    //
                    overridePendingTransition(R.anim.in_from_bottom, 0);
                }

            }
        });

        //歌曲详情
        ImageView songInfoImg = findViewById(R.id.songinfo);
        songInfoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHPApplication.getCurAudioInfo() == null) {

                    ToastUtil.showTextToast(getApplicationContext(), "请选择歌曲");

                } else {
                    hidePopView();
                    showSPIPopView(mHPApplication.getCurAudioInfo());
                }
            }
        });

        //歌曲进度减少按钮
        ButtonRelativeLayout lrcProgressJianBtn = findViewById(R.id.lyric_progress_jian);
        lrcProgressJianBtn.setDefFillColor(ColorUtil.parserColor(Color.WHITE, 20));
        lrcProgressJianBtn.setPressedFillColor(ColorUtil.parserColor(Color.WHITE, 50));
        lrcProgressJianBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mManyLineLyricsView.getLyricsUtil() != null) {
                    mManyLineLyricsView.getLyricsUtil().setOffset(mManyLineLyricsView.getLyricsUtil().getOffset() + (-500));
                    ToastUtil.showTextToast(LrcActivity.this, (float) mManyLineLyricsView.getLyricsUtil().getOffset() / 1000 + "秒");
                    if (mManyLineLyricsView.getLyricsLineTreeMap() != null) {

                        if (mHPApplication.getCurAudioMessage() != null) {
                            mManyLineLyricsView.updateView((int) mHPApplication.getCurAudioMessage().getPlayProgress());
                        }
                        //保存歌词文件
                        saveLrcFile(mManyLineLyricsView.getLyricsUtil().getLrcFilePath(), mManyLineLyricsView.getLyricsUtil().getLyricsIfno(), mManyLineLyricsView.getLyricsUtil().getPlayOffset());

                    }
                }
            }
        });
        //进度重置
        ButtonRelativeLayout resetProgressJianBtn = findViewById(R.id.lyric_progress_reset);
        resetProgressJianBtn.setDefFillColor(ColorUtil.parserColor(Color.WHITE, 20));
        resetProgressJianBtn.setPressedFillColor(ColorUtil.parserColor(Color.WHITE, 50));
        resetProgressJianBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mManyLineLyricsView.getLyricsUtil() != null) {
                    mManyLineLyricsView.getLyricsUtil().setOffset(0);
                    ToastUtil.showTextToast(LrcActivity.this, "还原了");
                    if (mManyLineLyricsView.getLyricsLineTreeMap() != null) {

                        if (mHPApplication.getCurAudioMessage() != null) {
                            mManyLineLyricsView.updateView((int) mHPApplication.getCurAudioMessage().getPlayProgress());
                        }
                        //保存歌词文件
                        saveLrcFile(mManyLineLyricsView.getLyricsUtil().getLrcFilePath(), mManyLineLyricsView.getLyricsUtil().getLyricsIfno(), mManyLineLyricsView.getLyricsUtil().getPlayOffset());

                    }
                }
            }
        });
        //进度增加
        ButtonRelativeLayout lrcProgressJiaBtn = findViewById(R.id.lyric_progress_jia);
        lrcProgressJiaBtn.setDefFillColor(ColorUtil.parserColor(Color.WHITE, 20));
        lrcProgressJiaBtn.setPressedFillColor(ColorUtil.parserColor(Color.WHITE, 50));
        lrcProgressJiaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mManyLineLyricsView.getLyricsUtil() != null) {
                    mManyLineLyricsView.getLyricsUtil().setOffset(mManyLineLyricsView.getLyricsUtil().getOffset() + (500));
                    ToastUtil.showTextToast(LrcActivity.this, (float) mManyLineLyricsView.getLyricsUtil().getOffset() / 1000 + "秒");
                    if (mManyLineLyricsView.getLyricsLineTreeMap() != null) {

                        if (mHPApplication.getCurAudioMessage() != null) {
                            mManyLineLyricsView.updateView((int) mHPApplication.getCurAudioMessage().getPlayProgress());
                        }
                        //保存歌词文件
                        saveLrcFile(mManyLineLyricsView.getLyricsUtil().getLrcFilePath(), mManyLineLyricsView.getLyricsUtil().getLyricsIfno(), mManyLineLyricsView.getLyricsUtil().getPlayOffset());

                    }
                }
            }
        });


        //字体大小
        final LrcSeekBar lrcSizeLrcSeekBar = findViewById(R.id.fontSizeseekbar);
        lrcSizeLrcSeekBar.setMax(mHPApplication.getMaxLrcFontSize() - mHPApplication.getMinLrcFontSize());
        lrcSizeLrcSeekBar.setProgress((mHPApplication.getLrcFontSize() - mHPApplication.getMinLrcFontSize()));
        lrcSizeLrcSeekBar.setBackgroundProgressColorColor(ColorUtil.parserColor(Color.WHITE, 50));
        lrcSizeLrcSeekBar.setProgressColor(Color.WHITE);
        lrcSizeLrcSeekBar.setThumbColor(Color.WHITE);
        lrcSizeLrcSeekBar.setOnChangeListener(new LrcSeekBar.OnChangeListener() {
            @Override
            public void onProgressChanged() {
                //logger.e("progress=" + lrcSizeLrcSeekBar.getProgress());
                if (mManyLineLyricsView.getLyricsUtil() != null) {
                    if (mManyLineLyricsView.getLyricsLineTreeMap() != null) {
                        if (mHPApplication.getCurAudioMessage() != null) {
                            mManyLineLyricsView.setLrcFontSize(lrcSizeLrcSeekBar.getProgress() + mHPApplication.getMinLrcFontSize(), (int) mHPApplication.getCurAudioMessage().getPlayProgress(), mScreensWidth / 4 * 3);
                        }
                    }
                } else {
                    mManyLineLyricsView.setLrcFontSize(lrcSizeLrcSeekBar.getProgress() + mHPApplication.getMinLrcFontSize());
                }
            }

            @Override
            public String getTimeText() {
                return null;
            }

            @Override
            public String getLrcText() {
                return null;
            }

            @Override
            public void dragFinish() {
                mHPApplication.setLrcFontSize(lrcSizeLrcSeekBar.getProgress() + mHPApplication.getMinLrcFontSize());
            }
        });

        //字体减小
        IconfontImageButtonTextView lrcSizeDecrease = findViewById(R.id.lyric_decrease);
        lrcSizeDecrease.setConvert(true);
        lrcSizeDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int curProgress = lrcSizeLrcSeekBar.getProgress();
                curProgress -= 2;
                if (curProgress < 0) {
                    curProgress = 0;
                }
                lrcSizeLrcSeekBar.setProgress(curProgress);
            }
        });

        //字体增加
        IconfontImageButtonTextView lrcSizeIncrease = findViewById(R.id.lyric_increase);
        lrcSizeIncrease.setConvert(true);
        lrcSizeIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int curProgress = lrcSizeLrcSeekBar.getProgress();
                curProgress += 2;
                if (curProgress > lrcSizeLrcSeekBar.getMax()) {
                    curProgress = lrcSizeLrcSeekBar.getMax();
                }
                lrcSizeLrcSeekBar.setProgress(curProgress);
            }
        });

        //歌词颜色面板
        ImageView[] colorPanel = new ImageView[mHPApplication.getLrcColorStr().length];
        final ImageView[] colorStatus = new ImageView[colorPanel.length];

        int i = 0;
        //
        colorPanel[i] = findViewById(R.id.color_panel1);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mHPApplication.getLrcColorIndex();
                if (index != 0) {
                    mHPApplication.setLrcColorIndex(0);
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[0].setVisibility(View.VISIBLE);

                    int lrcColor = ColorUtil.parserColor(mHPApplication.getLrcColorStr()[mHPApplication.getLrcColorIndex()]);
                    mManyLineLyricsView.setLrcColor(lrcColor);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status1);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel2);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mHPApplication.getLrcColorIndex();
                if (index != 1) {
                    mHPApplication.setLrcColorIndex(1);
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[1].setVisibility(View.VISIBLE);


                    int lrcColor = ColorUtil.parserColor(mHPApplication.getLrcColorStr()[mHPApplication.getLrcColorIndex()]);
                    mManyLineLyricsView.setLrcColor(lrcColor);

                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status2);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel3);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mHPApplication.getLrcColorIndex();
                if (index != 2) {
                    mHPApplication.setLrcColorIndex(2);
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[2].setVisibility(View.VISIBLE);

                    int lrcColor = ColorUtil.parserColor(mHPApplication.getLrcColorStr()[mHPApplication.getLrcColorIndex()]);
                    mManyLineLyricsView.setLrcColor(lrcColor);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status3);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel4);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mHPApplication.getLrcColorIndex();
                if (index != 3) {
                    mHPApplication.setLrcColorIndex(3);
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[3].setVisibility(View.VISIBLE);

                    int lrcColor = ColorUtil.parserColor(mHPApplication.getLrcColorStr()[mHPApplication.getLrcColorIndex()]);
                    mManyLineLyricsView.setLrcColor(lrcColor);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status4);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel5);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mHPApplication.getLrcColorIndex();
                if (index != 4) {
                    mHPApplication.setLrcColorIndex(4);
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[4].setVisibility(View.VISIBLE);

                    int lrcColor = ColorUtil.parserColor(mHPApplication.getLrcColorStr()[mHPApplication.getLrcColorIndex()]);
                    mManyLineLyricsView.setLrcColor(lrcColor);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status5);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel6);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mHPApplication.getLrcColorIndex();
                if (index != 5) {
                    mHPApplication.setLrcColorIndex(5);
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[5].setVisibility(View.VISIBLE);

                    int lrcColor = ColorUtil.parserColor(mHPApplication.getLrcColorStr()[mHPApplication.getLrcColorIndex()]);
                    mManyLineLyricsView.setLrcColor(lrcColor);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status6);

        //
        colorStatus[mHPApplication.getLrcColorIndex()].setVisibility(View.VISIBLE);

    }


    /**
     * 保存歌词文件
     *
     * @param lrcFilePath lrc歌词路径
     * @param lyricsInfo  lrc歌词数据
     * @param playOffset  lrc歌词快进进度
     */
    private void saveLrcFile(final String lrcFilePath, final LyricsInfo lyricsInfo, final int playOffset) {
        new Thread() {

            @Override
            public void run() {

                Map<String, Object> tags = lyricsInfo.getLyricsTags();

                tags.put(LyricsTag.TAG_OFFSET, playOffset);
                lyricsInfo.setLyricsTags(tags);


                //保存修改的歌词文件
                try {
                    LyricsIOUtils.getLyricsFileWriter(lrcFilePath).writer(lyricsInfo, lrcFilePath);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

        }.start();
    }


    @Override
    protected void loadData(boolean isRestoreInstance) {
        //
        AudioInfo curAudioInfo = mHPApplication.getCurAudioInfo();
        if (curAudioInfo != null) {
            Intent initIntent = new Intent(AudioBroadcastReceiver.ACTION_INITMUSIC);
            doAudioReceive(getApplicationContext(), initIntent);
        } else {
            Intent nullIntent = new Intent(AudioBroadcastReceiver.ACTION_NULLMUSIC);
            doAudioReceive(getApplicationContext(), nullIntent);
        }
    }


    /**
     * 初始化底部播放菜单
     */
    private void initPlayerViews() {

        mSongProgressTv = findViewById(R.id.songProgress);
        mSongDurationTv = findViewById(R.id.songDuration);

        //进度条
        mLrcSeekBar = findViewById(R.id.lrcseekbar);
        mLrcSeekBar.setOnChangeListener(new LrcSeekBar.OnChangeListener() {

            @Override
            public void onProgressChanged() {
                int playStatus = mHPApplication.getPlayStatus();
                if (playStatus != AudioPlayerManager.PLAYING) {
                    mSongProgressTv.setText(MediaUtil.parseTimeToString((mLrcSeekBar.getProgress())));
                }
            }

            @Override
            public String getTimeText() {
                return MediaUtil.parseTimeToString(mLrcSeekBar.getProgress());
            }

            @Override
            public String getLrcText() {


                return null;
            }

            @Override
            public void dragFinish() {
                seekToMusic(mLrcSeekBar.getProgress(), false);
            }
        });
        //
        mLrcSeekBar.setBackgroundProgressColorColor(ColorUtil.parserColor("#eeeeee", 50));
        mLrcSeekBar.setSecondProgressColor(Color.argb(100, 255, 255, 255));
        mLrcSeekBar.setProgressColor(Color.rgb(255, 64, 129));
        mLrcSeekBar.setThumbColor(Color.rgb(255, 64, 129));
        mLrcSeekBar.setTimePopupWindowViewColor(Color.argb(200, 255, 64, 129));

        //播放
        mPlayBtn = findViewById(R.id.playbtn);
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
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
        mPauseBtn = findViewById(R.id.pausebtn);
        mPauseBtn.setOnClickListener(new View.OnClickListener() {
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
        mNextBtn = findViewById(R.id.nextbtn);
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                Intent nextIntent = new Intent(AudioBroadcastReceiver.ACTION_NEXTMUSIC);
                nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(nextIntent);
            }
        });

        //上一首
        mPreBtn = findViewById(R.id.prebtn);
        mPreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                Intent nextIntent = new Intent(AudioBroadcastReceiver.ACTION_PREMUSIC);
                nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(nextIntent);
            }
        });

        /////////播放模式//////////////
        //顺序播放
        modeAllImg = findViewById(R.id.modeAll);
        modeRandomImg = findViewById(R.id.modeRandom);
        modeSingleImg = findViewById(R.id.modeSingle);


        modeAllImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(1, modeAllImg, modeRandomImg, modeSingleImg, true);
            }
        });

        modeRandomImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(3, modeAllImg, modeRandomImg, modeSingleImg, true);
            }
        });

        modeSingleImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(0, modeAllImg, modeRandomImg, modeSingleImg, true);
            }
        });
        initPlayModeView(mHPApplication.getPlayModel(), modeAllImg, modeRandomImg, modeSingleImg, false);

        //
        RelativeLayout playListMenu = findViewById(R.id.playlistmenu);
        playListMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlPopView();
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
    private void initPlayModeView(int playMode, ImageView modeAllImg, ImageView modeRandomImg, ImageView modeSingleImg, boolean isTipShow) {
        if (playMode == 0) {
            if (isTipShow)
                ToastUtil.showTextToast(LrcActivity.this, "顺序播放");
            modeAllImg.setVisibility(View.VISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else if (playMode == 1) {
            if (isTipShow)
                ToastUtil.showTextToast(LrcActivity.this, "随机播放");
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.VISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else {
            if (isTipShow)
                ToastUtil.showTextToast(LrcActivity.this, "单曲播放");
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.VISIBLE);
        }
        //
        mHPApplication.setPlayModel(playMode);
    }

    /**
     * 快进播放
     *
     * @param progress
     * @param isLrcSeekTo
     */
    private void seekToMusic(int progress, boolean isLrcSeekTo) {
        mHPApplication.setLrcSeekTo(isLrcSeekTo);
        //判断歌词快进时，是否超过歌曲的总时间
        if (mHPApplication.getCurAudioInfo().getDuration() < progress) {
            progress = (int) mHPApplication.getCurAudioInfo().getDuration();
        }
        //
        int playStatus = mHPApplication.getPlayStatus();
        if (playStatus == AudioPlayerManager.PLAYING) {
            //正在播放
            if (mHPApplication.getCurAudioMessage() != null) {
                AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
                // AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                //if (audioInfo != null) {
                //  audioMessage.setAudioInfo(audioInfo);
                if (audioMessage != null) {
                    audioMessage.setPlayProgress(progress);
                    Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_SEEKTOMUSIC);
                    resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                    resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(resumeIntent);
                }
            }
        } else {

            if (mHPApplication.getCurAudioMessage() != null)
                mHPApplication.getCurAudioMessage().setPlayProgress(progress);

            //歌词快进
            Intent lrcSeektoIntent = new Intent(AudioBroadcastReceiver.ACTION_LRCSEEKTO);
            lrcSeektoIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(lrcSeektoIntent);


        }
    }

    /**
     * 初始化服务
     */
    private void initService() {

        //注册接收音频播放广播
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(getApplicationContext(), mHPApplication);
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(getApplicationContext());

        //在线音乐广播
        mOnLineAudioReceiver = new OnLineAudioReceiver(getApplicationContext(), mHPApplication);
        mOnLineAudioReceiver.setOnlineAudioReceiverListener(mOnlineAudioReceiverListener);
        mOnLineAudioReceiver.registerReceiver(getApplicationContext());
    }


    @Override
    protected void onDestroy() {

        //注销广播
        mAudioBroadcastReceiver.unregisterReceiver(getApplicationContext());

        //在线歌曲
        mOnLineAudioReceiver.unregisterReceiver(getApplicationContext());
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isPopViewShow) {
            hidePopView();
            return;
        } else if (isPLPopViewShow) {
            hidePlPopView();
            return;
        } else if (isSPLPopViewShow) {
            hideSPLPopView();
            return;
        } else if (isSIPopViewShow) {
            hideSIPopView();
            return;
        }
        mRotateLinearLayout.finish();
    }


    @Override
    protected boolean isAddStatusBar() {

        setStatusColor(Color.TRANSPARENT);

        return true;
    }

    @Override
    public int setStatusBarParentView() {
        return R.id.lrc_layout;
    }


    public interface LrcActivityListener {
        void closeSingerPopListVeiw();
    }
}
