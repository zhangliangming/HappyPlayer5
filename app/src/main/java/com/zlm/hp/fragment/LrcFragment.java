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

import com.happy.lyrics.utils.LyricsUtil;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.libs.utils.ColorUtil;
import com.zlm.hp.libs.utils.ToastUtil;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.net.entity.DownloadLyricsResult;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.utils.ResourceFileUtil;
import com.zlm.hp.widget.ButtonRelativeLayout;
import com.zlm.hp.widget.lrc.ManyLineLyricsView;

import java.io.File;

/**
 * 歌词视图
 */
public class LrcFragment extends BaseFragment {

    private DownloadLyricsResult mDownloadLyricsResult;
    private ManyLineLyricsView mManyLineLyricsView;
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
    public LrcFragment(DownloadLyricsResult downloadLyricsResult) {
        this.mDownloadLyricsResult = downloadLyricsResult;
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        mHandler.sendEmptyMessageDelayed(LOADDATA, 500);

    }

    /**
     *
     */
    private void loadDataUtil() {

        if (mDownloadLyricsResult != null && mDownloadLyricsResult.getContent() != null) {
            if (mHPApplication.getCurAudioInfo() != null) {
                mHash = mHPApplication.getCurAudioInfo().getHash();
                //歌词文件名
                String fileName = "";
                if (mHPApplication.getCurAudioInfo().getSingerName().equals("未知")) {
                    fileName = mHPApplication.getCurAudioInfo().getSongName();
                } else {
                    fileName = mHPApplication.getCurAudioInfo().getSingerName() + " - " + mHPApplication.getCurAudioInfo().getSongName();
                }
                mLrcFilePath = ResourceFileUtil.getFilePath(mActivity.getApplicationContext(), ResourceConstants.PATH_LYRICS) + File.separator + fileName + ".krc";

                //
                LyricsUtil lyricsUtil = new LyricsUtil();
                lyricsUtil.setHash(mHash);
                lyricsUtil.loadLrc(mDownloadLyricsResult.getContent(), null, mLrcFilePath);
                mManyLineLyricsView.setLyricsUtil(lyricsUtil, mScreensWidth / 4 * 3);
                if (mHPApplication.getCurAudioMessage() != null) {
                    mManyLineLyricsView.updateView((int) mHPApplication.getCurAudioMessage().getPlayProgress());
                }
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
        mManyLineLyricsView.setDefLrcColor(ColorUtil.parserColor("#888888"));
        mManyLineLyricsView.setLrcColor(ColorUtil.parserColor("#0288d1"));
        mManyLineLyricsView.setTouchInterceptTrue();

        //使用歌词按钮
        mUseBtn = mainView.findViewById(R.id.uselrcbtn);
        mUseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mManyLineLyricsView != null && mManyLineLyricsView.getLyricsUtil() != null) {
                    mManyLineLyricsView.getLyricsUtil().setLrcFilePath(mLrcFilePath);
                    LyricsManager.getLyricsManager(mHPApplication, mActivity.getApplicationContext()).setUseLrcUtil(mHash, mManyLineLyricsView.getLyricsUtil());

                    //发送使用歌词广播
                    Intent searchingIntent = new Intent(AudioBroadcastReceiver.ACTION_LRCUSE);
                    searchingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mActivity.sendBroadcast(searchingIntent);

                    ToastUtil.showTextToast(mActivity.getApplicationContext(), "歌词设置成功");
                }
            }
        });

    }

    /**
     * 更新视图
     *
     * @param playProgress
     */
    public void updateView(int playProgress, String hash) {

        //更新歌词
        if (mManyLineLyricsView != null && mManyLineLyricsView.getLyricsUtil() != null && mManyLineLyricsView.getLyricsUtil().getHash().equals(hash)) {
            mManyLineLyricsView.updateView(playProgress);
        }
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
