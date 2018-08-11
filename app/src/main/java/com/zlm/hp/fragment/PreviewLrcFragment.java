package com.zlm.hp.fragment;

import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.utils.ColorUtils;
import com.zlm.hp.lyrics.utils.LyricsUtils;
import com.zlm.hp.lyrics.utils.TimeUtils;
import com.zlm.hp.lyrics.widget.AbstractLrcView;
import com.zlm.hp.lyrics.widget.ManyLyricsView;
import com.zlm.hp.ui.MakeLrcActivity;
import com.zlm.hp.ui.R;
import com.zlm.libs.widget.MusicSeekBar;

import java.io.IOException;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 预览歌词界面
 * Created by zhangliangming on 2018-03-25.
 */

public class PreviewLrcFragment extends BaseFragment {
    private MakeLrcActivity.MakeLrcFragmentEvent mMakeLrcFragmentEvent;

    /**
     * 初始化歌曲数据
     */
    private final int INITAUDIODATA = 1;
    private final int AUDIO_PLAY = 2;
    private final int AUDIO_PLAYING = 3;
    private final int AUDIO_PAUSE = 4;
    private final int AUDIO_FINISH = 5;

    private final int INITLRCVIEW = 6;

    /**
     * 歌曲路径
     */
    private String mAudioFilePath;
    /**
     * 进度条
     */
    private MusicSeekBar mMusicSeekBar;
    /**
     * 播放器
     */
    private IjkMediaPlayer mMediaPlayer;
    /**
     * 播放按钮
     */
    private ImageView mPlayImg;
    /**
     * 暂停播放按钮
     */
    private ImageView mPauseImg;

    /**
     * 是否快进
     */
    private boolean isSeekTo = false;

    /**
     * 多行歌词视图
     */
    private ManyLyricsView mManyLyricsView;

    private LyricsInfo mLyricsInfo;

    /**
     * 播放线程
     */
    private Runnable mPlayRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying() && !isSeekTo) {

                mHandler.sendEmptyMessage(AUDIO_PLAYING);

                mHandler.postDelayed(mPlayRunnable, 1000);
            }
        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case INITAUDIODATA:


                    mMusicSeekBar.setEnabled(false);
                    mMusicSeekBar.setProgress(0);
                    mMusicSeekBar.setSecondaryProgress(0);
                    mMusicSeekBar.setMax(0);

                    mPlayImg.setVisibility(View.VISIBLE);
                    mPauseImg.setVisibility(View.INVISIBLE);


                    break;

                case AUDIO_PLAY:


                    if (mMediaPlayer != null) {

                        mMusicSeekBar.setEnabled(true);
                        mMusicSeekBar.setMax((int) mMediaPlayer.getDuration());
                        mMusicSeekBar.setProgress((int) mMediaPlayer.getCurrentPosition());


                        //更新歌词视图
                        if (mManyLyricsView.getLyricsReader() != null && mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY) {
                            mManyLyricsView.play((int) mMediaPlayer.getCurrentPosition());
                        }

                    }

                    mPlayImg.setVisibility(View.INVISIBLE);
                    mPauseImg.setVisibility(View.VISIBLE);

                    mHandler.postDelayed(mPlayRunnable, 0);

                    break;

                case AUDIO_PLAYING:

                    mMusicSeekBar.setProgress((int) mMediaPlayer.getCurrentPosition());

                    break;

                case AUDIO_PAUSE:

                    if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                        mManyLyricsView.pause();
                    }

                    mHandler.removeCallbacks(mPlayRunnable);

                    if (mMediaPlayer != null) {

                        mMediaPlayer.pause();

                        mMusicSeekBar.setProgress((int) mMediaPlayer.getCurrentPosition());
                    }
                    mPlayImg.setVisibility(View.VISIBLE);
                    mPauseImg.setVisibility(View.INVISIBLE);

                    break;

                case AUDIO_FINISH:

                    mHandler.removeCallbacks(mPlayRunnable);

                    mPlayImg.setVisibility(View.VISIBLE);
                    mPauseImg.setVisibility(View.INVISIBLE);

                    mMusicSeekBar.setEnabled(false);
                    mMusicSeekBar.setProgress(0);
                    mMusicSeekBar.setSecondaryProgress(0);
                    mMusicSeekBar.setMax(0);

                    break;

                case INITLRCVIEW:

                    LyricsReader lyricsReader = new LyricsReader();
                    lyricsReader.setLyricsType(mLyricsInfo.getLyricsType());
                    lyricsReader.setLyricsInfo(mLyricsInfo);

                    //原始歌词
                    lyricsReader.setLrcLineInfos(mLyricsInfo.getLyricsLineInfoTreeMap());

                    //翻译歌词
                    if (mLyricsInfo.getTranslateLrcLineInfos() != null) {
                        List<LyricsLineInfo> translateLrcLineInfos = LyricsUtils.getTranslateLrc(mLyricsInfo.getLyricsType(), mLyricsInfo.getLyricsLineInfoTreeMap(), mLyricsInfo.getTranslateLrcLineInfos());
                        lyricsReader.setTranslateLrcLineInfos(translateLrcLineInfos);
                    }

                    //音译歌词
                    if (mLyricsInfo.getTransliterationLrcLineInfos() != null) {
                        List<LyricsLineInfo> transliterationLrcLineInfos = LyricsUtils.getTransliterationLrc(mLyricsInfo.getLyricsType(), mLyricsInfo.getLyricsLineInfoTreeMap(), mLyricsInfo.getTransliterationLrcLineInfos());
                        lyricsReader.setTransliterationLrcLineInfos(transliterationLrcLineInfos);
                    }

                    //加载歌词
                    mManyLyricsView.setLyricsReader(lyricsReader);
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY)
                            mManyLyricsView.play((int) mMediaPlayer.getCurrentPosition());
                    }


                    break;
            }
        }
    };


    @Override
    protected void loadData(boolean isRestoreInstance) {

    }

    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_preview_lrc;
    }

    @Override
    protected int setTitleViewId() {
        return R.layout.layout_close_title;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {


        TextView titleView = mainView.findViewById(R.id.title);
        titleView.setText("预览歌词");

        //返回
        RelativeLayout backImg = mainView.findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                releasePlayer();

                if (mMakeLrcFragmentEvent != null) {
                    mMakeLrcFragmentEvent.close();
                }

            }
        });

        //进度条
        mMusicSeekBar = mainView.findViewById(R.id.seekBar);
        mMusicSeekBar.setTrackingTouchSleepTime(200);
        mMusicSeekBar.setOnMusicListener(new MusicSeekBar.OnMusicListener() {
            @Override
            public String getTimeText() {
                return TimeUtils.parseMMSSString(mMusicSeekBar.getProgress());
            }

            @Override
            public String getLrcText() {
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
                if (mMediaPlayer != null) {
                    mMediaPlayer.seekTo(mMusicSeekBar.getProgress());
                    mManyLyricsView.seekto(mMusicSeekBar.getProgress());
                }
            }
        });
        mMusicSeekBar.setTimePopupWindowViewColor(Color.argb(200, 255, 64, 129));

        //播放
        mPlayImg = mainView.findViewById(R.id.bar_play);
        mPlayImg.setVisibility(View.VISIBLE);
        mPlayImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer == null) {
                    initPlayerData();
                } else if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                    mHandler.sendEmptyMessage(AUDIO_PLAY);

                }
            }
        });

        //暂停
        mPauseImg = mainView.findViewById(R.id.bar_pause);
        mPauseImg.setVisibility(View.INVISIBLE);
        mPauseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mHandler.sendEmptyMessage(AUDIO_PAUSE);

                }
            }
        });

        //多行歌词视图
        mManyLyricsView = mainView.findViewById(R.id.manyLyricsView);
        int paintColor = ColorUtils.parserColor("#555555");
        mManyLyricsView.setPaintColor(new int[]{paintColor, paintColor}, false);
        mManyLyricsView.setPaintLineColor(paintColor);
        int paintHLColor = ColorUtils.parserColor("#0288d1");
        mManyLyricsView.setPaintHLColor(new int[]{paintHLColor, paintHLColor}, false);
        mManyLyricsView.setOnLrcClickListener(new ManyLyricsView.OnLrcClickListener() {
            @Override
            public void onLrcPlayClicked(int seekProgress) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.seekTo(seekProgress);
                    mManyLyricsView.seekto(seekProgress);
                }
            }
        });

        //返回编辑
        Button backMakeLrc = mainView.findViewById(R.id.backMakeLrc);
        backMakeLrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releasePlayer();

                if (mMakeLrcFragmentEvent != null) {
                    mMakeLrcFragmentEvent.prePage(1);
                }
            }
        });

        //完成
        Button finishBtn = mainView.findViewById(R.id.finishBtn);
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releasePlayer();

                if (mMakeLrcFragmentEvent != null) {
                    mMakeLrcFragmentEvent.saveLrcData(mLyricsInfo);
                }
            }
        });
    }

    /**
     * 设置音频路径
     *
     * @param mAudioFilePath
     */
    public void setAudioFilePath(String mAudioFilePath) {
        this.mAudioFilePath = mAudioFilePath;

        if (mAudioFilePath != null && !mAudioFilePath.equals("")) {
            mHandler.sendEmptyMessage(INITAUDIODATA);
        }

    }

    /**
     * 清空数据
     */
    public void resetData() {
        mLyricsInfo = null;
        mManyLyricsView.initLrcData();
        mHandler.sendEmptyMessage(INITAUDIODATA);
    }

    /**
     * 初始化播放器数据
     */
    private void initPlayerData() {

        try {
            mMediaPlayer = new IjkMediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(mAudioFilePath);
            mMediaPlayer.prepareAsync();
            //播放器完成回调
            mMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {

                    mMediaPlayer.release();
                    mMediaPlayer = null;

                    mHandler.sendEmptyMessage(AUDIO_FINISH);
                }
            });

            mMediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(IMediaPlayer mp) {
                    isSeekTo = false;
                }
            });

            mMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {
                    mHandler.sendEmptyMessage(AUDIO_PLAY);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 释放播放器
     */
    private void releasePlayer() {

        if (mAudioFilePath != null && !mAudioFilePath.equals("")) {
            mHandler.sendEmptyMessage(INITAUDIODATA);
        }

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
            mManyLyricsView.pause();
        }
    }

    /**
     * 初始化数据
     *
     * @param audioFilePath
     * @param lyricsInfo
     */
    public void initData(String audioFilePath, LyricsInfo lyricsInfo) {
        setAudioFilePath(audioFilePath);
        this.mLyricsInfo = lyricsInfo;
        mHandler.sendEmptyMessage(INITLRCVIEW);
    }


    @Override
    protected boolean isAddStatusBar() {
        return true;
    }


    public void setMakeLrcFragmentEvent(MakeLrcActivity.MakeLrcFragmentEvent mMakeLrcFragmentEvent) {
        this.mMakeLrcFragmentEvent = mMakeLrcFragmentEvent;
    }
}
