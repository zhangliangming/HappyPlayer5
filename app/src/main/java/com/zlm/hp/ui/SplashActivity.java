package com.zlm.hp.ui;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;

import com.zlm.hp.constants.PreferencesConstants;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.libs.crash.CrashHandler;
import com.zlm.hp.libs.utils.ColorUtil;
import com.zlm.hp.libs.utils.PreferencesUtil;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.permissions.StoragePermissionUtil;
import com.zlm.hp.utils.MediaUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        boolean isFrist = (boolean) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.isFrist_KEY, true);
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
                    boolean flag = false;
                    for (int i = 0; i < audioInfos.size(); i++) {
                        AudioInfo audioInfo = audioInfos.get(i);
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
            if (audioInfos.size() > 0) {
                AudioInfoDB.getAudioInfoDB(getApplicationContext()).add(audioInfos);
            }
            mHPApplication.setFrist(false);
        } else {
            //设置延迟时间
            mDelayTime *= 2;
        }

        //注册捕捉全局异常
        CrashHandler crashHandler = new CrashHandler();
        crashHandler.init(mHPApplication);
        //初始化配置数据
        initPreferencesData();
        loadSplashMusic();
        mAnimationHandler.postDelayed(mAnimationRunnable, mDelayTime);
    }

    /**
     * 加载启动页面的问候语
     */
    protected void loadSplashMusic() {
        boolean isSayHello = (boolean) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.isSayHello_KEY, mHPApplication.isSayHello());
        mHPApplication.setSayHello(isSayHello);
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
        mHPApplication.setPlayStatus(AudioPlayerManager.STOP);
        //桌面歌词
        mHPApplication.setDesktopLyricsIsMove((boolean) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.desktopLyricsIsMove_KEY, true));
        mHPApplication.setShowDesktop((boolean) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.isShowDesktop_KEY, false));
        //锁屏标志
        mHPApplication.setShowLockScreen((boolean) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.isShowLockScreen_KEY, false));
        //线控标志
        mHPApplication.setWire((boolean) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.isWire_KEY, false));
        //wifi标志
        mHPApplication.setWifi((boolean) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.isWifi_KEY, true));
        mHPApplication.setBarMenuShow((boolean) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.isBarMenuShow_KEY, false));
        mHPApplication.setPlayIndexHashID((String) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.playIndexHashID_KEY, ""));
        mHPApplication.setPlayModel((int) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.playModel_KEY, 0));
        mHPApplication.setLrcColorIndex((int) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.lrcColorIndex_KEY, 0));
        mHPApplication.setLrcFontSize((int) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.lrcFontSize_KEY, 30));
        mHPApplication.setManyLineLrc((boolean) PreferencesUtil.getValue(getApplicationContext(), PreferencesConstants.isManyLineLrc_KEY, true));
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {

        if (!isRestoreInstance) {
            //
            doSomeThing();
        }

    }

    //用户处理权限反馈，在这里判断用户是否授予相应的权限
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mStoragePermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults, new StoragePermissionUtil.RequestPermissionsResult() {
            @Override
            public void acceptedCallback() {
                doSomeThing();
            }
        });
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
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);

        finish();
    }

}
