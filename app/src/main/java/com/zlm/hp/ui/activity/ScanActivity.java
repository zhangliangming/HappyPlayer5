package com.zlm.hp.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.R;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.utils.MediaUtil;

import java.util.ArrayList;
import java.util.List;

import base.utils.ThreadUtil;
import base.utils.ToastUtil;
import base.widget.ButtonRelativeLayout;
import base.widget.IconfontImageButtonTextView;

/**
 * @Description: 扫描窗口
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/8/3 20:42
 * @Throws:
 */
public class ScanActivity extends BaseActivity {

    /**
     * 扫描界面
     */
    private RelativeLayout mScanRelativeLayout;
    /**
     * 开始扫描按钮
     */
    private ButtonRelativeLayout mStartButton;

    ///////////////////////////////////////////////////////
    /**
     * 扫描中界面
     */
    private RelativeLayout mScaningRelativeLayout;
    /**
     * 停止扫描按钮
     */
    private ButtonRelativeLayout mStopButton;
    /**
     * 显示扫描结果
     */
    private TextView mResultTextView;
    /**
     * 扫描中图片
     */
    private ImageView mScaningImageView;

    /**
     * 旋转动画
     */
    private Animation mRotateAnimation;

    ///////////////////////////////////////////////////////
    /**
     * 扫描完成界面
     */
    private RelativeLayout mScanFinishRelativeLayout;
    /**
     * 显示扫描完成结果
     */
    private TextView mFinishResultTextView;
    /**
     * 完成
     */
    private ButtonRelativeLayout mFinishButtonRelativeLayout;

    private final int SCAN = 0;
    private final int SCANING = 1;
    private final int FINISH = 2;
    /**
     * 添加结果
     */
    private boolean mAddResult = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN:
                    showScaningView();
                    break;
                case SCANING:
                    updateScaningView();
                    break;
                case FINISH:
                    showFinishView();
                    break;

            }
        }
    };
    //////////////////////////////////////////////////
    private List<AudioInfo> mAudioInfoLists;
    private Runnable runnable;

    @Override
    protected int setContentViewId() {
        return R.layout.activity_scan;
    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }

    @Override
    public int setStatusBarParentView() {
        return 0;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        IconfontImageButtonTextView closeImg = findViewById(R.id.close_img);
        closeImg.setConvert(true);
        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        //扫描界面
        mScanRelativeLayout = findViewById(R.id.scan);
        mStartButton = findViewById(R.id.start_scan);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(SCAN);
            }
        });

        //扫描中界面
        mScaningRelativeLayout = findViewById(R.id.scaning);
        mStopButton = findViewById(R.id.stop_scan);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(FINISH);
            }
        });
        mResultTextView = findViewById(R.id.scaning_text);
        mScaningImageView = findViewById(R.id.scaning_img);
        mRotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.anim_rotate);
        mRotateAnimation.setInterpolator(new LinearInterpolator());// 匀速

        //扫描完成界面
        mScanFinishRelativeLayout = findViewById(R.id.scan_finish);
        mFinishResultTextView = findViewById(R.id.scaning_finish_text);
        mFinishButtonRelativeLayout = findViewById(R.id.finish_scan);
        mFinishButtonRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAudioInfoLists.size() > 0) {
                    doFinish();
                } else {
                    finish();
                    overridePendingTransition(0, 0);
                }

            }
        });

        showScanView();
    }

    /**
     * 显示扫描界面
     */
    private void showScanView() {
        mScanRelativeLayout.setVisibility(View.VISIBLE);
        mScaningRelativeLayout.setVisibility(View.INVISIBLE);
        mScaningImageView.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示扫描中界面
     */
    private void showScaningView() {
        mScaningImageView.clearAnimation();
        mScaningImageView.startAnimation(mRotateAnimation);
        mScanRelativeLayout.setVisibility(View.INVISIBLE);
        mScaningRelativeLayout.setVisibility(View.VISIBLE);
        mScaningImageView.setVisibility(View.VISIBLE);
        mScanFinishRelativeLayout.setVisibility(View.INVISIBLE);

        //扫描
        doScaningHandler();
    }

    /**
     * 扫描
     */
    private void doScaningHandler() {
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                MediaUtil.scanMusic(mContext, new MediaUtil.ForeachListener() {
                    @Override
                    public void before() {
                        AudioInfoDB.getAudioInfoDB(mContext).delete(AudioInfo.LOCAL);
                    }

                    @Override
                    public void foreach(List<AudioInfo> audioInfoList) {
                        if (audioInfoList != null) {
                            mAudioInfoLists.clear();
                            mAudioInfoLists.addAll(audioInfoList);
                        }

                        mHandler.sendEmptyMessage(SCANING);
                    }

                    @Override
                    public boolean filter(String hash) {
                        return AudioInfoDB.getAudioInfoDB(mContext).isExists(hash);
                    }
                });
                if (mAudioInfoLists.size() > 0) {
                    AudioInfoDB.getAudioInfoDB(mContext).add(mAudioInfoLists);
                }

                mHandler.sendEmptyMessage(FINISH);
            }
        });
    }

    /**
     * 显示完成界面
     */
    private void showFinishView() {
        mScanFinishRelativeLayout.setVisibility(View.VISIBLE);
        mScanRelativeLayout.setVisibility(View.INVISIBLE);
        mScaningRelativeLayout.setVisibility(View.INVISIBLE);
        mScaningImageView.setVisibility(View.INVISIBLE);
        mScaningImageView.clearAnimation();
        //
        mFinishResultTextView.setText(String.format(mContext.getString(R.string.update_local_songs, mAudioInfoLists.size())));
    }

    /**
     * 更新扫描中视图
     */
    private void updateScaningView() {
        mResultTextView.setText(String.format(mContext.getString(R.string.scan_local_songs, mAudioInfoLists.size())));
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        mAudioInfoLists = new ArrayList<AudioInfo>();
    }

    /**
     * 扫描完成
     */
    @SuppressLint("StaticFieldLeak")
    private void doFinish() {
        runnable = new Runnable() {
            @Override
            public void run() {
                mAddResult = AudioInfoDB.getAudioInfoDB(getApplicationContext()).add(mAudioInfoLists);

                if (mAddResult) {
                    //发送更新广播
                    Intent updateIntent = new Intent(AudioBroadcastReceiver.ACTION_LOCALUPDATE);
                    updateIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(updateIntent);
                    finish();
                    overridePendingTransition(0, 0);
                    ToastUtil.showTextToast(getApplicationContext(), mContext.getString(R.string.add_success));
                } else {
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            ToastUtil.showTextToast(getApplicationContext(), mContext.getString(R.string.add_fail));
                        }  });//切换至主线程更新ui

                }
            }
        };
        ThreadUtil.runInThread(runnable);
    }

    @Override
    public void onDestroy() {
        if(runnable != null) {
            ThreadUtil.cancelThread(runnable);
        }
        super.onDestroy();
    }
}