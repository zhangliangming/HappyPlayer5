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

import com.zlm.hp.adapter.MakeExtraLrcAdapter;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.model.MakeExtraLrcLineInfo;
import com.zlm.hp.lyrics.model.TranslateLrcLineInfo;
import com.zlm.hp.lyrics.utils.StringUtils;
import com.zlm.hp.lyrics.utils.TimeUtils;
import com.zlm.hp.ui.MakeTranslateLrcActivity;
import com.zlm.hp.ui.MakeLrcActivity;
import com.zlm.hp.ui.R;
import com.zlm.hp.widget.LinearLayoutRecyclerView;
import com.zlm.libs.widget.MusicSeekBar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 制作额外歌词界面
 * Created by zhangliangming on 2018-03-31.
 */

public class MakeExtraLrcFragment extends BaseFragment {

    /**
     * 初始化歌曲数据
     */
    private final int INITAUDIODATA = 1;
    private final int AUDIO_PLAY = 2;
    private final int AUDIO_PLAYING = 3;
    private final int AUDIO_PAUSE = 4;
    private final int AUDIO_FINISH = 5;

    private final int INITTITLE = 6;

    /**
     * 歌词列表
     */
    private ArrayList<MakeExtraLrcLineInfo> mMakeLrcs = new ArrayList<MakeExtraLrcLineInfo>();
    /**
     * 歌曲路径
     */
    private String mAudioFilePath;

    /**
     * 歌词路径
     */
    private String mLrcFilePath;

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
    private MakeExtraLrcAdapter mMakeExtraLrcAdapter;

    /**
     *
     */
    private MakeExtraLrcAdapter.ItemEvent mItemEvent;

    private TextView mTitleView;

    /**
     * 翻译
     */
    private  LyricsInfo mLyricsInfo;

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

                    mMakeExtraLrcAdapter.reset();

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

                case INITTITLE:

                    if (mExtraLrcType == 0) {
                        mTitleView.setText("音译歌词");
                    } else {
                        mTitleView.setText("翻译歌词");
                    }

                    break;

            }
        }
    };

    private MakeTranslateLrcActivity.ExtraItemEvent mExtraItemEvent;
    /**
     * 额外歌词类型,0是音译，1是翻译
     */
    private int mExtraLrcType;

    @Override
    protected void loadData(boolean isRestoreInstance) {

    }

    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_make_extra_lrc;
    }

    @Override
    protected int setTitleViewId() {
        return R.layout.layout_close_title;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {

        mTitleView = mainView.findViewById(R.id.title);
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


        mMakeExtraLrcAdapter = new MakeExtraLrcAdapter(getActivity().getApplicationContext(), mMakeLrcs);
        mItemEvent = new MakeExtraLrcAdapter.ItemEvent() {
            @Override
            public void itemClick(int index) {
                if (mExtraItemEvent != null) {
                    mExtraItemEvent.itemClick(index);
                }
            }
        };
        mMakeExtraLrcAdapter.setItemEvent(mItemEvent);
        mLinearLayoutRecyclerView.setAdapter(mMakeExtraLrcAdapter);


        //预览按钮
        Button preBtn = mainView.findViewById(R.id.preBtn);
        preBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                releasePlayer();

                if (mMakeLrcFragmentEvent != null) {

                    //初始化歌词数据
                    LyricsInfo lyricsInfo = new LyricsInfo();
                    lyricsInfo.setLyricsType(LyricsInfo.DYNAMIC);

                    TreeMap<Integer, LyricsLineInfo> lyricsLineInfoTreeMap = new TreeMap<Integer, LyricsLineInfo>();
                    List<TranslateLrcLineInfo> mTranslateLrcLineInfos = new ArrayList<TranslateLrcLineInfo>();
                    List<LyricsLineInfo> transliterationLrcLineInfos = new ArrayList<LyricsLineInfo>();
                    for (int i = 0; i < mMakeLrcs.size(); i++) {
                        MakeExtraLrcLineInfo makeExtraLrcLineInfo = mMakeLrcs.get(i);
                        //原始歌词
                        LyricsLineInfo lyricsLineInfo = makeExtraLrcLineInfo.getLyricsLineInfo();
                        lyricsLineInfoTreeMap.put(i, lyricsLineInfo);

                        if (mExtraLrcType == 0) {
                            String[] lyricsWords = lyricsLineInfo.getLyricsWords();
                            //音译
                            LyricsLineInfo transliterationLrcLineInfo = new LyricsLineInfo();
                            String extraLineLyrics = makeExtraLrcLineInfo.getExtraLineLyrics();
                            if (extraLineLyrics == null) {
                                extraLineLyrics = "";
                            } else {
                                extraLineLyrics = extraLineLyrics.trim();
                            }
                            if (StringUtils.isBlank(extraLineLyrics)) {
                                String[] extraLyricsWords = new String[lyricsWords.length];
                                for (int j = 0; j < extraLyricsWords.length; j++) {
                                    extraLyricsWords[j] = "";
                                }
                                transliterationLrcLineInfo.setLyricsWords(extraLyricsWords);
                            } else {
                                String[] extraLyricsWords = extraLineLyrics.split("∮");
                                //歌词字验证
                                if (lyricsWords.length != extraLyricsWords.length) {

                                    Toast.makeText(mActivity.getApplicationContext(), "第" + String.format("%0" + (mMakeLrcs.size() + "").length() + "d", (i + 1)) + "行歌词未完成，请先完成后再预览!", Toast.LENGTH_SHORT).show();

                                    return;
                                }
                                for (int j = 0; j < extraLyricsWords.length; j++) {
                                    extraLyricsWords[j] = extraLyricsWords[j].trim();
                                }
                                transliterationLrcLineInfo.setLyricsWords(extraLyricsWords);
                            }
                            transliterationLrcLineInfo.setLineLyrics(extraLineLyrics);
                            transliterationLrcLineInfos.add(transliterationLrcLineInfo);
                        } else {
                            //翻译歌词
                            TranslateLrcLineInfo translateLrcLineInfo = new TranslateLrcLineInfo();
                            String extraLineLyrics = makeExtraLrcLineInfo.getExtraLineLyrics();
                            if (extraLineLyrics == null) {
                                extraLineLyrics = "";
                            } else {
                                extraLineLyrics = extraLineLyrics.trim();
                            }
                            if (StringUtils.isBlank(extraLineLyrics)) {
                                extraLineLyrics = "";
                            }
                            translateLrcLineInfo.setLineLyrics(extraLineLyrics);
                            mTranslateLrcLineInfos.add(translateLrcLineInfo);
                        }
                    }
                    //
                    if (mExtraLrcType == 0) {
                        //音译
                        lyricsInfo.setTransliterationLrcLineInfos(transliterationLrcLineInfos);
                    } else {
                        lyricsInfo.setTranslateLrcLineInfos(mTranslateLrcLineInfos);
                    }
                    lyricsInfo.setLyricsLineInfoTreeMap(lyricsLineInfoTreeMap);

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
     * 设置歌词路径
     *
     * @param mLrcFilePath
     */
    public void setLrcFilePath(String mLrcFilePath) {
        this.mLrcFilePath = mLrcFilePath;
        if (mLrcFilePath != null && !mLrcFilePath.equals("")) {
            loadLrcData();
        }
    }

    public void setExtraLrcType(int mExtraLrcType) {
        this.mExtraLrcType = mExtraLrcType;
        mHandler.sendEmptyMessage(INITTITLE);
    }

    public void setExtraItemEvent(MakeTranslateLrcActivity.ExtraItemEvent mExtraItemEvent) {
        this.mExtraItemEvent = mExtraItemEvent;
    }

    /**
     * @param index
     * @return
     */
    public MakeExtraLrcLineInfo getMakeExtraLrcLineInfo(int index) {
        if (index >= 0 && index < mMakeLrcs.size()) {
            return mMakeLrcs.get(index);
        }
        return null;
    }

    /**
     * 保存和更新
     */
    public void saveAndUpdate() {
        mMakeExtraLrcAdapter.saveAndUpdate();
    }

    /**
     * 获取上一行歌词
     *
     * @return
     */
    public int getPreIndex() {
        return mMakeExtraLrcAdapter.getPreIndex();
    }

    /**
     * 获取下一行歌词
     *
     * @return
     */
    public int getNextIndex() {
        return mMakeExtraLrcAdapter.getNextIndex();
    }


    /**
     * 获取歌词数据大小
     *
     * @return
     */
    public int getLrcDataSize() {
        return mMakeLrcs.size();
    }


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

                        //
                        mLyricsInfo = lyricsReader.getLyricsInfo();

                        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = lyricsReader.getLrcLineInfos();

                        List<LyricsLineInfo> extraLrcLineInfos = null;
                        if (mExtraLrcType == 0) {
                            //音译
                            extraLrcLineInfos = lyricsReader.getTransliterationLrcLineInfos();
                        } else {
                            //翻译
                            extraLrcLineInfos = lyricsReader.getTranslateLrcLineInfos();
                        }

                        //初始化数据
                        for (int i = 0; i < lrcLineInfos.size(); i++) {
                            LyricsLineInfo lyricsLineInfo = lrcLineInfos.get(i);
                            MakeExtraLrcLineInfo makeExtraLrcLineInfo = new MakeExtraLrcLineInfo();
                            makeExtraLrcLineInfo.setLyricsLineInfo(lyricsLineInfo);
                            if (extraLrcLineInfos != null && i < extraLrcLineInfos.size()) {
                                if (mExtraLrcType == 0) {
                                    //音译
                                    String[] extraLyricsWords = extraLrcLineInfos.get(i).getLyricsWords();
                                    String extraLineLyrics = "";
                                    for (int j = 0; j < extraLyricsWords.length; j++) {
                                        if (j == 0) {
                                            extraLineLyrics += extraLyricsWords[j].trim();
                                        } else {
                                            extraLineLyrics += "∮" + extraLyricsWords[j].trim();
                                        }
                                    }
                                    makeExtraLrcLineInfo.setExtraLineLyrics(extraLineLyrics);
                                } else {
                                    //翻译
                                    String extraLineLyrics = extraLrcLineInfos.get(i).getLineLyrics();
                                    if (StringUtils.isNotBlank(extraLineLyrics)) {
                                        makeExtraLrcLineInfo.setExtraLineLyrics(extraLineLyrics);
                                    }
                                }
                            }

                            mMakeLrcs.add(makeExtraLrcLineInfo);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                return null;
            }

            @Override
            protected void onPostExecute(String s) {

                mMakeExtraLrcAdapter.notifyDataSetChanged();

                super.onPostExecute(s);
            }
        }.execute("");
    }

    public LyricsInfo getLyricsInfo() {
        return mLyricsInfo;
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

}
