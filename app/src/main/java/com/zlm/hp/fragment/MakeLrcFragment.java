package com.zlm.hp.fragment;

import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zlm.hp.adapter.MakeLrcAdapter;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.model.MakeLrcLineInfo;
import com.zlm.hp.lyrics.utils.LyricsUtils;
import com.zlm.hp.lyrics.utils.TimeUtils;
import com.zlm.hp.ui.MakeLrcActivity;
import com.zlm.hp.ui.R;
import com.zlm.hp.utils.StringUtils;
import com.zlm.hp.widget.LinearLayoutRecyclerView;
import com.zlm.libs.widget.MusicSeekBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 敲打歌词界面
 * Created by zhangliangming on 2018-03-25.
 */

public class MakeLrcFragment extends BaseFragment {

    /**
     * 初始化歌曲数据
     */
    private final int INITAUDIODATA = 1;
    private final int AUDIO_PLAY = 2;
    private final int AUDIO_PLAYING = 3;
    private final int AUDIO_PAUSE = 4;
    private final int AUDIO_FINISH = 5;
    /**
     * 歌词列表
     */
    /**
     * 歌词列表
     */
    private ArrayList<MakeLrcLineInfo> mMakeLrcs = new ArrayList<MakeLrcLineInfo>();
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


    private MakeLrcActivity.MakeLrcFragmentEvent mMakeLrcFragmentEvent;
    /**
     * 制作歌词list视图
     */
    private LinearLayoutRecyclerView mLinearLayoutRecyclerView;
    /**
     *
     */
    private MakeLrcAdapter mMakeLrcAdapter;
    /**
     * 回滚
     */
    private Button mBackPlayBtn;
    /**
     * 敲打
     */
    private Button mPlayBtn;
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

                    mBackPlayBtn.setEnabled(false);
                    mPlayBtn.setEnabled(false);

                    mMusicSeekBar.setEnabled(false);
                    mMusicSeekBar.setProgress(0);
                    mMusicSeekBar.setSecondaryProgress(0);
                    mMusicSeekBar.setMax(0);

                    mPlayImg.setVisibility(View.VISIBLE);
                    mPauseImg.setVisibility(View.INVISIBLE);

                    //
                    mMakeLrcAdapter.reset();

                    break;

                case AUDIO_PLAY:

                    mBackPlayBtn.setEnabled(true);
                    mPlayBtn.setEnabled(true);

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

                    mBackPlayBtn.setEnabled(false);
                    mPlayBtn.setEnabled(false);

                    mHandler.removeCallbacks(mPlayRunnable);

                    if (mMediaPlayer != null) {

                        mMediaPlayer.pause();

                        mMusicSeekBar.setProgress((int) mMediaPlayer.getCurrentPosition());
                    }
                    mPlayImg.setVisibility(View.VISIBLE);
                    mPauseImg.setVisibility(View.INVISIBLE);

                    break;

                case AUDIO_FINISH:

                    mBackPlayBtn.setEnabled(false);
                    mPlayBtn.setEnabled(false);

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

    @Override
    protected void loadData(boolean isRestoreInstance) {

    }

    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_make_lrc;
    }

    @Override
    protected int setTitleViewId() {
        return R.layout.layout_close_title;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {


        TextView titleView = mainView.findViewById(R.id.title);
        titleView.setText("敲打节奏");

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

        //返回编辑按钮
        Button mBackEditBtn = mainView.findViewById(R.id.backEdit);
        mBackEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releasePlayer();

                if (mMakeLrcFragmentEvent != null) {
                    mMakeLrcFragmentEvent.prePage(0);
                }
            }
        });

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

        //
        mLinearLayoutRecyclerView = mainView.findViewById(R.id.listview);
        //初始化内容视图
        mLinearLayoutRecyclerView.setLinearLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        DividerItemDecoration divider = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        mLinearLayoutRecyclerView.addItemDecoration(divider);

        mMakeLrcAdapter = new MakeLrcAdapter(getActivity().getApplicationContext(), mMakeLrcs);
        mLinearLayoutRecyclerView.setAdapter(mMakeLrcAdapter);

        //回滚按钮
        mBackPlayBtn = mainView.findViewById(R.id.backplayBtn);
        mBackPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    MakeLrcLineInfo makeLrcLineInfo = mMakeLrcAdapter.getCurMakeLrcLineInfo();
                    if (makeLrcLineInfo != null && makeLrcLineInfo.getStatus() != MakeLrcLineInfo.STATUS_FINISH) {
                        makeLrcLineInfo.back();
                        mMakeLrcAdapter.reshSelectedIndexView();
                    }
                }
            }
        });
        mBackPlayBtn.setEnabled(false);

        //敲打按钮
        mPlayBtn = mainView.findViewById(R.id.playBtn);
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    MakeLrcLineInfo makeLrcLineInfo = mMakeLrcAdapter.getCurMakeLrcLineInfo();
                    if (makeLrcLineInfo != null && makeLrcLineInfo.getStatus() != MakeLrcLineInfo.STATUS_FINISH) {
                        boolean isFinish = makeLrcLineInfo.play(mMediaPlayer.getCurrentPosition());
                        if (isFinish) {

                            mMakeLrcAdapter.setNextSelectIndex();

                            int selectedIndex = mMakeLrcAdapter.getSelectedIndex();
                            if (selectedIndex >= 0 && selectedIndex < mMakeLrcs.size()) {
                                mLinearLayoutRecyclerView.moveToMiddle(selectedIndex);
                            }

                        } else {
                            mMakeLrcAdapter.reshSelectedIndexView();
                        }
                    }
                }
            }
        });
        mPlayBtn.setEnabled(false);

        //预览按钮
        Button preBtn = mainView.findViewById(R.id.preBtn);
        preBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                if (mMakeLrcs == null || mMakeLrcs.size() == 0) {
                    Toast.makeText(mActivity.getApplicationContext(), "歌词录制内容不能为空!！", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < mMakeLrcs.size(); i++) {
                    if (mMakeLrcs.get(i).getStatus() != MakeLrcLineInfo.STATUS_FINISH) {
                        Toast.makeText(mActivity.getApplicationContext(), "歌词录制完成才可以预览！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                releasePlayer();

                if (mMakeLrcFragmentEvent != null) {

                    //初始化歌词数据
                    LyricsInfo lyricsInfo = new LyricsInfo();
                    lyricsInfo.setLyricsType(LyricsInfo.DYNAMIC);
                    TreeMap<Integer, LyricsLineInfo> lrcLineInfos = new TreeMap<Integer, LyricsLineInfo>();
                    for (int i = 0; i < mMakeLrcs.size(); i++) {
                        lrcLineInfos.put(i, mMakeLrcs.get(i).getFinishLrcLineInfo());
                    }
                    lyricsInfo.setLyricsLineInfoTreeMap(lrcLineInfos);


                    mMakeLrcFragmentEvent.openPreView(lyricsInfo);
                }
            }
        });
    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }

    public void setMakeLrcFragmentEvent(MakeLrcActivity.MakeLrcFragmentEvent mMakeLrcFragmentEvent) {
        this.mMakeLrcFragmentEvent = mMakeLrcFragmentEvent;
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
        mMakeLrcs.clear();
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
    }

    /**
     * 初始化数据
     *
     * @param audioFilePath
     * @param lrcComText
     */
    public void initData(String audioFilePath, String lrcComText) {
        setAudioFilePath(audioFilePath);
        paseLrcComText(lrcComText);
    }

    /**
     * 解析歌词内容
     *
     * @param lrcComText
     */
    private void paseLrcComText(final String lrcComText) {

        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {

                String lrcComTexts[] = lrcComText.split("\n");
                for (int i = 0; i < lrcComTexts.length; i++) {
                    String lineLyrics = lrcComTexts[i];
                    if (StringUtils.isEmpty(lineLyrics)
                            || StringUtils.isBlank(lineLyrics)) {
                        continue;
                    }
                    //
                    LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
                    lyricsLineInfo.setLineLyrics(lineLyrics);

                    //获取该行歌词的字数组
                    String[] mLyricsWords = LyricsUtils.getLyricsWords(lineLyrics);
                    lyricsLineInfo.setLyricsWords(mLyricsWords);

                    //
                    MakeLrcLineInfo makeLrcLineInfo = new MakeLrcLineInfo();
                    makeLrcLineInfo.setLyricsLineInfo(lyricsLineInfo);
                    mMakeLrcs.add(makeLrcLineInfo);
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                mMakeLrcAdapter.notifyDataSetChanged();
            }
        }.execute("");

    }

}
