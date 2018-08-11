package com.zlm.hp.fragment;

import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.utils.StringUtils;
import com.zlm.hp.lyrics.utils.TimeUtils;
import com.zlm.hp.ui.MakeLrcActivity;
import com.zlm.hp.ui.R;
import com.zlm.libs.widget.MusicSeekBar;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 编辑歌词界面
 * Created by zhangliangming on 2018-03-25.
 */

public class EditLrcTextFragment extends BaseFragment {
    /**
     * 歌曲路径
     */
    private String mAudioFilePath;

    /**
     * 歌词路径
     */
    private String mLrcFilePath;
    /**
     * lrc歌词编辑
     */
    private EditText mLrcEditText;


    private MakeLrcActivity.MakeLrcFragmentEvent mMakeLrcFragmentEvent;
    /**
     * 初始化歌词数据
     */
    private final int INITLRCDATA = 0;

    /**
     * 初始化歌曲数据
     */
    private final int INITAUDIODATA = 1;

    private final int AUDIO_PLAY = 2;
    private final int AUDIO_PLAYING = 3;
    private final int AUDIO_PAUSE = 4;
    private final int AUDIO_FINISH = 5;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INITLRCDATA:

                    String lrcText = (String) msg.obj;
                    mLrcEditText.setText(lrcText);

                    break;

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

                    }

                    mPlayImg.setVisibility(View.INVISIBLE);
                    mPauseImg.setVisibility(View.VISIBLE);

                    mHandler.postDelayed(mPlayRunnable, 0);

                    break;

                case AUDIO_PLAYING:

                    mMusicSeekBar.setProgress((int) mMediaPlayer.getCurrentPosition());

                    break;

                case AUDIO_PAUSE:

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
            }
        }
    };

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

    @Override
    protected void loadData(boolean isRestoreInstance) {

    }

    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_edit_lrc;
    }

    @Override
    protected int setTitleViewId() {
        return R.layout.layout_close_title;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {


        TextView titleView = mainView.findViewById(R.id.title);
        titleView.setText("编辑歌词");

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

        //lrc编辑修改
        mLrcEditText = mainView.findViewById(R.id.lrctext_edittext);
        //进度条
        mMusicSeekBar = mainView.findViewById(R.id.seekBar);
        mMusicSeekBar.setTrackingTouchSleepTime(1000);
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

        //节奏按钮
        Button playLrc = mainView.findViewById(R.id.goto_play);
        playLrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lrcText = mLrcEditText.getText().toString();
                if (StringUtils.isBlank(lrcText)) {
                    Toast.makeText(mActivity.getApplicationContext(), "请输入歌词！", Toast.LENGTH_SHORT).show();
                } else {

                    releasePlayer();

                    if (mMakeLrcFragmentEvent != null) {
                        mMakeLrcFragmentEvent.nextPage(1);
                    }
                }
            }
        });
    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }


    public void setAudioFilePath(String mAudioFilePath) {
        this.mAudioFilePath = mAudioFilePath;

        if (mAudioFilePath != null && !mAudioFilePath.equals("")) {
            mHandler.sendEmptyMessage(INITAUDIODATA);
        }

    }

    public void setLrcFilePath(String mLrcFilePath) {
        this.mLrcFilePath = mLrcFilePath;
        if (mLrcFilePath != null && !mLrcFilePath.equals("")) {
            loadLrcData();
        }
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
    private void releasePlayer(){

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
    }

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

    /**
     * 加载歌词数据
     */
    private void loadLrcData() {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {

                try {

                    File lrcFile = new File(mLrcFilePath);
                    LyricsReader lyricsReader = new LyricsReader();
                    lyricsReader.loadLrc(lrcFile);
                    if (lyricsReader.getLrcLineInfos() != null && lyricsReader.getLrcLineInfos().size() > 0) {
                        StringBuilder lrcTextSB = new StringBuilder();
                        for (int i = 0; i < lyricsReader.getLrcLineInfos().size(); i++) {
                            String lineLyrics = lyricsReader.getLrcLineInfos().get(i).getLineLyrics();
                            lrcTextSB.append(lineLyrics + "\n");
                        }

                        //发送初始化歌词信息

                        Message msg = new Message();
                        msg.what = INITLRCDATA;
                        msg.obj = lrcTextSB.toString();
                        mHandler.sendMessage(msg);


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                return null;
            }
        }.execute("");
    }

    public void setMakeLrcFragmentEvent(MakeLrcActivity.MakeLrcFragmentEvent mMakeLrcFragmentEvent) {
        this.mMakeLrcFragmentEvent = mMakeLrcFragmentEvent;
    }

    /**
     * 获取歌词文本内容
     *
     * @return
     */

    public String getLrcComText() {
        return mLrcEditText.getText().toString();
    }

}
