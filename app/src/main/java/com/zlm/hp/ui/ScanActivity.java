package com.zlm.hp.ui;

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

import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.libs.utils.ToastUtil;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.utils.AsyncTaskUtil;
import com.zlm.hp.utils.MediaUtil;
import com.zlm.hp.widget.ButtonRelativeLayout;
import com.zlm.hp.widget.IconfontImageButtonTextView;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected int setContentViewId() {
        return R.layout.activity_scan;
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
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MediaUtil.scanLocalMusic(ScanActivity.this, new MediaUtil.ForeachListener() {
                    @Override
                    public void foreach(AudioInfo audioInfo) {

                        if (audioInfo != null) {
                            mAudioInfoLists.add(audioInfo);
                        }

                        mHandler.sendEmptyMessage(SCANING);
                    }

                    @Override
                    public boolean filter(String hash) {
                        boolean flag = false;
                        for (int i = 0; i < mAudioInfoLists.size(); i++) {
                            AudioInfo audioInfo = mAudioInfoLists.get(i);
                            if (audioInfo.getHash().equals(hash)) {
                                flag = true;
                                break;
                            }
                        }
                        if (flag) {
                            return true;
                        }
                        return AudioInfoDB.getAudioInfoDB(getApplicationContext()).isExists(hash);
                    }
                });
                mHandler.sendEmptyMessage(FINISH);
            }
        }.start();


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
        mFinishResultTextView.setText("更新本地歌曲(" + mAudioInfoLists.size() + ")");
    }

    /**
     * 更新扫描中视图
     */
    private void updateScaningView() {
        mResultTextView.setText("已扫描" + mAudioInfoLists.size() + "首歌曲");
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

        new AsyncTaskUtil() {

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mAddResult) {

                    //发送更新广播
                    Intent updateIntent = new Intent(AudioBroadcastReceiver.ACTION_LOCALUPDATE);
                    updateIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(updateIntent);


                    finish();
                    overridePendingTransition(0, 0);
                    ToastUtil.showTextToast(getApplicationContext(), "添加成功");
                } else {
                    ToastUtil.showTextToast(getApplicationContext(), "添加失败");
                }
            }

            @Override
            protected Void doInBackground(String... strings) {
                mAddResult = AudioInfoDB.getAudioInfoDB(getApplicationContext()).add(mAudioInfoLists);
                return super.doInBackground(strings);
            }
        }.execute("");

    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }

    @Override
    public int setStatusBarParentView() {
        return 0;
    }
}