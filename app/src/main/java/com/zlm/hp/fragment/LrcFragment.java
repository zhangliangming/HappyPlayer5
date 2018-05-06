package com.zlm.hp.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;


import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.libs.utils.ColorUtil;
import com.zlm.hp.libs.utils.ToastUtil;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.widget.AbstractLrcView;
import com.zlm.hp.lyrics.widget.ManyLyricsView;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.net.entity.DownloadLyricsResult;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.utils.ImageUtil;
import com.zlm.hp.utils.ResourceFileUtil;
import com.zlm.hp.widget.ButtonRelativeLayout;

import java.io.File;

/**
 * 歌词视图
 */
public class LrcFragment extends BaseFragment {

    private DownloadLyricsResult mDownloadLyricsResult;
    private AudioInfo mCurAudioInfo;
    //多行歌词
    private ManyLyricsView mManyLineLyricsView;

    //、、、、、、、、、、、、、、、、、、、、、、、、、翻译和音译歌词、、、、、、、、、、、、、、、、、、、、、、、、、、、
    //翻译歌词
    private ImageView hideTranslateImg;
    private ImageView showTranslateImg;
    //音译歌词
    private ImageView hideTransliterationImg;
    private ImageView showTransliterationImg;

    //翻译歌词/音译歌词
    private ImageView showTTToTranslateImg;
    private ImageView showTTToTransliterationImg;
    private ImageView hideTTImg;

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
                    hideTranslateImg.setVisibility(View.INVISIBLE);
                    showTranslateImg.setVisibility(View.INVISIBLE);
                    //音译歌词
                    hideTransliterationImg.setVisibility(View.INVISIBLE);
                    showTransliterationImg.setVisibility(View.INVISIBLE);

                    //翻译歌词/音译歌词
                    showTTToTranslateImg.setVisibility(View.INVISIBLE);
                    showTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    hideTTImg.setVisibility(View.INVISIBLE);


                    break;
                case HASTRANSLATEANDTRANSLITERATIONLRC:


                    //翻译歌词
                    hideTranslateImg.setVisibility(View.INVISIBLE);
                    showTranslateImg.setVisibility(View.INVISIBLE);
                    //音译歌词
                    hideTransliterationImg.setVisibility(View.INVISIBLE);
                    showTransliterationImg.setVisibility(View.INVISIBLE);

                    //翻译歌词/音译歌词
                    showTTToTranslateImg.setVisibility(View.INVISIBLE);
                    showTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    hideTTImg.setVisibility(View.VISIBLE);

                    break;
                case HASTRANSLITERATIONLRC:

                    //翻译歌词
                    hideTranslateImg.setVisibility(View.INVISIBLE);
                    showTranslateImg.setVisibility(View.INVISIBLE);
                    //音译歌词
                    hideTransliterationImg.setVisibility(View.INVISIBLE);
                    showTransliterationImg.setVisibility(View.VISIBLE);

                    //翻译歌词/音译歌词
                    showTTToTranslateImg.setVisibility(View.INVISIBLE);
                    showTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    hideTTImg.setVisibility(View.INVISIBLE);

                    break;
                case HASTRANSLATELRC:

                    //翻译歌词
                    hideTranslateImg.setVisibility(View.INVISIBLE);
                    showTranslateImg.setVisibility(View.VISIBLE);
                    //音译歌词
                    hideTransliterationImg.setVisibility(View.INVISIBLE);
                    showTransliterationImg.setVisibility(View.INVISIBLE);

                    //翻译歌词/音译歌词
                    showTTToTranslateImg.setVisibility(View.INVISIBLE);
                    showTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    hideTTImg.setVisibility(View.INVISIBLE);


                    break;

            }

        }
    };


    private static final int LOADDATA = 0;
    /**
     * 屏幕宽度
     */
    private int mScreensWidth;
    /**
     * 使用歌词按钮
     */
    private ButtonRelativeLayout mUseBtn;
    private String mHash;
    private String mLrcFilePath;
    /**
     *
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOADDATA:
                    loadDataUtil();
                    break;
            }
        }
    };

    public LrcFragment() {

    }

    @SuppressLint("ValidFragment")
    public LrcFragment(DownloadLyricsResult downloadLyricsResult, AudioInfo audioInfo) {
        this.mCurAudioInfo = audioInfo;
        this.mDownloadLyricsResult = downloadLyricsResult;

    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        mHandler.sendEmptyMessage(LOADDATA);

    }

    /**
     *
     */
    private void loadDataUtil() {

        if (mDownloadLyricsResult != null && mDownloadLyricsResult.getContent() != null) {
            if (mCurAudioInfo != null) {
                mHash = mCurAudioInfo.getHash();
                //歌词文件名
                String fileName = "";
                if (mCurAudioInfo.getSingerName().equals("未知")) {
                    fileName = mCurAudioInfo.getSongName();
                } else {
                    fileName = mCurAudioInfo.getSingerName() + " - " + mCurAudioInfo.getSongName();
                }
                mLrcFilePath = ResourceFileUtil.getFilePath(mActivity.getApplicationContext(), ResourceConstants.PATH_LYRICS, fileName + ".krc");

                //
                LyricsReader lyricsReader = new LyricsReader();
                lyricsReader.setHash(mHash);
                lyricsReader.loadLrc(mDownloadLyricsResult.getContent(), null, mLrcFilePath);
                mManyLineLyricsView.setTextMaxWidth(mScreensWidth / 3 * 2);
                mManyLineLyricsView.setLyricsReader(lyricsReader);
                if (mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYING && mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLineLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY)

                    mManyLineLyricsView.play((int) mHPApplication.getCurAudioMessage().getPlayProgress());

            }
        }
    }


    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_lrc;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {

        WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mScreensWidth = display.getWidth();

        showContentView();

        //歌词视图
        mManyLineLyricsView = mainView.findViewById(R.id.lrcview);
        mManyLineLyricsView.setSize(30, 30, false);
        mManyLineLyricsView.setPaintColor(new int[]{ColorUtil.parserColor("#888888"), ColorUtil.parserColor("#888888")}, false);
        mManyLineLyricsView.setPaintHLColor(new int[]{ColorUtil.parserColor("#0288d1"), ColorUtil.parserColor("#0288d1")}, false);
        mManyLineLyricsView.setTouchAble(false);


        //翻译歌词
        hideTranslateImg = mainView.findViewById(R.id.hideTranslateImg);
        ImageUtil.getTranslateColorImg(mActivity.getApplicationContext(), hideTranslateImg, R.mipmap.bql, ColorUtil.parserColor("#0288d1"));

        hideTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideTranslateImg.setVisibility(View.INVISIBLE);
                showTranslateImg.setVisibility(View.VISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                }

            }
        });
        showTranslateImg = mainView.findViewById(R.id.showTranslateImg);
        ImageUtil.getTranslateColorImg(mActivity.getApplicationContext(), showTranslateImg, R.mipmap.bqm, ColorUtil.parserColor("#0288d1"));


        showTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideTranslateImg.setVisibility(View.VISIBLE);
                showTranslateImg.setVisibility(View.INVISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);
                }

            }
        });
        //音译歌词
        hideTransliterationImg = mainView.findViewById(R.id.hideTransliterationImg);
        ImageUtil.getTranslateColorImg(mActivity.getApplicationContext(), hideTransliterationImg, R.mipmap.bqn, ColorUtil.parserColor("#0288d1"));


        hideTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideTransliterationImg.setVisibility(View.INVISIBLE);
                showTransliterationImg.setVisibility(View.VISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                }


            }
        });
        showTransliterationImg = mainView.findViewById(R.id.showTransliterationImg);
        ImageUtil.getTranslateColorImg(mActivity.getApplicationContext(), showTransliterationImg, R.mipmap.bqo, ColorUtil.parserColor("#0288d1"));


        showTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideTransliterationImg.setVisibility(View.VISIBLE);
                showTransliterationImg.setVisibility(View.INVISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                }

            }
        });

        //翻译歌词/音译歌词
        showTTToTranslateImg = mainView.findViewById(R.id.showTTToTranslateImg);
        ImageUtil.getTranslateColorImg(mActivity.getApplicationContext(), showTTToTranslateImg, R.mipmap.bqi, ColorUtil.parserColor("#0288d1"));


        showTTToTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTTToTranslateImg.setVisibility(View.INVISIBLE);
                showTTToTransliterationImg.setVisibility(View.VISIBLE);
                hideTTImg.setVisibility(View.INVISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);
                }

            }
        });
        showTTToTransliterationImg = mainView.findViewById(R.id.showTTToTransliterationImg);
        ImageUtil.getTranslateColorImg(mActivity.getApplicationContext(), showTTToTransliterationImg, R.mipmap.bqj, ColorUtil.parserColor("#0288d1"));


        showTTToTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTTToTranslateImg.setVisibility(View.INVISIBLE);
                showTTToTransliterationImg.setVisibility(View.INVISIBLE);
                hideTTImg.setVisibility(View.VISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                }

            }
        });
        hideTTImg = mainView.findViewById(R.id.hideTTImg);
        ImageUtil.getTranslateColorImg(mActivity.getApplicationContext(), hideTTImg, R.mipmap.bqk, ColorUtil.parserColor("#0288d1"));

        hideTTImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTTToTranslateImg.setVisibility(View.VISIBLE);
                showTTToTransliterationImg.setVisibility(View.INVISIBLE);
                hideTTImg.setVisibility(View.INVISIBLE);


                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                }

            }
        });

        //
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

        //使用歌词按钮
        mUseBtn = mainView.findViewById(R.id.uselrcbtn);
        mUseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mManyLineLyricsView != null && mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.getLyricsReader().setLrcFilePath(mLrcFilePath);
                    LyricsManager.getLyricsManager(mHPApplication, mActivity.getApplicationContext()).setUseLrcUtil(mHash, mManyLineLyricsView.getLyricsReader());

                    //发送使用歌词广播
                    Intent searchingIntent = new Intent(AudioBroadcastReceiver.ACTION_LRCUSE);
                    searchingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mActivity.sendBroadcast(searchingIntent);

                    ToastUtil.showTextToast(mActivity.getApplicationContext(), "歌词设置成功");
                }
            }
        });

    }

    @Override
    protected int setTitleViewId() {
        return 0;
    }

    @Override
    protected boolean isAddStatusBar() {
        return false;
    }
}
