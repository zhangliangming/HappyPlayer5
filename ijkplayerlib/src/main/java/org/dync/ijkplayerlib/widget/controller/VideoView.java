package org.dync.ijkplayerlib.widget.controller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.dync.ijkplayerlib.R;
import org.dync.ijkplayerlib.widget.controller.impl.AnimationImpl;
import org.dync.ijkplayerlib.widget.controller.impl.IPlayerBottomImpl;
import org.dync.ijkplayerlib.widget.controller.impl.IPlayerImpl;
import org.dync.ijkplayerlib.widget.controller.impl.IPlayerTitleBarImpl;
import org.dync.ijkplayerlib.widget.controller.impl.IPlayerVolumeBrightImpl;
import org.dync.ijkplayerlib.widget.controller.util.NetworkUtil;
import org.dync.ijkplayerlib.widget.media.IRenderView;
import org.dync.ijkplayerlib.widget.media.IjkVideoView;
import org.dync.ijkplayerlib.widget.util.PlayerController;

import java.util.ArrayList;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by KathLine on 2017/12/13.
 */

public class VideoView extends RelativeLayout {

    private static final String TAG = "VideoView";
    private Context mContext;
    private RelativeLayout mRlVideoParent;
    private IjkVideoView mVideoView;
    private PlayerTitleBar mPlayerTitleBar;
    private PlayerVolumeBright mPlayerVolumeBright;
    private PlayerBottom mPlayerBottom;
    private ImageView mIvCover;
    private LinearLayout mLlLoading;
    private ProgressBar mProgressBar;
    private TextView mTvSpeed;

    private Animation mEnterFromTop;
    private Animation mEnterFromBottom;
    private Animation mExitFromTop;
    private Animation mExitFromBottom;

    private PlayerController mPlayerController;
    private Activity mActivity;
    private BroadcastReceiver mNetworkReceiver;//网络监听广播
    private boolean mNetworkAvailable;//网络是否可用
    private boolean mIsOnLocalSource;//是否是本地视频
    private Uri mVideoUri;//mVideoUri.getPath();得到String地址
    private String mVideoCoverUrl;

    private Handler mHandler = new Handler();

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(Activity activity, String path) {
        setVideoURI(activity, Uri.parse(path));
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void setVideoURI(Activity activity, Uri uri) {
        mActivity = activity;
        mVideoUri = uri;
        String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme)) {
            Log.e(TAG, "Null unknown scheme\n");
            return;
        }
        if (scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
            mIsOnLocalSource = true;
        } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            Log.e(TAG, "Can not resolve content below Android-ICS\n");
        }
        mVideoView.setVideoURI(uri);
        startOrRestartPlay();

        initPlayer();
        initNetworkMonitor();
        registerNetworkReceiver();
    }

    /**
     * 播放器控制功能对外开放接口,包括返回按钮,播放等...
     */
    public void setPlayerController(IPlayerImpl IPlayerImpl) {
        mIPlayerImpl = IPlayerImpl;
    }

    private IPlayerImpl mIPlayerImpl = null;

    public VideoView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        inflate(context, R.layout.video_view, this);

        mRlVideoParent = findViewById(R.id.rl_video_parent);
        mVideoView = findViewById(R.id.ijk_video_view);
        mPlayerTitleBar = findViewById(R.id.player_title_bar);
        mPlayerBottom = findViewById(R.id.player_bottom);
        mPlayerVolumeBright = findViewById(R.id.player_volume_bright);
        mIvCover = findViewById(R.id.iv_cover);
        mLlLoading = findViewById(R.id.ll_loading);
        mProgressBar = findViewById(R.id.progressBar);
        mTvSpeed = findViewById(R.id.tv_speed);

        mPlayerBottom.toggleExpandable(false);
        initAnimation();
        initLisenter();
        initVideoListener();
    }

    private void initLisenter() {
        mPlayerTitleBar.setTitleBarImpl(new IPlayerTitleBarImpl() {
            @Override
            public void onBackClick() {
                if (mIPlayerImpl != null) {
                    mIPlayerImpl.onBack();
                } else {
                    if (mActivity != null) {
                        mActivity.finish();
                    }
                }
            }
        });
        mPlayerVolumeBright.setPlayerVolumeBrightImpl(new IPlayerVolumeBrightImpl() {

            @Override
            public void onComplete() {

            }
        });
        mPlayerBottom.setPlayerBottomImpl(new IPlayerBottomImpl() {
            @Override
            public void onPlayTurn() {
                if (mVideoView.isPlaying()) {
                    updatePlayState(false);
                } else {
                    updatePlayState(true);
                }
            }

            @Override
            public void onProgressChange(int state, int progress) {

            }

            @Override
            public void onOrientationChange() {

            }
        });
    }

    private void initVideoListener() {
        mVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                hideVideoLoading();
                updatePlayState(true);
                mPlayerController
                        .setGestureEnabled(true)
                        .setAutoControlPanel(true);//视频加载后才自动隐藏操作面板
            }
        });
        final ArrayList<Integer> audios = new ArrayList<>();
        //音频软解成功通知
        audios.add(IMediaPlayer.MEDIA_INFO_OPEN_INPUT);
        audios.add(IMediaPlayer.MEDIA_INFO_FIND_STREAM_INFO);
        audios.add(IMediaPlayer.MEDIA_INFO_COMPONENT_OPEN);
        audios.add(IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED);
        audios.add(IMediaPlayer.MEDIA_INFO_AUDIO_DECODED_START);
        audios.add(IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START);
        final ArrayList<Integer> temp_audios = new ArrayList<>();
        mVideoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
                Log.d(TAG, "onInfo: what= " + what + ", extra= " + extra);
                if (what == IMediaPlayer.MEDIA_INFO_OPEN_INPUT) {
                    temp_audios.clear();
                    temp_audios.add(what);
                } else if (temp_audios.size() < 6) {
                    temp_audios.add(what);
                }
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START://视频开始渲染
                        Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                        hideVideoLoading();
                        if (TextUtils.isEmpty(mVideoUri.getPath())) {
                        }
                        mPlayerBottom.getSeekBar().setEnabled(true);
                        mPlayerBottom.getIvPlayPause().setEnabled(true);
                        updatePlayState(true);
                        break;
                    case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START://音频开始渲染
                        Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                        hideVideoLoading();
                        if (TextUtils.isEmpty(mVideoUri.getPath())) {
                        }
                        mPlayerBottom.getSeekBar().setEnabled(true);
                        mPlayerBottom.getIvPlayPause().setEnabled(true);
                        updatePlayState(true);
                        if (!temp_audios.contains(IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START)) {
                            if (!TextUtils.isEmpty(mVideoCoverUrl)) {
//                                Glide.with(mContext)
//                                        .load(mVideoCoverUrl)
//                                        .fitCenter()
//                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                        .placeholder(R.drawable.default_image)
//                                        .error(R.drawable.default_image)
//                                        .into(video_cover);
                            }
                        }
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START://视频缓冲开始
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                        showVideoLoading();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (temp_audios.get(0) == IMediaPlayer.MEDIA_INFO_OPEN_INPUT) {
                                    for (int i = 0; i < temp_audios.size(); i++) {
                                        if (!audios.get(i).equals(temp_audios.get(i))) {
                                            onDestroyVideo();
                                            if (mVideoUri != null) {
                                                mVideoView.setVideoPath(mVideoUri.getPath());
                                                mVideoView.start();
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        });
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END://视频缓冲结束
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                        hideVideoLoading();
                        if (TextUtils.isEmpty(mVideoCoverUrl)) {
                        }
                        mPlayerBottom.getSeekBar().setEnabled(true);
                        mPlayerBottom.getIvPlayPause().setEnabled(true);
                        updatePlayState(true);
                        break;
                }
                return true;
            }
        });
        mVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                updatePlayState(true);
                mVideoView.release(false);
            }
        });
        mVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                hideVideoLoading();
                if (mPlayerController != null) {
                    mPlayerController
                            .setGestureEnabled(false)
                            .setAutoControlPanel(false);
                }
                //  判断网络状态,如果有网络,则重新加载播放,如果没有则报错
                if ((!mIsOnLocalSource && mNetworkAvailable) || mIsOnLocalSource) {
                    mVideoView.setVideoURI(mVideoUri);
                    startOrRestartPlay();
                } else {
                    if (mIPlayerImpl != null) {
                        mIPlayerImpl.onError();
                    }
                }
                return true;
            }
        });
    }

    private void startOrRestartPlay() {
        if (!mIsOnLocalSource && !mNetworkAvailable) {
            return;
        }
        showVideoLoading();
        mVideoView.start();
    }

    public void initPlayer() {
        mPlayerController = new PlayerController(mActivity, mVideoView)
                .setVideoParentLayout(mRlVideoParent)
                .setVideoController(mPlayerBottom.getSeekBar())
                .setVolumeController()
                .setBrightnessController()
                .setVideoParentRatio(IRenderView.AR_16_9_FIT_PARENT)
                .setVideoRatio(IRenderView.AR_16_9_FIT_PARENT)
                .setOnlyFullScreen(true)
                .setKeepScreenOn(true)
                .setAutoControlListener(mPlayerTitleBar, mPlayerBottom)
                .setPanelControl(new PlayerController.PanelControlListener() {
                    @Override
                    public void operatorPanel(boolean isShowControlPanel) {
                        if (isShowControlPanel) {
                            showOrHideBars(true, true);
                        } else {
                            showOrHideBars(false, true);
                        }
                    }
                })
                .setSyncProgressListener(new PlayerController.SyncProgressListener() {
                    @Override
                    public void syncTime(long position, long duration) {
                        mPlayerBottom.getTvCurrentTime().setText(mPlayerController.generateTime(position));
                        mPlayerBottom.getTvTotalTime().setText(mPlayerController.generateTime(duration));
                    }
                })
                .setGestureListener(new PlayerController.GestureListener() {
                    @Override
                    public void onProgressSlide(long newPosition, long duration, int showDelta) {
                        if (showDelta != 0) {
                            mPlayerVolumeBright.getLlVideoFastForward().setVisibility(View.VISIBLE);
                            mPlayerVolumeBright.getLlVideoBrightness().setVisibility(View.GONE);
                            mPlayerVolumeBright.getLlVideoVolume().setVisibility(View.GONE);
                            mPlayerVolumeBright.getTvVideoFastForwardTarget().setVisibility(View.VISIBLE);
                            mPlayerVolumeBright.getTvVideoFastForwardAll().setVisibility(View.VISIBLE);
                            mPlayerVolumeBright.getTvVideoFastForwardTarget().setText(mPlayerController.generateTime(newPosition) + "/");
                            mPlayerVolumeBright.getTvVideoFastForwardAll().setText(mPlayerController.generateTime(duration));

                            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
                            mPlayerVolumeBright.getTvVideoFastForward().setVisibility(View.VISIBLE);
                            mPlayerVolumeBright.getTvVideoFastForward().setText(String.format("%ss", text));
                        }
                    }

                    @Override
                    public void onVolumeSlide(int volume) {
                        mPlayerVolumeBright.getLlVideoFastForward().setVisibility(View.GONE);
                        mPlayerVolumeBright.getLlVideoBrightness().setVisibility(View.GONE);
                        mPlayerVolumeBright.getLlVideoVolume().setVisibility(View.VISIBLE);
                        mPlayerVolumeBright.getTvVideoVolume().setVisibility(View.VISIBLE);
                        mPlayerVolumeBright.getTvVideoVolume().setText(volume + "%");
                    }

                    @Override
                    public void onBrightnessSlide(float brightness) {
                        mPlayerVolumeBright.getLlVideoFastForward().setVisibility(View.GONE);
                        mPlayerVolumeBright.getLlVideoBrightness().setVisibility(View.VISIBLE);
                        mPlayerVolumeBright.getLlVideoVolume().setVisibility(View.GONE);
                        mPlayerVolumeBright.getTvVideoBrightness().setVisibility(View.VISIBLE);
                        mPlayerVolumeBright.getTvVideoBrightness().setText((int) (brightness * 100) + "%");
                    }

                    @Override
                    public void endGesture() {
                        mPlayerVolumeBright.getLlVideoFastForward().setVisibility(View.GONE);
                        mPlayerVolumeBright.getLlVideoBrightness().setVisibility(View.GONE);
                        mPlayerVolumeBright.getLlVideoVolume().setVisibility(View.GONE);
                    }
                });
    }

    /**
     * 初始化网络变化监听器
     */
    public void initNetworkMonitor() {
        unRegisterNetworkReceiver();
        // 网络变化
        mNetworkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // 网络变化
                if (intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    mNetworkAvailable = NetworkUtil.isNetworkConnected(mActivity);
                    mPlayerBottom.updateNetworkState(mNetworkAvailable || mIsOnLocalSource);
                    if (!mNetworkAvailable) {
                        getBufferProgress();
                    }
                }
            }
        };
    }

    private void registerNetworkReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mActivity.registerReceiver(mNetworkReceiver, filter);
    }

    public void unRegisterNetworkReceiver() {
        if (mNetworkReceiver != null) {
            mActivity.unregisterReceiver(mNetworkReceiver);
            mNetworkReceiver = null;
        }
    }

    /**
     * 初始化标题栏/控制栏显隐动画效果
     */
    private void initAnimation() {
        mEnterFromTop = AnimationUtils.loadAnimation(mContext, R.anim.enter_from_top);
        mEnterFromBottom = AnimationUtils.loadAnimation(mContext, R.anim.enter_from_bottom);
        mExitFromTop = AnimationUtils.loadAnimation(mContext, R.anim.exit_from_top);
        mExitFromBottom = AnimationUtils.loadAnimation(mContext, R.anim.exit_from_bottom);

        mEnterFromTop.setAnimationListener(new AnimationImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mPlayerTitleBar.setVisibility(VISIBLE);
            }
        });
        mEnterFromBottom.setAnimationListener(new AnimationImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mPlayerBottom.setVisibility(VISIBLE);
            }
        });
        mExitFromTop.setAnimationListener(new AnimationImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mPlayerTitleBar.setVisibility(GONE);
            }
        });
        mExitFromBottom.setAnimationListener(new AnimationImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mPlayerBottom.setVisibility(GONE);
            }
        });
    }

    /**
     * 显隐标题栏和控制条
     *
     * @param show          是否显示
     * @param animateEffect 是否需要动画效果
     */
    public void showOrHideBars(boolean show, boolean animateEffect) {
        if (animateEffect) {
            animateShowOrHideBars(show);
        } else {
            forceShowOrHideBars(show);
        }
    }

    /**
     * 直接显隐标题栏和控制栏
     */
    public void forceShowOrHideBars(boolean show) {
        mPlayerTitleBar.clearAnimation();
        mPlayerBottom.clearAnimation();

        if (show) {
            mPlayerBottom.setVisibility(VISIBLE);
            mPlayerTitleBar.setVisibility(VISIBLE);
        } else {
            mPlayerBottom.setVisibility(GONE);
            mPlayerTitleBar.setVisibility(GONE);
        }
    }

    /**
     * 带动画效果的显隐标题栏和控制栏
     */
    private void animateShowOrHideBars(boolean show) {
        mPlayerTitleBar.clearAnimation();
        mPlayerBottom.clearAnimation();

        if (show) {
            if (mPlayerTitleBar.getVisibility() != VISIBLE) {
                mPlayerTitleBar.startAnimation(mEnterFromTop);
                mPlayerBottom.startAnimation(mEnterFromBottom);
            }
        } else {
            if (mPlayerTitleBar.getVisibility() != GONE) {
                mPlayerTitleBar.startAnimation(mExitFromTop);
                mPlayerBottom.startAnimation(mExitFromBottom);
            }
        }
    }

    /**
     * @return 缓冲百分比 0-100
     */
    public int getBufferProgress() {
        if (mVideoView != null && mVideoView.getDuration() > 0) {
            return mIsOnLocalSource ? 0 : mVideoView.getBufferPercentage();
        } else {
            return 0;
        }
    }

    public IjkVideoView getVideoView() {
        return mVideoView;
    }

    public PlayerTitleBar getPlayerTitleBar() {
        return mPlayerTitleBar;
    }

    public PlayerVolumeBright getPlayerVolumeBright() {
        return mPlayerVolumeBright;
    }

    public PlayerBottom getPlayerBottom() {
        return mPlayerBottom;
    }

    public void showVideoLoading() {
        if (mLlLoading != null) {
            mLlLoading.setVisibility(View.VISIBLE);
            mTvSpeed.setVisibility(View.VISIBLE);
            mTvSpeed.setText("");
        }
    }

    public void hideVideoLoading() {
        if (mLlLoading != null) {
            mLlLoading.setVisibility(View.GONE);
            mTvSpeed.setVisibility(View.GONE);
            mTvSpeed.setText("");
        }
    }

    public boolean isPlaying() {
        if (mVideoView != null) {
            return mVideoView.isPlaying();
        }
        return false;
    }

    /**
     * 更新放按钮状态
     *
     * @param isPlay true：播放中；false：暂停或者未播放
     */
    public void updatePlayState(boolean isPlay) {
        if (isPlay) {
            // 播放
            mPlayerBottom.updatePlayState(true);
            mVideoView.start();
        } else {
            // 暂停
            mPlayerBottom.updatePlayState(false);
            mVideoView.pause();
        }
    }

    /**
     *  更新SeekBar的进度
     * @param position
     */
    public void updateProgress(int position) {
        mPlayerController.updateProgress(position, mVideoView.getDuration());
    }

    /**
     * 发送message给handler,自动隐藏标题栏
     */
    public void sendAutoHideBarsMsg() {
        if(mPlayerController != null) {
            mPlayerController.sendAutoHideBarsMsg(5000);
        }
    }

    //////////////////////////////////////////开放方法///////////////////////////////////////////////

    public void stopPlayback() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }

    public void onDestroyVideo() {
        unRegisterNetworkReceiver();
        if (mPlayerController != null) {
            mPlayerController.onDestroy();
        }
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        }
    }

    public void onConfigurationChanged() {
        if (mPlayerController != null) {
            mPlayerController.onConfigurationChanged();
        }
    }

    //////////////////////////////////////////set方法///////////////////////////////////////////////

    /**
     * 设置视频标题
     */
    public void setTitle(String title) {
        mPlayerTitleBar.setTitle(title);
    }

    /**
     * 设置封面
     *
     * @param coverUrl
     */
    public void setVideoCoverUrl(String coverUrl) {
        mVideoCoverUrl = coverUrl;
    }

    /**
     * 设置进度条样式
     *
     * @param resId 进度条progressDrawable分层资源
     *              数组表示的进度资源分别为 background - secondaryProgress - progress
     *              若对应的数组元素值小于等于0,表示该层素材保持不变;
     *              注意:progress和secondaryProgress的shape资源需要做成clip的,否则会直接完全显示
     */
    public void setProgressLayerDrawables(@DrawableRes int resId) {
        mPlayerBottom.setProgressLayerDrawables(resId);
    }

    /**
     * 设置进度条按钮图片
     *
     * @param thumbId
     */
    public void setProgressThumbDrawable(@DrawableRes int thumbId) {
        mPlayerBottom.setProgressThumbDrawable(thumbId);
    }

    /**
     * 设置暂停按钮图标
     *
     * @param iconPause
     */
    public void setIconPause(@DrawableRes int iconPause) {
        mPlayerBottom.setIconPause(iconPause);
    }

    /**
     * 设置播放按钮图标
     *
     * @param iconPlay
     */
    public void setIconPlay(@DrawableRes int iconPlay) {
        mPlayerBottom.setIconPlay(iconPlay);
    }

    /**
     * 设置退出全屏按钮
     *
     * @param iconShrink
     */
    public void setIconShrink(@DrawableRes int iconShrink) {
        mPlayerBottom.setIconShrink(iconShrink);
    }

    /**
     * 设置退出全屏按钮
     *
     * @param iconExpand
     */
    public void setIconExpand(@DrawableRes int iconExpand) {
        mPlayerBottom.setIconExpand(iconExpand);
    }

    /**
     * 设置加载提示框图标资源
     */
    public void setIconLoading(@DrawableRes int iconLoading) {
        if (Build.VERSION.SDK_INT >= 21) {
            mProgressBar.setIndeterminateDrawable(getResources().getDrawable(iconLoading, null));
        } else {
            mProgressBar.setIndeterminateDrawable(getResources().getDrawable(iconLoading));
        }

    }
}
