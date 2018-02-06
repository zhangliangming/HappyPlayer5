package com.zlm.hp.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.zlm.hp.R;
import com.zlm.hp.application.HPApplication;
import com.zlm.hp.constants.PreferencesConstants;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.utils.IntentUtils;
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
    private int mDelayTime = 500;

    @Override
    protected int setContentViewId() {
        //设置状态栏颜色
        setStatusColor(ColorUtil.parserColor(Color.BLACK, 30));
        return R.layout.activity_splash;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AndPermission.with(mActivity)
                    .requestCode(110)
                    .permission(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .callback(new PermissionListener() {
                        @Override
                        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                            scanLocalMusic();
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            IntentUtils.gotoPermissionSetting(mActivity);
                        }
                    }).start();
        }else {
            scanLocalMusic();
        }
    }

    private void scanLocalMusic() {
        //是否是第一次使用
        boolean isFrist = PreferencesUtil.getBooleanValue(getApplicationContext(), PreferencesConstants.isFrist_KEY, true);
        if (isFrist) {
            //第一次使用扫描本地歌曲
            final List<AudioInfo> audioInfos = new ArrayList<AudioInfo>();
            MediaUtil.scanMusic(mContext, new MediaUtil.ForeachListener() {
                @Override
                public void before() {
                    AudioInfoDB.getAudioInfoDB(mContext).delete(AudioInfo.LOCAL);
                }

                @Override
                public void foreach(List<AudioInfo> audioInfoList) {
                    if (audioInfoList != null) {
                        audioInfos.clear();
                        audioInfos.addAll(audioInfoList);
                    }
                }

                @Override
                public boolean filter(String hash) {
                    return AudioInfoDB.getAudioInfoDB(mContext).isExists(hash);
                }
            });
            if (audioInfos.size() > 0) {
                AudioInfoDB.getAudioInfoDB(mContext).add(audioInfos);
            }

            //设置延迟时间
            mDelayTime *= 2;
            HPApplication.getInstance().setFrist(false);
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
        boolean isSayHello = (boolean) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.isSayHello_KEY, HPApplication.getInstance().isSayHello());
        HPApplication.getInstance().setSayHello(isSayHello);
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
        //歌曲播放状态
        HPApplication.getInstance().setPlayStatus(HPApplication.getInstance().getPlayStatus());
        //桌面歌词
        HPApplication.getInstance().setDesktop(HPApplication.getInstance().isDesktop());
        //锁屏标志
        HPApplication.getInstance().setShowLockScreen(HPApplication.getInstance().isLockScreen());
        //线控标志
        HPApplication.getInstance().setWire(HPApplication.getInstance().isWire());
        //wifi标志
        HPApplication.getInstance().setWifi(HPApplication.getInstance().isWifi());
        //底部按钮是否打开
        HPApplication.getInstance().setBarMenuShow(HPApplication.getInstance().isBarMenuShow());
        //播放歌曲id
        HPApplication.getInstance().setPlayIndexHashID(HPApplication.getInstance().getPlayIndexHashID());
        //歌曲播放模式
        HPApplication.getInstance().setPlayModel(HPApplication.getInstance().getPlayModel());
        //歌词颜色索引
        HPApplication.getInstance().setLrcColorIndex(HPApplication.getInstance().getLrcColorIndex());
        //歌词字体大小
        HPApplication.getInstance().setLrcFontSize(HPApplication.getInstance().getLrcFontSize());
        //是否是多行歌词
        HPApplication.getInstance().setManyLineLrc(HPApplication.getInstance().isManyLineLrc());
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {

        if (!isRestoreInstance) {
            //
            doSomeThing();
        }

    }

    /**
     * 跳转到主页面
     */
    private void goHome() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);

        finish();
    }

}
