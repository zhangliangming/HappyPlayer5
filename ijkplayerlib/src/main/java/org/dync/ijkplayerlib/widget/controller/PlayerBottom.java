package org.dync.ijkplayerlib.widget.controller;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.dync.ijkplayerlib.R;
import org.dync.ijkplayerlib.widget.controller.impl.IPlayerBottomImpl;
import org.dync.ijkplayerlib.widget.media.IjkVideoView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by KathLine on 2017/12/13.
 */

public class PlayerBottom extends RelativeLayout implements View.OnClickListener {

    private Context mContext;
    private IPlayerBottomImpl mIPlayerBottom;
    private Activity mActivity;
    private IjkVideoView mVideoView;
    private SeekBar mSeekBar;
    private RelativeLayout mRlPlayPause;
    private ImageView mIvPlayPause;
    private TextView mTvCurrentTime;
    private TextView mTvTotalTime;
    private ImageView mIvToggleExpandable;
    private int mDuration = 0;//视频长度(ms)
    private SimpleDateFormat mFormatter = null;
    private static final String ZERO_TIME = "00:00";
    private boolean mUserOperateSeecbar = false;//用户是否正在操作进度条
    private static final String TAG = "PlayerController";

    private int iconPause = R.drawable.player_pause;
    private int iconPlay = R.drawable.player_play;

    int iconShrink = R.drawable.player_shrink;
    int iconExpand = R.drawable.player_expand;
    private RelativeLayout mRlToggleExpandable;

    public PlayerBottom(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public PlayerBottom(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PlayerBottom(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayerBottom(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context){
        mContext = context;
        inflate(context, R.layout.video_bottom, this);

        mRlPlayPause = findViewById(R.id.rl_play_pause);
        mIvPlayPause = (ImageView) findViewById(R.id.iv_play_pause);
        mTvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        mTvTotalTime = (TextView) findViewById(R.id.tv_total_time);

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        mRlToggleExpandable = findViewById(R.id.rl_toggle_expandable);
        mIvToggleExpandable = (ImageView) findViewById(R.id.iv_toggle_expandable);

        initListener();
    }

    private void initListener() {
        mRlPlayPause.setOnClickListener(this);
        mIvPlayPause.setOnClickListener(this);
        mRlToggleExpandable.setOnClickListener(this);
        mIvToggleExpandable.setOnClickListener(this);
    }

    public void setPlayerBottomImpl(IPlayerBottomImpl playerBottom) {
        mIPlayerBottom = playerBottom;
    }

    @SuppressLint("SimpleDateFormat")
    private void initFormatter(int maxValue) {
        if (mFormatter == null) {
            if (maxValue >= (59 * 60 * 1000 + 59 * 1000)) {
                mFormatter = new SimpleDateFormat("HH:mm:ss");
            } else {
                mFormatter = new SimpleDateFormat("mm:ss");
            }
            mFormatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String formatPlayTime(long time) {
        if (time <= 0) {
            return ZERO_TIME;
        }

        if (mFormatter == null) {
            initFormatter(mDuration);
        }
        String timeStr = mFormatter.format(new Date(time));
        if (TextUtils.isEmpty(timeStr)) {
            timeStr = ZERO_TIME;
        }
        return timeStr;
    }

    /**
     * 设置隐藏显示切换横竖屏的按钮
     * @param isShow
     */
    public void toggleExpandable(boolean isShow){
        mIvToggleExpandable.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void updateNetworkState(boolean isAvailable) {
        mSeekBar.setEnabled(isAvailable);
    }

    /**
     * 更新播放按钮状态
     * @param curPlayState  true：播放中；false：暂停或者未播放
     */
    public void updatePlayState(boolean curPlayState) {
        if(curPlayState) {
            mIvPlayPause.setImageResource(iconPause);
        }else {
            mIvPlayPause.setImageResource(iconPlay);
        }
    }

    /**
     * 设置暂停按钮图标
     */
    public void setIconPause(@DrawableRes int iconPause) {
        this.iconPause = iconPause;
        mIvPlayPause.setImageResource(iconPause);
    }

    /**
     * 设置播放按钮图标
     */
    public void setIconPlay(@DrawableRes int iconPlay) {
        this.iconPlay = iconPlay;
        mIvPlayPause.setImageResource(iconPlay);
    }

    /**
     * 设置退出全屏按钮
     */
    public void setIconShrink(@DrawableRes int iconShrink) {
        this.iconShrink = iconShrink;
//        if (mCurOrientation == OrientationUtil.HORIZONTAL) {
//            mIvToggleExpandable.setImageResource(iconShrink);
//        }
    }

    /**
     * 设置退出全屏按钮
     */
    public void setIconExpand(@DrawableRes int iconExpand) {
        this.iconExpand = iconExpand;
//        if (mCurOrientation == OrientationUtil.VERTICAL) {
//            mIvToggleExpandable.setImageResource(iconExpand);
//        }
    }

    /**
     * 设置进度条样式
     *
     * @param resId 进度条progressDrawable分层资源
     *              数组表示的进度资源分别为 background - secondaryProgress - progress
     *              若对应的数组元素值 <=0,表示该层素材保持不变;
     *              注意:progress和secondaryProgress的shape资源需要做成clip的,否则会直接完全显示
     */
    public void setProgressLayerDrawables(@DrawableRes int resId) {
        if (mSeekBar != null) {
            Drawable drawable = getResources().getDrawable(resId);
            mSeekBar.setProgressDrawable(drawable);
        }
    }

    /**
     * 设置进度条按钮图片
     */
    public void setProgressThumbDrawable(@DrawableRes int thumbId) {
        if (thumbId > 0) {
            Drawable drawable = getResources().getDrawable(thumbId);
            if (drawable != null && mSeekBar != null) {
                mSeekBar.setThumb(drawable);
            }
        }
    }

    /**
     * 隐藏时间进度和总时间信息
     */
    public void hideTimes() {
        mTvCurrentTime.setVisibility(GONE);
        mTvTotalTime.setVisibility(GONE);
    }

    /**
     * 显示时间进度和总时间信息
     */
    public void showTimes() {
        mTvCurrentTime.setVisibility(VISIBLE);
        mTvTotalTime.setVisibility(VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if(mIPlayerBottom == null) {
            return;
        }
        int id = v.getId();
        if (id == R.id.rl_play_pause || id == R.id.iv_play_pause) {
            mIPlayerBottom.onPlayTurn();
        } else if (id == R.id.iv_toggle_expandable || id == R.id.rl_toggle_expandable) {
            mIPlayerBottom.onOrientationChange();
        }
    }

    /////////////////////////////////////////////////////get方法/////////////////////////////////////

    public SeekBar getSeekBar() {
        return mSeekBar;
    }

    public ImageView getIvPlayPause() {
        return mIvPlayPause;
    }

    public TextView getTvCurrentTime() {
        return mTvCurrentTime;
    }

    public TextView getTvTotalTime() {
        return mTvTotalTime;
    }

    public ImageView getIvToggleExpandable() {
        return mIvToggleExpandable;
    }

}
