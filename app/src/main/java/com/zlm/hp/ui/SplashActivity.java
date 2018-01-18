package com.zlm.hp.ui;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;

import com.zlm.hp.R;
import com.zlm.hp.constants.PreferencesConstants;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.utils.MediaUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import base.utils.ColorUtil;
import base.utils.PreferencesUtil;

/**
 * @Description: 启动页面
 * @Author: zhangliangming
 * @Date: 2017/7/15 20:04
 * @Version:
 */
public class SplashActivity extends BaseActivity {

    private Handler mAnimationHandler;
    private Runnable mAnimationRunnable;
    private int mDelayTime = 0;


    @Override
    protected void initViews(Bundle savedInstanceState) {
        //
        mAnimationHandler = new Handler();
        mAnimationRunnable = new Runnable() {
            @Override
            public void run() {
                goHome();
            }
        };

    }

    /**
     * 处理后台数据
     */
    private void doSomeThing() {
        //是否是第一次使用
        boolean isFrist = PreferencesUtil.getBooleanValue(getApplicationContext(), PreferencesConstants.isFrist_KEY, true);
        if (isFrist) {
            //第一次使用扫描本地歌曲
            final List<AudioInfo> audioInfos = new ArrayList<AudioInfo>();
            MediaUtil.scanLocalMusic(SplashActivity.this, new MediaUtil.ForeachListener() {
                @Override
                public void foreach(AudioInfo audioInfo) {
                    if (audioInfo != null) {
                        audioInfos.add(audioInfo);
                    }
                }

                @Override
                public boolean filter(String hash) {
                    return AudioInfoDB.getAudioInfoDB(getApplicationContext()).isExists(hash);
                }
            });
            if (audioInfos.size() > 0) {
                AudioInfoDB.getAudioInfoDB(getApplicationContext()).add(audioInfos);
            }

            //设置延迟时间
            mDelayTime *= 2;
            PreferencesConstants.setFrist(mContext, false);
        } else {
            //设置延迟时间
            mDelayTime *= 3;
        }
        //初始化配置数据
        initPreferencesData();
        loadSplashMusic();
        mAnimationHandler.postDelayed(mAnimationRunnable, mDelayTime);
    }

    /**
     * 加载启动页面的问候语
     */
    protected void loadSplashMusic() {
        boolean isSayHello = PreferencesUtil.getBooleanValue(mContext,
                PreferencesConstants.isSayHello_KEY,
                PreferencesConstants.isSayHello(mContext));
        PreferencesConstants.setSayHello(mContext, isSayHello);
        if (isSayHello) {
            AssetManager assetManager = getAssets();
            AssetFileDescriptor fileDescriptor;
            try {
                fileDescriptor = assetManager.openFd("audio/hellolele.mp3");
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                        fileDescriptor.getStartOffset(),
                        fileDescriptor.getLength());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化配置数据
     */
    private void initPreferencesData() {
        PreferencesConstants.setPlayStatus(mContext, AudioPlayerManager.STOP);
        //初始化wifi值
        PreferencesConstants.setWire(mContext, PreferencesConstants.isWire(mContext));
        PreferencesConstants.setWifi(mContext, PreferencesConstants.isWifi(mContext));
        PreferencesConstants.setBarMenuShow(mContext, PreferencesConstants.isBarMenuShow(mContext));
        PreferencesConstants.setPlayIndexHashID(mContext, PreferencesConstants.getPlayIndexHashID(mContext));
        PreferencesConstants.setPlayModel(mContext, PreferencesConstants.getPlayModel(mContext));
        PreferencesConstants.setLrcColorIndex(mContext, PreferencesConstants.getLrcColorIndex(mContext));
        PreferencesConstants.setLrcFontSize(mContext, PreferencesConstants.getLrcFontSize(mContext));
        PreferencesConstants.setManyLineLrc(mContext, PreferencesConstants.isManyLineLrc(mContext));
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        if (!isRestoreInstance) {
            //
            doSomeThing();
        }

    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }

    @Override
    protected int setContentViewId() {
        //设置状态栏颜色
        setStatusColor(ColorUtil.parserColor(Color.BLACK, 30));
        return R.layout.activity_splash;
    }

    @Override
    public int setStatusBarParentView() {
        return 0;
    }


    /**
     * 跳转到主页面
     */
    private void goHome() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
//        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);

        finish();
    }

}
