package com.zlm.hp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.R;
import com.zlm.hp.adapter.LrcPopPlayListAdapter;
import com.zlm.hp.adapter.LrcPopSingerListAdapter;
import com.zlm.hp.application.HPApplication;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.db.DownloadInfoDB;
import com.zlm.hp.db.DownloadThreadDB;
import com.zlm.hp.db.SongSingerDB;
import com.zlm.hp.media.lyrics.model.LyricsInfo;
import com.zlm.hp.media.lyrics.model.LyricsTag;
import com.zlm.hp.media.lyrics.utils.LyricsIOUtils;
import com.zlm.hp.media.lyrics.utils.LyricsUtil;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.DownloadAudioManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.manager.OnLineAudioManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.model.DownloadMessage;
import com.zlm.hp.model.SongSingerInfo;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.OnLineAudioReceiver;
import com.zlm.hp.utils.ImageUtil;
import com.zlm.hp.utils.MediaUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import base.utils.ColorUtil;
import base.utils.ThreadUtil;
import base.utils.ToastUtil;
import base.widget.ButtonRelativeLayout;
import base.widget.IconfontImageButtonTextView;
import base.widget.IconfontTextView;
import base.widget.LinearLayoutRecyclerView;
import base.widget.LrcSeekBar;
import base.widget.PlayListBGRelativeLayout;
import base.widget.RotateLinearLayout;
import base.widget.SingerImageView;
import base.widget.dialog.CustomDialog;
import base.widget.lrc.ManyLineLyricsViewV2;

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

    /**
     * 多行歌词视图
     */
    private ManyLineLyricsViewV2 mManyLineLyricsView;

    //、、、、、、、、、、、、、、、、、、、、、、、、、翻译和音译歌词、、、、、、、、、、、、、、、、、、、、、、、、、、、
    //翻译歌词
    private ImageView mHideTranslateImg;
    private ImageView mShowTranslateImg;
    //音译歌词
    private ImageView mHideTransliterationImg;
    private ImageView mShowTransliterationImg;

    //翻译歌词/音译歌词
    private ImageView mShowTTToTranslateImg;
    private ImageView mShowTTToTransliterationImg;
    private ImageView mHideTTImg;

    private final int HASTRANSLATELRC = 0;
    private final int HASTRANSLITERATIONLRC = 1;
    private final int HASTRANSLATEANDTRANSLITERATIONLRC = 2;
    private final int NOEXTRALRC = 3;

    private Handler mExtraLrcTypeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NOEXTRALRC:

                    //翻译歌词
                    mHideTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTranslateImg.setVisibility(View.INVISIBLE);
                    //音译歌词
                    mHideTransliterationImg.setVisibility(View.INVISIBLE);
                    mShowTransliterationImg.setVisibility(View.INVISIBLE);

                    //翻译歌词/音译歌词
                    mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    mHideTTImg.setVisibility(View.INVISIBLE);


                    break;
                case HASTRANSLATEANDTRANSLITERATIONLRC:


                    //翻译歌词
                    mHideTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTranslateImg.setVisibility(View.INVISIBLE);
                    //音译歌词
                    mHideTransliterationImg.setVisibility(View.INVISIBLE);
                    mShowTransliterationImg.setVisibility(View.INVISIBLE);

                    if(mManyLineLyricsView.isManyLineLrc()){
                        //翻译歌词/音译歌词
                        mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                        mShowTTToTransliterationImg.setVisibility(View.VISIBLE);
                        mHideTTImg.setVisibility(View.INVISIBLE);
                    }else{
                        //翻译歌词/音译歌词
                        mShowTTToTranslateImg.setVisibility(View.VISIBLE);
                        mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                        mHideTTImg.setVisibility(View.INVISIBLE);
                    }

                    break;
                case HASTRANSLITERATIONLRC:

                    //翻译歌词
                    mHideTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTranslateImg.setVisibility(View.INVISIBLE);

                    if(mManyLineLyricsView.isManyLineLrc()){
                        //音译歌词
                        mHideTransliterationImg.setVisibility(View.VISIBLE);
                        mShowTransliterationImg.setVisibility(View.INVISIBLE);
                    }else{
                        //音译歌词
                        mHideTransliterationImg.setVisibility(View.INVISIBLE);
                        mShowTransliterationImg.setVisibility(View.VISIBLE);
                    }


                    //翻译歌词/音译歌词
                    mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    mHideTTImg.setVisibility(View.INVISIBLE);

                    break;
                case HASTRANSLATELRC:

                    if(mManyLineLyricsView.isManyLineLrc()){
                        //翻译歌词
                        mHideTranslateImg.setVisibility(View.VISIBLE);
                        mShowTranslateImg.setVisibility(View.INVISIBLE);
                    }else{
                        //翻译歌词
                        mHideTranslateImg.setVisibility(View.INVISIBLE);
                        mShowTranslateImg.setVisibility(View.VISIBLE);
                    }


                    //音译歌词
                    mHideTransliterationImg.setVisibility(View.INVISIBLE);
                    mShowTransliterationImg.setVisibility(View.INVISIBLE);

                    //翻译歌词/音译歌词
                    mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    mHideTTImg.setVisibility(View.INVISIBLE);


                    break;

            }

        }
    };

    //、、、、、、、、、、、、、、、、、、、、、、、、、翻译和音译歌词、、、、、、、、、、、、、、、、、、、、、、、、、、、
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
    private ImageView mDownloadImgBtn;
    private ImageView mDownloadedImgBtn;
    private IconfontImageButtonTextView mLikeImgBtn;
    private IconfontImageButtonTextView mUnLikeImgBtn;

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
        public void closeSingerPopListView() {
            hideSPLPopView();
        }
    };

    //、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、
    //播放模式
    private IconfontTextView modeAllTv;
    private IconfontTextView modeRandomTv;
    private IconfontTextView modeSingleTv;

    //删除播放列表
    private IconfontTextView mDeleteTv;
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
    private CustomDialog mPopDialog;
    private CustomDialog mSonginfoPopDialog;

    @Override
    protected int setContentViewId() {
        return R.layout.activity_lrc;
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
            mManyLineLyricsView.setLyricsUtil(null, 0,0);
            //歌手写真
            mSingerImageView.setVisibility(View.INVISIBLE);
            mSingerImageView.setSongSingerInfos(mContext, null);

            //重置弹出窗口播放列表
            if (isPLPopViewShow) {
                if (mPopPlayListAdapter != null) {
                    mPopPlayListAdapter.reshViewHolder(null);
                }
            }

            //设置喜欢
            mUnLikeImgBtn.setVisibility(View.VISIBLE);
            mLikeImgBtn.setVisibility(View.GONE);


            //下载
            mDownloadedImgBtn.setVisibility(View.INVISIBLE);
            mDownloadImgBtn.setVisibility(View.VISIBLE);

        } else if (action.equals(AudioBroadcastReceiver.ACTION_INITMUSIC)) {
            //初始化
            AudioMessage audioMessage = HPApplication.getInstance().getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            AudioInfo audioInfo = HPApplication.getInstance().getCurAudioInfo();

            mSongNameTextView.setText(audioInfo.getSongName());
            mSingerNameTextView.setText(audioInfo.getSingerName());

            if (HPApplication.getInstance().getPlayStatus() == AudioPlayerManager.PLAYING) {
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
            if (audioInfo.getSingerName().equals(mContext.getString(R.string.unknown))) {
                keyWords = audioInfo.getSongName();
            } else {
                keyWords = audioInfo.getSingerName() + " - " + audioInfo.getSongName();
            }
            LyricsManager.getLyricsManager(mContext).loadLyricsUtil(keyWords, keyWords, audioInfo.getDuration() + "", audioInfo.getHash());

            //
            mManyLineLyricsView.setLyricsUtil(null, 0,0);

            //设置弹出窗口播放列表
            if (isPLPopViewShow) {
                if (mPopPlayListAdapter != null) {
                    mPopPlayListAdapter.reshViewHolder(audioInfo);
                }
            }

            mSingerImageView.setVisibility(View.INVISIBLE);
            mSingerImageView.setSongSingerInfos(mContext, null);
            //加载歌手写真
            ImageUtil.loadSingerImg(mContext, audioInfo.getHash(), audioInfo.getSingerName());


            //设置喜欢
            boolean isLike = AudioInfoDB.getAudioInfoDB(getApplicationContext()).isRecentOrLikeExists(audioInfo.getHash(), audioInfo.getType(), false);
            if (isLike) {
                mUnLikeImgBtn.setVisibility(View.GONE);
                mLikeImgBtn.setVisibility(View.VISIBLE);
            } else {
                mUnLikeImgBtn.setVisibility(View.VISIBLE);
                mLikeImgBtn.setVisibility(View.GONE);
            }

            //

            if (audioInfo.getType() == AudioInfo.NET || audioInfo.getType() == AudioInfo.DOWNLOAD) {

                //下载
                if (DownloadInfoDB.getAudioInfoDB(getApplicationContext()).isExists(audioInfo.getHash()) || AudioInfoDB.getAudioInfoDB(getApplicationContext()).isNetAudioExists(audioInfo.getHash())) {

                    mDownloadedImgBtn.setVisibility(View.VISIBLE);
                    mDownloadImgBtn.setVisibility(View.INVISIBLE);
                } else {
                    mDownloadedImgBtn.setVisibility(View.INVISIBLE);
                    mDownloadImgBtn.setVisibility(View.VISIBLE);
                }

            } else {
                mDownloadedImgBtn.setVisibility(View.VISIBLE);
                mDownloadImgBtn.setVisibility(View.INVISIBLE);
            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYMUSIC)) {
            //播放

            AudioMessage audioMessage = HPApplication.getInstance().getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);

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
            AudioMessage audioMessage = HPApplication.getInstance().getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            if (audioMessage != null) {
                mSongProgressTv.setText(MediaUtil.parseTimeToString((int) audioMessage.getPlayProgress()));
                mLrcSeekBar.setProgress((int) audioMessage.getPlayProgress());
                AudioInfo audioInfo = HPApplication.getInstance().getCurAudioInfo();
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
            AudioMessage curAudioMessage = HPApplication.getInstance().getCurAudioMessage();
            AudioMessage audioMessage = (AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            String hash = audioMessage.getHash();
            if (hash.equals(HPApplication.getInstance().getCurAudioInfo().getHash())) {
                //
                LyricsUtil lyricsUtil = LyricsManager.getLyricsManager(mContext).getLyricsUtil(hash);
                if (lyricsUtil != null) {
                    lyricsUtil.setHash(hash);
                    mManyLineLyricsView.setLyricsUtil(lyricsUtil, mScreensWidth / 3 * 2,(int) curAudioMessage.getPlayProgress());
                    mManyLineLyricsView.updateView((int) curAudioMessage.getPlayProgress());
                }
            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_LRCSEEKTO)) {
            //歌词快进
            if (HPApplication.getInstance().getCurAudioMessage() != null) {
                mSongProgressTv.setText(MediaUtil.parseTimeToString((int) HPApplication.getInstance().getCurAudioMessage().getPlayProgress()));
                mLrcSeekBar.setProgress((int) HPApplication.getInstance().getCurAudioMessage().getPlayProgress());
                if (HPApplication.getInstance().getCurAudioInfo() != null) {
                    if (mManyLineLyricsView.getLyricsUtil() != null &&
                            mManyLineLyricsView.getLyricsUtil().getHash().
                                    equals(HPApplication.getInstance().getCurAudioInfo().getHash())) {
                        mManyLineLyricsView.updateView((int) HPApplication.getInstance().getCurAudioMessage().getPlayProgress());
                    }
                }
            }
        } else if (action.equals(AudioBroadcastReceiver.ACTION_RELOADSINGERIMG)) {
            //重新加载歌手写真
            if (HPApplication.getInstance().getCurAudioInfo() != null) {
                String hash = intent.getStringExtra("hash");
                if (HPApplication.getInstance().getCurAudioInfo().getHash().equals(hash)) {
                    String singerName = intent.getStringExtra("singerName");
                    mSingerImageView.setVisibility(View.INVISIBLE);
                    mSingerImageView.setSongSingerInfos(mContext, null);
                    //加载歌手写真
                    ImageUtil.loadSingerImg(mContext, hash, singerName);

                }
            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SINGERIMGLOADED)) {
            //歌手写真加载完成
            if (HPApplication.getInstance().getCurAudioInfo() != null) {
                String hash = intent.getStringExtra("hash");
                if (HPApplication.getInstance().getCurAudioInfo().getHash().equals(hash)) {
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
                    List<SongSingerInfo> list = SongSingerDB.getSongSingerDB(context).getAllSingerImg(singerNameArray, false);
                    mSingerImageView.setSongSingerInfos(mContext, list);
                }
            }
        }
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mScreensWidth = display.getWidth();

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

                if (HPApplication.getInstance().getCurAudioMessage() != null) {
                    mManyLineLyricsView.setManyLineLrc(
                            !mManyLineLyricsView.isManyLineLrc(),
                            (int) HPApplication.getInstance().getCurAudioMessage().getPlayProgress());
                } else {
                    mManyLineLyricsView.setManyLineLrc(!mManyLineLyricsView.isManyLineLrc(), 0);
                }

                HPApplication.getInstance().setManyLineLrc(mManyLineLyricsView.isManyLineLrc());
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
        //翻译歌词
        mHideTranslateImg = findViewById(R.id.hideTranslateImg);
        mHideTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTranslateImg.setVisibility(View.INVISIBLE);
                mShowTranslateImg.setVisibility(View.VISIBLE);
                setExtraLrc();

            }
        });
        mShowTranslateImg = findViewById(R.id.showTranslateImg);
        mShowTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTranslateImg.setVisibility(View.VISIBLE);
                mShowTranslateImg.setVisibility(View.INVISIBLE);

                setTranslateLrc();

            }
        });
        //音译歌词
        mHideTransliterationImg = findViewById(R.id.hideTransliterationImg);
        mHideTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTransliterationImg.setVisibility(View.INVISIBLE);
                mShowTransliterationImg.setVisibility(View.VISIBLE);

                setExtraLrc();

            }
        });
        mShowTransliterationImg = findViewById(R.id.showTransliterationImg);
        mShowTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTransliterationImg.setVisibility(View.VISIBLE);
                mShowTransliterationImg.setVisibility(View.INVISIBLE);

                setTransliterationLrc();
            }
        });

        //翻译歌词/音译歌词
        mShowTTToTranslateImg = findViewById(R.id.showTTToTranslateImg);
        mShowTTToTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                mShowTTToTransliterationImg.setVisibility(View.VISIBLE);
                mHideTTImg.setVisibility(View.INVISIBLE);

                setTranslateLrc();
            }
        });
        mShowTTToTransliterationImg = findViewById(R.id.showTTToTransliterationImg);
        mShowTTToTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                mHideTTImg.setVisibility(View.VISIBLE);

                setTransliterationLrc();
            }
        });
        mHideTTImg = findViewById(R.id.hideTTImg);
        mHideTTImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowTTToTranslateImg.setVisibility(View.VISIBLE);
                mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                mHideTTImg.setVisibility(View.INVISIBLE);

                setExtraLrc();
            }
        });


        //设置额外歌词回调事件
        mManyLineLyricsView.setExtraLyricsListener(new ManyLineLyricsViewV2.ExtraLyricsListener() {

            @Override
            public void hasTranslateLrcCallback() {
                mExtraLrcTypeHandler.sendEmptyMessage(HASTRANSLATELRC);
            }

            @Override
            public void hasTransliterationLrcCallback() {
                mExtraLrcTypeHandler.sendEmptyMessage(HASTRANSLITERATIONLRC);
            }

            @Override
            public void hasTranslateAndTransliterationLrcCallback() {
                mExtraLrcTypeHandler.sendEmptyMessage(HASTRANSLATEANDTRANSLITERATIONLRC);
            }

            @Override
            public void noExtraLrcCallback() {
                mExtraLrcTypeHandler.sendEmptyMessage(NOEXTRALRC);
            }
        });
        //
        mManyLineLyricsView.setOnLrcClickListener(new ManyLineLyricsViewV2.OnLrcClickListener() {
            @Override
            public void onLrcPlayClicked(int progress, boolean isLrcSeekTo) {
                seekToMusic(progress, isLrcSeekTo);
            }
        });

        //
        mRotateLinearLayout.setVerticalScrollView(mManyLineLyricsView);
        //设置字体大小和歌词颜色
        mManyLineLyricsView.setLrcFontSize(HPApplication.getInstance().getLrcFontSize());
        int lrcColor = ColorUtil.parserColor(
                HPApplication.getInstance().getLrcColorStr()
                        [HPApplication.getInstance().getLrcColorIndex()]);
        mManyLineLyricsView.setLrcColor(lrcColor);
        mManyLineLyricsView.setManyLineLrc(HPApplication.getInstance().isManyLineLrc(), 0);
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

        //下载按钮
        mDownloadImgBtn = findViewById(R.id.download_img);
        mDownloadImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AudioInfo audioInfo = HPApplication.getInstance().getCurAudioInfo();
                if (audioInfo != null) {
                    DownloadAudioManager.getDownloadAudioManager(mContext).addTask(audioInfo);
                    mDownloadedImgBtn.setVisibility(View.VISIBLE);
                    mDownloadImgBtn.setVisibility(View.INVISIBLE);
                }

            }
        });
        //已下载按钮
        mDownloadedImgBtn = findViewById(R.id.downloaded_img);
        mDownloadedImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioInfo audioInfo = HPApplication.getInstance().getCurAudioInfo();
                if (audioInfo != null) {
                    DownloadAudioManager.getDownloadAudioManager(mContext).addTask(audioInfo);

                }
            }
        });
        mDownloadedImgBtn.setVisibility(View.INVISIBLE);

        //喜欢按钮
        mLikeImgBtn = findViewById(R.id.liked_menu);
        mLikeImgBtn.setConvert(true);
        mLikeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HPApplication.getInstance().getCurAudioInfo() != null) {
                    ToastUtil.showTextToast(getApplicationContext(), mContext.getString(R.string.cancel_success));

                    mUnLikeImgBtn.setVisibility(View.VISIBLE);
                    mLikeImgBtn.setVisibility(View.GONE);

                    //删除喜欢歌曲
                    Intent delIntent = new Intent(AudioBroadcastReceiver.ACTION_LIKEDELETE);
                    delIntent.putExtra(AudioInfo.KEY, HPApplication.getInstance().getCurAudioInfo());
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
                if (HPApplication.getInstance().getCurAudioInfo() != null) {
                    ToastUtil.showTextToast(getApplicationContext(), mContext.getString(R.string.added_collection));

                    mUnLikeImgBtn.setVisibility(View.GONE);
                    mLikeImgBtn.setVisibility(View.VISIBLE);

                    //添加喜欢歌曲
                    Intent addIntent = new Intent(AudioBroadcastReceiver.ACTION_LIKEADD);
                    addIntent.putExtra(AudioInfo.KEY, HPApplication.getInstance().getCurAudioInfo());
                    addIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(addIntent);
                }
            }
        });
        mUnLikeImgBtn.setVisibility(View.VISIBLE);
        mLikeImgBtn.setVisibility(View.GONE);

//        initPopView(customDialog);
        initPLPopView();
        initSPLPopView();
//        initSIPopView(customDialog);

    }

    private void setTranslateLrc() {
        if (HPApplication.getInstance().getCurAudioMessage() != null) {
            mManyLineLyricsView.setExtraLrcStatus(
                    ManyLineLyricsViewV2.SHOWTRANSLATELRC,
                    (int) HPApplication.getInstance().getCurAudioMessage().getPlayProgress());
        } else {
            mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.SHOWTRANSLATELRC, 0);
        }

        HPApplication.getInstance().setManyLineLrc(mManyLineLyricsView.isManyLineLrc());
    }

    private void setTransliterationLrc() {
        if (HPApplication.getInstance().getCurAudioMessage() != null) {
            mManyLineLyricsView.setExtraLrcStatus(
                    ManyLineLyricsViewV2.SHOWTRANSLITERATIONLRC,
                    (int) HPApplication.getInstance().getCurAudioMessage().getPlayProgress());

        } else {
            mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.SHOWTRANSLITERATIONLRC, 0);
        }
        HPApplication.getInstance().setManyLineLrc(mManyLineLyricsView.isManyLineLrc());
    }

    private void setExtraLrc() {
        if (HPApplication.getInstance().getCurAudioMessage() != null) {
            mManyLineLyricsView.setExtraLrcStatus(
                    ManyLineLyricsViewV2.NOSHOWEXTRALRC,
                    (int) HPApplication.getInstance().getCurAudioMessage().getPlayProgress());
        } else {
            mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.NOSHOWEXTRALRC, 0);
        }

        HPApplication.getInstance().setManyLineLrc(mManyLineLyricsView.isManyLineLrc());
    }

    /**
     * 隐藏歌曲信息
     */
    private void hideSIPopView() {
        mSonginfoPopDialog.dismiss();
    }

    /**
     * 显示歌曲信息
     */
    private void showSPIPopView(final AudioInfo audioInfo) {
        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        mSonginfoPopDialog = builder.setContentView(R.layout.layout_lrc_songinfo_pop)
                .setAnimId(R.style.AnimBottom)
                .setLayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
                .setBackgroundDrawable(true)
                .setDimAmount(0)
                .setGravity(Gravity.BOTTOM)
                .show(new CustomDialog.Builder.onInitListener() {
                    @Override
                    public void init(final CustomDialog customDialog) {
                        initSIPopView(customDialog, audioInfo);
                    }
                });
    }

    /**
     * 初始化歌曲信息窗口
     * @param dialog
     * @param audioInfo
     */
    private void initSIPopView(CustomDialog dialog, AudioInfo audioInfo) {
        LinearLayout cancelLinearLayout = dialog.findViewById(R.id.songcalcel);
        LinearLayout sIPopLinearLayout = dialog.findViewById(R.id.songinfoPopLayout);
        TextView popSingerNameTv = dialog.findViewById(R.id.pop_singerName);
        TextView popFileExtTv = dialog.findViewById(R.id.pop_fileext);
        TextView popTimeTv = dialog.findViewById(R.id.pop_time);
        TextView popFileSizeTv = dialog.findViewById(R.id.pop_filesize);
        //设置歌曲信息
        popSingerNameTv.setText(audioInfo.getSingerName());
        popFileExtTv.setText(audioInfo.getFileExt());
        popTimeTv.setText(audioInfo.getDurationText());
        popFileSizeTv.setText(audioInfo.getFileSizeText());
        sIPopLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSIPopView();
            }
        });
        cancelLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSIPopView();
            }
        });

        sIPopLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSIPopView();
            }
        });
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
        mSPlPLayout.startAnimation(translateAnimation);
    }

    /**
     * 显示歌手列表弹出窗口
     */
    private void showSPLPopView(String[] singerNameArray) {

        //
        mLrcPopSingerListAdapter = new LrcPopSingerListAdapter(mContext, singerNameArray, mLrcActivityListener);
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
        initPLPlayModeView(HPApplication.getInstance().getPlayModel(), modeAllTv, modeRandomTv, modeSingleTv, false);

        //删除播放列表
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
    private void initPLPlayModeView(int playMode, IconfontTextView modeAllImg, IconfontTextView modeRandomImg, IconfontTextView modeSingleImg, boolean isTipShow) {
        if (playMode == 0) {
            if (isTipShow)
                ToastUtil.showTextToast(LrcActivity.this, mContext.getString(R.string.order_play));
            modeAllImg.setVisibility(View.VISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else if (playMode == 1) {
            if (isTipShow)
                ToastUtil.showTextToast(LrcActivity.this, mContext.getString(R.string.random_play));
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.VISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else {
            if (isTipShow)
                ToastUtil.showTextToast(LrcActivity.this, mContext.getString(R.string.single_play));
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 显示播放列表弹出窗口
     */
    private void showPlPopView() {
        initPLPlayModeView(HPApplication.getInstance().getPlayModel(), modeAllTv, modeRandomTv, modeSingleTv, false);

        //加载当前播放列表数据
        List<AudioInfo> curAudioInfos = HPApplication.getInstance().getCurAudioInfos();
        if (curAudioInfos == null) {
            curAudioInfos = new ArrayList<AudioInfo>();
        }
        mCurPLSizeTv.setText(curAudioInfos.size() + "");
        mPopPlayListAdapter = new LrcPopPlayListAdapter(mContext, curAudioInfos);
        mCurRecyclerView.setAdapter(mPopPlayListAdapter);
        //滚动到当前播放位置
        int position = mPopPlayListAdapter.getPlayIndexPosition(HPApplication.getInstance().getCurAudioInfo());
        if (position >= 0)
            mCurRecyclerView.move(position,
                    LinearLayoutRecyclerView.scroll);
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
        mPlPLayout.startAnimation(translateAnimation);
    }

    /**
     * 隐藏popview
     */
    private void hidePopView() {
        mPopDialog.dismiss();
    }

    /**
     * 显示popview
     */
    private void showPopView() {
        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        mPopDialog = builder.setContentView(R.layout.layout_lrc_pop)
                .setAnimId(R.style.AnimBottom)
                .setLayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
                .setBackgroundDrawable(true)
                .setDimAmount(0)
                .setGravity(Gravity.BOTTOM)
                .show(new CustomDialog.Builder.onInitListener() {
                    @Override
                    public void init(final CustomDialog customDialog) {
                        initPopView(customDialog);
                    }
                });
    }

    /**
     * 初始化pop
     * @param dialog
     */
    private void initPopView(CustomDialog dialog) {
        //搜索歌手写真
        ImageView searchSingerImg = dialog.findViewById(R.id.search_singer_pic);
        searchSingerImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HPApplication.getInstance().getCurAudioInfo() == null) {

                    ToastUtil.showTextToast(getApplicationContext(), "请选择歌曲");

                } else {
                    hidePopView();
                    //歌手名称
                    String singerName = HPApplication.getInstance().getCurAudioInfo().getSingerName();
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
        ImageView searchLrcImg = dialog.findViewById(R.id.search_lrc);
        searchLrcImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HPApplication.getInstance().getCurAudioInfo() == null) {

                    ToastUtil.showTextToast(getApplicationContext(), mContext.getString(R.string.please_choose_a_song));

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

        LinearLayout cancelLinearLayout = dialog.findViewById(R.id.calcel);
        LinearLayout popLinearLayout = dialog.findViewById(R.id.lrcPopLayout);
        //歌曲详情
        ImageView songInfoImg = dialog.findViewById(R.id.songinfo);
        songInfoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HPApplication.getInstance().getCurAudioInfo() == null) {

                    ToastUtil.showTextToast(getApplicationContext(), mContext.getString(R.string.please_choose_a_song));

                } else {
                    hidePopView();
                    showSPIPopView(HPApplication.getInstance().getCurAudioInfo());
                }
            }
        });
        popLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidePopView();
            }
        });
        cancelLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidePopView();
            }
        });

        //歌词进度减少按钮
        ButtonRelativeLayout lrcProgressJianBtn = dialog.findViewById(R.id.lyric_progress_jian);
        lrcProgressJianBtn.setDefFillColor(ColorUtil.parserColor(Color.WHITE, 20));
        lrcProgressJianBtn.setPressedFillColor(ColorUtil.parserColor(Color.WHITE, 50));
        lrcProgressJianBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mManyLineLyricsView.getLyricsUtil() != null) {
                    mManyLineLyricsView.getLyricsUtil().setOffset(mManyLineLyricsView.getLyricsUtil().getOffset() + (-500));
                    ToastUtil.showTextToast(LrcActivity.this, (float) mManyLineLyricsView.getLyricsUtil().getOffset() / 1000 + "秒");
                    if (mManyLineLyricsView.getLyricsLineTreeMap() != null) {


                        //保存歌词文件
                        saveLrcFile(mManyLineLyricsView.getLyricsUtil().getLrcFilePath(), mManyLineLyricsView.getLyricsUtil().getLyricsIfno(), mManyLineLyricsView.getLyricsUtil().getPlayOffset());

                    }
                }
            }
        });
        //歌词进度重置
        ButtonRelativeLayout resetProgressJianBtn = dialog.findViewById(R.id.lyric_progress_reset);
        resetProgressJianBtn.setDefFillColor(ColorUtil.parserColor(Color.WHITE, 20));
        resetProgressJianBtn.setPressedFillColor(ColorUtil.parserColor(Color.WHITE, 50));
        resetProgressJianBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mManyLineLyricsView.getLyricsUtil() != null) {
                    mManyLineLyricsView.getLyricsUtil().setOffset(0);
                    ToastUtil.showTextToast(LrcActivity.this, mContext.getString(R.string.restore));
                    if (mManyLineLyricsView.getLyricsLineTreeMap() != null) {

                        //保存歌词文件
                        saveLrcFile(mManyLineLyricsView.getLyricsUtil().getLrcFilePath(), mManyLineLyricsView.getLyricsUtil().getLyricsIfno(), mManyLineLyricsView.getLyricsUtil().getPlayOffset());

                    }
                }
            }
        });
        //歌词进度增加
        ButtonRelativeLayout lrcProgressJiaBtn = dialog.findViewById(R.id.lyric_progress_jia);
        lrcProgressJiaBtn.setDefFillColor(ColorUtil.parserColor(Color.WHITE, 20));
        lrcProgressJiaBtn.setPressedFillColor(ColorUtil.parserColor(Color.WHITE, 50));
        lrcProgressJiaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mManyLineLyricsView.getLyricsUtil() != null) {
                    mManyLineLyricsView.getLyricsUtil().setOffset(mManyLineLyricsView.getLyricsUtil().getOffset() + (500));
                    ToastUtil.showTextToast(LrcActivity.this, (float) mManyLineLyricsView.getLyricsUtil().getOffset() / 1000 + "秒");
                    if (mManyLineLyricsView.getLyricsLineTreeMap() != null) {

                        //保存歌词文件
                        saveLrcFile(mManyLineLyricsView.getLyricsUtil().getLrcFilePath(), mManyLineLyricsView.getLyricsUtil().getLyricsIfno(), mManyLineLyricsView.getLyricsUtil().getPlayOffset());

                    }
                }
            }
        });


        //字体大小
        final LrcSeekBar lrcSizeLrcSeekBar = dialog.findViewById(R.id.fontSizeseekbar);
        lrcSizeLrcSeekBar.setMax(HPApplication.getInstance().getMaxLrcFontSize() - HPApplication.getInstance().getMinLrcFontSize());
        lrcSizeLrcSeekBar.setProgress((HPApplication.getInstance().getLrcFontSize() - HPApplication.getInstance().getMinLrcFontSize()));
        lrcSizeLrcSeekBar.setBackgroundProgressColorColor(ColorUtil.parserColor(Color.WHITE, 50));
        lrcSizeLrcSeekBar.setProgressColor(Color.WHITE);
        lrcSizeLrcSeekBar.setThumbColor(Color.WHITE);
        lrcSizeLrcSeekBar.setOnChangeListener(new LrcSeekBar.OnChangeListener() {
            @Override
            public void onProgressChanged() {
                //logger.e("progress=" + lrcSizeLrcSeekBar.getProgress());
                if (mManyLineLyricsView.getLyricsUtil() != null) {
                    if (mManyLineLyricsView.getLyricsLineTreeMap() != null) {
                        if (HPApplication.getInstance().getCurAudioMessage() != null) {
                            mManyLineLyricsView.setLrcFontSize(
                                    lrcSizeLrcSeekBar.getProgress() + HPApplication.getInstance().getMinLrcFontSize(), (int) HPApplication.getInstance().getCurAudioMessage().getPlayProgress());
                        }
                    }
                } else {
                    mManyLineLyricsView.setLrcFontSize(lrcSizeLrcSeekBar.getProgress() + HPApplication.getInstance().getMinLrcFontSize());
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
                HPApplication.getInstance().setLrcFontSize(lrcSizeLrcSeekBar.getProgress() + HPApplication.getInstance().getMinLrcFontSize());
            }
        });

        //字体减小
        IconfontImageButtonTextView lrcSizeDecrease = dialog.findViewById(R.id.lyric_decrease);
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
        IconfontImageButtonTextView lrcSizeIncrease = dialog.findViewById(R.id.lyric_increase);
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
        ImageView[] colorPanel = new ImageView[HPApplication.getInstance().getLrcColorStr().length];
        final ImageView[] colorStatus = new ImageView[colorPanel.length];

        int i = 0;
        //
        colorPanel[i] = dialog.findViewById(R.id.color_panel1);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setColorPanel(colorStatus, 0);
            }
        });
        colorStatus[i] = dialog.findViewById(R.id.color_status1);

        //
        i++;
        colorPanel[i] = dialog.findViewById(R.id.color_panel2);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setColorPanel(colorStatus, 1);
            }
        });
        colorStatus[i] = dialog.findViewById(R.id.color_status2);

        //
        i++;
        colorPanel[i] = dialog.findViewById(R.id.color_panel3);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setColorPanel(colorStatus, 2);
            }
        });
        colorStatus[i] = dialog.findViewById(R.id.color_status3);

        //
        i++;
        colorPanel[i] = dialog.findViewById(R.id.color_panel4);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setColorPanel(colorStatus, 3);
            }
        });
        colorStatus[i] = dialog.findViewById(R.id.color_status4);

        //
        i++;
        colorPanel[i] = dialog.findViewById(R.id.color_panel5);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setColorPanel(colorStatus, 4);
            }
        });
        colorStatus[i] = dialog.findViewById(R.id.color_status5);

        //
        i++;
        colorPanel[i] = dialog.findViewById(R.id.color_panel6);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setColorPanel(colorStatus, 5);
            }
        });
        colorStatus[i] = dialog.findViewById(R.id.color_status6);

        //
        colorStatus[HPApplication.getInstance().getLrcColorIndex()].setVisibility(View.VISIBLE);

    }

    private void setColorPanel(ImageView[] colorStatus, int curColorPanel) {
        int index = HPApplication.getInstance().getLrcColorIndex();
        if (index != curColorPanel) {
            HPApplication.getInstance().setLrcColorIndex(curColorPanel);
            colorStatus[index].setVisibility(View.GONE);
            colorStatus[curColorPanel].setVisibility(View.VISIBLE);

            int lrcColor = ColorUtil.parserColor(
                    HPApplication.getInstance().getLrcColorStr()[HPApplication.getInstance().getLrcColorIndex()]);
            mManyLineLyricsView.setLrcColor(lrcColor);
        }
    }


    Runnable runnable;

    /**
     * 保存歌词文件
     *
     * @param lrcFilePath lrc歌词路径
     * @param lyricsInfo  lrc歌词数据
     * @param playOffset  lrc歌词快进进度
     */
    private void saveLrcFile(final String lrcFilePath, final LyricsInfo lyricsInfo, final int playOffset) {

        runnable=new Runnable() {
            @Override public void run() {
                Map<String, Object> tags = lyricsInfo.getLyricsTags();
                tags.put(LyricsTag.TAG_OFFSET, playOffset);
                lyricsInfo.setLyricsTags(tags);

                try {
                    LyricsIOUtils.getLyricsFileWriter(lrcFilePath).writer(lyricsInfo, lrcFilePath);  //保存修改的歌词文件
                } catch (Exception e) {  e.printStackTrace();  }

            }  };
       ThreadUtil.runInThread(runnable);

    }


    @Override
    protected void loadData(boolean isRestoreInstance) {
        //
        AudioInfo curAudioInfo = HPApplication.getInstance().getCurAudioInfo();
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
                int playStatus = HPApplication.getInstance().getPlayStatus();
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
        mPauseBtn = findViewById(R.id.pausebtn);
        mPauseBtn.setOnClickListener(new View.OnClickListener() {
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
        initPlayModeView(HPApplication.getInstance().getPlayModel(), modeAllImg, modeRandomImg, modeSingleImg, false);

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
                ToastUtil.showTextToast(LrcActivity.this, mContext.getString(R.string.order_play));
            modeAllImg.setVisibility(View.VISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else if (playMode == 1) {
            if (isTipShow)
                ToastUtil.showTextToast(LrcActivity.this, mContext.getString(R.string.random_play));
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.VISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else {
            if (isTipShow)
                ToastUtil.showTextToast(LrcActivity.this, mContext.getString(R.string.single_play));
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.VISIBLE);
        }
        //
        HPApplication.getInstance().setPlayModel(playMode);
    }

    /**
     * 快进播放
     *
     * @param progress
     * @param isLrcSeekTo
     */
    private void seekToMusic(int progress, boolean isLrcSeekTo) {
        HPApplication.getInstance().setLrcSeekTo(isLrcSeekTo);
        //判断歌词快进时，是否超过歌曲的总时间
        if (HPApplication.getInstance().getCurAudioInfo().getDuration() < progress) {
            progress = (int) HPApplication.getInstance().getCurAudioInfo().getDuration();
        }
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
                    audioMessage.setPlayProgress(progress);
                    Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_SEEKTOMUSIC);
                    resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                    resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(resumeIntent);
                }
            }
        } else {

            if (HPApplication.getInstance().getCurAudioMessage() != null)
                HPApplication.getInstance().getCurAudioMessage().setPlayProgress(progress);

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
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(getApplicationContext());
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(getApplicationContext());

        //在线音乐广播
        mOnLineAudioReceiver = new OnLineAudioReceiver(getApplicationContext());
        mOnLineAudioReceiver.setOnlineAudioReceiverListener(mOnlineAudioReceiverListener);
        mOnLineAudioReceiver.registerReceiver(getApplicationContext());
    }


    @Override protected void onDestroy() {
        mAudioBroadcastReceiver.unregisterReceiver(getApplicationContext()); //注销广播
        mOnLineAudioReceiver.unregisterReceiver(getApplicationContext()); //在线歌曲
        super.onDestroy();
        ThreadUtil.cancelThread(runnable);
    }

    @Override public void onBackPressed() {
        if (isPLPopViewShow) {
            hidePlPopView(); return;
        } else if (isSPLPopViewShow) {
            hideSPLPopView(); return;
        }
        mRotateLinearLayout.finish();
    }

    public interface LrcActivityListener {
        void closeSingerPopListView();
    }


}
