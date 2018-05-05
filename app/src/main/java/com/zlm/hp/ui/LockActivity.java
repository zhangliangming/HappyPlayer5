package com.zlm.hp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlm.hp.db.SongSingerDB;
import com.zlm.hp.libs.utils.ColorUtil;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.widget.AbstractLrcView;
import com.zlm.hp.lyrics.widget.ManyLyricsView;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.model.SongSingerInfo;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.utils.AniUtil;
import com.zlm.hp.utils.ImageUtil;
import com.zlm.hp.widget.SingerImageView;
import com.zlm.hp.widget.lock.LockButtonRelativeLayout;
import com.zlm.hp.widget.lock.LockPalyOrPauseButtonRelativeLayout;
import com.zlm.libs.widget.SwipeBackLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * @Description: 锁屏界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2018/1/19 16:07
 * @Throws:
 */
public class LockActivity extends BaseActivity {

    private final int HASTRANSLATELRC = 0;
    private final int HASTRANSLITERATIONLRC = 1;
    private final int HASTRANSLATEANDTRANSLITERATIONLRC = 2;
    private final int NOEXTRALRC = 3;
    private final int INITDATA = 1;
    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;
    /**
     * 滑动提示图标
     */
    private ImageView lockImageView;
    private AnimationDrawable aniLoading;
    /**
     * 时间
     */
    private TextView timeTextView;
    /**
     * 日期
     */
    private TextView dateTextView;
    /**
     * 星期几
     */
    private TextView dayTextView;
    /**
     * 歌名
     */
    private TextView songNameTextView;
    /**
     * 歌手
     */
    private TextView songerTextView;
    //暂停、播放图标
    private ImageView playImageView;
    private ImageView pauseImageView;
    /**
     * 上一首按钮
     */
    private LockButtonRelativeLayout prewButton;
    /**
     * 下一首按钮
     */
    private LockButtonRelativeLayout nextButton;
    /**
     * 播放或者暂停按钮
     */
    private LockPalyOrPauseButtonRelativeLayout playOrPauseButton;
    /**
     * 音频广播
     */
    private AudioBroadcastReceiver mAudioBroadcastReceiver;
    /**
     * 分钟广播
     */
    private Handler mTimeHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            setDate();
        }
    };
    /**
     * 分钟变化广播
     */
    private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                mTimeHandler.sendEmptyMessage(0);
            }
        }
    };
    /**
     * 歌手写真
     */
    private SingerImageView mSingerImageView;
    /**
     * 多行歌词视图
     */
    private ManyLyricsView mManyLineLyricsView;
    /**
     * 广播监听
     */
    private AudioBroadcastReceiver.AudioReceiverListener mAudioReceiverListener = new AudioBroadcastReceiver.AudioReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doAudioReceive(context, intent);
        }
    };
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
    /**
     * 屏幕宽度
     */
    private int mScreensWidth;
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


                    //翻译歌词/音译歌词
                    mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    mHideTTImg.setVisibility(View.VISIBLE);

                    break;
                case HASTRANSLITERATIONLRC:

                    //翻译歌词
                    mHideTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTranslateImg.setVisibility(View.INVISIBLE);


                    //音译歌词
                    mHideTransliterationImg.setVisibility(View.VISIBLE);
                    mShowTransliterationImg.setVisibility(View.INVISIBLE);


                    //翻译歌词/音译歌词
                    mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    mHideTTImg.setVisibility(View.INVISIBLE);

                    break;
                case HASTRANSLATELRC:


                    //翻译歌词
                    mHideTranslateImg.setVisibility(View.VISIBLE);
                    mShowTranslateImg.setVisibility(View.INVISIBLE);


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
    /**
     *
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case INITDATA:
                    initData();
                    break;
            }
        }
    };


    //、、、、、、、、、、、、、、、、、、、、、、、、、翻译和音译歌词、、、、、、、、、、、、、、、、、、、、、、、、、、、


    @Override
    protected int setContentViewId() {
        return R.layout.activity_lock;
    }

    @Override
    protected void preLoad() {
        super.preLoad();
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void contentViewFinish(View contentView) {
        //
        mSwipeBackLayout = contentView.findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setContentView(R.layout.activity_lock_layout, SwipeBackLayout.CONTENTVIEWTYPE_RELATIVELAYOUT);
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        //

        mSwipeBackLayout.setSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {
            @Override
            public void finishActivity() {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        //提示右滑动图标
        lockImageView = findViewById(R.id.tip_image);
        aniLoading = (AnimationDrawable) lockImageView.getBackground();

        //时间
        timeTextView = findViewById(R.id.time);
        dateTextView = findViewById(R.id.date);
        dayTextView = findViewById(R.id.day);

        //歌手与歌名
        songNameTextView = findViewById(R.id.songName);
        songerTextView = findViewById(R.id.songer);


        playImageView = findViewById(R.id.play);
        pauseImageView = findViewById(R.id.pause);
        //播放按钮、上一首，下一首
        prewButton = findViewById(R.id.prev_button);
        prewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //
                Intent preIntent = new Intent(AudioBroadcastReceiver.ACTION_PREMUSIC);
                preIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(preIntent);

            }
        });

        nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //
                Intent nextIntent = new Intent(AudioBroadcastReceiver.ACTION_NEXTMUSIC);
                nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(nextIntent);

            }
        });

        playOrPauseButton = findViewById(R.id.play_pause_button);
        playOrPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int playStatus = mHPApplication.getPlayStatus();
                if (playStatus == AudioPlayerManager.PLAYING) {

                    Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_PAUSEMUSIC);
                    resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(resumeIntent);

                } else {
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
            }
        });


        //歌手写真
        mSingerImageView = findViewById(R.id.singerimg);
        mSingerImageView.setVisibility(View.INVISIBLE);

        //多行歌词

        //
        mManyLineLyricsView = findViewById(R.id.lock_manyLineLyricsView);
        //不能触摸和点击事件
        mManyLineLyricsView.setTouchAble(false);
        //翻译歌词
        mHideTranslateImg = findViewById(R.id.hideTranslateImg);
        mHideTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTranslateImg.setVisibility(View.INVISIBLE);
                mShowTranslateImg.setVisibility(View.VISIBLE);


                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                }

            }
        });
        mShowTranslateImg = findViewById(R.id.showTranslateImg);
        mShowTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTranslateImg.setVisibility(View.VISIBLE);
                mShowTranslateImg.setVisibility(View.INVISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);

                }

            }
        });
        //音译歌词
        mHideTransliterationImg = findViewById(R.id.hideTransliterationImg);
        mHideTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTransliterationImg.setVisibility(View.INVISIBLE);
                mShowTransliterationImg.setVisibility(View.VISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                }

            }
        });
        mShowTransliterationImg = findViewById(R.id.showTransliterationImg);
        mShowTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTransliterationImg.setVisibility(View.VISIBLE);
                mShowTransliterationImg.setVisibility(View.INVISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                }
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

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);
                }
            }
        });
        mShowTTToTransliterationImg = findViewById(R.id.showTTToTransliterationImg);
        mShowTTToTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                mHideTTImg.setVisibility(View.VISIBLE);


                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                }
            }
        });
        mHideTTImg = findViewById(R.id.hideTTImg);
        mHideTTImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowTTToTranslateImg.setVisibility(View.VISIBLE);
                mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                mHideTTImg.setVisibility(View.INVISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                }
            }
        });


        //设置额外歌词回调事件
        mManyLineLyricsView.setExtraLyricsListener(new AbstractLrcView.ExtraLyricsListener() {
            @Override
            public void extraLrcCallback() {
                int extraLrcType = mManyLineLyricsView.getExtraLrcType();
                if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_NOLRC) {
                    mExtraLrcTypeHandler.sendEmptyMessage(NOEXTRALRC);
                } else if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_TRANSLATELRC) {
                    mExtraLrcTypeHandler.sendEmptyMessage(HASTRANSLATELRC);
                } else if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_TRANSLITERATIONLRC) {
                    mExtraLrcTypeHandler.sendEmptyMessage(HASTRANSLITERATIONLRC);
                } else if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_BOTH) {
                    mExtraLrcTypeHandler.sendEmptyMessage(HASTRANSLATEANDTRANSLITERATIONLRC);
                }


            }
        });


        //
        //设置字体大小和歌词颜色
        mManyLineLyricsView.setSize(mHPApplication.getLrcFontSize(), mHPApplication.getLrcFontSize(), false);
        int lrcColor = ColorUtil.parserColor(mHPApplication.getLrcColorStr()[mHPApplication.getLrcColorIndex()]);
        mManyLineLyricsView.setPaintHLColor(new int[]{lrcColor, lrcColor}, false);
        mManyLineLyricsView.setPaintColor(new int[]{Color.WHITE, Color.WHITE}, false);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mScreensWidth = display.getWidth();

        //注册广播
        registerReceiver();

    }

    private void registerReceiver() {


        //注册接收音频播放广播
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(getApplicationContext(), mHPApplication);
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(getApplicationContext());

        //注册分钟变化广播
        IntentFilter mTimeFilter = new IntentFilter();
        mTimeFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mTimeReceiver, mTimeFilter);
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        mHandler.sendEmptyMessage(INITDATA);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        AniUtil.startAnimation(aniLoading);
        setDate();

        //加载音频数据
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
     * 设置日期
     */
    private void setDate() {

        String str = "";
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

        Calendar lastDate = Calendar.getInstance();
        str = sdfDate.format(lastDate.getTime());
        dateTextView.setText(str);
        str = sdfTime.format(lastDate.getTime());
        timeTextView.setText(str);

        String mWay = String.valueOf(lastDate.get(Calendar.DAY_OF_WEEK));
        if ("1".equals(mWay)) {
            mWay = "日";
        } else if ("2".equals(mWay)) {
            mWay = "一";
        } else if ("3".equals(mWay)) {
            mWay = "二";
        } else if ("4".equals(mWay)) {
            mWay = "三";
        } else if ("5".equals(mWay)) {
            mWay = "四";
        } else if ("6".equals(mWay)) {
            mWay = "五";
        } else if ("7".equals(mWay)) {
            mWay = "六";
        }
        dayTextView.setText("星期" + mWay);

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

            songNameTextView.setText(R.string.def_songName);
            songerTextView.setText(R.string.def_artist);

            playImageView.setVisibility(View.VISIBLE);
            pauseImageView.setVisibility(View.INVISIBLE);

            playOrPauseButton.setPlayingProgress(0);
            playOrPauseButton.setMaxProgress(0);
            playOrPauseButton.invalidate();


            //歌手写真
            mSingerImageView.setVisibility(View.INVISIBLE);
            mSingerImageView.setSongSingerInfos(mHPApplication, getApplicationContext(), null);

            //
            mManyLineLyricsView.initLrcData();


        } else if (action.equals(AudioBroadcastReceiver.ACTION_INITMUSIC)) {

            //初始化
            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            AudioInfo audioInfo = mHPApplication.getCurAudioInfo();


            songNameTextView.setText(audioInfo.getSongName());
            songerTextView.setText(audioInfo.getSingerName());

            if (mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYING) {
                playImageView.setVisibility(View.INVISIBLE);
                pauseImageView.setVisibility(View.VISIBLE);
            } else {
                playImageView.setVisibility(View.VISIBLE);
                pauseImageView.setVisibility(View.INVISIBLE);
            }

            playOrPauseButton.setMaxProgress((int) audioInfo
                    .getDuration());
            playOrPauseButton.setPlayingProgress((int) audioMessage.getPlayProgress());
            playOrPauseButton.invalidate();


            //
            mSingerImageView.setVisibility(View.INVISIBLE);
            mSingerImageView.setSongSingerInfos(mHPApplication, getApplicationContext(), null);
            //加载歌手写真
            ImageUtil.loadSingerImg(mHPApplication, getApplicationContext(), audioInfo.getHash(), audioInfo.getSingerName());


            //加载歌词
            String keyWords = "";
            if (audioInfo.getSingerName().equals("未知")) {
                keyWords = audioInfo.getSongName();
            } else {
                keyWords = audioInfo.getSingerName() + " - " + audioInfo.getSongName();
            }
            LyricsManager.getLyricsManager(mHPApplication, getApplicationContext()).loadLyricsUtil(keyWords, keyWords, audioInfo.getDuration() + "", audioInfo.getHash());

            //
            mManyLineLyricsView.initLrcData();
            //加载中
            mManyLineLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYMUSIC)) {

            //播放

            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);

            if (pauseImageView.getVisibility() != View.VISIBLE) {
                pauseImageView.setVisibility(View.VISIBLE);
            }
            if (playImageView.getVisibility() != View.INVISIBLE) {
                playImageView.setVisibility(View.INVISIBLE);
            }
            playOrPauseButton.setPlayingProgress((int) audioMessage.getPlayProgress());
            playOrPauseButton.invalidate();

            if (audioMessage != null) {


                AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                if (audioInfo != null) {

                    //更新歌词

                    if (mManyLineLyricsView.getLyricsReader() != null && mManyLineLyricsView.getLyricsReader().getHash().equals(audioInfo.getHash()) && mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLineLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY) {
                        mManyLineLyricsView.play((int) audioMessage.getPlayProgress());
                    }
                }

            }


        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PAUSEMUSIC)) {
            //暂停完成
            pauseImageView.setVisibility(View.INVISIBLE);
            playImageView.setVisibility(View.VISIBLE);

            if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                mManyLineLyricsView.pause();
            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_RESUMEMUSIC)) {
            //唤醒完成
            pauseImageView.setVisibility(View.VISIBLE);
            playImageView.setVisibility(View.INVISIBLE);

            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
            if (audioMessage != null) {
                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.play((int) audioMessage.getPlayProgress());
                }
            }


        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_SEEKTOMUSIC)) {
            //唤醒完成
            pauseImageView.setVisibility(View.VISIBLE);
            playImageView.setVisibility(View.INVISIBLE);
            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
            if (audioMessage != null) {
                if (mManyLineLyricsView != null && mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.play((int) audioMessage.getPlayProgress());
                }
            }
        }else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYINGMUSIC)) {
            //播放中
            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            if (audioMessage != null) {

                playOrPauseButton.setPlayingProgress((int) audioMessage.getPlayProgress());
                playOrPauseButton.invalidate();

            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_LRCLOADED)) {

            //歌词加载完成
            AudioMessage curAudioMessage = mHPApplication.getCurAudioMessage();
            AudioMessage audioMessage = (AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            String hash = audioMessage.getHash();
            if (hash.equals(mHPApplication.getCurAudioInfo().getHash())) {
                //
                LyricsReader lyricsReader = LyricsManager.getLyricsManager(mHPApplication, getApplicationContext()).getLyricsUtil(hash);
                if (lyricsReader != null) {
                    lyricsReader.setHash(hash);

                    mManyLineLyricsView.setLyricsReader(lyricsReader);
                    if (mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYING && mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLineLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY)
                        mManyLineLyricsView.play((int) curAudioMessage.getPlayProgress());
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
                    List<SongSingerInfo> list = SongSingerDB.getSongSingerDB(context).getAllSingerImg(singerNameArray, false);
                    mSingerImageView.setSongSingerInfos(mHPApplication, getApplicationContext(), list);
                }
            }
        }
    }


    @Override
    protected boolean isAddStatusBar() {
        setStatusColor(Color.TRANSPARENT);
        return true;
    }

    @Override
    public int setStatusBarParentView() {
        return R.id.status_parent_view;
    }

    @Override
    public void finish() {
        AniUtil.stopAnimation(aniLoading);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        //注销广播
        mAudioBroadcastReceiver.unregisterReceiver(getApplicationContext());
        //注销分钟变化广播
        unregisterReceiver(mTimeReceiver);
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) { // 屏蔽按键
        if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
